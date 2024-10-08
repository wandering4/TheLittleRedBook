package com.haishi.littleredbookauth;

import com.haishi.framework.common.util.JsonUtil;
import com.haishi.littleredbookauth.domain.DO.UserDO;
import com.haishi.littleredbookauth.domain.mapper.UserDOMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
@Slf4j
class LittleRedBookAuthApplicationTests {

    @Resource
    private UserDOMapper userDOMapper;

    /**
     * 测试插入数据
     */
    @Test
    void testInsert() {
        UserDO userDO = UserDO.builder()
                .username("犬小哈")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        userDOMapper.insert(userDO);
    }

    /**
     * 查询数据
     */
    @Test
    void testSelect() {
        UserDO userDO=userDOMapper.selectByPrimaryKey(2l);
        log.info("User: {}", JsonUtil.toJsonString(userDO));
    }

    /**
     * 更新数据
     */
    @Test
    void testUpdate() {
        UserDO userDO = UserDO.builder()
                .id(2L)
                .username("犬小哈教程")
                .updateTime(LocalDateTime.now())
                .build();

        // 根据主键 ID 更新记录
        userDOMapper.updateByPrimaryKey(userDO);
    }

    /**
     * 删除数据
     */
    @Test
    void testDelete() {
        // 删除主键 ID 为 4 的记录
        userDOMapper.deleteByPrimaryKey(1L);
    }

}
