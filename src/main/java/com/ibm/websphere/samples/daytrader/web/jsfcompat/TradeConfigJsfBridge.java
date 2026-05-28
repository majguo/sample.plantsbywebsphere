package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.io.PrintWriter;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;
import com.ibm.websphere.samples.daytrader.impl.direct.TradeDirectDBUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Component("tradeconfig")
@RequestScope
public class TradeConfigJsfBridge extends JsfFacesSupport {

    private final RuntimeSettingsService runtimeSettings;
    private final TradeDirectDBUtils dbUtils;

    private String runtimeMode;
    private String orderProcessingMode;
    private int maxUsers;
    private int maxQuotes;
    private int marketSummaryInterval;
    private String webInterface;
    private int primIterations;
    private int listQuotePriceChangeFrequency;
    private boolean publishQuotePriceChange;
    private boolean longRun;
    private boolean displayOrderAlerts;
    private final String[] runtimeModeList = TradeConfig.getRunTimeModeNames();
    private final String[] orderProcessingModeList = TradeConfig.getOrderProcessingModeNames();
    private final String[] webInterfaceList = TradeConfig.getWebInterfaceNames();
    private String result = "";

    public TradeConfigJsfBridge(RuntimeSettingsService runtimeSettings, TradeDirectDBUtils dbUtils) {
        this.runtimeSettings = runtimeSettings;
        this.dbUtils = dbUtils;
        refreshFromRuntimeSettings();
    }

    public void updateConfig() {
        String currentConfig = "\n\n########## Trade configuration update. Current config:\n\n";

        currentConfig += "\t\tRunTimeMode:\t\t\t" + runtimeModeList[runtimeSettings.getRuntimeMode()] + "\n";
        applyNamedChoice(orderProcessingMode, orderProcessingModeList, runtimeSettings::setOrderProcessingMode, "orderProcessing");
        currentConfig += "\t\tOrderProcessingMode:\t\t" + orderProcessingModeList[runtimeSettings.getOrderProcessingMode()] + "\n";

        applyNamedChoice(webInterface, webInterfaceList, runtimeSettings::setWebInterface, "WebInterface");
        currentConfig += "\t\tWeb Interface:\t\t\t" + webInterfaceList[runtimeSettings.getWebInterface()] + "\n";

        runtimeSettings.setMaxUsers(maxUsers);
        runtimeSettings.setMaxQuotes(maxQuotes);
        currentConfig += "\t\tTrade  Users:\t\t\t" + runtimeSettings.getMaxUsers() + "\n";
        currentConfig += "\t\tTrade Quotes:\t\t\t" + runtimeSettings.getMaxQuotes() + "\n";

        runtimeSettings.setMarketSummaryInterval(marketSummaryInterval);
        currentConfig += "\t\tMarket Summary Interval:\t" + runtimeSettings.getMarketSummaryInterval() + "\n";

        runtimeSettings.setPrimIterations(primIterations);
        currentConfig += "\t\tPrimitive Iterations:\t\t" + runtimeSettings.getPrimIterations() + "\n";

        runtimeSettings.setPublishQuotePriceChange(publishQuotePriceChange);
        currentConfig += "\t\tTradeStreamer MDB Enabled:\t" + runtimeSettings.isPublishQuotePriceChange() + "\n";

        runtimeSettings.setListQuotePriceChangeFrequency(listQuotePriceChangeFrequency);
        currentConfig += "\t\t% of trades on Websocket:\t" + runtimeSettings.getListQuotePriceChangeFrequency() + "\n";

        runtimeSettings.setLongRun(longRun);
        currentConfig += "\t\tLong Run Enabled:\t\t" + runtimeSettings.isLongRun() + "\n";

        runtimeSettings.setDisplayOrderAlerts(displayOrderAlerts);
        currentConfig += "\t\tDisplay Order Alerts:\t\t" + runtimeSettings.isDisplayOrderAlerts() + "\n";

        System.out.println(currentConfig);
        refreshFromRuntimeSettings();
        setResult("DayTrader Configuration Updated");
    }

    public String resetTrade() {
        try {
            RunStatsDataBean runStatsData = dbUtils.resetTrade(false);
            session(true).setAttribute("runStatsData", runStatsData);
            session(true).setAttribute("tradeConfig", new TradeConfig());
            result = "Trade Reset completed successfully";
        } catch (Exception exception) {
            result = "Trade Reset Error  - see log for details";
            session(true).setAttribute("result", result);
            Log.error(exception, result);
        }
        return "stats";
    }

    public String populateDatabase() {
        try {
            dbUtils.buildDB(new PrintWriter(System.out), null);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to populate DayTrader database", exception);
        }

        result = "TradeBuildDB: **** DayTrader Database Built - " + runtimeSettings.getMaxUsers() + " users created, "
                + runtimeSettings.getMaxQuotes() + " quotes created. ****<br/>";
        result += "TradeBuildDB: **** Check System.Out for any errors. ****<br/>";
        return "database";
    }

