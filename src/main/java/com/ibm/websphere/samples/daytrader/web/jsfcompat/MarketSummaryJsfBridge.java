package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.web.jsf.QuoteData;

import jakarta.annotation.PostConstruct;

@Component("marketdata")
@RequestScope
public class MarketSummaryJsfBridge {

    private final TradeServices tradeServices;

    private BigDecimal TSIA;
    private BigDecimal openTSIA;
    private double volume;
    private QuoteData[] topGainers;
    private QuoteData[] topLosers;
    private Date summaryDate;
    private BigDecimal gainPercent;

    public MarketSummaryJsfBridge(TradeServices tradeServices) {
        this.tradeServices = tradeServices;
    }

    @PostConstruct
    public void getMarketSummary() {
        try {
            MarketSummaryDataBean marketSummaryData = tradeServices.getMarketSummary();
            setSummaryDate(marketSummaryData.getSummaryDate());
            setTSIA(marketSummaryData.getTSIA());
            setOpenTSIA(marketSummaryData.getOpenTSIA());
            setVolume(marketSummaryData.getVolume());
            setGainPercent(marketSummaryData.getGainPercent());
            setTopGainers(toQuoteRows(marketSummaryData.getTopGainers()));
            setTopLosers(toQuoteRows(marketSummaryData.getTopLosers()));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load market summary for JSF view", exception);
        }
    }

    private QuoteData[] toQuoteRows(Collection<?> source) {
        QuoteData[] rows = new QuoteData[Math.min(source.size(), 5)];
        int index = 0;
        for (Object candidate : source) {
            if (index >= rows.length) {
                break;
            }
            QuoteDataBean quote = (QuoteDataBean) candidate;
            rows[index++] = new QuoteData(quote.getPrice(), quote.getOpen(), quote.getSymbol());
        }
        return rows;
    }

    public BigDecimal getTSIA() {
        return TSIA;
    }

    public void setTSIA(BigDecimal tSIA) {
        TSIA = tSIA;
    }

    public BigDecimal getOpenTSIA() {
        return openTSIA;
    }

    public void setOpenTSIA(BigDecimal openTSIA) {
        this.openTSIA = openTSIA;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public QuoteData[] getTopGainers() {
        return topGainers;
    }

    public void setTopGainers(QuoteData[] topGainers) {
        this.topGainers = topGainers;
    }

    public QuoteData[] getTopLosers() {
        return topLosers;
    }

    public void setTopLosers(QuoteData[] topLosers) {
        this.topLosers = topLosers;
    }

    public Date getSummaryDate() {
        return summaryDate;
    }

    public void setSummaryDate(Date summaryDate) {
        this.summaryDate = summaryDate;
    }

    public BigDecimal getGainPercent() {
        return gainPercent;
    }

    public void setGainPercent(BigDecimal gainPercent) {
        this.gainPercent = gainPercent == null ? null : gainPercent.setScale(2, RoundingMode.HALF_UP);
    }

    public String getGainPercentHTML() {
        return FinancialUtils.printGainPercentHTML(gainPercent);
    }
}