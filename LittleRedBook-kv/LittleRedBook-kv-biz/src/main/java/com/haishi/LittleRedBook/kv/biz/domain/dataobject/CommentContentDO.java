package com.haishi.LittleRedBook.kv.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

/**
 * @version: v1.0.0
 * @description: 评论内容
 **/
@Table("comment_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentContentDO {

    @PrimaryKey
    private CommentContentPrimaryKey primaryKey;

    private String content;
}
