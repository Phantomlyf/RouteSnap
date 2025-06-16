package com.skymmer.service;


import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.Travel;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface TravelService {
    int upload(MultipartFile file,String location,String content);

    List<Integer> listId();

    Travel getById(Integer id);

    List<Travel> selectTravel(LocalDateTime startTime,LocalDateTime endTime,String location);

    List<GpsInfo> genTra(Timestamp start, Timestamp end);
}
