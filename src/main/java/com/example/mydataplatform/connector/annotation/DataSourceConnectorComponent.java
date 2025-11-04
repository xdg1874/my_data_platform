package com.example.mydataplatform.connector.annotation;

import com.example.mydataplatform.enums.DataSourceType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据源连接器注册注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface DataSourceConnectorComponent {
    DataSourceType value();
}
