package com.yjyh.phoneloan.backend.loan;

import com.yjyh.phoneloan.backend.common.CurrentUser;
import com.yjyh.phoneloan.backend.loan.LoanDtos.BorrowByImeiRequest;
import com.yjyh.phoneloan.backend.loan.LoanDtos.LoanResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;
    private final CurrentUser currentUser;

    public LoanController(LoanService loanService, CurrentUser currentUser) {
        this.loanService = loanService;
        this.currentUser = currentUser;
    }

    @PostMapping("/borrow-by-imei")
    LoanResponse borrowByImei(@RequestHeader("Authorization") String authorization, @Valid @RequestBody BorrowByImeiRequest request) {
        return loanService.borrowByImei(request.imei(), currentUser.fromAuthorization(authorization));
    }

    @GetMapping("/active")
    List<LoanResponse> active(@RequestHeader("Authorization") String authorization) {
        return loanService.active(currentUser.fromAuthorization(authorization));
    }

    @PostMapping("/{id}/return")
    LoanResponse returnLoan(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        return loanService.returnLoan(id, currentUser.fromAuthorization(authorization));
    }

    @PostMapping("/{id}/urge-return")
    LoanResponse urgeReturn(@RequestHeader("Authorization") String authorization, @PathVariable UUID id) {
        return loanService.urgeReturn(id, currentUser.fromAuthorization(authorization));
    }
}
