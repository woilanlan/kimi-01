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
// ✓ CORRECT
public Result<UserDto> getUser(Long id) {
    return Result.success(dto);
}

public Result<Void> deleteUser(Long id) {
    userService.removeById(id);
    return Result.success();
}

// ✗ WRONG
public UserDto getUser(Long id) {  // ❌ Missing Result wrapper
    return dto;
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

### Security Annotations
| Annotation | Usage | Example |
|------------|-------|---------|
| `@PreAuthorize("hasRole('ADMIN')")` | Role check | Class/Method |
| `@PreAuthorize("hasAuthority('user:query')")` | Permission check | Method |
| `@Transactional` | Write operations | Service method |
| `@Valid` | Parameter validation | Controller param |
| `@Slf4j` | Logging | Class |

### Permission Format
```
格式: 资源:操作
user:query, user:add, user:update, user:delete
role:query, role:add, role:update, role:delete
permission:query, permission:add, permission:update, permission:delete
```

### Result<T> Response
```json
{
  "code": 200,
  "data": {},
  "msg": "success"
}
```

**Status codes**: 200=success, 400=business error, 401=unauthorized, 403=forbidden, 500=system error

### DTO Naming
- Request: `XxxReq.java` (e.g., UserCreateReq)
- Response: `XxxResp.java` (e.g., JwtResp)
- Data: `XxxDto.java` (e.g., UserDto)

### JWT Headers
```
Authorization: Bearer <accessToken>
```

### Common Operations (MyBatis Plus)
```java
// Query
userMapper.selectById(id);
userMapper.selectList(wrapper);
userMapper.selectPage(page, wrapper);

// Write
userMapper.insert(entity);
userMapper.updateById(entity);
userMapper.deleteById(id);

// Wrapper
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
wrapper.eq(User::getUsername, "admin")
       .like(User::getNickname, "张");
```

### Controller Template
```java
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:query')")
    public Result<Page<UserDto>> listUsers(SortPageReq req) {
        return Result.success(userService.listUsers(req));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createUser(@Valid @RequestBody UserCreateReq req) {
        userService.createUser(req);
        return Result.success();
    }
}
```

### Service Template
```java
public interface UserService extends IService<User> {
    void createUser(UserCreateReq req);
    UserDto getUserById(Long id);
}

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateReq req) {
        log.info("【UserService】创建用户：{}", req.getUsername());
        save(convertToEntity(req));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new UserException("用户不存在");
        }
        return convertToDto(user);
    }
}
```

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
