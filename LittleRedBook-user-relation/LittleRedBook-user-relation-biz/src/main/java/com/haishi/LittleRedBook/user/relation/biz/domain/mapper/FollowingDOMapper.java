package com.haishi.LittleRedBook.user.relation.biz.domain.mapper;

import com.haishi.LittleRedBook.user.relation.biz.domain.dataobject.FollowingDO;
import org.apache.ibatis.annotations.Param;

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
     *
     * @param userId
     * @return
     */
    List<FollowingDO> selectByUserId(Long userId);

    /**
     * 取关
     *
     * @param userId
     * @param unfollowUserId
     * @return
     */
    int deleteByUserIdAndFollowingUserId(@Param("userId") Long userId, @Param("unfollowUserId") Long unfollowUserId);

    /**
     * 查询记录总数
     *
     * @param userId
     * @return
     */
    long selectCountByUserId(Long userId);

    /**
     * 分页查询
     *
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<FollowingDO> selectPageListByUserId(@Param("userId") Long userId, @Param("offset") long offset, @Param("limit") long limit);

    /**
     * 查询关注用户列表
     * @param userId
     * @return
     */
    List<FollowingDO> selectAllByUserId(Long userId);

}