package com.haishi.littleredbookauth.alarm.impl;

import com.haishi.littleredbookauth.alarm.AlarmInterface;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmsAlarmHelper implements AlarmInterface {

    /**
     * 发送告警信息
     *
     * @param message
     * @return
     */
    @Override
    public boolean send(String message) {
        log.info("==> 【短信告警】：{}", message);

        // TODO

        return true;
    }
}