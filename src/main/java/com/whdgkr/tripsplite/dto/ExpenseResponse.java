package com.whdgkr.tripsplite.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExpenseResponse {
    private Long id;
    private String title;
    private String occurredAt;
    private Integer totalAmount;
    private String currency;
    private String createdAt;
    private List<PaymentDetail> payments;
    private List<ShareDetail> shares;
    
    @Data
    @Builder
    public static class PaymentDetail {
        private Long participantId;
        private String participantName;
        private Integer amount;
    }
    
    @Data
    @Builder
    public static class ShareDetail {
        private Long participantId;
        private String participantName;
        private Integer amount;
    }
}
