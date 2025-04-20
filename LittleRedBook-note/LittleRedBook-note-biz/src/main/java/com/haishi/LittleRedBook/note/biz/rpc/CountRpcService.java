package com.haishi.LittleRedBook.note.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.haishi.LittleRedBook.count.api.CountFeignApi;
import com.haishi.LittleRedBook.count.dto.request.FindNoteCountsByIdsReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindNoteCountsByIdRspDTO;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @date: 2024/4/20 23:02
 * @version: v1.0.0
 * @description: 计数服务
 **/
@Slf4j
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;

    /**
     * 批量查询笔记计数
     *
     * @param noteIds
     * @return
     */
    public List<FindNoteCountsByIdRspDTO> findByNoteIds(List<Long> noteIds) {
        if (CollUtil.isEmpty(noteIds)) {
            return Lists.newArrayList();
        }

        FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO = new FindNoteCountsByIdsReqDTO();
        findNoteCountsByIdsReqDTO.setNoteIds(noteIds);

        Response<List<FindNoteCountsByIdRspDTO>> response = countFeignApi.findNotesCount(findNoteCountsByIdsReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData())) {
            log.error("计数服务-用户计数接口调用失败");
            return Lists.newArrayList();
        }

        if (CollUtil.isEmpty(response.getData())) {
            return Lists.newArrayList();
        }

        return response.getData();
    }

}
