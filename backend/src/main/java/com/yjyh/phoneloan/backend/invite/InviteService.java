package com.yjyh.phoneloan.backend.invite;

import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.invite.InviteDtos.InviteCodeResponse;
import com.yjyh.phoneloan.backend.user.User;
import com.yjyh.phoneloan.backend.user.UserRole;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InviteService {
    private final InviteCodeRepository inviteCodeRepository;

    public InviteService(InviteCodeRepository inviteCodeRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
    }

    public List<InviteCodeResponse> mine(User user) {
        return inviteCodeRepository.findByCreatedByUserId(user.getId()).stream().map(this::toResponse).toList();
    }

    @Transactional
    public InviteCodeResponse apply(User user) {
        if (user.getInviteQuotaUsed() >= 10) {
            throw new ApiException(HttpStatus.CONFLICT, "INVITE_QUOTA_FULL", "邀请码配额已满");
        }
        Instant now = Instant.now();
        user.increaseInviteQuota(now);
        return toResponse(inviteCodeRepository.save(new InviteCode(UUID.randomUUID(), nextCode("USER"), user.getId(), now.plus(30, ChronoUnit.DAYS), now)));
    }

    public List<InviteCodeResponse> ownerAll(User user) {
        requireOwner(user);
        return inviteCodeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public InviteCodeResponse ownerCreate(User user) {
        requireOwner(user);
        Instant now = Instant.now();
        return toResponse(inviteCodeRepository.save(new InviteCode(UUID.randomUUID(), nextCode("OWNER"), user.getId(), now.plus(90, ChronoUnit.DAYS), now)));
    }

    private void requireOwner(User user) {
        if (user.getRole() != UserRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "OWNER_REQUIRED", "需要 Owner 权限");
        }
    }

    private String nextCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private InviteCodeResponse toResponse(InviteCode code) {
        return new InviteCodeResponse(
            code.getId().toString(),
            code.getCode(),
            code.getStatus().name(),
            code.getExpiresAt().toString(),
            code.getUsedByUserId() == null ? null : code.getUsedByUserId().toString()
        );
    }
}
