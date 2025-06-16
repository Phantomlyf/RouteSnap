package com.skymmer.controller;

import com.skymmer.pojo.ImageInfo;
import com.skymmer.pojo.Result;
import com.skymmer.pojo.Travel;
import com.skymmer.utils.ImageStorageProperties;
import com.skymmer.utils.MetaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Timestamp;

@RestController
public class ImageController {
    @Autowired
    ImageStorageProperties imageStorageProperties;
    @PostMapping("/upload/image")
    public Result uploadImage(@RequestParam MultipartFile file) throws Exception {
        //验证文件类型
        String contentType = file.getContentType();
        System.out.println(contentType);
        if(!imageStorageProperties.getAllowedTypes().contains(contentType)){
            return Result.error(1001,"图片上传类型错误");
        }
        try (InputStream inputStream = file.getInputStream()) {
            MetaUtils.init(inputStream);
            Double latitude = MetaUtils.getLatitude();
            Double longitude = MetaUtils.getLongitude();
            Timestamp takenTime = MetaUtils.getDateTimeInfo();
            String make = MetaUtils.getMakeInfo();
            String model = MetaUtils.getModelInfo();
            String type = MetaUtils.getTypeInfo();
            String width = MetaUtils.getWidthInfo();
            String height = MetaUtils.getHeightInfo();
            String fnumber = MetaUtils.getFnumberInfo();
            String exposureTime = MetaUtils.getExposureTimeInfo();
            String iso = MetaUtils.getISOInfo();

            ImageInfo imageInfo = new ImageInfo(latitude, longitude, takenTime, make, model, type, width, height, fnumber, exposureTime, iso);
            return Result.success(imageInfo);
        }
    }
}
