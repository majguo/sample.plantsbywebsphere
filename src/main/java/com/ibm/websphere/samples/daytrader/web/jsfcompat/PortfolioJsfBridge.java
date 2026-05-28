package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.web.jsf.OrderData;
import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

import jakarta.annotation.PostConstruct;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.validation.constraints.PositiveOrZero;

@Component("portfolio")
@RequestScope
public class PortfolioJsfBridge extends JsfFacesSupport {

    private final TradeServices tradeServices;
    private final CompatibilitySessionFacade sessionFacade;

    private BigDecimal balance;
    private BigDecimal openBalance;

    @PositiveOrZero
    private Integer numberHoldings;

    private BigDecimal holdingsTotal;
    private BigDecimal sumOfCashHoldings;
    private BigDecimal totalGain = BigDecimal.ZERO;
    private BigDecimal totalValue = BigDecimal.ZERO;
    private BigDecimal totalBasis = BigDecimal.ZERO;
    private BigDecimal totalGainPercent = BigDecimal.ZERO;
    private ArrayList<HoldingDataView> holdingDatas;
    private HtmlDataTable dataTable;

    public PortfolioJsfBridge(TradeServices tradeServices, CompatibilitySessionFacade sessionFacade) {
        this.tradeServices = tradeServices;
        this.sessionFacade = sessionFacade;
    }

    @PostConstruct
    public void getPortfolio() {
        String userId = sessionFacade.getUserId(session(false));
        if (userId == null) {
            holdingDatas = new ArrayList<>();
            numberHoldings = 0;
            return;
        }

        try {
            Collection<?> holdingDataBeans = tradeServices.getHoldings(userId);
            numberHoldings = holdingDataBeans.size();
            holdingDatas = new ArrayList<>(holdingDataBeans.size());

            for (Object candidate : holdingDataBeans) {
                HoldingDataBean holdingData = (HoldingDataBean) candidate;
                QuoteDataBean quoteData = tradeServices.getQuote(holdingData.getQuoteID());

                BigDecimal basis = holdingData.getPurchasePrice().multiply(BigDecimal.valueOf(holdingData.getQuantity()));
                BigDecimal marketValue = quoteData.getPrice().multiply(BigDecimal.valueOf(holdingData.getQuantity()));
                totalBasis = totalBasis.add(basis);
                totalValue = totalValue.add(marketValue);
                BigDecimal gain = marketValue.subtract(basis);
                totalGain = totalGain.add(gain);

                HoldingDataView row = new HoldingDataView();
                row.setHoldingID(holdingData.getHoldingID());
                row.setPurchaseDate(holdingData.getPurchaseDate());
                row.setQuoteID(holdingData.getQuoteID());
                row.setQuantity(holdingData.getQuantity());
                row.setPurchasePrice(holdingData.getPurchasePrice());
                row.setBasis(basis);
                row.setGain(gain);
                row.setMarketValue(marketValue);
                row.setPrice(quoteData.getPrice());
                holdingDatas.add(row);
            }

            if (!holdingDatas.isEmpty()) {
                setTotalGainPercent(FinancialUtils.computeGainPercent(totalValue, totalBasis));
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load portfolio data for JSF view", exception);
        }
    }

    public String sell() {
        String userId = sessionFacade.getUserId(session(false));
        if (userId == null || dataTable == null) {
            return "sell";
        }

        HoldingDataView holdingData = (HoldingDataView) dataTable.getRowData();
        try {
            OrderDataBean orderDataBean = tradeServices.sell(userId, holdingData.getHoldingID(), TradeConfig.getOrderProcessingMode());
            holdingDatas.remove(holdingData);
            OrderData orderData = new OrderData(orderDataBean.getOrderID(), orderDataBean.getOrderStatus(), orderDataBean.getOpenDate(),
                    orderDataBean.getCompletionDate(), orderDataBean.getOrderFee(), orderDataBean.getOrderType(),
                    orderDataBean.getQuantity(), orderDataBean.getSymbol());
            session(true).setAttribute("orderData", orderData);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to complete portfolio sell action", exception);
        }

        return "sell";
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

    public BigDecimal getTotalGain() {
        return totalGain;
    }

    public void setTotalGain(BigDecimal totalGain) {
        this.totalGain = totalGain;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public BigDecimal getTotalBasis() {
        return totalBasis;
    }

    public void setTotalBasis(BigDecimal totalBasis) {
        this.totalBasis = totalBasis;
    }

    public BigDecimal getTotalGainPercent() {
        return totalGainPercent;
    }

    public void setTotalGainPercent(BigDecimal totalGainPercent) {
        this.totalGainPercent = totalGainPercent;
    }

    public String getTotalGainPercentHTML() {
        return FinancialUtils.printGainPercentHTML(totalGainPercent);
    }

    public ArrayList<HoldingDataView> getHoldingDatas() {
        return holdingDatas;
    }

    public void setHoldingDatas(ArrayList<HoldingDataView> holdingDatas) {
        this.holdingDatas = holdingDatas;
    }

    public HtmlDataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(HtmlDataTable dataTable) {
        this.dataTable = dataTable;
    }
}