package com.haishi.LittleRedBook.user.biz.rpc;

import com.haishi.LittleRedBook.count.api.CountFeignApi;
import com.haishi.LittleRedBook.count.dto.request.FindUserCountsByIdReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindUserCountsByIdRspDTO;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @date: 2024/4/13 23:29
 * @version: v1.0.0
 * @description: 计数服务
 **/
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;

    /**
     * 查询用户计数信息
     * @param userId
     * @return
     */
    public FindUserCountsByIdRspDTO findUserCountById(Long userId) {
        FindUserCountsByIdReqDTO findUserCountsByIdReqDTO = new FindUserCountsByIdReqDTO();
        findUserCountsByIdReqDTO.setUserId(userId);

        Response<FindUserCountsByIdRspDTO> response = countFeignApi.findUserCount(findUserCountsByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

}
