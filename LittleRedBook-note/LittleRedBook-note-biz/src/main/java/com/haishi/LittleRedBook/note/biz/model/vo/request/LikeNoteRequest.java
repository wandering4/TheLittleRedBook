package com.haishi.LittleRedBook.note.biz.model.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeNoteRequest {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;

}
