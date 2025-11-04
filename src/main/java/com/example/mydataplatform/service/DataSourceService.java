package com.example.mydataplatform.service;

import com.example.mydataplatform.config.DataSourceConfig;
import com.example.mydataplatform.connector.DataSourceConnector;
import com.example.mydataplatform.connector.factory.DataSourceConnectorFactory;
import com.example.mydataplatform.dto.*;
import com.example.mydataplatform.dto.request.CreateDataSourceRequest;
import com.example.mydataplatform.dto.request.QueryDataRequest;
import com.example.mydataplatform.entity.DataSource;
import com.example.mydataplatform.enums.DataSourceStatus;
import com.example.mydataplatform.enums.DataSourceType;
import com.example.mydataplatform.enums.HealthStatus;
import com.example.mydataplatform.exception.DataSourceException;
import com.example.mydataplatform.repository.DataSourceRepository;
import com.example.mydataplatform.util.ConfigUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据源服务
 */
@Service
@Transactional
public class DataSourceService {

    private static final Logger log = LoggerFactory.getLogger(DataSourceService.class);

    @Autowired
    private DataSourceConnectorFactory connectorFactory;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建数据源
     */
    public DataSourceDTO createDataSource(CreateDataSourceRequest request) {
        // 检查名称是否已存在
        if (dataSourceRepository.existsByName(request.getName())) {
            throw new DataSourceException("数据源名称已存在: " + request.getName());
        }

        // 验证配置
        DataSourceConfig config = ConfigUtils.parseConfig(request.getType(), request.getConfig());
        config.validate();

        // 测试连接
        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(request.getType()));
        ConnectionResult testResult = connector.testConnection(config);

        if (!testResult.isSuccess()) {
            throw new DataSourceException("数据源连接测试失败: " + testResult.getMessage());
        }

        // 加密敏感信息
        Map<String, Object> encryptedConfig = ConfigUtils.encryptSensitiveFields(request.getConfig());

        // 保存数据源
        DataSource dataSource = new DataSource();
        dataSource.setName(request.getName());
        dataSource.setType(request.getType());
        dataSource.setConfig(encryptedConfig);
        dataSource.setDescription(request.getDescription());
        dataSource.setHealthStatus(HealthStatus.HEALTHY);
        dataSource.setLastTestTime(LocalDateTime.now());
        dataSource.setTestResult(testResult.getMessage());
        dataSource.setCreatedBy(getCurrentUser());

        dataSource = dataSourceRepository.save(dataSource);

