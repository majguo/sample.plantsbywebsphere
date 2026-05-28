package com.ibm.websphere.samples.daytrader.streaming;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;

@Component
public class MarketSummaryPublisher {

    private final TradeServicesFacade tradeServicesFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final RuntimeSettingsService runtimeSettings;
    private volatile long nextPublicationAt;

    public MarketSummaryPublisher(
            TradeServicesFacade tradeServicesFacade,
            ApplicationEventPublisher eventPublisher,
            RuntimeSettingsService runtimeSettings) {
        this.tradeServicesFacade = tradeServicesFacade;
        this.eventPublisher = eventPublisher;
        this.runtimeSettings = runtimeSettings;
    }

    @Scheduled(fixedDelayString = "${daytrader.streaming.market-summary-publisher-tick-ms:1000}")
    public void publishSummary() {
        publishSummary(System.currentTimeMillis());
    }

    void publishSummary(long currentTimeMillis) {
        int intervalSeconds = runtimeSettings.getMarketSummaryInterval();
        if (intervalSeconds < 0) {
            return;
        }
        if (intervalSeconds > 0 && currentTimeMillis < nextPublicationAt) {
            return;
        }

        eventPublisher.publishEvent(new MarketSummaryUpdatedEvent(tradeServicesFacade.getMarketSummary()));

        if (intervalSeconds > 0) {
            nextPublicationAt = currentTimeMillis + (intervalSeconds * 1000L);
        }
    }
}