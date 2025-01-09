package com.haishi.LittleRedBook.kv.biz.service;

import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.DeleteNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.FindNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.resp.FindNoteContentResponse;
import com.haishi.framework.commons.response.Response;

public interface NoteContentService {

    /**
     * 添加笔记内容
     *
     * @param addNoteContentRequest
     * @return
     */
    Response<?> addNoteContent(AddNoteContentRequest addNoteContentRequest);

    /**
     * 查询笔记内容
     *
     * @param findNoteContentRequest
     * @return
     */
    Response<FindNoteContentResponse> findNoteContent(FindNoteContentRequest findNoteContentRequest);

    /**
     * 删除笔记内容
     *
     * @param deleteNoteContentReqDTO
     * @return
     */
    Response<?> deleteNoteContent(DeleteNoteContentRequest deleteNoteContentReqDTO);
}