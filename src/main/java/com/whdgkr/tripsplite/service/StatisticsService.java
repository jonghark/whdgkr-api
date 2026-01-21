package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.StatisticsResponse;
import com.whdgkr.tripsplite.entity.Expense;
import com.whdgkr.tripsplite.entity.ExpenseCategory;
import com.whdgkr.tripsplite.entity.Trip;
import com.whdgkr.tripsplite.repository.ExpenseRepository;
import com.whdgkr.tripsplite.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TripRepository tripRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public StatisticsResponse getTripStatistics(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));

        List<Expense> expenses = expenseRepository.findByTripIdAndDeleteYnOrderByOccurredAtDesc(tripId, "N");

        // 총 지출 계산
        int totalExpense = expenses.stream()
                .mapToInt(Expense::getTotalAmount)
                .sum();

        // 카테고리별 집계
        Map<ExpenseCategory, Integer> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingInt(Expense::getTotalAmount)
                ));

        // 카테고리별 통계 생성
        List<StatisticsResponse.CategoryStat> categoryStats = new ArrayList<>();
        for (ExpenseCategory category : ExpenseCategory.values()) {
            int amount = categoryMap.getOrDefault(category, 0);
            if (amount > 0) {
                double percentage = totalExpense > 0 ? (amount * 100.0 / totalExpense) : 0.0;
                categoryStats.add(StatisticsResponse.CategoryStat.builder()
                        .category(category.name())
                        .categoryName(category.getDisplayName())
                        .amount(amount)
                        .percentage(Math.round(percentage * 10) / 10.0) // 소수점 1자리
                        .build());
            }
        }

        // 금액 내림차순 정렬
        categoryStats.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));

        return StatisticsResponse.builder()
                .totalExpense(totalExpense)
                .categoryStats(categoryStats)
                .build();
    }
}
