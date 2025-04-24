package com.haishi.LittleRedBook.note.biz.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;

/**
 * @version: v1.0.0
 * @description: 事务消息：笔记发布入库
 **/
@RocketMQTransactionListener
@Slf4j
public class PublishNote2DBLocalTransactionListener implements RocketMQLocalTransactionListener {

    /**
     * 执行本地事务
     *
     * @param msg
     * @param arg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        // 1. 解析消息内容
        String payload = new String((byte[]) msg.getPayload());
        log.info("## 事务消息: 开始执行本地事务：{}", payload);

        // TODO 2. 执行本地事务（如数据库操作）

        // TODO 3. 返回事务状态
        return RocketMQLocalTransactionState.COMMIT;
    }

    /**
     * 事务状态回查（由 Broker 主动调用）
     *
     * @param msg
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        // 1. 解析消息内容
        String payload = new String((byte[]) msg.getPayload());
        log.info("## 事务消息: 开始事务回查：{}", payload);

        // TODO 2. 检查本地事务状态

        // TODO 3. 返回最终状态
        return RocketMQLocalTransactionState.COMMIT;
    }

}

