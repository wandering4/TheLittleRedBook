package com.haishi.LittleRedBook.kv.biz.controller;

import com.haishi.LittleRedBook.kv.biz.service.NoteContentService;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.DeleteNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.FindNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.resp.FindNoteContentResponse;
import com.haishi.framework.biz.operationlog.aspect.ApiOperationLog;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/kv")
@Slf4j
public class NoteContentController {

    @Resource
    private NoteContentService noteContentService;

    @PostMapping(value = "/note/content/add")
    @ApiOperationLog(description = "添加或更新笔记")
    public Response<?> addNoteContent(@Validated @RequestBody AddNoteContentRequest addNoteContentRequest) {
        return noteContentService.addNoteContent(addNoteContentRequest);
    }


    @PostMapping(value = "/note/content/find")
    @ApiOperationLog(description = "查询笔记")
    public Response<FindNoteContentResponse> findNoteContent(@Validated @RequestBody FindNoteContentRequest findNoteContentRequest) {
        return noteContentService.findNoteContent(findNoteContentRequest);
    }

    @PostMapping(value = "/note/content/delete")
    public Response<?> deleteNoteContent(@Validated @RequestBody DeleteNoteContentRequest deleteNoteContentReqDTO) {
        return noteContentService.deleteNoteContent(deleteNoteContentReqDTO);
    }

}
