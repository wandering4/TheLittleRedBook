package com.haishi.LittleRedBook.user.relation.biz.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询关注列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFollowingUserResponse {

    private Long userId;

    private String avatar;

    private String nickname;

    private String introduction;

}
