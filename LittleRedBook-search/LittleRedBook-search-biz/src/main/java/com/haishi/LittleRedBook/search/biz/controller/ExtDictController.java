package com.haishi.LittleRedBook.search.biz.controller;

import com.haishi.LittleRedBook.search.biz.service.ExtDictService;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version: v1.0.0
 * @description: 热更新拓展词典
 **/
@RestController
@RequestMapping("/search")
@Slf4j
public class ExtDictController {

    @Resource
    private ExtDictService extDictService;

    @GetMapping("/ext/dict")
    @ApiOperationLog(description = "热更新词典")
    public ResponseEntity<String> extDict() {
        return extDictService.getHotUpdateExtDict();
    }

}