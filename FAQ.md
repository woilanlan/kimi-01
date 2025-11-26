# 常见问题与解决方案

> 本文档汇总了项目开发和使用过程中遇到的常见问题及解决方案

---

## 🐛 启动运行问题

### Q1: 数据库连接失败

**现象**: 启动时报错 `Communications link failure` 或 `Connection refused`

**原因**:
- MySQL 服务未启动
- 数据库 `kimi` 未创建
- 数据库连接配置错误（URL、用户名、密码）
- MySQL 版本不兼容

**解决方案**:

```bash
# 1. 检查 MySQL 服务状态
systemctl status mysql        # Linux
net start mysql               # Windows

# 2. 创建数据库
mysql -u root -p -e "CREATE DATABASE kimi DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 3. 导入初始化数据
mysql -u root -p kimi < src/main/resources/db/setup-database.sql

# 4. 检查配置文件 application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/kimi?useSSL=false&serverTimezone=GMT%2B8
    username: your_username      # 修改为你的用户名
    password: your_password      # 修改为你的密码
    driver-class-name: com.mysql.cj.jdbc.Driver

# 5. 检查 MySQL 版本（需要 8.0+）
mysql --version
```

**验证**:
```bash
# 测试数据库连接
mysql -u your_username -p your_password -e "USE kimi; SELECT 1;"
```

---

## 🔐 认证授权问题

### Q2: JWT Token 过期或无效

**现象**: 接口返回 `401 Unauthorized` 或 `Token expired`

**原因**:
- accessToken 已过期（默认2小时）
- refreshToken 已过期（默认7天）
- Token 格式错误
- Token 被篡改
- 密钥配置错误

**解决方案**:

```bash
# 1. 使用 refresh_token 获取新的 access_token
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"your-refresh-token"}'

# 2. 如果 refresh_token 也过期，重新登录
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}'
```

**预防措施**:
- 前端应实现 Token 自动刷新机制
- 在 Token 即将过期（剩余5分钟）时刷新
- 存储 Token 时使用安全方式（HttpOnly Cookie 或加密存储）

### Q3: 权限不足（403 Forbidden）

**现象**: 接口返回 `403 Access Denied`

**原因**:
- 用户没有分配的权限
- 权限编码错误
- Spring Security 配置错误

**解决方案**:

1. **使用超级管理员测试**:
```bash
curl -X GET http://localhost:8080/api/admin/users \
  -H "Authorization: Bearer <admin-token>"
```

2. **检查用户权限**:
```bash
# 登录 admin 用户后查询
SELECT u.username, r.role_code, p.permission_code
FROM sys_user u
LEFT JOIN sys_user_role ur ON u.id = ur.user_id
LEFT JOIN sys_role r ON ur.role_id = r.id
LEFT JOIN sys_role_permission rp ON r.id = rp.role_id
LEFT JOIN sys_permission p ON rp.permission_id = p.id
WHERE u.username = 'test';
```

3. **检查权限注解**: 查看 Controller 方法上的权限配置
```java
@PreAuthorize("hasAuthority('user:query')")  // 需要此权限
```

4. **检查数据初始化**: 确认 `setup-database.sql` 已正确执行

---

## 🔍 数据访问问题

### Q4: SQL 不打印/日志不显示

**现象**: 控制台不输出 SQL 语句

**原因**:
- 日志级别不是 DEBUG
- MyBatis Plus 配置错误
- Logback 配置问题

**解决方案**:

```yaml
# application-dev.yml
logging:
  level:
    top.hxll.kimi: DEBUG              # 项目日志级别
    org.springframework.security: DEBUG  # Security 日志

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

**验证**:
访问任意查询接口，查看控制台是否输出 SQL

### Q5: 数据库字段不匹配

**现象**: 报错 `Unknown column` 或 `Column not found`

**原因**:
- 数据库表结构未更新
- 实体类字段与表字段不匹配
- 字段大小写问题（Linux 环境下）

**解决方案**:

```bash
# 1. 重新导入数据库脚本
mysql -u root -p kimi < src/main/resources/db/setup-database.sql

# 2. 检查实体类字段映射
@Column(name = "user_name")  // 指定列名
private String userName;

# 3. 检查表结构
DESC sys_user;
DESC sys_role;
DESC sys_permission;
```

---

## 🌐 接口访问问题

### Q6: Swagger 文档无法访问

**现象**: 访问 http://localhost:8080/doc.html 报错

**原因**:
- 项目启动失败
- 端口被占用
- 路径错误
- 安全配置拦截

**解决方案**:

```bash
# 1. 确认项目启动成功
# 查看日志：Started KimiApplication in x.x seconds

# 2. 检查端口占用
netstat -ano | findstr 8080    # Windows
lsof -i :8080                  # Linux/Mac

