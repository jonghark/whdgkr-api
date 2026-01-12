package com.whdgkr.tripsplite.service;

import com.whdgkr.tripsplite.dto.SettlementResponse;
import com.whdgkr.tripsplite.entity.*;
import com.whdgkr.tripsplite.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final TripRepository tripRepository;

    @Transactional(readOnly = true)
    public SettlementResponse calculateSettlement(Long tripId) {
        return calculateSettlement(tripId, "UNSETTLED");
    }

    @Transactional(readOnly = true)
    public SettlementResponse calculateSettlement(Long tripId, String scope) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found: " + tripId));

        // 활성 동행자 목록 (UI 표시용)
        List<Participant> activeParticipants = trip.getParticipants().stream()
                .filter(p -> "N".equals(p.getDeleteYn()))
                .toList();

        // 활성 지출만 계산 (scope에 따라 필터링)
        List<Expense> activeExpenses = trip.getExpenses().stream()
                .filter(e -> "N".equals(e.getDeleteYn()))
                .filter(e -> "ALL".equalsIgnoreCase(scope) || "N".equals(e.getSettledYn()))
                .toList();

        // 활성 동행자의 잔액 계산
        Map<Long, Integer> paidTotals = new HashMap<>();
        Map<Long, Integer> shareTotals = new HashMap<>();

        for (Participant participant : activeParticipants) {
            paidTotals.put(participant.getId(), 0);
            shareTotals.put(participant.getId(), 0);
        }

        for (Expense expense : activeExpenses) {
            // 활성 결제 기록 처리
            for (ExpensePayment payment : expense.getPayments()) {
                if ("N".equals(payment.getDeleteYn())) {
                    Long participantId = payment.getParticipant().getId();
                    // 활성 동행자의 결제만 계산 (삭제된 동행자 제외)
                    if (paidTotals.containsKey(participantId)) {
                        paidTotals.put(participantId, paidTotals.get(participantId) + payment.getAmount());
                    }
                }
            }

            // 활성 분배 기록 처리
            for (ExpenseShare share : expense.getShares()) {
                if ("N".equals(share.getDeleteYn())) {
                    Long participantId = share.getParticipant().getId();
                    // 활성 동행자의 분배만 계산 (삭제된 동행자 제외)
                    if (shareTotals.containsKey(participantId)) {
                        shareTotals.put(participantId, shareTotals.get(participantId) + share.getAmount());
                    }
                }
            }
        }

        // 잔액 목록 생성
        List<SettlementResponse.ParticipantBalance> balances = new ArrayList<>();
        Map<Long, String> participantNames = activeParticipants.stream()
                .collect(Collectors.toMap(Participant::getId, Participant::getName));

        int totalPaid = 0;
        int totalShare = 0;
        int totalNetBalance = 0;

        for (Participant participant : activeParticipants) {
            Long participantId = participant.getId();
            int paid = paidTotals.get(participantId);
            int share = shareTotals.get(participantId);
            int netBalance = paid - share;

            totalPaid += paid;
            totalShare += share;
            totalNetBalance += netBalance;

            balances.add(SettlementResponse.ParticipantBalance.builder()
                    .participantId(participantId)
                    .participantName(participant.getName())
                    .paidTotal(paid)
                    .shareTotal(share)
                    .netBalance(netBalance)
                    .build());
        }

        // 정산 금액 합계 검증 로그
        log.info("Settlement calculation for tripId={}: totalPaid={}, totalShare={}, totalNetBalance={}",
                tripId, totalPaid, totalShare, totalNetBalance);

        if (totalNetBalance != 0) {
            log.warn("Settlement balance mismatch! tripId={}, totalNetBalance={} (expected 0)", tripId, totalNetBalance);
        }

        // Calculate total expense
        int totalExpense = activeExpenses.stream()
                .mapToInt(Expense::getTotalAmount)
                .sum();

        // Calculate recommended transfers (minimize transfers)
        List<SettlementResponse.Transaction> transfers = calculateMinimalTransactions(balances, participantNames);

        return SettlementResponse.builder()
                .totalExpense(totalExpense)
                .balances(balances)
                .transfers(transfers)
                .build();
    }

    private List<SettlementResponse.Transaction> calculateMinimalTransactions(
            List<SettlementResponse.ParticipantBalance> balances,
            Map<Long, String> participantNames) {

        List<SettlementResponse.Transaction> transactions = new ArrayList<>();

        // Separate payers (positive balance) and receivers (negative balance)
        PriorityQueue<BalanceNode> payers = new PriorityQueue<>((a, b) -> b.amount - a.amount);
        PriorityQueue<BalanceNode> receivers = new PriorityQueue<>((a, b) -> a.amount - b.amount);

        for (SettlementResponse.ParticipantBalance balance : balances) {
            if (balance.getNetBalance() > 0) {
                payers.add(new BalanceNode(balance.getParticipantId(), balance.getNetBalance()));
            } else if (balance.getNetBalance() < 0) {
                receivers.add(new BalanceNode(balance.getParticipantId(), balance.getNetBalance()));
            }
        }

        // Greedy matching
        while (!payers.isEmpty() && !receivers.isEmpty()) {
            BalanceNode payer = payers.poll();
            BalanceNode receiver = receivers.poll();

            int transferAmount = Math.min(payer.amount, -receiver.amount);

            transactions.add(SettlementResponse.Transaction.builder()
                    .fromParticipantId(receiver.participantId)
                    .fromParticipantName(participantNames.get(receiver.participantId))
                    .toParticipantId(payer.participantId)
                    .toParticipantName(participantNames.get(payer.participantId))
                    .amount(transferAmount)
                    .build());

            payer.amount -= transferAmount;
            receiver.amount += transferAmount;

            if (payer.amount > 0) {
                payers.add(payer);
            }
            if (receiver.amount < 0) {
                receivers.add(receiver);
            }
        }

        return transactions;
    }

    private static class BalanceNode {
        Long participantId;
        int amount;

        BalanceNode(Long participantId, int amount) {
            this.participantId = participantId;
            this.amount = amount;
        }
    }
}
