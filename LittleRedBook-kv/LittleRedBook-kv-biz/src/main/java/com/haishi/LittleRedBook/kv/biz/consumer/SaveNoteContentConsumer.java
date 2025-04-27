package com.haishi.LittleRedBook.kv.biz.consumer;

import com.haishi.LittleRedBook.kv.biz.constant.MQConstants;
import com.haishi.LittleRedBook.kv.biz.model.dto.PublishNoteDTO;
import com.haishi.LittleRedBook.kv.biz.service.NoteContentService;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentRequest;
import com.haishi.framework.commons.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(consumerGroup = "LittleRedBook_group_" + MQConstants.TOPIC_PUBLISH_NOTE_TRANSACTION, // Group 组
        topic = MQConstants.TOPIC_PUBLISH_NOTE_TRANSACTION // 消费的主题 Topic
)
@Slf4j
public class SaveNoteContentConsumer implements RocketMQListener<Message> {

    @Resource
    private NoteContentService noteContentService;

    @Override
    public void onMessage(Message message) {
        // 消息体
        String bodyJsonStr = new String(message.getBody());

        log.info("## SaveNoteContentConsumer 消费了事务消息 {}", bodyJsonStr);

        // 笔记正文保存到 Cassandra 中
        if (StringUtils.isNotBlank(bodyJsonStr)) {
            PublishNoteDTO publishNoteDTO = JsonUtils.parseObject(bodyJsonStr, PublishNoteDTO.class);
            String content = publishNoteDTO.getContent();
            String uuid = publishNoteDTO.getContentUuid();


            noteContentService.addNoteContent(AddNoteContentRequest.builder()
                    .uuid(uuid)
                    .content(content)
                    .build());
        }
    }

}