# 3. 使用正确路径
http://localhost:8080/doc.html    # 正确
http://localhost:8080/swagger-ui  # 错误

# 4. 检查 SecurityConfig
# /api/xxx, /actuator/**, /doc.html 应允许匿名访问
```

**替代方案**: 使用 Postman 导入 `http://localhost:8080/v3/api-docs`

### Q7: 接口返回 404 Not Found

**现象**: 接口访问返回 404

**原因**:
- URL 路径错误
- Controller 未被扫描
- 请求方法错误（GET/POST/PUT/DELETE）

**解决方案**:

```java
// 检查 Controller 配置
@RestController
@RequestMapping("/api/admin/users")  // 路径是否正确
@PreAuthorize("hasAuthority('user:query')")  // 权限是否配置
public class UserController {
    @GetMapping("/{id}")  // 方法是否匹配
    public Result getUser(@PathVariable Long id) {
        // ...
    }
}
```

**验证**:
```bash
# 查看所有接口映射
curl http://localhost:8080/actuator/mappings
```

---

## 🛠️ 开发与调试问题

### Q8: Lombok 注解不生效

**现象**: 编译错误，找不到 get/set 方法

**原因**:
- IDE 未安装 Lombok 插件
- Lombok 插件未启用
- 编译器配置错误

**解决方案**:

**IntelliJ IDEA**:
1. Settings → Plugins → 安装 Lombok Plugin
2. Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - ✅ Enable annotation processing
3. 重启 IDE

**Eclipse**:
1. 安装 Lombok: `java -jar lombok.jar`
2. 重启 Eclipse

**Maven 命令行**:
```bash
# 确保 pom.xml 中有 lombok 依赖
mvn clean compile
```

### Q9: Maven 依赖冲突

**现象**: 启动时报错 `NoSuchMethodError` 或 `ClassNotFoundException`

**原因**:
- 依赖版本冲突
- 传递依赖问题

**解决方案**:

```bash
# 1. 查看依赖树
mvn dependency:tree

# 2. 查找冲突
mvn dependency:tree -Dverbose | grep <conflict-package>

# 3. 排除冲突依赖（在 pom.xml 中）
<dependency>
    <groupId>xxx</groupId>
    <artifactId>xxx</artifactId>
    <exclusions>
        <exclusion>
            <groupId>conflict-group</groupId>
            <artifactId>conflict-artifact</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### Q10: Token 签名验证失败

**现象**: 报错 `JWT signature does not match`

**原因**:
- jwt.secret 配置错误
- Token 被篡改
- 密钥长度不足

**解决方案**:

```yaml
# application.yml
jwt:
  secret: your-secret-key-minimum-32-characters-long  # 至少32字符
```

**注意**: 生产环境必须修改默认密钥！

---

## 📊 性能与优化问题

### Q11: 分页查询性能慢

**现象**: 分页接口响应慢

**原因**:
- 未添加索引
- 查询数据量过大
- SQL 未优化

**解决方案**:

```sql
-- 为常用查询字段添加索引
ALTER TABLE sys_user ADD INDEX idx_username (username);
ALTER TABLE sys_user ADD INDEX idx_status (status);
ALTER TABLE sys_role ADD INDEX idx_role_code (role_code);
ALTER TABLE sys_permission ADD INDEX idx_parent_id (parent_id);
```

**代码优化**:
```java
// 只查询需要的字段
@Select("SELECT id, username, nickname FROM sys_user WHERE status = 1")
List<User> selectActiveUsers();
```

---

## 📝 最佳实践

### ✅ 配置文件管理
- 开发环境使用 `application-dev.yml`
- 生产环境使用 `application-prod.yml`
- 敏感信息（密码、密钥）使用环境变量或加密配置

### ✅ 代码规范
- 使用 Lombok 减少样板代码
- 采用接口+实现类的 Service 层结构
- 所有 REST 接口返回 `Result<T>` 对象
- 使用 `@Transactional` 管理事务
- 使用 `@PreAuthorize` 控制权限

### ✅ 安全保障
- JWT 密钥定期更换
- Token 设置合理过期时间
- 密码使用 BCrypt 加密存储
- SQL 注入防护（MyBatis Plus 自动处理）

### ✅ 测试建议
- 使用 admin 用户进行完整功能测试
- 测试权限控制是否正常
- 测试 Token 过期刷新流程
- 使用 Swagger UI 或 Postman 测试所有接口

---

## 📞 问题反馈

如果以上方案无法解决你的问题，请：

1. 检查日志文件（控制台输出）
2. 查看 [API.md](./API.md) 接口文档
3. 查看 [PROJECT-STRUCTURE.md](./PROJECT-STRUCTURE.md) 项目结构
4. 提交 Issue（附上错误日志和环境信息）

---

**最后更新**: 2025-01-01
**维护团队**: Kimi 开发团队
**文档版本**: v1.0
