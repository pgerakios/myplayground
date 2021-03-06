package com.movierama.rest.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
public class FlatMapEntry {
    private String key;
    private String val;
}
