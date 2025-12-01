# CLAUDE.md

This file provides **mandatory guidelines** for Claude Code (claude.ai/code) when working with code in this repository.

## 🚨 MANDATORY CONSTRAINTS (必须遵守)

### 1. Language
- **Respond in Chinese** (使用中文回答所有问题)

### 2. Service Layer Architecture (强制)
**MUST follow Interface + Implementation pattern:**

```java
// ✓ CORRECT
public interface UserService extends IService<User> {
    void createUser(UserCreateReq req);
}

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    public void createUser(UserCreateReq req) {
        // implementation
    }
}

// ✗ WRONG
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    // ❌ Missing interface
}
```

### 3. REST API Response Format (强制)
**ALL endpoints must return Result<T>:**

```java
@RestController
@RequestMapping("/api/admin/users")
public class UserController {
    // ✓ CORRECT
    @GetMapping("/{id}")
    public Result<UserDto> getUser(@PathVariable Long id) {
        return Result.success(dto);
    }

    @PostMapping
    public Result<Void> createUser(@Valid @RequestBody UserCreateReq req) {
        userService.createUser(req);
        return Result.success();
    }

    // ✗ WRONG
    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable Long id) {  // ❌ Missing Result wrapper
        return dto;
    }
}
```

**Status codes**: 200=success, 400=business error, 401=unauthorized, 403=forbidden, 500=system error

**Result format**:
```json
{
  "code": 200,
  "data": {},
  "msg": "success"
}
```

### 4. Transaction Management (强制)
**ALL write operations MUST use @Transactional:**

```java
// ✓ CORRECT
@Transactional(rollbackFor = Exception.class)
public void createUser(UserCreateReq req) {
    // multiple DB operations
}

// ✗ WRONG
public void createUser(UserCreateReq req) {  // ❌ No transaction
    // risk of inconsistency
}
```

### 5. Permission Control (强制)
**ALWAYS use @PreAuthorize for secured endpoints:**

```java
// ✓ CORRECT
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('user:query')")  // Class level
public class UserController {

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:query')")  // Method level
    public Result<UserDto> getUser(@PathVariable Long id) {
        // ...
    }
}

// ✗ WRONG
@GetMapping("/{id}")
public Result<UserDto> getUser(@PathVariable Long id) {  // ❌ No security
    // security vulnerability
}
```

### 6. Service Layer Method Calls (强制)
**Service 层必须使用 this 调用写入方法，利用框架自动填充：**

```java
// ✓ CORRECT
public interface UserService extends IService<User> {
    void createUser(UserCreateReq req);
}

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @Transactional
    public void createUser(UserCreateReq req) {
        // ✅ 使用 this 调用触发自动填充（create_time, create_by）
        this.save(convertToEntity(req));
    }

    @Override
    @Transactional
    public boolean updateUser(Long id, UserUpdateReq req) {
        User user = userMapper.selectById(id);  // 查询可用 Mapper
        user.setName(req.getName());

        // ✅ 更新必须用 this 触发自动填充（update_time, update_by）
        return this.updateById(user);
    }
}

// ✗ WRONG
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @Transactional
    public void createUser(UserCreateReq req) {
        // ❌ 绕过自动填充
        userMapper.insert(entity);
    }
}
```

**规则**：查询可用 Mapper，写入必须用 `this`（触发自动填充机制）

## 📦 PROJECT STRUCTURE

```
top.hxll.kimi/
├── config/                      # Configuration classes
├── controller/                  # REST controllers
│   ├── HelloController.java    # Public endpoints
│   ├── AuthController.java     # Authentication
│   └── UserController.java     # Requires authorization
├── dto/
│   ├── req/                    # Request objects (XxxReq.java)
│   └── resp/                   # Response objects (XxxResp.java)
├── entity/                      # Entity classes (extends BaseEntity)
├── mapper/                      # MyBatis Mappers (extends BaseMapper)
├── security/
│   ├── config/SecurityConfig.java
│   ├── filter/JwtAuthenticationFilter.java
│   ├── handler/
│   ├── service/
│   └── util/JwtUtils.java
├── service/                     # Service interfaces (extends IService)
│   └── impl/                   # Implementations (extends ServiceImpl)
└── common/
    ├── Result.java              # Unified response
    ├── PageReq.java
    └── exception/               # Custom exceptions
```

## ⚡ QUICK COMMANDS

```bash
# Run
mvn spring-boot:run -Dspring.profiles.active=dev

# Package
mvn clean package

# Test
mvn test

# Skip tests
mvn clean package -DskipTests
```

## 🎯 QUICK REFERENCE

| 项目 | 说明                                           | 示例 |
|------|----------------------------------------------|------|
| **Security** | `@PreAuthorize("hasAuthority('user:query')")` | Class/Method |
| **Transaction** | `@Transactional` on write ops                | Service method |
| **JWT Header** | `Authorization: Bearer <token>`              | - |
| **Permission** | 格式: `资源:操作`                                  | `user:query`, `role:add` |
| **DTO Naming** | Req=请求, Resp=响应, Dto=数据                      | `UserCreateReq` |
| **MyBatis Plus** | 查询用 Mapper, 写入用 `this`                       | `userMapper.selectById()`<br>`this.save()` |

## ✅ CODE CHECKLIST

### Before Coding
- [ ] Database created and initialized
- [ ] Configuration in `application-dev.yml` verified
- [ ] Service uses interface + implementation pattern

### When Writing Code
- [ ] REST endpoint returns `Result<T>`
- [ ] `@Transactional` added to write operations
- [ ] `@PreAuthorize` added for security
- [ ] `@Slf4j` and proper logging used
- [ ] DTO naming follows convention
- [ ] Validation with `@Valid` where needed
- [ ] **Service 层写入操作使用 `this` 调用**（触发自动填充）

### Before Submitting
- [ ] Security annotations present
- [ ] Transaction boundaries correct
- [ ] Response format validated
- [ ] Logging is appropriate

## 🆘 TROUBLESHOOTING

| Problem | Solution |
|---------|----------|
| Database connection fails | Check MySQL service, database exists |
| 401 Unauthorized | Token expired or missing, re-login or refresh |
| 403 Forbidden | Check `@PreAuthorize`, verify user authority |
| 404 Not Found | Check URL mapping, Controller location |
| SQL not logged | Set logging to DEBUG in `application-dev.yml` |
| Lombok not working | Install plugin, enable annotation processing |

For detailed solutions, see [FAQ.md](./FAQ.md)

## 🔗 DOCUMENTATION

- **[README.md](./README.md)** - Project overview and deployment
- **[API.md](./API.md)** - Complete API documentation
- **[PROJECT-STRUCTURE.md](./PROJECT-STRUCTURE.md)** - Detailed project structure
- **[FAQ.md](./FAQ.md)** - Common issues and solutions

## 🔑 DEFAULT CREDENTIALS

```
Username: admin
Password: 123456
Role: Super Admin (has all permissions)
```
