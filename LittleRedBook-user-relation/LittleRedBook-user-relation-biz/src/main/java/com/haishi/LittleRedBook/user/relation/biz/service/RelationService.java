package com.haishi.LittleRedBook.user.relation.biz.service;

import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FindFansListRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FindFollowingListRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FollowUserRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.UnfollowUserRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.response.FindFansUserResponse;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.response.FindFollowingUserResponse;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;

public interface RelationService {

    /**
     * 关注用户
     * @param request
     * @return
     */
    Response<?> follow(FollowUserRequest request);

    /**
     * 取关用户
     * @param unfollowUserRequest
     * @return
     */
    Response<?> unfollow(UnfollowUserRequest unfollowUserRequest);

    /**
     * 查询关注列表
     * @param findFollowingListRequest
     * @return
     */
    PageResponse<FindFollowingUserResponse> findFollowingList(FindFollowingListRequest findFollowingListRequest);


    /**
     * 查询关注列表
     * @param findFansListRequest
     * @return
     */
    PageResponse<FindFansUserResponse> findFansList(FindFansListRequest findFansListRequest);

}
