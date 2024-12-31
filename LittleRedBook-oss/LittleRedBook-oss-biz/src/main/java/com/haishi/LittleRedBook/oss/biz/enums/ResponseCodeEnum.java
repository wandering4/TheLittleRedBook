package com.haishi.LittleRedBook.oss.biz.enums;

import com.haishi.framework.commons.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    // ----------- 通用异常状态码 -----------
    SYSTEM_ERROR("OSS-10000", "出错啦，后台小哥正在努力修复中..."),
    PARAM_NOT_VALID("OSS-10001", "参数错误"),

    // ----------- 业务异常状态码 -----------
    PICTURE_ERROR("OSS-10002","图片异常"),
    PICTURE_TOO_BIG("OSS-10003","图片过大，无法上传"),
    
    ;

    // 异常码
    private final String errorCode;
    // 错误信息
    private final String errorMessage;

}