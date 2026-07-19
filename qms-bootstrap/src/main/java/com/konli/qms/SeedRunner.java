package com.konli.qms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.konli.qms.domain.uop.entity.SysUser;
import com.konli.qms.domain.uop.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 演示数据种子（空壳冒烟用，幂等）。
 *
 * <p>后端当前 schema 迁移未包含 sys_user 的演示账号，导致登录无可用账号。
 * 本 Runner 在应用启动时确保以下演示账号存在且密码统一为 123456（与前端 mock 演示账号同名）：
 *   admin(集团管理员) / mz.insp(MZ) / sz.insp(SZ) / sz.sqe(SZ)
 * 若账号已存在则仅重置密码，便于前后端联调。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeedRunner implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /** 演示账号：username -> (真实姓名, orgId)。orgId 为 null 表示集团(全部公司)。 */
    private record DemoSeed(String username, String realName, String orgId) {}

    @Override
    public void run(ApplicationArguments args) {
        List<DemoSeed> seeds = List.of(
                new DemoSeed("admin", "系统管理员", null),
                new DemoSeed("mz.insp", "MZ 巡检员", null),
                new DemoSeed("sz.insp", "SZ 巡检员", null),
                new DemoSeed("sz.sqe", "SZ 供应商质量工程师", null)
        );

        for (DemoSeed s : seeds) {
            SysUser existing = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, s.username()));
            if (existing != null) {
                existing.setPasswordHash(passwordEncoder.encode("123456"));
                existing.setStatus("启用");
                userMapper.updateById(existing);
                log.info("[SeedRunner] 重置演示账号密码: {}", s.username());
            } else {
                SysUser u = new SysUser();
                u.setUsername(s.username());
                u.setRealName(s.realName());
                u.setPasswordHash(passwordEncoder.encode("123456"));
                u.setOrgId(s.orgId());
                u.setStatus("启用");
                userMapper.insert(u);
                log.info("[SeedRunner] 写入演示账号: {}", s.username());
            }
        }
    }
}
