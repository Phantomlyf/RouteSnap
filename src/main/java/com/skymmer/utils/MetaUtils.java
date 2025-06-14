package com.skymmer.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.file.FileTypeDirectory;
import com.drew.metadata.heif.HeifDirectory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;

public class MetaUtils {
    private static Metadata metadata;
    public static Collection<GpsDirectory> gpsDirectories;
    public static Collection<ExifIFD0Directory> exifIFD0Directories;
    public static Collection<ExifSubIFDDirectory> exifSubIFDDirectories;
    public static Collection<FileTypeDirectory> fileTypeDirectories;
    public static void init(String imagePath) throws ImageProcessingException, IOException {
        File imageFile = new File(imagePath);
         if (!imageFile.exists()) {
         System.err.println("图片文件不存在: " + imagePath);
         return;
         }
        metadata = ImageMetadataReader.readMetadata(imageFile);
        gpsDirectories= metadata.getDirectoriesOfType(GpsDirectory.class);
        exifIFD0Directories=metadata.getDirectoriesOfType(ExifIFD0Directory.class);
        exifSubIFDDirectories=metadata.getDirectoriesOfType(ExifSubIFDDirectory.class);
        fileTypeDirectories=metadata.getDirectoriesOfType(FileTypeDirectory.class);
    }

    public static void init(InputStream inputStream) throws ImageProcessingException, IOException {
        metadata = ImageMetadataReader.readMetadata(inputStream);
        gpsDirectories= metadata.getDirectoriesOfType(GpsDirectory.class);
        exifIFD0Directories=metadata.getDirectoriesOfType(ExifIFD0Directory.class);
        exifSubIFDDirectories=metadata.getDirectoriesOfType(ExifSubIFDDirectory.class);
        fileTypeDirectories=metadata.getDirectoriesOfType(FileTypeDirectory.class);
    }
    public static Double getLatitude() {
        for(GpsDirectory directory : gpsDirectories){
            GeoLocation geoLocation = directory.getGeoLocation();
            if (geoLocation != null) {
                return geoLocation.getLatitude();
            }
        }
        return null;
    }
    public static Double getLongitude() {
        for(GpsDirectory directory : gpsDirectories){
            GeoLocation geoLocation = directory.getGeoLocation();
            if (geoLocation != null) {
                return geoLocation.getLongitude();
            }
        }
        return null;
    }
    public static String getMakeInfo(){
        for(ExifIFD0Directory directory : exifIFD0Directories){
            if(directory.hasTagName(ExifIFD0Directory.TAG_MAKE)){
               return directory.getString(ExifIFD0Directory.TAG_MAKE);
            }
        }
        return null;
    }
    public static String getModelInfo(){
        for(ExifIFD0Directory directory : exifIFD0Directories){
            if(directory.hasTagName(ExifIFD0Directory.TAG_MODEL)){
                return directory.getString(ExifIFD0Directory.TAG_MODEL);
            }
        }
        return null;
    }
    public static String getTypeInfo(){
        for (FileTypeDirectory directory : fileTypeDirectories) {
            if (directory.hasTagName(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)) {
                return directory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE);
            }
        }
        return  null;
    }

    public static String getWidthInfo(){
        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
            if (directory.hasTagName(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)) {
                return directory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH);
            }
        }
        return null;
    }

    public static String getHeightInfo(){
        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
            if (directory.hasTagName(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)) {
                return directory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT);
            }
        }
        return null;
    }

    public static Timestamp getDateTimeInfo(){
        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
                Date dateOriginal = directory.getDateOriginal();
                if (dateOriginal != null) {
                    LocalDateTime DateTime = Instant.ofEpochMilli(dateOriginal.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime();
                    return Timestamp.valueOf(DateTime);
                }
        }
        return null;
    }

    public static String getFnumberInfo(){
        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
            if (directory.hasTagName(ExifSubIFDDirectory.TAG_FNUMBER)){
                return directory.getString(ExifSubIFDDirectory.TAG_FNUMBER);
            }
        }
        return null;
    }

    public static  String getExposureTimeInfo(){
        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
            if (directory.hasTagName(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)){
                return directory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME);
            }
        }
        return null;
    }

    public static String getISOInfo(){
        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
            if (directory.hasTagName(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)){
                return directory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT);
            }
        }
        return null;
    }
}


