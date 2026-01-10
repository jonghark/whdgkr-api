package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 개발/테스트 전용 서비스
 * - 데이터 초기화 등 개발 편의 기능 제공
 * - DELETE 방식만 사용 (TRUNCATE/DROP 절대 금지)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DevService {

    private final ExpenseShareRepository expenseShareRepository;
    private final ExpensePaymentRepository expensePaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final ParticipantRepository participantRepository;
    private final TripRepository tripRepository;
    private final FriendRepository friendRepository;

    /**
     * 모든 데이터 초기화 (앱 최초 설치 상태로 복원)
     *
     * 삭제 순서 (FK 제약 준수):
     * 1. expense_shares
     * 2. expense_payments
     * 3. expenses
     * 4. participants
     * 5. trips
     * 6. friends
     *
     * @return 테이블별 삭제된 레코드 수
     */
    @Transactional
    public Map<String, Integer> resetAllData() {
        Map<String, Integer> deletedCounts = new LinkedHashMap<>();

        // 1. expense_shares 삭제
        long shareCount = expenseShareRepository.count();
        expenseShareRepository.deleteAll();
        deletedCounts.put("expense_shares", (int) shareCount);
        log.info("Deleted {} expense_shares", shareCount);

        // 2. expense_payments 삭제
        long paymentCount = expensePaymentRepository.count();
        expensePaymentRepository.deleteAll();
        deletedCounts.put("expense_payments", (int) paymentCount);
        log.info("Deleted {} expense_payments", paymentCount);

        // 3. expenses 삭제
        long expenseCount = expenseRepository.count();
        expenseRepository.deleteAll();
        deletedCounts.put("expenses", (int) expenseCount);
        log.info("Deleted {} expenses", expenseCount);

        // 4. participants 삭제
        long participantCount = participantRepository.count();
        participantRepository.deleteAll();
        deletedCounts.put("participants", (int) participantCount);
        log.info("Deleted {} participants", participantCount);

        // 5. trips 삭제
        long tripCount = tripRepository.count();
        tripRepository.deleteAll();
        deletedCounts.put("trips", (int) tripCount);
        log.info("Deleted {} trips", tripCount);

        // 6. friends 삭제
        long friendCount = friendRepository.count();
        friendRepository.deleteAll();
        deletedCounts.put("friends", (int) friendCount);
        log.info("Deleted {} friends", friendCount);

        return deletedCounts;
    }

    /**
     * 현재 데이터 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDataStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        stats.put("trips", tripRepository.count());
        stats.put("participants", participantRepository.count());
        stats.put("expenses", expenseRepository.count());
        stats.put("expense_payments", expensePaymentRepository.count());
        stats.put("expense_shares", expenseShareRepository.count());
        stats.put("friends", friendRepository.count());

        return stats;
    }
}
