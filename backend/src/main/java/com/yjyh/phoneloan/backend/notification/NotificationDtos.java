package com.yjyh.phoneloan.backend.notification;

public final class NotificationDtos {
    private NotificationDtos() {
    }

    public record NotificationResponse(
        String id,
        String type,
        String title,
        String content,
        String relatedDeviceId,
        String relatedLoanRecordId,
        boolean read
    ) {}
}
