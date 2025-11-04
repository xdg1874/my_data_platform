package com.example.mydataplatform.dto;

import com.example.mydataplatform.enums.DataSourceStatus;
import com.example.mydataplatform.enums.HealthStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据源DTO
 */
public class DataSourceDTO {
    private Long id;
    private String name;
    private String type;
    private String description;
    private DataSourceStatus status;
    private HealthStatus healthStatus;
    private LocalDateTime lastTestTime;
    private String testResult;
    private String createdBy;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 不包含敏感配置信息，只显示脱敏后的配置
    private Map<String, Object> configMask;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataSourceStatus getStatus() {
        return status;
    }

    public void setStatus(DataSourceStatus status) {
        this.status = status;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public LocalDateTime getLastTestTime() {
        return lastTestTime;
    }

    public void setLastTestTime(LocalDateTime lastTestTime) {
        this.lastTestTime = lastTestTime;
    }

    public String getTestResult() {
        return testResult;
    }

    public void setTestResult(String testResult) {
        this.testResult = testResult;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Map<String, Object> getConfigMask() {
        return configMask;
    }

    public void setConfigMask(Map<String, Object> configMask) {
        this.configMask = configMask;
    }
}
