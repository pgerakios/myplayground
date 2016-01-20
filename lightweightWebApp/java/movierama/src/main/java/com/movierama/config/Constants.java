package com.movierama.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {
    public static final Logger LOGGER = LoggerFactory.getLogger(Constants.class);
    public static final String DEFAULT_LANG_JSON_TRANSLATION = "/resources/lang_en.json";
    public static final String LANG_JSON_TRANSLATION_PATH_FORMAT = "/resources/%s.json";
    public static final String PROPERTY_PACKAGES = "com.movierama.rest.resources";
    //TODO: move these to config
    public static final Boolean DEV_MODE = false;
    public static final Boolean MOCK_MODE = DEV_MODE && false; //true;
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String DEFAULT_CONFIG_JSON_FILE_PATH = "/resources/config.json";
    public static final String CONFIG_JSON_PROPERTY = "config.json.path";
    public static final String WEB_APP_DIR_PROPERTY = "webapp.dir";
}
