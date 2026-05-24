package com.yjyh.phoneloan.backend.notification;

import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.notification.NotificationDtos.NotificationResponse;
import com.yjyh.phoneloan.backend.user.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void create(UUID recipientUserId, NotificationType type, String title, String content, UUID deviceId, UUID loanId) {
        if (recipientUserId == null) {
            return;
        }
        notificationRepository.save(new NotificationRecord(UUID.randomUUID(), recipientUserId, type, title, content, deviceId, loanId, Instant.now()));
    }

    public List<NotificationResponse> mine(User user) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID id, User user) {
        NotificationRecord record = notificationRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND", "通知不存在"));
        if (!record.getRecipientUserId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "无权操作该通知");
        }
        record.markRead(Instant.now());
        return toResponse(record);
    }

    private NotificationResponse toResponse(NotificationRecord record) {
        return new NotificationResponse(
            record.getId().toString(),
            record.getType().name(),
            record.getTitle(),
            record.getContent(),
            record.getRelatedDeviceId() == null ? null : record.getRelatedDeviceId().toString(),
            record.getRelatedLoanRecordId() == null ? null : record.getRelatedLoanRecordId().toString(),
            record.getReadAt() != null
        );
    }
}
