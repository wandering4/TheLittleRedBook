//package com.haishi.littleredbookauth;
//
//import com.haishi.framework.commons.util.JsonUtil;
//import com.haishi.littleredbookauth.domain.DO.UserDO;
//import com.haishi.littleredbookauth.domain.mapper.UserDOMapper;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.time.LocalDateTime;
//
//@SpringBootTest
//@Slf4j
//class LittleRedBookAuthApplicationTests {
//
//    @Resource
//    private UserDOMapper userDOMapper;
//
//    /**
//     * 测试插入数据
//     */
//    @Test
//    void testInsert() {
//        UserDO userDO = UserDO.builder()
//                .nickname("xzf")
//                .createTime(LocalDateTime.now())
//                .updateTime(LocalDateTime.now())
//                .build();
//
//        userDOMapper.insert(userDO);
//    }
//
//    /**
//     * 查询数据
//     */
//    @Test
//    void testSelect() {
//        UserDO userDO=userDOMapper.selectByPrimaryKey(3l);
//        log.info("User: {}", JsonUtil.toJsonString(userDO));
//    }
//
//    /**
//     * 更新数据
//     */
//    @Test
//    void testUpdate() {
//        UserDO userDO = UserDO.builder()
//                .id(3L)
//                .password("123456")
//                .build();
//
//        // 根据主键 ID 更新记录
//        userDOMapper.updateByPrimaryKeySelective(userDO);
//    }
//
//    /**
//     * 删除数据
//     */
//    @Test
//    void testDelete() {
//        // 删除主键 ID 为 4 的记录
//        userDOMapper.deleteByPrimaryKey(1L);
//    }
//
//}
