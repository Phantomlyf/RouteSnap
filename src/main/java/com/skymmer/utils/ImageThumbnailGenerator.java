package com.skymmer.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


//缩略图生成，能根据用户上传的图片转化为缩略图并保存到指定路径中
@Component
public class ImageThumbnailGenerator {

    @Value("${app.storage.thumbnailDir:D:/AAA/thumbnail}")
    private String thumbnailDir;
    // 支持的输入格式
}