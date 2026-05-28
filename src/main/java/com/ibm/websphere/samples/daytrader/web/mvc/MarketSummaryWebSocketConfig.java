package com.ibm.websphere.samples.daytrader.web.mvc;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class MarketSummaryWebSocketConfig implements WebSocketConfigurer {

    private final MarketSummaryWebSocketHandler handler;

    public MarketSummaryWebSocketConfig(MarketSummaryWebSocketHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/marketsummary").setAllowedOriginPatterns("*");
    }
}