package com.haishi.LittleRedBook.biz;

import com.haishi.LittleRedBook.note.biz.LittleRedBookNoteBizApplication;
import com.haishi.LittleRedBook.note.biz.service.ChannelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LittleRedBookNoteBizApplication.class)
public class DruidTest {
    @Resource
    private ChannelService channelService;

    @Test
    public void channelTest(){
        channelService.findAll().forEach(System.out::println);
    }

}
