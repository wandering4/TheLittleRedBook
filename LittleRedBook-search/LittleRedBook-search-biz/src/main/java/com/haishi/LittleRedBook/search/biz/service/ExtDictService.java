package com.haishi.LittleRedBook.search.biz.service;

import org.springframework.http.ResponseEntity;

/**
 * @version: v1.0.0
 * @description: 拓展词典
 **/
public interface ExtDictService {

    /**
     * 获取热更新词典
     * @return
     */
    ResponseEntity<String> getHotUpdateExtDict();
}
