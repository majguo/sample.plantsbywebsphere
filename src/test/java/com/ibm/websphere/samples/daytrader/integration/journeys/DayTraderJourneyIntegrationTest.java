package com.ibm.websphere.samples.daytrader.integration.journeys;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.ibm.websphere.samples.daytrader.boot.DayTraderApplication;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.support.AbstractDayTraderIntegrationTestSupport;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@SpringBootTest(classes = DayTraderApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DayTraderJourneyIntegrationTest extends AbstractDayTraderIntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginHomeAndLogoutJourneyPreservesCompatibilitySessionMarkers() throws Exception {
        AccountDataBean account = account("uid:1");
        when(tradeServicesFacade.login("uid:1", "secret")).thenReturn(account);
        when(tradeServicesFacade.getAccountData("uid:1")).thenReturn(account);

        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/app")
                        .param("action", "login")
                        .param("uid", "uid:1")
                        .param("passwd", "secret"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/tradehome.jsp"))
                .andExpect(request().sessionAttribute("uidBean", "uid:1"))
                .andExpect(request().sessionAttribute("sessionCreationDate", org.hamcrest.Matchers.notNullValue()))
                .andReturn()
                .getRequest()
                .getSession(false);

        mockMvc.perform(get("/app")
                        .session(session)
                        .param("action", "home"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/tradehome.jsp"))
                .andExpect(model().attribute("results", "Ready to Trade"));

        mockMvc.perform(post("/app")
                        .session(session)
                        .param("action", "logout"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/welcome.jsp"))
                .andExpect(request().sessionAttributeDoesNotExist("uidBean", "sessionCreationDate"));
    }

    @Test
    void registrationConfigAdminAndScenarioJourneysStayDeterministicInsideBootContext() throws Exception {
        mockMvc.perform(post("/app")
                        .param("action", "register")
                        .param("user id", "uid:new")
                        .param("passwd", "secret")
                        .param("confirm passwd", "different")
                        .param("Full Name", "User New")
                        .param("Credit Card Number", "cc")
                        .param("money", "1000")
                        .param("email", "new@example.com")
                        .param("snail mail", "addr"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/register.jsp"))
                .andExpect(model().attribute("results", "Registration operation failed, your passwords did not match"));

        mockMvc.perform(post("/config")
                        .param("action", "updateConfig")
                        .param("OrderProcessingMode", "1")
                        .param("WebInterface", "2")
                        .param("MaxUsers", "333")
                        .param("MaxQuotes", "444")
                        .param("marketSummaryInterval", "7")
                        .param("primIterations", "3")
                        .param("ListQuotePriceChangeFrequency", "25")
                        .param("EnablePublishQuotePriceChange", "on")
                        .param("DisplayOrderAlerts", "on"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/config.jsp"));

        org.junit.jupiter.api.Assertions.assertEquals(1, runtimeSettingsService.getOrderProcessingMode());
        org.junit.jupiter.api.Assertions.assertEquals(2, runtimeSettingsService.getWebInterface());
        org.junit.jupiter.api.Assertions.assertEquals(333, runtimeSettingsService.getMaxUsers());
        org.junit.jupiter.api.Assertions.assertEquals(444, runtimeSettingsService.getMaxQuotes());
        org.junit.jupiter.api.Assertions.assertEquals(7, runtimeSettingsService.getMarketSummaryInterval());
        org.junit.jupiter.api.Assertions.assertEquals(3, runtimeSettingsService.getPrimIterations());
        org.junit.jupiter.api.Assertions.assertEquals(25, runtimeSettingsService.getListQuotePriceChangeFrequency());
        org.junit.jupiter.api.Assertions.assertTrue(runtimeSettingsService.isPublishQuotePriceChange());
        org.junit.jupiter.api.Assertions.assertTrue(runtimeSettingsService.isDisplayOrderAlerts());
        org.junit.jupiter.api.Assertions.assertEquals(1, TradeConfig.getOrderProcessingMode());

        mockMvc.perform(post("/config")
                        .param("action", "buildDB"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/config.jsp"))
                .andExpect(model().attribute("status", containsString("DayTrader Database Built")));

        verify(tradeDirectDBUtils).buildDB(any(), isNull());

        mockMvc.perform(get("/scenario").param("action", "n"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello")));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void quotesAndBuyJourneysExposeLegacyContractsAcrossOrderModes(int orderMode) throws Exception {
        runtimeSettingsService.setOrderProcessingMode(orderMode);

        QuoteDataBean quote = quote("s:1", "Sample One", new BigDecimal("12.34"), 1.25d);
        when(tradeServicesFacade.getQuote("s:1")).thenReturn(quote);

        OrderDataBean order = new OrderDataBean();
        order.setOrderID(77);
        order.setOrderType("buy");
        order.setOrderStatus("open");
        order.setQuantity(5d);
        when(tradeServicesFacade.buy(anyString(), anyString(), anyDouble(), anyInt())).thenReturn(order);

        MockHttpSession session = new MockHttpSession();
        session.setAttribute("uidBean", "uid:1");

        mockMvc.perform(get("/app")
                        .session(session)
                        .param("action", "quotes")
                        .param("symbols", "s:1"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/quote.jsp"))
                .andExpect(model().attribute("quoteDataBeans", hasSize(1)));

        mockMvc.perform(post("/app")
                        .session(session)
                        .param("action", "buy")
                        .param("symbol", "s:1")
                        .param("quantity", "5"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/order.jsp"))
                .andExpect(model().attributeExists("orderData"));

        mockMvc.perform(get("/rest/quotes/s:1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].symbol").value("s:1"));

        mockMvc.perform(post("/rest/quotes")
                        .contentType("application/x-www-form-urlencoded")
                        .param("symbols", "s:1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price").value(12.34));

        org.junit.jupiter.api.Assertions.assertEquals(orderMode, TradeConfig.getOrderProcessingMode());
    }
}