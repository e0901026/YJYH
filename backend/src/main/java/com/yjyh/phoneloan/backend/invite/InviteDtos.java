package com.yjyh.phoneloan.backend.invite;

public final class InviteDtos {
    private InviteDtos() {
    }

    public record InviteCodeResponse(String id, String code, String status, String expiresAt, String usedByUserId) {}
}
