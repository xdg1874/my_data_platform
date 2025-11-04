package com.example.mydataplatform.controller;

import com.example.mydataplatform.dto.*;
import com.example.mydataplatform.dto.request.CreateDataSourceRequest;
import com.example.mydataplatform.dto.request.QueryDataRequest;
import com.example.mydataplatform.dto.response.ApiResponse;
import com.example.mydataplatform.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 数据源管理控制器
 */
@RestController
@RequestMapping("/api/v1/datasources")
@Validated
public class DataSourceController {

    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 获取支持的数据源类型
     */
    @GetMapping("/types")
    public ApiResponse<List<DataSourceTypeInfo>> getSupportedTypes() {
        List<DataSourceTypeInfo> types = dataSourceService.getSupportedTypes();
        return ApiResponse.success(types);
    }

    /**
     * 创建数据源
     */
    @PostMapping
    public ApiResponse<DataSourceDTO> createDataSource(@Valid @RequestBody CreateDataSourceRequest request) {
        DataSourceDTO dataSource = dataSourceService.createDataSource(request);
        return ApiResponse.success("数据源创建成功", dataSource);
    }

    /**
     * 获取数据源列表
     */
    @GetMapping
    public ApiResponse<Page<DataSourceDTO>> getDataSources(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {

        Page<DataSourceDTO> result = dataSourceService.getDataSources(page, size, type, status, keyword);
        return ApiResponse.success(result);
    }

    /**
     * 获取数据源详情
     */
    @GetMapping("/{id}")
    public ApiResponse<DataSourceDTO> getDataSource(@PathVariable Long id) {
        DataSourceDTO dataSource = dataSourceService.getDataSourceById(id);
        return ApiResponse.success(dataSource);
    }

    /**
     * 删除数据源
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteDataSource(@PathVariable Long id) {
        dataSourceService.deleteDataSource(id);
        return ApiResponse.success("数据源删除成功");
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/{id}/test")
    public ApiResponse<ConnectionResult> testConnection(@PathVariable Long id) {
        ConnectionResult result = dataSourceService.testConnection(id);
        return ApiResponse.success(result);
    }

    /**
     * 获取数据库列表
     */
    @GetMapping("/{id}/databases")
    public ApiResponse<List<String>> getDatabases(@PathVariable Long id) {
        List<String> databases = dataSourceService.getDatabases(id);
        return ApiResponse.success(databases);
    }

    /**
     * 获取表列表
     */
    @GetMapping("/{id}/tables")
    public ApiResponse<List<TableInfo>> getTables(
            @PathVariable Long id,
            @RequestParam(required = false) String database) {

        List<TableInfo> tables = dataSourceService.getTables(id, database);
        return ApiResponse.success(tables);
    }

    /**
     * 获取表结构
     */
    @GetMapping("/{id}/tables/{table}/structure")
    public ApiResponse<TableInfo> getTableStructure(
            @PathVariable Long id,
            @PathVariable String table,
            @RequestParam(required = false) String database) {

        TableInfo tableInfo = dataSourceService.getTableStructure(id, database, table);
        return ApiResponse.success(tableInfo);
    }

    /**
     * 查询数据
     */
    @PostMapping("/{id}/query")
    public ApiResponse<QueryResult> queryData(
            @PathVariable Long id,
            @Valid @RequestBody QueryDataRequest request) {

        QueryResult result = dataSourceService.queryData(id, request);
        return ApiResponse.success(result);
    }

    /**
     * 预览数据（限制返回条数）
     */
    @PostMapping("/{id}/preview")
    public ApiResponse<QueryResult> previewData(
            @PathVariable Long id,
            @RequestParam(required = false) String database,
            @RequestParam String table,
            @RequestParam(defaultValue = "10") int limit) {

        // 构建预览查询请求
        QueryDataRequest request = new QueryDataRequest();
        request.setDatabase(database);
        request.setTable(table);
        request.setPageSize(Math.min(limit, 100)); // 最多100条
        
        // 构建简单的查询SQL
        request.setSql("SELECT * FROM " + table + " LIMIT " + request.getPageSize());

        QueryResult result = dataSourceService.queryData(id, request);
        return ApiResponse.success(result);
    }

    /**
     * 写入数据
     */
    @PostMapping("/{id}/write")
    public ApiResponse<WriteResult> writeData(
            @PathVariable Long id,
            @RequestParam(required = false) String database,
            @RequestParam String table,
            @RequestBody List<Map<String, Object>> data) {

        WriteResult result = dataSourceService.writeData(id, database, table, data);
        return ApiResponse.success(result);
    }

    /**
     * 启用/禁用数据源
     */
    @PostMapping("/{id}/toggle")
    public ApiResponse<String> toggleDataSource(@PathVariable Long id) {
        dataSourceService.toggleDataSource(id);
        return ApiResponse.success("数据源状态切换成功");
    }
}
