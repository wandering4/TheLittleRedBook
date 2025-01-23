package com.haishi.LittleRedBook.user.relation.biz.service;

import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FollowUserReqVO;
import com.haishi.framework.commons.response.Response;

public interface RelationService {

    /**
     * 关注用户
     * @param request
     * @return
     */
    Response<?> follow(FollowUserReqVO request);
}
