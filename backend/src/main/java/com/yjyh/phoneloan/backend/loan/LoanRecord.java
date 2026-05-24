package com.yjyh.phoneloan.backend.loan;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "loan_records")
public class LoanRecord {
    @Id
    private UUID id;
    private UUID deviceId;
    private UUID borrowerUserId;
    private UUID previousHolderUserId;
    private UUID ownerUserId;
    private Instant startedAt;
    private Instant endedAt;
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    protected LoanRecord() {
    }

    public LoanRecord(UUID id, UUID deviceId, UUID borrowerUserId, UUID previousHolderUserId, UUID ownerUserId, Instant now) {
        this.id = id;
        this.deviceId = deviceId;
        this.borrowerUserId = borrowerUserId;
        this.previousHolderUserId = previousHolderUserId;
        this.ownerUserId = ownerUserId;
        this.startedAt = now;
        this.status = LoanStatus.ACTIVE;
    }

    public UUID getId() { return id; }
    public UUID getDeviceId() { return deviceId; }
    public UUID getBorrowerUserId() { return borrowerUserId; }
    public UUID getPreviousHolderUserId() { return previousHolderUserId; }
    public UUID getOwnerUserId() { return ownerUserId; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getEndedAt() { return endedAt; }
    public LoanStatus getStatus() { return status; }

    public void markReturned(Instant now) {
        this.endedAt = now;
        this.status = LoanStatus.RETURNED;
    }
}
