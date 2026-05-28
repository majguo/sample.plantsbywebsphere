package com.ibm.websphere.samples.daytrader.streaming;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;

@Component
public class StreamingHub {

    private final ObjectMapper objectMapper;
    private final RecentQuotePriceChangeList recentQuotePriceChangeList;
    private final TradeServicesFacade tradeServicesFacade;
    private final Set<SseEmitter> emitters = ConcurrentHashMap.newKeySet();
    private final Set<WebSocketSession> marketSummarySessions = ConcurrentHashMap.newKeySet();

    public StreamingHub(
            ObjectMapper objectMapper,
            RecentQuotePriceChangeList recentQuotePriceChangeList,
            TradeServicesFacade tradeServicesFacade) {
        this.objectMapper = objectMapper;
        this.recentQuotePriceChangeList = recentQuotePriceChangeList;
        this.tradeServicesFacade = tradeServicesFacade;
    }

    public SseEmitter registerBroadcastEmitter() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            emitter.complete();
        });
        emitter.onError(error -> emitters.remove(emitter));

        try {
            if (recentQuotePriceChangeList.isEmpty()) {
                emitter.send(SseEmitter.event().data("welcome!"));
            } else {
                emitter.send(SseEmitter.event().data(recentQuoteArrayJson()));
            }
        } catch (IOException sendFailure) {
            discardEmitter(emitter);
        }
        return emitter;
    }

    public void registerMarketSummarySession(WebSocketSession session) {
        marketSummarySessions.add(session);
    }

    public void unregisterMarketSummarySession(WebSocketSession session) {
        marketSummarySessions.remove(session);
    }

    void registerEmitterForTest(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public void handleMarketSummaryMessage(WebSocketSession session, String payload) throws IOException {
        JsonNode message = objectMapper.readTree(payload);
        String action = message.path("action").asText(null);
        if ("updateMarketSummary".equals(action)) {
            sendText(session, marketSummaryJson(tradeServicesFacade.getMarketSummary()));
            return;
        }
        if ("updateRecentQuotePriceChange".equals(action)) {
            sendText(session, recentQuotePriceChangeJson());
        }
    }

    @EventListener
    public void onQuotePriceChange(QuotePriceChangeEvent event) {
        String ssePayload = recentQuoteArrayJson(event.recentQuotes());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(ssePayload));
            } catch (IOException sendFailure) {
                discardEmitter(emitter);
            }
        }

        broadcastToMarketSummarySessions(recentQuotePriceChangeJson(event.recentQuotes()));
    }

    @EventListener
    public void onMarketSummaryUpdated(MarketSummaryUpdatedEvent event) {
        broadcastToMarketSummarySessions(marketSummaryJson(event.summary()));
    }

    private void broadcastToMarketSummarySessions(String payload) {
        for (WebSocketSession session : marketSummarySessions) {
            sendText(session, payload);
        }
    }

    private void sendText(WebSocketSession session, String payload) {
        if (!session.isOpen()) {
            marketSummarySessions.remove(session);
            return;
        }
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException sendFailure) {
            marketSummarySessions.remove(session);
            try {
                session.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void discardEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
        try {
            emitter.complete();
        } catch (IllegalStateException ignored) {
        }
    }

    private String recentQuoteArrayJson() {
        return recentQuoteArrayJson(recentQuotePriceChangeList.recentList());
    }

    private String recentQuoteArrayJson(Collection<QuoteDataBean> quotes) {
        try {
            return objectMapper.writeValueAsString(quotes);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize recent quote changes", e);
        }
    }

    private String recentQuotePriceChangeJson() {
        return recentQuotePriceChangeJson(recentQuotePriceChangeList.recentList());
    }

    private String recentQuotePriceChangeJson(Collection<QuoteDataBean> quotes) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        int i = 1;
        for (QuoteDataBean quote : quotes) {
            payload.put("change" + i + "_stock", quote.getSymbol());
            payload.put("change" + i + "_price", money(quote.getPrice()));
            payload.put("change" + i + "_change", quote.getChange());
            i++;
        }
        return writeJson(payload);
    }

    private String marketSummaryJson(MarketSummaryDataBean summary) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        int i = 1;
        for (QuoteDataBean quote : summary.getTopGainers()) {
            payload.put("gainer" + i + "_stock", quote.getSymbol());
            payload.put("gainer" + i + "_price", money(quote.getPrice()));
            payload.put("gainer" + i + "_change", quote.getChange());
            i++;
        }
        i = 1;
        for (QuoteDataBean quote : summary.getTopLosers()) {
            payload.put("loser" + i + "_stock", quote.getSymbol());
            payload.put("loser" + i + "_price", money(quote.getPrice()));
            payload.put("loser" + i + "_change", quote.getChange());
            i++;
        }
        payload.put("tsia", summary.getTSIA());
        payload.put("volume", summary.getVolume());
        payload.put("date", summary.getSummaryDate().toString());
        return writeJson(payload);
    }

    private String money(BigDecimal amount) {
        return "$" + amount;
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize streaming payload", e);
        }
    }
}