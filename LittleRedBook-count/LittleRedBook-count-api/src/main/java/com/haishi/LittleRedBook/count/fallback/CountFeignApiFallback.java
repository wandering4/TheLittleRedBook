package com.haishi.LittleRedBook.count.fallback;

import com.google.common.collect.Lists;
import com.haishi.LittleRedBook.count.api.CountFeignApi;
import com.haishi.LittleRedBook.count.dto.request.FindNoteCountsByIdsReqDTO;
import com.haishi.LittleRedBook.count.dto.request.FindUserCountsByIdReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindNoteCountsByIdRspDTO;
import com.haishi.LittleRedBook.count.dto.response.FindUserCountsByIdRspDTO;
import com.haishi.framework.commons.response.Response;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 降级处理
 */
@Component
public class CountFeignApiFallback implements CountFeignApi {

    /**
     * 查询用户计数降级
     *
     * @param findUserCountsByIdReqDTO
     * @return
     */
    @Override
    public Response<FindUserCountsByIdRspDTO> findUserCount(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO) {
        // 要查询的用户 ID
        Long userId = findUserCountsByIdReqDTO.getUserId();

        // 降级后，所有计数默认为 0
        return Response.success(FindUserCountsByIdRspDTO.builder()
                .userId(userId)
                .noteTotal(0L)
                .likeTotal(0L)
                .followingTotal(0L)
                .fansTotal(0L)
                .collectTotal(0L)
                .build());
    }

    /**
     * 批量查询笔记计数降级
     *
     * @param findNoteCountsByIdsReqDTO
     * @return
     */
    @Override
    public Response<List<FindNoteCountsByIdRspDTO>> findNotesCount(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO) {
        List<FindNoteCountsByIdRspDTO> findNoteCountsByIdRspDTOS = Lists.newArrayList();

        List<Long> noteIds = findNoteCountsByIdsReqDTO.getNoteIds();

        noteIds.forEach(noteId ->
                findNoteCountsByIdRspDTOS.add(FindNoteCountsByIdRspDTO.builder()
                        .noteId(noteId)
                        .collectTotal(0L)
                        .commentTotal(0L)
                        .likeTotal(0L)
                        .build())
        );

        return Response.success(findNoteCountsByIdRspDTOS);
    }

}