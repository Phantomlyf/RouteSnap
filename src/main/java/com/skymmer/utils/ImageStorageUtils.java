package com.skymmer.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

@Component
public class ImageStorageUtils {
    @Autowired
    private  ImageStorageProperties imageStorageProperties;
    public String StorageImage(MultipartFile file) throws IOException {
        //获取原始文件名（含后缀）
        String filename = file.getOriginalFilename();
        //截取后缀
        int index = filename.lastIndexOf('.');
        String extname = filename.substring(index);
        String newFileName = UUID.randomUUID().toString()+extname;

       //将图片放入存储目录中
        String imageDir = imageStorageProperties.getImageDir();
        // 确保目录存在
        File dir = new File(imageDir);
        if (!dir.exists()) {
            // 如果文件夹不存在，创建它
            boolean dirsCreated = dir.mkdirs();
            if (!dirsCreated) {
                throw new IOException("无法创建目录: " + imageDir);
            }
        }
        String filepath = imageDir + File.separator + newFileName;

        File destFile = new File(filepath);
        try {
            // 将文件保存到本地
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new IOException("文件保存失败: " + e.getMessage(), e);
        }

        return filepath;
    }

}
