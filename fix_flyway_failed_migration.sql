-- 修复 Flyway 失败的迁移记录
-- 使用方法：在数据库中执行此脚本，然后重启应用

-- 方案1: 删除失败的迁移记录（如果还没有创建表）
-- DELETE FROM flyway_schema_history WHERE version = '1' AND success = 0;

-- 方案2: 如果表已经存在，将失败的记录标记为已修复（手动修复）
-- 注意：只有在确认表结构正确的情况下才使用此方法

-- 方案3: 清理所有 Flyway 历史（谨慎使用，会清除所有迁移记录）
-- DROP TABLE IF EXISTS flyway_schema_history;

-- 推荐方案：手动删除失败的记录并重新运行迁移
-- 执行以下 SQL 来查看当前状态
SELECT * FROM flyway_schema_history WHERE version = '1' ORDER BY installed_rank DESC;

-- 如果确认需要删除失败的记录，执行：
-- DELETE FROM flyway_schema_history WHERE version = '1' AND success = 0;

-- 如果表已经存在但记录失败，可以手动修复：
-- 1. 确保表结构正确（与 V1__Create_datasource_tables.sql 一致）
-- 2. 删除失败的记录
-- 3. 手动插入成功的记录（示例）：
/*
INSERT INTO flyway_schema_history (
    installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success
) VALUES (
    (SELECT COALESCE(MAX(installed_rank), 0) + 1 FROM flyway_schema_history),
    '1',
    'Create datasource tables',
    'SQL',
    'V1__Create_datasource_tables.sql',
    -1234567890,
    CURRENT_USER(),
    NOW(),
    100,
    1
);
*/

