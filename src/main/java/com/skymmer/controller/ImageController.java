package com.skymmer.controller;

import com.skymmer.pojo.ImageInfo;
import com.skymmer.pojo.Result;
import com.skymmer.pojo.Travel;
import com.skymmer.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

@RestController
public class ImageController {
    @Autowired
    ImageStorageProperties imageStorageProperties;
    @Autowired
    ImageConverter imageConverter;
    @Autowired
    GpsConverter gpsConverter;
    @Autowired
    GetLocationUtils getLocationUtils;
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
            Double GCJ_lat = null;
            Double GCJ_lon = null;
            String location = null;
            //WSG84坐标系转为GCJ-02坐标系
            if(latitude != null && longitude != null) {
                double[] to_gcj02 = gpsConverter.gps84_To_Gcj02(latitude, longitude);
                GCJ_lat = gpsConverter.retain6(to_gcj02[0]);
                GCJ_lon = gpsConverter.retain6(to_gcj02[1]);
                location = getLocationUtils.getLocation(GCJ_lat,GCJ_lon);
            }

            String previewPath =  null;
            if(contentType.equals("image/heic") || contentType.equals("image/heif") || contentType.equals("image/HEIC") || contentType.equals("image/HEIF")){
                System.out.println("生成预览地址");
                previewPath = imageConverter.previewHeic(file);
            }
            //文件流上传，不知道原始路径。
            ImageInfo imageInfo = new ImageInfo(null, previewPath, latitude, longitude,GCJ_lat,GCJ_lon,location,takenTime, make, model, type, width, height, fnumber, exposureTime, iso);
            return Result.success(imageInfo);
        }
    }

    @GetMapping("/upload")
    public Result uploadImagePath(@RequestParam String imagePath) throws Exception{
        String decodedPath = URLDecoder.decode(imagePath, StandardCharsets.UTF_8);
        //获取扩展名
        int index = decodedPath.lastIndexOf('.');
        String extname = decodedPath.substring(index+1);
        String extnameLowerCase = extname.toLowerCase();
        String contentType = "image/"+extnameLowerCase;

        if(!imageStorageProperties.getAllowedTypes().contains(contentType)){
            return Result.error(1001,"图片上传类型错误");
        }

        MetaUtils.init(decodedPath);
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
        Double GCJ_lat = null;
        Double GCJ_lon = null;
        String location = null;
        //WSG84坐标系转为GCJ-02坐标系
        if(latitude != null && longitude != null) {
            double[] to_gcj02 = gpsConverter.gps84_To_Gcj02(latitude, longitude);
            GCJ_lat = gpsConverter.retain6(to_gcj02[0]);
            GCJ_lon = gpsConverter.retain6(to_gcj02[1]);
            location = getLocationUtils.getLocation(GCJ_lat,GCJ_lon);
        }

        String previewPath =  null;
        if(contentType.equals("image/heic") || contentType.equals("image/heif")){
            previewPath = imageConverter.previewHeic(decodedPath);
        }
        else{
            previewPath = decodedPath;
        }
        ImageInfo imageInfo = new ImageInfo(decodedPath,previewPath, latitude, longitude,GCJ_lat, GCJ_lon,location, takenTime, make, model, type, width, height, fnumber, exposureTime, iso);
        System.out.println(imageInfo);
        return Result.success(imageInfo);
    }

    @GetMapping("/api/temp-images/{fileId}")
    public Result previewImage(@PathVariable String fileId){
        Path tempFile = imageConverter.getTempFile(fileId);
        return Result.success(tempFile.toString());
    }

}
