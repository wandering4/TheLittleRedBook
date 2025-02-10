package com.haishi.LittleRedBook.comment.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 评论级别
 **/
@Getter
@AllArgsConstructor
public enum CommentLevelEnum {
    // 一级评论
    ONE(1),
    // 二级评论
    TWO(2),
    ;

    private final Integer code;

}
