package com.haishi.LittleRedBook.user.biz.rpc;

import com.haishi.LittleRedBook.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class DistributedIdGeneratorRpcService {

    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;

    /**
     * Leaf 号段模式：小红书 ID 业务标识
     */
    private static final String BIZ_TAG_LittleRedBook_ID = "leaf-segment-LittleRedBook-id";

    /**
     * Leaf 号段模式：用户 ID 业务标识
     */
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";

    /**
     * 调用分布式 ID 生成服务生成小红书 ID
     *
     * @return
     */
    public String getLittleRedBookId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_LittleRedBook_ID);
    }

    /**
     * 调用分布式 ID 生成服务用户 ID
     *
     * @return
     */
    public String getUserId() {
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID);
    }
}