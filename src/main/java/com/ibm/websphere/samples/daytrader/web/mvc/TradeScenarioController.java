package com.ibm.websphere.samples.daytrader.web.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Controller
public class TradeScenarioController {

    private final TradeAppCompatibilityController tradeAppController;
    private final CompatibilitySessionFacade sessionFacade;

    public TradeScenarioController(
            TradeAppCompatibilityController tradeAppController,
            CompatibilitySessionFacade sessionFacade) {
        this.tradeAppController = tradeAppController;
        this.sessionFacade = sessionFacade;
    }

    @RequestMapping(value = "/scenario", method = { RequestMethod.GET, RequestMethod.POST })
    public void handle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        performTask(request, response);
    }

    void performTask(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        char action = ' ';
        String userID = null;
        String dispatchPath = null;

        resp.setContentType("text/html");

        String scenarioAction = req.getParameter("action");
        if (scenarioAction != null && !scenarioAction.isEmpty()) {
            action = scenarioAction.charAt(0);
            if (action == 'n') {
                writeHelloResponse(resp);
                return;
            }
        }

        HttpSession session;
        try {
            session = req.getSession(true);
            userID = (String) session.getAttribute("uidBean");
        } catch (Exception e) {
            Log.error("trade_client.TradeScenarioController.performTask(...): performing " + scenarioAction
                    + " error getting ServletContext,HttpSession, or UserID from session"
                    + " will make scenarioAction a login and try to recover from there", e);
            userID = null;
            action = 'l';
            session = req.getSession(true);
        }

        if (userID == null) {
            action = 'l';
            TradeConfig.incrementScenarioCount();
        } else if (action == ' ') {
            action = TradeConfig.getScenarioAction(userID.startsWith(TradeConfig.newUserPrefix));
        }

        switch (action) {
            case 'q':
                renderAppAction(req, resp, "quotes", Map.of("symbols", TradeConfig.rndSymbols()));
                break;
            case 'a':
                renderAppAction(req, resp, "account", Map.of());
                break;
            case 'u':
                String fullName = "rnd" + System.currentTimeMillis();
                String address = "rndAddress";
                String password = "xxx";
                String email = "rndEmail";
                String creditcard = "rndCC";
                renderAppAction(req, resp, "update_profile", Map.of(
                        "fullname", fullName,
                        "password", password,
                        "cpassword", password,
                        "address", address,
                        "email", email,
                        "creditcard", creditcard));
                break;
            case 'h':
                renderAppAction(req, resp, "home", Map.of());
                break;
            case 'l':
                userID = TradeConfig.getUserID();
                ActionResult loginResult = invokeAppAction(req, "login", Map.of(
                        "inScenario", "true",
                        "uid", userID,
                        "passwd", "xxx"));
                if (session.getAttribute("uidBean") == null) {
                    loginResult = invokeAppAction(req, "login", Map.of(
                            "inScenario", "true",
                            "uid", CompatibilitySessionFacade.OPERATOR_USER_ID,
                            "passwd", "xxx"));
                }
                renderActionResult(req, resp, loginResult);
                if (session.getAttribute("uidBean") == null) {
                    System.out.println("TradeScenario login failed. Reset DB between runs");
                }
                break;
            case 'o':
                renderAppAction(req, resp, "logout", Map.of());
                break;
            case 'p':
                renderAppAction(req, resp, "portfolio", Map.of());
                break;
            case 'r':
                sessionFacade.invalidateSession(req);

                userID = TradeConfig.rndNewUserID();
                String passwd = "yyy";
                fullName = TradeConfig.rndFullName();
                creditcard = TradeConfig.rndCreditCard();
                String money = TradeConfig.rndBalance();
                email = TradeConfig.rndEmail(userID);
                String smail = TradeConfig.rndAddress();
                renderAppAction(req, resp, "register", Map.of(
                        "Full Name", fullName,
                        "snail mail", smail,
                        "email", email,
                        "user id", userID,
                        "passwd", passwd,
                        "confirm passwd", passwd,
                        "money", money,
                        "Credit Card Number", creditcard));
                break;
            case 's':
                ActionResult portfolioResult = invokeAppAction(req, "portfolioNoEdge", Map.of());
                Collection<?> holdings = (Collection<?>) portfolioResult.model().getAttribute("holdingDataBeans");
                if (holdings != null && !holdings.isEmpty()) {
                    Iterator<?> iterator = holdings.iterator();
                    boolean foundHoldingToSell = false;
                    while (iterator.hasNext()) {
                        HoldingDataBean holdingData = (HoldingDataBean) iterator.next();
                        if (!holdingData.getPurchaseDate().equals(new java.util.Date(0))) {
                            Integer holdingID = holdingData.getHoldingID();
                            renderAppAction(req, resp, "sell", Map.of("holdingID", String.valueOf(holdingID)));
                            foundHoldingToSell = true;
                            break;
                        }
                    }
                    if (foundHoldingToSell) {
                        break;
                    }

                    Log.trace("TradeScenario: No holding to sell -switch to buy -- userID = " + userID + "  Collection count = " + holdings.size());
                }

                if (!userID.startsWith(TradeConfig.newUserPrefix)) {
                    TradeConfig.incrementSellDeficit();
                }
            case 'b':
                String symbol = TradeConfig.rndSymbol();
                String amount = TradeConfig.rndQuantity() + "";
                renderAppAction(req, resp, "buy", Map.of(
                        "quantity", amount,
                        "symbol", symbol));
                break;
            default:
                break;
        }
    }

    private void renderAppAction(HttpServletRequest request, HttpServletResponse response, String action, Map<String, String> params)
            throws Exception {
        ActionResult result = invokeAppAction(request, action, params);
        renderActionResult(request, response, result);
        }

        private void renderActionResult(HttpServletRequest request, HttpServletResponse response, ActionResult result)
            throws Exception {
        copyModelToRequest(request, result.model());
        request.getRequestDispatcher(extractForwardPath(result.viewName())).forward(request, response);
    }

    private ActionResult invokeAppAction(HttpServletRequest request, String action, Map<String, String> params) throws Exception {
        Model model = new ExtendedModelMap();
        HttpServletRequest wrappedRequest = new ScenarioRequestWrapper(request, params);
        String viewName = tradeAppController.handle(action, wrappedRequest, model);
        return new ActionResult(viewName, model);
    }

    private void copyModelToRequest(HttpServletRequest request, Model model) {
        for (Map.Entry<String, Object> entry : model.asMap().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    private String extractForwardPath(String viewName) {
        if (viewName != null && viewName.startsWith("forward:")) {
            return viewName.substring("forward:".length());
        }
        return viewName;
    }

    private void writeHelloResponse(HttpServletResponse response) throws IOException {
        try {
            PrintWriter writer = new PrintWriter(response.getOutputStream());
            writer.println("<HTML><HEAD>TradeScenarioServlet</HEAD><BODY>Hello</BODY></HTML>");
            writer.close();
        } catch (Exception e) {
            Log.error("trade_client.TradeScenarioController.performTask(...) error creating printwriter from response.getOutputStream", e);
            response.sendError(500,
                    "trade_client.TradeScenarioController.performTask(...): error creating and writing to PrintStream created from response.getOutputStream()");
        }
    }

    private record ActionResult(String viewName, Model model) {
    }

    private static final class ScenarioRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String[]> parameterMap;

        private ScenarioRequestWrapper(HttpServletRequest request, Map<String, String> overrideParameters) {
            super(request);
            this.parameterMap = new LinkedHashMap<>(request.getParameterMap());
            this.parameterMap.put("action", new String[] { overrideParameters.containsKey("action")
                    ? overrideParameters.get("action")
                    : request.getParameter("action") });
            for (Map.Entry<String, String> entry : overrideParameters.entrySet()) {
                this.parameterMap.put(entry.getKey(), new String[] { entry.getValue() });
            }
        }

        @Override
        public String getParameter(String name) {
            String[] values = parameterMap.get(name);
            return values == null || values.length == 0 ? null : values[0];
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return parameterMap;
        }

        @Override
        public String[] getParameterValues(String name) {
            return parameterMap.get(name);
        }
    }
}