package com.haishi.LittleRedBook.kv.api;

import com.haishi.LittleRedBook.kv.constant.ApiConstants;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.DeleteNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.req.FindNoteContentRequest;
import com.haishi.LittleRedBook.kv.dto.resp.FindNoteContentResponse;
import com.haishi.framework.commons.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentRequest addNoteContentRequest);

    @PostMapping(value = PREFIX + "/note/content/find")
    Response<FindNoteContentResponse> findNoteContent(@RequestBody FindNoteContentRequest findNoteContentRequest);

    @PostMapping(value = PREFIX + "/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody DeleteNoteContentRequest deleteNoteContentReqDTO);
}