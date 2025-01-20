package com.haishi.LittleRedBook.note.biz.controller;

import com.haishi.LittleRedBook.note.biz.model.vo.request.FindNoteDetailRequest;
import com.haishi.LittleRedBook.note.biz.model.vo.request.PublishNoteRequest;
import com.haishi.LittleRedBook.note.biz.model.vo.request.UpdateNoteRequest;
import com.haishi.LittleRedBook.note.biz.model.vo.response.FindNoteDetailResponse;
import com.haishi.LittleRedBook.note.biz.service.NoteService;
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
@RequestMapping("/note")
@Slf4j
public class NoteController {

    @Resource
    private NoteService noteService;

    @PostMapping(value = "/publish")
    @ApiOperationLog(description = "笔记发布")
    public Response<?> publishNote(@Validated @RequestBody PublishNoteRequest request) {
        return noteService.publishNote(request);
    }

    @PostMapping(value = "/detail")
    @ApiOperationLog(description = "笔记详情")
    public Response<FindNoteDetailResponse> findNoteDetail(@Validated @RequestBody FindNoteDetailRequest request) {
        return noteService.findNoteDetail(request);
    }

    @PostMapping(value = "/update")
    @ApiOperationLog(description = "笔记修改")
    public Response<?> updateNote(@Validated @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(request);
    }

}
