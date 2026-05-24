package com.yjyh.phoneloan.backend.common;

import com.yjyh.phoneloan.backend.invite.InviteCode;
import com.yjyh.phoneloan.backend.invite.InviteCodeRepository;
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
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public DataInitializer(UserRepository userRepository, InviteCodeRepository inviteCodeRepository) {
        this.userRepository = userRepository;
        this.inviteCodeRepository = inviteCodeRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmployeeNo("10086")) {
            return;
        }
        Instant now = Instant.now();
        User owner = userRepository.save(new User(
            UUID.randomUUID(),
            "10086",
            "王晓明",
            passwordEncoder.encode("password123"),
            UserRole.OWNER,
            null,
            now
        ));
        inviteCodeRepository.save(new InviteCode(
            UUID.randomUUID(),
            "OWNER-SEED-0001",
            owner.getId(),
            now.plus(30, ChronoUnit.DAYS),
            now
        ));
    }
}
