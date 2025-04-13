package com.haishi.LittleRedBook.user.biz.model.vo.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * @date: 2024/4/13 18:17
 * @version: v1.0.0
 * @description: 修改用户信息
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserInfoRequest {

    @NotNull(message = "用户 ID 不能为空")
    private Long userId;

    /**
     * 头像
     */
    private MultipartFile avatar;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 小哈书 ID
     */
    private String accountId;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 背景图
     */
    private MultipartFile backgroundImg;
}
