package com.ibm.websphere.samples.daytrader.web.jsfcompat;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;
import com.ibm.websphere.samples.daytrader.interfaces.TradeServices;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.web.jsf.OrderData;
import com.ibm.websphere.samples.daytrader.web.mvc.CompatibilitySessionFacade;

import jakarta.annotation.PostConstruct;

@Component("orderdata")
@RequestScope
public class OrderJsfBridge extends JsfFacesSupport {

    private final TradeServices tradeServices;
    private final CompatibilitySessionFacade sessionFacade;

    private OrderData[] allOrders;
    private OrderData orderData;

    public OrderJsfBridge(TradeServices tradeServices, CompatibilitySessionFacade sessionFacade) {
        this.tradeServices = tradeServices;
        this.sessionFacade = sessionFacade;
    }

    public void getAllOrder() {
        String userId = sessionFacade.getUserId(session(false));
        if (userId == null) {
            allOrders = new OrderData[0];
            return;
        }

        try {
            ArrayList<?> orderDataBeans = TradeConfig.getLongRun() ? new ArrayList<>() : (ArrayList<?>) tradeServices.getOrders(userId);
            OrderData[] orders = new OrderData[orderDataBeans.size()];
            int index = 0;
            for (Object candidate : orderDataBeans) {
                OrderDataBean order = (OrderDataBean) candidate;
                OrderData row = new OrderData(order.getOrderID(), order.getOrderStatus(), order.getOpenDate(), order.getCompletionDate(),
                        order.getOrderFee(), order.getOrderType(), order.getQuantity(), order.getSymbol());
                row.setPrice(order.getPrice());
                row.setTotal(row.getPrice().multiply(BigDecimal.valueOf(row.getQuantity())));
                orders[index++] = row;
            }
            setAllOrders(orders);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load orders for JSF view", exception);
        }
    }

    @PostConstruct
    public void getOrder() {
        getAllOrder();
        Object currentOrder = session(true).getAttribute("orderData");
        if (currentOrder instanceof OrderData) {
            setOrderData((OrderData) currentOrder);
        }
    }

    public OrderData[] getAllOrders() {
        return allOrders;
    }

    public void setAllOrders(OrderData[] allOrders) {
        this.allOrders = allOrders;
    }

    public OrderData getOrderData() {
        return orderData;
    }

    public void setOrderData(OrderData orderData) {
        this.orderData = orderData;
    }
}