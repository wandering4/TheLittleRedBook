package com.haishi.LittleRedBook.note.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NoteDO {
    private Long id;

    private String title;

    /**
     * 内容是否为空
     */
    private Boolean isContentEmpty;

    private Long creatorId;

    private Long topicId;

    private String topicName;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 笔记类型: 0 代表图文笔记，1 代表视频笔记
     */
    private Integer type;

    private String imgUris;

    private String videoUri;

    /**
     * 可见范围: 0公开 1私有
     */
    private Integer visible;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer status;

    private String contentUuid;

}