package com.yjyh.phoneloan.backend.invite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteCodeRepository extends JpaRepository<InviteCode, UUID> {
    Optional<InviteCode> findByCode(String code);
    List<InviteCode> findByCreatedByUserId(UUID createdByUserId);
}
