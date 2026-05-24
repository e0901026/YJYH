package com.yjyh.phoneloan.backend.device;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    Optional<Device> findByImei1(String imei1);
    boolean existsByImei1(String imei1);
}
