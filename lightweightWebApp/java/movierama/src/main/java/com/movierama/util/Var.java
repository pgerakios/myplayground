package com.movierama.util;

import com.movierama.config.Constants;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.omg.CORBA.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Var {
    private static final Logger LOGGER = LoggerFactory.getLogger(Var.class);
    private static final Class[] parameters = new Class[]{URL.class};
    private static final Calendar cal = Calendar.getInstance();

    public static String encodeURI(String uri) {
        try {
            return URLEncoder.encode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unspported encoding exception", e);
            return null;
        }
    }

    public static <A> A[] toArray(Class<A> klass, Collection<A> collection) {
        final int size = collection.size();
        Object o = Array.newInstance(klass, size);
        Iterator<A> it = collection.iterator();
        for (int i = 0; i < size; i++) {
            Array.set(o, i, it.next());
        }
        return (A[]) o;
    }

    public static <A> A[] toArray(Class<A> klass, List<A> collection) {
        final int size = collection.size();
        Object o = Array.newInstance(klass, size);
        Iterator<A> it = collection.iterator();
        for (int i = 0; i < size; i++) {
            Array.set(o, i, it.next());
        }
        return (A[]) o;
    }

    public static <A> A[] toArray(List<A> collection) {
        Class<?> cls = null;
        for (A arg : collection) {
            if (arg != null) {
                cls = arg.getClass();
                break;
            }
        }
        if (cls == null) {
            throw new RuntimeException();
        }
        return toArray((Class<A>) cls, collection);
    }

    public static <A> A[] toArray(Collection<A> collection) {
        Class<?> cls = null;
        for (A arg : collection) {
            if (arg != null) {
                cls = arg.getClass();
                break;
            }
        }
        if (cls == null) {
            throw new RuntimeException();
        }
        return toArray((Class<A>) cls, collection);
    }

    public static <A> List<A> toList(Collection<A> collection) {
        if (collection == null)
            return null;
        List<A> retVal = new ArrayList<>();
        retVal.addAll(collection);
        return retVal;
    }

    public static <A> List<A> toList(A... args) {
        return Arrays.asList(args);
    }

    public static String eatWhitespace(String str) {
        return str.replaceAll("\\s+", "");
    }

    public static String symbolsToSpace(String str) {
        return str.replaceAll("[^A-Za-z0-9]", " ");
    }

    public static List<String> split(String dest, String pattern) {
        return toList(dest.split((pattern)));
    }

    public static String getHostName() {
        String hostName = "localhost";
        try {
            hostName = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
        }
        return hostName;
    }

    public static void addUrl(String u) {
        try {
            addUrl(new File(u).toURI().toURL());
        } catch (MalformedURLException e) {
            LOGGER.error("Failed to add URL to classpath " + e.getMessage());
        }
    }

    public static void addUrl(URL u) {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader
                .getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[]{u});
        } catch (NoSuchMethodException e) {
            LOGGER.error("Failed to add URL to classpath", e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.error("Failed to add URL to classpath", e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.error("Failed to add URL to classpath", e.getMessage());
        }
    }

    public static InputStream openResource(String path) {
        String rootPath = com.movierama.config.Environment.env().rootPath();
        String finalPath = rootPath + path;
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(finalPath);
            LOGGER.error("Opened file: " + finalPath);
            return inputStream;
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found: " + finalPath, e.getMessage());
            return null;
        }
        //return  ClassLoader.getSystemClassLoader().getSystemResourceAsStream(path);
    }

    public static Date parseDate(DateFormat dateFormat, String dateString) {
        try {
            Date date = dateFormat.parse(dateString);
            return date;
        } catch (ParseException parseException) {
            LOGGER.info("Bad date format: " + dateString + " formatter: " + dateFormat);
            return null;
        } catch (Throwable parseException) {
            LOGGER.info("Bad date format: " + dateString + " formatter: " + dateFormat);
            return null;
        }
    }

    public static Integer yearOf(Date date) {
        if(date == null) {
            return null;
        } else {
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
//            int month = cal.get(Calendar.MONTH);
//            int day = cal.get(Calendar.DAY_OF_MONTH);
            return year;
        }
    }

    public static Integer tryParseInt(String str) {
        if(str == null) {
            return null;
        }
        try {
            Integer retVal = Integer.valueOf(str);
            return retVal;
        } catch (Throwable t) {
            if (Constants.DEV_MODE) {
                Constants.LOGGER.info("Failed to parse integer :" + str);
            }
            return null;
        }
    }

    public static String substParamsOfTemplate(String templateString, Map<String, String> valuesMap) {
//        if(Constants.DEV_MODE) {
//            LOGGER.info("subst Template. Params = " + valuesMap + " template = " + templateString);
//        }
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String resolvedString = sub.replace(templateString);
        return resolvedString;
    }

    public static List<String> sortNames(List<String> names) throws Exception {
        Map<String, String> map = new TreeMap<String, String>();
        for(String name : names) {
            String tokens[] = name.split("[\\s']");
            String surname = tokens[tokens.length-1];
            map.put(surname, name);
        }
        List<String> retVal = new ArrayList<>();
        for(String actorId : map.keySet()) {
            retVal.add(map.get(actorId));
        }
        return retVal;
    }

}
