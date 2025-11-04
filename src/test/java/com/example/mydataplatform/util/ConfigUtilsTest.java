package com.example.mydataplatform.util;

import com.example.mydataplatform.config.MySQLConfig;
import com.example.mydataplatform.config.DataSourceConfig;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置工具类测试
 */
public class ConfigUtilsTest {

    @Test
    public void testParseConfig() {
        // 创建MySQL配置数据
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("host", "localhost");
        configMap.put("port", 3306);
        configMap.put("database", "test");
        configMap.put("username", "root");
        configMap.put("password", "password");

        // 解析配置
        DataSourceConfig config = ConfigUtils.parseConfig("mysql", configMap);

        // 验证结果
        assertNotNull(config);
        assertTrue(config instanceof MySQLConfig);
        assertEquals("mysql", config.getType());
        assertEquals("localhost", config.getHost());
        assertEquals(Integer.valueOf(3306), config.getPort());
        assertEquals("root", config.getUsername());
        assertEquals("password", config.getPassword());
    }

    @Test
    public void testEncryptSensitiveFields() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("username", "root");
        config.put("password", "secret123");
        config.put("token", "abc123");

        Map<String, Object> encrypted = ConfigUtils.encryptSensitiveFields(config);

        // 验证非敏感字段保持不变
        assertEquals("localhost", encrypted.get("host"));
        assertEquals("root", encrypted.get("username"));

        // 验证敏感字段被加密
        assertNotEquals("secret123", encrypted.get("password"));
        assertNotEquals("abc123", encrypted.get("token"));
    }

    @Test
    public void testMaskSensitiveFields() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", "localhost");
        config.put("username", "root");
        config.put("password", "secret123");

        Map<String, Object> masked = ConfigUtils.maskSensitiveFields(config);

        // 验证非敏感字段保持不变
        assertEquals("localhost", masked.get("host"));
        assertEquals("root", masked.get("username"));

        // 验证敏感字段被脱敏
        assertEquals("******", masked.get("password"));
    }

    @Test
    public void testDecryptSensitiveFields() {
        // 先加密
        Map<String, Object> original = new HashMap<>();
        original.put("password", "secret123");
        Map<String, Object> encrypted = ConfigUtils.encryptSensitiveFields(original);

        // 再解密
        Map<String, Object> decrypted = ConfigUtils.decryptSensitiveFields(encrypted);

        // 验证解密结果
        assertEquals("secret123", decrypted.get("password"));
    }
}
