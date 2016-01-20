package com.movierama.rest.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
public class FlatMapDTO implements DTO {

    List<FlatMapEntry> data = new ArrayList<FlatMapEntry>();

    public Map<String, String> toMap() {
        final Map<String, String> map = new HashMap<String, String>();
        for (FlatMapEntry entry : data) {
            map.put(entry.getKey(), entry.getVal());
        }
        return map;
    }
}
