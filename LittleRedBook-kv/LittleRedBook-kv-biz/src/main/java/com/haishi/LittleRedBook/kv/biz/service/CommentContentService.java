package com.haishi.LittleRedBook.kv.biz.service;

import com.haishi.LittleRedBook.kv.dto.req.BatchAddCommentContentReqDTO;
import com.haishi.framework.commons.response.Response;

/**
 * @version: v1.0.0
 * @description: 评论内容存储业务
 **/
public interface CommentContentService {


    /**
     * 批量添加评论内容
     * @param batchAddCommentContentReqDTO
     * @return
     */
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);

}
