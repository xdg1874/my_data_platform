package com.example.mydataplatform.config;

import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka数据源配置
 */
public class KafkaConfig extends DataSourceConfig {
    private String bootstrapServers;
    private String securityProtocol = "PLAINTEXT";
    private String saslMechanism;
    private String saslJaasConfig;
    private Map<String, String> additionalProperties = new HashMap<>();

    @Override
    public void validate() {
        Assert.hasText(bootstrapServers, "Kafka服务器地址不能为空");
    }

    @Override
    public String getConnectionString() {
        return bootstrapServers;
    }

    // Getters and Setters
    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSaslJaasConfig() {
        return saslJaasConfig;
    }

    public void setSaslJaasConfig(String saslJaasConfig) {
        this.saslJaasConfig = saslJaasConfig;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
