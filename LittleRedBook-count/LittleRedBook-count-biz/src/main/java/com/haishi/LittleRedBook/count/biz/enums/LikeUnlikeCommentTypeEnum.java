package com.haishi.LittleRedBook.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @description: 评论点赞、取消点赞 Type
 **/
@Getter
@AllArgsConstructor
public enum LikeUnlikeCommentTypeEnum {
    // 点赞
    LIKE(1),
    // 取消点赞
    UNLIKE(0),
    ;

    private final Integer code;

    public static LikeUnlikeCommentTypeEnum valueOf(Integer code) {
        for (LikeUnlikeCommentTypeEnum likeUnlikeCommentTypeEnum : LikeUnlikeCommentTypeEnum.values()) {
            if (Objects.equals(code, likeUnlikeCommentTypeEnum.getCode())) {
                return likeUnlikeCommentTypeEnum;
            }
        }
        return null;
    }
}