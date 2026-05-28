package com.ibm.websphere.samples.daytrader.web.mvc;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.application.TradeServicesFacade;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;

@RestController
@RequestMapping("/rest/quotes")
public class QuoteRestController {

    private final TradeServicesFacade tradeServicesFacade;

    public QuoteRestController(TradeServicesFacade tradeServicesFacade) {
        this.tradeServicesFacade = tradeServicesFacade;
    }

    @GetMapping(path = "/{symbols}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuoteDataBean> quotesGet(@PathVariable String symbols) throws Exception {
        return getQuotes(symbols);
    }

    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<QuoteDataBean> quotesPost(@RequestParam String symbols) throws Exception {
        return getQuotes(symbols);
    }

    private List<QuoteDataBean> getQuotes(String symbols) throws Exception {
        List<QuoteDataBean> quoteDataBeans = new ArrayList<QuoteDataBean>();
        for (String symbol : symbols.split(",")) {
            quoteDataBeans.add(tradeServicesFacade.getQuote(symbol.trim()));
        }
        return quoteDataBeans;
    }
}