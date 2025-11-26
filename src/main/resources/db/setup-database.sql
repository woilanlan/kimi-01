-- MySQL 8.0 数据库和用户设置脚本
-- 运行此脚本需要root权限

-- 创建数据库
CREATE DATABASE IF NOT EXISTS kimi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权（如果用户已存在则修改密码）
CREATE USER IF NOT EXISTS 'kimi'@'%' IDENTIFIED BY '123456';
CREATE USER IF NOT EXISTS 'kimi'@'localhost' IDENTIFIED BY '123456';

-- 授权用户管理kimi数据库
GRANT ALL PRIVILEGES ON kimi.* TO 'kimi'@'%' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON kimi.* TO 'kimi'@'localhost' WITH GRANT OPTION;

-- 刷新权限
FLUSH PRIVILEGES;

-- 切换到kimi数据库
USE kimi;

-- 创建用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email),
    KEY idx_create_time (create_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 创建权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
    permission_name VARCHAR(50) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(50) NOT NULL COMMENT '权限编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_type TINYINT NOT NULL COMMENT '权限类型：1-菜单，2-按钮，3-接口',
    path VARCHAR(255) COMMENT '路径',
    method VARCHAR(10) COMMENT '请求方法（GET,POST,PUT,DELETE）',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description VARCHAR(255) COMMENT '权限描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 创建登录日志表
CREATE TABLE IF NOT EXISTS sys_login_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    login_ip VARCHAR(50) COMMENT '登录IP',
    login_location VARCHAR(100) COMMENT '登录地点',
    user_agent TEXT COMMENT '用户代理',
    login_status TINYINT DEFAULT 1 COMMENT '登录状态：1-成功，0-失败',
    error_message VARCHAR(255) COMMENT '错误信息',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 创建操作日志表
CREATE TABLE IF NOT EXISTS sys_operation_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation_module VARCHAR(50) COMMENT '操作模块',
    operation_type VARCHAR(50) COMMENT '操作类型',
    operation_desc TEXT COMMENT '操作描述',
    request_method VARCHAR(10) COMMENT '请求方法',
    request_url VARCHAR(255) COMMENT '请求URL',
    request_param TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    operation_ip VARCHAR(50) COMMENT '操作IP',
    operation_location VARCHAR(100) COMMENT '操作地点',
    operation_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    cost_time BIGINT COMMENT '耗时（毫秒）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-成功，0-失败',
    error_message TEXT COMMENT '错误信息',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_operation_time (operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 插入初始数据

-- 插入初始权限数据
INSERT INTO sys_permission (permission_name, permission_code, parent_id, permission_type, path, method, description) VALUES
('系统管理', 'system', 0, 1, '/system', NULL, '系统管理菜单'),
('用户管理', 'user_manage', 1, 1, '/system/user', NULL, '用户管理菜单'),
('角色管理', 'role_manage', 1, 1, '/system/role', NULL, '角色管理菜单'),
('权限管理', 'permission_manage', 1, 1, '/system/permission', NULL, '权限管理菜单'),
('用户查询', 'user_query', 2, 3, '/api/users', 'GET', '查询用户权限'),
('用户新增', 'user_add', 2, 3, '/api/users', 'POST', '新增用户权限'),
('用户修改', 'user_update', 2, 3, '/api/users/*', 'PUT', '修改用户权限'),
('用户删除', 'user_delete', 2, 3, '/api/users/*', 'DELETE', '删除用户权限'),
('角色查询', 'role_query', 3, 3, '/api/roles', 'GET', '查询角色权限'),
('角色新增', 'role_add', 3, 3, '/api/roles', 'POST', '新增角色权限'),
('角色修改', 'role_update', 3, 3, '/api/roles/*', 'PUT', '修改角色权限'),
('角色删除', 'role_delete', 3, 3, '/api/roles/*', 'DELETE', '删除角色权限');

-- 插入初始角色数据
INSERT INTO sys_role (role_name, role_code, description, sort_order) VALUES
('超级管理员', 'admin', '系统超级管理员，拥有所有权限', 1),
('普通用户', 'user', '普通用户角色', 2),
('访客用户', 'guest', '访客用户角色，只有查看权限', 3);

-- 为超级管理员分配所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission;

-- 插入初始用户数据（密码为 123456，已加密）
INSERT INTO sys_user (username, password, email, phone, nickname, status) VALUES
('admin', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'admin@example.com', '13800138000', '超级管理员', 1),
('test', '$2a$10$ahbTvr7ULUr5rz9m1CbAV.mL1JgsmkNE9EG/xTaLcKaza4urASrIC', 'test@example.com', '13900139000', '测试用户', 1);

-- 为用户分配角色
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),  -- admin 用户分配超级管理员角色
(2, 2);  -- test 用户分配普通用户角色

-- 显示创建结果
SELECT '数据库和用户设置完成！' AS result;
SELECT CONCAT('数据库: ', COUNT(*) , ' 个表') AS info FROM information_schema.tables WHERE table_schema = 'kimi';
SELECT CONCAT('权限: ', COUNT(*), ' 条记录') AS info FROM sys_permission;
SELECT CONCAT('角色: ', COUNT(*), ' 条记录') AS info FROM sys_role;
SELECT CONCAT('用户: ', COUNT(*), ' 条记录') AS info FROM sys_user;