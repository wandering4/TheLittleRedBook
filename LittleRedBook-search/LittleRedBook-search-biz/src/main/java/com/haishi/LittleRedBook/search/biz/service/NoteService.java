package com.haishi.LittleRedBook.search.biz.service;

import com.haishi.LittleRedBook.search.biz.model.vo.request.SearchNoteReqVO;
import com.haishi.LittleRedBook.search.biz.model.vo.response.SearchNoteRspVO;
import com.haishi.LittleRedBook.search.dto.RebuildNoteDocumentReqDTO;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;

/**
 * @version: v1.0.0
 * @description: 笔记搜索业务
 **/
public interface NoteService {

    /**
     * 搜索笔记
     * @param searchNoteReqVO
     * @return
     */
    PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO);

    /**
     * 重建笔记文档
     * @param rebuildNoteDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO);
}


