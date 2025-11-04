package com.example.mydataplatform.enums;

/**
 * 数据源类型枚举
 */
public enum DataSourceType {
    MYSQL("mysql", "MySQL数据库"),
    KAFKA("kafka", "Apache Kafka"),
    ELASTICSEARCH("elasticsearch", "Elasticsearch"),
    CLICKHOUSE("clickhouse", "ClickHouse数据库");

    private final String code;
    private final String description;

    DataSourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static DataSourceType fromCode(String code) {
        for (DataSourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown data source type: " + code);
    }
}
