package com.yjyh.phoneloan.backend.notification;

import com.yjyh.phoneloan.backend.common.CurrentUser;
import com.yjyh.phoneloan.backend.notification.NotificationDtos.NotificationResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final CurrentUser currentUser;

    public NotificationController(NotificationService notificationService, CurrentUser currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @GetMapping
    List<NotificationResponse> mine(@RequestHeader("Authorization") String authorization) {
        return notificationService.mine(currentUser.fromAuthorization(authorization));
    }

    @PostMapping("/{id}/read")
    NotificationResponse read(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        return notificationService.markRead(id, currentUser.fromAuthorization(authorization));
    }
}
