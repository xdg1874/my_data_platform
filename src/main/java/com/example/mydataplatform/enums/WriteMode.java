package com.example.mydataplatform.enums;

/**
 * 写入模式枚举
 */
public enum WriteMode {
    INSERT("插入"),
    UPDATE("更新"),
    UPSERT("插入或更新"),
    REPLACE("替换");

    private final String description;

    WriteMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
