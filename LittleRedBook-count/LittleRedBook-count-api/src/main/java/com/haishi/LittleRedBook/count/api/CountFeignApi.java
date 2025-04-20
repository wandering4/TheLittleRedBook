package com.haishi.LittleRedBook.count.api;

import com.haishi.LittleRedBook.count.constant.ApiConstants;
import com.haishi.LittleRedBook.count.dto.request.FindNoteCountsByIdsReqDTO;
import com.haishi.LittleRedBook.count.dto.request.FindUserCountsByIdReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindNoteCountsByIdRspDTO;
import com.haishi.LittleRedBook.count.dto.response.FindUserCountsByIdRspDTO;
import com.haishi.framework.commons.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @date: 2024/4/13 22:56
 * @version: v1.0.0
 * @description: 计数服务 Feign 接口
 **/
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CountFeignApi {

    String PREFIX = "/count";

    /**
     * 查询用户计数
     *
     * @param findUserCountsByIdReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/user/data")
    Response<FindUserCountsByIdRspDTO> findUserCount(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);


    /**
     * 批量查询笔记计数
     *
     * @param findNoteCountsByIdsReqDTO
     * @return
     */
    @PostMapping(value = PREFIX + "/notes/data")
    Response<List<FindNoteCountsByIdRspDTO>> findNotesCount(@RequestBody FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO);


}

