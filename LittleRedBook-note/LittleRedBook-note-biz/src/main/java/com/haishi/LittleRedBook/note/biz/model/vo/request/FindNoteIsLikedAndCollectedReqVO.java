package com.haishi.LittleRedBook.note.biz.model.vo.request;

import jakarta.validation.constraints.NotNull;
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
public class FindNoteIsLikedAndCollectedReqVO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long noteId;

}