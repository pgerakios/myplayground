package com.movierama.config;


import com.movierama.config.dto.ConfigDTO;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.persistence.manage.CacheManager;
import com.movierama.util.Var;
import com.movierama.util.datastructure.SimpleTaskExecutor;
import com.movierama.util.io.PropertiesIO;
import lombok.Data;
import com.movierama.util.enums.EnumMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Data
public class Environment {
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);
    private static EnumMap<EnvVar> VARS;
    private ConfigDTO configDTO;
    private static final Environment ENVIRONMENT = new Environment();
    // start services
    private static transient SimpleTaskExecutor POOL;
    private static transient MoviesConnector moviesConnector;
    private static transient CacheManager cacheManager;


    public String rootPath() {
        String webAppDir = VARS.get(EnvVar.WEBAPP_DIR);
        if (StringUtils.isNotEmpty(webAppDir)) {
            return webAppDir + File.separator;
        } else {
            return System.getProperty("user.dir") + File.separator;
        }

    }

    public static void init() {
        VARS = EnumMap.ofStringMap(EnvVar.ids, PropertiesIO.ofProperties(System.getProperties()));
        String webAppDir = VARS.get(EnvVar.WEBAPP_DIR);
        if (StringUtils.isNotEmpty(webAppDir)) {
            LOGGER.info("Found web app path: " + webAppDir);
            Var.addUrl(webAppDir);
        }

        String configFilePath = Environment.VARS.get(EnvVar.CONFIG_FILE_PATH);
        if (StringUtils.isEmpty(configFilePath)) {
            configFilePath = Constants.DEFAULT_CONFIG_JSON_FILE_PATH;
            VARS.put(EnvVar.CONFIG_FILE_PATH, Constants.DEFAULT_CONFIG_JSON_FILE_PATH);
        }
        ENVIRONMENT.configDTO = ConfigDTO.ofPath(configFilePath);
        if (ENVIRONMENT.configDTO == null) {
            ENVIRONMENT.configDTO = new ConfigDTO();
        }
        // start services
        POOL = ENVIRONMENT.pool();
        moviesConnector = ENVIRONMENT.moviesConnector();
        cacheManager = ENVIRONMENT.cacheManager();
    }

    public  MoviesConnector moviesConnector() {
        if (moviesConnector == null) {
            moviesConnector = ClassFactory.instance(MoviesConnector.class);
        }
        return moviesConnector;
    }

    public CacheManager cacheManager() {
        if (cacheManager == null) {
            cacheManager = ClassFactory.instance(CacheManager.class);
        }
        return cacheManager;
    }

    public SimpleTaskExecutor pool() {
        if (POOL == null) {
            POOL = new SimpleTaskExecutor(Environment.env().getConfigDTO().getThreadPoolSize());
        }
        return POOL;
    }

    public static Environment env() {
        return ENVIRONMENT;
    }
}
