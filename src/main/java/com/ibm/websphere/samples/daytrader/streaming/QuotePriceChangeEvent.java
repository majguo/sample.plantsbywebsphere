package com.ibm.websphere.samples.daytrader.streaming;

import java.util.List;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;

public record QuotePriceChangeEvent(List<QuoteDataBean> recentQuotes) {
}