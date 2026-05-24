package com.yjyh.phoneloan.backend.invite;

import com.yjyh.phoneloan.backend.common.CurrentUser;
import com.yjyh.phoneloan.backend.invite.InviteDtos.InviteCodeResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class InviteController {
    private final InviteService inviteService;
    private final CurrentUser currentUser;

    public InviteController(InviteService inviteService, CurrentUser currentUser) {
        this.inviteService = inviteService;
        this.currentUser = currentUser;
    }

    @GetMapping("/invite-codes/my")
    List<InviteCodeResponse> mine(@RequestHeader("Authorization") String authorization) {
        return inviteService.mine(currentUser.fromAuthorization(authorization));
    }

    @PostMapping("/invite-codes/apply")
    InviteCodeResponse apply(@RequestHeader("Authorization") String authorization) {
        return inviteService.apply(currentUser.fromAuthorization(authorization));
    }

    @GetMapping("/owner/invite-codes")
    List<InviteCodeResponse> ownerAll(@RequestHeader("Authorization") String authorization) {
        return inviteService.ownerAll(currentUser.fromAuthorization(authorization));
    }

    @PostMapping("/owner/invite-codes")
    InviteCodeResponse ownerCreate(@RequestHeader("Authorization") String authorization) {
        return inviteService.ownerCreate(currentUser.fromAuthorization(authorization));
    }
}
