# Kimi - Spring Boot RBAC 权限管理系统

基于 Spring Boot 2.7.18 + Spring Security + JWT + MyBatis Plus 的 RESTful API 权限管理系统，支持 RBAC（基于角色的访问控制）模型。

## 功能特性

- 🔐 **JWT 认证授权** - 无状态认证机制
- 👥 **用户管理** - 完整的用户CRUD操作
- 🎭 **角色管理** - 角色权限分配
- 🔑 **权限管理** - 细粒度权限控制
- 📝 **系统日志** - 操作审计日志
- 🛡️ **安全防护** - XSS、SQL注入防护
- 📊 **监控管理** - Spring Boot Actuator

## 技术栈

- **后端框架**: Spring Boot 2.7.18
- **安全框架**: Spring Security + JWT
- **数据访问**: MyBatis Plus 3.5.3
- **数据库**: MySQL 8.0
- **连接池**: HikariCP
- **工具类**: Apache Commons Lang3, Lombok

## 📚 文档索引

- **[API.md](API.md)** - 完整的API接口文档（含认证、权限、数据模型）
- **[CLAUDE.md](CLAUDE.md)** - Claude Code 开发指南和项目规范

## 🚀 快速开始

### 1. 环境要求

- Java 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 克隆项目

```bash
git clone <repository-url>
cd kimi
```

### 3. 配置数据库

创建 MySQL 数据库：

```sql
CREATE DATABASE kimi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 修改配置

编辑 `src/main/resources/application-dev.yml` 文件，配置数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kimi?useSSL=false&serverTimezone=GMT%2B8
    username: your_username
    password: your_password
```

### 5. 运行项目

```bash
# 开发环境运行
mvn spring-boot:run -Dspring.profiles.active=dev

# 或打包后运行
mvn clean package
java -jar target/kimi-1.0.0.jar
```

### 6. 测试接口

项目启动后，访问以下地址：

- **Swagger UI**: http://localhost:8080/doc.html
- **健康检查**: http://localhost:8080/actuator/health
- **API文档**: http://localhost:8080/v3/api-docs

## 🔐 默认测试用户

| 用户名 | 密码 | 角色 | 权限说明 |
|--------|------|------|----------|
| admin | 123456 | 超级管理员 | 所有权限 |
| admin2 | 123456 | 系统管理员 | 用户/角色/权限管理 |
| auditor | 123456 | 审计员 | 日志查看权限 |
| test | 123456 | 普通用户 | 个人信息管理 |
| user1 | 123456 | 普通用户 | 个人信息管理 |
| user2 | 123456 | 普通用户 | 个人信息管理 |
| disabled | 123456 | 禁用用户 | 已禁用状态 |

## 📋 项目结构

```
src/
├── main/java/top/hxll/kimi/
│   ├── KimiApplication.java          # 主应用类
│   ├── controller/                   # REST控制器
│   ├── service/                      # 服务接口
│   ├── service/impl/                 # 服务实现
│   ├── mapper/                       # MyBatis Plus Mapper
│   ├── entity/                       # 实体类
│   ├── dto/                          # 数据传输对象
│   ├── security/                     # 安全配置
│   └── common/                       # 公共组件
└── resources/
    ├── application.yml               # 主配置
    ├── application-dev.yml          # 开发配置
    └── application-prod.yml         # 生产配置
```

## 🔧 开发规范

### 服务层架构

采用 **接口 + 实现类** 的标准分层结构：

```
service/
├── UserService.java          # 服务接口
├── RoleService.java          # 服务接口
├── PermissionService.java    # 服务接口
└── impl/
    ├── UserServiceImpl.java  # 服务实现
    ├── RoleServiceImpl.java  # 服务实现
    └── PermissionServiceImpl.java # 服务实现
```

### 核心注解

- **权限控制**: `@PreAuthorize("hasAuthority('user:query')")`
- **角色验证**: `@PreAuthorize("hasRole('ADMIN')")`
- **事务管理**: `@Transactional`
- **参数验证**: `@Valid`

## 📖 相关文档

- **[API.md](API.md)** - 详细的API接口文档和测试指南
- **[CLAUDE.md](CLAUDE.md)** - Claude Code 专用开发指南

## 📝 许可证

MIT License

## 👥 技术支持

如有问题，请提交 Issue 或联系开发团队。

---

**最后更新**: 2024-01-01