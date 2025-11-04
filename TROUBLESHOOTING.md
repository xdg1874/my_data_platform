# 问题排查指南

## 常见问题及解决方案

### 1. Java版本兼容性问题

#### 问题描述
```
Set.of() 方法编译错误
找不到符号 Set.of
```

#### 解决方案
我们已经修复了Java版本兼容性问题，使用Java 8兼容的语法：

```java
// 修改前（Java 9+）
private static final Set<String> SENSITIVE_FIELDS = Set.of("password", "token", "secret", "key");

// 修改后（Java 8兼容）
private static final Set<String> SENSITIVE_FIELDS = new HashSet<>(Arrays.asList("password", "token", "secret", "key"));
```

### 2. Jackson注解问题

#### 问题描述
```
找不到 com.fasterxml.jackson.annotation.JsonTypeInfo 注解
```

#### 解决方案
我们已经移除了对Jackson多态序列化注解的依赖，改用更直接的配置解析方式。如果仍然遇到Jackson相关问题：

1. **检查依赖**：确保pom.xml中包含了完整的Jackson依赖
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
</dependency>
```

2. **清理重新编译**：
```bash
mvn clean compile -s settings.xml
```

3. **IDE刷新**：在IDE中刷新Maven项目

### 3. Maven依赖下载问题

#### 问题描述
```
Plugin org.springframework.boot:spring-boot-maven-plugin:2.7.18 could not be resolved
```

#### 解决方案
使用项目提供的Maven配置文件：

```bash
# 方法1：使用-s参数指定配置文件
mvn clean compile -s settings.xml

# 方法2：复制到Maven配置目录
# Windows
copy settings.xml %USERPROFILE%\.m2\settings.xml
# Linux/Mac
cp settings.xml ~/.m2/settings.xml

# 方法3：清理本地仓库缓存
mvn dependency:purge-local-repository -s settings.xml
```

### 4. 数据库连接问题

#### 问题描述
```
Communications link failure
```

#### 解决方案
1. **检查数据库配置**：
   - 确认数据库地址、端口、用户名、密码
   - 检查数据库是否启动
   - 确认网络连通性

2. **检查配置文件**：
```yaml
# application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/data_platform?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

3. **创建数据库**：
```sql
CREATE DATABASE data_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 5. Flyway迁移问题

#### 问题描述
```
Flyway migration failed
FlywayEditionUpgradeRequiredException: MariaDB 10.1 is no longer supported by Flyway Community Edition
```

#### 解决方案

**方案1：降级Flyway版本（推荐）**
我们已经在pom.xml中指定了兼容的Flyway版本：
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>8.5.13</version>
</dependency>
```

**方案2：使用无Flyway配置**
```bash
# 使用禁用Flyway的配置启动
mvn spring-boot:run -Dspring.profiles.active=dev-no-flyway -s settings.xml
```

**方案3：手动初始化数据库**
```bash
# 执行数据库初始化脚本
mysql -h 10.20.85.20 -P 3307 -u mysql -p < init-database.sql
```

**方案4：使用JPA自动建表**
修改配置文件：
```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # 改为update或create-drop
  flyway:
    enabled: false  # 禁用Flyway
```

**方案5：检查数据库权限**
确保用户有DDL权限：
```sql
GRANT ALL PRIVILEGES ON my_dp_1.* TO 'mysql'@'%';
FLUSH PRIVILEGES;
```

### 6. 端口占用问题

#### 问题描述
```
Port 8069 was already in use
```

#### 解决方案
1. **查找占用进程**：
```bash
# Windows
netstat -ano | findstr :8069
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8069
kill -9 <PID>
```

2. **修改端口**：
```yaml
# application-dev.yml
server:
  port: 8070  # 改为其他端口
```

### 7. IDE相关问题

#### 问题描述
IDE中显示编译错误，但实际代码正确

#### 解决方案
1. **刷新项目**：
   - IntelliJ IDEA: File → Reload Maven Projects
   - Eclipse: 右键项目 → Maven → Reload Projects

2. **清理IDE缓存**：
   - IntelliJ IDEA: File → Invalidate Caches and Restart
   - Eclipse: Project → Clean

3. **重新导入**：删除IDE配置文件，重新导入项目

### 8. 日志级别问题

#### 问题描述
看不到详细的错误日志

#### 解决方案
临时调整日志级别：
```yaml
# application-dev.yml
logging:
  level:
    com.example.mydataplatform: DEBUG
    org.springframework: DEBUG
    root: DEBUG
```

### 9. 内存不足问题

#### 问题描述
```
OutOfMemoryError: Java heap space
```

#### 解决方案
增加JVM内存：
```bash
# 启动时指定内存
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1024m -Xms512m"

# 或设置环境变量
export MAVEN_OPTS="-Xmx1024m -Xms512m"
```

## 调试技巧

### 1. 启用详细日志
```yaml
logging:
  level:
    com.example.mydataplatform: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

### 2. 使用Actuator监控
访问监控端点：
- 健康检查: `/actuator/health`
- 应用信息: `/actuator/info`
- 所有端点: `/actuator` (开发环境)

### 3. 数据库连接测试
使用API测试数据源连接：
```bash
curl -X POST http://localhost:8069/api/v1/datasources/1/test
```

### 4. 查看应用启动日志
注意启动过程中的错误信息，特别是：
- Bean创建失败
- 数据库连接失败
- 端口绑定失败

## 获取帮助

如果以上解决方案都无法解决问题：

1. **查看完整错误日志**：包括堆栈跟踪信息
2. **检查环境配置**：Java版本、Maven版本、数据库版本
3. **提供复现步骤**：详细描述操作步骤和错误现象
4. **查看相关文档**：Spring Boot、Flyway、Jackson等官方文档
