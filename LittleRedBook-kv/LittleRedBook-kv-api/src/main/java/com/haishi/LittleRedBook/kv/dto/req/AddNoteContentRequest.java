package com.haishi.LittleRedBook.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddNoteContentRequest {

    @NotBlank(message = "笔记内容 UUID 不能为空")
    private String uuid;

    @NotNull(message = "笔记内容不能为空")
    private String content;

}