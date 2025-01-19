package com.haishi.LittleRedBook.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishNoteReqVO {
    /**
     * 笔记类型: 0 代表图文笔记，1 代表视频笔记
     * @see com.haishi.LittleRedBook.note.biz.enums.NoteTypeEnum
     */
    @NotNull(message = "笔记类型不能为空")
    private Integer type;

    /**
     * 图片链接数组，当为图文笔记时，此字段不能为空
     */
    private List<String> imgUris;

    /**
     * 视频连接，当为视频笔记时，此字段不能为空
     */
    private String videoUri;

    /**
     * 笔记标题
     */
    private String title;

    /**
     * 笔记内容（可不填）
     */
    private String content;

    /**
     * 话题 ID（可不填）
     */
    private Long topicId;

}
