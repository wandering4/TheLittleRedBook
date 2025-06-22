package com.haishi.LittleRedBook.count.biz.enums;

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
    SYSTEM_ERROR("COUNT-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("COUNT-10001", "参数错误"),
    FLOW_LIMIT("COUNT-10002", "操作过于频繁"),

    // ----------- 业务异常状态码 -----------
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}
