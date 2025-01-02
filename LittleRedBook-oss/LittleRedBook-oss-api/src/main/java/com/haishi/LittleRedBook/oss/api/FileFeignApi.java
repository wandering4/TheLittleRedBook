package com.haishi.LittleRedBook.oss.api;

import com.haishi.LittleRedBook.oss.constant.ApiConstants;
import com.haishi.framework.commons.response.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface FileFeignApi {

    String PREFIX = "/file";

    @PostMapping(value = PREFIX + "/test")
    Response<?> test();

}