@echo off
echo ========================================
echo 数据清洗平台启动脚本
echo ========================================
echo.
echo 请选择启动方式:
echo 1. 标准启动 (使用Flyway数据库迁移)
echo 2. 无Flyway启动 (使用JPA自动建表)
echo 3. 手动初始化数据库后启动
echo 4. 编译检查
echo 5. 退出
echo.

set /p choice=请输入选项 (1-5): 

if "%choice%"=="1" goto standard
if "%choice%"=="2" goto no_flyway  
if "%choice%"=="3" goto manual_init
if "%choice%"=="4" goto compile_check
if "%choice%"=="5" goto exit
goto invalid

:standard
echo.
echo 正在使用标准配置启动...
mvn spring-boot:run -Dspring.profiles.active=dev -s settings.xml
goto end

:no_flyway
echo.
echo 正在使用无Flyway配置启动...
mvn spring-boot:run -Dspring.profiles.active=dev-no-flyway -s settings.xml
goto end

:manual_init
echo.
echo 请先手动执行数据库初始化:
echo mysql -h 10.20.85.20 -P 3307 -u mysql -p ^< init-database.sql
echo.
echo 执行完成后按任意键继续启动应用...
pause
mvn spring-boot:run -Dspring.profiles.active=dev-no-flyway -s settings.xml
goto end

:compile_check
echo.
echo 正在进行编译检查...
call compile-check.bat
goto end

:invalid
echo.
echo 无效选项，请重新选择
goto start

:exit
echo.
echo 退出脚本
goto end

:end
echo.
echo 脚本执行完成
pause
