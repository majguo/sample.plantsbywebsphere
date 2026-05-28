/**
 * (C) Copyright IBM Corporation 2019.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.util;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.ibm.websphere.samples.daytrader.streaming.QuotePriceChangeEvent;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;


/** This class is a holds the last 5 stock changes, used by the MarketSummary WebSocket
 *  and the JAX-RS SSE Broadcaster
 *  It fires a CDI event everytime a price change is added
 **/

@Component
public class RecentQuotePriceChangeList  {

  private final CopyOnWriteArrayList<QuoteDataBean> list = new CopyOnWriteArrayList<QuoteDataBean>();
  private final ApplicationEventPublisher eventPublisher;
  private final int maxSize = 5;

  public RecentQuotePriceChangeList(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public boolean add(QuoteDataBean quoteData) {

    int symbolNumber = new Integer(quoteData.getSymbol().substring(2));

    if ( symbolNumber < TradeConfig.getMAX_QUOTES() * TradeConfig.getListQuotePriceChangeFrequency() * 0.01) {
      list.add(0, quoteData);

      // Add stock, remove if needed
      if(list.size() > maxSize) {
        list.remove(maxSize);
      }
      eventPublisher.publishEvent(new QuotePriceChangeEvent(recentList()));
      return true;
    }
    return false;
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Size(max=5)
  @NotEmpty
  public List<@NotNull QuoteDataBean> recentList() {
    return new CopyOnWriteArrayList<QuoteDataBean>(list);
  }
}
