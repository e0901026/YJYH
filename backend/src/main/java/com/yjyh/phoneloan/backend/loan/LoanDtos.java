package com.yjyh.phoneloan.backend.loan;

import com.yjyh.phoneloan.backend.device.DeviceDtos.DeviceResponse;
import jakarta.validation.constraints.NotBlank;

public final class LoanDtos {
    private LoanDtos() {
    }

    public record BorrowByImeiRequest(@NotBlank String imei) {}
    public record LoanResponse(String id, DeviceResponse device, String status, String startedAt, String endedAt) {}
}
