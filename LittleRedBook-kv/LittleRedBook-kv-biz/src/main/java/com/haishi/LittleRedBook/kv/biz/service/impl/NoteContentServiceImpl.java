package com.haishi.LittleRedBook.kv.biz.service.impl;

import com.haishi.LittleRedBook.kv.biz.domain.dataobject.NoteContentDO;
import com.haishi.LittleRedBook.kv.biz.domain.repository.NoteContentRepository;
import com.haishi.LittleRedBook.kv.biz.enums.ResponseCodeEnum;
import com.haishi.LittleRedBook.kv.biz.service.NoteContentService;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.DeleteNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.FindNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.resp.FindNoteContentResponse;
import com.haishi.framework.commons.exception.BizException;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class NoteContentServiceImpl implements NoteContentService {

    @Resource
    private NoteContentRepository noteContentRepository;


    @Override
    public Response<?> addNoteContent(AddNoteContentRequest addNoteContentRequest) {
        // 笔记 ID
        String uuid = addNoteContentRequest.getUuid();
        // 笔记内容
        String content = addNoteContentRequest.getContent();

        // 构建数据库 DO 实体类
        NoteContentDO nodeContent = NoteContentDO.builder()
                .id(UUID.fromString(uuid))
                .content(content)
                .build();

        // 插入数据
        noteContentRepository.save(nodeContent);

        return Response.success();
    }

    /**
     * 查询笔记内容
     *
     * @param findNoteContentRequest
     * @return
     */
    @Override
    public Response<FindNoteContentResponse> findNoteContent(FindNoteContentRequest findNoteContentRequest) {
        String uuid = findNoteContentRequest.getUuid();

        Optional<NoteContentDO> optional  = noteContentRepository.findById(UUID.fromString(uuid));

        // 若笔记内容不存在
        if (!optional.isPresent()) {
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }

        NoteContentDO noteContentDO = optional.get();
        // 构建返参 DTO
        FindNoteContentResponse findNoteContentResponse = FindNoteContentResponse.builder()
                .uuid(noteContentDO.getId())
                .content(noteContentDO.getContent())
                .build();

        return Response.success(findNoteContentResponse);


    }

    /**
     * 删除笔记内容
     * @param deleteNoteContentReqDTO
     * @return
     */
    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentRequest deleteNoteContentReqDTO) {
        String uuid = deleteNoteContentReqDTO.getUuid();
        noteContentRepository.deleteById(UUID.fromString(uuid));
        return Response.success();
    }
}
