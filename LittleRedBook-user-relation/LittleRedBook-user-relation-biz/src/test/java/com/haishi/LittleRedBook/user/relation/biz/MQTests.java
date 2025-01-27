package com.haishi.LittleRedBook.user.relation.biz;

import com.haishi.LittleRedBook.user.relation.biz.constant.MQConstants;
import com.haishi.LittleRedBook.user.relation.biz.model.dto.FollowUserMqDTO;
import com.haishi.framework.commons.util.JsonUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Slf4j
class MQTests {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 测试：发送一万条 MQ
     */
    @Test
    void testBatchSendMQ() {
//        for (long i = 0; i < 10000; i++) {
//            // 构建消息体 DTO
//            FollowUserMqDTO followUserMqDTO = FollowUserMqDTO.builder()
//                    .userId(i)
//                    .followUserId(i)
//                    .createTime(LocalDateTime.now())
//                    .build();
//
//            // 构建消息对象，并将 DTO 转成 Json 字符串设置到消息体中
//            Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO))
//                    .build();
//
//            // 通过冒号连接, 可让 MQ 发送给主题 Topic 时，携带上标签 Tag
//            String destination = MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW + ":" + MQConstants.TAG_FOLLOW;
//
//            log.info("==> 开始发送关注操作 MQ, 消息体: {}", followUserMqDTO);
//
//            // 异步发送 MQ 消息，提升接口响应速度
//            rocketMQTemplate.asyncSend(destination, message, new SendCallback() {
//                @Override
//                public void onSuccess(SendResult sendResult) {
//                    log.info("==> MQ 发送成功，SendResult: {}", sendResult);
//                }
//
//                @Override
//                public void onException(Throwable throwable) {
//                    log.error("==> MQ 发送异常: ", throwable);
//                }
//            });
//        }
    }

    @Test
    public void testReceive(){
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("LittleRedBook_relation_group");

        // 设置 NameServer 地址
        consumer.setNamesrvAddr("127.0.0.1:9876");

        // 设置消费模式（集群模式）
        consumer.setMessageModel(MessageModel.BROADCASTING);

        // 设置从哪里开始消费（比如从队列头部开始消费）
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);

        // 订阅 Topic
        try {
            consumer.subscribe(MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW, "*"); // 订阅指定的 Topic（可以指定 tags）

            // 注册消息监听器
            consumer.registerMessageListener((List<MessageExt> msgs, ConsumeConcurrentlyContext context) -> {
                for (MessageExt msg : msgs) {
                    String messageBody = new String(msg.getBody());
                    log.info("收到消息：{}", messageBody);  // 打印消息内容
                }
                // 返回消费状态
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS; // 消费成功
            });

            // 启动消费者
            consumer.start();
            log.info("消费者启动成功，正在拉取消息...");

            // 等待几秒钟以便接收消息
            Thread.sleep(5000);  // 等待5秒，可以根据实际情况调整

            // 停止消费者
            consumer.shutdown();
            log.info("消费者停止。");

        } catch (Exception e) {
            log.error("消费者启动失败", e);
        }
    }

}

