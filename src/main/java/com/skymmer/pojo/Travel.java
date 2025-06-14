package com.skymmer.pojo;

import lombok.Data;

import java.sql.Timestamp;

//游记实体类
@Data
public class Travel {
    private Integer id;
    private String imagePath;
    private String content;
    private Double latitude;
    private Double longitude;
    private Timestamp takenTime;
    private String location;
    private String make;
    private String model;
    private String type;
    private String width;
    private String height;
    private String fnumber;
    private String exposureTime;
    private String iso;

    public Travel() {
    }

    public Travel(Double latitude, Double longitude, Timestamp takenTime, String make, String model, String type, String width, String height, String fnumber, String exposureTime, String iso) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.takenTime = takenTime;
        this.make = make;
        this.model = model;
        this.type = type;
        this.width = width;
        this.height = height;
        this.fnumber = fnumber;
        this.exposureTime = exposureTime;
        this.iso = iso;
    }

    public Travel(String imagePath, String content, Double latitude, Double longitude, Timestamp takenTime, String location, String make, String model, String type, String width, String height, String fnumber, String exposureTime, String iso) {
        this.imagePath = imagePath;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.takenTime = takenTime;
        this.location = location;
        this.make = make;
        this.model = model;
        this.type = type;
        this.width = width;
        this.height = height;
        this.fnumber = fnumber;
        this.exposureTime = exposureTime;
        this.iso = iso;
    }

    public Travel(Integer id, String imagePath, String content, Double latitude, Double longitude, Timestamp takenTime, String location, String make, String model, String type, String width, String height, String fnumber, String exposureTime, String iso) {
        this.id = id;
        this.imagePath = imagePath;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.takenTime = takenTime;
        this.location = location;
        this.make = make;
        this.model = model;
        this.type = type;
        this.width = width;
        this.height = height;
        this.fnumber = fnumber;
        this.exposureTime = exposureTime;
        this.iso = iso;
    }
}
