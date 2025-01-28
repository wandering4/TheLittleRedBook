package com.haishi.LittleRedBook.user.relation.biz.model.vo.request;

import jakarta.validation.constraints.NotNull;
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
public class FindFollowingListRequest {

    @NotNull(message = "查询用户 ID 不能为空")
    private Long userId;

    @NotNull(message = "页码不能为空")
    private Integer pageNo = 1; // 默认值为第一页

    private Integer pageSize = 10;
}
