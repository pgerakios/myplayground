package com.movierama.connectors.integration.movies.impl;

import com.movierama.config.Environment;
import com.movierama.config.dto.RestResourceConfigDTO;
import com.movierama.rest.dto.QueryType;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonValue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@JsonTypeInfo (use=JsonTypeInfo.Id.NONE, include=JsonTypeInfo.As.PROPERTY)
public enum ProviderType{
    ROTTENTOMATOES("rottentomatoes"),
    THEMOVIEDB("themoviedb");

    String id;
    ProviderType(String id){
        this.id=id;
    }
    @JsonValue
    public String getId(){
        return this.id;
    }

    @JsonCreator
    public static ProviderType forValue(String value){
        for(ProviderType provider : ProviderType.values()) {
            if(provider.getId().equals(value))
                return provider;
        }
        return null;
    }

    public static Map<ProviderType, MoviesConnectorImpl.MovieList> instance(){
        Map<ProviderType, MoviesConnectorImpl.MovieList> retVal=new ConcurrentHashMap<>();
        for(ProviderType providerType : ProviderType.values()){
            retVal.put(providerType, new MoviesConnectorImpl.MovieListImpl());
        }
        return retVal;
    }

    public String requestURL(final QueryType type, final String... args){
        RestResourceConfigDTO config = Environment.env().getConfigDTO().getIntegrationInfos().lookup(this, type);
        final String url = config.toURLString(args);
        if(type == QueryType.DYNAMIC) {

        }
        return url;
    }

//    public RestConfigDTO findRestDTO(){
//        return MoviesConnectorImpl.findRestDTO(this);
//    }
}
