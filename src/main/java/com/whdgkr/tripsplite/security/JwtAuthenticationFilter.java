package com.whdgkr.tripsplite.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> PERMIT_ALL_PATHS = List.of(
            "/api/auth/**",
            "/api/dev/**"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean skip = PERMIT_ALL_PATHS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
        if (skip) {
            log.debug("[JWT Filter] Skipping filter for path: {}", path);
        }
        return skip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Auth 엔드포인트는 절대 건드리지 않음 (이중 안전장치)
        if (path.startsWith("/api/auth/signup") || path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/refresh") || path.startsWith("/api/dev/")) {
            log.debug("[JWT Filter] Skipping auth check for: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        log.debug("[JWT Filter] Processing request: {} {}", request.getMethod(), path);

        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            if (jwtProvider.validateToken(token)) {
                String tokenType = jwtProvider.getTokenType(token);
                if ("access".equals(tokenType)) {
                    Long memberId = jwtProvider.getMemberIdFromToken(token);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(memberId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("[JWT Filter] Authentication set for memberId: {}", memberId);
                } else {
                    log.debug("[JWT Filter] Token type is not 'access': {}", tokenType);
                }
            } else {
                log.debug("[JWT Filter] Invalid token provided");
            }
        } else {
            log.debug("[JWT Filter] No token provided");
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
