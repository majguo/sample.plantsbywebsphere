package com.ibm.websphere.samples.daytrader.web.mvc;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class MarketSummaryHandshakeInterceptor implements HandshakeInterceptor {

    private final CompatibilitySessionFacade sessionFacade;

    public MarketSummaryHandshakeInterceptor(CompatibilitySessionFacade sessionFacade) {
        this.sessionFacade = sessionFacade;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            java.util.Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        HttpServletRequest httpRequest = servletRequest.getServletRequest();
        if (!sessionFacade.hasAuthenticatedUser(httpRequest.getSession(false))) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        if (!isSameOrigin(request)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }
        attributes.put("uidBean", sessionFacade.getUserId(httpRequest.getSession(false)));
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }

    private boolean isSameOrigin(ServerHttpRequest request) {
        String origin = request.getHeaders().getFirst(HttpHeaders.ORIGIN);
        if (origin == null || origin.isBlank()) {
            return true;
        }
        URI requestUri = request.getURI();
        URI originUri = URI.create(origin);
        return originUri.getHost() != null
                && originUri.getHost().equalsIgnoreCase(requestUri.getHost())
                && effectivePort(originUri) == effectivePort(requestUri);
    }

    private int effectivePort(URI uri) {
        if (uri.getPort() > 0) {
            return uri.getPort();
        }
        return switch (uri.getScheme()) {
            case "https", "wss" -> 443;
            default -> 80;
        };
    }
}