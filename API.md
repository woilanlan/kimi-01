# API 接口文档

> 本文档详细描述了系统的所有 RESTful API 接口

## 📋 接口概览

- **公共接口**: 无需认证即可访问
- **认证接口**: 需要JWT Token认证
- **管理接口**: 需要特定权限认证

## 🔓 公共接口（无需认证）

### 基础接口

| 接口 | 方法 | 描述 | 请求参数 | 响应示例 |
|-----|------|------|----------|----------|
| `/api/hello` | GET | 基础问候接口 | 无 | `{ "code": 200, "data": "Hello Kimi!", "msg": "success" }` |
| `/api/hello/{name}` | GET | 带参数的问候接口 | `name`: String | `{ "code": 200, "data": "Hello {name}!", "msg": "success" }` |
| `/api/echo` | POST | POST示例接口 | `message`: String | `{ "code": 200, "data": "Echo: {message}", "msg": "success" }` |
| `/api/health` | GET | 健康检查接口 | 无 | `{ "code": 200, "data": "UP", "msg": "success" }` |

### 认证接口

| 接口 | 方法 | 描述 | 请求参数 | 响应示例 |
|-----|------|------|----------|----------|
| `/api/auth/login` | POST | 用户登录 | `username`: String<br>`password`: String | `{ "code": 200, "data": { "accessToken": "xxx", "refreshToken": "xxx" }, "msg": "登录成功" }` |
| `/api/auth/register` | POST | 用户注册 | `username`: String<br>`password`: String<br>`email`: String | `{ "code": 200, "data": { "id": 1, "username": "user" }, "msg": "注册成功" }` |
| `/api/auth/refresh` | POST | 刷新访问令牌 | `refreshToken`: String | `{ "code": 200, "data": { "accessToken": "xxx" }, "msg": "刷新成功" }` |

### 监控端点

| 接口 | 方法 | 描述 |
|-----|------|------|
| `/actuator/health` | GET | Spring Boot健康端点 |
| `/actuator/info` | GET | 应用信息端点 |

## 🔐 认证接口（需要JWT Token）

### 用户认证信息

| 接口 | 方法 | 描述 | 请求头 |
|-----|------|------|--------|
| `/api/auth/info` | GET | 获取当前用户信息 | `Authorization: Bearer <token>` |
| `/api/auth/logout` | POST | 用户登出 | `Authorization: Bearer <token>` |
| `/api/auth/change-password` | POST | 修改密码 | `Authorization: Bearer <token>`<br>请求体：`{ "oldPassword": "xxx", "newPassword": "xxx" }` |

## 👥 用户管理接口（需要权限：user:*）

### 用户查询

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/users` | GET | 获取用户列表（分页） | `page`: 页码(默认1)<br>`size`: 每页条数(默认10)<br>`keyword`: 搜索关键词 | `user:query` |
| `/api/admin/users/{id}` | GET | 获取用户详情 | `id`: 用户ID | `user:query` |
| `/api/admin/users/stats` | GET | 获取用户统计信息 | 无 | `user:query` |

### 用户管理

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/users` | POST | 创建用户 | 请求体：用户对象 | `user:add` |
| `/api/admin/users/{id}` | PUT | 更新用户信息 | `id`: 用户ID<br>请求体：用户对象 | `user:update` |
| `/api/admin/users/{id}` | DELETE | 删除用户 | `id`: 用户ID | `user:delete` |
| `/api/admin/users/batch` | DELETE | 批量删除用户 | 请求体：`[id1, id2, ...]` | `user:delete` |
| `/api/admin/users/{id}/status` | PUT | 启用/禁用用户 | `id`: 用户ID<br>`status`: 状态(0/1) | `user:update` |
| `/api/admin/users/{id}/reset-password` | PUT | 重置用户密码 | `id`: 用户ID<br>请求体：`{ "newPassword": "xxx" }` | `user:update` |
| `/api/admin/users/{id}/roles` | PUT | 更新用户角色 | `id`: 用户ID<br>请求体：`[roleId1, roleId2, ...]` | `user:update` |

## 🎭 角色管理接口（需要权限：role:*）

### 角色查询

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/roles/all` | GET | 获取所有角色列表 | 无 | `role:query` |
| `/api/admin/roles` | GET | 获取角色列表（分页） | `page`: 页码(默认1)<br>`size`: 每页条数(默认10)<br>`keyword`: 搜索关键词<br>`status`: 状态 | `role:query` |
| `/api/admin/roles/{id}` | GET | 获取角色详情 | `id`: 角色ID | `role:query` |
| `/api/admin/roles/code/{code}` | GET | 根据编码获取角色 | `code`: 角色编码 | `role:query` |
| `/api/admin/roles/user/{userId}` | GET | 获取用户角色列表 | `userId`: 用户ID | `role:query` |
| `/api/admin/roles/stats` | GET | 获取角色统计信息 | 无 | `role:query` |
| `/api/admin/roles/check-code` | GET | 检查角色编码是否存在 | `code`: 角色编码 | `role:query` |

### 角色管理

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/roles` | POST | 创建角色 | 请求体：角色对象 | `role:add` |
| `/api/admin/roles/{id}` | PUT | 更新角色 | `id`: 角色ID<br>请求体：角色对象 | `role:update` |
| `/api/admin/roles/{id}` | DELETE | 删除角色 | `id`: 角色ID | `role:delete` |
| `/api/admin/roles/batch` | DELETE | 批量删除角色 | 请求体：`[id1, id2, ...]` | `role:delete` |
| `/api/admin/roles/{id}/status` | PUT | 启用/禁用角色 | `id`: 角色ID<br>`status`: 状态(0/1) | `role:update` |

