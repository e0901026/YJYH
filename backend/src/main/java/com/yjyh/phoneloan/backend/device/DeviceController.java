package com.yjyh.phoneloan.backend.device;

import com.yjyh.phoneloan.backend.common.CurrentUser;
import com.yjyh.phoneloan.backend.device.DeviceDtos.CreateDeviceRequest;
import com.yjyh.phoneloan.backend.device.DeviceDtos.DeviceResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceService deviceService;
    private final CurrentUser currentUser;

    public DeviceController(DeviceService deviceService, CurrentUser currentUser) {
        this.deviceService = deviceService;
        this.currentUser = currentUser;
    }

    @GetMapping
    List<DeviceResponse> all() {
        return deviceService.all();
    }

    @GetMapping("/{id}")
    DeviceResponse byId(@PathVariable UUID id) {
        return deviceService.byId(id);
    }

    @GetMapping("/by-imei/{imei}")
    DeviceResponse byImei(@PathVariable String imei) {
        return deviceService.byImei(imei);
    }

    @PostMapping
    DeviceResponse create(@RequestHeader("Authorization") String authorization, @Valid @RequestBody CreateDeviceRequest request) {
        return deviceService.create(request, currentUser.fromAuthorization(authorization));
    }
}
