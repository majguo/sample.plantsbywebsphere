package com.ibm.websphere.samples.daytrader.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.application.auth.AuthenticationApplicationService;
import com.ibm.websphere.samples.daytrader.application.orders.TradeOrderApplicationService;
import com.ibm.websphere.samples.daytrader.beans.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.persistence.jpa.QuoteJpaRepository;

@Service
@Primary
@Transactional(readOnly = true)
public class TradeServicesFacade implements TradeServices {

    private final AuthenticationApplicationService authenticationService;
    private final TradeOrderApplicationService tradeOrderService;
    private final QuoteJpaRepository quoteRepository;
    private final RuntimeSettingsService runtimeSettings;

    private volatile MarketSummaryDataBean cachedMarketSummary;
    private volatile long nextMarketSummaryRefresh;

    public TradeServicesFacade(
            AuthenticationApplicationService authenticationService,
            TradeOrderApplicationService tradeOrderService,
            QuoteJpaRepository quoteRepository,
            RuntimeSettingsService runtimeSettings) {
        this.authenticationService = authenticationService;
        this.tradeOrderService = tradeOrderService;
        this.quoteRepository = quoteRepository;
        this.runtimeSettings = runtimeSettings;
    }

    @Override
    public MarketSummaryDataBean getMarketSummary() {
        int refreshIntervalSeconds = runtimeSettings.getMarketSummaryInterval();
        if (refreshIntervalSeconds == 0) {
            return computeMarketSummary();
        }
        if (refreshIntervalSeconds < 0) {
            if (cachedMarketSummary == null) {
                synchronized (this) {
                    if (cachedMarketSummary == null) {
                        cachedMarketSummary = computeMarketSummary();
                    }
                }
            }
            return cachedMarketSummary;
        }

        long now = System.currentTimeMillis();
        if (cachedMarketSummary != null && now < nextMarketSummaryRefresh) {
            return cachedMarketSummary;
        }

        synchronized (this) {
            if (cachedMarketSummary == null || now >= nextMarketSummaryRefresh) {
                cachedMarketSummary = computeMarketSummary();
                nextMarketSummaryRefresh = now + (refreshIntervalSeconds * 1000L);
            }
            return cachedMarketSummary;
        }
    }

    @Override
    public OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote, HoldingDataBean holding, String orderType, double quantity)
            throws Exception {
        return tradeOrderService.createOrder(account, quote, holding, orderType, quantity);
    }

    @Override
    @Transactional
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception {
        return tradeOrderService.buy(userID, symbol, quantity, orderProcessingMode);
    }

    @Override
    @Transactional
    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception {
        return tradeOrderService.sell(userID, holdingID, orderProcessingMode);
    }

    @Override
    @Transactional
    public void queueOrder(Integer orderID, boolean twoPhase) throws Exception {
        tradeOrderService.queueOrder(orderID, twoPhase);
    }

    @Override
    @Transactional
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        return tradeOrderService.completeOrder(orderID, twoPhase);
    }

    @Override
    public Future<OrderDataBean> completeOrderAsync(Integer orderID, boolean twoPhase) throws Exception {
        return tradeOrderService.completeOrderAsync(orderID, twoPhase);
    }

    @Override
    @Transactional
    public void cancelOrder(Integer orderID, boolean twoPhase) throws Exception {
        tradeOrderService.cancelOrder(orderID, twoPhase);
    }

    @Override
    @Transactional
    public void orderCompleted(String userID, Integer orderID) throws Exception {
        tradeOrderService.orderCompleted(userID, orderID);
    }

    @Override
    public Collection<?> getOrders(String userID) throws Exception {
        return tradeOrderService.getOrders(userID);
    }

    @Override
    @Transactional
    public Collection<?> getClosedOrders(String userID) throws Exception {
        return tradeOrderService.getClosedOrders(userID);
    }

    @Override
    @Transactional
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception {
        return tradeOrderService.createQuote(symbol, companyName, price);
    }

    @Override
    public QuoteDataBean getQuote(String symbol) throws Exception {
        return tradeOrderService.getQuote(symbol);
    }

    @Override
    public Collection<?> getAllQuotes() throws Exception {
        return tradeOrderService.getAllQuotes();
    }

    @Override
    @Transactional
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal newPrice, double sharesTraded) throws Exception {
        return tradeOrderService.updateQuotePriceVolume(symbol, newPrice, sharesTraded);
    }

    @Override
    public Collection<HoldingDataBean> getHoldings(String userID) throws Exception {
        return (Collection<HoldingDataBean>) tradeOrderService.getHoldings(userID);
    }

    @Override
    public HoldingDataBean getHolding(Integer holdingID) throws Exception {
        return tradeOrderService.getHolding(holdingID);
    }

    @Override
    public AccountDataBean getAccountData(String userID) throws Exception {
        return authenticationService.getAccountData(userID);
    }

    @Override
    public AccountProfileDataBean getAccountProfileData(String userID) throws Exception {
        return authenticationService.getAccountProfileData(userID);
    }

    @Override
    @Transactional
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception {
        return authenticationService.updateAccountProfile(profileData);
    }

    @Override
    @Transactional
    public AccountDataBean login(String userID, String password) throws Exception {
        return authenticationService.login(userID, password);
    }

    @Override
    @Transactional
    public void logout(String userID) throws Exception {
        authenticationService.logout(userID);
    }

    @Override
    @Transactional
    public AccountDataBean register(String userID, String password, String fullname, String address, String email, String creditcard,
            BigDecimal openBalance) throws Exception {
        return authenticationService.register(userID, password, fullname, address, email, creditcard, openBalance);
    }

    @Override
    public int getImpl() {
        return runtimeSettings.getRuntimeMode();
    }

    @Override
    public QuoteDataBean pingTwoPhase(String symbol) throws Exception {
        return getQuote(symbol);
    }

    @Override
    public double investmentReturn(double investment, double netValue) throws Exception {
        double diff = netValue - investment;
        return diff / investment;
    }

    @Override
    public void setInSession(boolean inSession) {
        // The Spring facade is stateless; session coupling lives in web adapters.
    }

    private MarketSummaryDataBean computeMarketSummary() {
        List<QuoteDataBean> quotes = quoteRepository.findAllByOrderByChange1Desc();
        if (quotes.isEmpty()) {
            return new MarketSummaryDataBean(
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    0.0,
                    new ArrayList<>(),
                    new ArrayList<>());
        }

        BigDecimal tsia = BigDecimal.ZERO;
        BigDecimal openTsia = BigDecimal.ZERO;
        double totalVolume = 0.0;
        for (QuoteDataBean quote : quotes) {
            tsia = tsia.add(quote.getPrice());
            openTsia = openTsia.add(quote.getOpen());
            totalVolume += quote.getVolume();
        }

        tsia = tsia.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);
        openTsia = openTsia.divide(BigDecimal.valueOf(quotes.size()), RoundingMode.HALF_UP);

        List<QuoteDataBean> topGainers = new ArrayList<>(quoteRepository.findTop5ByOrderByChange1Desc());
        List<QuoteDataBean> topLosers = new ArrayList<>(quoteRepository.findTop5ByOrderByChange1Asc());
        return new MarketSummaryDataBean(tsia, openTsia, totalVolume, topGainers, topLosers);
    }
}