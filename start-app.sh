#!/bin/bash

echo "========================================"
echo "数据清洗平台启动脚本"
echo "========================================"
echo ""
echo "请选择启动方式:"
echo "1. 标准启动 (使用Flyway数据库迁移)"
echo "2. 无Flyway启动 (使用JPA自动建表)"
echo "3. 手动初始化数据库后启动"
echo "4. 编译检查"
echo "5. 退出"
echo ""

read -p "请输入选项 (1-5): " choice

case $choice in
    1)
        echo ""
        echo "正在使用标准配置启动..."
        mvn spring-boot:run -Dspring.profiles.active=dev -s settings.xml
        ;;
    2)
        echo ""
        echo "正在使用无Flyway配置启动..."
        mvn spring-boot:run -Dspring.profiles.active=dev-no-flyway -s settings.xml
        ;;
    3)
        echo ""
        echo "请先手动执行数据库初始化:"
        echo "mysql -h 10.20.85.20 -P 3307 -u mysql -p < init-database.sql"
        echo ""
        read -p "执行完成后按回车键继续启动应用..."
        mvn spring-boot:run -Dspring.profiles.active=dev-no-flyway -s settings.xml
        ;;
    4)
        echo ""
        echo "正在进行编译检查..."
        ./compile-check.sh
        ;;
    5)
        echo ""
        echo "退出脚本"
        exit 0
        ;;
    *)
        echo ""
        echo "无效选项，请重新运行脚本"
        exit 1
        ;;
esac

echo ""
echo "脚本执行完成"
