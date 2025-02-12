package com.haishi.LittleRedBook.comment.biz.enums;

import com.haishi.framework.commons.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @description: 响应异常码
 **/
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("COMMENT-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("COMMENT-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    PARENT_COMMENT_NOT_FOUND("COMMENT-20000", "此父评论不存在"),
    COMMENT_NOT_FOUND("COMMENT-20001", "此评论不存在"),

    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}