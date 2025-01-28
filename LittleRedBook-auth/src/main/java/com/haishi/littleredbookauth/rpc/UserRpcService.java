package com.haishi.littleredbookauth.rpc;

import com.haishi.LittleRedBook.user.api.UserFeignApi;
import com.haishi.LittleRedBook.user.dto.req.FindUserByPhoneRequest;
import com.haishi.LittleRedBook.user.dto.req.RegisterUserRequest;
import com.haishi.LittleRedBook.user.dto.req.UpdateUserPasswordRequest;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 用户注册
     *
     * @param phone
     * @return
     */
    public Long registerUser(String phone) {
        RegisterUserRequest registerUserRequest = new RegisterUserRequest();
        registerUserRequest.setPhone(phone);

        Response<Long> response = userFeignApi.registerUser(registerUserRequest);

        if (!response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

    /**
     * 根据手机号查询用户信息
     *
     * @param phone
     * @return
     */
    public FindUserByPhoneRspDTO findUserByPhone(String phone) {
        FindUserByPhoneRequest findUserByPhoneRequest = new FindUserByPhoneRequest();
        findUserByPhoneRequest.setPhone(phone);

        Response<FindUserByPhoneRspDTO> response = userFeignApi.findByPhone(findUserByPhoneRequest);

        if (!response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

    /**
     * 密码更新
     *
     * @param encodePassword
     */
    public void updatePassword(String encodePassword) {
        UpdateUserPasswordRequest updateUserPasswordRequest = new UpdateUserPasswordRequest();
        updateUserPasswordRequest.setEncodePassword(encodePassword);

        userFeignApi.updatePassword(updateUserPasswordRequest);
    }

}
