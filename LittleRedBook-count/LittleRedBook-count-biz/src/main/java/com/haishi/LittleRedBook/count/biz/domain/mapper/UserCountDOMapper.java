package com.haishi.LittleRedBook.count.biz.domain.mapper;

import com.haishi.LittleRedBook.count.biz.domain.dataobject.UserCountDO;

public interface UserCountDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCountDO record);

    int insertSelective(UserCountDO record);

    UserCountDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCountDO record);

    int updateByPrimaryKey(UserCountDO record);
}