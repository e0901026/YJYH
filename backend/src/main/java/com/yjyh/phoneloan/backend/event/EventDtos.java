package com.yjyh.phoneloan.backend.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public final class EventDtos {
    private EventDtos() {
    }

    public record EventRequest(
        @NotBlank String sessionId,
        @NotBlank String eventName,
        @NotBlank String screen,
        @NotBlank String action,
        @NotNull EventResult result,
        @NotNull EventSeverity severity,
        Map<String, Object> context,
        String appVersion,
        String deviceModel,
        String osVersion
    ) {}

    public record EventResponse(String id) {}
}
