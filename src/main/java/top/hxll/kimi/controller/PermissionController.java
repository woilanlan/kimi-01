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
import top.hxll.kimi.entity.Permission;
import top.hxll.kimi.service.PermissionService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 权限管理控制器
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限树形结构
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<List<PermissionDto>> getPermissionTree() {
        log.info("Getting permission tree");

        try {
            List<PermissionDto> permissionTree = permissionService.getPermissionTree();
            return Result.success(permissionTree);
        } catch (Exception e) {
            log.error("Failed to get permission tree", e);
            return Result.error("获取权限树失败");
        }
    }

    /**
     * 获取权限列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<IPage<PermissionDto>> getPermissionPage(
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "10") @Min(1) Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer permissionType) {

        log.info("Getting permission page: page={}, size={}, keyword={}, permissionType={}",
                 page, size, keyword, permissionType);

        try {
            Page<Permission> pageRequest = new Page<>(page, size);
            IPage<PermissionDto> permissionPage = permissionService.getPermissionPage(pageRequest, keyword, permissionType);
            return Result.success(permissionPage);
        } catch (Exception e) {
            log.error("Failed to get permission page", e);
            return Result.error("获取权限列表失败");
        }
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<Permission> getPermissionById(@PathVariable @NotNull Long id) {
        log.info("Getting permission by ID: {}", id);

        try {
            Permission permission = permissionService.getPermissionById(id);
            if (permission == null) {
                return Result.notFound("权限不存在");
            }
            return Result.success(permission);
        } catch (Exception e) {
            log.error("Failed to get permission by ID: {}", id, e);
            return Result.error("获取权限详情失败");
        }
    }

    /**
     * 根据权限编码获取权限
     */
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<Permission> getPermissionByCode(@PathVariable @NotNull String code) {
        log.info("Getting permission by code: {}", code);

        try {
            Permission permission = permissionService.getPermissionByCode(code);
            if (permission == null) {
                return Result.notFound("权限不存在");
            }
            return Result.success(permission);
        } catch (Exception e) {
            log.error("Failed to get permission by code: {}", code, e);
            return Result.error("获取权限失败");
        }
    }

    /**
     * 创建权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('permission:add') or hasRole('ADMIN')")
    public Result<Permission> createPermission(@Valid @RequestBody PermissionDto permissionDto) {
        log.info("Creating permission: {}", permissionDto.getPermissionCode());

        try {
            Permission permission = permissionService.createPermission(permissionDto);
            return Result.success("权限创建成功", permission);
        } catch (Exception e) {
            log.error("Failed to create permission: {}", permissionDto.getPermissionCode(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update') or hasRole('ADMIN')")
    public Result<Permission> updatePermission(
            @PathVariable @NotNull Long id,
            @Valid @RequestBody PermissionDto permissionDto) {
        log.info("Updating permission: {}", id);

        try {
            Permission permission = permissionService.updatePermission(id, permissionDto);
            return Result.success("权限更新成功", permission);
        } catch (Exception e) {
            log.error("Failed to update permission: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete') or hasRole('ADMIN')")
    public Result<Object> deletePermission(@PathVariable @NotNull Long id) {
        log.info("Deleting permission: {}", id);

        try {
            boolean result = permissionService.deletePermission(id);
            if (result) {
                return Result.success("权限删除成功");
            } else {
                return Result.error("权限删除失败");
            }
        } catch (Exception e) {
            log.error("Failed to delete permission: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除权限
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('permission:delete') or hasRole('ADMIN')")
    public Result<Object> deletePermissions(@RequestBody Long[] ids) {
        log.info("Batch deleting permissions: {}", ids);

        try {
            permissionService.deletePermissions(Arrays.asList(ids));
            return Result.success("批量删除权限成功");
        } catch (Exception e) {
            log.error("Failed to batch delete permissions", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用/禁用权限
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('permission:update') or hasRole('ADMIN')")
    public Result<Object> togglePermissionStatus(
            @PathVariable @NotNull Long id,
            @RequestParam @NotNull Integer status) {
        log.info("Toggling permission status: {} -\u003e {}", id, status);

        try {
            boolean result = permissionService.togglePermissionStatus(id, status);
            if (result) {
                return Result.success("权限状态更新成功");
            } else {
                return Result.error("权限状态更新失败");
            }
        } catch (Exception e) {
            log.error("Failed to toggle permission status: {}", id, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据权限类型获取权限列表
     */
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<List<Permission>> getPermissionsByType(@PathVariable @NotNull Integer type) {
        log.info("Getting permissions by type: {}", type);

        try {
            List<Permission> permissions = permissionService.getPermissionsByType(type);
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("Failed to get permissions by type: {}", type, e);
            return Result.error("获取权限列表失败");
        }
    }

    /**
     * 获取权限统计信息
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<Map<String, Long>> getPermissionStats() {
        log.info("Getting permission statistics");

        try {
            Map<String, Long> stats = permissionService.getPermissionTypeStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("Failed to get permission statistics", e);
            return Result.error("获取权限统计失败");
        }
    }

    /**
     * 检查权限编码是否存在
     */
    @GetMapping("/check-code")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<Boolean> checkPermissionCodeExists(@RequestParam @NotNull String code) {
        log.info("Checking if permission code exists: {}", code);

        try {
            Permission permission = permissionService.getPermissionByCode(code);
            return Result.success(permission != null);
        } catch (Exception e) {
            log.error("Failed to check permission code: {}", code, e);
            return Result.error("检查权限编码失败");
        }
    }

    /**
     * 获取父权限列表（用于创建权限时的选择）
     */
    @GetMapping("/parents")
    @PreAuthorize("hasAuthority('permission:query') or hasRole('ADMIN')")
    public Result<List<Permission>> getParentPermissions() {
        log.info("Getting parent permissions");

        try {
            List<Permission> permissions = permissionService.getParentPermissions();
            return Result.success(permissions);
        } catch (Exception e) {
            log.error("Failed to get parent permissions", e);
            return Result.error("获取父权限列表失败");
        }
    }
}

/**
 * 权限类型常量
 */
interface PermissionType {
    int MENU = 1;
    int BUTTON = 2;
    int API = 3;
}