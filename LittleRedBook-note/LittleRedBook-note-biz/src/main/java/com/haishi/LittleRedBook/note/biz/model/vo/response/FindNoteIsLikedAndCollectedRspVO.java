package com.haishi.LittleRedBook.note.biz.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @date: 2024/4/7 15:17
 * @version: v1.0.0
 * @description: 获取笔记是否点赞、收藏数据
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteIsLikedAndCollectedRspVO {

    /**
     * 笔记 ID
     */
    private Long noteId;

    /**
     * 是否被当前登录的用户点赞
     */
    private Boolean isLiked;

    /**
     * 是否被当前登录的用户收藏
     */
    private Boolean isCollected;

}
