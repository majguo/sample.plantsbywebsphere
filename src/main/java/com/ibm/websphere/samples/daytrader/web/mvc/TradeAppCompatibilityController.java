package com.ibm.websphere.samples.daytrader.web.mvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/app")
public class TradeAppCompatibilityController {

    private final TradeServices tradeServices;
    private final CompatibilitySessionFacade sessionFacade;

    public TradeAppCompatibilityController(TradeServices tradeServices, CompatibilitySessionFacade sessionFacade) {
        this.tradeServices = tradeServices;
        this.sessionFacade = sessionFacade;
    }

    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public String handle(
            @RequestParam(value = "action", required = false) String action,
            HttpServletRequest request,
            Model model) throws Exception {
        if (action == null) {
            return welcome(model, "");
        }

        if ("login".equals(action)) {
            return login(request, model);
        }
        if ("register".equals(action)) {
            return register(request, model);
        }

        String userId = sessionFacade.getUserId(request.getSession(false));
        if (userId == null) {
            return welcome(model, "User Not Logged in");
        }

        return switch (action) {
            case "home" -> home(userId, model);
            case "account" -> account(userId, model, "");
            case "update_profile" -> updateProfile(userId, request, model);
            case "quotes" -> quotes(userId, request, model);
            case "buy" -> buy(userId, request, model);
            case "sell" -> sell(userId, request, model);
            case "portfolio", "portfolioNoEdge" -> portfolio(userId, model, "Portfolio as of " + new java.util.Date());
            case "logout" -> logout(userId, request, model);
            case "mksummary" -> marketSummary(userId, model);
            default -> welcome(model, "TradeAppServlet: Invalid Action" + action);
        };
    }

    private String login(HttpServletRequest request, Model model) throws Exception {
        String userId = request.getParameter("uid");
        String password = request.getParameter("passwd");
        try {
            AccountDataBean accountData = tradeServices.login(userId, password);
            if (accountData != null) {
                sessionFacade.establishSession(request, userId);
                return home(userId, model, "Ready to Trade");
            }
            return welcome(model, "\nCould not find account for + " + userId);
        } catch (RuntimeException exception) {
            return welcome(model, "Login failed");
        }
    }

    private String register(HttpServletRequest request, Model model) throws Exception {
        String userId = request.getParameter("user id");
        String password = request.getParameter("passwd");
        String confirmPassword = request.getParameter("confirm passwd");
        String fullName = request.getParameter("Full Name");
        String creditCard = request.getParameter("Credit Card Number");
        String openBalanceString = request.getParameter("money");
        String email = request.getParameter("email");
        String address = request.getParameter("snail mail");

        if (password == null || !password.equals(confirmPassword) || password.isEmpty()) {
            model.addAttribute("results", "Registration operation failed, your passwords did not match");
            return forwardPage(TradeConfig.REGISTER_PAGE);
        }

        try {
            AccountDataBean accountData = tradeServices.register(
                    userId,
                    password,
                    fullName,
                    address,
                    email,
                    creditCard,
                    new BigDecimal(openBalanceString));
            if (accountData == null) {
                model.addAttribute("results", "Registration operation failed;");
                return forwardPage(TradeConfig.REGISTER_PAGE);
            }
            AccountDataBean loggedInAccount = tradeServices.login(userId, password);
            if (loggedInAccount != null) {
                sessionFacade.establishSession(request, userId);
                return home(userId, model, "Ready to Trade");
            }
            return welcome(model, "\nCould not find account for + " + userId);
        } catch (NumberFormatException exception) {
            model.addAttribute("results", "Registration operation failed;");
            return forwardPage(TradeConfig.REGISTER_PAGE);
        }
    }

    private String home(String userId, Model model) throws Exception {
        return home(userId, model, "Ready to Trade");
    }

    private String home(String userId, Model model, String results) throws Exception {
        AccountDataBean accountData = tradeServices.getAccountData(userId);
        Collection<?> holdingDataBeans = tradeServices.getHoldings(userId);
        Collection<?> closedOrders = tradeServices.getClosedOrders(userId);
        model.addAttribute("accountData", accountData);
        model.addAttribute("holdingDataBeans", holdingDataBeans);
        model.addAttribute("results", results);
        if (closedOrders != null && !closedOrders.isEmpty()) {
            model.addAttribute("closedOrders", closedOrders);
        }
        return forwardPage(TradeConfig.HOME_PAGE);
    }

    private String account(String userId, Model model, String results) throws Exception {
        AccountDataBean accountData = tradeServices.getAccountData(userId);
        AccountProfileDataBean accountProfileData = tradeServices.getAccountProfileData(userId);
        Collection<?> orderDataBeans = TradeConfig.getLongRun()
                ? new ArrayList<>()
                : tradeServices.getOrders(userId);
        model.addAttribute("accountData", accountData);
        model.addAttribute("accountProfileData", accountProfileData);
        model.addAttribute("orderDataBeans", orderDataBeans);
        model.addAttribute("results", results);
        return forwardPage(TradeConfig.ACCOUNT_PAGE);
    }

