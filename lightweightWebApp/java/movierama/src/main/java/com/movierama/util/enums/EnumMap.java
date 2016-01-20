package com.movierama.util.enums;

import com.movierama.util.datastructure.StringMap;
import com.movierama.util.io.PropertiesIO;

import java.util.concurrent.ConcurrentHashMap;

public class EnumMap<E extends Enum & EnumID> extends ConcurrentHashMap<E, String> {

    public static <E extends Enum & EnumID> EnumMap<E> ofProperties(EnumIDMap<E> map, final String propFileName) {
        StringMap props = PropertiesIO.ofFile(propFileName);
        EnumMap<E> retVal = ofStringMap(map, props);
        return retVal;
    }

    public static <E extends Enum & EnumID> EnumMap<E> ofStringMap(EnumIDMap<E> map, StringMap stringMap) {
        EnumMap<E> retVal = new EnumMap<>();

        for (String key : stringMap.keySet()) {
            String value = stringMap.get(key);
            E e = map.enumOf(key);
            if(e == null) {
                continue;
            }
            retVal.put(e, value);
        }
        return retVal;
    }

}
