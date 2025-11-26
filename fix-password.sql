-- 修复admin用户密码为123456
USE kimi;

-- 更新admin用户密码为正确的BCrypt哈希值（123456）
UPDATE sys_user SET
    password = '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC',
    update_time = NOW()
WHERE username = 'admin';

-- 验证更新结果
SELECT username, password, status FROM sys_user WHERE username = 'admin';