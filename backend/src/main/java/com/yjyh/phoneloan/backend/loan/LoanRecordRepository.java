package com.yjyh.phoneloan.backend.loan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanRecordRepository extends JpaRepository<LoanRecord, UUID> {
    Optional<LoanRecord> findByDeviceIdAndStatus(UUID deviceId, LoanStatus status);
    List<LoanRecord> findByBorrowerUserIdAndStatus(UUID borrowerUserId, LoanStatus status);

    @Query("""
        select record from LoanRecord record
        where record.status = :status
          and (
            record.borrowerUserId = :userId
            or record.ownerUserId = :userId
            or record.previousHolderUserId = :userId
          )
        order by record.startedAt desc
        """)
    List<LoanRecord> findRelevantActiveLoans(UUID userId, LoanStatus status);
}
