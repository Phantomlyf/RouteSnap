package com.skymmer.service.impl;

import com.drew.imaging.ImageProcessingException;
import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.Result;
import com.skymmer.pojo.Travel;
import com.skymmer.service.TravelService;
import com.skymmer.utils.ImageStorageUtils;
import com.skymmer.utils.ImageThumbnailGenerator;
import com.skymmer.utils.MetaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TravelServiceA implements TravelService {
    @Autowired
    ImageStorageUtils imageStorageUtils;

    @Autowired
    TravelMapper travelMapper;

    @Autowired
    ImageThumbnailGenerator imageThumbnailGenerator;
    @Override
    public void upload(MultipartFile file,String location,String content) {
        try {
            //1.将图片存储到本地，获得本地地址
            String imagePath = imageStorageUtils.StorageImage(file);
            //2.根据本地地址解析图片
            MetaUtils.init(imagePath);
            Timestamp takenTime = MetaUtils.getDateTimeInfo();
            Double latitude = MetaUtils.getLatitude();
            Double longitude = MetaUtils.getLongitude();
            //location直接传入
            String make = MetaUtils.getMakeInfo();
            String model = MetaUtils.getModelInfo();
            String type = MetaUtils.getTypeInfo();
            String width = MetaUtils.getWidthInfo();
            String height = MetaUtils.getHeightInfo();
            String fnumber = MetaUtils.getFnumberInfo();
            String exposureTime = MetaUtils.getExposureTimeInfo();
            String iso = MetaUtils.getISOInfo();
            //3.将图片转化为缩略图并保存
            //4.存入数据
            Travel travel = new Travel(imagePath,content,latitude,longitude, takenTime, location, make, model, type, width, height, fnumber, exposureTime, iso);
            travelMapper.insert(travel);
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("游记存储服务层出错");
        }
    }

    @Override
    public List<Travel> selectTravel(LocalDateTime startTime, LocalDateTime endTime, String location) {
        Timestamp start = Timestamp.valueOf(startTime);
        Timestamp end = Timestamp.valueOf(endTime);
        List<Travel> selectedTravels = travelMapper.select(start, end, location);
        return  selectedTravels;
    }

    @Override
    public List<GpsInfo> genTra(Timestamp start, Timestamp end) {
        List<GpsInfo> list=travelMapper.genTra(start,end);
        return list;
    }


}
