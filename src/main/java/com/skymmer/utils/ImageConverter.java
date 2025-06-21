package com.skymmer.utils;

import openize.heic.decoder.HeicImage;
import openize.heic.decoder.PixelFormat;
import openize.io.IOFileStream;
import openize.io.IOMode;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ImageConverter {
    @Autowired
    private ImageConverterProperties  imageConverterProperties;

    // 存储文件ID和文件路径的映射
    private final Map<String, Path> tempFiles = new ConcurrentHashMap<>();

    // 定时任务执行器，用于自动清理过期文件
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // 当前临时文件路径
    private volatile Path currentTempFile;

    /**
     * 处理HEIC文件转换
     * @param multipartFile HEIC格式的文件
     * @return 包含访问URL的结果对象
     * @throws Exception 处理异常
     */
    public String  previewHeic(MultipartFile multipartFile) throws Exception {
        //缓存机制
        // 计算文件哈希值作为唯一标识
        String fileHash;
        try (InputStream is = multipartFile.getInputStream()) {
            fileHash = DigestUtils.md5Hex(is);
        }

        Path path = tempFiles.get(fileHash);
        if (path != null) {
            System.out.println("缓存命中");
            return path.toString();
        }
        
        // 创建临时 HEIC 文件
        Path tempHeicFile = Files.createTempFile("heic-input-", ".heic");

        try {
            // 将 MultipartFile 写入临时文件
            multipartFile.transferTo(tempHeicFile.toFile());

            // 使用 Openize 解码 HEIC
            try (IOFileStream fs = new IOFileStream(tempHeicFile.toString(), IOMode.READ)) {
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

                // 创建临时输出目录
                File outputDir = new File(imageConverterProperties.getTempDir());
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                // 生成唯一文件ID和临时文件
                String fileName = UUID.randomUUID() + ".jpg";
                Path tempOutputFile = outputDir.toPath().resolve(fileName);

                // 写出 JPEG 图像到临时文件
                boolean isWrite = ImageIO.write(rgbImage, "JPEG", tempOutputFile.toFile());
                if (!isWrite) {
                    throw new IOException("Failed to write JPEG image");
                }

                // 存储文件映射
                tempFiles.put(fileHash, tempOutputFile);

//                // 生成访问URL
//                String accessUrl = imageConverterProperties.getBaseUrl()+ "/api/temp-images/" + fileHash;

                // 设置自动清理任务
                scheduleFileCleanup(fileHash);

                return tempOutputFile.toString();

            }
        } finally {
            // 删除临时输入文件
            Files.deleteIfExists(tempHeicFile);
        }
    }

    public String previewHeic(String filePath) throws Exception {
        // 验证文件路径是否存在且可读
        File inputFile = new File(filePath);
        if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
            throw new FileNotFoundException("文件不存在或不可读: " + filePath);
        }

        // 计算文件哈希值作为唯一标识
        String fileHash;
        try (InputStream is = new FileInputStream(inputFile)) {
            fileHash = DigestUtils.md5Hex(is);
        }

        // 检查缓存
        Path cachedPath = tempFiles.get(fileHash);
        if (cachedPath != null) {
            System.out.println("缓存命中");
            return cachedPath.toString();
        }

        // 创建临时HEIC文件(实际上是源文件的硬链接或复制)
        Path tempHeicFile = Files.createTempFile("heic-input-", ".heic");

        try {
            // 将源文件复制到临时位置
            Files.copy(inputFile.toPath(), tempHeicFile, StandardCopyOption.REPLACE_EXISTING);

            // 使用Openize解码HEIC
            try (IOFileStream fs = new IOFileStream(tempHeicFile.toString(), IOMode.READ)) {
                HeicImage image = HeicImage.load(fs);
                int[] pixels = image.getInt32Array(PixelFormat.Argb32);
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();

                // 创建ARGB格式BufferedImage
                BufferedImage argbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                argbImage.setRGB(0, 0, width, height, pixels, 0, width);

                // 创建RGB格式BufferedImage(去除alpha通道)
                BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = rgbImage.createGraphics();
                g.drawImage(argbImage, 0, 0, null);
                g.dispose();

                // 创建临时输出目录
                File outputDir = new File(imageConverterProperties.getTempDir());
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                // 生成唯一文件ID和临时文件
                String fileName = UUID.randomUUID() + ".jpg";
                Path tempOutputFile = outputDir.toPath().resolve(fileName);

                // 写出JPEG图像到临时文件
                boolean isWrite = ImageIO.write(rgbImage, "JPEG", tempOutputFile.toFile());
                if (!isWrite) {
                    throw new IOException("Failed to write JPEG image");
                }

                // 存储文件映射
                tempFiles.put(fileHash, tempOutputFile);

                // 设置自动清理任务
                scheduleFileCleanup(fileHash);

                return tempOutputFile.toString();
            }
        } finally {
            // 删除临时输入文件
            Files.deleteIfExists(tempHeicFile);
        }
    }

    /**
     * 根据文件ID获取临时文件
     * @param fileHash 文件ID
     * @return 临时文件路径，如果不存在返回null
     */
    public Path getTempFile(String fileHash) {
        return tempFiles.get(fileHash);
    }

    /**
     * 清理指定的临时文件
     * @param fileHash 文件ID
     * @return 是否成功清理
     */
    public boolean cleanupTempFile(String fileHash) {
        Path filePath = tempFiles.remove(fileHash);
        if (filePath != null) {
            try {
                boolean deleted = Files.deleteIfExists(filePath);
                System.out.println("临时文件清理" + (deleted ? "成功" : "失败") + ": " + filePath);
                return deleted;
            } catch (IOException e) {
                System.err.println("清理临时文件时发生错误: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * 清理所有临时文件
     */
    public void cleanupAllTempFiles() {
        tempFiles.keySet().forEach(this::cleanupTempFile);
    }

    /**
     * 设置自动清理任务
     * @param fileHash 文件ID
     */
    private void scheduleFileCleanup(String fileHash) {
        scheduler.schedule(() -> {
            if (tempFiles.containsKey(fileHash)) {
                cleanupTempFile(fileHash);
                System.out.println("自动清理过期临时文件: " + fileHash);
            }
        }, imageConverterProperties.getCleanupDelayMinutes(), TimeUnit.MINUTES);
    }

}