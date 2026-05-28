package com.ibm.websphere.samples.daytrader.integration.streaming;

import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.test.context.ActiveProfiles;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.boot.DayTraderApplication;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.streaming.MarketSummaryUpdatedEvent;
import com.ibm.websphere.samples.daytrader.support.AbstractDayTraderIntegrationTestSupport;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;

@SpringBootTest(classes = DayTraderApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DayTraderStreamingIntegrationTest extends AbstractDayTraderIntegrationTestSupport {

    @LocalServerPort
    private int port;

    @Autowired
    private RecentQuotePriceChangeList recentQuotePriceChangeList;

    @Autowired
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    private HttpClient httpClient;

    @BeforeEach
    void setUpClient() throws Exception {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        httpClient = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        when(tradeServicesFacade.login("uid:1", "secret")).thenReturn(account("uid:1"));
        when(tradeServicesFacade.getAccountData("uid:1")).thenReturn(account("uid:1"));
        when(tradeServicesFacade.getMarketSummary()).thenReturn(emptySummary());
    }

    @Test
    void sseEndpointEmitsImmediateWelcomeFrame() throws Exception {
        authenticateSession();

        HttpRequest request = HttpRequest.newBuilder(baseUri("/rest/broadcastevents"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "text/event-stream")
                .GET()
                .build();

        HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

        org.junit.jupiter.api.Assertions.assertEquals(200, response.statusCode());
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
        String payload = reader.lines()
                .filter(line -> line.startsWith("data:"))
                .findFirst()
                .orElse("");
        org.junit.jupiter.api.Assertions.assertTrue(payload.contains("welcome!"), payload);
        response.body().close();
    }

    @Test
    void websocketEndpointReturnsRecentQuoteChangesAndMarketSummaryPayloads() throws Exception {
        authenticateSession();

        QuoteDataBean quote = quote("s:1", "s:1", new BigDecimal("12.34"), 1.25d);
        recentQuotePriceChangeList.add(quote);

        MarketSummaryDataBean summary = new MarketSummaryDataBean(
                new BigDecimal("15.00"),
                new BigDecimal("14.00"),
                5000d,
                List.of(quote),
                List.of(quote("s:2", "s:2", new BigDecimal("9.99"), -0.75d)));
        when(tradeServicesFacade.getMarketSummary()).thenReturn(summary);

        TestWebSocketListener listener = new TestWebSocketListener();
        WebSocket webSocket = httpClient.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(baseWsUri("/marketsummary"), listener)
                .get(5, TimeUnit.SECONDS);

        webSocket.sendText("{\"action\":\"updateRecentQuotePriceChange\"}", true).get(5, TimeUnit.SECONDS);
        String recentPayload = listener.awaitMessage();
        org.junit.jupiter.api.Assertions.assertTrue(recentPayload.contains("change1_stock"), recentPayload);
        org.junit.jupiter.api.Assertions.assertTrue(recentPayload.contains("s:1"), recentPayload);

        webSocket.sendText("{\"action\":\"updateMarketSummary\"}", true).get(5, TimeUnit.SECONDS);
        String summaryPayload = listener.awaitMessage();
        org.junit.jupiter.api.Assertions.assertTrue(summaryPayload.contains("gainer1_stock"), summaryPayload);
        org.junit.jupiter.api.Assertions.assertTrue(summaryPayload.contains("tsia"), summaryPayload);

        eventPublisher.publishEvent(new MarketSummaryUpdatedEvent(summary));
        String pushedPayload = listener.awaitMessage();
        org.junit.jupiter.api.Assertions.assertTrue(pushedPayload.contains("volume"), pushedPayload);

        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").get(5, TimeUnit.SECONDS);
    }

    @Test
    void alternateSurfacesRemainReachableOnTheRunningBootServer() throws Exception {
        HttpResponse<String> welcomeFaces = httpClient.send(
                HttpRequest.newBuilder(baseUri("/welcome.faces"))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        HttpResponse<String> primitiveServlet = httpClient.send(
                HttpRequest.newBuilder(baseUri("/servlet/PingServlet"))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString());

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK.value(), welcomeFaces.statusCode());
        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK.value(), primitiveServlet.statusCode());
        org.junit.jupiter.api.Assertions.assertTrue(primitiveServlet.body().contains("Ping Servlet"), primitiveServlet.body());
    }

    private URI baseUri(String path) {
        return URI.create("http://localhost:" + port + "/daytrader" + path);
    }

        private void authenticateSession() throws Exception {
        String loginBody = UriComponentsBuilder.newInstance()
            .queryParam("action", "login")
            .queryParam("uid", "uid:1")
            .queryParam("passwd", "secret")
            .build()
            .getQuery();

        HttpResponse<String> loginResponse = httpClient.send(
            HttpRequest.newBuilder(baseUri("/app"))
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                .build(),
            HttpResponse.BodyHandlers.ofString());

        org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.OK.value(), loginResponse.statusCode());
        }

    private URI baseWsUri(String path) {
        return URI.create("ws://localhost:" + port + "/daytrader" + path);
    }

    private static final class TestWebSocketListener implements WebSocket.Listener {

        private final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        private final StringBuilder currentText = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            currentText.append(data);
            if (last) {
                messages.offer(currentText.toString());
                currentText.setLength(0);
            }
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            webSocket.request(1);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            messages.offer("");
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            messages.offer("ERROR: " + error.getMessage());
        }

        String awaitMessage() throws Exception {
            String message = messages.poll(5, TimeUnit.SECONDS);
            org.junit.jupiter.api.Assertions.assertNotNull(message, "Timed out waiting for WebSocket payload");
            return message;
        }
    }
}