package com.ibm.websphere.samples.daytrader.support;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.application.orders.OrderWorkProcessor;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.impl.direct.TradeDirectDBUtils;
import com.ibm.websphere.samples.daytrader.streaming.MarketSummaryPublisher;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

public abstract class AbstractDayTraderIntegrationTestSupport {

    @Autowired
    protected RuntimeSettingsService runtimeSettingsService;

    @MockBean
    protected TradeServicesFacade tradeServicesFacade;

    @MockBean
    protected MarketSummaryPublisher marketSummaryPublisher;

    @MockBean
    protected OrderWorkProcessor orderWorkProcessor;

    @MockBean
    protected TradeDirectDBUtils tradeDirectDBUtils;

    @BeforeEach
    void resetHarnessState() throws Exception {
        reset(tradeServicesFacade, marketSummaryPublisher, orderWorkProcessor, tradeDirectDBUtils);
        resetRuntimeSettings();

        when(tradeServicesFacade.getClosedOrders(anyString())).thenReturn(List.of());
        when(tradeServicesFacade.getHoldings(anyString())).thenReturn(List.of());
        when(tradeServicesFacade.getOrders(anyString())).thenReturn(List.of());
        when(tradeServicesFacade.getMarketSummary()).thenReturn(emptySummary());
    }

    protected void resetRuntimeSettings() {
        runtimeSettingsService.setRuntimeMode(0);
        runtimeSettingsService.setOrderProcessingMode(0);
        runtimeSettingsService.setWebInterface(0);
        runtimeSettingsService.setMaxUsers(15000);
        runtimeSettingsService.setMaxQuotes(10000);
        runtimeSettingsService.setPrimIterations(1);
        runtimeSettingsService.setLongRun(true);
        runtimeSettingsService.setUpdateQuotePrices(true);
        runtimeSettingsService.setPublishQuotePriceChange(true);
        runtimeSettingsService.setDisplayOrderAlerts(true);
        runtimeSettingsService.setMarketSummaryInterval(20);
        runtimeSettingsService.setListQuotePriceChangeFrequency(100);
        TradeConfig.setOrderProcessingMode(0);
    }

    protected AccountDataBean account(String userId) {
        AccountDataBean account = new AccountDataBean(100, 1, 0, null, null, BigDecimal.TEN, BigDecimal.TEN, userId);
        account.setProfileID(userId);
        return account;
    }

    protected QuoteDataBean quote(String symbol, String companyName, BigDecimal price, double change) {
        QuoteDataBean quote = new QuoteDataBean();
        quote.setSymbol(symbol);
        quote.setCompanyName(companyName);
        quote.setPrice(price);
        quote.setOpen(price);
        quote.setLow(price);
        quote.setHigh(price);
        quote.setChange(change);
        quote.setVolume(10d);
        return quote;
    }

    protected MarketSummaryDataBean emptySummary() {
        return new MarketSummaryDataBean(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0d,
                List.of(),
                List.of());
    }
}