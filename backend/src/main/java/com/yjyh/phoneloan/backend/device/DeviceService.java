package com.yjyh.phoneloan.backend.device;

import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.common.Imei;
import com.yjyh.phoneloan.backend.device.DeviceDtos.CreateDeviceRequest;
import com.yjyh.phoneloan.backend.device.DeviceDtos.DeviceResponse;
import com.yjyh.phoneloan.backend.device.DeviceDtos.UserSummary;
import com.yjyh.phoneloan.backend.user.User;
import com.yjyh.phoneloan.backend.user.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public DeviceService(DeviceRepository deviceRepository, UserRepository userRepository) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
    }

    public List<DeviceResponse> all() {
        return deviceRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DeviceResponse byId(UUID id) {
        return toResponse(requireDevice(id));
    }

    public DeviceResponse byImei(String imei) {
        if (!Imei.isValid(imei)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMEI_INVALID", "IMEI 必须为 15 位数字");
        }
        return deviceRepository.findByImei1(imei)
            .map(this::toResponse)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", "设备未建档"));
    }

    @Transactional
    public DeviceResponse create(CreateDeviceRequest request, User actor) {
        if (!Imei.isValid(request.imei1())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "IMEI_INVALID", "IMEI 必须为 15 位数字");
        }
        if (deviceRepository.existsByImei1(request.imei1())) {
            throw new ApiException(HttpStatus.CONFLICT, "DEVICE_EXISTS", "设备已建档");
        }
        Device device = new Device(
            UUID.randomUUID(),
            request.name(),
            request.imei1(),
            request.imei2(),
            actor.getId(),
            actor.getId(),
            Instant.now()
        );
        return toResponse(deviceRepository.save(device));
    }

    public Device requireDevice(UUID id) {
        return deviceRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DEVICE_NOT_FOUND", "设备不存在"));
    }

    public DeviceResponse toResponse(Device device) {
        User owner = requireUser(device.getOwnerUserId());
        User holder = device.getCurrentHolderUserId() == null ? null : requireUser(device.getCurrentHolderUserId());
        return new DeviceResponse(
            device.getId().toString(),
            device.getName(),
            device.getImei1(),
            device.getImei2(),
            toSummary(owner),
            holder == null ? null : toSummary(holder),
            device.getStatus().name()
        );
    }

    public UserSummary toSummary(User user) {
        return new UserSummary(user.getId().toString(), user.getEmployeeNo(), user.getName());
    }

    private User requireUser(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
    }
}
