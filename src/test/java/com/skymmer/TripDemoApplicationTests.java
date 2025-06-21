package com.skymmer;

import com.drew.imaging.ImageProcessingException;
import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.Travel;
import com.skymmer.utils.MetaUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class TripDemoApplicationTests {
    @Test
    void contextLoads() {
    }

    @Test
    void testMetaUtils(){
        String imagePath="C:\\Users\\33580\\Desktop\\IMG_8625.HEIC";
    }

    @Autowired
    TravelMapper travelMapper;
    @Test
    void testInsert() throws ImageProcessingException, IOException {
        String imagePath="C:\\Users\\33580\\Desktop\\IMG_8625.HEIC";
        MetaUtils.init(imagePath);
        Timestamp datetime = MetaUtils.getDateTimeInfo();
        Double latitude = MetaUtils.getLatitude();
        Double longitude = MetaUtils.getLongitude();
        String make = MetaUtils.getMakeInfo();
        String model = MetaUtils.getModelInfo();
        String type = MetaUtils.getTypeInfo();
        String width = MetaUtils.getWidthInfo();
        String height = MetaUtils.getHeightInfo();
        String fnumber = MetaUtils.getFnumberInfo();
        String exposureTime = MetaUtils.getExposureTimeInfo();
        String iso = MetaUtils.getISOInfo();

        Travel travel = new Travel(latitude, longitude, datetime, make, model, type, width, height, fnumber, exposureTime, iso);
        travelMapper.insert(travel);
    }


    @Test
    void testSelectAll(){
        travelMapper.selectALl();
    }

    @Test
    void testSelectPath(){
        List<String> strings = travelMapper.selectPath();
        System.out.println(strings);
    }


}
