package com.skymmer.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;


@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
public class ListInfo {
    private Integer id;
    private Timestamp takenTime;
    private String location;
    private String content;

}
