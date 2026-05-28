package com.ibm.websphere.samples.daytrader.persistence.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibm.websphere.samples.daytrader.entities.QuoteDataBean;

public interface QuoteJpaRepository extends JpaRepository<QuoteDataBean, String> {

    List<QuoteDataBean> findAllByOrderByChange1Desc();

    List<QuoteDataBean> findTop5ByOrderByChange1Desc();

    List<QuoteDataBean> findTop5ByOrderByChange1Asc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select q from quoteejb q where q.symbol = :symbol")
    Optional<QuoteDataBean> findBySymbolForUpdate(@Param("symbol") String symbol);
}