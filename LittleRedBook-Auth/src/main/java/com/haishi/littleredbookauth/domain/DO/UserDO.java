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

    private String username;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}