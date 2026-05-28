package com.ibm.websphere.samples.daytrader.application.orders;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.config.RuntimeSettingsService;
import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;
import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.entities.OrderWorkItemEntity;
import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.persistence.jdbc.KeySequenceJdbcRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.AccountProfileJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.HoldingJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.OrderJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.OrderWorkItemJpaRepository;
import com.ibm.websphere.samples.daytrader.persistence.jpa.QuoteJpaRepository;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@Service
public class TradeOrderApplicationService {

    private final AccountProfileJpaRepository accountProfileRepository;
    private final QuoteJpaRepository quoteRepository;
    private final OrderJpaRepository orderRepository;
    private final HoldingJpaRepository holdingRepository;
    private final KeySequenceJdbcRepository keySequenceRepository;
    private final RuntimeSettingsService runtimeSettings;
    private final OrderWorkItemJpaRepository orderWorkItemRepository;
    private final RecentQuotePriceChangeList recentQuotePriceChangeList;

    public TradeOrderApplicationService(
            AccountProfileJpaRepository accountProfileRepository,
            QuoteJpaRepository quoteRepository,
            OrderJpaRepository orderRepository,
            HoldingJpaRepository holdingRepository,
            KeySequenceJdbcRepository keySequenceRepository,
            RuntimeSettingsService runtimeSettings,
            OrderWorkItemJpaRepository orderWorkItemRepository,
            RecentQuotePriceChangeList recentQuotePriceChangeList) {
        this.accountProfileRepository = accountProfileRepository;
        this.quoteRepository = quoteRepository;
        this.orderRepository = orderRepository;
        this.holdingRepository = holdingRepository;
        this.keySequenceRepository = keySequenceRepository;
        this.runtimeSettings = runtimeSettings;
        this.orderWorkItemRepository = orderWorkItemRepository;
        this.recentQuotePriceChangeList = recentQuotePriceChangeList;
    }

    @Transactional
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        AccountProfileDataBean profile = getRequiredProfile(userID);
        AccountDataBean account = profile.getAccount();
        QuoteDataBean quote = getRequiredQuoteForUpdate(symbol);

        OrderDataBean order = createOrder(account, quote, null, "buy", quantity);
        BigDecimal total = BigDecimal.valueOf(quantity).multiply(quote.getPrice()).add(order.getOrderFee());
        account.setBalance(account.getBalance().subtract(total));

