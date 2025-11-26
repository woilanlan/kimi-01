package top.hxll.kimi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限DTO
 *
 * @author kimi
 * @since 1.0.0
 */
@Data
public class PermissionDto {

    /**
     * 权限ID
     */
    private Long id;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 父权限ID
     */
    private Long parentId;

    /**
     * 权限类型：1-菜单，2-按钮，3-接口
     */
    private Integer permissionType;

    /**
     * 路径
     */
    private String path;

    /**
     * 请求方法（GET,POST,PUT,DELETE）
     */
    private String method;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 权限描述
     */
    private String description;

    /**
     * 状态：1-正常，0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 子权限列表
     */
    private List<PermissionDto> children;

    /**
     * 是否选中（用于角色权限分配）
     */
    private Boolean checked;
}
