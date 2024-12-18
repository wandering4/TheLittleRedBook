package com.haishi.littleredbookauth.domain.DO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDO {
    private Long id;

    private String accountId;

    private String password;

    private String nickname;

    private String avatar;

    private LocalDateTime birthday;

    private String backgroundImg;

    private String phone;

    private Integer sex;

    private Integer status;

    private String introduction;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean isDeleted;

}