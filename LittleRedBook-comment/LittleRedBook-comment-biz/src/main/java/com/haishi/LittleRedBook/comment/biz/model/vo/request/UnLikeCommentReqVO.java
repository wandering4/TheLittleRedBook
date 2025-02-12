package com.haishi.LittleRedBook.comment.biz.model.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version: v1.0.0
 * @description: 取消评论点赞
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnLikeCommentReqVO {

    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;

}