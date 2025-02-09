package com.haishi.LittleRedBook.data.align.rpc;

import com.haishi.LittleRedBook.search.api.SearchFeignApi;
import com.haishi.LittleRedBook.search.dto.RebuildNoteDocumentReqDTO;
import com.haishi.LittleRedBook.search.dto.RebuildUserDocumentReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * @version: v1.0.0
 * @description: 搜索服务
 **/
@Component
public class SearchRpcService {

    @Resource
    private SearchFeignApi searchFeignApi;

    /**
     * 调用重建笔记文档接口
     * @param noteId
     */
    public void rebuildNoteDocument(Long noteId) {
        RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO = RebuildNoteDocumentReqDTO.builder()
                .id(noteId)
                .build();

        searchFeignApi.rebuildNoteDocument(rebuildNoteDocumentReqDTO);
    }

    /**
     * 调用重建用户文档接口
     * @param userId
     */
    public void rebuildUserDocument(Long userId) {
        RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO = RebuildUserDocumentReqDTO.builder()
                .id(userId)
                .build();

        searchFeignApi.rebuildUserDocument(rebuildUserDocumentReqDTO);
    }

}
