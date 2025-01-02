package com.haishi.LittleRedBook.user.biz.service.impl;

import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.haishi.LittleRedBook.oss.api.FileFeignApi;
import com.haishi.LittleRedBook.user.biz.domain.dataobject.UserDO;
import com.haishi.LittleRedBook.user.biz.domain.mapper.UserDOMapper;
import com.haishi.LittleRedBook.user.biz.enums.ResponseCodeEnum;
import com.haishi.LittleRedBook.user.biz.enums.SexEnum;
import com.haishi.LittleRedBook.user.biz.model.vo.UpdateUserInfoReqVO;
import com.haishi.LittleRedBook.user.biz.service.UserService;
import com.haishi.framework.biz.context.holder.LoginUserContextHolder;
import com.haishi.framework.commons.response.Response;
import com.haishi.framework.commons.util.ParamUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @description 用户业务
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private FileFeignApi fileFeignApi;

    /**
     * 更新用户信息
     *
     * @param updateUserInfoReqVO
     * @return
     */
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        UserDO userDO = new UserDO();
        //获取当前用户id
        userDO.setId(LoginUserContextHolder.getUserId());

        boolean needUpdate=false;

        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();
        if(ObjectUtils.isNotEmpty(avatarFile)){
            //调用对象存储服务上传文件
            fileFeignApi.test();
            //TODO
        }

        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小红书账号
        String accountId = updateUserInfoReqVO.getAccountId();
        if (StringUtils.isNotBlank(accountId)) {
            Preconditions.checkArgument(ParamUtils.checkAccountId(accountId), ResponseCodeEnum.ACCOUNT_ID_VALID_FAIL.getErrorMessage());
            userDO.setAccountId(accountId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            // todo: 调用对象存储服务上传文件
        }

        if (needUpdate) {
            // 更新用户信息
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }
        return Response.success();


    }
}
