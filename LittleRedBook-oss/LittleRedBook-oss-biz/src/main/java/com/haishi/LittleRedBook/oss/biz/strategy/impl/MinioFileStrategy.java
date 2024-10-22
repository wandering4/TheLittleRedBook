package com.haishi.LittleRedBook.oss.biz.strategy.impl;

import com.haishi.LittleRedBook.oss.biz.config.MinioProperties;
import com.haishi.LittleRedBook.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
public class MinioFileStrategy implements FileStrategy {

    @Resource
    private MinioProperties minioProperties;

    @Resource
    private MinioClient minioClient;

    @Override
    @SneakyThrows
    public String uploadFile(MultipartFile file, String bucketName) {
        log.info("## 上传文件至 Minio ...");

        //判断文件是否为空
        if(file==null||file.getSize()==0){
            log.error("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }

        //文件原始名称
        String originalFilename = file.getOriginalFilename();

        //文件类型
        String contentType = file.getContentType();

        //文件后缀
        int dotIndex = originalFilename.lastIndexOf(".");
        if(dotIndex==-1){
            throw new IllegalArgumentException("文件名没有后缀");
        }

        //新名称
        //存储对象名称
        String newName = UUID.randomUUID().toString().replace("-", "")
                //文件后缀
                +originalFilename.substring(dotIndex);

        log.info("==> 开始上传文件至 Minio, ObjectName: {}", newName);

        // 上传文件至 Minio
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName)
                .object(newName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(contentType)
                .build());

        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, newName);
        log.info("==> 上传文件至 Minio 成功，访问路径: {}", url);
        return url;
    }
}
