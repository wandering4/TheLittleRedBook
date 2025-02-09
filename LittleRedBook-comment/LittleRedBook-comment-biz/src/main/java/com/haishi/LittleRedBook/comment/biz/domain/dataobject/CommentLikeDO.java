package com.haishi.LittleRedBook.comment.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeDO {
    private Long id;

    private Long userId;

    private Long commentId;

    private LocalDateTime createTime;
}