package com.haishi.LittleRedBook.comment.biz.controller;

import com.haishi.LittleRedBook.comment.biz.model.vo.request.PublishCommentReqVO;
import com.haishi.LittleRedBook.comment.biz.service.CommentService;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
