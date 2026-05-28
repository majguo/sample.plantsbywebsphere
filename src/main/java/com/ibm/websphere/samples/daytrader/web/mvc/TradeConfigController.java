package com.ibm.websphere.samples.daytrader.web.mvc;

import java.io.InputStream;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.ibm.websphere.samples.daytrader.beans.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;
import com.ibm.websphere.samples.daytrader.impl.direct.TradeDirectDBUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Controller
public class TradeConfigController {

    private final RuntimeSettingsService runtimeSettings;
    private final TradeDirectDBUtils dbUtils;

    public TradeConfigController(RuntimeSettingsService runtimeSettings, TradeDirectDBUtils dbUtils) {
        this.runtimeSettings = runtimeSettings;
        this.dbUtils = dbUtils;
    }

    @RequestMapping(value = "/config", method = { RequestMethod.GET, RequestMethod.POST })
    public Object handle(
            @RequestParam(value = "action", required = false) String action,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        response.setContentType("text/html");

        if (!StringUtils.hasText(action)) {
            return configView("<b><br>Current DayTrader Configuration:</br></b>");
        }

        switch (action) {
            case "updateConfig":
                applyConfiguration(request);
                return configView("<B><BR>DayTrader Configuration Updated</BR></B>");
            case "resetTrade":
                return resetTrade();
            case "buildDB":
                dbUtils.buildDB(response.getWriter(), null);
                return configView("DayTrader Database Built - " + runtimeSettings.getMaxUsers() + "users createdCurrent DayTrader Configuration:");
            case "buildDBTables":
                return rebuildTables(request, response);
            default:
                return configView("Current DayTrader Configuration:");
        }
    }

    private ModelAndView configView(String status) {
        ModelAndView modelAndView = new ModelAndView("config");
        modelAndView.addObject("tradeConfig", new TradeConfig());
        modelAndView.addObject("status", status);
        return modelAndView;
    }

    private ModelAndView resetTrade() throws Exception {
        RunStatsDataBean runStatsData = dbUtils.resetTrade(false);
        ModelAndView modelAndView = new ModelAndView("runStats");
        modelAndView.addObject("runStatsData", runStatsData);
        modelAndView.addObject("tradeConfig", new TradeConfig());
        modelAndView.addObject("status", "Trade Reset completed successfully");
        return modelAndView;
    }

    private ModelAndView rebuildTables(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("text/html");

        String dbProductName;
        try {
            dbProductName = dbUtils.checkDBProductName();
        } catch (Exception e) {
            Log.error(e, "TradeBuildDB: Unable to check DB Product name");
            response.getWriter().println(
                    "<BR>TradeBuildDB: **** Unable to check DB Product name, please check Database/AppServer configuration and retry ****</BR></BODY>");
            return null;
        }

        if (!StringUtils.hasText(dbProductName)) {
            response.getWriter().println(
                    "<BR>TradeBuildDB: **** Unable to check DB Product name, please check Database/AppServer configuration and retry ****</BR></BODY>");
            return null;
        }

        String ddlFile = resolveDdlFile(dbProductName, response);
        response.getWriter().println("<BR>TradeBuildDB: **** The DDL file at path <I>" + ddlFile + "</I> will be used ****</BR>");
        response.getWriter().flush();

        try (InputStream ddlStream = request.getServletContext().getResourceAsStream(ddlFile)) {
            if (ddlStream == null) {
                response.getWriter().println("<BR>TradeBuildDB: **** Unable to locate DDL file for the specified database ****</BR></BODY>");
                return null;
            }
            dbUtils.buildDB(response.getWriter(), ddlStream);
        }

        return configView("Current DayTrader Configuration:");
    }

    private String resolveDdlFile(String dbProductName, HttpServletResponse response) throws Exception {
        response.getWriter().println("<BR>TradeBuildDB: **** Database Product detected: " + dbProductName + " ****</BR>");
        if (dbProductName.startsWith("DB2/")) {
            return "/dbscripts/db2/Table.ddl";
        }
        if (dbProductName.startsWith("Apache Derby")) {
            return "/dbscripts/derby/Table.ddl";
        }
        if (dbProductName.startsWith("Oracle")) {
            return "/dbscripts/oracle/Table.ddl";
        }
        response.getWriter().println("<BR>TradeBuildDB: **** This Database is unsupported/untested use at your own risk ****</BR>");
        return "/dbscripts/other/Table.ddl";
    }

    private void applyConfiguration(HttpServletRequest request) {
        updateIndexedValue(request.getParameter("OrderProcessingMode"), TradeConfig.getOrderProcessingModeNames().length,
                runtimeSettings::setOrderProcessingMode, "orderProcessing");
        updateIndexedValue(request.getParameter("WebInterface"), TradeConfig.getWebInterfaceNames().length,
                runtimeSettings::setWebInterface, "WebInterface");
        updateIntegerValue(request.getParameter("MaxUsers"), runtimeSettings::setMaxUsers, "maxusers");
        updateIntegerValue(request.getParameter("MaxQuotes"), runtimeSettings::setMaxQuotes, "max_quotes");
        updateIntegerValue(request.getParameter("marketSummaryInterval"), runtimeSettings::setMarketSummaryInterval,
                "marketSummaryInterval");
        updateIntegerValue(request.getParameter("primIterations"), runtimeSettings::setPrimIterations, "primIterations");
        updateIntegerValue(request.getParameter("ListQuotePriceChangeFrequency"), runtimeSettings::setListQuotePriceChangeFrequency,
                "percentSentToWebSocket");

        runtimeSettings.setPublishQuotePriceChange(request.getParameter("EnablePublishQuotePriceChange") != null);
        runtimeSettings.setLongRun(request.getParameter("EnableLongRun") != null);
        runtimeSettings.setDisplayOrderAlerts(request.getParameter("DisplayOrderAlerts") != null);
    }

    private void updateIndexedValue(String rawValue, int upperExclusive, IntSetter setter, String fieldName) {
        if (!StringUtils.hasText(rawValue)) {
            return;
        }
        try {
            int parsed = Integer.parseInt(rawValue);
            if (parsed >= 0 && parsed < upperExclusive) {
                setter.set(parsed);
            }
        } catch (NumberFormatException ex) {
            Log.error(ex, "TradeConfigController.updateIndexedValue(..): minor exception caught",
                    "trying to set " + fieldName + " to " + rawValue,
                    "reverting to current value");
        }
    }

    private void updateIntegerValue(String rawValue, IntSetter setter, String fieldName) {
        if (!StringUtils.hasText(rawValue)) {
            return;
        }
        try {
            setter.set(Integer.parseInt(rawValue));
        } catch (NumberFormatException ex) {
            Log.error(ex, "TradeConfigController.updateIntegerValue(..): minor exception caught",
                    "trying to set " + fieldName + " to " + rawValue,
                    "reverting to current value");
        }
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }
}