package com.haishi.LittleRedBook.user.relation.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FollowingDO {

    private Long id;

    private Long userId;

    /**
     * 关注   id
     */
    private Long followingUserId;

    private LocalDateTime createTime;

}