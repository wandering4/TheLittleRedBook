package com.haishi.LittleRedBook.note.biz.rpc;

import com.haishi.LittleRedBook.user.api.UserFeignApi;
import com.haishi.LittleRedBook.user.dto.req.FindUserByIdRequest;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 查询用户信息
     * @param userId
     * @return
     */
    public FindUserByIdResponse findById(Long userId) {
        FindUserByIdRequest findUserByIdRequest = new FindUserByIdRequest();
        findUserByIdRequest.setId(userId);

        Response<FindUserByIdResponse> response = userFeignApi.findById(findUserByIdRequest);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }
}
