package com.yjyh.phoneloan.backend.device;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    private UUID id;
    private String name;
    private String imei1;
    private String imei2;
    private UUID ownerUserId;
    private UUID currentHolderUserId;
    @Enumerated(EnumType.STRING)
    private DeviceStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    protected Device() {
    }

    public Device(UUID id, String name, String imei1, String imei2, UUID ownerUserId, UUID holderUserId, Instant now) {
        this.id = id;
        this.name = name;
        this.imei1 = imei1;
        this.imei2 = imei2;
        this.ownerUserId = ownerUserId;
        this.currentHolderUserId = holderUserId;
        this.status = DeviceStatus.HELD_BY_ME;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getImei1() { return imei1; }
    public String getImei2() { return imei2; }
    public UUID getOwnerUserId() { return ownerUserId; }
    public UUID getCurrentHolderUserId() { return currentHolderUserId; }
    public DeviceStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateHolder(UUID holderUserId, DeviceStatus status, Instant now) {
        this.currentHolderUserId = holderUserId;
        this.status = status;
        this.updatedAt = now;
    }
}
