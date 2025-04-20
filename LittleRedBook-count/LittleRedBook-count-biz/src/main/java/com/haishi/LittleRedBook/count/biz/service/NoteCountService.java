package com.haishi.LittleRedBook.count.biz.service;

import com.haishi.LittleRedBook.count.dto.request.FindNoteCountsByIdsReqDTO;
import com.haishi.LittleRedBook.count.dto.response.FindNoteCountsByIdRspDTO;
import com.haishi.framework.commons.response.Response;

import java.util.List;

/**
 * @date: 2024/4/20 15:41
 * @version: v1.0.0
 * @description: 笔记计数业务
 **/
public interface NoteCountService {

    /**
     * 批量查询笔记计数
     * @param findNoteCountsByIdsReqDTO
     * @return
     */
    Response<List<FindNoteCountsByIdRspDTO>> findNotesCountData(FindNoteCountsByIdsReqDTO findNoteCountsByIdsReqDTO);
}
