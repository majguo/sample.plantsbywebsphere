package com.ibm.websphere.samples.daytrader.persistence.jdbc;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class KeySequenceJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public KeySequenceJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public int nextValue(String keyName, int allocationSize) {
        Integer currentValue = jdbcTemplate.queryForObject(
                "SELECT KEYVAL FROM KEYGENEJB WHERE KEYNAME = ?",
                Integer.class,
                keyName);
        if (currentValue == null) {
            throw new IllegalStateException("Missing KEYGENEJB row for key " + keyName);
        }

        int nextValue = currentValue + allocationSize;
        int updated = jdbcTemplate.update(
                "UPDATE KEYGENEJB SET KEYVAL = ? WHERE KEYNAME = ?",
                nextValue,
                keyName);
        if (updated != 1) {
            throw new IllegalStateException("Failed to advance KEYGENEJB row for key " + keyName);
        }
        return nextValue;
    }
}