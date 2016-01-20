package com.movierama.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonTypeInfo (use=JsonTypeInfo.Id.NONE, include=JsonTypeInfo.As.PROPERTY)
public enum QueryType{
    NOW_PLAYING("nowplaying"),
    BY_TITLE("bytitle"),
    BY_ID("byid"),
    REVIEWS_BYID("reviewsbyid"),
    CAST_BYID("castbyid"),
    DYNAMIC(""),;

    private static final Map<String, QueryType> toQueryType=new ConcurrentHashMap<>();

    static{
        for(QueryType queryType : QueryType.values()){
            toQueryType.put(queryType.id, queryType);
        }
    }

    String id;

    QueryType(String id){
        this.id=id;
    }

    @JsonCreator
    public static QueryType forValue(String value){
        return toQueryType.get(value);
    }

    @JsonValue
    public String toValue(){
        return this.id;
    }
}
