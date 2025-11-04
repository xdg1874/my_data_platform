-- 数据平台数据库初始化脚本
-- 如果Flyway无法正常工作，可以手动执行此脚本

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS my_dp_1 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE my_dp_1;

-- 数据源表
CREATE TABLE IF NOT EXISTS datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    type VARCHAR(50) NOT NULL COMMENT '数据源类型(MYSQL/KAFKA/ELASTICSEARCH/CLICKHOUSE等)',
    config JSON NOT NULL COMMENT '连接配置(加密存储)',
    description TEXT COMMENT '数据源描述',
    status TINYINT DEFAULT 1 COMMENT '状态(1:启用 0:禁用)',
    health_status TINYINT DEFAULT 0 COMMENT '健康状态(0:未知 1:正常 2:异常)',
    last_test_time DATETIME COMMENT '最后测试时间',
    test_result TEXT COMMENT '测试结果信息',
    created_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_health_status (health_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- 数据源连接历史表
CREATE TABLE IF NOT EXISTS datasource_connection_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    test_time DATETIME NOT NULL COMMENT '测试时间',
    success TINYINT NOT NULL COMMENT '是否成功(1:成功 0:失败)',
    response_time BIGINT COMMENT '响应时间(毫秒)',
    error_message TEXT COMMENT '错误信息',
    metadata JSON COMMENT '连接元数据',
    
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_test_time (test_time),
    FOREIGN KEY (datasource_id) REFERENCES datasource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源连接历史表';

-- 数据源操作日志表
CREATE TABLE IF NOT EXISTS datasource_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    datasource_id BIGINT NOT NULL COMMENT '数据源ID',
    operation_type VARCHAR(50) NOT NULL COMMENT '操作类型(QUERY/WRITE/TEST等)',
    operation_detail JSON COMMENT '操作详情',
    success TINYINT NOT NULL COMMENT '是否成功(1:成功 0:失败)',
    execution_time BIGINT COMMENT '执行时间(毫秒)',
    records_affected BIGINT DEFAULT 0 COMMENT '影响记录数',
    error_message TEXT COMMENT '错误信息',
    operator VARCHAR(50) COMMENT '操作人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_datasource_id (datasource_id),
    INDEX idx_operation_type (operation_type),
    INDEX idx_create_time (create_time),
    FOREIGN KEY (datasource_id) REFERENCES datasource(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源操作日志表';

-- 插入一些示例数据（可选）
INSERT IGNORE INTO datasource (id, name, type, config, description, created_by) VALUES 
(1, 'MySQL测试数据源', 'mysql', '{"host":"localhost","port":3306,"database":"test","username":"root","password":"cGFzc3dvcmQ="}', 'MySQL测试数据源', 'system');

-- 显示创建的表
SHOW TABLES;

SELECT 'Database initialization completed!' as message;
