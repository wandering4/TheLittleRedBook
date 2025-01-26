package com.haishi.LittleRedBook.user.relation.biz.domain.mapper;

import com.haishi.LittleRedBook.user.relation.biz.domain.dataobject.FollowingDO;

import java.util.List;

public interface FollowingDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(FollowingDO record);

    int insertSelective(FollowingDO record);

    FollowingDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(FollowingDO record);

    int updateByPrimaryKey(FollowingDO record);


    /**
     * 查询关注列表
     * @param userId
     * @return
     */
    List<FollowingDO> selectByUserId(Long userId);

}