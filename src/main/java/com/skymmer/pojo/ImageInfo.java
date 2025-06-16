package com.skymmer.pojo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageInfo {
    private Double latitude;
    private Double longitude;
    private Timestamp takenTime;
    private String make;
    private String model;
    private String type;
    private String width;
    private String height;
    private String fnumber;
    private String exposureTime;
    private String iso;

}
