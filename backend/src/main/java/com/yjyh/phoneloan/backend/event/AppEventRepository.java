package com.yjyh.phoneloan.backend.event;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppEventRepository extends JpaRepository<AppEvent, UUID> {
}
