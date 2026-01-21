package com.whdgkr.tripsplite.entity;

public enum ExpenseCategory {
    FOOD("식비"),
    ACCOMMODATION("숙박"),
    TRANSPORTATION("교통"),
    ENTERTAINMENT("관광"),
    SHOPPING("쇼핑"),
    OTHER("기타");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
