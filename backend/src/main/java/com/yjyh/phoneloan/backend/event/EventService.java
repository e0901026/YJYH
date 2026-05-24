package com.yjyh.phoneloan.backend.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yjyh.phoneloan.backend.event.EventDtos.EventRequest;
import com.yjyh.phoneloan.backend.event.EventDtos.EventResponse;
import com.yjyh.phoneloan.backend.user.User;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class EventService {
    private final AppEventRepository appEventRepository;
    private final ObjectMapper objectMapper;

    public EventService(AppEventRepository appEventRepository, ObjectMapper objectMapper) {
        this.appEventRepository = appEventRepository;
        this.objectMapper = objectMapper;
    }

    public EventResponse record(EventRequest request, User user) {
        AppEvent event = appEventRepository.save(new AppEvent(
            UUID.randomUUID(),
            user == null ? null : user.getId(),
            request.sessionId(),
            request.eventName(),
            request.screen(),
            request.action(),
            request.result(),
            request.severity(),
            toSafeJson(request.context()),
            request.appVersion(),
            request.deviceModel(),
            request.osVersion(),
            Instant.now()
        ));
        return new EventResponse(event.getId().toString());
    }

    private String toSafeJson(Map<String, Object> context) {
        if (context == null || context.isEmpty()) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(context);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }
}
