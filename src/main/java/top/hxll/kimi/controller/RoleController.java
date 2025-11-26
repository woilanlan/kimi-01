package top.hxll.kimi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.hxll.kimi.common.Result;
import top.hxll.kimi.dto.PermissionDto;
import top.hxll.kimi.dto.RoleDto;
import top.hxll.kimi.entity.Permission;
import top.hxll.kimi.entity.Role;
import top.hxll.kimi.service.PermissionService;
import top.hxll.kimi.service.RoleService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 角色管理控制器
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;
    private final PermissionService permissionService;

    /**
     * 获取所有角色列表
     */
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<List<RoleDto>> getAllRoles() {
        log.info("Getting all roles");

        try {
            List<RoleDto> roles = roleService.getAllRoles();
            return Result.success(roles);
        } catch (Exception e) {
            log.error("Failed to get all roles", e);
            return Result.error("获取角色列表失败");
        }
    }

    /**
     * 获取角色列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<IPage<RoleDto>> getRolePage(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        log.info("Getting role page: page={}, size={}, keyword={}, status={}", page, size, keyword, status);

        try {
            Page<Role> pageRequest = new Page<>(page, size);
            IPage<RoleDto> rolePage = roleService.getRolePage(pageRequest, keyword, status);
            return Result.success(rolePage);
        } catch (Exception e) {
            log.error("Failed to get role page", e);
            return Result.error("获取角色列表失败");
        }
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<Role> getRoleById(@PathVariable @NotNull Long id) {
        log.info("Getting role by ID: {}", id);

        try {
            Role role = roleService.getRoleByIdRaw(id);
            if (role == null) {
                return Result.notFound("角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            log.error("Failed to get role by ID: {}", id, e);
            return Result.error("获取角色详情失败");
        }
    }

    /**
     * 根据角色编码获取角色
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<Role> getRoleByCode(@PathVariable @NotNull String code) {
        log.info("Getting role by code: {}", code);

        try {
            Role role = roleService.getRoleByCode(code);
            if (role == null) {
                return Result.notFound("角色不存在");
            }
            return Result.success(role);
        } catch (Exception e) {
            log.error("Failed to get role by code: {}", code, e);
            return Result.error("获取角色失败");
        }
    }

    /**
     * 根据用户ID获取角色列表
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<List<RoleDto>> getRolesByUserId(@PathVariable @NotNull Long userId) {
        log.info("Getting roles by user ID: {}", userId);

        try {
            List<RoleDto> roles = roleService.getRolesByUserId(userId);
            return Result.success(roles);
        } catch (Exception e) {
            log.error("Failed to get roles by user ID: {}", userId, e);
            return Result.error("获取用户角色列表失败");
        }
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('role:add') or hasRole('ADMIN')")
    public Result<Role> createRole(@Valid @RequestBody RoleDto roleDto) {
        log.info("Creating role: {}", roleDto.getRoleCode());

        try {
            Role role = roleService.createRole(roleDto);
            return Result.success("角色创建成功", role);
        } catch (Exception e) {
            log.error("Failed to create role: {}", roleDto.getRoleCode(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public Result<Role> updateRole(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody RoleDto roleDto) {
        log.info("Updating role: {}", id);

        try {
            Role role = roleService.updateRole(id, roleDto);
            return Result.success("角色更新成功", role);
        } catch (Exception e) {
            log.error("Failed to update role: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete') or hasRole('ADMIN')")
    public Result<Object> deleteRole(@PathVariable @NotNull Long id) {
        log.info("Deleting role: {}", id);

        try {
            boolean result = roleService.deleteRole(id);
            if (result) {
                return Result.success("角色删除成功");
            } else {
                return Result.error("角色删除失败");
            }
        } catch (Exception e) {
            log.error("Failed to delete role: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除角色
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('role:delete') or hasRole('ADMIN')")
    public Result<Object> deleteRoles(@RequestBody Long[] ids) {
        log.info("Batch deleting roles: {}", ids);

        try {
            roleService.deleteRoles(Arrays.asList(ids));
            return Result.success("批量删除角色成功");
        } catch (Exception e) {
            log.error("Failed to batch delete roles", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用/禁用角色
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public Result<Object> toggleRoleStatus(
            @PathVariable @NotNull Long id,
            @RequestParam @NotNull Integer status) {
        log.info("Toggling role status: {} -\u003e {}", id, status);

        try {
            boolean result = roleService.toggleRoleStatus(id, status);
            if (result) {
                return Result.success("角色状态更新成功");
            } else {
                return Result.error("角色状态更新失败");
            }
        } catch (Exception e) {
            log.error("Failed to toggle role status: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取角色权限列表
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<List<PermissionDto>> getRolePermissions(@PathVariable @NotNull Long id) {
        log.info("Getting permissions for role: {}", id);

        try {
            List<PermissionDto> permissions = permissionService.getPermissionsByRoleId(id);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("Failed to get permissions for role: {}", id, e);
            return Result.error("获取角色权限列表失败");
        }
    }

    /**
     * 更新角色权限
     */
    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('role:update') or hasRole('ADMIN')")
    public Result<Object> updateRolePermissions(
            @PathVariable @NotNull Long id,
            @RequestBody Long[] permissionIds) {
        log.info("Updating permissions for role: {} -\u003e {}", id, permissionIds);

        try {
            boolean result = roleService.updateRolePermissions(id, Arrays.asList(permissionIds));
            if (result) {
                return Result.success("角色权限更新成功");
            } else {
                return Result.error("角色权限更新失败");
            }
        } catch (Exception e) {
            log.error("Failed to update role permissions: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取角色权限ID列表
     */
    @GetMapping("/{id}/permission-ids")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<List<Long>> getRolePermissionIds(@PathVariable @NotNull Long id) {
        log.info("Getting permission IDs for role: {}", id);

        try {
            List<Long> permissionIds = roleService.getRolePermissionIds(id);
            return Result.success(permissionIds);
        } catch (Exception e) {
            log.error("Failed to get permission IDs for role: {}", id, e);
            return Result.error("获取角色权限ID列表失败");
        }
    }

    /**
     * 获取角色统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<Map<String, Long>> getRoleStats() {
        log.info("Getting role statistics");

        try {
            Map<String, Long> stats = roleService.getRoleStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Failed to get role statistics", e);
            return Result.error("获取角色统计失败");
        }
    }

    /**
     * 检查角色编码是否存在
     */
    @GetMapping("/check-code")
    @PreAuthorize("hasAuthority('role:query') or hasRole('ADMIN')")
    public Result<Boolean> checkRoleCodeExists(@RequestParam @NotNull String code) {
        log.info("Checking if role code exists: {}", code);

        try {
            Role role = roleService.getRoleByCode(code);
            return Result.success(role != null);
        } catch (Exception e) {
            log.error("Failed to check role code: {}", code, e);
            return Result.error("检查角色编码失败");
        }
    }
}

/**
 * 角色类型常量
 */
interface RoleStatus {
    int DISABLED = 0;
    int ENABLED = 1;
}