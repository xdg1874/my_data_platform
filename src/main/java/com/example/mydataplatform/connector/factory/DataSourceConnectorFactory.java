package com.example.mydataplatform.connector.factory;

import com.example.mydataplatform.connector.DataSourceConnector;
import com.example.mydataplatform.enums.DataSourceType;
import com.example.mydataplatform.exception.UnsupportedDataSourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源连接器工厂
 */
@Component
public class DataSourceConnectorFactory {

    @Autowired
    private ApplicationContext applicationContext;

    private final Map<DataSourceType, DataSourceConnector> connectors = new ConcurrentHashMap<>();

    /**
     * 注册数据源连接器
     */
    public void registerConnector(DataSourceConnector connector) {
        connectors.put(connector.getSupportedType(), connector);
    }

    /**
     * 获取数据源连接器
     */
    public DataSourceConnector getConnector(DataSourceType type) {
        DataSourceConnector connector = connectors.get(type);
        if (connector == null) {
            throw new UnsupportedDataSourceException("不支持的数据源类型: " + type);
        }
        return connector;
    }

    /**
     * 获取所有支持的数据源类型
     */
    public Set<DataSourceType> getSupportedTypes() {
        return connectors.keySet();
    }

    /**
     * 自动注册所有连接器
     */
    @PostConstruct
    public void autoRegisterConnectors() {
        Map<String, DataSourceConnector> beans = applicationContext.getBeansOfType(DataSourceConnector.class);
        beans.values().forEach(this::registerConnector);
    }
}
