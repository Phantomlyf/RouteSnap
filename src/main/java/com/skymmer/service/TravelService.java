package com.skymmer.service;


import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.Travel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface TravelService {
    public void upload(MultipartFile file,String location,String content);

    public List<Travel> selectTravel(LocalDateTime startTime,LocalDateTime endTime,String location);

    public List<GpsInfo> genTra(Timestamp start, Timestamp end);
}
