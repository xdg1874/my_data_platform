package com.example.mydataplatform.dto;

import java.util.Map;

/**
 * 连接测试结果
 */
public class ConnectionResult {
    private boolean success;
    private String message;
    private long responseTime;
    private Map<String, Object> metadata;

    public ConnectionResult() {}

    public ConnectionResult(boolean success, String message, long responseTime, Map<String, Object> metadata) {
        this.success = success;
        this.message = message;
        this.responseTime = responseTime;
        this.metadata = metadata;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
