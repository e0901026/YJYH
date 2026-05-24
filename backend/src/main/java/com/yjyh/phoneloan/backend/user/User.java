package com.yjyh.phoneloan.backend.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;
    private String employeeNo;
    private String name;
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private UUID invitedByUserId;
    private int inviteQuotaUsed;
    private Instant createdAt;
    private Instant updatedAt;

    protected User() {
    }

    public User(UUID id, String employeeNo, String name, String passwordHash, UserRole role, UUID invitedByUserId, Instant now) {
        this.id = id;
        this.employeeNo = employeeNo;
        this.name = name;
        this.passwordHash = passwordHash;
        this.role = role;
        this.invitedByUserId = invitedByUserId;
        this.inviteQuotaUsed = 0;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getEmployeeNo() { return employeeNo; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
    public UserRole getRole() { return role; }
    public UUID getInvitedByUserId() { return invitedByUserId; }
    public int getInviteQuotaUsed() { return inviteQuotaUsed; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void increaseInviteQuota(Instant now) {
        this.inviteQuotaUsed += 1;
        this.updatedAt = now;
    }
}
