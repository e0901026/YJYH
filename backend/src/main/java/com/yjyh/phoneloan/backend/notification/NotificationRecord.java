package com.yjyh.phoneloan.backend.notification;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationRecord {
    @Id
    private UUID id;
    private UUID recipientUserId;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String title;
    private String content;
    private UUID relatedDeviceId;
    private UUID relatedLoanRecordId;
    private Instant readAt;
    private Instant createdAt;

    protected NotificationRecord() {
    }

    public NotificationRecord(UUID id, UUID recipientUserId, NotificationType type, String title, String content, UUID deviceId, UUID loanId, Instant now) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedDeviceId = deviceId;
        this.relatedLoanRecordId = loanId;
        this.createdAt = now;
    }

    public UUID getId() { return id; }
    public UUID getRecipientUserId() { return recipientUserId; }
    public NotificationType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public UUID getRelatedDeviceId() { return relatedDeviceId; }
    public UUID getRelatedLoanRecordId() { return relatedLoanRecordId; }
    public Instant getReadAt() { return readAt; }
    public Instant getCreatedAt() { return createdAt; }

    public void markRead(Instant now) {
        this.readAt = now;
    }
}
