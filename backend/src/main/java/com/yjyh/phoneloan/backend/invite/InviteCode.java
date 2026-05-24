package com.yjyh.phoneloan.backend.invite;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invite_codes")
public class InviteCode {
    @Id
    private UUID id;
    private String code;
    private UUID createdByUserId;
    private UUID usedByUserId;
    @Enumerated(EnumType.STRING)
    private InviteCodeStatus status;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;

    protected InviteCode() {
    }

    public InviteCode(UUID id, String code, UUID createdByUserId, Instant expiresAt, Instant now) {
        this.id = id;
        this.code = code;
        this.createdByUserId = createdByUserId;
        this.status = InviteCodeStatus.UNUSED;
        this.expiresAt = expiresAt;
        this.createdAt = now;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public UUID getCreatedByUserId() { return createdByUserId; }
    public UUID getUsedByUserId() { return usedByUserId; }
    public InviteCodeStatus getStatus() { return status; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }

    public boolean canUse(Instant now) {
        return status == InviteCodeStatus.UNUSED && expiresAt.isAfter(now);
    }

    public void markUsed(UUID userId, Instant now) {
        this.usedByUserId = userId;
        this.usedAt = now;
        this.status = InviteCodeStatus.USED;
    }
}
