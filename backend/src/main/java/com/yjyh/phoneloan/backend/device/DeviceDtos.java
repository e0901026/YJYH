package com.yjyh.phoneloan.backend.device;

import jakarta.validation.constraints.NotBlank;

public final class DeviceDtos {
    private DeviceDtos() {
    }

    public record CreateDeviceRequest(@NotBlank String name, @NotBlank String imei1, String imei2) {}
    public record UserSummary(String id, String employeeNo, String name) {}
    public record DeviceResponse(
        String id,
        String name,
        String imei1,
        String imei2,
        UserSummary owner,
        UserSummary currentHolder,
        String status
    ) {}
}
