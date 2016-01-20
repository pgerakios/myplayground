package com.movierama.config.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
public class LocalServiceConfigDTO {
    EndpointConfigDTO localServer = new EndpointConfigDTO();
    List<PathInfoDTO> staticContentPaths =  new ArrayList<>();
    String urlPath;
}
