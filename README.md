# 数据清洗平台

## 项目简介

这是一个基于Spring Boot的数据清洗平台后端项目，提供了完整的数据源管理功能，支持MySQL、ClickHouse、Kafka、Elasticsearch等多种数据源的连接、查询和写入操作。

## 功能特性

### 🚀 核心功能
- **多数据源支持**: MySQL、ClickHouse、Kafka、Elasticsearch
- **连接管理**: 连接测试、健康检查、连接池管理
- **元数据获取**: 数据库列表、表列表、表结构查询
- **数据操作**: 数据查询、预览、写入
- **安全性**: 敏感信息加密存储、参数验证

### 🛠 技术特性
- **可扩展架构**: 策略模式 + 工厂模式，易于扩展新数据源
- **数据库迁移**: 使用Flyway管理SQL脚本
- **健康监控**: 定时健康检查、操作日志记录
- **异常处理**: 全局异常处理、统一响应格式

## 快速开始

### 1. 环境要求
- Java 11+
- Maven 3.6+
- MySQL 8.0+

### 1.1 Maven配置（重要）
如果遇到Maven依赖下载问题，请使用项目根目录下的 `settings.xml` 文件：

```bash
# 使用项目提供的Maven配置
mvn clean compile -s settings.xml

# 或者将settings.xml复制到Maven配置目录
cp settings.xml ~/.m2/settings.xml  # Linux/Mac
copy settings.xml %USERPROFILE%\.m2\settings.xml  # Windows
```

### 2. 数据库准备
```sql
-- 创建数据库
CREATE DATABASE data_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'data_platform'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON data_platform.* TO 'data_platform'@'%';
FLUSH PRIVILEGES;
```

### 3. 配置数据库连接

项目支持多环境配置，根据不同环境选择配置方式：

#### 开发环境
编辑 `src/main/resources/application-dev.yml` 文件：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/data_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

#### 生产环境
使用环境变量配置（推荐）：
```bash
export DB_HOST=your-db-host
export DB_PORT=3306
export DB_NAME=data_platform
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
```

或编辑 `src/main/resources/application-prod.yml` 文件

### 4. 启动项目

#### 快速启动（推荐）
```bash
# Windows
start-app.bat

# Linux/Mac
chmod +x start-app.sh
./start-app.sh
```

#### 手动启动

**标准启动（使用Flyway）**
```bash
# 编译项目
mvn clean compile -s settings.xml

# 运行项目（默认使用dev环境）
mvn spring-boot:run -s settings.xml
```

**如果遇到Flyway问题，使用无Flyway启动**
```bash
# 使用JPA自动建表，禁用Flyway
mvn spring-boot:run -Dspring.profiles.active=dev-no-flyway -s settings.xml
```

#### 生产环境启动
```bash
# 使用环境变量
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run

# 或使用参数
mvn spring-boot:run -Dspring.profiles.active=prod
```

#### 测试环境启动
```bash
mvn test -Dspring.profiles.active=test
```

### 5. 验证启动
根据启动环境访问对应端口：
- **开发环境**: http://localhost:8069
- **生产环境**: http://localhost:8080

验证URL：
- 健康检查: `/actuator/health`
- 数据源类型: `/api/v1/datasources/types`

## API文档

### 数据源管理

#### 获取支持的数据源类型
```
GET /api/v1/datasources/types
```

#### 创建数据源
```
POST /api/v1/datasources
Content-Type: application/json

{
  "name": "MySQL测试库",
  "type": "mysql",
  "description": "MySQL测试数据源",
  "config": {
    "host": "localhost",
    "port": 3306,
    "database": "test",
    "username": "root",
    "password": "password"
  }
}
```

#### 测试数据源连接
```
POST /api/v1/datasources/{id}/test
```

#### 获取数据源列表
```
GET /api/v1/datasources?page=1&size=20&type=mysql&status=ENABLED&keyword=test
```

#### 获取数据库列表
```
GET /api/v1/datasources/{id}/databases
```

#### 获取表列表
```
GET /api/v1/datasources/{id}/tables?database=test
```

#### 查询数据
```
POST /api/v1/datasources/{id}/query
Content-Type: application/json

{
  "sql": "SELECT * FROM users LIMIT 10",
  "database": "test",
  "pageSize": 10,
  "timeout": 30000
}
```

#### 预览数据
```
POST /api/v1/datasources/{id}/preview?database=test&table=users&limit=10
```

## 数据源配置示例

### MySQL配置
```json
{
  "host": "localhost",
  "port": 3306,
  "database": "test",
  "username": "root",
  "password": "password",
  "charset": "utf8mb4",
  "connectTimeout": 30000,
  "socketTimeout": 60000,
  "useSSL": false
}
```

### ClickHouse配置
```json
{
  "host": "localhost",
  "port": 8123,
  "database": "default",
  "username": "default",
  "password": "",
  "connectTimeout": 30000,
  "socketTimeout": 60000,
  "compress": true
}
```

### Kafka配置
```json
{
  "bootstrapServers": "localhost:9092",
  "securityProtocol": "PLAINTEXT",
  "saslMechanism": "PLAIN",
  "additionalProperties": {
    "client.id": "data-platform"
  }
}
```

### Elasticsearch配置
```json
{
  "scheme": "http",
  "hosts": ["localhost:9200"],
  "username": "elastic",
  "password": "password",
  "connectTimeout": 30000,
  "socketTimeout": 60000
}
```

## 扩展新数据源

### 1. 创建配置类
```java
public class RedisConfig extends DataSourceConfig {
    private String database = "0";
    
    @Override
    public void validate() {
        Assert.hasText(host, "Redis主机地址不能为空");
        Assert.notNull(port, "Redis端口不能为空");
    }
    
    @Override
    public String getConnectionString() {
        return String.format("redis://%s:%d/%s", host, port, database);
    }
}
```

### 2. 实现连接器
```java
@DataSourceConnectorComponent(DataSourceType.REDIS)
public class RedisConnector implements DataSourceConnector {
    // 实现接口方法...
}
```

### 3. 更新枚举
```java
public enum DataSourceType {
    // ... 现有类型
    REDIS("redis", "Redis数据库");
}
```

## 项目结构

```
src/main/java/com/example/mydataplatform/
├── config/                 # 数据源配置类
├── connector/              # 数据源连接器
│   ├── annotation/         # 注解
│   ├── factory/           # 工厂类
│   └── impl/              # 连接器实现
├── controller/            # REST控制器
├── dto/                   # 数据传输对象
├── entity/                # 实体类
├── enums/                 # 枚举类
├── exception/             # 异常类
├── repository/            # 数据访问层
├── service/               # 业务服务层
└── util/                  # 工具类

src/main/resources/
├── db/migration/          # Flyway数据库迁移脚本
└── application.yml        # 应用配置
```

## 开发计划

- [x] 数据源管理模块
- [ ] 清洗规则配置模块  
- [ ] 清洗任务管理模块
- [ ] Flink任务集成
- [ ] 前端界面开发

## 许可证

MIT License