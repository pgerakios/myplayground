package com.movierama.config;

import com.movierama.util.enums.EnumID;
import com.movierama.util.enums.EnumIDMap;

import static com.movierama.util.enums.EnumIDMap.bind;

public enum EnvVar implements EnumID{
    CONFIG_FILE_PATH,
    WEBAPP_DIR;
    static EnumIDMap<EnvVar> ids=bind().bind(CONFIG_FILE_PATH, Constants.CONFIG_JSON_PROPERTY).
            bind(WEBAPP_DIR, Constants.WEB_APP_DIR_PROPERTY);

    @Override
    public String getID(){
        return ids.idOf(this);
    }
}
