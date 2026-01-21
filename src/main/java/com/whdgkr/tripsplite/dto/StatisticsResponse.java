package com.whdgkr.tripsplite.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StatisticsResponse {
    private Integer totalExpense;
    private List<CategoryStat> categoryStats;

    @Data
    @Builder
    public static class CategoryStat {
        private String category;
        private String categoryName;
        private Integer amount;
        private Double percentage;
    }
}
