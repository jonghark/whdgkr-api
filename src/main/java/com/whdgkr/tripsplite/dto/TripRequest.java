package com.whdgkr.tripsplite.dto;

import lombok.Data;

import java.util.List;

@Data
public class TripRequest {
    private String title;
    private String name;      // Alternative field name for frontend compatibility
    private String startDate; // ISO-8601: 2025-12-28
    private String endDate;
    private List<Long> friendIds;  // Friend IDs to add as participants
    private List<ParticipantInfo> participants;  // Detailed participant info

    @Data
    public static class ParticipantInfo {
        private String name;
        private String phone;
        private String email;
        private Boolean isOwner;
    }

    // Helper method to get the trip name from either field
    public String getTripName() {
        return title != null ? title : name;
    }
}
