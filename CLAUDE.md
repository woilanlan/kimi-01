# CLAUDE.md

This file provides **mandatory guidelines** for Claude Code (claude.ai/code) when working with code in this repository.

## 核心规则（必须遵守）

### 1. 语言

- 全部使用**中文**注释和文档

### 2. 服务层架构

```java
// 正确
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
```

### 3. REST API 响应

```java
// 正确
@GetMapping("/{id}")
public Result<UserDto> getUser(@PathVariable Long id) {
    return Result.success(dto);
}
```

### 4. 事务管理

```java
// 必须
@Transactional(rollbackFor = Exception.class)
public void createUser(UserCreateReq req) { /* ... */ }
```

### 5. 权限控制

```java
// 必须
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('user:query')")
public class UserController {
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:query')")
    public Result<UserDto> getUser(@PathVariable Long id) {
        // ...
    }
}
```

### 6. MyBatisPlus 规则

- **写入操作**：使用 `this.save()` / `this.updateById()`（触发自动填充）
- **单表查询**：使用 `this.getById()` / `this.list()` / `this.page()`
- **复杂查询**：使用 Mapper + XML

```java
// 正确
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public void createUser(UserCreateReq req) {
        this.save(convertToEntity(req)); // 触发自动填充
    }
    
    @Override
    public UserDto getUser(Long id) {
        return this.getById(id); // 单表查询
    }
    
    @Override
    public IPage<UserDto> searchUsers(Page<User> page, String keyword) {
        return userMapper.selectUserDetailPage(page, keyword); // 复杂查询
    }
}
```

## 项目结构

```
top.hxll.kimi/
├── controller/
├── dto/
│   ├── req/    # XxxReq.java
│   └── resp/   # XxxResp.java
├── entity/     # 实体类
├── mapper/     # Mapper接口
├── service/    # 服务接口
│   └── impl/   # 服务实现
├── security/   # 安全配置
└── common/
    ├── Result.java    # 统一响应
    └── exception/     # 自定义异常
```

## 代码生成检查清单

生成代码前确认：

- [ ] Service 使用接口+实现类模式
- [ ] Controller 返回 `Result<T>`
- [ ] 写操作添加 `@Transactional`
- [ ] 受保护接口添加 `@PreAuthorize`
- [ ] 写入使用 `this.save()` 而非 `mapper.insert()`
- [ ] 复杂查询使用 Mapper + XML

## 🔑 默认账号

默认账号：admin/123456（超级管理员）
