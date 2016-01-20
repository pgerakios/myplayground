package com.movierama.config.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movierama.config.Constants;
import com.movierama.util.Var;
import com.movierama.util.io.JsonIO;
import lombok.Data;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
public class ConfigDTO {
    LocalServiceConfigDTO serviceInfo = new LocalServiceConfigDTO();
    PersistenceConfigDTO persistenceInfo = new PersistenceConfigDTO();
    IntegrationConfigDTO integrationInfos = new IntegrationConfigDTO();
    String language;
    Integer threadPoolSize;
    Integer restServiceTimeoutMillis;

    public static ConfigDTO ofPath(final String path) {
        return JsonIO.unmarshallJSONFile(ConfigDTO.class, path);
    }

    @JsonIgnore
    public String getPathOfTranslationJSON() {
        return String.format(Constants.LANG_JSON_TRANSLATION_PATH_FORMAT, language);
    }
}
