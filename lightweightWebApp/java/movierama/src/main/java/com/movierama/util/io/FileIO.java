package com.movierama.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileIO {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileIO.class);

    public static String readFile(String path) {
        String retVal = readFile(path, Charset.defaultCharset());
        return retVal;
    }

    public static String readFile(String path, Charset encoding)   {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            LOGGER.error("IO exception", e);
        }
        return new String(encoded, encoding);
    }
}
