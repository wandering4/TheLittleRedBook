package com.haishi.LittleRedBook.user.biz.model.vo.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date: 2024/4/7 15:17
 * @version: v1.0.0
 * @description: 获取用户主页信息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserProfileReqVO {

    /**
     * 用户 ID
     */
    private Long userId;

}
