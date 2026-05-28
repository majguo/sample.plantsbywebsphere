package com.ibm.websphere.samples.daytrader.application.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.ibm.websphere.samples.daytrader.util.RecentQuotePriceChangeList;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

@ExtendWith(MockitoExtension.class)
class TradeOrderApplicationServiceTest {

    @Mock
    private AccountProfileJpaRepository accountProfileRepository;
    @Mock
    private QuoteJpaRepository quoteRepository;
    @Mock
    private OrderJpaRepository orderRepository;
    @Mock
    private HoldingJpaRepository holdingRepository;
    @Mock
    private KeySequenceJdbcRepository keySequenceRepository;
    @Mock
    private RuntimeSettingsService runtimeSettings;
    @Mock
    private OrderWorkItemJpaRepository orderWorkItemRepository;
    @Mock
    private RecentQuotePriceChangeList recentQuotePriceChangeList;

    private TradeOrderApplicationService service;

    @BeforeEach
    void setUp() {
        service = new TradeOrderApplicationService(
                accountProfileRepository,
                quoteRepository,
                orderRepository,
                holdingRepository,
                keySequenceRepository,
                runtimeSettings,
                orderWorkItemRepository,
                recentQuotePriceChangeList);
    }

    @Test
    void buyQueuesDurableWorkForAsyncMode() {
        AccountDataBean account = new AccountDataBean();
        account.setAccountID(7);
        account.setBalance(new BigDecimal("1000.00"));
        account.setCreationDate(new Date());
        account.setOpenBalance(new BigDecimal("1000.00"));

        AccountProfileDataBean profile = new AccountProfileDataBean();
        profile.setUserID("uid:1");
        profile.setAccount(account);

        QuoteDataBean quote = new QuoteDataBean();
        quote.setSymbol("s:1");
        quote.setPrice(new BigDecimal("25.00"));
        quote.setOpen(new BigDecimal("24.00"));
        quote.setLow(new BigDecimal("24.00"));
        quote.setHigh(new BigDecimal("25.00"));
        quote.setChange(1.0d);
        quote.setVolume(10d);
        quote.setCompanyName("Sample");

        when(accountProfileRepository.findById("uid:1")).thenReturn(Optional.of(profile));
        when(quoteRepository.findBySymbolForUpdate("s:1")).thenReturn(Optional.of(quote));
        when(keySequenceRepository.nextValue("order", 1)).thenReturn(101);
        when(keySequenceRepository.nextValue("orderwork", 1)).thenReturn(501);
        when(orderWorkItemRepository.findByOrderId(101)).thenReturn(Optional.empty());
        when(orderRepository.save(any(OrderDataBean.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDataBean order = service.buy("uid:1", "s:1", 2.0d, TradeConfig.ASYNCH);

        assertEquals("open", order.getOrderStatus());
        assertEquals(Integer.valueOf(101), order.getOrderID());
        assertEquals(
            0,
            new BigDecimal("1000.00").subtract(new BigDecimal("50.00")).subtract(TradeConfig.getOrderFee("buy"))
                .compareTo(account.getBalance()));
        verify(orderWorkItemRepository).save(argThat(workItem ->
                workItem.getOrderId().equals(101)
                        && OrderWorkItemEntity.STATUS_PENDING.equals(workItem.getStatus())
                        && !workItem.isTwoPhase()));
        verify(holdingRepository, never()).save(any(HoldingDataBean.class));
    }

    @Test
    void queueOrderIsIdempotentWhenWorkAlreadyExists() {
        OrderWorkItemEntity existing = new OrderWorkItemEntity();
        existing.setWorkId(99);
        existing.setOrderId(101);
        existing.setStatus(OrderWorkItemEntity.STATUS_PENDING);
        existing.setTwoPhase(false);

        when(orderWorkItemRepository.findByOrderId(101)).thenReturn(Optional.of(existing));

        service.queueOrder(101, false);

        assertFalse(existing.isTwoPhase());
        verify(orderWorkItemRepository, never()).save(any(OrderWorkItemEntity.class));
    }
}