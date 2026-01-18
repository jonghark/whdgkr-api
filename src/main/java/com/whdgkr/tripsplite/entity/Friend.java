package com.whdgkr.tripsplite.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_member_id")
    private Member ownerMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_member_id")
    private Member friendMember;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    @Column(name = "friend_id", length = 50)
    private String friendId;

    @Column(name = "matched_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Builder.Default
    private String matchedYn = "N";

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    @Column(name = "delete_yn", nullable = false, columnDefinition = "CHAR(1) DEFAULT 'N'")
    @Builder.Default
    private String deleteYn = "N";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isMatched() {
        return "Y".equals(matchedYn);
    }

    public boolean isActive() {
        return "N".equals(deleteYn);
    }

    public void matchWithMember(Member member) {
        this.friendMember = member;
        this.matchedYn = "Y";
        this.matchedAt = LocalDateTime.now();
    }
}
