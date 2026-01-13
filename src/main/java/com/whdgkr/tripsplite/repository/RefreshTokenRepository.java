package com.whdgkr.tripsplite.repository;

import com.whdgkr.tripsplite.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHashAndRevokedYn(String tokenHash, String revokedYn);
    void deleteByMemberId(Long memberId);
}
