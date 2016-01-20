package com.movierama.config.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movierama.connectors.integration.movies.impl.ProviderType;
import com.movierama.rest.dto.QueryType;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
public class IntegrationConfigDTO {
    List<RestResourceConfigDTO> apis = new ArrayList<>();


    public RestResourceConfigDTO lookup(ProviderType providerType, QueryType queryType) {
        RestResourceConfigDTO retVal = RestResourceConfigDTO.lookup(apis, providerType, queryType);
        retVal = retVal.copy();
        return retVal;
    }


}
