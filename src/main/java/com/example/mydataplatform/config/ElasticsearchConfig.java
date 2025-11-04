package com.example.mydataplatform.config;

import org.springframework.util.Assert;

import java.util.List;

/**
 * Elasticsearch数据源配置
 */
public class ElasticsearchConfig extends DataSourceConfig {
    private String scheme = "http";
    private List<String> hosts;
    private String pathPrefix;
    private Integer connectTimeout = 30000;
    private Integer socketTimeout = 60000;
    private Boolean enableSniffer = false;

    @Override
    public void validate() {
        Assert.notEmpty(hosts, "Elasticsearch主机列表不能为空");
    }

    @Override
    public String getConnectionString() {
        return String.format("%s://%s", scheme, String.join(",", hosts));
    }

    // Getters and Setters
    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public List<String> getHosts() {
        return hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
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

    public Boolean getEnableSniffer() {
        return enableSniffer;
    }

    public void setEnableSniffer(Boolean enableSniffer) {
        this.enableSniffer = enableSniffer;
    }
}
