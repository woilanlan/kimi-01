-- 额外的测试数据
USE kimi;

-- 插入更多权限数据
INSERT INTO sys_permission (permission_name, permission_code, parent_id, permission_type, path, method, description, sort_order) VALUES
('用户管理', 'user_manage', 1, 1, '/system/user', NULL, '用户管理菜单', 1),
('角色管理', 'role_manage', 1, 1, '/system/role', NULL, '角色管理菜单', 2),
('权限管理', 'permission_manage', 1, 1, '/system/permission', NULL, '权限管理菜单', 3),
('系统日志', 'system_log', 1, 1, '/system/log', NULL, '系统日志菜单', 4),

-- 用户管理相关权限
('用户查询', 'user_query', 2, 3, '/api/admin/users', 'GET', '查询用户权限', 1),
('用户新增', 'user_add', 2, 3, '/api/admin/users', 'POST', '新增用户权限', 2),
('用户修改', 'user_update', 2, 3, '/api/admin/users/*', 'PUT', '修改用户权限', 3),
('用户删除', 'user_delete', 2, 3, '/api/admin/users/*', 'DELETE', '删除用户权限', 4),
('用户状态', 'user_status', 2, 3, '/api/admin/users/*/status', 'PUT', '修改用户状态权限', 5),
('重置密码', 'user_reset_pwd', 2, 3, '/api/admin/users/*/reset-password', 'PUT', '重置用户密码权限', 6),

-- 角色管理相关权限
('角色查询', 'role_query', 3, 3, '/api/admin/roles', 'GET', '查询角色权限', 1),
('角色新增', 'role_add', 3, 3, '/api/admin/roles', 'POST', '新增角色权限', 2),
('角色修改', 'role_update', 3, 3, '/api/admin/roles/*', 'PUT', '修改角色权限', 3),
('角色删除', 'role_delete', 3, 3, '/api/admin/roles/*', 'DELETE', '删除角色权限', 4),
('角色状态', 'role_status', 3, 3, '/api/admin/roles/*/status', 'PUT', '修改角色状态权限', 5),
('角色权限', 'role_permission', 3, 3, '/api/admin/roles/*/permissions', 'PUT', '分配角色权限', 6),

-- 权限管理相关权限
('权限查询', 'permission_query', 4, 3, '/api/admin/permissions', 'GET', '查询权限权限', 1),
('权限新增', 'permission_add', 4, 3, '/api/admin/permissions', 'POST', '新增权限权限', 2),
('权限修改', 'permission_update', 4, 3, '/api/admin/permissions/*', 'PUT', '修改权限权限', 3),
('权限删除', 'permission_delete', 4, 3, '/api/admin/permissions/*', 'DELETE', '删除权限权限', 4),
('权限状态', 'permission_status', 4, 3, '/api/admin/permissions/*/status', 'PUT', '修改权限状态权限', 5),

-- 系统日志相关权限
('日志查询', 'log_query', 5, 3, '/api/admin/logs', 'GET', '查询日志权限', 1),
('日志删除', 'log_delete', 5, 3, '/api/admin/logs/*', 'DELETE', '删除日志权限', 2),

-- 个人信息相关权限
('个人信息', 'profile_info', 0, 1, '/profile', NULL, '个人信息菜单', 10),
('修改密码', 'profile_change_pwd', 32, 3, '/api/auth/change-password', 'POST', '修改个人密码权限', 1),
('更新信息', 'profile_update', 32, 3, '/api/auth/info', 'PUT', '更新个人信息权限', 2);

-- 插入更多角色数据
INSERT INTO sys_role (role_name, role_code, description, sort_order) VALUES
('系统管理员', 'system_admin', '系统管理员，拥有系统管理权限', 4),
('普通管理员', 'admin', '普通管理员，拥有大部分管理权限', 5),
('审计员', 'auditor', '审计员，拥有日志查看权限', 6),
('测试用户', 'test_user', '测试用户角色', 7);

-- 为超级管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE deleted = 0;

-- 为系统管理员分配系统管理权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 4, id FROM sys_permission
WHERE permission_code IN ('user_manage', 'role_manage', 'permission_manage', 'system_log')
   OR parent_id IN (2, 3, 4, 5) AND deleted = 0;

-- 为普通管理员分配用户和角色管理权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 5, id FROM sys_permission
WHERE permission_code IN ('user_manage', 'role_manage')
   OR parent_id IN (2, 3) AND deleted = 0;

-- 为审计员分配日志查看权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 6, id FROM sys_permission
WHERE permission_code IN ('system_log', 'log_query', 'log_delete')
   OR parent_id = 5 AND deleted = 0;

-- 为普通用户分配个人信息权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission
WHERE permission_code IN ('profile_info', 'profile_change_pwd', 'profile_update')
   OR parent_id = 32 AND deleted = 0;

-- 插入更多测试用户
INSERT INTO sys_user (username, password, email, phone, nickname, status) VALUES
('admin2', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'admin2@example.com', '13800138001', '系统管理员2', 1),
('auditor', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'auditor@example.com', '13800138002', '审计员', 1),
('user1', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'user1@example.com', '13900139001', '测试用户1', 1),
('user2', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'user2@example.com', '13900139002', '测试用户2', 1),
('disabled', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'disabled@example.com', '13900139003', '禁用用户', 0);

-- 为用户分配角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(3, 4),  -- admin2 -> 系统管理员
(4, 6),  -- auditor -> 审计员
(5, 2),  -- user1 -> 普通用户
(6, 2),  -- user2 -> 普通用户
(7, 2);  -- disabled -> 普通用户

-- 显示创建结果
SELECT '数据库和测试数据设置完成！' AS result;
SELECT CONCAT('权限: ', COUNT(*), ' 条记录') AS info FROM sys_permission WHERE deleted = 0;
SELECT CONCAT('角色: ', COUNT(*), ' 条记录') AS info FROM sys_role WHERE deleted = 0;
SELECT CONCAT('用户: ', COUNT(*), ' 条记录') AS info FROM sys_user WHERE deleted = 0;
SELECT CONCAT('用户角色关联: ', COUNT(*), ' 条记录') AS info FROM sys_user_role;
SELECT CONCAT('角色权限关联: ', COUNT(*), ' 条记录') AS info FROM sys_role_permission;