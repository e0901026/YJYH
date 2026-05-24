package com.yjyh.phoneloan.backend.user;

import com.yjyh.phoneloan.backend.common.ApiException;
import com.yjyh.phoneloan.backend.user.UserDtos.OwnerUserResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<OwnerUserResponse> ownerUsers(User user) {
        if (user.getRole() != UserRole.OWNER) {
            throw new ApiException(HttpStatus.FORBIDDEN, "OWNER_REQUIRED", "需要 Owner 权限");
        }
        return userRepository.findAll().stream()
            .map(row -> new OwnerUserResponse(
                row.getId().toString(),
                row.getEmployeeNo(),
                row.getName(),
                row.getRole().name(),
                row.getInvitedByUserId() == null ? null : row.getInvitedByUserId().toString()
            ))
            .toList();
    }
}
