package com.haishi.LittleRedBook.comment.biz.domain.mapper;

import com.haishi.LittleRedBook.comment.biz.domain.dataobject.CommentDO;

public interface CommentDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentDO record);

    int insertSelective(CommentDO record);

    CommentDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentDO record);

    int updateByPrimaryKey(CommentDO record);
}