package com.yjyh.phoneloan.backend.user;

public final class UserDtos {
    private UserDtos() {
    }

    public record OwnerUserResponse(String id, String employeeNo, String name, String role, String invitedByUserId) {}
}
