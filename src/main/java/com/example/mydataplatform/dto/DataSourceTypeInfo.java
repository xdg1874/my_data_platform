package com.example.mydataplatform.dto;

/**
 * 数据源类型信息
 */
public class DataSourceTypeInfo {
    private String code;
    private String description;

    public DataSourceTypeInfo() {}

    public DataSourceTypeInfo(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
