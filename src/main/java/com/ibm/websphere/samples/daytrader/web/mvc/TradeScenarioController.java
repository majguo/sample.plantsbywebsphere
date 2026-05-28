package com.ibm.websphere.samples.daytrader.web.mvc;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Controller
public class TradeScenarioController {

    private static final String TAS_PATH_PREFIX = "/app?action=";

    @RequestMapping(value = "/scenario", method = { RequestMethod.GET, RequestMethod.POST })
    public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        performTask(request, response);
    }

    void performTask(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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

        ServletContext context;
        HttpSession session;
        try {
            context = req.getServletContext();
            session = req.getSession(true);
            userID = (String) session.getAttribute("uidBean");
        } catch (Exception e) {
            Log.error("trade_client.TradeScenarioController.performTask(...): performing " + scenarioAction
                    + " error getting ServletContext,HttpSession, or UserID from session"
                    + " will make scenarioAction a login and try to recover from there", e);
            userID = null;
            action = 'l';
            context = req.getServletContext();
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
                dispatchPath = TAS_PATH_PREFIX + "quotes&symbols=" + TradeConfig.rndSymbols();
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 'a':
                dispatchPath = TAS_PATH_PREFIX + "account";
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 'u':
                dispatchPath = TAS_PATH_PREFIX + "account";
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                String fullName = "rnd" + System.currentTimeMillis();
                String address = "rndAddress";
                String password = "xxx";
                String email = "rndEmail";
                String creditcard = "rndCC";
                dispatchPath = TAS_PATH_PREFIX + "update_profile&fullname=" + fullName + "&password=" + password + "&cpassword=" + password
                        + "&address=" + address + "&email=" + email + "&creditcard=" + creditcard;
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 'h':
                dispatchPath = TAS_PATH_PREFIX + "home";
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 'l':
                userID = TradeConfig.getUserID();
                dispatchPath = TAS_PATH_PREFIX + "login&inScenario=true&uid=" + userID + "&passwd=xxx";
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                if (session.getAttribute("uidBean") == null) {
                    System.out.println("TradeScenario login failed. Reset DB between runs");
                }
                break;
            case 'o':
                dispatchPath = TAS_PATH_PREFIX + "logout";
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 'p':
                dispatchPath = TAS_PATH_PREFIX + "portfolio";
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 'r':
                req.setAttribute("TSS-RecreateSessionInLogout", Boolean.TRUE);
                dispatchPath = TAS_PATH_PREFIX + "logout";
                context.getRequestDispatcher(dispatchPath).include(req, resp);

                userID = TradeConfig.rndNewUserID();
                String passwd = "yyy";
                fullName = TradeConfig.rndFullName();
                creditcard = TradeConfig.rndCreditCard();
                String money = TradeConfig.rndBalance();
                email = TradeConfig.rndEmail(userID);
                String smail = TradeConfig.rndAddress();
                dispatchPath = TAS_PATH_PREFIX + "register&Full Name=" + fullName + "&snail mail=" + smail + "&email=" + email + "&user id=" + userID
                        + "&passwd=" + passwd + "&confirm passwd=" + passwd + "&money=" + money + "&Credit Card Number=" + creditcard;
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            case 's':
                dispatchPath = TAS_PATH_PREFIX + "portfolioNoEdge";
                context.getRequestDispatcher(dispatchPath).include(req, resp);

                Collection<?> holdings = (Collection<?>) req.getAttribute("holdingDataBeans");
                if (holdings != null && !holdings.isEmpty()) {
                    Iterator<?> iterator = holdings.iterator();
                    boolean foundHoldingToSell = false;
                    while (iterator.hasNext()) {
                        HoldingDataBean holdingData = (HoldingDataBean) iterator.next();
                        if (!holdingData.getPurchaseDate().equals(new java.util.Date(0))) {
                            Integer holdingID = holdingData.getHoldingID();
                            dispatchPath = TAS_PATH_PREFIX + "sell&holdingID=" + holdingID;
                            context.getRequestDispatcher(dispatchPath).include(req, resp);
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
                dispatchPath = TAS_PATH_PREFIX + "quotes&symbols=" + symbol;
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                dispatchPath = TAS_PATH_PREFIX + "buy&quantity=" + amount + "&symbol=" + symbol;
                context.getRequestDispatcher(dispatchPath).include(req, resp);
                break;
            default:
                break;
        }
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
}