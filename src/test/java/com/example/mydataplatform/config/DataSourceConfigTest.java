package com.example.mydataplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据源配置测试
 */
@SpringBootTest
public class DataSourceConfigTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testMySQLConfigSerialization() throws Exception {
        // 创建MySQL配置
        MySQLConfig mysqlConfig = new MySQLConfig();
        mysqlConfig.setType("mysql");
        mysqlConfig.setHost("localhost");
        mysqlConfig.setPort(3306);
        mysqlConfig.setDatabase("test");
        mysqlConfig.setUsername("root");
        mysqlConfig.setPassword("password");

        // 序列化
        String json = objectMapper.writeValueAsString(mysqlConfig);
        System.out.println("MySQL Config JSON: " + json);

        // 反序列化
        DataSourceConfig config = objectMapper.readValue(json, DataSourceConfig.class);
        assertTrue(config instanceof MySQLConfig);
        assertEquals("mysql", config.getType());
        assertEquals("localhost", config.getHost());
    }

    @Test
    public void testConfigFromMap() throws Exception {
        // 模拟从前端接收到的配置数据
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("type", "mysql");
        configMap.put("host", "localhost");
        configMap.put("port", 3306);
        configMap.put("database", "test");
        configMap.put("username", "root");
        configMap.put("password", "password");

        // 转换为JSON字符串
        String json = objectMapper.writeValueAsString(configMap);
        System.out.println("Config Map JSON: " + json);

        // 反序列化为DataSourceConfig
        DataSourceConfig config = objectMapper.readValue(json, DataSourceConfig.class);
        assertTrue(config instanceof MySQLConfig);
        assertEquals("mysql", config.getType());
    }

    @Test
    public void testMySQLConfigValidation() {
        MySQLConfig config = new MySQLConfig();
        config.setHost("localhost");
        config.setPort(3306);
        config.setDatabase("test");
        config.setUsername("root");

        // 验证应该通过
        assertDoesNotThrow(() -> config.validate());

        // 测试验证失败的情况
        MySQLConfig invalidConfig = new MySQLConfig();
        assertThrows(IllegalArgumentException.class, () -> invalidConfig.validate());
    }
}
