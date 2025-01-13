package com.haishi.LittleRedBook.distributed.id.generator.biz.core;


import com.haishi.LittleRedBook.distributed.id.generator.biz.core.common.Result;

public interface IDGen {
    Result get(String key);
    boolean init();
}
