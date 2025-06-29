package com.haishi.LittleRedBook.count.biz.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.haishi.LittleRedBook.count.biz.enums.ResponseCodeEnum;
import com.haishi.LittleRedBook.count.biz.service.NoteCountService;
import com.haishi.LittleRedBook.count.dto.request.FindNoteCountsByIdsReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindNoteCountsByIdRspDTO;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @date: 2024/4/20 13:22
 * @version: v1.0.0
 * @description: 用户维度计数
 **/
@RestController
@RequestMapping("/count")
@Slf4j
public class NoteCountController {

    @Resource
    private NoteCountService noteCountService;

    @PostMapping(value = "/notes/data")
    @ApiOperationLog(description = "批量获取笔记计数数据")
    @SentinelResource(value = "findNotesCountData4Controller", blockHandler = "blockHandler4findNotesCountData")
    public Response<List<FindNoteCountsByIdRspDTO>> findNotesCountData(@Validated @RequestBody FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO) {
        return noteCountService.findNotesCountData(findNoteCountsByIdsReqDTO);
    }

    /**
     * blockHandler 函数，原方法调用被限流/降级/系统保护的时候调用
     * 注意, 需要包含限流方法的所有参数，和 BlockException 参数
     *
     * @param findNoteCountsByIdsReqDTO
     * @param blockException
     * @return
     */
    public Response<List<FindNoteCountsByIdRspDTO>> blockHandler4findNotesCountData(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO, BlockException blockException) {
        throw new BizException(ResponseCodeEnum.FLOW_LIMIT);
    }

}