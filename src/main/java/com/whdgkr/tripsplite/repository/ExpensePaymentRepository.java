package com.whdgkr.tripsplite.repository;

import com.whdgkr.tripsplite.entity.ExpensePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpensePaymentRepository extends JpaRepository<ExpensePayment, Long> {
}
