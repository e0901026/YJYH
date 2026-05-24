package com.yjyh.phoneloan.backend.event;

import com.yjyh.phoneloan.backend.auth.AuthService;
import com.yjyh.phoneloan.backend.event.EventDtos.EventRequest;
import com.yjyh.phoneloan.backend.event.EventDtos.EventResponse;
import com.yjyh.phoneloan.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final AuthService authService;

    public EventController(EventService eventService, AuthService authService) {
        this.eventService = eventService;
        this.authService = authService;
    }

    @PostMapping
    EventResponse record(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody EventRequest request) {
        User user = authorization == null ? null : authService.requireUser(authorization);
        return eventService.record(request, user);
    }
}
