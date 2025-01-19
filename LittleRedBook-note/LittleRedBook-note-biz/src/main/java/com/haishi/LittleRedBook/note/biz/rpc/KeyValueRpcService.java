package com.haishi.LittleRedBook.note.biz.rpc;

import com.haishi.LittleRedBook.kv.api.KeyValueFeignApi;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.DeleteNoteContentRequest;
import com.haishi.framework.commons.response.Response;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KeyValueRpcService {

    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    /**
     * 保存笔记内容
     *
     * @param uuid
     * @param content
     * @return
     */
    public boolean saveNoteContent(String uuid, String content) {
        AddNoteContentRequest addNoteContentRequest = new AddNoteContentRequest();
        addNoteContentRequest.setUuid(uuid);
        addNoteContentRequest.setContent(content);

        Response<?> response = keyValueFeignApi.addNoteContent(addNoteContentRequest);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }

        return true;
    }

    /**
     * 删除笔记内容
     *
     * @param uuid
     * @return
     */
    public boolean deleteNoteContent(String uuid) {
        DeleteNoteContentRequest deleteNoteContentRequest = new DeleteNoteContentRequest();
        deleteNoteContentRequest.setUuid(uuid);

        Response<?> response = keyValueFeignApi.deleteNoteContent(deleteNoteContentRequest);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return false;
        }

        return true;
    }

}