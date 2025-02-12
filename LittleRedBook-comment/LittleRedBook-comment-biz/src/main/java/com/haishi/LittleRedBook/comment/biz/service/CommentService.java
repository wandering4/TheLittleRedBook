package com.haishi.LittleRedBook.comment.biz.service;

import com.haishi.LittleRedBook.comment.biz.model.vo.request.FindChildCommentPageListReqVO;
import com.haishi.LittleRedBook.comment.biz.model.vo.request.FindCommentPageListReqVO;
import com.haishi.LittleRedBook.comment.biz.model.vo.request.PublishCommentReqVO;
import com.haishi.LittleRedBook.comment.biz.model.vo.response.FindChildCommentItemRspVO;
import com.haishi.LittleRedBook.comment.biz.model.vo.response.FindCommentItemRspVO;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;

/**
 * @version: v1.0.0
 * @description: 评论业务
 **/
public interface CommentService {

    /**
     * 发布评论
     * @param publishCommentReqVO
     * @return
     */
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);

    /**
     * 评论列表分页查询
     * @param findCommentPageListReqVO
     * @return
     */
    PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO);

    /**
     * 二级评论分页查询
     * @param findChildCommentPageListReqVO
     * @return
     */
    PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO);


}
