package com.haishi.LittleRedBook.comment.biz.domain.mapper;

import com.haishi.LittleRedBook.comment.biz.domain.dataobject.CommentDO;
import com.haishi.LittleRedBook.comment.biz.model.bo.CommentBO;
import com.haishi.LittleRedBook.comment.biz.model.bo.CommentHeatBO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CommentDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(CommentDO record);

    int insertSelective(CommentDO record);

    CommentDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(CommentDO record);

    int updateByPrimaryKey(CommentDO record);

    /**
     * 根据评论 ID 批量查询
     *
     * @param commentIds
     * @return
     */
    List<CommentDO> selectByCommentIds(@Param("commentIds") List<Long> commentIds);

    /**
     * 批量插入评论
     *
     * @param comments
     * @return
     */
    int batchInsert(@Param("comments") List<CommentBO> comments);

    /**
     * 批量更新热度值
     *
     * @param commentIds
     * @param commentHeatBOS
     * @return
     */
    int batchUpdateHeatByCommentIds(@Param("commentIds") List<Long> commentIds, @Param("commentHeatBOS") List<CommentHeatBO> commentHeatBOS);


    /**
     * 查询一级评论下最早回复的评论
     *
     * @param parentId
     * @return
     */
    CommentDO selectEarliestByParentId(Long parentId);

    /**
     * 更新一级评论的 first_reply_comment_id
     *
     * @param firstReplyCommentId
     * @param id
     * @return
     */
    int updateFirstReplyCommentIdByPrimaryKey(@Param("firstReplyCommentId") Long firstReplyCommentId, @Param("id") Long id);


    /**
     * 查询评论分页数据(一级评论)
     *
     * @param noteId
     * @param offset
     * @param pageSize
     * @return
     */
    List<CommentDO> selectPageList(@Param("noteId") Long noteId, @Param("offset") long offset, @Param("pageSize") long pageSize);


    /**
     * 批量查询二级评论
     * @param commentIds
     * @return
     */
    List<CommentDO> selectTwoLevelCommentByIds(@Param("commentIds") List<Long> commentIds);


}