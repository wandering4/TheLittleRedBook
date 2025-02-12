package com.haishi.LittleRedBook.user.relation.biz.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

public class LuaUtils {
    public static <T> DefaultRedisScript<T> getLuaScript(String path, Class<T> tClass) {
        DefaultRedisScript<T> script = new DefaultRedisScript<>();
        // Lua 脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(path)));
        // 返回值类型
        script.setResultType(tClass);
        return script;
    }
}
