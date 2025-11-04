# 配置文件说明

## 配置文件结构

项目采用Spring Boot的多环境配置方式，包含以下配置文件：

### 1. application.yml (主配置文件)
包含所有环境共用的配置：
- 应用基本信息
- Jackson序列化配置
- JPA/Hibernate通用配置
- Flyway通用配置
- Actuator监控配置
- 日志格式配置

### 2. application-dev.yml (开发环境)
开发环境专用配置：
- **端口**: 8069
- **数据库**: 开发数据库连接信息
- **连接池**: 较小的连接池配置
- **日志级别**: DEBUG，包含SQL日志
- **开发工具**: 启用热重载
- **监控**: 暴露所有监控端点

### 3. application-prod.yml (生产环境)
生产环境专用配置：
- **端口**: 8080
- **数据库**: 支持环境变量配置
- **连接池**: 大容量连接池配置
- **日志级别**: INFO/WARN，关闭SQL日志
- **安全**: 不暴露敏感信息
- **监控**: 只暴露必要的监控端点
- **日志文件**: 配置日志轮转

### 4. application-test.yml (测试环境)
测试环境专用配置：
- **端口**: 随机端口
- **数据库**: H2内存数据库
- **JPA**: 自动建表模式
- **Flyway**: 禁用
- **H2控制台**: 启用

## 环境切换

### 1. 开发环境启动
```bash
# 默认使用dev环境（application.yml中配置）
mvn spring-boot:run

# 或显式指定
mvn spring-boot:run -Dspring.profiles.active=dev
```

### 2. 生产环境启动
```bash
# 使用环境变量
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-db-host
export DB_PORT=3306
export DB_NAME=data_platform
export DB_USERNAME=your-username
export DB_PASSWORD=your-password

mvn spring-boot:run

# 或使用参数
mvn spring-boot:run -Dspring.profiles.active=prod \
  -DDB_HOST=your-db-host \
  -DDB_USERNAME=your-username \
  -DDB_PASSWORD=your-password
```

### 3. 测试环境
```bash
mvn test -Dspring.profiles.active=test
```

## 生产环境环境变量

生产环境支持以下环境变量配置：

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| `DB_HOST` | localhost | 数据库主机地址 |
| `DB_PORT` | 3306 | 数据库端口 |
| `DB_NAME` | data_platform | 数据库名称 |
| `DB_USERNAME` | root | 数据库用户名 |
| `DB_PASSWORD` | password | 数据库密码 |

## 配置优先级

Spring Boot配置优先级（从高到低）：
1. 命令行参数
2. 环境变量
3. application-{profile}.yml
4. application.yml

## 日志配置

### 开发环境
- 控制台输出：DEBUG级别
- SQL日志：启用
- 文件：`logs/my-data-platform-dev.log`

### 生产环境
- 控制台输出：WARN级别
- SQL日志：禁用
- 文件：`logs/my-data-platform.log`
- 日志轮转：100MB/文件，保留30天

### 测试环境
- 控制台输出：DEBUG级别
- SQL日志：启用
- 无文件输出

## 监控端点

### 开发环境
- 暴露所有端点：`/actuator/*`
- 包含shutdown端点

### 生产环境
- 限制端点：`/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`
- 禁用shutdown端点
- 健康检查详情需要授权

### 测试环境
- 基本端点：`/actuator/health`, `/actuator/info`

## 安全注意事项

1. **敏感信息**: 生产环境使用环境变量，不在配置文件中硬编码
2. **错误信息**: 生产环境不暴露堆栈跟踪
3. **监控端点**: 生产环境限制暴露的端点
4. **SSL**: 生产环境数据库连接启用SSL

## Docker部署示例

```dockerfile
# Dockerfile
FROM openjdk:11-jre-slim

COPY target/my-data-platform-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```yaml
# docker-compose.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - DB_NAME=data_platform
      - DB_USERNAME=root
      - DB_PASSWORD=your-password
    depends_on:
      - mysql
  
  mysql:
    image: mysql:8.0
    environment:
      - MYSQL_ROOT_PASSWORD=your-password
      - MYSQL_DATABASE=data_platform
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```
