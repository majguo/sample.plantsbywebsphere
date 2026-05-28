package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

@WebMvcTest(controllers = TradeAppCompatibilityController.class)
@ContextConfiguration(classes = {
    TradeAppCompatibilityController.class,
    CompatibilitySessionFacade.class
})
@Import(CompatibilitySessionFacade.class)
class TradeAppCompatibilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeServices tradeServices;

    @Test
    void loginSetsCompatibilitySessionMarkersAndForwardsHome() throws Exception {
        AccountDataBean accountData = new AccountDataBean(100, 1, 0, null, null, BigDecimal.TEN, BigDecimal.TEN, "uid:1");
        accountData.setProfileID("uid:1");

        when(tradeServices.login("uid:1", "secret")).thenReturn(accountData);
        when(tradeServices.getAccountData("uid:1")).thenReturn(accountData);
        when(tradeServices.getHoldings("uid:1")).thenReturn(List.of());
        when(tradeServices.getClosedOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(post("/app")
                .param("action", "login")
                .param("uid", "uid:1")
                .param("passwd", "secret"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/tradehome.jsp"))
            .andExpect(request().sessionAttribute("uidBean", "uid:1"))
            .andExpect(request().sessionAttribute("sessionCreationDate", org.hamcrest.Matchers.notNullValue()))
            .andExpect(model().attributeExists("accountData", "holdingDataBeans", "results"));
    }

    @Test
    void unauthenticatedAccountRequestFallsBackToWelcomePage() throws Exception {
        mockMvc.perform(get("/app").param("action", "account"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/welcome.jsp"))
            .andExpect(model().attribute("results", "User Not Logged in"));
    }

    @Test
    void updateProfileValidationFailureRedisplaysAccountPage() throws Exception {
        AccountDataBean accountData = new AccountDataBean(100, 1, 0, null, null, BigDecimal.TEN, BigDecimal.TEN, "uid:1");
        AccountProfileDataBean profileData = new AccountProfileDataBean("uid:1", "secret", "User One", "addr", "mail@example.com", "cc");

        when(tradeServices.getAccountData("uid:1")).thenReturn(accountData);
        when(tradeServices.getAccountProfileData("uid:1")).thenReturn(profileData);
        when(tradeServices.getOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(post("/app")
                .sessionAttr("uidBean", "uid:1")
                .param("action", "update_profile")
                .param("password", "secret")
                .param("cpassword", "different")
                .param("fullname", "User One")
                .param("address", "addr")
                .param("creditcard", "cc")
                .param("email", "mail@example.com"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/account.jsp"))
            .andExpect(model().attribute("results", "Update profile error: passwords do not match"));
    }

    @Test
    void portfolioLoadsHoldingsAndQuotes() throws Exception {
        HoldingDataBean holdingDataBean = new HoldingDataBean();
        holdingDataBean.setHoldingID(11);
        holdingDataBean.setQuoteID("s:1");
        QuoteDataBean quoteDataBean = new QuoteDataBean("s:1", "Sample", 0, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0);

        when(tradeServices.getHoldings("uid:1")).thenReturn(List.of(holdingDataBean));
        when(tradeServices.getQuote("s:1")).thenReturn(quoteDataBean);
        when(tradeServices.getClosedOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(get("/app")
                .sessionAttr("uidBean", "uid:1")
                .param("action", "portfolio"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/portfolio.jsp"))
            .andExpect(model().attributeExists("holdingDataBeans", "quoteDataBeans", "results"));
    }

    @Test
    void quotesLoadsRequestedSymbolsIntoQuotePage() throws Exception {
        QuoteDataBean firstQuote = new QuoteDataBean("s:1", "Sample One", 0, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0);
        QuoteDataBean secondQuote = new QuoteDataBean("s:2", "Sample Two", 0, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN, 0);

        when(tradeServices.getQuote("s:1")).thenReturn(firstQuote);
        when(tradeServices.getQuote("s:2")).thenReturn(secondQuote);
        when(tradeServices.getClosedOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(get("/app")
                .sessionAttr("uidBean", "uid:1")
                .param("action", "quotes")
                .param("symbols", "s:1, s:2"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/quote.jsp"))
            .andExpect(model().attributeExists("quoteDataBeans"));
    }

    @Test
    void buyCreatesOrderAndForwardsOrderPage() throws Exception {
        OrderDataBean orderDataBean = new OrderDataBean();
        orderDataBean.setOrderID(77);
        orderDataBean.setOrderType("buy");
        orderDataBean.setOrderStatus("open");
        orderDataBean.setQuantity(100d);

        when(tradeServices.buy("uid:1", "s:1", 100d, 0)).thenReturn(orderDataBean);
        when(tradeServices.getClosedOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(post("/app")
                .sessionAttr("uidBean", "uid:1")
                .param("action", "buy")
                .param("symbol", "s:1")
                .param("quantity", "100"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/order.jsp"))
            .andExpect(model().attributeExists("orderData", "results"));
    }

    @Test
    void sellCreatesOrderAndForwardsOrderPage() throws Exception {
        OrderDataBean orderDataBean = new OrderDataBean();
        orderDataBean.setOrderID(78);
        orderDataBean.setOrderType("sell");
        orderDataBean.setOrderStatus("open");
        orderDataBean.setQuantity(25d);

        when(tradeServices.sell("uid:1", 11, 0)).thenReturn(orderDataBean);
        when(tradeServices.getClosedOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(post("/app")
                .sessionAttr("uidBean", "uid:1")
                .param("action", "sell")
                .param("holdingID", "11"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/order.jsp"))
            .andExpect(model().attributeExists("orderData", "results"));
    }

    @Test
    void marketSummaryLoadsSummaryAndClosedOrdersIntoPage() throws Exception {
        MarketSummaryDataBean summary = MarketSummaryDataBean.getRandomInstance();
        OrderDataBean closedOrder = new OrderDataBean();
        closedOrder.setOrderID(90);
        closedOrder.setOrderStatus("closed");

        when(tradeServices.getMarketSummary()).thenReturn(summary);
        doReturn(List.of(closedOrder)).when(tradeServices).getClosedOrders("uid:1");

        mockMvc.perform(get("/app")
                .sessionAttr("uidBean", "uid:1")
                .param("action", "mksummary"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("/marketSummary.jsp"))
            .andExpect(model().attributeExists("marketSummaryData", "closedOrders", "results"));
    }
}