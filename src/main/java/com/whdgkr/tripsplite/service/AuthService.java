package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.*;
import com.whdgkr.tripsplite.entity.Member;
import com.whdgkr.tripsplite.entity.RefreshToken;
import com.whdgkr.tripsplite.repository.MemberRepository;
import com.whdgkr.tripsplite.repository.RefreshTokenRepository;
import com.whdgkr.tripsplite.security.JwtProvider;
import com.whdgkr.tripsplite.security.TokenHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(SignupRequest request) {
        log.info("[AUTH] register called: loginId={}, email={}", request.getLoginId(), request.getEmail());

        if (memberRepository.existsByLoginId(request.getLoginId())) {
            log.warn("[AUTH] register failed: loginId already exists: {}", request.getLoginId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Login ID already exists");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            log.warn("[AUTH] register failed: email already exists: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        log.info("[AUTH] Creating new member: loginId={}", request.getLoginId());
        Member member = Member.builder()
                .loginId(request.getLoginId())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .build();

        log.info("[AUTH] Saving member to database...");
        Member saved = memberRepository.save(member);

        log.info("[AUTH] saved: userId={}, loginId={}, name={}, email={}",
                saved.getId(), saved.getLoginId(), saved.getName(), saved.getEmail());

        return MemberResponse.builder()
                .memberId(saved.getId())
                .loginId(saved.getLoginId())
                .name(saved.getName())
                .email(saved.getEmail())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request, String userAgent, String ipAddress) {
        Member member = memberRepository.findByLoginIdAndDeleteYn(request.getLoginId(), "N")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String accessToken = jwtProvider.createAccessToken(member.getId(), member.getLoginId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        saveRefreshToken(member, refreshToken, userAgent, ipAddress);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .member(MemberResponse.builder()
                        .memberId(member.getId())
                        .loginId(member.getLoginId())
                        .name(member.getName())
                        .email(member.getEmail())
                        .build())
                .build();
    }

    @Transactional
    public TokenResponse refresh(String refreshTokenValue, String userAgent, String ipAddress) {
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String tokenType = jwtProvider.getTokenType(refreshTokenValue);
        if (!"refresh".equals(tokenType)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token type");
        }

        String tokenHash = TokenHashUtil.hash(refreshTokenValue);
        RefreshToken storedToken = refreshTokenRepository.findByTokenHashAndRevokedYn(tokenHash, "N")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found or revoked"));

        if (!storedToken.isValid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        Member member = storedToken.getMember();
        String newAccessToken = jwtProvider.createAccessToken(member.getId(), member.getLoginId());
        String newRefreshToken = jwtProvider.createRefreshToken(member.getId());

        saveRefreshToken(member, newRefreshToken, userAgent, ipAddress);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isEmpty()) {
            return;
        }

        String tokenHash = TokenHashUtil.hash(refreshTokenValue);
        refreshTokenRepository.findByTokenHashAndRevokedYn(tokenHash, "N")
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }

    private void saveRefreshToken(Member member, String refreshToken, String userAgent, String ipAddress) {
        long expiryMs = jwtProvider.getRefreshTokenExpiry();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiryMs / 1000);

        RefreshToken token = RefreshToken.builder()
                .member(member)
                .tokenHash(TokenHashUtil.hash(refreshToken))
                .expiresAt(expiresAt)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(token);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));

        if (!member.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
        }

        return MemberResponse.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}
