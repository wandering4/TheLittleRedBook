package com.haishi.LittleRedBook.user.relation.biz.service.impl;

import com.haishi.LittleRedBook.user.dto.resp.FindUserByIdResponse;
import com.haishi.LittleRedBook.user.relation.biz.enums.ResponseCodeEnum;
import com.haishi.LittleRedBook.user.relation.biz.model.vo.request.FollowUserReqVO;
import com.haishi.LittleRedBook.user.relation.biz.rpc.UserRpcService;
import com.haishi.LittleRedBook.user.relation.biz.service.RelationService;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;

import java.util.Objects;

public class RelationServiceImpl implements RelationService {

    @Resource
    UserRpcService userRpcService;


    /**
     * 关注用户
     *
     * @param request
     * @return
     */
    @Override
    public Response<?> follow(FollowUserReqVO request) {
        // 关注的用户 ID
        Long followUserId = request.getFollowUserId();

        //当前登录用户ID
        Long userId = LoginUserContextHolder.getUserId();

        checkFollowSelf(followUserId, userId);

        //校验关注的用户是否存在
        FindUserByIdResponse followUser = userRpcService.findById(followUserId);
        if(Objects.isNull(followUser)) {
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        // TODO: 校验关注数是否已经达到上限

        // TODO: 写入 Redis ZSET 关注列表

        // TODO: 发送 MQ

        return Response.success();
    }

    /**
     * 校验：无法关注自己
     *
     * @param followUserId
     * @param userId
     */
    private void checkFollowSelf(Long followUserId, Long userId) {
        if (Objects.equals(userId, followUserId)) {
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }
    }
}