    public String buildDatabaseTables() {
        try {
            String dbProductName = dbUtils.checkDBProductName();
            if (dbProductName == null) {
                result += "TradeBuildDB: **** Unable to check DB Product name, please check Database/AppServer configuration and retry ****<br/>";
                return "database";
            }

            String ddlFile = resolveDdlFile(dbProductName);
            dbUtils.buildDB(new PrintWriter(System.out), externalContext().getResourceAsStream(ddlFile));
            result = result + "TradeBuildDB: **** DayTrader Database Created, Check System.Out for any errors. ****<br/>";
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to rebuild DayTrader database tables", exception);
        }

        return "database";
    }

    private String resolveDdlFile(String dbProductName) {
        result = result + "TradeBuildDB: **** Database Product detected: " + dbProductName + " ****<br/>";
        if (dbProductName.startsWith("DB2/")) {
            result = result + "TradeBuildDB: **** The DDL file at path/dbscripts/db2/Table.ddl will be used ****<br/>";
            return "/dbscripts/db2/Table.ddl";
        }
        if (dbProductName.startsWith("Apache Derby")) {
            result = result + "TradeBuildDB: **** The DDL file at path/dbscripts/derby/Table.ddl will be used ****<br/>";
            return "/dbscripts/derby/Table.ddl";
        }
        if (dbProductName.startsWith("Oracle")) {
            result = result + "TradeBuildDB: **** The DDL file at path/dbscripts/oracle/Table.ddl will be used ****<br/>";
            return "/dbscripts/oracle/Table.ddl";
        }
        result = result + "TradeBuildDB: **** This Database is unsupported/untested use at your own risk ****<br/>";
        result = result + "TradeBuildDB: **** The DDL file at path/dbscripts/other/Table.ddl will be used ****<br/>";
        return "/dbscripts/other/Table.ddl";
    }

    private void applyNamedChoice(String selectedValue, String[] options, IntSetter setter, String fieldName) {
        if (selectedValue == null) {
            return;
        }
        try {
            for (int index = 0; index < options.length; index++) {
                if (selectedValue.equals(options[index])) {
                    setter.set(index);
                }
            }
        } catch (Exception exception) {
            Log.error(exception, "TradeConfigJsfBridge.updateConfig(..): minor exception caught",
                    "trying to set " + fieldName + " to " + selectedValue,
                    "reverting to current value");
        }
    }

    private void refreshFromRuntimeSettings() {
        this.runtimeMode = runtimeModeList[runtimeSettings.getRuntimeMode()];
        this.orderProcessingMode = orderProcessingModeList[runtimeSettings.getOrderProcessingMode()];
        this.maxUsers = runtimeSettings.getMaxUsers();
        this.maxQuotes = runtimeSettings.getMaxQuotes();
        this.marketSummaryInterval = runtimeSettings.getMarketSummaryInterval();
        this.webInterface = webInterfaceList[runtimeSettings.getWebInterface()];
        this.primIterations = runtimeSettings.getPrimIterations();
        this.listQuotePriceChangeFrequency = runtimeSettings.getListQuotePriceChangeFrequency();
        this.publishQuotePriceChange = runtimeSettings.isPublishQuotePriceChange();
        this.longRun = runtimeSettings.isLongRun();
        this.displayOrderAlerts = runtimeSettings.isDisplayOrderAlerts();
    }

    public String getRuntimeMode() {
        return runtimeMode;
    }

    public void setRuntimeMode(String runtimeMode) {
        this.runtimeMode = runtimeMode;
    }

    public String getOrderProcessingMode() {
        return orderProcessingMode;
    }

    public void setOrderProcessingMode(String orderProcessingMode) {
        this.orderProcessingMode = orderProcessingMode;
    }

    public int getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public int getMaxQuotes() {
        return maxQuotes;
    }

    public void setMaxQuotes(int maxQuotes) {
        this.maxQuotes = maxQuotes;
    }

    public int getMarketSummaryInterval() {
        return marketSummaryInterval;
    }

    public void setMarketSummaryInterval(int marketSummaryInterval) {
        this.marketSummaryInterval = marketSummaryInterval;
    }

    public String getWebInterface() {
        return webInterface;
    }

    public void setWebInterface(String webInterface) {
        this.webInterface = webInterface;
    }

    public int getPrimIterations() {
        return primIterations;
    }

    public void setPrimIterations(int primIterations) {
        this.primIterations = primIterations;
    }

    public int getListQuotePriceChangeFrequency() {
        return listQuotePriceChangeFrequency;
    }

    public void setListQuotePriceChangeFrequency(int listQuotePriceChangeFrequency) {
        this.listQuotePriceChangeFrequency = listQuotePriceChangeFrequency;
    }

    public boolean getPublishQuotePriceChange() {
        return publishQuotePriceChange;
    }

    public void setPublishQuotePriceChange(boolean publishQuotePriceChange) {
        this.publishQuotePriceChange = publishQuotePriceChange;
    }

    public boolean getLongRun() {
        return longRun;
    }

    public void setLongRun(boolean longRun) {
        this.longRun = longRun;
    }

    public boolean getDisplayOrderAlerts() {
        return displayOrderAlerts;
    }

    public void setDisplayOrderAlerts(boolean displayOrderAlerts) {
        this.displayOrderAlerts = displayOrderAlerts;
    }

    public String[] getRuntimeModeList() {
        return runtimeModeList;
    }

    public String[] getOrderProcessingModeList() {
        return orderProcessingModeList;
    }

    public String[] getWebInterfaceList() {
        return webInterfaceList;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }
}