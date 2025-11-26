package top.hxll.kimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.hxll.kimi.dto.PermissionDto;
import top.hxll.kimi.entity.Permission;
import top.hxll.kimi.entity.RolePermission;
import top.hxll.kimi.mapper.PermissionMapper;
import top.hxll.kimi.mapper.RolePermissionMapper;
import top.hxll.kimi.service.PermissionService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<PermissionDto> getPermissionTree() {
        // 查询所有权限
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0)
               .orderByAsc("sort_order", "id");

        List<Permission> permissions = permissionMapper.selectList(wrapper);

        // 转换为DTO
        List<PermissionDto> dtoList = permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        // 构建树形结构
        return buildPermissionTree(dtoList);
    }

    @Override
    public IPage<PermissionDto> getPermissionPage(Page<Permission> page, String keyword, Integer permissionType) {
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like("permission_name", keyword)
                   .or()
                   .like("permission_code", keyword);
        }

        if (permissionType != null) {
            wrapper.eq("permission_type", permissionType);
        }

        wrapper.eq("deleted", 0)
               .orderByAsc("sort_order", "id");

        IPage<Permission> permissionPage = permissionMapper.selectPage(page, wrapper);
        IPage<PermissionDto> dtoPage = new Page<>();
        dtoPage.setCurrent(permissionPage.getCurrent());
        dtoPage.setSize(permissionPage.getSize());
        dtoPage.setTotal(permissionPage.getTotal());
        dtoPage.setRecords(permissionPage.getRecords().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()));

        return dtoPage;
    }

    @Override
    public List<PermissionDto> getPermissionsByRoleId(Long roleId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Permission getPermissionById(Long id) {
        return permissionMapper.selectById(id);
    }

    @Override
    public Permission getPermissionByCode(String permissionCode) {
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        wrapper.eq("permission_code", permissionCode)
               .eq("deleted", 0);
        return permissionMapper.selectOne(wrapper);
    }

    @Override
    @Transactional
    public Permission createPermission(PermissionDto permissionDto) {
        // 检查权限编码是否已存在
        if (getPermissionByCode(permissionDto.getPermissionCode()) != null) {
            throw new RuntimeException("权限编码已存在");
        }

        Permission permission = new Permission();
        permission.setPermissionName(permissionDto.getPermissionName());
        permission.setPermissionCode(permissionDto.getPermissionCode());
        permission.setParentId(permissionDto.getParentId() != null ? permissionDto.getParentId() : 0L);
        permission.setPermissionType(permissionDto.getPermissionType());
        permission.setPath(permissionDto.getPath());
        permission.setMethod(permissionDto.getMethod());
        permission.setIcon(permissionDto.getIcon());
        permission.setSortOrder(permissionDto.getSortOrder() != null ? permissionDto.getSortOrder() : 0);
        permission.setDescription(permissionDto.getDescription());
        permission.setStatus(permissionDto.getStatus() != null ? permissionDto.getStatus() : 1);
        permission.setCreateBy(getCurrentUserId());
        permission.setCreateTime(LocalDateTime.now());

        permissionMapper.insert(permission);
        log.info("Permission created successfully: {}", permission.getPermissionCode());
        return permission;
    }

    @Override
    @Transactional
    public Permission updatePermission(Long id, PermissionDto permissionDto) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        // 检查权限编码是否被其他权限使用
        if (!permission.getPermissionCode().equals(permissionDto.getPermissionCode())) {
            Permission existingPermission = getPermissionByCode(permissionDto.getPermissionCode());
            if (existingPermission != null && !existingPermission.getId().equals(id)) {
                throw new RuntimeException("权限编码已存在");
            }
        }

        permission.setPermissionName(permissionDto.getPermissionName());
        permission.setPermissionCode(permissionDto.getPermissionCode());
        permission.setParentId(permissionDto.getParentId() != null ? permissionDto.getParentId() : 0L);
        permission.setPermissionType(permissionDto.getPermissionType());
        permission.setPath(permissionDto.getPath());
        permission.setMethod(permissionDto.getMethod());
        permission.setIcon(permissionDto.getIcon());
        permission.setSortOrder(permissionDto.getSortOrder() != null ? permissionDto.getSortOrder() : 0);
        permission.setDescription(permissionDto.getDescription());
        permission.setStatus(permissionDto.getStatus() != null ? permissionDto.getStatus() : 1);
        permission.setUpdateBy(getCurrentUserId());
        permission.setUpdateTime(LocalDateTime.now());

        permissionMapper.updateById(permission);
        log.info("Permission updated successfully: {}", permission.getPermissionCode());
        return permission;
    }

    @Override
    @Transactional
    public boolean deletePermission(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        // 检查是否有子权限
        List<Permission> children = permissionMapper.selectByParentId(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new RuntimeException("该权限存在子权限，不能删除");
        }

        // 检查是否被角色使用
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new QueryWrapper<RolePermission>().eq("permission_id", id));
        if (!CollectionUtils.isEmpty(rolePermissions)) {
            throw new RuntimeException("该权限已被角色使用，不能删除");
        }

        // 逻辑删除
        permission.setDeleted(1);
        permission.setUpdateBy(getCurrentUserId());
        permission.setUpdateTime(LocalDateTime.now());

        int result = permissionMapper.updateById(permission);
        log.info("Permission deleted logically: {}", permission.getPermissionCode());
        return result > 0;
    }

    @Override
    @Transactional
    public boolean deletePermissions(List<Long> ids) {
        for (Long id : ids) {
            deletePermission(id);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean togglePermissionStatus(Long id, Integer status) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new RuntimeException("权限不存在");
        }

        permission.setStatus(status);
        permission.setUpdateBy(getCurrentUserId());
        permission.setUpdateTime(LocalDateTime.now());

        int result = permissionMapper.updateById(permission);
        log.info("Permission status changed: {} -> {}", permission.getPermissionCode(), status);
        return result > 0;
    }

    @Override
    public List<Permission> getPermissionsByType(Integer type) {
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        wrapper.eq("permission_type", type)
               .eq("deleted", 0)
               .orderByAsc("sort_order", "id");
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public List<Permission> getParentPermissions() {
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", 0)
               .eq("deleted", 0)
               .orderByAsc("sort_order", "id");
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Long> getPermissionTypeStats() {
        Map<String, Long> stats = new HashMap<>();

        // 菜单权限数量
        long menuCount = permissionMapper.selectCount(
                new QueryWrapper<Permission>().eq("permission_type", 1).eq("deleted", 0));
        stats.put("menu", menuCount);

        // 按钮权限数量
        long buttonCount = permissionMapper.selectCount(
                new QueryWrapper<Permission>().eq("permission_type", 2).eq("deleted", 0));
        stats.put("button", buttonCount);

        // 接口权限数量
        long apiCount = permissionMapper.selectCount(
                new QueryWrapper<Permission>().eq("permission_type", 3).eq("deleted", 0));
        stats.put("api", apiCount);

        return stats;
    }

    /**
     * 构建权限树
     */
    private List<PermissionDto> buildPermissionTree(List<PermissionDto> permissionDtos) {
        Map<Long, PermissionDto> permissionMap = permissionDtos.stream()
                .collect(Collectors.toMap(PermissionDto::getId, dto -> dto));

        List<PermissionDto> rootPermissions = new ArrayList<>();

        for (PermissionDto permissionDto : permissionDtos) {
            if (permissionDto.getParentId() == null || permissionDto.getParentId() == 0) {
                rootPermissions.add(permissionDto);
            } else {
                PermissionDto parent = permissionMap.get(permissionDto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permissionDto);
                }
            }
        }

        return rootPermissions;
    }

    /**
     * 实体转换为DTO
     */
    private PermissionDto convertToDto(Permission permission) {
        PermissionDto dto = new PermissionDto();
        dto.setId(permission.getId());
        dto.setPermissionName(permission.getPermissionName());
        dto.setPermissionCode(permission.getPermissionCode());
        dto.setParentId(permission.getParentId());
        dto.setPermissionType(permission.getPermissionType());
        dto.setPath(permission.getPath());
        dto.setMethod(permission.getMethod());
        dto.setIcon(permission.getIcon());
        dto.setSortOrder(permission.getSortOrder());
        dto.setDescription(permission.getDescription());
        dto.setStatus(permission.getStatus());
        dto.setCreateTime(permission.getCreateTime());
        return dto;
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof top.hxll.kimi.security.service.UserDetailsImpl) {
                top.hxll.kimi.security.service.UserDetailsImpl userDetails =
                    (top.hxll.kimi.security.service.UserDetailsImpl) authentication.getPrincipal();
                return userDetails.getId();
            }
        } catch (Exception e) {
            log.error("Failed to get current user ID", e);
        }
        return null;
    }
}