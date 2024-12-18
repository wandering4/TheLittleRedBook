package com.haishi.littleredbookauth.runner;


import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import com.alibaba.nacos.shaded.com.google.common.collect.Maps;
import com.haishi.framework.commons.constant.RedisKeyConstants;
import com.haishi.framework.commons.util.JsonUtil;
import com.haishi.littleredbookauth.domain.DO.PermissionDO;
import com.haishi.littleredbookauth.domain.DO.RoleDO;
import com.haishi.littleredbookauth.domain.DO.RolePermissionDO;
import com.haishi.littleredbookauth.domain.mapper.PermissionDOMapper;
import com.haishi.littleredbookauth.domain.mapper.RoleDOMapper;
import com.haishi.littleredbookauth.domain.mapper.RolePermissionDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PushRolePermissions2RedisRunner implements ApplicationRunner {

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private PermissionDOMapper permissionDOMapper;
    @Resource
    private RolePermissionDOMapper rolePermissionDOMapper;

    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");

        //仅针对普通用户的操作进行缓存和鉴权
       try {
           // 查询出所有角色
           List<RoleDO> roleDOS = roleDOMapper.selectEnabledList();

           if (CollUtil.isNotEmpty(roleDOS)) {
               List<Long> roleIds=roleDOS.stream().map(RoleDO::getId).toList();

               // 根据角色 ID, 批量查询出所有角色对应的权限
               List<RolePermissionDO> rolePermissionDOS = rolePermissionDOMapper.selectByRoleIds(roleIds);

               // 按角色 ID 分组, 每个角色 ID 对应多个权限 ID
               Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionDOS.stream().collect(
                       Collectors.groupingBy(RolePermissionDO::getRoleId,
                               Collectors.mapping(RolePermissionDO::getPermissionId, Collectors.toList()))
               );

               // 查询 APP 端所有被启用的权限
               List<PermissionDO> permissionDOS = permissionDOMapper.selectAppEnabledList();
               // 权限 ID - 权限 DO
               Map<Long, PermissionDO> permissionIdDOMap = permissionDOS.stream().collect(
                      Collectors.toMap(PermissionDO::getId,permissionDO -> permissionDO));

               // 组织 角色ID-权限 关系
               Map<String, List<String>> roleKeyPermissionsMap = Maps.newHashMap();

               // 循环所有角色
               roleDOS.forEach(roleDO -> {
                   // 当前角色 ID
                   Long roleId = roleDO.getId();
                   // 当前角色 roleKey
                   String roleKey = roleDO.getRoleKey();
                   // 当前角色 ID 对应的权限 ID 集合
                   List<Long> permissionIds = roleIdPermissionIdsMap.get(roleId);
                   if (CollUtil.isNotEmpty(permissionIds)) {
                       List<String> permissionKeys = Lists.newArrayList();
                       permissionIds.forEach(permissionId -> {
                           // 根据权限 ID 获取具体的权限 DO 对象
                           PermissionDO permissionDO = permissionIdDOMap.get(permissionId);
                           permissionKeys.add(permissionDO.getPermissionKey());
                       });
                       roleKeyPermissionsMap.put(roleKey, permissionKeys);
                   }
               });

               // 同步至 Redis 中，方便后续网关查询鉴权使用
               roleKeyPermissionsMap.forEach((roleKey, permissions) -> {
                   String key = RedisKeyConstants.buildRolePermissionsKey(roleKey);
                   redisTemplate.opsForValue().set(key, JsonUtil.toJsonString(permissions));
               });

           }


           log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
       }catch (Exception e){
           log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
       }
    }
}