        return convertToDTO(dataSource);
    }

    /**
     * 获取数据源列表
     */
    @Transactional(readOnly = true)
    public Page<DataSourceDTO> getDataSources(int page, int size, String type, String status, String keyword) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        DataSourceStatus statusEnum = null;
        if (status != null) {
            statusEnum = DataSourceStatus.valueOf(status.toUpperCase());
        }

        Page<DataSource> dataSourcePage = dataSourceRepository.findDataSources(type, statusEnum, keyword, pageable);
        
        return dataSourcePage.map(this::convertToDTO);
    }

    /**
     * 根据ID获取数据源
     */
    @Transactional(readOnly = true)
    public DataSourceDTO getDataSourceById(Long id) {
        DataSource dataSource = getDataSourceEntityById(id);
        return convertToDTO(dataSource);
    }

    /**
     * 测试数据源连接
     */
    public ConnectionResult testConnection(Long dataSourceId) {
        DataSource dataSource = getDataSourceEntityById(dataSourceId);

        DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));

        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
        ConnectionResult result = connector.testConnection(config);

        // 更新健康状态
        dataSource.setHealthStatus(result.isSuccess() ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY);
        dataSource.setLastTestTime(LocalDateTime.now());
        dataSource.setTestResult(result.getMessage());
        dataSourceRepository.save(dataSource);

        return result;
    }

    /**
     * 获取数据库列表
     */
    @Transactional(readOnly = true)
    public List<String> getDatabases(Long dataSourceId) {
        DataSource dataSource = getDataSourceEntityById(dataSourceId);

        DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));

        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
        return connector.getDatabases(config);
    }

    /**
     * 获取表列表
     */
    @Transactional(readOnly = true)
    public List<TableInfo> getTables(Long dataSourceId, String database) {
        DataSource dataSource = getDataSourceEntityById(dataSourceId);

        DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));

        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
        return connector.getTables(config, database);
    }

    /**
     * 获取表结构
     */
    @Transactional(readOnly = true)
    public TableInfo getTableStructure(Long dataSourceId, String database, String table) {
        DataSource dataSource = getDataSourceEntityById(dataSourceId);

        DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));

        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
        return connector.getTableStructure(config, database, table);
    }

    /**
     * 查询数据
     */
    @Transactional(readOnly = true)
    public QueryResult queryData(Long dataSourceId, QueryDataRequest request) {
        DataSource dataSource = getDataSourceEntityById(dataSourceId);

        DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));

        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
        return connector.executeQuery(config, request);
    }

    /**
     * 写入数据
     */
    public WriteResult writeData(Long dataSourceId, String database, String table, List<Map<String, Object>> data) {
        DataSource dataSource = getDataSourceEntityById(dataSourceId);

        DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));

        DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
        return connector.writeData(config, database, table, data);
    }

    /**
     * 删除数据源
     */
    public void deleteDataSource(Long id) {
        DataSource dataSource = getDataSourceEntityById(id);
        
        // 关闭连接
        try {
            DataSourceConfig config = ConfigUtils.parseConfig(dataSource.getType(),
                    ConfigUtils.decryptSensitiveFields(dataSource.getConfig()));
            DataSourceConnector connector = connectorFactory.getConnector(DataSourceType.fromCode(dataSource.getType()));
            connector.closeConnection(config);
        } catch (Exception e) {
            log.warn("关闭数据源连接失败: {}", e.getMessage());
        }

        dataSourceRepository.delete(dataSource);
    }

    /**
     * 启用/禁用数据源
     */
    public void toggleDataSource(Long id) {
        DataSource dataSource = getDataSourceEntityById(id);
        dataSource.setStatus(dataSource.getStatus() == DataSourceStatus.ENABLED ? 
                           DataSourceStatus.DISABLED : DataSourceStatus.ENABLED);
        dataSourceRepository.save(dataSource);
    }

    /**
     * 获取支持的数据源类型
     */
    @Transactional(readOnly = true)
    public List<DataSourceTypeInfo> getSupportedTypes() {
        return connectorFactory.getSupportedTypes().stream()
                .map(type -> new DataSourceTypeInfo(type.getCode(), type.getDescription()))
                .collect(Collectors.toList());
    }

    /**
     * 健康检查
     */
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void healthCheck() {
        List<DataSource> dataSources = dataSourceRepository.findByStatus(DataSourceStatus.ENABLED);

        for (DataSource dataSource : dataSources) {
            try {
                ConnectionResult result = testConnection(dataSource.getId());
                log.info("数据源健康检查 - {}: {}", dataSource.getName(),
                        result.isSuccess() ? "正常" : "异常");
            } catch (Exception e) {
                log.error("数据源健康检查失败 - {}: {}", dataSource.getName(), e.getMessage());
            }
        }
    }

    // 私有辅助方法
    private DataSource getDataSourceEntityById(Long id) {
        return dataSourceRepository.findById(id)
                .orElseThrow(() -> new DataSourceException("数据源不存在: " + id));
    }

    private DataSourceDTO convertToDTO(DataSource dataSource) {
        DataSourceDTO dto = new DataSourceDTO();
        BeanUtils.copyProperties(dataSource, dto);
        
        // 设置脱敏后的配置信息
        dto.setConfigMask(ConfigUtils.maskSensitiveFields(dataSource.getConfig()));
        
        return dto;
    }

    private String getCurrentUser() {
        // TODO: 从安全上下文获取当前用户
        return "system";
    }
}
