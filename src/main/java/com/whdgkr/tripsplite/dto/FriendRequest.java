package com.whdgkr.tripsplite.dto;

import lombok.Data;

@Data
public class FriendRequest {
    private String name;
    private String email;
    private String phone;
    private String friendLoginId;
    private String friendId;  // 친구의 로그인 ID (필수)
}
