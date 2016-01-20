package com.movierama.util.io;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.movierama.config.Constants;
import com.movierama.util.Var;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonIO.class);

    public static String marshallJSON(final Object obj) {
        String retVal = marshallJSON(obj, Constants.DEV_MODE);
        return retVal;
    }

    public static String marshallJSON(final Object obj, final boolean pretty) {
        ObjectWriter objectWriter;
        if(pretty) {
          objectWriter  = new ObjectMapper().writer().withDefaultPrettyPrinter();
        }  else {
            objectWriter  = new ObjectMapper().writer();
        }
        String retVal = null;
        try {
            retVal = objectWriter.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            LOGGER.error("marshallJSON", e);
        }
        return retVal;
    }

    public static <A> A unmarshallJSONString(final Class<A> klass, final String jsonInString){
        ObjectMapper mapper = new ObjectMapper();
        A retVal = null;
        try {
            if(Constants.DEV_MODE){
                LOGGER.info("(umarshall) jsonString: " + jsonInString);
            }
            retVal = mapper.readValue(jsonInString, klass);
        } catch (IOException e) {
            LOGGER.error("unmarshallJSONString", e);
        }
        return retVal;
    }

    public static JSONObject toJsonObject(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            LOGGER.error("toJsonObject exception", e);
            return null;
        }
    }

    public static <A> A unmarshallJSONFile(final Class<A> klass, final String jsonFilePath){
        ObjectMapper mapper = new ObjectMapper();
        A retVal = null;
        try {
            LOGGER.info("unmarshallJSONFile " + jsonFilePath );
            retVal = mapper.readValue(Var.openResource(jsonFilePath), klass);
            if(retVal == null) {
                LOGGER.error("unmarshallJSONFile error PWD=" + System.getProperty("user.dir"));
                //throw new RuntimeException();
            }
        } catch (Throwable e) {
            LOGGER.error("unmarshallJSONFile", e);
        }
        return retVal;
    }
}
