package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.ExpenseRequest;
import com.whdgkr.tripsplite.dto.ExpenseResponse;
import com.whdgkr.tripsplite.entity.*;
import com.whdgkr.tripsplite.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final TripRepository tripRepository;
    private final ParticipantRepository participantRepository;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByTripId(Long tripId) {
        return expenseRepository.findByTripIdAndDeleteYnOrderByOccurredAtDesc(tripId, "N").stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponse createExpense(Long tripId, ExpenseRequest request) {
        validateExpenseRequest(request);

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));

        Expense expense = Expense.builder()
                .trip(trip)
                .title(request.getTitle())
                .occurredAt(LocalDateTime.parse(request.getOccurredAt(), dateTimeFormatter))
                .totalAmount(request.getTotalAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "KRW")
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // Save payments
        for (ExpenseRequest.PaymentItem payment : request.getPayments()) {
            Participant participant = participantRepository.findById(payment.getParticipantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found: " + payment.getParticipantId()));

            ExpensePayment expensePayment = ExpensePayment.builder()
                    .expense(savedExpense)
                    .participant(participant)
                    .amount(payment.getAmount())
                    .build();
            savedExpense.getPayments().add(expensePayment);
        }

        // Save shares
        for (ExpenseRequest.ShareItem share : request.getShares()) {
            Participant participant = participantRepository.findById(share.getParticipantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found: " + share.getParticipantId()));

            ExpenseShare expenseShare = ExpenseShare.builder()
                    .expense(savedExpense)
                    .participant(participant)
                    .amount(share.getAmount())
                    .build();
            savedExpense.getShares().add(expenseShare);
        }

        Expense result = expenseRepository.save(savedExpense);
        return toResponse(result);
    }

    @Transactional
    public ExpenseResponse updateExpense(Long expenseId, ExpenseRequest request) {
        validateExpenseRequest(request);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + expenseId));

        expense.setTitle(request.getTitle());
        expense.setOccurredAt(LocalDateTime.parse(request.getOccurredAt(), dateTimeFormatter));
        expense.setTotalAmount(request.getTotalAmount());
        expense.setCurrency(request.getCurrency() != null ? request.getCurrency() : "KRW");

        // Soft delete existing payments and shares
        expense.getPayments().forEach(p -> p.setDeleteYn("Y"));
        expense.getShares().forEach(s -> s.setDeleteYn("Y"));

        // Add new payments
        for (ExpenseRequest.PaymentItem payment : request.getPayments()) {
            Participant participant = participantRepository.findById(payment.getParticipantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found: " + payment.getParticipantId()));

            ExpensePayment expensePayment = ExpensePayment.builder()
                    .expense(expense)
                    .participant(participant)
                    .amount(payment.getAmount())
                    .build();
            expense.getPayments().add(expensePayment);
        }

        // Add new shares
        for (ExpenseRequest.ShareItem share : request.getShares()) {
            Participant participant = participantRepository.findById(share.getParticipantId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found: " + share.getParticipantId()));

            ExpenseShare expenseShare = ExpenseShare.builder()
                    .expense(expense)
                    .participant(participant)
                    .amount(share.getAmount())
                    .build();
            expense.getShares().add(expenseShare);
        }

        Expense result = expenseRepository.save(expense);
        return toResponse(result);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + expenseId));

        expense.setDeleteYn("Y");
        expense.getPayments().forEach(p -> p.setDeleteYn("Y"));
        expense.getShares().forEach(s -> s.setDeleteYn("Y"));
        expenseRepository.save(expense);
    }

    private void validateExpenseRequest(ExpenseRequest request) {
        log.info("Validating expense request: title={}, totalAmount={}", request.getTitle(), request.getTotalAmount());

        if (request.getPayments() == null || request.getPayments().isEmpty()) {
            log.warn("Validation failed: no payments provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "결제자 정보가 필요합니다");
        }

        if (request.getShares() == null || request.getShares().isEmpty()) {
            log.warn("Validation failed: no shares provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "분배 대상이 필요합니다");
        }

        int paymentSum = request.getPayments().stream()
                .mapToInt(ExpenseRequest.PaymentItem::getAmount)
                .sum();

        int shareSum = request.getShares().stream()
                .mapToInt(ExpenseRequest.ShareItem::getAmount)
                .sum();

        log.info("Validation sums: paymentSum={}, shareSum={}, totalAmount={}", paymentSum, shareSum, request.getTotalAmount());

        if (paymentSum != request.getTotalAmount()) {
            log.warn("Validation failed: payment sum ({}) != total amount ({})", paymentSum, request.getTotalAmount());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "결제 금액 합계(" + paymentSum + ")가 총액(" + request.getTotalAmount() + ")과 일치하지 않습니다");
        }

        if (shareSum != request.getTotalAmount()) {
            log.warn("Validation failed: share sum ({}) != total amount ({})", shareSum, request.getTotalAmount());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "분배 금액 합계(" + shareSum + ")가 총액(" + request.getTotalAmount() + ")과 일치하지 않습니다");
        }

        log.info("Expense request validation passed");
    }

    private ExpenseResponse toResponse(Expense expense) {
        List<ExpenseResponse.PaymentDetail> payments = expense.getPayments().stream()
                .filter(p -> "N".equals(p.getDeleteYn()))
                .map(p -> ExpenseResponse.PaymentDetail.builder()
                        .participantId(p.getParticipant().getId())
                        .participantName(p.getParticipant().getName())
                        .amount(p.getAmount())
                        .build())
                .collect(Collectors.toList());

        List<ExpenseResponse.ShareDetail> shares = expense.getShares().stream()
                .filter(s -> "N".equals(s.getDeleteYn()))
                .map(s -> ExpenseResponse.ShareDetail.builder()
                        .participantId(s.getParticipant().getId())
                        .participantName(s.getParticipant().getName())
                        .amount(s.getAmount())
                        .build())
                .collect(Collectors.toList());

        return ExpenseResponse.builder()
                .id(expense.getId())
                .title(expense.getTitle())
                .occurredAt(expense.getOccurredAt().format(dateTimeFormatter))
                .totalAmount(expense.getTotalAmount())
                .currency(expense.getCurrency())
                .createdAt(expense.getCreatedAt().format(dateTimeFormatter))
                .settledYn(expense.getSettledYn())
                .settledAt(expense.getSettledAt() != null ? expense.getSettledAt().format(dateTimeFormatter) : null)
                .payments(payments)
                .shares(shares)
                .build();
    }

    @Transactional
    public ExpenseResponse updateSettledStatus(Long expenseId, boolean settled) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + expenseId));

        if (expense.isDeleted()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found: " + expenseId);
        }

        expense.setSettledYn(settled ? "Y" : "N");
        expense.setSettledAt(settled ? LocalDateTime.now() : null);

        Expense result = expenseRepository.save(expense);
        return toResponse(result);
    }
}
