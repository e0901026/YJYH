package com.yjyh.phoneloan.backend.user;

public final class UserDtos {
    private UserDtos() {
    }

    public record OwnerCreateUserRequest(String employeeNo, String name, String password, String role) {}
    public record OwnerUpdateUserRequest(String name, String password, String role) {}
    public record OwnerUserResponse(
        String id,
        String employeeNo,
        String name,
        String role,
        String invitedByUserId,
        boolean enabled,
        String createdAt
    ) {}
}
