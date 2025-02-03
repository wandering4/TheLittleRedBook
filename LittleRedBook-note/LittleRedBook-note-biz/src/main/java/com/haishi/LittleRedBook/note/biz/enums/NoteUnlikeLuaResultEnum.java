package com.haishi.LittleRedBook.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum NoteUnlikeLuaResultEnum {
    // 布隆过滤器不存在
    NOT_EXIST(-1L),
    // 笔记已点赞
    NOTE_LIKED(1L),
    // 笔记未点赞
    NOTE_NOT_LIKED(0L),
    ;

    private final Long code;

    /**
     * 根据类型 code 获取对应的枚举
     *
     * @param code
     * @return
     */
    public static NoteUnlikeLuaResultEnum valueOf(Long code) {
        for (NoteUnlikeLuaResultEnum noteUnlikeLuaResultEnum : NoteUnlikeLuaResultEnum.values()) {
            if (Objects.equals(code, noteUnlikeLuaResultEnum.getCode())) {
                return noteUnlikeLuaResultEnum;
            }
        }
        return null;
    }
}
