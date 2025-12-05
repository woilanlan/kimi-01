package top.hxll.kimi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hxll.kimi.common.PageReq;
import top.hxll.kimi.common.Result;
import top.hxll.kimi.common.exception.PasswordException;
import top.hxll.kimi.dto.*;
import top.hxll.kimi.dto.req.user.UserCreateReq;
import top.hxll.kimi.dto.req.user.UserUpdateReq;
import top.hxll.kimi.entity.User;
import top.hxll.kimi.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Map;

/**
 * 用户管理控制器
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 获取用户列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<UserDto>> getUserPage(PageReq pageReq,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Integer status) {

        log.info("Getting user page: page={}, size={}, keyword={}, status={}",
                pageReq.getPage(), pageReq.getSize(), keyword, status);
        IPage<UserDto> userPage = userService.getUserPage(pageReq.toPage(), keyword, status);
        return Result.success(userPage);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDto> getUserById(@PathVariable @NotNull Long id) {
        log.info("Getting user by ID: {}", id);
        UserDto user = userService.getUserWithRolesAndPermissions(id);
        return Result.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<User> createUser(@Valid @RequestBody UserCreateReq req) {
        log.info("Creating user: {}", req.getUsername());
        // 验证密码一致性
        if (!req.isPasswordMatch()) {
            return Result.error("两次输入的密码不一致");
        }
        User user = userService.createUser(req);
        return Result.success("用户创建成功", user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> updateUser(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody UserUpdateReq updateRequest) {
        log.info("Updating user: {}", id);
        boolean result = userService.updateUserInfo(id, updateRequest);
        return result ? Result.success("用户更新成功") : Result.error("用户更新失败");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> deleteUser(@PathVariable @NotNull Long id) {
        log.info("Deleting user: {}", id);
        boolean result = userService.deleteUser(id);
        return result ? Result.success("用户删除成功") : Result.error("用户删除失败");
    }

    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> deleteUsers(@RequestBody Long[] ids) {
        log.info("Batch deleting users: {}", ids);
        boolean result = userService.deleteUsers(Arrays.asList(ids));
        return result ? Result.success("批量删除用户成功") : Result.error("批量删除用户失败");
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> toggleUserStatus(
            @PathVariable @NotNull Long id,
            @RequestParam @NotNull Integer status) {
        log.info("Toggling user status: {} -> {}", id, status);
        boolean result = userService.toggleUserStatus(id, status);
        return result ? Result.success("用户状态更新成功") : Result.error("用户状态更新失败");
    }

    /**
     * 重置用户密码（重置为默认密码: 123456）
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> resetPassword(@PathVariable @NotNull Long id) {
        log.info("Resetting password for user: {}", id);
        boolean result = userService.resetPassword(id);
        return result ? Result.success("密码重置成功，默认密码为：123456") : Result.error("密码重置失败");
    }

    /**
     * 更新用户角色
     */
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> updateUserRoles(
            @PathVariable @NotNull Long id,
            @RequestBody Long[] roleIds) {
        log.info("Updating roles for user: {} - {}", id, roleIds);

        boolean result = userService.updateUserRoles(id, Arrays.asList(roleIds));
        return result ? Result.success("用户角色更新成功") : Result.error("用户角色更新失败");
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Object> getUserStats() {
        log.info("Getting user statistics");
        Map<String, Long> stats = userService.getUserStatistics();
        return Result.success(stats);
    }
}