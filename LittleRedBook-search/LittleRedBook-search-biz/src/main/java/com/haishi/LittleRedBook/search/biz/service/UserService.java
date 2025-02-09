package com.haishi.LittleRedBook.search.biz.service;

import com.haishi.LittleRedBook.search.biz.model.vo.request.SearchUserReqVO;
import com.haishi.LittleRedBook.search.biz.model.vo.response.SearchUserRspVO;
import com.haishi.LittleRedBook.search.dto.RebuildUserDocumentReqDTO;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;

public interface UserService {

    /**
     * 搜索用户
     * @param searchUserReqVO
     * @return
     */
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);




    /**
     * 重建用户文档
     * @param rebuildUserDocumentReqDTO
     * @return
     */
    Response<Long> rebuildDocument(RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);


}