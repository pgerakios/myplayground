package com.movierama.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.movierama.config.Constants;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestIO.class);

    public static <A> A restGET(Class<A> klass, final String url) {
        String jsonString = restGETString(url);
        A retVal = JsonIO.unmarshallJSONString(klass, jsonString);
        return retVal;
    }

    public static String restGETString(final String url) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);

            String line = "";

            if(Constants.DEV_MODE) {
                LOGGER.info("restGET: " + request);
            }

            HttpResponse response = null;
            response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String jsonString = "";
            while ((line = rd.readLine()) != null) {
                jsonString += line;
            }

            return jsonString;
        } catch (IOException e) {
            LOGGER.error("An IO exception has occurred in restGETString", e);
            return null;
        }
    }
}
