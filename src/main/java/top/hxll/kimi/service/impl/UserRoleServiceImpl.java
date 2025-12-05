package top.hxll.kimi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import top.hxll.kimi.entity.UserRole;
import top.hxll.kimi.mapper.UserRoleMapper;
import top.hxll.kimi.service.UserRoleService;

import java.util.List;

/**
 * 用户角色关联服务实现类
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleService {

    private final UserRoleMapper userRoleMapper;

    @Override
    @Transactional
    public boolean deleteByUserId(Long userId) {
        log.debug("删除用户角色关联, userId: {}", userId);

        // 删除用户角色关联(不触发自动填充)
        LambdaQueryWrapper<UserRole> qw = new LambdaQueryWrapper<>();
        qw.eq(UserRole::getUserId, userId);
        return this.remove(qw);
    }

    @Override
    @Transactional
    public void deleteByUserIds(List<Long> userIds) {
        if (!CollectionUtils.isEmpty(userIds)) {
            QueryWrapper<UserRole> wrapper = new QueryWrapper<>();
            wrapper.in("user_id", userIds);
            this.remove(wrapper);
        }
    }


    @Override
    @Transactional
    public int batchInsert(List<UserRole> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            return 0;
        }
        log.debug("批量插入用户角色关联, 数量: {}", userRoles.size());
        return userRoleMapper.batchInsert(userRoles);
    }
}
