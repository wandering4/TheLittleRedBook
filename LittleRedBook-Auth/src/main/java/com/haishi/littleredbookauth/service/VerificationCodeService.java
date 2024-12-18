package com.haishi.littleredbookauth.service;


import com.haishi.framework.commons.response.Response;
import com.haishi.littleredbookauth.model.vo.veriticationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {
    /**
     * 发送短信验证码
     *
     * @param sendVerificationCodeReqVO
     * @return
     */
    Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
