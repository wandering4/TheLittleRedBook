package com.haishi.LittleRedBook.user.relation.biz.controller;

import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FindFansListRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FindFollowingListRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FollowUserRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.UnfollowUserRequest;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.response.FindFansUserResponse;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.response.FindFollowingUserResponse;
import com.haishi.LittleRedBook.user.relation.biz.service.RelationService;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.response.PageResponse;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relation")
@Slf4j
public class RelationController {

    @Resource
    private RelationService relationService;

    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> follow(@Validated @RequestBody FollowUserRequest followUserRequest) {
        return relationService.follow(followUserRequest);
    }

    @PostMapping("/unfollow")
    @ApiOperationLog(description = "取关用户")
    public Response<?> unfollow(@Validated @RequestBody UnfollowUserRequest unfollowUserRequest) {
        return relationService.unfollow(unfollowUserRequest);
    }

    @PostMapping("/following/list")
    @ApiOperationLog(description = "查询用户关注列表")
    public PageResponse<FindFollowingUserResponse> findFollowingList(@Validated @RequestBody FindFollowingListRequest findFollowingListRequest) {
        return relationService.findFollowingList(findFollowingListRequest);
    }

    @PostMapping("/fans/list")
    @ApiOperationLog(description = "查询用户粉丝列表")
    public PageResponse<FindFansUserResponse> findFansList(@Validated @RequestBody FindFansListRequest findFansListRequest) {
        return relationService.findFansList(findFansListRequest);
    }

}
