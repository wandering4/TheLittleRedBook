package com.haishi.LittleRedBook.note.biz.model.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @version: v1.0.0
 * @description: 取消点赞笔记
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnlikeNoteRequest {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;

}