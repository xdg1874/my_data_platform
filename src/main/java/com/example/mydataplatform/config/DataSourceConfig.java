package com.example.mydataplatform.config;

/**
 * 数据源配置基类
 */
public abstract class DataSourceConfig {
    protected String type;
    protected String host;
    protected Integer port;
    protected String username;
    protected String password;

    /**
     * 验证配置参数
     */
    public abstract void validate();

    /**
     * 获取连接字符串
     */
    public abstract String getConnectionString();

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
