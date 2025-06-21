package com.skymmer.utils;


import openize.heic.decoder.HeicImage;
import openize.heic.decoder.PixelFormat;
import openize.io.IOFileStream;
import openize.io.IOMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
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
        String filepath = imageDir + "/" + newFileName;

        File destFile = new File(filepath);
        try {
            // 将文件保存到本地
            file.transferTo(destFile);
        } catch (IOException e) {
            throw new IOException("文件保存失败: " + e.getMessage(), e);
        }

        return filepath;
    }

    public String StorageImage(String previewPath) throws IOException {
        String extname = previewPath.substring(previewPath.lastIndexOf('.') + 1);
        String newFileName = UUID.randomUUID().toString() + extname;

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
        String filepath = imageDir + "/" + newFileName;

        File source = new File(previewPath);
        File dest = new File(filepath);
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
        return filepath;

    }
    public String StorageHeif(MultipartFile multipartFile) throws Exception {
        // 创建临时 HEIC 文件
        Path tempFile = Files.createTempFile("heic-", ".heic");

        try {
            // 将 MultipartFile 写入临时文件
            multipartFile.transferTo(tempFile.toFile());

            // 使用 Openize 解码 HEIC
            try (IOFileStream fs = new IOFileStream(tempFile.toString(), IOMode.READ)) {
                HeicImage image = HeicImage.load(fs);
                int[] pixels = image.getInt32Array(PixelFormat.Argb32);
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();

                // 创建 ARGB 格式 BufferedImage
                BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                argbImage.setRGB(0, 0, width, height, pixels, 0, width);

                // 创建 RGB 格式 BufferedImage（去除 alpha 通道）
                BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgbImage.createGraphics();
                g.drawImage(argbImage, 0, 0, null);
                g.dispose();

                // 确保输出目录存在
                File outputDir = new File(imageStorageProperties.getImageDir());
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                String newFileName = UUID.randomUUID().toString()+".jpg";
                // 写出 JPEG 图像
                File outputFile = new File(outputDir, newFileName);
                boolean isWrite = ImageIO.write(rgbImage, "JPEG", outputFile);
                System.out.println("是否写入成功: " + isWrite);

                return imageStorageProperties.getImageDir()+"/"+newFileName;
            }
        } finally {
            // 删除临时文件
            Files.deleteIfExists(tempFile);
        }
    }

    public String StorageHeif(String previewPath) throws IOException {
        try (IOFileStream fs = new IOFileStream(previewPath, IOMode.READ)) {
            HeicImage image = HeicImage.load(fs);
            int[] pixels = image.getInt32Array(PixelFormat.Argb32);
            int width = (int) image.getWidth();
            int height = (int) image.getHeight();

            // 创建 ARGB 格式 BufferedImage
            BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            argbImage.setRGB(0, 0, width, height, pixels, 0, width);

            // 创建 RGB 格式 BufferedImage（去除 alpha 通道）
            BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(argbImage, 0, 0, null);
            g.dispose();

            // 确保输出目录存在
            File outputDir = new File(imageStorageProperties.getImageDir());
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            String newFileName = UUID.randomUUID().toString()+".jpg";
            // 写出 JPEG 图像
            File outputFile = new File(outputDir, newFileName);
            boolean isWrite = ImageIO.write(rgbImage, "JPEG", outputFile);
            System.out.println("是否写入成功: " + isWrite);

            return imageStorageProperties.getImageDir()+"/"+newFileName;
        }
    }

}
