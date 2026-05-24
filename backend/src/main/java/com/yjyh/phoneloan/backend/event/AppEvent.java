package com.yjyh.phoneloan.backend.event;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "app_events")
public class AppEvent {
    @Id
    private UUID id;
    private UUID userId;
    private String sessionId;
    private String eventName;
    private String screen;
    private String action;
    @Enumerated(EnumType.STRING)
    private EventResult result;
    @Enumerated(EnumType.STRING)
    private EventSeverity severity;
    private String contextJson;
    private String appVersion;
    private String deviceModel;
    private String osVersion;
    private Instant createdAt;

    protected AppEvent() {
    }

    public AppEvent(UUID id, UUID userId, String sessionId, String eventName, String screen, String action, EventResult result, EventSeverity severity, String contextJson, String appVersion, String deviceModel, String osVersion, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.eventName = eventName;
        this.screen = screen;
        this.action = action;
        this.result = result;
        this.severity = severity;
        this.contextJson = contextJson;
        this.appVersion = appVersion;
        this.deviceModel = deviceModel;
        this.osVersion = osVersion;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getSessionId() { return sessionId; }
    public String getEventName() { return eventName; }
    public String getScreen() { return screen; }
    public String getAction() { return action; }
    public EventResult getResult() { return result; }
    public EventSeverity getSeverity() { return severity; }
    public String getContextJson() { return contextJson; }
    public String getAppVersion() { return appVersion; }
    public String getDeviceModel() { return deviceModel; }
    public String getOsVersion() { return osVersion; }
    public Instant getCreatedAt() { return createdAt; }
}
