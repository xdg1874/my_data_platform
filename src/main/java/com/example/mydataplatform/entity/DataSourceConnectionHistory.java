package com.example.mydataplatform.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据源连接历史实体类
 */
@Entity
@Table(name = "datasource_connection_history")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class DataSourceConnectionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datasource_id", nullable = false)
    private Long dataSourceId;

    @Column(name = "test_time", nullable = false)
    private LocalDateTime testTime;

    @Column(name = "success", nullable = false, columnDefinition = "TINYINT")
    private Boolean success;

    @Column(name = "response_time")
    private Long responseTime;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Type(type = "json")
    @Column(name = "metadata", columnDefinition = "TEXT")
    private Map<String, Object> metadata;

    // 关联数据源
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasource_id", insertable = false, updatable = false)
    private DataSource dataSource;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public LocalDateTime getTestTime() {
        return testTime;
    }

    public void setTestTime(LocalDateTime testTime) {
        this.testTime = testTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Long responseTime) {
        this.responseTime = responseTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
