package com.example.mydataplatform.connector;

import com.example.mydataplatform.config.DataSourceConfig;
import com.example.mydataplatform.dto.ConnectionResult;
import com.example.mydataplatform.dto.QueryResult;
import com.example.mydataplatform.dto.TableInfo;
import com.example.mydataplatform.dto.WriteResult;
import com.example.mydataplatform.dto.request.QueryDataRequest;
import com.example.mydataplatform.enums.DataSourceType;

import java.util.List;
import java.util.Map;

/**
 * 数据源连接器接口
 */
public interface DataSourceConnector {

    /**
     * 获取支持的数据源类型
     */
    DataSourceType getSupportedType();

    /**
     * 测试连接
     */
    ConnectionResult testConnection(DataSourceConfig config);

    /**
     * 获取数据库列表
     */
    List<String> getDatabases(DataSourceConfig config);

    /**
     * 获取表列表
     */
    List<TableInfo> getTables(DataSourceConfig config, String database);

    /**
     * 获取表结构
     */
    TableInfo getTableStructure(DataSourceConfig config, String database, String table);

    /**
     * 执行查询（支持分页）
     */
    QueryResult executeQuery(DataSourceConfig config, QueryDataRequest request);

    /**
     * 写入数据
     */
    WriteResult writeData(DataSourceConfig config, String database, String table, List<Map<String, Object>> data);

    /**
     * 关闭连接
     */
    void closeConnection(DataSourceConfig config);
}
