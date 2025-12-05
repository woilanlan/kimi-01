package top.hxll.kimi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import top.hxll.kimi.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联服务接口
 *
 * @author kimi
 * @since 1.0.0
 */
public interface UserRoleService extends IService<UserRole> {

    /**
     * 根据用户ID删除用户角色关联
     *
     * @param userId 用户ID
     * @return 删除的记录数
     */
    boolean deleteByUserId(Long userId);

    /**
     * 根据用户ID列表批量删除用户角色关联
     *
     * @param userIds 用户ID列表
     * @return 删除的记录数
     */
    void deleteByUserIds(List<Long> userIds);

    /**
     * 批量插入用户角色关联
     *
     * @param userRoles 用户角色关联列表
     * @return 插入的记录数
     */
    int batchInsert(List<UserRole> userRoles);


}
