package com.haishi.LittleRedBook.count.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @date: 2024/4/20 15:17
 * @version: v1.0.0
 * @description: 批量查询笔记维度相关计数
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteCountsByIdsReqDTO {

    @NotNull(message = "笔记 ID 集合不能为空")
    @Size(min = 1, max = 20, message = "笔记 ID 集合大小必须大于等于 1, 小于等于 20")
    private List<Long> noteIds;

}