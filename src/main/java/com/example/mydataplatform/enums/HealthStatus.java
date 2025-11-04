package com.example.mydataplatform.enums;

/**
 * 健康状态枚举
 */
public enum HealthStatus {
    UNKNOWN(0, "未知"),
    HEALTHY(1, "正常"),
    UNHEALTHY(2, "异常");

    private final int code;
    private final String description;

    HealthStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
