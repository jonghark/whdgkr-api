package com.whdgkr.tripsplite.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(length = 3, nullable = false)
    @Builder.Default
    private String currency = "KRW";

    @Column(name = "delete_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Builder.Default
    private String deleteYn = "N";

    @Column(name = "settled_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Builder.Default
    private String settledYn = "N";

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpensePayment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseShare> shares = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isDeleted() {
        return "Y".equals(deleteYn);
    }

    public boolean isActive() {
        return "N".equals(deleteYn);
    }

    public boolean isSettled() {
        return "Y".equals(settledYn);
    }

    public boolean isUnsettled() {
        return "N".equals(settledYn);
    }
}
