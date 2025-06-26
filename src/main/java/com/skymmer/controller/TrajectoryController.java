package com.skymmer.controller;


import com.skymmer.pojo.GpsInfo;
import com.skymmer.pojo.Result;
import com.skymmer.service.TravelService;
import com.skymmer.utils.DateConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.List;

@RestController
public class TrajectoryController {

    @Autowired
    TravelService travelService;

    @GetMapping("/genTra")
    public Result genTra(@RequestParam(required = false) String startTime,
                         @RequestParam(required = false) String endTime){
        Timestamp start = DateConverter.convertToTimestamp(startTime);
        Timestamp end = DateConverter.convertToTimestamp(endTime);
        List<Integer> list =travelService.genTra(start,end);

        if(list == null){
            return Result.error(1101,"未在该时间段内找到行程");
        }
        return Result.success(list);
    }
}
