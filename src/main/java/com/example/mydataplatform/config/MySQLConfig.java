package com.example.mydataplatform.config;

import org.springframework.util.Assert;

/**
 * MySQL数据源配置
 */
public class MySQLConfig extends DataSourceConfig {
    private String database;
    private String charset = "utf8mb4";
    private Integer connectTimeout = 30000;
    private Integer socketTimeout = 60000;
    private Boolean useSSL = false;

    @Override
    public void validate() {
        Assert.hasText(host, "MySQL主机地址不能为空");
        Assert.notNull(port, "MySQL端口不能为空");
        Assert.hasText(database, "数据库名不能为空");
        Assert.hasText(username, "用户名不能为空");
    }

    @Override
    public String getConnectionString() {
        return String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=%s&useSSL=%s&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true",
                host, port, database, charset, useSSL);
    }

    // Getters and Setters
    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
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

    public Boolean getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(Boolean useSSL) {
        this.useSSL = useSSL;
    }
}
