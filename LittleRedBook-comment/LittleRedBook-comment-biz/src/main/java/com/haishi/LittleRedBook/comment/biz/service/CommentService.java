package com.haishi.LittleRedBook.comment.biz.service;

import com.haishi.LittleRedBook.comment.biz.model.vo.request.PublishCommentReqVO;
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

}
