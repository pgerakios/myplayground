package com.movierama.util.io;

import com.movierama.util.Var;
import com.movierama.util.datastructure.StringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesIO.class);

    public static StringMap ofFile(final String propFileName) {
        Properties prop = new Properties();
        try(InputStream inputStream= Var.openResource(propFileName)) {

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (Exception e) {
            LOGGER.info("An error has occurred", e);
        }

        StringMap retVal = ofProperties(prop);
        return retVal;
    }

    public static StringMap ofProperties(Properties prop) {
        StringMap retVal = new StringMap();
        for(String key: prop.stringPropertyNames()) {
            retVal.put(key, prop.getProperty(key));
        }
        return retVal;
    }
}
