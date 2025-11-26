# 项目结构详解

> 本文档详细描述了 Kimi 项目的完整目录结构和各模块说明

## 目录总览

```
kimi/
├── src/
│   ├── main/
│   │   ├── java/top/hxll/kimi/          # Java 源代码
│   │   └── resources/                   # 配置文件和脚本
│   └── test/                            # 测试代码
├── pom.xml                              # Maven 配置文件
├── README.md                            # 项目说明文档
├── API.md                               # API接口文档
├── CLAUDE.md                            # Claude Code 开发指南
└── PROJECT-STRUCTURE.md                 # 本文档
```

---

## 源代码结构

### 主包：`top.hxll.kimi`

#### 1. 配置层 (`config/`)
```
config/
├── MyBatisPlusConfig.java              # MyBatis Plus 配置
│   - 分页插件配置
│   - SQL 注入器配置
│   - 乐观锁插件（预留）
│
└── CustomInfoContributor.java          # Spring Boot Actuator 自定义信息
    - 应用自定义健康信息
    - 版本信息展示
```

#### 2. 控制器层 (`controller/`)
```
controller/
├── HelloController.java                # 示例控制器（公共接口）
│   - 测试接口：/api/hello
│   - 回声接口：/api/echo
│   - 健康检查：/api/health
│
├── AuthController.java                 # 认证控制器
│   - 登录：/api/auth/login
│   - 注册：/api/auth/register
│   - 刷新令牌：/api/auth/refresh
│   - 获取用户信息：/api/auth/info
│   - 登出：/api/auth/logout
│   - 修改密码：/api/auth/change-password
│
├── UserController.java                 # 用户管理控制器（需认证）
│   - 用户查询：/api/admin/users
│   - 用户创建：/api/admin/users
│   - 用户更新：/api/admin/users/{id}
│   - 用户删除：/api/admin/users/{id}
│   - 批量删除：/api/admin/users/batch
│   - 启用/禁用：/api/admin/users/{id}/status
│   - 重置密码：/api/admin/users/{id}/reset-password
│   - 更新角色：/api/admin/users/{id}/roles
│   - 统计信息：/api/admin/users/stats
│
├── RoleController.java                 # 角色管理控制器
│   - 角色查询：/api/admin/roles
│   - 角色创建：/api/admin/roles
│   - 角色更新：/api/admin/roles/{id}
│   - 角色删除：/api/admin/roles/{id}
│   - 批量删除：/api/admin/roles/batch
│   - 启用/禁用：/api/admin/roles/{id}/status
│   - 更新权限：/api/admin/roles/{id}/permissions
│   - 获取用户角色：/api/admin/roles/user/{userId}
│   - 检查编码：/api/admin/roles/check-code
│   - 统计信息：/api/admin/roles/stats
│
└── PermissionController.java           # 权限管理控制器
    - 权限查询：/api/admin/permissions
    - 权限创建：/api/admin/permissions
    - 权限更新：/api/admin/permissions/{id}
    - 权限删除：/api/admin/permissions/{id}
    - 批量删除：/api/admin/permissions/batch
    - 启用/禁用：/api/admin/permissions/{id}/status
    - 权限树：/api/admin/permissions/tree
    - 父权限列表：/api/admin/permissions/parents
    - 检查编码：/api/admin/permissions/check-code
    - 统计信息：/api/admin/permissions/stats
```

#### 3. 数据传输对象 (`dto/`)
```
dto/
├── UserDto.java                        # 用户 DTO
├── RoleDto.java                        # 角色 DTO
├── PermissionDto.java                  # 权限 DTO
│
├── req/                                # 请求对象
│   ├── auth/
│   │   ├── LoginReq.java              # 登录请求
│   │   │   - username: String
│   │   │   - password: String
│   │   │
│   │   ├── RegisterReq.java           # 注册请求
│   │   │   - username: String
│   │   │   - password: String
│   │   │   - email: String
│   │   │
│   │   └── RefreshTokenReq.java       # 刷新令牌请求
│   │       - refreshToken: String
│   │
│   └── user/
│       ├── UserCreateReq.java         # 创建用户请求
│       ├── UserUpdateReq.java         # 更新用户请求
│       ├── PasswordChangeReq.java     # 修改密码请求
│       │   - oldPassword: String
│       │   - newPassword: String
│       │
│       └── PasswordResetReq.java      # 重置密码请求
│           - newPassword: String
│
└── resp/                                 # 响应对象
    └── JwtResp.java                      # JWT 响应
        - accessToken: String           # 访问令牌
        - refreshToken: String          # 刷新令牌
        - tokenType: String             # 令牌类型（Bearer）
        - expiresIn: Long               # 过期时间（秒）
```

