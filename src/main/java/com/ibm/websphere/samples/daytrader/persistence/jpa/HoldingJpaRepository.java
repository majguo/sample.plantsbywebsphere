package com.ibm.websphere.samples.daytrader.persistence.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.websphere.samples.daytrader.entities.HoldingDataBean;

public interface HoldingJpaRepository extends JpaRepository<HoldingDataBean, Integer> {

    @EntityGraph(attributePaths = { "account", "quote" })
    List<HoldingDataBean> findByAccountProfileUserID(String userID);
}