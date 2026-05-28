package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.ibm.websphere.samples.daytrader.streaming.StreamingHub;

@Component
public class MarketSummaryWebSocketHandler extends TextWebSocketHandler {

    private final StreamingHub streamingHub;

    public MarketSummaryWebSocketHandler(StreamingHub streamingHub) {
        this.streamingHub = streamingHub;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        streamingHub.registerMarketSummarySession(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        streamingHub.handleMarketSummaryMessage(session, message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        streamingHub.unregisterMarketSummarySession(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        streamingHub.unregisterMarketSummarySession(session);
    }
}