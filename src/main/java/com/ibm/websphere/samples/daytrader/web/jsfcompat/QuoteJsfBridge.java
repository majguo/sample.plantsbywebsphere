package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.web.jsf.OrderData;
import com.ibm.websphere.samples.daytrader.web.jsf.QuoteData;
import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

import jakarta.annotation.PostConstruct;
import jakarta.faces.component.html.HtmlDataTable;

@Component("quotedata")
@RequestScope
public class QuoteJsfBridge extends JsfFacesSupport {

    private final TradeServices tradeServices;
    private final CompatibilitySessionFacade sessionFacade;

    private QuoteData[] quotes;
    private String symbols;
    private HtmlDataTable dataTable;
    private Integer quantity = 100;

    public QuoteJsfBridge(TradeServices tradeServices, CompatibilitySessionFacade sessionFacade) {
        this.tradeServices = tradeServices;
        this.sessionFacade = sessionFacade;
    }

    @PostConstruct
    public void getAllQuotes() {
        getQuotesBySymbols();
    }

    public String getQuotesBySymbols() {
        if (symbols == null && session(true).getAttribute("symbols") == null) {
            setSymbols("s:0,s:1,s:2,s:3,s:4");
            session(true).setAttribute("symbols", getSymbols());
        } else if (symbols == null) {
            setSymbols((String) session(true).getAttribute("symbols"));
        } else {
            session(true).setAttribute("symbols", getSymbols());
        }

        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(symbols, " ,");
        QuoteData[] rows = new QuoteData[tokenizer.countTokens()];
        int index = 0;

        while (tokenizer.hasMoreElements()) {
            String symbol = tokenizer.nextToken();
            try {
                QuoteDataBean quoteData = tradeServices.getQuote(symbol);
                rows[index++] = new QuoteData(quoteData.getOpen(), quoteData.getPrice(), quoteData.getSymbol(), quoteData.getHigh(),
                        quoteData.getLow(), quoteData.getCompanyName(), quoteData.getVolume(), quoteData.getChange());
            } catch (Exception exception) {
                Log.error(exception.toString());
            }
        }

        setQuotes(rows);
        return "quotes";
    }

    public String buy() {
        String userId = sessionFacade.getUserId(session(false));
        if (userId == null || dataTable == null) {
            return "buy";
        }

        QuoteData quoteData = (QuoteData) dataTable.getRowData();
        try {
            OrderDataBean orderDataBean = tradeServices.buy(userId, quoteData.getSymbol(), quantity.doubleValue(),
                    TradeConfig.getOrderProcessingMode());
            OrderData orderData = new OrderData(orderDataBean.getOrderID(), orderDataBean.getOrderStatus(), orderDataBean.getOpenDate(),
                    orderDataBean.getCompletionDate(), orderDataBean.getOrderFee(), orderDataBean.getOrderType(),
                    orderDataBean.getQuantity(), orderDataBean.getSymbol());
            session(true).setAttribute("orderData", orderData);
        } catch (Exception exception) {
            Log.error(exception.toString());
            throw new IllegalStateException("Unable to complete quote buy action", exception);
        }
        return "buy";
    }

    public QuoteData[] getQuotes() {
        return quotes;
    }

    public void setQuotes(QuoteData[] quotes) {
        this.quotes = quotes;
    }

    public String getSymbols() {
        return symbols;
    }

    public void setSymbols(String symbols) {
        this.symbols = symbols;
    }

    public HtmlDataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(HtmlDataTable dataTable) {
        this.dataTable = dataTable;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}