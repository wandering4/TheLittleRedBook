package com.haishi.LittleRedBook.user.relation.biz.rpc;

import com.haishi.LittleRedBook.user.api.UserFeignApi;
import com.haishi.LittleRedBook.user.dto.req.FindUserByIdRequest;
import com.haishi.LittleRedBook.user.dto.req.FindUserByPhoneReqDTO;
import com.haishi.LittleRedBook.user.dto.req.RegisterUserReqDTO;
import com.haishi.LittleRedBook.user.dto.req.UpdateUserPasswordReqDTO;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByPhoneRspDTO;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    /**
     * 根据用户 ID 查询
     *
     * @param userId
     * @return
     */
    public FindUserByIdResponse findById(Long userId) {
        FindUserByIdRequest findUserByIdRequest = new FindUserByIdRequest();
        findUserByIdRequest.setId(userId);

        Response<FindUserByIdResponse> response = userFeignApi.findById(findUserByIdRequest);

        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            return null;
        }

        return response.getData();
    }

}

