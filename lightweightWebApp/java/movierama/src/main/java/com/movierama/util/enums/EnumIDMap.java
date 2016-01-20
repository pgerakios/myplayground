package com.movierama.util.enums;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EnumIDMap<E extends Enum> {

    final Map<String, E> enumMap = new ConcurrentHashMap<>();
    final Map<Integer, String> idMap = new ConcurrentHashMap<>();

    public static <K extends Enum> EnumIDMap<K> bind() {
        return new EnumIDMap<>();
    }

    private EnumIDMap() {

    }

    public EnumIDMap bind(E e, String id) {
        enumMap.put(id, e);
        idMap.put(System.identityHashCode(e), id);
        return this;
    }

    public String idOf(E e) {
        final String retVal = idMap.get(System.identityHashCode(e));
        return retVal;
    }

    public E enumOf(final String id) {
        E retVal = enumMap.get(id);
        return retVal;
    }
}
