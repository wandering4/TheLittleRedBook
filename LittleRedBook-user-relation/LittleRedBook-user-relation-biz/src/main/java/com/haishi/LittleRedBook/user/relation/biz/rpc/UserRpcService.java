package com.haishi.LittleRedBook.user.relation.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.haishi.LittleRedBook.user.api.UserFeignApi;
import com.haishi.LittleRedBook.user.dto.req.FindUserByIdRequest;
import com.haishi.LittleRedBook.user.dto.req.FindUsersByIdsReqDTO;
import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
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

    /**
     * 批量查询用户信息
     *
     * @param userIds
     * @return
     */
    public List<FindUserByIdResponse> findByIds(List<Long> userIds) {
        FindUsersByIdsReqDTO findUsersByIdsReqDTO = new FindUsersByIdsReqDTO();
        findUsersByIdsReqDTO.setIds(userIds);

        Response<List<FindUserByIdResponse>> response = userFeignApi.findByIds(findUsersByIdsReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }

}

