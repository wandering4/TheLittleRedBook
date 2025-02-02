package com.haishi.LittleRedBook.note.biz.domain.mapper;


import com.haishi.LittleRedBook.note.biz.domain.dataobject.NoteLikeDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NoteLikeDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteLikeDO record);

    int insertSelective(NoteLikeDO record);

    NoteLikeDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteLikeDO record);

    int updateByPrimaryKey(NoteLikeDO record);

    /**
     * 查询当前用户是否已经点赞当前笔记
     * @param userId
     * @param noteId
     * @return
     */
    int selectCountByUserIdAndNoteId(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询当前用户所有点赞的笔记id
     * @param userId
     * @return
     */
    List<NoteLikeDO> selectByUserId(@Param("userId") Long userId);
}