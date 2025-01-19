package com.haishi.LittleRedBook.note.biz.service;

import com.haishi.LittleRedBook.note.biz.model.vo.PublishNoteReqVO;
import com.haishi.framework.commons.response.Response;

public interface NoteService {

    /**
     * 笔记发布
     * @param publishNoteReqVO
     * @return
     */
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);

}