package com.ibm.websphere.samples.daytrader.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.websphere.samples.daytrader.entities.AccountDataBean;

public interface AccountDataJpaRepository extends JpaRepository<AccountDataBean, Integer> {
}