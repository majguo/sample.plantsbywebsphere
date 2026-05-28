package com.ibm.websphere.samples.daytrader.streaming;

import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;

public record MarketSummaryUpdatedEvent(MarketSummaryDataBean summary) {
}