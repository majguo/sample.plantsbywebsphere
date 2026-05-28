package com.ibm.websphere.samples.daytrader.application.orders;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.entities.OrderWorkItemEntity;
import com.ibm.websphere.samples.daytrader.persistence.jpa.OrderWorkItemJpaRepository;

@Component
public class OrderWorkProcessor {

    private final OrderWorkItemJpaRepository orderWorkItemRepository;
    private final TradeOrderApplicationService tradeOrderService;
    private final long retryDelayMs;
    private final int batchSize;
    private final int maxAttempts;

    public OrderWorkProcessor(
            OrderWorkItemJpaRepository orderWorkItemRepository,
            TradeOrderApplicationService tradeOrderService,
            @Value("${daytrader.async.retry-delay-ms:2000}") long retryDelayMs,
            @Value("${daytrader.async.batch-size:10}") int batchSize,
            @Value("${daytrader.async.max-attempts:5}") int maxAttempts) {
        this.orderWorkItemRepository = orderWorkItemRepository;
        this.tradeOrderService = tradeOrderService;
        this.retryDelayMs = retryDelayMs;
        this.batchSize = batchSize;
        this.maxAttempts = maxAttempts;
    }

    @Scheduled(fixedDelayString = "${daytrader.async.worker-delay-ms:250}")
    public void processPendingOrders() {
        List<OrderWorkItemEntity> workItems = orderWorkItemRepository
                .findTop10ByStatusInAndAvailableAtLessThanEqualOrderByAvailableAtAscWorkIdAsc(
                        List.of(OrderWorkItemEntity.STATUS_PENDING, OrderWorkItemEntity.STATUS_RETRY),
                        new Date(System.currentTimeMillis()));
        int processed = 0;
        for (OrderWorkItemEntity workItem : workItems) {
            if (processed >= batchSize) {
                return;
            }
            processWorkItem(workItem.getWorkId());
            processed++;
        }
    }

    public void processWorkItem(Integer workId) {
        OrderWorkItemEntity workItem = orderWorkItemRepository.findById(workId).orElse(null);
        if (workItem == null) {
            return;
        }
        if (!(OrderWorkItemEntity.STATUS_PENDING.equals(workItem.getStatus())
                || OrderWorkItemEntity.STATUS_RETRY.equals(workItem.getStatus()))) {
            return;
        }

        Date now = new Date(System.currentTimeMillis());
        workItem.setAttemptCount(workItem.getAttemptCount() + 1);
        workItem.setUpdatedAt(now);
        workItem.setMaxAttempts(Math.max(workItem.getMaxAttempts(), maxAttempts));
        orderWorkItemRepository.save(workItem);

        try {
            tradeOrderService.completeOrder(workItem.getOrderId(), workItem.isTwoPhase());
            markCompleted(workId, now);
        } catch (IllegalStateException duplicateCompletion) {
            if (duplicateCompletion.getMessage() != null && duplicateCompletion.getMessage().contains("already completed")) {
                markCompleted(workId, now);
                return;
            }
            reschedule(workId, duplicateCompletion, now);
        } catch (Exception failure) {
            reschedule(workId, failure, now);
        }
    }

    @Transactional
    protected void markCompleted(Integer workId, Date now) {
        OrderWorkItemEntity workItem = orderWorkItemRepository.findById(workId).orElseThrow();
        workItem.setStatus(OrderWorkItemEntity.STATUS_COMPLETED);
        workItem.setLastError(null);
        workItem.setAvailableAt(now);
        workItem.setUpdatedAt(now);
    }

    @Transactional
    protected void reschedule(Integer workId, Exception failure, Date now) {
        OrderWorkItemEntity workItem = orderWorkItemRepository.findById(workId).orElseThrow();
        workItem.setLastError(failure.getMessage());
        workItem.setUpdatedAt(now);
        if (workItem.getAttemptCount() >= workItem.getMaxAttempts()) {
            workItem.setStatus(OrderWorkItemEntity.STATUS_FAILED);
            workItem.setAvailableAt(now);
            return;
        }
        workItem.setStatus(OrderWorkItemEntity.STATUS_RETRY);
        workItem.setAvailableAt(new Date(now.getTime() + retryDelayMs));
    }
}