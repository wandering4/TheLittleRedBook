package com.haishi.LittleRedBook.comment.biz.service.impl;

import com.google.common.base.Preconditions;
import com.haishi.LittleRedBook.comment.biz.constant.MQConstants;
import com.haishi.LittleRedBook.comment.biz.model.dto.PublishCommentMqDTO;
import com.haishi.LittleRedBook.comment.biz.model.vo.request.PublishCommentReqVO;
import com.haishi.LittleRedBook.comment.biz.retry.SendMqRetryHelper;
import com.haishi.LittleRedBook.comment.biz.service.CommentService;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.response.Response;
import com.haishi.framework.commons.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @version: v1.0.0
 * @description: 评论业务
 **/
@Service
@Slf4j
public class CommentServiceImpl implements CommentService {


    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private SendMqRetryHelper sendMqRetryHelper;

    /**
     * 发布评论
     *
     * @param publishCommentReqVO
     * @return
     */
    @Override
    public Response<?> publishComment(PublishCommentReqVO publishCommentReqVO) {
        // 评论正文
        String content = publishCommentReqVO.getContent();
        // 附近图片
        String imageUrl = publishCommentReqVO.getImageUrl();

        // 评论内容和图片不能同时为空
        Preconditions.checkArgument(StringUtils.isNotBlank(content) || StringUtils.isNotBlank(imageUrl),
                "评论正文和图片不能同时为空");

        // 发布者 ID
        Long creatorId = LoginUserContextHolder.getUserId();

        // 发送 MQ
        // 构建消息体 DTO
        PublishCommentMqDTO publishCommentMqDTO = PublishCommentMqDTO.builder()
                .noteId(publishCommentReqVO.getNoteId())
                .content(content)
                .imageUrl(imageUrl)
                .replyCommentId(publishCommentReqVO.getReplyCommentId())
                .createTime(LocalDateTime.now())
                .creatorId(creatorId)
                .build();

        // 发送 MQ (包含重试机制)
        sendMqRetryHelper.asyncSend(MQConstants.TOPIC_PUBLISH_COMMENT, JsonUtils.toJsonString(publishCommentMqDTO));


        return Response.success();
    }
}