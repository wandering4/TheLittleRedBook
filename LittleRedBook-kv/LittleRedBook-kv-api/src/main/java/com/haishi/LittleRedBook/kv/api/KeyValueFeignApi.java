package com.haishi.LittleRedBook.kv.api;

import com.haishi.LittleRedBook.kv.constant.ApiConstants;
import com.haishi.LittleRedBook.kv.dto.req.AddNoteContentReqDTO;
import com.haishi.framework.commons.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {

    String PREFIX = "/kv";

    @PostMapping(value = PREFIX + "/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

}