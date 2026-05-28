package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.web.jsf.OrderData;
import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;

@Component("accountdata")
@RequestScope
public class AccountJsfBridge extends JsfFacesSupport {

    private final TradeServices tradeServices;
    private final CompatibilitySessionFacade sessionFacade;

    private Date sessionCreationDate;
    private Date currentTime;
    private String profileID;
    private Integer accountID;

    @PastOrPresent
    private Date creationDate;

    @PositiveOrZero
    private int loginCount;

    @PastOrPresent
    private Date lastLogin;

    @PositiveOrZero
    private int logoutCount;
    private BigDecimal balance;
    private BigDecimal openBalance;
    private Integer numberHoldings;
    private BigDecimal holdingsTotal;
    private BigDecimal sumOfCashHoldings;
    private BigDecimal gain;
    private BigDecimal gainPercent;
    private OrderData[] closedOrders;
    private OrderData[] allOrders;
    private Integer numberOfOrders = 0;
    private Integer numberOfOrderRows = 5;

    public AccountJsfBridge(TradeServices tradeServices, CompatibilitySessionFacade sessionFacade) {
        this.tradeServices = tradeServices;
        this.sessionFacade = sessionFacade;
    }

    @PostConstruct
    public void home() {
        String userId = sessionFacade.getUserId(session(false));
        if (userId == null) {
            return;
        }

        try {
            AccountDataBean accountData = tradeServices.getAccountData(userId);
            Collection<HoldingDataBean> holdingDataBeans = tradeServices.getHoldings(userId);

            if (TradeConfig.getDisplayOrderAlerts()) {
                Collection<?> closedOrderBeans = tradeServices.getClosedOrders(userId);
                if (closedOrderBeans != null && !closedOrderBeans.isEmpty()) {
                    session(true).setAttribute("closedOrders", closedOrderBeans);
                    setClosedOrders(toOrderDataArray(closedOrderBeans, false));
                }
            }

            Collection<?> orderDataBeans = TradeConfig.getLongRun() ? new ArrayList<>() : (Collection<?>) tradeServices.getOrders(userId);
            if (orderDataBeans != null && !orderDataBeans.isEmpty()) {
                session(true).setAttribute("orderDataBeans", orderDataBeans);
                setAllOrders(toOrderDataArray(orderDataBeans, true));
                setNumberOfOrders(orderDataBeans.size());
            }

            setSessionCreationDate((Date) session(true).getAttribute("sessionCreationDate"));
            setCurrentTime(new Date());
            doAccountData(accountData, holdingDataBeans);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load account data for JSF view", exception);
        }
    }

    private void doAccountData(AccountDataBean accountData, Collection<HoldingDataBean> holdingDataBeans) {
        setProfileID(accountData.getProfileID());
        setAccountID(accountData.getAccountID());
        setCreationDate(accountData.getCreationDate());
        setLoginCount(accountData.getLoginCount());
        setLogoutCount(accountData.getLogoutCount());
        setLastLogin(accountData.getLastLogin());
        setOpenBalance(accountData.getOpenBalance());
        setBalance(accountData.getBalance());
        setNumberHoldings(holdingDataBeans.size());
        setHoldingsTotal(FinancialUtils.computeHoldingsTotal(holdingDataBeans));
        setSumOfCashHoldings(balance.add(holdingsTotal));
        setGain(FinancialUtils.computeGain(sumOfCashHoldings, openBalance));
        setGainPercent(FinancialUtils.computeGainPercent(sumOfCashHoldings, openBalance));
    }

    private OrderData[] toOrderDataArray(Collection<?> orderBeans, boolean includePrice) {
        OrderData[] rows = new OrderData[orderBeans.size()];
        int index = 0;
        for (Object candidate : orderBeans) {
            OrderDataBean order = (OrderDataBean) candidate;
            rows[index++] = includePrice
                    ? new OrderData(order.getOrderID(), order.getOrderStatus(), order.getOpenDate(), order.getCompletionDate(),
                            order.getOrderFee(), order.getOrderType(), order.getQuantity(), order.getSymbol(), order.getPrice())
                    : new OrderData(order.getOrderID(), order.getOrderStatus(), order.getOpenDate(), order.getCompletionDate(),
                            order.getOrderFee(), order.getOrderType(), order.getQuantity(), order.getSymbol());
        }
        return rows;
    }

    public void toggleShowAllRows() {
        setNumberOfOrderRows(0);
    }

    public Date getSessionCreationDate() {
        return sessionCreationDate;
    }

    public void setSessionCreationDate(Date sessionCreationDate) {
        this.sessionCreationDate = sessionCreationDate;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    public Integer getAccountID() {
        return accountID;
    }

    public void setAccountID(Integer accountID) {
        this.accountID = accountID;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getLogoutCount() {
        return logoutCount;
    }

    public void setLogoutCount(int logoutCount) {
        this.logoutCount = logoutCount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getOpenBalance() {
        return openBalance;
    }

    public void setOpenBalance(BigDecimal openBalance) {
        this.openBalance = openBalance;
    }

    public Integer getNumberHoldings() {
        return numberHoldings;
    }

    public void setNumberHoldings(Integer numberHoldings) {
        this.numberHoldings = numberHoldings;
    }

    public BigDecimal getHoldingsTotal() {
        return holdingsTotal;
    }

    public void setHoldingsTotal(BigDecimal holdingsTotal) {
        this.holdingsTotal = holdingsTotal;
    }

    public BigDecimal getSumOfCashHoldings() {
        return sumOfCashHoldings;
    }

    public void setSumOfCashHoldings(BigDecimal sumOfCashHoldings) {
        this.sumOfCashHoldings = sumOfCashHoldings;
    }

    public BigDecimal getGain() {
        return gain;
    }

    public void setGain(BigDecimal gain) {
        this.gain = gain;
    }

    public BigDecimal getGainPercent() {
        return gainPercent;
    }

    public void setGainPercent(BigDecimal gainPercent) {
        this.gainPercent = gainPercent == null ? null : gainPercent.setScale(2);
    }

    public OrderData[] getClosedOrders() {
        return closedOrders;
    }

    public void setClosedOrders(OrderData[] closedOrders) {
        this.closedOrders = closedOrders;
    }

    public OrderData[] getAllOrders() {
        return allOrders;
    }

    public void setAllOrders(OrderData[] allOrders) {
        this.allOrders = allOrders;
    }

    public Integer getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(Integer numberOfOrders) {
        this.numberOfOrders = numberOfOrders;
    }

    public Integer getNumberOfOrderRows() {
        return numberOfOrderRows;
    }

    public void setNumberOfOrderRows(Integer numberOfOrderRows) {
        this.numberOfOrderRows = numberOfOrderRows;
    }
}