package com.whdgkr.tripsplite.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_shares")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_id", nullable = false)
    private Participant participant;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "delete_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Builder.Default
    private String deleteYn = "N";

    public boolean isDeleted() {
        return "Y".equals(deleteYn);
    }

    public boolean isActive() {
        return "N".equals(deleteYn);
    }
}
