package com.whdgkr.tripsplite.controller;

import com.whdgkr.tripsplite.dto.MemberResponse;
import com.whdgkr.tripsplite.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe(@AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(authService.getMe(memberId));
    }
}
