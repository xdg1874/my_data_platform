package com.example.mydataplatform.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Map;

/**
 * 创建数据源请求
 */
public class CreateDataSourceRequest {

    @NotBlank(message = "数据源名称不能为空")
    @Size(max = 100, message = "数据源名称长度不能超过100字符")
    private String name;

    @NotBlank(message = "数据源类型不能为空")
    private String type;

    @NotNull(message = "数据源配置不能为空")
    private Map<String, Object> config;

    @Size(max = 500, message = "描述长度不能超过500字符")
    private String description;

    // Getters and Setters
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

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