//
// try {
//         File imageFile = new File(imagePath);
//         if (!imageFile.exists()) {
//         System.err.println("图片文件不存在: " + imagePath);
//         return;
//         }
//         // 读取图片元数据
//         Metadata metadata = ImageMetadataReader.readMetadata(imageFile);
////
////            for (Directory directory : metadata.getDirectories()) {
////                String name = directory.getName();
////                System.out.println("当前目录名称:" + name);
////                System.out.println("----------");
////                for (Tag tag : directory.getTags()) {
////                    String tagName = tag.getTagName();  //标签名
////                    String desc = tag.getDescription(); //标签信息
////                    System.out.println(tagName + "===" + desc);//照片信息
////                }
////            }
//         // 获取GPS目录
//         Collection<GpsDirectory> gpsDirectories = metadata.getDirectoriesOfType(GpsDirectory.class);
//        Collection<ExifIFD0Directory> exifIFD0Directories = metadata.getDirectoriesOfType(ExifIFD0Directory.class);
//        Collection<ExifSubIFDDirectory> exifSubIFDDirectories = metadata.getDirectoriesOfType(ExifSubIFDDirectory.class);
//        Collection<FileTypeDirectory> fileTypeDirectories = metadata.getDirectoriesOfType(FileTypeDirectory.class);
//
//        for (ExifIFD0Directory directory : exifIFD0Directories) {
//        if (directory.hasTagName(ExifIFD0Directory.TAG_MAKE)) {
//        System.out.println("厂商:" + directory.getString(ExifIFD0Directory.TAG_MAKE));
//        }
//        if (directory.hasTagName(ExifIFD0Directory.TAG_MODEL)) {
//        System.out.println("型号:" + directory.getString(ExifIFD0Directory.TAG_MODEL));
//        }
//        }
//        for (FileTypeDirectory directory : fileTypeDirectories) {
//        if (directory.hasTagName(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE)) {
//        System.out.println("类型:" + directory.getString(FileTypeDirectory.TAG_DETECTED_FILE_MIME_TYPE));
//        }
//        }
//        for (ExifSubIFDDirectory directory : exifSubIFDDirectories) {
//        if (directory.hasTagName(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH)) {
//        System.out.println("水平像素: " + directory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
//        }
//        if (directory.hasTagName(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT)) {
//        System.out.println("竖直像素: " + directory.getString(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
//        }
//        if (directory.hasTagName(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
//        System.out.println("拍摄时间: " + directory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL));
//        }
//        if (directory.hasTagName(ExifSubIFDDirectory.TAG_FNUMBER)) {
//        System.out.println("光圈值: " + directory.getString(ExifSubIFDDirectory.TAG_FNUMBER));
//        }
//        if (directory.hasTagName(ExifSubIFDDirectory.TAG_EXPOSURE_TIME)) {
//        System.out.println("曝光时间: " + directory.getString(ExifSubIFDDirectory.TAG_EXPOSURE_TIME));
//        }
//        if (directory.hasTagName(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT)) {
//        System.out.println("iso速度: " + directory.getString(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
//        }
//        }
//
//        if (gpsDirectories.isEmpty()) {
//        System.out.println("该图片没有GPS信息");
//        return;
//        }
//
//        for (GpsDirectory gpsDirectory : gpsDirectories) {
//        printGPSInfo(gpsDirectory);
//        }
//
//        } catch (ImageProcessingException e) {
//        System.err.println("图片处理异常: " + e.getMessage());
//        } catch (IOException e) {
//        System.err.println("文件读取异常: " + e.getMessage());
//        }
//
