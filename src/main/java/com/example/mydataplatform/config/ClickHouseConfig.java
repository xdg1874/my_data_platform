package com.example.mydataplatform.config;

import org.springframework.util.Assert;

/**
 * ClickHouse数据源配置
 */
public class ClickHouseConfig extends DataSourceConfig {
    private String database = "default";
    private Integer connectTimeout = 30000;
    private Integer socketTimeout = 60000;
    private Boolean compress = true;
    private String profile;

    @Override
    public void validate() {
        Assert.hasText(host, "ClickHouse主机地址不能为空");
        Assert.notNull(port, "ClickHouse端口不能为空");
    }

    @Override
    public String getConnectionString() {
        return String.format("jdbc:clickhouse://%s:%d/%s", host, port, database);
    }

    // Getters and Setters
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Boolean getCompress() {
        return compress;
    }

    public void setCompress(Boolean compress) {
        this.compress = compress;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
