package com.movierama.config.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.movierama.config.Environment;
import com.movierama.util.Var;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
@ToString
public class CacheConfigDTO {
    @JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
    public enum CacheType {
        MOVIE_CACHE("movieCache"),
        FILE_CACHE("fileCache"),
        THROTTLE_CACHE("throttleCache"),
        KEYWORD_CACHE("keywordCache"),
        ;

        private static final Map<String, CacheType> toCacheType = new ConcurrentHashMap<>();

        static {
            for(CacheType cacheType : CacheType.values()) {
                toCacheType.put(cacheType.id, cacheType);
            }
        }

        String id;
        CacheType(String id) {
            this.id = id;
        }

        @JsonCreator
        public static CacheType forValue(String value) {
            return toCacheType.get(value);
        }

        @JsonValue
        public String toValue() {
            return this.id;
        }
    };

    CacheType cacheType = CacheType.MOVIE_CACHE;
    //TODO: remove this from initialization code
    String dataFile = "/db/" + cacheType + "db.dat";
    Integer maxEntries = 5000;
    Long maxIdle = 604800000L;
    Long defaultLifespan = 604800000L;

    CacheConfigDTO() {
    }

    CacheConfigDTO(CacheType cacheType) {
        this.cacheType = cacheType;
        this.dataFile = "/db/" + cacheType + "_db.dat";
        if(cacheType == CacheType.MOVIE_CACHE) {
            maxEntries = 5000;
            maxIdle = 604800000L; // one wekk
            defaultLifespan = 604800000L;
        } else {
            maxEntries = 256;
            maxIdle = 604800L;
            defaultLifespan = 604800L;
        }
    }

    public static List<CacheConfigDTO> defaultCaches() {
        List<CacheConfigDTO> caches = new ArrayList<>();
        for(CacheType cacheType : CacheType.values()) {
            caches.add(new CacheConfigDTO(cacheType));
        }
        return caches;
    }

    public static Map<CacheType, CacheConfigDTO> toMap(List<CacheConfigDTO> caches) {
        final Map<CacheType, CacheConfigDTO> map = new ConcurrentHashMap<>();
        for(CacheConfigDTO cacheConfigDTO : caches) {
            map.put(cacheConfigDTO.getCacheType(), cacheConfigDTO);
        }
        return map;
    }
}