    private String updateProfile(String userId, HttpServletRequest request, Model model) throws Exception {
        String password = trimmedParameter(request, "password");
        String confirmPassword = trimmedParameter(request, "cpassword");
        String fullName = trimmedParameter(request, "fullname");
        String address = trimmedParameter(request, "address");
        String creditCard = trimmedParameter(request, "creditcard");
        String email = trimmedParameter(request, "email");

        String results = "";
        boolean update = true;
        if (!password.equals(confirmPassword)) {
            results = "Update profile error: passwords do not match";
            update = false;
        } else if (password.isEmpty() || fullName.isEmpty() || address.isEmpty() || creditCard.isEmpty() || email.isEmpty()) {
            results = "Update profile error: please fill in all profile information fields";
            update = false;
        }

        if (update) {
            AccountProfileDataBean profileData = new AccountProfileDataBean(userId, password, fullName, address, email, creditCard);
            tradeServices.updateAccountProfile(profileData);
            results = "Account profile update successful";
        }
        return account(userId, model, results);
    }

    private String portfolio(String userId, Model model, String results) throws Exception {
        Collection<HoldingDataBean> holdingDataBeans = tradeServices.getHoldings(userId);
        List<QuoteDataBean> quoteDataBeans = new ArrayList<>();
        for (HoldingDataBean holdingDataBean : holdingDataBeans) {
            QuoteDataBean quoteDataBean = tradeServices.getQuote(holdingDataBean.getQuoteID());
            if (quoteDataBean != null) {
                quoteDataBeans.add(quoteDataBean);
            }
        }

        Collection<?> closedOrders = tradeServices.getClosedOrders(userId);
        model.addAttribute("results", holdingDataBeans.isEmpty() ? results + ".  Your portfolio is empty." : results);
        model.addAttribute("holdingDataBeans", holdingDataBeans);
        model.addAttribute("quoteDataBeans", quoteDataBeans);
        if (closedOrders != null && !closedOrders.isEmpty()) {
            model.addAttribute("closedOrders", closedOrders);
        }
        return forwardPage(TradeConfig.PORTFOLIO_PAGE);
    }

    private String quotes(String userId, HttpServletRequest request, Model model) throws Exception {
        List<QuoteDataBean> quoteDataBeans = new ArrayList<>();
        String symbols = request.getParameter("symbols");
        if (symbols != null) {
            for (String symbol : symbols.split(",")) {
                String trimmedSymbol = symbol.trim();
                if (!trimmedSymbol.isEmpty()) {
                    quoteDataBeans.add(tradeServices.getQuote(trimmedSymbol));
                }
            }
        }

        model.addAttribute("quoteDataBeans", quoteDataBeans);
        addClosedOrders(userId, model);
        return forwardPage(TradeConfig.QUOTE_PAGE);
    }

    private String buy(String userId, HttpServletRequest request, Model model) throws Exception {
        String symbol = trimmedParameter(request, "symbol");
        String quantity = trimmedParameter(request, "quantity");

        OrderDataBean orderData = tradeServices.buy(
                userId,
                symbol,
                Double.parseDouble(quantity),
                TradeConfig.getOrderProcessingMode());

        model.addAttribute("orderData", orderData);
        model.addAttribute("results", "");
        addClosedOrders(userId, model);
        return forwardPage(TradeConfig.ORDER_PAGE);
    }

    private String sell(String userId, HttpServletRequest request, Model model) throws Exception {
        String holdingId = trimmedParameter(request, "holdingID");
        OrderDataBean orderData = tradeServices.sell(
                userId,
                Integer.valueOf(holdingId),
                TradeConfig.getOrderProcessingMode());

        model.addAttribute("orderData", orderData);
        model.addAttribute("results", "");
        addClosedOrders(userId, model);
        return forwardPage(TradeConfig.ORDER_PAGE);
    }

    private String logout(String userId, HttpServletRequest request, Model model) throws Exception {
        tradeServices.logout(userId);
        sessionFacade.invalidateSession(request);
        try {
            request.logout();
        } catch (ServletException ignored) {
            // No container-managed auth is active yet; preserve logout flow without failing the page.
        }
        return welcome(model, "");
    }

    private String marketSummary(String userId, Model model) throws Exception {
        MarketSummaryDataBean marketSummaryData = tradeServices.getMarketSummary();
        model.addAttribute("marketSummaryData", marketSummaryData);
        model.addAttribute("results", "test");
        addClosedOrders(userId, model);
        return forwardPage(TradeConfig.MARKET_SUMMARY_PAGE);
    }

    private String welcome(Model model, String results) {
        model.addAttribute("results", results);
        return forwardPage(TradeConfig.WELCOME_PAGE);
    }

    private String forwardPage(int pageNumber) {
        return "forward:" + TradeConfig.getPage(pageNumber);
    }

    private String trimmedParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return value == null ? "" : value.trim();
    }

    private void addClosedOrders(String userId, Model model) throws Exception {
        Collection<?> closedOrders = tradeServices.getClosedOrders(userId);
        if (closedOrders != null && !closedOrders.isEmpty()) {
            model.addAttribute("closedOrders", closedOrders);
        }
    }
}