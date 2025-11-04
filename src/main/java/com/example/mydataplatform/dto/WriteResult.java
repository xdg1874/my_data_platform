package com.example.mydataplatform.dto;

import java.util.List;

/**
 * 写入结果
 */
public class WriteResult {
    private boolean success;
    private long affectedRows;
    private String message;
    private long executionTime;
    private List<String> errors;

    public WriteResult() {}

    public WriteResult(boolean success, long affectedRows, String message, long executionTime, List<String> errors) {
        this.success = success;
        this.affectedRows = affectedRows;
        this.message = message;
        this.executionTime = executionTime;
        this.errors = errors;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getAffectedRows() {
        return affectedRows;
    }

    public void setAffectedRows(long affectedRows) {
        this.affectedRows = affectedRows;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
