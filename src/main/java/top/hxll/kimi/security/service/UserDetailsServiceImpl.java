package top.hxll.kimi.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.hxll.kimi.entity.User;
import top.hxll.kimi.mapper.UserMapper;

/**
 * Spring Security 用户详情服务实现类
 *
 * @author kimi
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户信息（包含角色和权限）
        User user = userMapper.selectUserWithRolesAndPermissions(username);
        if (user == null) {
            log.error("User not found: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 如果权限列表为空，查询权限信息
        if (user.getPermissions() == null || user.getPermissions().isEmpty()) {
            user.setPermissions(userMapper.selectPermissionsByUserId(user.getId()));
        }

        return UserDetailsImpl.build(user);
    }
}