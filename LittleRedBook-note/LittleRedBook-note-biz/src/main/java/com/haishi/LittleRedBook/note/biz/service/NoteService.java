package com.haishi.LittleRedBook.note.biz.service;

import com.haishi.LittleRedBook.note.biz.model.vo.request.FindNoteDetailRequest;
import com.haishi.LittleRedBook.note.biz.model.vo.request.PublishNoteRequest;
import com.haishi.LittleRedBook.note.biz.model.vo.request.UpdateNoteRequest;
import com.haishi.LittleRedBook.note.biz.model.vo.response.FindNoteDetailResponse;
import com.haishi.framework.commons.response.Response;

public interface NoteService {

    /**
     * 笔记发布
     * @param publishNoteRequest
     * @return
     */
    Response<?> publishNote(PublishNoteRequest publishNoteRequest);

    /**
     * 笔记详情
     * @param findNoteDetailRequest
     * @return
     */
    Response<FindNoteDetailResponse> findNoteDetail(FindNoteDetailRequest findNoteDetailRequest);

    /**
     * 笔记更新
     * @param updateNoteRequest
     * @return
     */
    Response<?> updateNote(UpdateNoteRequest updateNoteRequest);

}