package com.whdgkr.tripsplite.repository;

import com.whdgkr.tripsplite.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByTripIdOrderByOccurredAtDesc(Long tripId);
    List<Expense> findByTripIdAndDeleteYnOrderByOccurredAtDesc(Long tripId, String deleteYn);
}