#### 4. 实体层 (`entity/`)
```
entity/
├── BaseEntity.java                     # 基础实体（抽象类）
│   - id: Long                         # 主键ID
│   - createTime: LocalDateTime        # 创建时间
│   - updateTime: LocalDateTime        # 更新时间
│
├── User.java                           # 用户实体
│   - username: String                 # 用户名（唯一）
│   - password: String                 # 密码（BCrypt加密）
│   - nickname: String                 # 昵称
│   - email: String                    # 邮箱
│   - phone: String                    # 手机号
│   - status: Integer                  # 状态（0禁用 1启用）
│
├── Role.java                           # 角色实体
│   - roleCode: String                 # 角色编码
│   - roleName: String                 # 角色名称
│   - description: String              # 描述
│   - status: Integer                  # 状态
│   - sortOrder: Integer               # 排序
│
├── Permission.java                     # 权限实体
│   - parentId: Long                   # 父权限ID
│   - permissionCode: String           # 权限编码
│   - permissionName: String           # 权限名称
│   - permissionType: Integer          # 类型（1目录 2菜单 3操作）
│   - permissionDesc: String           # 描述
│   - status: Integer                  # 状态
│
├── UserRole.java                       # 用户-角色关联实体
│   - userId: Long                     # 用户ID
│   - roleId: Long                     # 角色ID
│
└── RolePermission.java                 # 角色-权限关联实体
    - roleId: Long                     # 角色ID
    - permissionId: Long               # 权限ID
```

#### 5. 数据访问层 (`mapper/`)
```
mapper/
├── UserMapper.java                     # 用户 Mapper
├── RoleMapper.java                     # 角色 Mapper
├── PermissionMapper.java               # 权限 Mapper
├── UserRoleMapper.java                 # 用户角色 Mapper
└── RolePermissionMapper.java           # 角色权限 Mapper

注意：所有 Mapper 继承 MyBatis Plus 的 BaseMapper<T>，无需手动编写基本 CRUD
```

#### 6. 安全模块 (`security/`)
```
security/
├── config/
│   └── SecurityConfig.java             # Spring Security 配置
│       - JWT 过滤器配置
│       - 权限拦截规则
│       - 认证/授权异常处理
│       - 密码加密配置（BCrypt）
│
├── filter/
│   └── JwtAuthenticationFilter.java    # JWT 认证过滤器
│       - Token 解析和验证
│       - 用户认证信息设置
│       - 认证失败处理
│
├── handler/
│   ├── JwtAuthenticationEntryPoint.java  # 认证失败处理器
│   │   - 未登录或 Token 无效处理
│   │   - 返回 401 状态码
│   │
│   └── JwtAccessDeniedHandler.java       # 权限不足处理器
│       - 权限不足处理
│       - 返回 403 状态码
│
├── service/
│   ├── UserDetailsServiceImpl.java     # 用户详情服务
│   │   - 加载用户信息
│   │   - 查询用户权限
│   │
│   └── UserDetailsImpl.java            # 用户详情实现
│       - 实现 UserDetails 接口
│       - 封装认证用户信息
│
└── util/
    └── JwtUtils.java                   # JWT 工具类
        - Token 生成
        - Token 解析
        - Token 验证
        - 密钥管理
```

#### 7. 服务层 (`service/`)
```
service/                                # 服务接口
├── UserService.java                    # 用户服务接口
│   - 继承 IService<User>
│   - 自定义用户管理方法
│
├── RoleService.java                    # 角色服务接口
│   - 继承 IService<Role>
│   - 自定义角色管理方法
│
└── PermissionService.java              # 权限服务接口
    - 继承 IService<Permission>
    - 自定义权限管理方法

service/impl/                           # 服务实现
├── UserServiceImpl.java                # 用户服务实现
│   - 实现 UserService 接口
│   - 继承 ServiceImpl<UserMapper, User>
│   - 事务管理 @Transactional
│
├── RoleServiceImpl.java                # 角色服务实现
│   - 实现 RoleService 接口
│   - 继承 ServiceImpl<RoleMapper, Role>
│   - 事务管理 @Transactional
│
└── PermissionServiceImpl.java          # 权限服务实现
    - 实现 PermissionService 接口
    - 继承 ServiceImpl<PermissionMapper, Permission>
    - 事务管理 @Transactional
```

