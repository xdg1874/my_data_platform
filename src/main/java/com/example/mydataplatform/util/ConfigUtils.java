package com.example.mydataplatform.util;

import com.example.mydataplatform.config.*;
import com.example.mydataplatform.enums.DataSourceType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Base64Utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 配置工具类
 */
public class ConfigUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 敏感字段列表
    private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList("password", "token", "secret", "key"));

    /**
     * 解析配置
     */
    public static DataSourceConfig parseConfig(String type, Map<String, Object> configMap) {
        try {
            DataSourceType dataSourceType = DataSourceType.fromCode(type);
            
            // 添加type字段到配置中
            Map<String, Object> configWithType = new HashMap<>(configMap);
            configWithType.put("type", type);
            
            String configJson = objectMapper.writeValueAsString(configWithType);
            
            switch (dataSourceType) {
                case MYSQL:
                    return objectMapper.readValue(configJson, MySQLConfig.class);
                case CLICKHOUSE:
                    return objectMapper.readValue(configJson, ClickHouseConfig.class);
                case KAFKA:
                    return objectMapper.readValue(configJson, KafkaConfig.class);
                case ELASTICSEARCH:
                    return objectMapper.readValue(configJson, ElasticsearchConfig.class);
                default:
                    throw new IllegalArgumentException("不支持的数据源类型: " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException("解析配置失败: " + e.getMessage(), e);
        }
    }

    /**
     * 加密敏感字段
     */
    public static Map<String, Object> encryptSensitiveFields(Map<String, Object> config) {
        Map<String, Object> encryptedConfig = new HashMap<>(config);
        
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (SENSITIVE_FIELDS.contains(key) && entry.getValue() != null) {
                String value = entry.getValue().toString();
                // 简单的Base64编码，实际项目中应使用更安全的加密方式
                encryptedConfig.put(entry.getKey(), Base64Utils.encodeToString(value.getBytes()));
            }
        }
        
        return encryptedConfig;
    }

    /**
     * 解密敏感字段
     */
    public static Map<String, Object> decryptSensitiveFields(Map<String, Object> config) {
        Map<String, Object> decryptedConfig = new HashMap<>(config);
        
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (SENSITIVE_FIELDS.contains(key) && entry.getValue() != null) {
                String encryptedValue = entry.getValue().toString();
                try {
                    // 简单的Base64解码，实际项目中应使用对应的解密方式
                    String decryptedValue = new String(Base64Utils.decodeFromString(encryptedValue));
                    decryptedConfig.put(entry.getKey(), decryptedValue);
                } catch (Exception e) {
                    // 如果解码失败，可能是未加密的值，保持原值
                    decryptedConfig.put(entry.getKey(), encryptedValue);
                }
            }
        }
        
        return decryptedConfig;
    }

    /**
     * 脱敏敏感字段
     */
    public static Map<String, Object> maskSensitiveFields(Map<String, Object> config) {
        Map<String, Object> maskedConfig = new HashMap<>(config);
        
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (SENSITIVE_FIELDS.contains(key) && entry.getValue() != null) {
                maskedConfig.put(entry.getKey(), "******");
            }
        }
        
        return maskedConfig;
    }
}
