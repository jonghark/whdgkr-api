package com.whdgkr.tripsplite.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExpenseRequest {
    private String title;
    private String occurredAt; // ISO-8601: 2025-12-28T10:00:00
    private Integer totalAmount;
    private String currency = "KRW"; // 기본값: KRW
    private List<PaymentItem> payments;
    private List<ShareItem> shares;
    
    @Data
    public static class PaymentItem {
        private Long participantId;
        private Integer amount;
    }
    
    @Data
    public static class ShareItem {
        private Long participantId;
        private Integer amount;
    }
}
