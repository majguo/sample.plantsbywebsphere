package com.ibm.websphere.samples.daytrader.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity(name = "orderworkejb")
@Table(name = "ORDERWORKEJB")
public class OrderWorkItemEntity implements Serializable {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_RETRY = "retry";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_FAILED = "failed";

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "WORKID", nullable = false)
    private Integer workId;

    @Column(name = "ORDERID", nullable = false, unique = true)
    private Integer orderId;

    @Column(name = "TWOPHASE", nullable = false)
    private short twoPhaseFlag;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "ATTEMPTCOUNT", nullable = false)
    private int attemptCount;

    @Column(name = "MAXATTEMPTS", nullable = false)
    private int maxAttempts;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "AVAILABLEAT", nullable = false)
    private Date availableAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATEDAT", nullable = false)
    private Date updatedAt;

    @Column(name = "LASTERROR", length = 512)
    private String lastError;

    public Integer getWorkId() {
        return workId;
    }

    public void setWorkId(Integer workId) {
        this.workId = workId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public boolean isTwoPhase() {
        return twoPhaseFlag == 1;
    }

    public void setTwoPhase(boolean twoPhase) {
        this.twoPhaseFlag = (short) (twoPhase ? 1 : 0);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Date getAvailableAt() {
        return availableAt;
    }

    public void setAvailableAt(Date availableAt) {
        this.availableAt = availableAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}