package com.ibm.websphere.samples.daytrader.web.mvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;

@WebMvcTest(controllers = { TradeScenarioController.class, TradeAppCompatibilityController.class })
@ContextConfiguration(classes = {
        TradeScenarioController.class,
        TradeAppCompatibilityController.class,
        CompatibilitySessionFacade.class
})
@Import(CompatibilitySessionFacade.class)
class TradeScenarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TradeServices tradeServices;

    @Test
    void respondsToNullScenarioProbeWithoutDependingOnTheTradingSurface() throws Exception {
        mockMvc.perform(get("/scenario").param("action", "n"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello")));
    }

    @Test
    void loginScenarioActionAuthenticatesAndForwardsToTheTradingHome() throws Exception {
        AccountDataBean accountData = account("uid:0");

        when(tradeServices.login(anyString(), anyString())).thenReturn(null, accountData);
        when(tradeServices.getAccountData(anyString())).thenReturn(accountData);
        when(tradeServices.getHoldings(anyString())).thenReturn(List.of());
        when(tradeServices.getClosedOrders(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/scenario").param("action", "l"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/tradehome.jsp"))
            .andExpect(request().sessionAttribute("uidBean", notNullValue()));
    }

    @Test
    void buyScenarioActionUsesTheAuthenticatedFlowAndForwardsToOrderPage() throws Exception {
        AccountDataBean accountData = account("uid:1");
        OrderDataBean orderData = new OrderDataBean();
        orderData.setOrderID(91);
        orderData.setOrderType("buy");
        orderData.setOrderStatus("closed");

        when(tradeServices.buy("uid:1", "s:1", 1.0d, 0)).thenReturn(orderData);
        when(tradeServices.getClosedOrders("uid:1")).thenReturn(List.of());

        mockMvc.perform(get("/scenario")
                        .sessionAttr("uidBean", "uid:1")
                        .param("action", "b"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/order.jsp"));
    }

    private AccountDataBean account(String userId) {
        AccountDataBean accountData = new AccountDataBean(100, 1, 0, null, null, BigDecimal.TEN, BigDecimal.TEN, userId);
        accountData.setProfileID(userId);
        return accountData;
    }
}