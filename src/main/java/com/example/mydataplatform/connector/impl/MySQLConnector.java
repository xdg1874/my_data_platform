package com.example.mydataplatform.connector.impl;

import com.example.mydataplatform.config.DataSourceConfig;
import com.example.mydataplatform.config.MySQLConfig;
import com.example.mydataplatform.connector.DataSourceConnector;
import com.example.mydataplatform.connector.annotation.DataSourceConnectorComponent;
import com.example.mydataplatform.dto.*;
import com.example.mydataplatform.dto.request.QueryDataRequest;
import com.example.mydataplatform.enums.DataSourceType;
import com.example.mydataplatform.exception.DataSourceException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MySQL连接器实现
 */
@DataSourceConnectorComponent(DataSourceType.MYSQL)
public class MySQLConnector implements DataSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(MySQLConnector.class);
    
    private final Map<String, HikariDataSource> connectionPools = new ConcurrentHashMap<>();

    @Override
    public DataSourceType getSupportedType() {
        return DataSourceType.MYSQL;
    }

    @Override
    public ConnectionResult testConnection(DataSourceConfig config) {
        MySQLConfig mysqlConfig = (MySQLConfig) config;
        long startTime = System.currentTimeMillis();

        try {
            mysqlConfig.validate();

            // 创建测试连接
            HikariConfig hikariConfig = createHikariConfig(mysqlConfig);
            hikariConfig.setMaximumPoolSize(1);
            hikariConfig.setConnectionTimeout(mysqlConfig.getConnectTimeout());

            try (HikariDataSource dataSource = new HikariDataSource(hikariConfig);
                 Connection connection = dataSource.getConnection()) {

                // 执行简单查询测试
                try (PreparedStatement stmt = connection.prepareStatement("SELECT 1");
                     ResultSet rs = stmt.executeQuery()) {

                    long responseTime = System.currentTimeMillis() - startTime;

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("version", connection.getMetaData().getDatabaseProductVersion());
                    metadata.put("driverVersion", connection.getMetaData().getDriverVersion());

                    return new ConnectionResult(true, "连接成功", responseTime, metadata);
                }
            }
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("MySQL连接测试失败", e);
            return new ConnectionResult(false, "连接失败: " + e.getMessage(), responseTime, null);
        }
    }

    @Override
    public List<String> getDatabases(DataSourceConfig config) {
        MySQLConfig mysqlConfig = (MySQLConfig) config;
        List<String> databases = new ArrayList<>();

        try (Connection connection = getConnection(mysqlConfig);
             PreparedStatement stmt = connection.prepareStatement("SHOW DATABASES");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String dbName = rs.getString(1);
                if (!isSystemDatabase(dbName)) {
                    databases.add(dbName);
                }
            }
        } catch (SQLException e) {
            log.error("获取MySQL数据库列表失败", e);
            throw new DataSourceException("获取数据库列表失败", e);
        }

        return databases;
    }

    @Override
    public List<TableInfo> getTables(DataSourceConfig config, String database) {
        MySQLConfig mysqlConfig = (MySQLConfig) config;
        List<TableInfo> tables = new ArrayList<>();

        String sql = "SELECT TABLE_NAME, TABLE_COMMENT, TABLE_ROWS, CREATE_TIME " +
                    "FROM information_schema.TABLES " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'";

        try (Connection connection = getConnection(mysqlConfig);
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, database);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TableInfo table = new TableInfo();
                    table.setName(rs.getString("TABLE_NAME"));
                    table.setComment(rs.getString("TABLE_COMMENT"));
                    table.setRowCount(rs.getLong("TABLE_ROWS"));
                    table.setCreateTime(rs.getString("CREATE_TIME"));
                    tables.add(table);
                }
            }
        } catch (SQLException e) {
            log.error("获取MySQL表列表失败", e);
            throw new DataSourceException("获取表列表失败", e);
        }

        return tables;
    }

    @Override
    public TableInfo getTableStructure(DataSourceConfig config, String database, String table) {
        MySQLConfig mysqlConfig = (MySQLConfig) config;
        TableInfo tableInfo = new TableInfo();
        tableInfo.setName(table);

        // 获取表信息
        String tableInfoSql = "SELECT TABLE_COMMENT, TABLE_ROWS, CREATE_TIME " +
                             "FROM information_schema.TABLES " +
                             "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";

        // 获取列信息
        String columnsSql = "SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, " +
                           "COLUMN_DEFAULT, COLUMN_COMMENT " +
                           "FROM information_schema.COLUMNS " +
                           "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? " +
                           "ORDER BY ORDINAL_POSITION";

        try (Connection connection = getConnection(mysqlConfig)) {
            // 获取表基本信息
            try (PreparedStatement stmt = connection.prepareStatement(tableInfoSql)) {
                stmt.setString(1, database);
                stmt.setString(2, table);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        tableInfo.setComment(rs.getString("TABLE_COMMENT"));
                        tableInfo.setRowCount(rs.getLong("TABLE_ROWS"));
                        tableInfo.setCreateTime(rs.getString("CREATE_TIME"));
                    }
                }
            }

            // 获取列信息
            List<ColumnInfo> columns = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(columnsSql)) {
                stmt.setString(1, database);
                stmt.setString(2, table);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ColumnInfo column = new ColumnInfo();
                        column.setName(rs.getString("COLUMN_NAME"));
                        column.setType(rs.getString("DATA_TYPE"));
                        column.setNullable("YES".equals(rs.getString("IS_NULLABLE")));
                        column.setDefaultValue(rs.getObject("COLUMN_DEFAULT"));
                        column.setComment(rs.getString("COLUMN_COMMENT"));
                        columns.add(column);
                    }
                }
            }
            tableInfo.setColumns(columns);

        } catch (SQLException e) {
            log.error("获取MySQL表结构失败", e);
            throw new DataSourceException("获取表结构失败", e);
        }

        return tableInfo;
    }

    @Override
    public QueryResult executeQuery(DataSourceConfig config, QueryDataRequest request) {
        MySQLConfig mysqlConfig = (MySQLConfig) config;

        try (Connection connection = getConnection(mysqlConfig);
             PreparedStatement stmt = connection.prepareStatement(request.getSql())) {

            // 设置查询超时
            stmt.setQueryTimeout(request.getTimeout() / 1000);

            // 设置参数
            if (request.getParameters() != null) {
                int index = 1;
                for (Object param : request.getParameters().values()) {
                    stmt.setObject(index++, param);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return buildQueryResult(rs, request.getPageSize());
            }

        } catch (SQLException e) {
            log.error("MySQL查询执行失败", e);
            throw new DataSourceException("查询执行失败", e);
        }
    }

    @Override
    public WriteResult writeData(DataSourceConfig config, String database, String table, List<Map<String, Object>> data) {
        MySQLConfig mysqlConfig = (MySQLConfig) config;
        long startTime = System.currentTimeMillis();

        try (Connection connection = getConnection(mysqlConfig)) {
            connection.setAutoCommit(false);

            String sql = buildInsertSQL(table, data.get(0).keySet());

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setQueryTimeout(30); // 30秒超时

                int affectedRows = 0;
                for (Map<String, Object> row : data) {
                    setStatementParameters(stmt, row);
                    affectedRows += stmt.executeUpdate();
                }

                connection.commit();

                long executionTime = System.currentTimeMillis() - startTime;
                return new WriteResult(true, affectedRows, "写入成功", executionTime, null);

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("MySQL写入数据失败", e);
            return new WriteResult(false, 0, "写入失败: " + e.getMessage(),
                                 executionTime, Arrays.asList(e.getMessage()));
        }
    }

    @Override
    public void closeConnection(DataSourceConfig config) {
        String key = config.getConnectionString();
        HikariDataSource dataSource = connectionPools.remove(key);
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    // 私有辅助方法
    private HikariConfig createHikariConfig(MySQLConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getConnectionString());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(config.getConnectTimeout());
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        return hikariConfig;
    }

    private Connection getConnection(MySQLConfig config) throws SQLException {
        String key = config.getConnectionString();
        HikariDataSource dataSource = connectionPools.computeIfAbsent(key,
            k -> new HikariDataSource(createHikariConfig(config)));
        return dataSource.getConnection();
    }

    private boolean isSystemDatabase(String dbName) {
        return Arrays.asList("information_schema", "mysql", "performance_schema", "sys")
                    .contains(dbName.toLowerCase());
    }

    private QueryResult buildQueryResult(ResultSet rs, Integer pageSize) throws SQLException {
        QueryResult result = new QueryResult();
        List<Map<String, Object>> data = new ArrayList<>();
        List<ColumnInfo> columns = new ArrayList<>();

        // 获取列信息
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            ColumnInfo column = new ColumnInfo();
            column.setName(metaData.getColumnName(i));
            column.setType(metaData.getColumnTypeName(i));
            column.setNullable(metaData.isNullable(i) == ResultSetMetaData.columnNullable);
            columns.add(column);
        }

        // 获取数据
        int count = 0;
        while (rs.next() && count < pageSize) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                row.put(metaData.getColumnName(i), rs.getObject(i));
            }
            data.add(row);
            count++;
        }

        result.setColumns(columns);
        result.setData(data);
        result.setTotalCount(data.size());
        result.setHasMore(rs.next()); // 检查是否还有更多数据

        return result;
    }

    private String buildInsertSQL(String table, Set<String> columns) {
        return "INSERT INTO " + table + " (" +
                String.join(", ", columns) +
                ") VALUES (" +
                String.join(", ", Collections.nCopies(columns.size(), "?")) +
                ")";
    }

    private void setStatementParameters(PreparedStatement stmt, Map<String, Object> row) throws SQLException {
        int index = 1;
        for (Object value : row.values()) {
            stmt.setObject(index++, value);
        }
    }
}
