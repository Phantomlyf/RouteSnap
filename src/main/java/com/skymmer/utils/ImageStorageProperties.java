package com.skymmer.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@ConfigurationProperties(prefix = "app.storage")
public class ImageStorageProperties {
    private String imageDir;
    private Integer maxSizeMB;
    private List<String> allowedTypes;
}
