package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 개발/테스트 전용 서비스
 * - 데이터 초기화 등 개발 편의 기능 제공
 * - TRUNCATE 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DevService {

    private final EntityManager entityManager;
    private final ExpenseShareRepository expenseShareRepository;
    private final ExpensePaymentRepository expensePaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 모든 데이터 초기화 (앱 최초 설치 상태로 복원)
     * TRUNCATE 사용
     */
    @Transactional
    public Map<String, Integer> resetAllData() {
        Map<String, Integer> counts = new LinkedHashMap<>();

        counts.put("expense_shares", (int) expenseShareRepository.count());
        counts.put("expense_payments", (int) expensePaymentRepository.count());
        counts.put("expenses", (int) expenseRepository.count());
        counts.put("participants", (int) participantRepository.count());
        counts.put("trips", (int) tripRepository.count());
        counts.put("friends", (int) friendRepository.count());
        counts.put("refresh_tokens", (int) refreshTokenRepository.count());
        counts.put("members", (int) memberRepository.count());

        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE expense_shares").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE expense_payments").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE expenses").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE participants").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE trips").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE friends").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE refresh_tokens").executeUpdate();
        entityManager.createNativeQuery("TRUNCATE TABLE members").executeUpdate();
        entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

        return counts;
    }

    /**
     * 현재 데이터 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDataStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("members", memberRepository.count());
        stats.put("refresh_tokens", refreshTokenRepository.count());
        stats.put("friends", friendRepository.count());
        stats.put("trips", tripRepository.count());
        stats.put("participants", participantRepository.count());
        stats.put("expenses", expenseRepository.count());
        stats.put("expense_payments", expensePaymentRepository.count());
        stats.put("expense_shares", expenseShareRepository.count());

        return stats;
    }
}
