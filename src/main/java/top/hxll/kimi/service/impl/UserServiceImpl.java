package top.hxll.kimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

        // 一次性查询所有可能冲突的用户信息
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", registerReq.getUsername())
                .or(qw -> qw.eq("email", registerReq.getEmail()))
                .or(qw -> qw.eq("phone", registerReq.getPhone()));

        List<User> existingUsers = userMapper.selectList(queryWrapper);

        // 检查冲突
        for (User existingUser : existingUsers) {
            if (existingUser.getUsername().equals(registerReq.getUsername())) {
                throw new UserException("用户名已存在");
            }
            if (registerReq.getEmail() != null &&
                    registerReq.getEmail().equals(existingUser.getEmail())) {
                throw new UserException("邮箱已存在");
            }
            if (registerReq.getPhone() != null &&
                    registerReq.getPhone().equals(existingUser.getPhone())) {
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
        Role role = roleMapper.selectByRoleCode(roleCode);
        if (role != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRole.setCreateBy(user.getId());
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

        User user = getUserByIdOrThrow(userId);

        // 验证旧密码
        if (!passwordEncoder.matches(passwordChangeReq.getOldPassword(), user.getPassword())) {
            throw new PasswordException("旧密码不正确");
        }

        // 验证新密码是否一致
        if (!passwordChangeReq.isNewPasswordMatch()) {
            throw new PasswordException("两次输入的新密码不一致");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordChangeReq.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(userId);

        boolean result = this.updateById(user);
        log.info("Password changed successfully for user: {}", userId);
        return result;
    }

    @Override
    @Transactional
    public boolean resetPassword(Long userId) {
        log.info("Resetting password for user: {}", userId);

        User user = getUserByIdOrThrow(userId);

        // 重置为默认密码
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
        QueryWrapper<User> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("username", keyword)
                   .or()
                   .like("nickname", keyword)
                   .or()
                   .like("email", keyword);
        }

        if (status != null) {
            wrapper.eq("status", status);
        }

        wrapper.eq("deleted", 0)
               .orderByDesc("create_time");

        // 使用selectUserBasicPage只查询基本字段，避免敏感字段查询
        IPage<User> userPage = userMapper.selectUserBasicPage(page, wrapper);
        IPage<UserDto> dtoPage = new Page<>();
        dtoPage.setCurrent(userPage.getCurrent());
        dtoPage.setSize(userPage.getSize());
        dtoPage.setTotal(userPage.getTotal());

        // 使用BeanUtils.copyProperties简化对象复制，分页列表不需要角色权限信息
        dtoPage.setRecords(userPage.getRecords().stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    BeanUtils.copyProperties(user, dto);
                    return dto;
                })
                .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    @Transactional
    public boolean updateUserInfo(Long userId, UserUpdateReq updateRequest) {
        User user = getUserByIdOrThrow(userId);

        // 更新用户信息
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

        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(UserContextUtils.getCurrentUserId());

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

        // 逻辑删除 - 使用 UpdateWrapper 配合框架自动填充
        // 设置 deleted = 1，触发自动填充 update_time 和 update_by
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userId).set("deleted", 1);
        boolean result = this.update(updateWrapper);

        // 删除用户角色关联
        userRoleService.deleteByUserId(userId);

        log.info("User deleted logically: {}", userId);
        return result;
    }

    @Override
    @Transactional
    public boolean updateUserRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色关联
        userRoleService.deleteByUserId(userId);

        // 添加新角色关联
        if (!CollectionUtils.isEmpty(roleIds)) {
            List<UserRole> userRoles = roleIds.stream()
                    .distinct()
                    .map(roleId -> {
                        UserRole userRole = new UserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(roleId);
                        userRole.setCreateBy(UserContextUtils.getCurrentUserId());
                        return userRole;
                    })
                    .collect(Collectors.toList());

            if (!userRoles.isEmpty()) {
                userRoleService.batchInsert(userRoles);
            }
        }

        log.info("Updated roles for user {}: {}", userId, roleIds);
        return true;
    }

    @Override
    @Transactional
    public User createUser(UserCreateReq createRequest) {
        log.info("Creating user by admin: {}", createRequest.getUsername());

        // 验证密码一致性
        if (!createRequest.isPasswordMatch()) {
            throw new PasswordException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (userMapper.selectCount(new QueryWrapper<User>().eq("username", createRequest.getUsername())) > 0) {
            throw new UserException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(createRequest.getUsername());
        user.setPassword(passwordEncoder.encode(createRequest.getPassword()));
        user.setEmail(createRequest.getEmail());
        user.setPhone(createRequest.getPhone());
        user.setNickname(createRequest.getNickname());
        user.setStatus(createRequest.getStatus() != null ? createRequest.getStatus() : 1);
        user.setAvatar("/default-avatar.png");
        user.setCreateBy(UserContextUtils.getCurrentUserId());
        user.setCreateTime(LocalDateTime.now());

        this.save(user);

        // 分配角色
        if (createRequest.getRoleIds() != null && createRequest.getRoleIds().length > 0) {
            List<Long> roleIds = Arrays.asList(createRequest.getRoleIds());
            updateUserRoles(user.getId(), roleIds);
        }

        log.info("User created by admin: {}", user.getUsername());
        return user;
    }

    @Override
    @Transactional
    public boolean toggleUserStatus(Long userId, Integer status) {
        User user = getUserByIdOrThrow(userId);

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdateBy(UserContextUtils.getCurrentUserId());

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

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(User::getId, userIds)
                .set(User::getDeleted, 1)
                .set(User::getUpdateTime, LocalDateTime.now())
                .set(User::getUpdateBy, UserContextUtils.getCurrentUserId());

        this.update(updateWrapper);

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
        UserDto dto = new UserDto();
        dto.setId(user.getId());
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
        List<Role> roles = roleMapper.selectRolesByUserId(user.getId());
        if (roles != null) {
            dto.setRoles(roles.stream()
                    .map(role -> {
                        RoleDto roleDto = new RoleDto();
                        roleDto.setId(role.getId());
                        roleDto.setRoleName(role.getRoleName());
                        roleDto.setRoleCode(role.getRoleCode());
                        roleDto.setDescription(role.getDescription());
                        return roleDto;
                    })
                    .collect(Collectors.toList()));
        }

        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        dto.setPermissions(permissions);

        return dto;
    }
}