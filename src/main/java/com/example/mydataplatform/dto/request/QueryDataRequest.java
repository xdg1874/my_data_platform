package com.example.mydataplatform.dto.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Map;

/**
 * 查询数据请求
 */
public class QueryDataRequest {

    private String sql;
    private String database;
    private String table;
    private Map<String, Object> parameters;

    @Min(value = 1, message = "页面大小必须大于0")
    @Max(value = 10000, message = "页面大小不能超过10000")
    private Integer pageSize = 1000;

    private String cursor;

    @Min(value = 1000, message = "超时时间不能小于1秒")
    @Max(value = 300000, message = "超时时间不能超过5分钟")
    private Integer timeout = 30000;

    // Getters and Setters
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getCursor() {
        return cursor;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
