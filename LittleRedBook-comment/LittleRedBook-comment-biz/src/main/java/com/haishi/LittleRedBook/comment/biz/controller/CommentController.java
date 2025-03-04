package com.haishi.LittleRedBook.comment.biz.controller;

import com.haishi.LittleRedBook.comment.biz.model.vo.request.*;
import com.haishi.LittleRedBook.comment.biz.model.vo.response.FindChildCommentItemRspVO;
import com.haishi.LittleRedBook.comment.biz.model.vo.response.FindCommentItemRspVO;
import com.haishi.LittleRedBook.comment.biz.service.CommentService;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version: v1.0.0
 * @description: 评论接口
 */
@RestController
@RequestMapping("/comment")
@Slf4j
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping("/publish")
    @ApiOperationLog(description = "发布评论")
    public Response<?> publishComment(@Validated @RequestBody PublishCommentReqVO publishCommentReqVO) {
        return commentService.publishComment(publishCommentReqVO);
    }

    @PostMapping("/list")
    @ApiOperationLog(description = "评论分页查询")
    public PageResponse<FindCommentItemRspVO> findCommentPageList(@Validated @RequestBody FindCommentPageListReqVO findCommentPageListReqVO) {
        return commentService.findCommentPageList(findCommentPageListReqVO);
    }

    @PostMapping("/child/list")
    @ApiOperationLog(description = "二级评论分页查询")
    public PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(@Validated @RequestBody FindChildCommentPageListReqVO findChildCommentPageListReqVO) {
        return commentService.findChildCommentPageList(findChildCommentPageListReqVO);
    }


    @PostMapping("/like")
    @ApiOperationLog(description = "评论点赞")
    public Response<?> likeComment(@Validated @RequestBody LikeCommentReqVO likeCommentReqVO) {
        return commentService.likeComment(likeCommentReqVO);
    }

    @PostMapping("/unlike")
    @ApiOperationLog(description = "评论取消点赞")
    public Response<?> unlikeComment(@Validated @RequestBody UnLikeCommentReqVO unLikeCommentReqVO) {
        return commentService.unlikeComment(unLikeCommentReqVO);
    }

    @PostMapping("/delete")
    @ApiOperationLog(description = "删除评论")
    public Response<?> deleteComment(@Validated @RequestBody DeleteCommentReqVO deleteCommentReqVO) {
        return commentService.deleteComment(deleteCommentReqVO);
    }

}
