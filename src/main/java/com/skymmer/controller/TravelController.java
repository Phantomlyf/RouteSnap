package com.skymmer.controller;

import com.skymmer.mapper.TravelMapper;
import com.skymmer.pojo.Travel;
import com.skymmer.service.TravelService;
import com.skymmer.utils.DateConverter;
import com.skymmer.utils.MetaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.skymmer.pojo.Result;

import java.io.InputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RestController
public class TravelController {

    @Autowired
    TravelService travelService;
    @PostMapping("/upload/travel")
    public Result uploadTravel(@RequestParam MultipartFile file,@RequestParam String location, @RequestParam String content){
        travelService.upload(file,location,content);

        return Result.success();
    }


    @GetMapping("list")
    public Result selectTravel(@RequestParam(required=false) String startTime,
                               @RequestParam(required=false) String endTime,
                               @RequestParam(required=false) String location){
        LocalDateTime start = DateConverter.convertToLocalDateTime(startTime);
        LocalDateTime end = DateConverter.convertToLocalDateTime(endTime);
        List<Travel> travels = travelService.selectTravel(start,end,location);
        return Result.success(travels);
    }


}
