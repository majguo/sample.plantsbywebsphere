package com.ibm.websphere.samples.daytrader.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.util.TradeConfig;

class RuntimeSettingsServiceTest {

    @AfterEach
    void resetTradeConfigDefaults() {
        TradeConfig.setRunTimeMode(0);
        TradeConfig.setOrderProcessingMode(0);
        TradeConfig.setWebInterface(0);
        TradeConfig.setMAX_USERS(15000);
        TradeConfig.setMAX_QUOTES(10000);
        TradeConfig.setPrimIterations(1);
        TradeConfig.setLongRun(true);
        TradeConfig.setUpdateQuotePrices(true);
        TradeConfig.setPublishQuotePriceChange(true);
        TradeConfig.setDisplayOrderAlerts(true);
        TradeConfig.setMarketSummaryInterval(20);
        TradeConfig.setListQuotePriceChangeFrequency(100);
    }

    @Test
    void syncsBootDefaultsIntoLegacyTradeConfigAndMutatesBothViews() {
        RuntimeSettingsService settings = new RuntimeSettingsService(2, 1, 1, 111, 222, 3, false, false, false, false, 45, 60);

        settings.syncLegacyTradeConfig();

        assertEquals(2, TradeConfig.getRunTimeMode());
        assertEquals(1, TradeConfig.getOrderProcessingMode());
        assertEquals(1, TradeConfig.getWebInterface());
        assertEquals(111, TradeConfig.getMAX_USERS());
        assertEquals(222, TradeConfig.getMAX_QUOTES());
        assertEquals(3, TradeConfig.getPrimIterations());
        assertFalse(TradeConfig.getLongRun());
        assertFalse(TradeConfig.getUpdateQuotePrices());
        assertFalse(TradeConfig.getPublishQuotePriceChange());
        assertFalse(TradeConfig.getDisplayOrderAlerts());
        assertEquals(45, TradeConfig.getMarketSummaryInterval());
        assertEquals(60, TradeConfig.getListQuotePriceChangeFrequency());

        settings.setWebInterface(2);
        settings.setMaxUsers(333);
        settings.setMaxQuotes(444);
        settings.setPrimIterations(5);
        settings.setLongRun(true);
        settings.setPublishQuotePriceChange(true);
        settings.setDisplayOrderAlerts(true);
        settings.setMarketSummaryInterval(10);
        settings.setListQuotePriceChangeFrequency(25);

        assertEquals(2, TradeConfig.getWebInterface());
        assertEquals(333, TradeConfig.getMAX_USERS());
        assertEquals(444, TradeConfig.getMAX_QUOTES());
        assertEquals(5, TradeConfig.getPrimIterations());
        assertTrue(TradeConfig.getLongRun());
        assertTrue(TradeConfig.getPublishQuotePriceChange());
        assertTrue(TradeConfig.getDisplayOrderAlerts());
        assertEquals(10, TradeConfig.getMarketSummaryInterval());
        assertEquals(25, TradeConfig.getListQuotePriceChangeFrequency());
    }
}