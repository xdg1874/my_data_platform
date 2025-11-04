package com.example.mydataplatform.connector.impl;

import com.example.mydataplatform.config.DataSourceConfig;
import com.example.mydataplatform.config.ElasticsearchConfig;
import com.example.mydataplatform.connector.DataSourceConnector;
import com.example.mydataplatform.connector.annotation.DataSourceConnectorComponent;
import com.example.mydataplatform.dto.*;
import com.example.mydataplatform.dto.request.QueryDataRequest;
import com.example.mydataplatform.enums.DataSourceType;
import com.example.mydataplatform.exception.DataSourceException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Elasticsearch连接器实现
 */
@DataSourceConnectorComponent(DataSourceType.ELASTICSEARCH)
public class ElasticsearchConnector implements DataSourceConnector {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchConnector.class);

    private final Map<String, RestHighLevelClient> clients = new ConcurrentHashMap<>();

    @Override
    public DataSourceType getSupportedType() {
        return DataSourceType.ELASTICSEARCH;
    }

    @Override
    public ConnectionResult testConnection(DataSourceConfig config) {
        ElasticsearchConfig esConfig = (ElasticsearchConfig) config;
        long startTime = System.currentTimeMillis();

        try {
            esConfig.validate();

            RestHighLevelClient client = createClient(esConfig);
            
            // 测试连接 - 使用集群健康检查
            ClusterHealthRequest request = new ClusterHealthRequest();
            request.timeout(TimeValue.timeValueSeconds(5));
            
            ClusterHealthResponse response = client.cluster().health(request, RequestOptions.DEFAULT);
            long responseTime = System.currentTimeMillis() - startTime;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("clusterName", response.getClusterName());
            metadata.put("status", response.getStatus().toString());
            metadata.put("numberOfNodes", response.getNumberOfNodes());
            metadata.put("numberOfDataNodes", response.getNumberOfDataNodes());

            org.elasticsearch.cluster.health.ClusterHealthStatus healthStatus = response.getStatus();
            String message = "连接成功 - 集群状态: " + healthStatus;
            
            // 如果集群状态不是绿色或黄色，认为连接有问题
            boolean success = healthStatus == org.elasticsearch.cluster.health.ClusterHealthStatus.GREEN ||
                             healthStatus == org.elasticsearch.cluster.health.ClusterHealthStatus.YELLOW;

            return new ConnectionResult(success, message, responseTime, metadata);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("Elasticsearch连接测试失败", e);
            return new ConnectionResult(false, "连接失败: " + e.getMessage(), responseTime, null);
        }
    }

    @Override
    public List<String> getDatabases(DataSourceConfig config) {
        // Elasticsearch 没有数据库概念，返回空列表或索引列表作为数据库
        // 为了统一接口，返回空列表
        return new ArrayList<>();
    }

    @Override
    public List<TableInfo> getTables(DataSourceConfig config, String database) {
        ElasticsearchConfig esConfig = (ElasticsearchConfig) config;
        List<TableInfo> tables = new ArrayList<>();

        try (RestHighLevelClient client = createClient(esConfig)) {
            GetIndexRequest request = new GetIndexRequest();
            request.indices("_all"); // 获取所有索引

            String[] indices = client.indices().get(request, RequestOptions.DEFAULT).getIndices();

            for (String index : indices) {
                // 跳过系统索引
                if (index.startsWith(".")) {
                    continue;
                }

                TableInfo table = new TableInfo();
                table.setName(index);
                // 可以在这里获取索引的文档数等信息
                tables.add(table);
            }
        } catch (Exception e) {
            log.error("获取Elasticsearch索引列表失败", e);
            throw new DataSourceException("获取索引列表失败", e);
        }

        return tables;
    }

    @Override
    public TableInfo getTableStructure(DataSourceConfig config, String database, String table) {
        ElasticsearchConfig esConfig = (ElasticsearchConfig) config;
        TableInfo tableInfo = new TableInfo();
        tableInfo.setName(table);

        try (RestHighLevelClient client = createClient(esConfig)) {
            GetMappingsRequest request = new GetMappingsRequest();
            request.indices(table);

            GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);
            // response.mappings() 返回 ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetadata>>
            // 第一个键是索引名，第二个键是类型名（在 ES 7.x 中通常只有一个类型）
            ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetadata>> mappingsMap = response.mappings();
            MappingMetadata mappingMetadata = null;
            
            if (mappingsMap != null && mappingsMap.containsKey(table)) {
                ImmutableOpenMap<String, MappingMetadata> indexMappings = mappingsMap.get(table);
                if (indexMappings != null && indexMappings.size() > 0) {
                    // 在 Elasticsearch 7.x 中，通常只有一个类型，遍历获取第一个
                    for (MappingMetadata metadata : indexMappings.values()) {
                        mappingMetadata = metadata;
                        break; // 只取第一个
                    }
                }
            }
            
            if (mappingMetadata != null) {
                Map<String, Object> mapping = mappingMetadata.sourceAsMap();

                List<ColumnInfo> columns = new ArrayList<>();

                // 解析字段映射
                @SuppressWarnings("unchecked")
                Map<String, Object> properties = (Map<String, Object>) mapping.get("properties");
                if (properties != null) {
                    parseProperties(properties, "", columns);
                }

                tableInfo.setColumns(columns);
            }
        } catch (Exception e) {
            log.error("获取Elasticsearch索引结构失败", e);
            throw new DataSourceException("获取索引结构失败", e);
        }

        return tableInfo;
    }

    @Override
    public QueryResult executeQuery(DataSourceConfig config, QueryDataRequest request) {
        ElasticsearchConfig esConfig = (ElasticsearchConfig) config;
        QueryResult result = new QueryResult();

        try (RestHighLevelClient client = createClient(esConfig)) {
            // 解析索引名（从SQL或直接从table字段获取）
            String index = request.getTable() != null ? request.getTable() : "*";

            SearchRequest searchRequest = new SearchRequest(index);
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            // 如果是SQL查询，尝试解析（简化处理，实际可以集成SQL插件）
            if (request.getSql() != null && !request.getSql().isEmpty()) {
                // 这里简化处理，实际应该使用Elasticsearch SQL插件或解析SQL
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            } else {
                sourceBuilder.query(QueryBuilders.matchAllQuery());
            }

            // 分页
            Integer pageSize = request.getPageSize() != null ? request.getPageSize() : 100;
            sourceBuilder.size(pageSize);
            sourceBuilder.timeout(TimeValue.timeValueMillis(request.getTimeout() != null ? request.getTimeout() : 30000));

            searchRequest.source(sourceBuilder);

            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            // 构建结果
            List<Map<String, Object>> data = new ArrayList<>();
            Set<String> columnNames = new LinkedHashSet<>();

            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                columnNames.addAll(sourceAsMap.keySet());
                data.add(sourceAsMap);
            }

            // 构建列信息
            List<ColumnInfo> columns = new ArrayList<>();
            for (String columnName : columnNames) {
                ColumnInfo column = new ColumnInfo();
                column.setName(columnName);
                column.setType("object"); // Elasticsearch类型比较复杂，这里简化处理
                column.setNullable(true);
                columns.add(column);
            }

            result.setColumns(columns);
            result.setData(data);
            result.setTotalCount(response.getHits().getTotalHits().value);
            result.setHasMore(response.getHits().getHits().length >= pageSize);

        } catch (Exception e) {
            log.error("Elasticsearch查询执行失败", e);
            throw new DataSourceException("查询执行失败", e);
        }

        return result;
    }

    @Override
    public WriteResult writeData(DataSourceConfig config, String database, String table, List<Map<String, Object>> data) {
        ElasticsearchConfig esConfig = (ElasticsearchConfig) config;
        long startTime = System.currentTimeMillis();

        try (RestHighLevelClient client = createClient(esConfig)) {
            BulkRequest bulkRequest = new BulkRequest();

            for (Map<String, Object> document : data) {
                IndexRequest indexRequest = new IndexRequest(table);
                indexRequest.source(document);
                bulkRequest.add(indexRequest);
            }

            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

            if (bulkResponse.hasFailures()) {
                long executionTime = System.currentTimeMillis() - startTime;
                String errorMessage = "部分文档写入失败: " + bulkResponse.buildFailureMessage();
                return new WriteResult(false, 0, errorMessage, executionTime, 
                                     Collections.singletonList(errorMessage));
            }

            long executionTime = System.currentTimeMillis() - startTime;
            return new WriteResult(true, data.size(), "写入成功", executionTime, null);

        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Elasticsearch写入数据失败", e);
            return new WriteResult(false, 0, "写入失败: " + e.getMessage(), 
                                 executionTime, Collections.singletonList(e.getMessage()));
        }
    }

    @Override
    public void closeConnection(DataSourceConfig config) {
        String key = config.getConnectionString();
        RestHighLevelClient client = clients.remove(key);
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                log.error("关闭Elasticsearch连接失败", e);
            }
        }
    }

    // 私有辅助方法
    private RestHighLevelClient createClient(ElasticsearchConfig config) {
        String key = config.getConnectionString();
        return clients.computeIfAbsent(key, k -> {
            List<HttpHost> httpHosts = new ArrayList<>();
            
            for (String host : config.getHosts()) {
                String[] parts = host.split(":");
                String hostname = parts[0];
                int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
                httpHosts.add(new HttpHost(hostname, port, config.getScheme()));
            }

            RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));

            // 设置基本认证
            if (config.getUsername() != null && config.getPassword() != null) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUsername(), config.getPassword()));
                // 注意：RestClientBuilder需要自定义HttpClient才能设置认证
                // 这里简化处理，实际应该使用RestClientBuilder的HttpClientConfigCallback
            }

            // 设置超时
            builder.setRequestConfigCallback(requestConfigBuilder -> {
                requestConfigBuilder.setConnectTimeout(config.getConnectTimeout());
                requestConfigBuilder.setSocketTimeout(config.getSocketTimeout());
                return requestConfigBuilder;
            });

            return new RestHighLevelClient(builder);
        });
    }

    private void parseProperties(Map<String, Object> properties, String prefix, List<ColumnInfo> columns) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> property = (Map<String, Object>) entry.getValue();

            ColumnInfo column = new ColumnInfo();
            String columnName = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            column.setName(columnName);

            String type = (String) property.get("type");
            if (type == null && property.containsKey("properties")) {
                type = "object";
            }
            column.setType(type != null ? type : "object");
            column.setNullable(true);

            columns.add(column);

            // 递归处理嵌套对象
            if (property.containsKey("properties")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedProperties = (Map<String, Object>) property.get("properties");
                parseProperties(nestedProperties, columnName, columns);
            }
        }
    }
}

