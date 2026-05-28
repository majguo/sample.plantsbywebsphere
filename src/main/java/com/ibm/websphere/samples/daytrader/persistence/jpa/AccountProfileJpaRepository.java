package com.ibm.websphere.samples.daytrader.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ibm.websphere.samples.daytrader.entities.AccountProfileDataBean;

public interface AccountProfileJpaRepository extends JpaRepository<AccountProfileDataBean, String> {
}