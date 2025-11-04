package com.example.mydataplatform.entity;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据源操作日志实体类
 */
@Entity
@Table(name = "datasource_operation_log")
@TypeDef(name = "json", typeClass = JsonStringType.class)
public class DataSourceOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datasource_id", nullable = false)
    private Long dataSourceId;

    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;

    @Type(type = "json")
    @Column(name = "operation_detail", columnDefinition = "TEXT")
    private Map<String, Object> operationDetail;

    @Column(name = "success", nullable = false, columnDefinition = "TINYINT")
    private Boolean success;

    @Column(name = "execution_time")
    private Long executionTime;

    @Column(name = "records_affected")
    private Long recordsAffected = 0L;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    // 关联数据源
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "datasource_id", insertable = false, updatable = false)
    private DataSource dataSource;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }

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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Map<String, Object> getOperationDetail() {
        return operationDetail;
    }

    public void setOperationDetail(Map<String, Object> operationDetail) {
        this.operationDetail = operationDetail;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getRecordsAffected() {
        return recordsAffected;
    }

    public void setRecordsAffected(Long recordsAffected) {
        this.recordsAffected = recordsAffected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
