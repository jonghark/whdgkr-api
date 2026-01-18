package com.whdgkr.tripsplite.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FriendResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String friendId;  // 친구의 로그인 ID
    private Long friendMemberId;
    private String matchedYn;
    private LocalDateTime matchedAt;
    private LocalDateTime createdAt;
}
