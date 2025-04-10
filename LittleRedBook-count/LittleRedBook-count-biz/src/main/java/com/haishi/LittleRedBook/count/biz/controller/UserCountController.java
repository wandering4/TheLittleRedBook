package com.haishi.LittleRedBook.count.biz.controller;

import com.haishi.LittleRedBook.count.biz.service.UserCountService;
import com.haishi.LittleRedBook.count.dto.request.FindUserCountsByIdReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindUserCountsByIdRspDTO;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @date: 2024/4/4 13:22
 * @version: v1.0.0
 * @description: 用户维度计数
 **/
@RestController
@RequestMapping("/count")
@Slf4j
public class UserCountController {

    @Resource
    private UserCountService userCountService;


    @PostMapping(value = "/user/data")
    @ApiOperationLog(description = "获取用户计数数据")
    public Response<FindUserCountsByIdRspDTO> findUserCountData(@Validated @RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) {
        return userCountService.findUserCountData(findUserCountsByIdReqDTO);
    }

}