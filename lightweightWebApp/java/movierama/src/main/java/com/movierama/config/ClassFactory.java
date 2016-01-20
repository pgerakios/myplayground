package com.movierama.config;

import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.connectors.integration.movies.impl.CachedMoviesConnectorImpl;
import com.movierama.persistence.manage.CacheManager;
import com.movierama.persistence.manage.impl.CacheManagerImpl;
import com.movierama.util.datastructure.Klass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassFactory.class);
    private static final Map<Klass<?>, Class<?>> instMap = new ConcurrentHashMap<>();

    public static Map<Klass<?>, Class<?>> classMap() {
        return instMap;
    }
    static {
        ClassFactory.classMap().put(new Klass(MoviesConnector.class), CachedMoviesConnectorImpl.class);
        ClassFactory.classMap().put(new Klass(CacheManager.class), CacheManagerImpl.class);
    }
    public static <A> A instance(Class<?> c) {
        Class<?> impl = classMap().get(new Klass(c));
        try {
            A retVal = (A) impl.newInstance();
            return retVal;
        } catch (InstantiationException e) {
           LOGGER.error("Instantiation error", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Illegal access error", e);
        } catch(Throwable t){
            LOGGER.error("Throwable from classMap class = " + c.getName(), t);
        }
        return null;
    }
}
