package com.whdgkr.tripsplite.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SettlementResponse {
    private Integer totalExpense;
    private List<ParticipantBalance> balances;
    private List<Transaction> transfers;
    
    @Data
    @Builder
    public static class ParticipantBalance {
        private Long participantId;
        private String participantName;
        private Integer paidTotal;
        private Integer shareTotal;
        private Integer netBalance;
    }
    
    @Data
    @Builder
    public static class Transaction {
        private Long fromParticipantId;
        private String fromParticipantName;
        private Long toParticipantId;
        private String toParticipantName;
        private Integer amount;
    }
}
