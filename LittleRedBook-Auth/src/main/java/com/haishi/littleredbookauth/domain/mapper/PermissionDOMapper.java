package com.haishi.littleredbookauth.domain.mapper;

import com.haishi.littleredbookauth.domain.DO.PermissionDO;

import java.util.List;

public interface PermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(PermissionDO record);

    int insertSelective(PermissionDO record);

    PermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(PermissionDO record);

    int updateByPrimaryKey(PermissionDO record);

    /**
     * 查询 APP 端所有被启用的权限
     * 普通用户只需要查看按钮权限
     * @return
     */
    List<PermissionDO> selectAppEnabledList();
}