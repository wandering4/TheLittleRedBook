package com.haishi.LittleRedBook.kv.biz.service;

import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentReqDTO;
import com.haishi.framework.commons.response.Response;

public interface NoteContentService {

    /**
     * 添加笔记内容
     *
     * @param addNoteContentReqDTO
     * @return
     */
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

}