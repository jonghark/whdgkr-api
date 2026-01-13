package com.whdgkr.tripsplite.repository;

import com.whdgkr.tripsplite.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginIdAndDeleteYn(String loginId, String deleteYn);
    Optional<Member> findByEmailAndDeleteYn(String email, String deleteYn);
    boolean existsByLoginId(String loginId);
    boolean existsByEmail(String email);
}
