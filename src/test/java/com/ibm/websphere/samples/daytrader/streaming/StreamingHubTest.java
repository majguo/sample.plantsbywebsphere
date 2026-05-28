package com.ibm.websphere.samples.daytrader.streaming;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;

class StreamingHubTest {

    @Test
    void quotePublishRemovesDisconnectedEmitterWithoutThrowing() throws Exception {
        RecentQuotePriceChangeList recentQuotePriceChangeList = new RecentQuotePriceChangeList(
            mock(ApplicationEventPublisher.class));
        StreamingHub streamingHub = new StreamingHub(
                new ObjectMapper(),
                recentQuotePriceChangeList,
                mock(TradeServicesFacade.class));
        SseEmitter emitter = mock(SseEmitter.class);

        doThrow(new IOException("disconnected")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));
        doThrow(new IllegalStateException("already completed")).when(emitter).complete();

        streamingHub.registerEmitterForTest(emitter);

        assertDoesNotThrow(() -> streamingHub.onQuotePriceChange(new QuotePriceChangeEvent(List.of(quote()))));
        verify(emitter).complete();
    }

    private QuoteDataBean quote() {
        QuoteDataBean quote = new QuoteDataBean();
        quote.setSymbol("s:1");
        quote.setCompanyName("s:1");
        quote.setPrice(new BigDecimal("12.34"));
        quote.setOpen(new BigDecimal("12.34"));
        quote.setLow(new BigDecimal("12.34"));
        quote.setHigh(new BigDecimal("12.34"));
        quote.setChange(1.25d);
        quote.setVolume(10d);
        return quote;
    }
}