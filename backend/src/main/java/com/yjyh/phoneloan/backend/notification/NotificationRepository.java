package com.yjyh.phoneloan.backend.notification;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationRecord, UUID> {
    List<NotificationRecord> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
}
