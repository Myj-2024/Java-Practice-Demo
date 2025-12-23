package com.example.Controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.example.pojo.Result;
import com.example.utils.MinioOperator;  // 替换为MinIO工具类
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Value("${spring.servlet.multipart.location}")
    private String fileTempPath;

    @PostMapping(value = "/local",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Dict local(@RequestParam("file")MultipartFile file){
        if (file.isEmpty()){
            return Dict.create().set("code", 400).set("msg", "上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        String rawFileName = StrUtil.subBefore(fileName, ".", true);
        String fileType = StrUtil.subAfter(fileName, ".", true);
        String localFilePath = StrUtil.appendIfMissing(fileTempPath, "/") + rawFileName + "-" + DateUtil.current() + "." + fileType;
        try {
            file.transferTo(new File(localFilePath));
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage());
            return Dict.create().set("code", 500).set("msg", "上传文件失败");
        }
        log.info("【文件上传至本地】绝对路径：{}", localFilePath);
        return Dict.create().set("code", 200).set("message", "上传成功").set("data", Dict.create().set("fileName", fileName).set("filePath", localFilePath));
    }




    @Autowired
    private MinioOperator minioOperator;  // 注入MinIO工具类

    @PostMapping(value = "/yun", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result upload(MultipartFile file) throws Exception {
        log.info("文件上传：{}", file.getOriginalFilename());
        // 调用MinIO工具类上传文件
        String url = minioOperator.upload(file.getBytes(), file.getOriginalFilename());
        log.info("文件上传完成，返回访问路径：{}", url);
        return Result.success(url);
    }
}