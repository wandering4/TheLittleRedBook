package com.haishi.littleredbookauth.domain.mapper;

import com.haishi.littleredbookauth.domain.DO.UserDO;
import org.apache.ibatis.annotations.Param;

public interface UserDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    UserDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);


    /**
     * 根据手机号查询记录
     * @param phone
     * @return
     */
    UserDO selectByPhone(@Param("phone") String phone);
}