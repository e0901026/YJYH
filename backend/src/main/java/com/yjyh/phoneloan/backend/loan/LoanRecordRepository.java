package com.yjyh.phoneloan.backend.loan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRecordRepository extends JpaRepository<LoanRecord, UUID> {
    Optional<LoanRecord> findByDeviceIdAndStatus(UUID deviceId, LoanStatus status);
    List<LoanRecord> findByBorrowerUserIdAndStatus(UUID borrowerUserId, LoanStatus status);
}
