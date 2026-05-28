package com.ibm.websphere.samples.daytrader.persistence.jpa;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.websphere.samples.daytrader.entities.OrderWorkItemEntity;

public interface OrderWorkItemJpaRepository extends JpaRepository<OrderWorkItemEntity, Integer> {

    Optional<OrderWorkItemEntity> findByOrderId(Integer orderId);

    List<OrderWorkItemEntity> findTop10ByStatusInAndAvailableAtLessThanEqualOrderByAvailableAtAscWorkIdAsc(
            Collection<String> statuses,
            Date availableAt);
}