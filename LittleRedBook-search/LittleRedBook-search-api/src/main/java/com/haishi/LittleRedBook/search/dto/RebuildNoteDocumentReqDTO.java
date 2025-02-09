package com.haishi.LittleRedBook.search.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @version: v1.0.0
 * @description: 重建笔记文档
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RebuildNoteDocumentReqDTO {

    @NotNull(message = "笔记 ID 不能为空")
    private Long id;

}