### 角色权限管理

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/roles/{id}/permissions` | GET | 获取角色权限列表 | `id`: 角色ID | `role:query` |
| `/api/admin/roles/{id}/permission-ids` | GET | 获取角色权限ID列表 | `id`: 角色ID | `role:query` |
| `/api/admin/roles/{id}/permissions` | PUT | 更新角色权限 | `id`: 角色ID<br>请求体：`[permissionId1, permissionId2, ...]` | `role:update` |

## 🔑 权限管理接口（需要权限：permission:*）

### 权限查询

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/permissions/tree` | GET | 获取权限树形结构 | 无 | `permission:query` |
| `/api/admin/permissions` | GET | 获取权限列表（分页） | `page`: 页码(默认1)<br>`size`: 每页条数(默认10)<br>`keyword`: 搜索关键词<br>`permissionType`: 权限类型 | `permission:query` |
| `/api/admin/permissions/{id}` | GET | 获取权限详情 | `id`: 权限ID | `permission:query` |
| `/api/admin/permissions/code/{code}` | GET | 根据编码获取权限 | `code`: 权限编码 | `permission:query` |
| `/api/admin/permissions/type/{type}` | GET | 根据权限类型获取权限列表 | `type`: 权限类型(1/2/3) | `permission:query` |
| `/api/admin/permissions/stats` | GET | 获取权限统计信息 | 无 | `permission:query` |
| `/api/admin/permissions/check-code` | GET | 检查权限编码是否存在 | `code`: 权限编码 | `permission:query` |
| `/api/admin/permissions/parents` | GET | 获取父权限列表 | 无 | `permission:query` |

### 权限管理

| 接口 | 方法 | 描述 | 请求参数 | 所需权限 |
|-----|------|------|----------|----------|
| `/api/admin/permissions` | POST | 创建权限 | 请求体：权限对象 | `permission:add` |
| `/api/admin/permissions/{id}` | PUT | 更新权限 | `id`: 权限ID<br>请求体：权限对象 | `permission:update` |
| `/api/admin/permissions/{id}` | DELETE | 删除权限 | `id`: 权限ID | `permission:delete` |
| `/api/admin/permissions/batch` | DELETE | 批量删除权限 | 请求体：`[id1, id2, ...]` | `permission:delete` |
| `/api/admin/permissions/{id}/status` | PUT | 启用/禁用权限 | `id`: 权限ID<br>`status`: 状态(0/1) | `permission:update` |

## 📊 数据模型

### 统一响应格式

```json
{
  "code": 200,
  "data": "响应数据",
  "msg": "操作成功",
  "timestamp": "2024-01-01T12:00:00"
}
```

### 分页响应格式

```json
{
  "code": 200,
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  },
  "msg": "success"
}
```

### 错误响应格式

```json
{
  "code": 400,
  "data": null,
  "msg": "错误信息"
}
```

## 🔐 认证机制

### JWT Token 格式

在请求头中添加认证信息：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTYxMjM0NTY3OH0.xxx
```

### Token 刷新流程

1. 访问令牌过期后，使用刷新令牌获取新的访问令牌
2. 刷新令牌有效期更长，可多次使用
3. 刷新令牌也过期后，需要重新登录

## 📋 权限编码规范

### 角色权限
- `role:query` - 查询角色
- `role:add` - 新增角色
- `role:update` - 更新角色
- `role:delete` - 删除角色

### 用户权限
- `user:query` - 查询用户
- `user:add` - 新增用户
- `user:update` - 更新用户
- `user:delete` - 删除用户

### 权限管理
- `permission:query` - 查询权限
- `permission:add` - 新增权限
- `permission:update` - 更新权限
- `permission:delete` - 删除权限

## 🚀 快速测试

### 使用 curl 测试

```bash
# 登录获取token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'

# 使用token访问受保护接口
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 使用 Swagger UI

访问 http://localhost:8080/doc.html 查看完整的 Swagger UI 文档

### 使用 Postman

导入 API 文档：http://localhost:8080/v3/api-docs

## 📖 相关文档

- [README.md](README.md) - 项目介绍和部署指南
- [CLAUDE.md](CLAUDE.md) - Claude Code 开发指南
- [数据库文档](DATABASE.md) - 数据库设计和表结构（如有）

## 📞 接口变更记录

| 版本 | 日期 | 变更内容 | 影响范围 |
|------|------|----------|----------|
| v1.0 | 2024-01-01 | 初始版本发布 | 无 |

---

**最后更新**: 2024-01-01
**维护团队**: Kimi 开发团队