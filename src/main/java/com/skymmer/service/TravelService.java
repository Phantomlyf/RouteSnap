package com.skymmer.service;


import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.ListInfo;
import com.skymmer.pojo.Travel;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface TravelService {
    int upload(MultipartFile file,String location,String content) throws Exception;

    int upload(String previewPath,Travel travel) throws Exception;

    List<Integer> listId();

    Travel getById(Integer id);

    List<GpsInfo> listGps();

    List<ListInfo> listShort(Integer page);

    List<Integer> selectTravel(LocalDateTime startTime,LocalDateTime endTime,String location);

    List<Integer> genTra(Timestamp start, Timestamp end);

    void updateLonLat(Integer id, Double lat,Double lon);

    void updateContent(Integer id,String Content);

    void deleteById(Integer id);
}
