package top.hxll.kimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.xml.internal.bind.v2.TODO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import top.hxll.kimi.dto.*;
import top.hxll.kimi.dto.req.auth.RegisterReq;
import top.hxll.kimi.dto.req.user.PasswordChangeReq;
import top.hxll.kimi.dto.req.user.UserCreateReq;
import top.hxll.kimi.dto.req.user.UserUpdateReq;
import top.hxll.kimi.entity.Role;
import top.hxll.kimi.entity.User;
import top.hxll.kimi.entity.UserRole;
import top.hxll.kimi.common.UserContextUtils;
import top.hxll.kimi.common.exception.UserException;
import top.hxll.kimi.common.exception.PasswordException;
import top.hxll.kimi.mapper.RoleMapper;
import top.hxll.kimi.mapper.UserMapper;
import top.hxll.kimi.mapper.UserRoleMapper;
import top.hxll.kimi.security.service.UserDetailsImpl;
import top.hxll.kimi.service.RoleService;
import top.hxll.kimi.service.UserRoleService;
import top.hxll.kimi.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 默认密码（重置密码时使用）
     */
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final RoleService roleService;
    private final UserRoleService userRoleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(RegisterReq registerReq) {
        log.info("Registering new user: {}", registerReq.getUsername());

        // 验证密码是否一致
        if (!registerReq.isPasswordMatch()) {
            throw new PasswordException("两次输入的密码不一致");
        }

        // 查询所有可能冲突的用户信息（数据量不大）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, registerReq.getUsername())
                .or(qw -> qw.eq(User::getEmail, registerReq.getEmail()))
                .or(qw -> qw.eq(User::getPhone, registerReq.getPhone()));
        List<User> existingUsers = userMapper.selectList(queryWrapper);

        for (User existingUser : existingUsers) {
            if (registerReq.getUsername().equals(existingUser.getUsername())) {
                throw new UserException("用户名已存在");
            }
            if (Objects.equals(registerReq.getEmail(), existingUser.getEmail())) {
                throw new UserException("邮箱已存在");
            }
            if (Objects.equals(registerReq.getPhone(), existingUser.getPhone())) {
                throw new UserException("手机号已存在");
            }
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(registerReq.getUsername());
        user.setPassword(passwordEncoder.encode(registerReq.getPassword()));
        user.setEmail(registerReq.getEmail());
        user.setPhone(registerReq.getPhone());
        user.setNickname(registerReq.getNickname());
        user.setStatus(1); // 默认启用
        user.setAvatar("/default-avatar.png"); // 默认头像

        // 保存用户
        this.save(user);

        // 分配默认角色
        String roleCode = registerReq.getRoleCode() != null ? registerReq.getRoleCode() : "user";

        LambdaQueryWrapper<Role> qw1 = new LambdaQueryWrapper<Role>().eq(Role::getRoleCode, roleCode);
        // 等价于 getOne(queryWrapper, false)，即默认不抛出异常，返回null
        // 查询到多条记录时，返回第一条记录
        Role role = roleService.getOne(qw1);
        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRole.setCreateBy(user.getId());     //手动设置了字段值，自动填充通常不会覆盖已设置的值
            userRoleService.save(userRole);
            log.info("Assigned role {} to user {}", roleCode, user.getUsername());
        }

        log.info("User registered successfully: {}", user.getUsername());
        return user;
    }

    @Override
    @Transactional
    public boolean changePassword(Long userId, PasswordChangeReq passwordChangeReq) {
        log.info("Changing password for user: {}", userId);

        User exUser  = getUserByIdOrThrow(userId);
        // 验证旧密码
        if (!passwordEncoder.matches(passwordChangeReq.getOldPassword(), exUser.getPassword())) {
            throw new PasswordException("旧密码不正确");
        }

        // 验证新密码是否一致（核心业务安全保障）
        if (!passwordChangeReq.isNewPasswordMatch()) {
            throw new PasswordException("两次输入的新密码不一致");
        }

        // 构造最小更新对象
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(passwordChangeReq.getNewPassword()));

        // 执行更新（自动填充 update_time / update_by）
        boolean result = this.updateById(user);
        log.info("Password changed successfully for user: {}", userId);
        return result;
    }

    @Override
    @Transactional
    public boolean resetPassword(Long userId) {
        log.info("Resetting password for user: {}", userId);

        User exUser = getUserByIdOrThrow(userId);

        // 重置为默认密码
        User user = new User();
        user.setId(userId);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        log.info("Password will be reset to default: '{}'", DEFAULT_PASSWORD);

        boolean result = this.updateById(user);
        log.info("Password reset successfully for user: {}", userId);
        return result;
    }

    @Override
    public UserDto getUserWithRolesAndPermissions(Long userId) {
        User user = getUserByIdOrThrow(userId);
        return convertToDtoWithRolesAndPermissions(user);
    }


    @Override
    public IPage<UserDto> getUserPage(Page<User> page, String keyword, Integer status) {
        // 使用 LambdaQueryWrapper 替代 QueryWrapper，提高类型安全性
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(wq -> wq.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword));
        }

        // 状态筛选条件
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        // 使用 ServiceImpl 提供的 page 方法替代自定义 Mapper 方法
        IPage<User> userPage = this.page(page, wrapper);

        // 转换为 DTO 分页结果
        IPage<UserDto> dtoPage = userPage.convert(user -> {
            UserDto dto = new UserDto();
            BeanUtils.copyProperties(user, dto);
            return dto;
        });

        return dtoPage;
    }

    @Override
    @Transactional
    public boolean updateUserInfo(Long userId, UserUpdateReq updateRequest) {
        User exUser = getUserByIdOrThrow(userId);

        // 更新用户信息
        User user = new User();
        user.setId(userId);

        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }
        if (updateRequest.getNickname() != null) {
            user.setNickname(updateRequest.getNickname());
        }
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar());
        }
        if (updateRequest.getStatus() != null) {
            user.setStatus(updateRequest.getStatus());
        }
        // 执行更新（自动填充 update_time / update_by）
        boolean result = this.updateById(user);

        // 更新角色关联
        if (updateRequest.getRoleIds() != null) {
            updateUserRoles(userId, Arrays.asList(updateRequest.getRoleIds()));
        }
        return result;
    }

    @Override
    @Transactional
    public boolean deleteUser(Long userId) {
        // 验证用户存在
        getUserByIdOrThrow(userId);

        // 逻辑删除 - 触发自动填充 update_time 和 update_by
        boolean result = this.removeById(userId);

        // 删除用户角色关联(不触发自动填充)
        LambdaQueryWrapper<UserRole> qw = new LambdaQueryWrapper<>();
        qw.eq(UserRole::getUserId, userId);
        userRoleService.remove(qw);

        log.info("User deleted logically: {}", userId);
        return result;
    }

    @Override
    @Transactional
    public boolean updateUserRoles(Long userId, List<Long> roleIds) {
        // todo: 待优化
        // 删除原有角色关联
        userRoleService.deleteByUserId(userId);

        // 添加新角色关联
        if (!CollectionUtils.isEmpty(roleIds)) {
            // 校验角色是否存在
            Set<Long> validRoleIds = roleService.getValidRoleIds(roleIds);

            if (!validRoleIds.isEmpty()) {
                // 直接使用有效角色ID创建UserRole对象
                List<UserRole> userRoles = validRoleIds.stream()
                        .map(roleId -> {
                            UserRole userRole = new UserRole();
                            userRole.setUserId(userId);
                            userRole.setRoleId(roleId);
                            userRole.setCreateBy(UserContextUtils.getCurrentUserId());
                            return userRole;
                        })
                        .collect(Collectors.toList());

                // 使用 MyBatis-Plus 的 saveBatch 方法
                // TODO: 测试使用框架自动注入
                boolean success = userRoleService.saveBatch(userRoles);
                if (!success) {
                    throw new RuntimeException("Failed to insert user-role relations");
                }
            }
        }
        log.info("Successfully updated roles for user {}, roles: {}", userId, roleIds);
        return true;
    }


    @Override
    @Transactional
    public User createUser(UserCreateReq req) {
        log.info("Creating user by admin: {}", req.getUsername());

        // todo: 用户名，邮箱，手机号，都不能重复
        LambdaQueryWrapper<User> qw = new LambdaQueryWrapper<>();
        qw.eq(User::getUsername, req.getUsername());
        if (this.count(qw) > 0) {
            throw new UserException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setNickname(req.getNickname());
        user.setStatus(req.getStatus() != null ? req.getStatus() : 1);
        user.setAvatar("/default-avatar.png");

        this.save(user);
        // 分配角色
        if (req.getRoleIds() != null && req.getRoleIds().length > 0) {
            List<Long> roleIds = Arrays.asList(req.getRoleIds());
            updateUserRoles(user.getId(), roleIds);
        }
        log.info("User created by admin: {}", user.getUsername());

        // 让调用方立即获得创建成功的用户完整信息,符合 RESTful API 的实践
        return user;
    }

    @Override
    @Transactional
    public boolean toggleUserStatus(Long userId, Integer status) {
        User exUser = getUserByIdOrThrow(userId);

        // 构造最小更新对象
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        boolean result = this.updateById(user);
        log.info("User status changed: {} -> {}", userId, status);
        return result;
    }

    @Override
    @Transactional
    public boolean deleteUsers(List<Long> userIds) {
        if (ObjectUtils.isEmpty(userIds)) {
            return true;
        }
        log.info("Batch deleting users: {}", userIds);

        // 逻辑删除 - 触发自动填充 update_time 和 update_by
        this.removeByIds(userIds);

        // 批量删除用户角色关联
        userRoleService.deleteByUserIds(userIds);

        log.info("Batch deleted {} users", userIds.size());
        return true;
    }

    @Override
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> statistics = new HashMap<>();

        // 总用户数
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        statistics.put("total", userMapper.selectCount(wrapper));

        // 启用用户数
        wrapper.eq("status", 1);
        statistics.put("active", userMapper.selectCount(wrapper));

        // 禁用用户数
        wrapper.clear();
        wrapper.eq("deleted", 0).eq("status", 0);
        statistics.put("inactive", userMapper.selectCount(wrapper));

        return statistics;
    }

    /**
     * 根据ID获取用户，不存在则抛出异常
     *
     * @param userId 用户ID
     * @return 用户实体
     * @throws UserException 用户不存在时抛出
     */
    private User getUserByIdOrThrow(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new UserException("用户不存在");
        }
        return user;
    }

    /**
     * 将User实体转换为UserDto（包含角色和权限信息）
     */
    private UserDto convertToDtoWithRolesAndPermissions(User user) {
        Long userId = user.getId();

        UserDto dto = new UserDto();
        dto.setId(userId);
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setLastLoginTime(user.getLastLoginTime());
        dto.setLastLoginIp(user.getLastLoginIp());
        dto.setCreateTime(user.getCreateTime());

        // 获取角色和权限信息
        List<Role> roles = roleMapper.selectRolesByUserId(userId);
        dto.setRoles(roles.stream()
                .map(this::convertToRoleDto)
                .collect(Collectors.toList()));

        List<String> permissions = userMapper.selectPermissionsByUserId(userId);
        dto.setPermissions(permissions);

        return dto;
    }

    /**
     * 将Role实体转换为RoleDto
     */
    private RoleDto convertToRoleDto(Role role) {
        RoleDto roleDto = new RoleDto();
        roleDto.setId(role.getId());
        roleDto.setRoleName(role.getRoleName());
        roleDto.setRoleCode(role.getRoleCode());
        roleDto.setDescription(role.getDescription());
        return roleDto;

    }


}