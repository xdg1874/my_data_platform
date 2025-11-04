package com.example.mydataplatform.exception;

/**
 * 不支持的数据源类型异常
 */
public class UnsupportedDataSourceException extends RuntimeException {

    public UnsupportedDataSourceException(String message) {
        super(message);
    }

    public UnsupportedDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
