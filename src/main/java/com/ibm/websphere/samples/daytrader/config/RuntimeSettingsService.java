package com.ibm.websphere.samples.daytrader.config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Service
public class RuntimeSettingsService {

    private final AtomicInteger runtimeMode;
    private final AtomicInteger orderProcessingMode;
    private final AtomicInteger webInterface;
    private final AtomicInteger maxUsers;
    private final AtomicInteger maxQuotes;
    private final AtomicInteger primIterations;
    private final AtomicBoolean longRun;
    private final AtomicBoolean updateQuotePrices;
    private final AtomicBoolean publishQuotePriceChange;
    private final AtomicBoolean displayOrderAlerts;
    private final AtomicInteger marketSummaryInterval;
    private final AtomicInteger listQuotePriceChangeFrequency;

    public RuntimeSettingsService(
            @Value("${daytrader.runtime.runtime-mode:0}") int runtimeMode,
            @Value("${daytrader.runtime.order-processing-mode:0}") int orderProcessingMode,
            @Value("${daytrader.runtime.web-interface:0}") int webInterface,
            @Value("${daytrader.runtime.max-users:15000}") int maxUsers,
            @Value("${daytrader.runtime.max-quotes:10000}") int maxQuotes,
            @Value("${daytrader.runtime.prim-iterations:1}") int primIterations,
            @Value("${daytrader.runtime.long-run:true}") boolean longRun,
            @Value("${daytrader.runtime.update-quote-prices:true}") boolean updateQuotePrices,
            @Value("${daytrader.runtime.publish-quote-price-change:true}") boolean publishQuotePriceChange,
            @Value("${daytrader.runtime.display-order-alerts:true}") boolean displayOrderAlerts,
            @Value("${daytrader.runtime.market-summary-interval:20}") int marketSummaryInterval,
            @Value("${daytrader.runtime.list-quote-price-change-frequency:100}") int listQuotePriceChangeFrequency) {
        this.runtimeMode = new AtomicInteger(runtimeMode);
        this.orderProcessingMode = new AtomicInteger(orderProcessingMode);
        this.webInterface = new AtomicInteger(webInterface);
        this.maxUsers = new AtomicInteger(maxUsers);
        this.maxQuotes = new AtomicInteger(maxQuotes);
        this.primIterations = new AtomicInteger(primIterations);
        this.longRun = new AtomicBoolean(longRun);
        this.updateQuotePrices = new AtomicBoolean(updateQuotePrices);
        this.publishQuotePriceChange = new AtomicBoolean(publishQuotePriceChange);
        this.displayOrderAlerts = new AtomicBoolean(displayOrderAlerts);
        this.marketSummaryInterval = new AtomicInteger(marketSummaryInterval);
        this.listQuotePriceChangeFrequency = new AtomicInteger(listQuotePriceChangeFrequency);
    }

    @PostConstruct
    void syncLegacyTradeConfig() {
        TradeConfig.setRunTimeMode(runtimeMode.get());
        TradeConfig.setOrderProcessingMode(orderProcessingMode.get());
        TradeConfig.setWebInterface(webInterface.get());
        TradeConfig.setMAX_USERS(maxUsers.get());
        TradeConfig.setMAX_QUOTES(maxQuotes.get());
        TradeConfig.setPrimIterations(primIterations.get());
        TradeConfig.setLongRun(longRun.get());
        TradeConfig.setUpdateQuotePrices(updateQuotePrices.get());
        TradeConfig.setPublishQuotePriceChange(publishQuotePriceChange.get());
        TradeConfig.setDisplayOrderAlerts(displayOrderAlerts.get());
        TradeConfig.setMarketSummaryInterval(marketSummaryInterval.get());
        TradeConfig.setListQuotePriceChangeFrequency(listQuotePriceChangeFrequency.get());
    }

    public int getRuntimeMode() {
        return runtimeMode.get();
    }

    public void setRuntimeMode(int value) {
        runtimeMode.set(value);
        TradeConfig.setRunTimeMode(value);
    }

    public int getOrderProcessingMode() {
        return orderProcessingMode.get();
    }

    public void setOrderProcessingMode(int value) {
        orderProcessingMode.set(value);
        TradeConfig.setOrderProcessingMode(value);
    }

    public int getWebInterface() {
        return webInterface.get();
    }

    public void setWebInterface(int value) {
        webInterface.set(value);
        TradeConfig.setWebInterface(value);
    }

    public int getMaxUsers() {
        return maxUsers.get();
    }

    public void setMaxUsers(int value) {
        maxUsers.set(value);
        TradeConfig.setMAX_USERS(value);
    }

    public int getMaxQuotes() {
        return maxQuotes.get();
    }

    public void setMaxQuotes(int value) {
        maxQuotes.set(value);
        TradeConfig.setMAX_QUOTES(value);
    }

    public int getPrimIterations() {
        return primIterations.get();
    }

    public void setPrimIterations(int value) {
        primIterations.set(value);
        TradeConfig.setPrimIterations(value);
    }

    public boolean isLongRun() {
        return longRun.get();
    }

    public void setLongRun(boolean value) {
        longRun.set(value);
        TradeConfig.setLongRun(value);
    }

    public boolean isUpdateQuotePrices() {
        return updateQuotePrices.get();
    }

    public void setUpdateQuotePrices(boolean value) {
        updateQuotePrices.set(value);
        TradeConfig.setUpdateQuotePrices(value);
    }

    public boolean isPublishQuotePriceChange() {
        return publishQuotePriceChange.get();
    }

    public void setPublishQuotePriceChange(boolean value) {
        publishQuotePriceChange.set(value);
        TradeConfig.setPublishQuotePriceChange(value);
    }

    public boolean isDisplayOrderAlerts() {
        return displayOrderAlerts.get();
    }

    public void setDisplayOrderAlerts(boolean value) {
        displayOrderAlerts.set(value);
        TradeConfig.setDisplayOrderAlerts(value);
    }

    public int getMarketSummaryInterval() {
        return marketSummaryInterval.get();
    }

    public void setMarketSummaryInterval(int value) {
        marketSummaryInterval.set(value);
        TradeConfig.setMarketSummaryInterval(value);
    }

    public int getListQuotePriceChangeFrequency() {
        return listQuotePriceChangeFrequency.get();
    }

    public void setListQuotePriceChangeFrequency(int value) {
        listQuotePriceChangeFrequency.set(value);
        TradeConfig.setListQuotePriceChangeFrequency(value);
    }
}