package com.haishi.LittleRedBook.note.biz.service;

import com.haishi.LittleRedBook.note.biz.model.vo.request.*;
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

    /**
     * 删除本地笔记缓存
     * @param noteId
     */
    void deleteNoteLocalCache(Long noteId);

    /**
     * 删除笔记
     * @param deleteNoteRequest
     * @return
     */
    Response<?> deleteNote(DeleteNoteRequest deleteNoteRequest);

    /**
     * 笔记仅对自己可见
     * @param updateNoteVisibleOnlyMeRequest
     * @return
     */
    Response<?> visibleOnlyMe(UpdateNoteVisibleOnlyMeRequest updateNoteVisibleOnlyMeRequest);

    /**
     * 笔记置顶 / 取消置顶
     * @param topNoteRequest
     * @return
     */
    Response<?> topNote(TopNoteRequest topNoteRequest);


    /**
     * 点赞笔记
     * @param likeNoteRequest
     * @return
     */
    Response<?> likeNote(LikeNoteRequest likeNoteRequest);


    /**
     * 取消点赞笔记
     * @param unlikeNoteRequest
     * @return
     */
    Response<?> unlikeNote(UnlikeNoteRequest unlikeNoteRequest);

    /**
     * 收藏笔记
     * @param collectNoteReqVO
     * @return
     */
    Response<?> collectNote(CollectNoteReqVO collectNoteReqVO);

    /**
     * 取消收藏笔记
     * @param unCollectNoteReqVO
     * @return
     */
    Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO);
}