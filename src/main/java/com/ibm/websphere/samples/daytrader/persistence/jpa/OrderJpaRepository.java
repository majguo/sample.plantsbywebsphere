package com.ibm.websphere.samples.daytrader.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.websphere.samples.daytrader.entities.OrderDataBean;

public interface OrderJpaRepository extends JpaRepository<OrderDataBean, Integer> {

    @EntityGraph(attributePaths = { "account", "account.profile", "holding", "quote" })
    List<OrderDataBean> findByAccountProfileUserID(String userID);

    @EntityGraph(attributePaths = { "account", "account.profile", "holding", "quote" })
    List<OrderDataBean> findByAccountProfileUserIDAndOrderStatus(String userID, String orderStatus);
}