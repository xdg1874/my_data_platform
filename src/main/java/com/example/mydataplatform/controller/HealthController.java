package com.example.mydataplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 呼吸检测接口 - 用于检查服务是否正常运行
     * 
     * @return 服务状态信息
     */
    @GetMapping("/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "服务正常运行");
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.put("service", "my-data-platform");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 简单的ping接口
     * 
     * @return pong响应
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    /**
     * 详细的健康检查接口
     * 
     * @return 详细的系统状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 基本信息
        status.put("application", "my-data-platform");
        status.put("version", "1.0.0");
        status.put("status", "HEALTHY");
        status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // 系统信息
        Map<String, Object> system = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("totalMemory", runtime.totalMemory());
        system.put("freeMemory", runtime.freeMemory());
        system.put("maxMemory", runtime.maxMemory());
        system.put("availableProcessors", runtime.availableProcessors());
        
        status.put("system", system);
        
        return ResponseEntity.ok(status);
    }
} 