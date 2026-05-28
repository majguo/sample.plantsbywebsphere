package com.ibm.websphere.samples.daytrader.streaming;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;

@Component
public class MarketSummaryPublisher {

    private final TradeServicesFacade tradeServicesFacade;
    private final ApplicationEventPublisher eventPublisher;

    public MarketSummaryPublisher(
            TradeServicesFacade tradeServicesFacade,
            ApplicationEventPublisher eventPublisher) {
        this.tradeServicesFacade = tradeServicesFacade;
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(fixedDelayString = "${daytrader.streaming.market-summary-publish-delay-ms:5000}")
    public void publishSummary() {
        eventPublisher.publishEvent(new MarketSummaryUpdatedEvent(tradeServicesFacade.getMarketSummary()));
    }
}