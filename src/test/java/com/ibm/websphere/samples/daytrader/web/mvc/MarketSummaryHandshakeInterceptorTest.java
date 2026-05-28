package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.socket.WebSocketHandler;

class MarketSummaryHandshakeInterceptorTest {

    private final CompatibilitySessionFacade sessionFacade = new CompatibilitySessionFacade();
    private final MarketSummaryHandshakeInterceptor interceptor = new MarketSummaryHandshakeInterceptor(sessionFacade);

    @Test
    void rejectsAnonymousHandshakeRequests() {
        MockHttpServletRequest servletRequest = request("localhost", 9080, null);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        Map<String, Object> attributes = new HashMap<String, Object>();

        boolean allowed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse),
                mock(WebSocketHandler.class),
                attributes);

        assertFalse(allowed);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), servletResponse.getStatus());
        assertTrue(attributes.isEmpty());
    }

    @Test
    void rejectsCrossOriginHandshakeRequests() {
        MockHttpServletRequest servletRequest = request("localhost", 9080, "http://evil.example:9080");
        servletRequest.getSession(true).setAttribute("uidBean", "uid:1");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        Map<String, Object> attributes = new HashMap<String, Object>();

        boolean allowed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse),
                mock(WebSocketHandler.class),
                attributes);

        assertFalse(allowed);
        assertEquals(HttpStatus.FORBIDDEN.value(), servletResponse.getStatus());
        assertTrue(attributes.isEmpty());
    }

    @Test
    void acceptsAuthenticatedSameOriginHandshakeRequests() {
        MockHttpServletRequest servletRequest = request("localhost", 9080, "http://localhost:9080");
        servletRequest.getSession(true).setAttribute("uidBean", "uid:1");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        Map<String, Object> attributes = new HashMap<String, Object>();

        boolean allowed = interceptor.beforeHandshake(
                new ServletServerHttpRequest(servletRequest),
                new ServletServerHttpResponse(servletResponse),
                mock(WebSocketHandler.class),
                attributes);

        assertTrue(allowed);
        assertEquals(200, servletResponse.getStatus());
        assertEquals("uid:1", attributes.get("uidBean"));
    }

    private MockHttpServletRequest request(String serverName, int serverPort, String origin) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/marketsummary");
        request.setScheme("http");
        request.setServerName(serverName);
        request.setServerPort(serverPort);
        if (origin != null) {
            request.addHeader("Origin", origin);
        }
        return request;
    }
}