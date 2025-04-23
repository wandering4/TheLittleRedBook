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
    int selectNoteIsLiked(@Param("userId") Long userId, @Param("noteId") Long noteId);

    /**
     * 查询当前用户所有点赞的笔记id
     * @param userId
     * @return
     */
    List<NoteLikeDO> selectByUserId(@Param("userId") Long userId);


    /**
     * 查询当前用户点赞笔记
     * @param userId
     * @param limit
     * @return
     */
    List<NoteLikeDO> selectLikedByUserIdAndLimit(@Param("userId") Long userId, @Param("limit")  int limit);

    /**
     * 新增笔记点赞记录，若已存在，则更新笔记点赞记录
     * @param noteLikeDO
     * @return
     */
    int insertOrUpdate(NoteLikeDO noteLikeDO);


    /**
     * 取消点赞
     * @param noteLikeDO
     * @return
     */
    int update2UnlikeByUserIdAndNoteId(NoteLikeDO noteLikeDO);



    /**
     * 查询某用户，对于一批量笔记的已点赞记录
     * @param userId
     * @param noteIds
     * @return
     */
    List<NoteLikeDO> selectByUserIdAndNoteIds(@Param("userId") Long userId, @Param("noteIds") List<Long> noteIds);


}