package com.haishi.LittleRedBook.count.biz.service;

import com.haishi.LittleRedBook.count.dto.request.FindUserCountsByIdReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindUserCountsByIdRspDTO;
import com.haishi.framework.commons.response.Response;

/**
 * @date: 2024/4/7 15:41
 * @version: v1.0.0
 * @description: 用户计数业务
 **/
public interface UserCountService {

    /**
     * 查询用户相关计数
     * @param findUserCountsByIdReqDTO
     * @return
     */
    Response<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);
}