        if (orderProcessingMode == TradeConfig.SYNCH) {
            return completeOrderInternal(order.getOrderID());
        }
        enqueueOrderWork(order.getOrderID(), orderProcessingMode == TradeConfig.ASYNCH_2PHASE);
        return order;
    }

    @Transactional
    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) {
        AccountProfileDataBean profile = getRequiredProfile(userID);
        AccountDataBean account = profile.getAccount();
        HoldingDataBean holding = holdingRepository.findById(holdingID).orElse(null);

        if (holding == null) {
            OrderDataBean cancelledOrder = new OrderDataBean();
            cancelledOrder.setOrderStatus("cancelled");
            return orderRepository.save(cancelledOrder);
        }

        QuoteDataBean quote = getRequiredQuoteForUpdate(holding.getQuote().getSymbol());
        OrderDataBean order = createOrder(account, quote, holding, "sell", holding.getQuantity());
        holding.setPurchaseDate(new Timestamp(0));

        BigDecimal total = BigDecimal.valueOf(holding.getQuantity()).multiply(quote.getPrice()).subtract(order.getOrderFee());
        account.setBalance(account.getBalance().add(total));

        if (orderProcessingMode == TradeConfig.SYNCH) {
            return completeOrderInternal(order.getOrderID());
        }
        enqueueOrderWork(order.getOrderID(), orderProcessingMode == TradeConfig.ASYNCH_2PHASE);
        return order;
    }

    @Transactional
    public void queueOrder(Integer orderID, boolean twoPhase) {
        enqueueOrderWork(orderID, twoPhase);
    }

    @Transactional
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) {
        return completeOrderInternal(orderID);
    }

    public Future<OrderDataBean> completeOrderAsync(Integer orderID, boolean twoPhase) {
        enqueueOrderWork(orderID, twoPhase);
        return CompletableFuture.completedFuture(getRequiredOrder(orderID));
    }

    @Transactional
    public void cancelOrder(Integer orderID, boolean twoPhase) {
        OrderDataBean order = getRequiredOrder(orderID);
        order.cancel();
        order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
    }

    @Transactional
    public void orderCompleted(String userID, Integer orderID) {
        getRequiredOrder(orderID);
    }

    @Transactional(readOnly = true)
    public Collection<OrderDataBean> getOrders(String userID) {
        return orderRepository.findByAccountProfileUserID(userID);
    }

    @Transactional
    public Collection<OrderDataBean> getClosedOrders(String userID) {
        List<OrderDataBean> closedOrders = orderRepository.findByAccountProfileUserIDAndOrderStatus(userID, "closed");
        for (OrderDataBean order : closedOrders) {
            if (runtimeSettings.isLongRun()) {
                orderRepository.delete(order);
            } else {
                order.setOrderStatus("completed");
            }
        }
        return closedOrders;
    }

    @Transactional
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) {
        QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
        return quoteRepository.save(quote);
    }

    @Transactional(readOnly = true)
    public QuoteDataBean getQuote(String symbol) {
        return quoteRepository.findById(symbol).orElse(null);
    }

    @Transactional(readOnly = true)
    public Collection<QuoteDataBean> getAllQuotes() {
        return quoteRepository.findAll();
    }

    @Transactional
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) {
        if (!runtimeSettings.isUpdateQuotePrices()) {
            return quoteRepository.findById(symbol).orElse(new QuoteDataBean(symbol));
        }

        QuoteDataBean quote = getRequiredQuoteForUpdate(symbol);
        BigDecimal oldPrice = quote.getPrice();
        BigDecimal openPrice = quote.getOpen();

        if (oldPrice.equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        } else if (oldPrice.compareTo(TradeConfig.MAXIMUM_STOCK_PRICE) > 0) {
            changeFactor = TradeConfig.MAXIMUM_STOCK_SPLIT_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, RoundingMode.HALF_UP);
        quote.setPrice(newPrice);
        quote.setChange(newPrice.subtract(openPrice).doubleValue());
        quote.setVolume(quote.getVolume() + sharesTraded);
        recentQuotePriceChangeList.add(quote);
        return quote;
    }

    @Transactional(readOnly = true)
    public Collection<HoldingDataBean> getHoldings(String userID) {
        return holdingRepository.findByAccountProfileUserID(userID);
    }

    @Transactional(readOnly = true)
    public HoldingDataBean getHolding(Integer holdingID) {
        return holdingRepository.findById(holdingID).orElse(null);
    }

    @Transactional
    public OrderDataBean createOrder(
            AccountDataBean account,
            QuoteDataBean quote,
            HoldingDataBean holding,
            String orderType,
            double quantity) {
        OrderDataBean order = new OrderDataBean(
                orderType,
                "open",
                new Timestamp(System.currentTimeMillis()),
                null,
                quantity,
                quote.getPrice().setScale(FinancialUtils.SCALE, RoundingMode.HALF_UP),
                TradeConfig.getOrderFee(orderType),
                account,
                quote,
                holding);
            order.setOrderID(keySequenceRepository.nextValue("order", 1));
        return orderRepository.save(order);
    }

    private OrderDataBean completeOrderInternal(Integer orderID) {
        OrderDataBean order = getRequiredOrder(orderID);
        if (order.isCompleted()) {
            throw new IllegalStateException("Order already completed: " + orderID);
        }

        QuoteDataBean quote = getRequiredQuoteForUpdate(order.getQuote().getSymbol());
        double quantity = order.getQuantity();

        if (order.isBuy()) {
            HoldingDataBean newHolding = new HoldingDataBean(
                    quantity,
                    order.getPrice(),
                    new Timestamp(System.currentTimeMillis()),
                    order.getAccount(),
                quote);
            newHolding.setHoldingID(keySequenceRepository.nextValue("holding", 1));
            newHolding = holdingRepository.save(newHolding);
            order.setHolding(newHolding);
            order.setOrderStatus("closed");
            order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
            updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
            return order;
        }

        if (order.isSell()) {
            HoldingDataBean holding = order.getHolding();
            if (holding == null) {
                order.cancel();
                order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
                return order;
            }

            holdingRepository.delete(holding);
            order.setHolding(null);
            order.setOrderStatus("closed");
            order.setCompletionDate(new Timestamp(System.currentTimeMillis()));
            updateQuotePriceVolume(quote.getSymbol(), TradeConfig.getRandomPriceChangeFactor(), quantity);
        }

        return order;
    }

    private AccountProfileDataBean getRequiredProfile(String userID) {
        return accountProfileRepository.findById(userID)
                .orElseThrow(() -> new IllegalStateException("No such user: " + userID));
    }

    private OrderDataBean getRequiredOrder(Integer orderID) {
        return orderRepository.findById(orderID)
                .orElseThrow(() -> new IllegalStateException("No such order: " + orderID));
    }

    private QuoteDataBean getRequiredQuoteForUpdate(String symbol) {
        return quoteRepository.findBySymbolForUpdate(symbol)
                .orElseThrow(() -> new IllegalStateException("No such symbol: " + symbol));
    }

    private void enqueueOrderWork(Integer orderID, boolean twoPhase) {
        OrderWorkItemEntity existing = orderWorkItemRepository.findByOrderId(orderID).orElse(null);
        if (existing != null) {
            existing.setTwoPhase(twoPhase);
            if (OrderWorkItemEntity.STATUS_FAILED.equals(existing.getStatus())) {
                existing.setStatus(OrderWorkItemEntity.STATUS_RETRY);
                existing.setAvailableAt(new Date(System.currentTimeMillis()));
                existing.setLastError(null);
            }
            return;
        }

        Date now = new Date(System.currentTimeMillis());
        OrderWorkItemEntity workItem = new OrderWorkItemEntity();
        workItem.setWorkId(keySequenceRepository.nextValue("orderwork", 1));
        workItem.setOrderId(orderID);
        workItem.setTwoPhase(twoPhase);
        workItem.setStatus(OrderWorkItemEntity.STATUS_PENDING);
        workItem.setAttemptCount(0);
        workItem.setMaxAttempts(5);
        workItem.setAvailableAt(now);
        workItem.setUpdatedAt(now);
        orderWorkItemRepository.save(workItem);
    }
}