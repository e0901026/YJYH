package com.yjyh.phoneloan.backend.loan;

import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.common.Imei;
import com.yjyh.phoneloan.backend.device.Device;
import com.yjyh.phoneloan.backend.device.DeviceRepository;
import com.yjyh.phoneloan.backend.device.DeviceService;
import com.yjyh.phoneloan.backend.device.DeviceStatus;
import com.yjyh.phoneloan.backend.loan.LoanDtos.LoanResponse;
import com.yjyh.phoneloan.backend.notification.NotificationService;
import com.yjyh.phoneloan.backend.notification.NotificationType;
import com.yjyh.phoneloan.backend.user.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanService {
    private final DeviceRepository deviceRepository;
    private final LoanRecordRepository loanRecordRepository;
    private final DeviceService deviceService;
    private final NotificationService notificationService;

    public LoanService(DeviceRepository deviceRepository, LoanRecordRepository loanRecordRepository, DeviceService deviceService, NotificationService notificationService) {
        this.deviceRepository = deviceRepository;
        this.loanRecordRepository = loanRecordRepository;
        this.deviceService = deviceService;
        this.notificationService = notificationService;
    }

    @Transactional
    public LoanResponse borrowByImei(String imei, User borrower) {
        if (!Imei.isValid(imei)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMEI_INVALID", "IMEI 必须为 15 位数字");
        }
        Device device = deviceRepository.findByImei1(imei)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", "设备未建档"));
        Instant now = Instant.now();
        UUID previousHolderId = device.getCurrentHolderUserId();
        if (borrower.getId().equals(previousHolderId)) {
            throw new ApiException(HttpStatus.CONFLICT, "DEVICE_ALREADY_HELD", "设备已在当前用户手上");
        }
        loanRecordRepository.findByDeviceIdAndStatus(device.getId(), LoanStatus.ACTIVE)
            .ifPresent(record -> record.markReturned(now));
        LoanRecord next = loanRecordRepository.save(new LoanRecord(UUID.randomUUID(), device.getId(), borrower.getId(), previousHolderId, device.getOwnerUserId(), now));
        device.updateHolder(borrower.getId(), DeviceStatus.HELD_BY_ME, now);
        notificationService.create(previousHolderId, NotificationType.BORROWED, "手机已被借走", device.getName() + " 已被 " + borrower.getName() + " 借走", device.getId(), next.getId());
        notificationService.create(device.getOwnerUserId(), NotificationType.BORROWED, "手机借出记录", device.getName() + " 已被 " + borrower.getName() + " 借走", device.getId(), next.getId());
        return toResponse(next);
    }

    public List<LoanResponse> active(User user) {
        return loanRecordRepository.findRelevantActiveLoans(user.getId(), LoanStatus.ACTIVE)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public LoanResponse returnLoan(UUID loanId, User user) {
        LoanRecord record = requireLoan(loanId);
        if (!record.getBorrowerUserId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "只有当前借入人可以归还");
        }
        if (record.getStatus() == LoanStatus.RETURNED) {
            return toResponse(record);
        }
        Instant now = Instant.now();
        record.markReturned(now);
        Device device = deviceService.requireDevice(record.getDeviceId());
        device.updateHolder(null, DeviceStatus.AVAILABLE, now);
        notificationService.create(record.getOwnerUserId(), NotificationType.RETURNED, "手机已归还", device.getName() + " 已由 " + user.getName() + " 归还", device.getId(), record.getId());
        return toResponse(record);
    }

    @Transactional
    public LoanResponse urgeReturn(UUID loanId, User user) {
        LoanRecord record = requireLoan(loanId);
        Device device = deviceService.requireDevice(record.getDeviceId());
        if (!device.getOwnerUserId().equals(user.getId()) && !user.getId().equals(record.getPreviousHolderUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "只有设备 owner 或上一位持有人可以催还");
        }
        notificationService.create(record.getBorrowerUserId(), NotificationType.URGE_RETURN, "请归还手机", device.getName() + " 被发起催还，请尽快处理", device.getId(), record.getId());
        return toResponse(record);
    }

    private LoanRecord requireLoan(UUID loanId) {
        return loanRecordRepository.findById(loanId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "LOAN_NOT_FOUND", "借用记录不存在"));
    }

    private LoanResponse toResponse(LoanRecord record) {
        return new LoanResponse(
            record.getId().toString(),
            deviceService.byId(record.getDeviceId()),
            record.getStatus().name(),
            record.getStartedAt().toString(),
            record.getEndedAt() == null ? null : record.getEndedAt().toString()
        );
    }
}
