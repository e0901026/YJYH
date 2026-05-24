package com.yjyh.phoneloan.backend.common;

import com.yjyh.phoneloan.backend.device.Device;
import com.yjyh.phoneloan.backend.device.DeviceRepository;
import com.yjyh.phoneloan.backend.device.DeviceStatus;
import com.yjyh.phoneloan.backend.invite.InviteCode;
import com.yjyh.phoneloan.backend.invite.InviteCodeRepository;
import com.yjyh.phoneloan.backend.loan.LoanRecord;
import com.yjyh.phoneloan.backend.loan.LoanRecordRepository;
import com.yjyh.phoneloan.backend.user.User;
import com.yjyh.phoneloan.backend.user.UserRepository;
import com.yjyh.phoneloan.backend.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final InviteCodeRepository inviteCodeRepository;
    private final DeviceRepository deviceRepository;
    private final LoanRecordRepository loanRecordRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(
        UserRepository userRepository,
        InviteCodeRepository inviteCodeRepository,
        DeviceRepository deviceRepository,
        LoanRecordRepository loanRecordRepository
    ) {
        this.userRepository = userRepository;
        this.inviteCodeRepository = inviteCodeRepository;
        this.deviceRepository = deviceRepository;
        this.loanRecordRepository = loanRecordRepository;
    }

    @Override
    public void run(String... args) {
        Instant now = Instant.now();
        User owner = userRepository.findByEmployeeNo("10086")
            .orElseGet(() -> userRepository.save(new User(
                UUID.randomUUID(),
                "10086",
                "王晓明",
                passwordEncoder.encode("password123"),
                UserRole.OWNER,
                null,
                now
            )));
        User li = userRepository.findByEmployeeNo("10248")
            .orElseGet(() -> userRepository.save(new User(
                UUID.randomUUID(),
                "10248",
                "李雷",
                passwordEncoder.encode("password123"),
                UserRole.USER,
                owner.getId(),
                now.minus(15, ChronoUnit.DAYS)
            )));
        User han = userRepository.findByEmployeeNo("10881")
            .orElseGet(() -> userRepository.save(new User(
                UUID.randomUUID(),
                "10881",
                "韩梅梅",
                passwordEncoder.encode("password123"),
                UserRole.USER,
                owner.getId(),
                now.minus(12, ChronoUnit.DAYS)
            )));
        if (inviteCodeRepository.findAll().isEmpty()) {
            inviteCodeRepository.save(new InviteCode(
                UUID.randomUUID(),
                "OWNER-SEED-0001",
                owner.getId(),
                now.plus(30, ChronoUnit.DAYS),
                now
            ));
        }
        if (deviceRepository.findAll().isEmpty()) {
            seedDemoDevice("小米14 白", "869301065812347", null, owner, li, owner, now.minus(6, ChronoUnit.DAYS));
            seedHeldDevice("OPPO Find X7", "866001123456789", "866001123456797", owner, now.minus(2, ChronoUnit.DAYS));
            seedDemoDevice("iPhone 15 Pro", "867450991234568", null, han, owner, han, now.minus(1, ChronoUnit.DAYS));
        }
    }

    private void seedHeldDevice(String name, String imei1, String imei2, User owner, Instant createdAt) {
        deviceRepository.save(new Device(UUID.randomUUID(), name, imei1, imei2, owner.getId(), owner.getId(), createdAt));
    }

    private void seedDemoDevice(String name, String imei1, String imei2, User owner, User holder, User previousHolder, Instant startedAt) {
        Device device = new Device(UUID.randomUUID(), name, imei1, imei2, owner.getId(), owner.getId(), startedAt);
        device.updateHolder(holder.getId(), DeviceStatus.HELD_BY_ME, startedAt);
        deviceRepository.save(device);
        loanRecordRepository.save(new LoanRecord(
            UUID.randomUUID(),
            device.getId(),
            holder.getId(),
            previousHolder.getId(),
            owner.getId(),
            startedAt
        ));
    }
}
