package com.movierama.rest.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RestParam {
    APIKEY("apikey"),
    ID("id"),
    QUERY("query"),
    DYNAMIC("dynamic")
    ;

    String id;
    RestParam(String id){
        this.id=id;
    }
    @JsonValue
    public String getId(){
        return this.id;
    }

    public String toString() {
        return this.getId();
    }

    @JsonCreator
    public static RestParam forValue(String value){
        for(RestParam provider : RestParam.values()) {
            if(provider.getId().equals(value))
                return provider;
        }
        return null;
    }
}
