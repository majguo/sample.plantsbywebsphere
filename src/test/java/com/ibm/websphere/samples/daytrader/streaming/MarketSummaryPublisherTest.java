package com.ibm.websphere.samples.daytrader.streaming;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;

@ExtendWith(MockitoExtension.class)
class MarketSummaryPublisherTest {

    @Mock
    private TradeServicesFacade tradeServicesFacade;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private RuntimeSettingsService runtimeSettings;

    @Test
    void usesRuntimeSettingsIntervalToThrottlePublicationCadence() {
        when(runtimeSettings.getMarketSummaryInterval()).thenReturn(5);

        MarketSummaryPublisher publisher = new MarketSummaryPublisher(tradeServicesFacade, eventPublisher, runtimeSettings);

        publisher.publishSummary(1_000L);
        publisher.publishSummary(4_000L);
        publisher.publishSummary(6_000L);

        verify(tradeServicesFacade, times(2)).getMarketSummary();
        verify(eventPublisher, times(2)).publishEvent(org.mockito.ArgumentMatchers.any(MarketSummaryUpdatedEvent.class));
    }

    @Test
    void skipsPeriodicPublicationWhenOperatorDisablesCachingInterval() {
        when(runtimeSettings.getMarketSummaryInterval()).thenReturn(-1);

        MarketSummaryPublisher publisher = new MarketSummaryPublisher(tradeServicesFacade, eventPublisher, runtimeSettings);

        publisher.publishSummary(1_000L);

        verifyNoInteractions(tradeServicesFacade, eventPublisher);
    }
}