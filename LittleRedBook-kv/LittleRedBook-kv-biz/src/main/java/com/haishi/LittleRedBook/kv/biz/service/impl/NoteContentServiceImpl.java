package com.haishi.LittleRedBook.kv.biz.service.impl;

import com.haishi.LittleRedBook.kv.biz.domain.dataobject.NoteContentDO;
import com.haishi.LittleRedBook.kv.biz.domain.repository.NoteContentRepository;
import com.haishi.LittleRedBook.kv.biz.service.NoteContentService;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentReqDTO;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class NoteContentServiceImpl implements NoteContentService {

    @Resource
    private NoteContentRepository noteContentRepository;


    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO) {
        // 笔记 ID
        Long noteId = addNoteContentReqDTO.getNoteId();
        // 笔记内容
        String content = addNoteContentReqDTO.getContent();

        // 构建数据库 DO 实体类
        NoteContentDO nodeContent = NoteContentDO.builder()
                .id(UUID.randomUUID()) // TODO: 暂时用 UUID
                .content(content)
                .build();

        // 插入数据
        noteContentRepository.save(nodeContent);

        return Response.success();
    }
}