#### 8. 公共组件 (`common/`)
```
common/
├── Result.java                          # 统一响应结果
│   - code: Integer                    # 业务状态码
│   - data: T                          # 响应数据
│   - msg: String                      # 提示消息
│   - timestamp: String                # 时间戳
│
├── PageReq.java                         # 分页请求
│   - page: Long                       # 页码（从1开始）
│   - size: Long                       # 每页条数
│
├── SortItem.java                        # 排序项
│   - column: String                   # 排序字段
│   - asc: Boolean                     # 是否升序
│
├── SortPageReq.java                     # 排序分页请求
│   - 继承 PageReq
│   - sorts: List<SortItem>            # 排序列表
│
├── UserContextUtils.java                # 用户上下文工具
│   - 获取当前登录用户
│   - 获取用户ID
│   - 获取用户名
│
└── exception/                          # 自定义异常包
    ├── GlobalExceptionHandler.java    # 全局异常处理器
    │   - 统一异常处理
    │   - 转换为 Result 响应
    │
    ├── BusinessException.java         # 业务异常（通用）
    ├── AuthException.java             # 认证异常（401）
    ├── TokenException.java            # Token 异常（401）
    ├── UserException.java             # 用户异常（400）
    └── PasswordException.java         # 密码异常（400）
```

---

## 资源文件结构

### `resources/` 目录
```
resources/
├── mapper/                              # MyBatis XML 映射文件
│   ├── UserMapper.xml                  # 用户 SQL 映射（自定义查询）
│   └── RoleMapper.xml                  # 角色 SQL 映射（自定义查询）
│
├── db/                                  # 数据库脚本
│   ├── setup-database.sql              # 数据库初始化脚本
│   │   - 创建表结构（5张表）
│   │   - 初始化权限数据
│   │   - 初始化角色数据
│   │   - 初始化用户数据
│   │   - 初始化关联关系
│   │
│   └── additional-test-data.sql        # 额外测试数据
│       - 更多测试用户
│       - 更多测试角色
│       - 额外权限配置
│
├── application.yml                      # 主配置文件
│   - Spring 基础配置
│   - 激活 dev 环境
│   - 默认配置项
│
├── application-dev.yml                  # 开发环境配置
│   - 数据库连接（开发）
│   - 日志级别：DEBUG
│   - 显示 SQL 日志
│   - Swagger 启用
│
└── application-prod.yml                 # 生产环境配置
    - 数据库连接（生产）
    - 日志级别：INFO
    - SQL 日志关闭
    - Swagger 禁用
```

---

## 测试代码结构

### `test/` 目录
```
test/java/top/hxll/kimi/
├── KimiApplicationTests.java           # 应用基础测试
│   - 应用上下文加载测试
│   - 配置验证
│
└── controller/                         # 控制器测试
    └── HelloControllerTest.java       # Hello 控制器测试
        - 测试公共接口
        - 测试健康检查
```

---

## 关键设计模式

### 1. 分层架构模式
- **表现层**: Controller 处理 HTTP 请求
- **业务层**: Service 处理业务逻辑（接口 + 实现）
- **持久层**: Mapper 负责数据访问
- **模型层**: Entity/DTO 定义数据模型

### 2. RBAC 权限模型
- **User**: 系统用户
- **Role**: 角色（权限集合）
- **Permission**: 权限（具体操作）
- **关联关系**: User-Role (N:N), Role-Permission (N:N)

### 3. 统一响应模式
- 所有接口返回 `Result<T>` 对象
- 统一状态码和消息格式
- 统一错误处理

### 4. 安全设计模式
- JWT 无状态认证
- Spring Security 过滤器链
- 注解式权限控制

---

## 文档索引

- **[README.md](../README.md)** - 项目概览和快速开始
- **[API.md](../API.md)** - 完整的 API 接口文档
- **[CLAUDE.md](../CLAUDE.md)** - Claude Code 开发指南
- **Swagger UI** - 运行时 API 文档（启动后访问）

---

## 最后更新

**最后更新**: 2025-01-01
**维护团队**: Kimi 开发团队
**文档版本**: v1.0
