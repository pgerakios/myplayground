package com.movierama.connectors.integration.movies.impl;

import com.movierama.config.Constants;
import com.movierama.config.Environment;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.rest.dto.MovieDTO;
import com.movierama.rest.dto.QueryType;
import com.movierama.util.Var;
import com.movierama.util.datastructure.Promises;
import com.movierama.util.datastructure.functional.PureFun;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MoviesConnectorImpl implements MoviesConnector {

    public interface MovieList extends List<MovieDTO> {
    }

    public static class MovieListImpl extends ArrayList<MovieDTO> implements MovieList {
    }

    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesConnectorImpl.class);

    private final Map<ProviderType, MoviesConnector> specificConnectors = new ConcurrentHashMap<ProviderType, MoviesConnector>() {{
        this.put(ProviderType.ROTTENTOMATOES, new com.movierama.connectors.integration.movies.impl.rottentomatoes.MoviesConnectorImpl());
        this.put(ProviderType.THEMOVIEDB, new com.movierama.connectors.integration.movies.impl.themoviedb.MoviesConnectorImpl());
    }};


    @Override
    public List<MovieDTO> getMovies(final QueryType type, final String... args) throws ProviderException {
        Map<ProviderType, MovieList> map = ProviderType.instance();
        final ConcurrentHashMap<String, MovieDTO> localDB = new ConcurrentHashMap<>();
        final CountDownLatch latch = new CountDownLatch(ProviderType.values().length);
        final List<Runnable> providers = new ArrayList<>();
        // create a list of promises, which will be executed concurrently. Our localDB will also be
        // concurrently updated.
        for (final ProviderType providerType : ProviderType.values()) {
            //final MovieList movieList = map.get(providerType);
            providers.add(new Runnable() {
                @Override
                public void run() {
                    // assumption: a movie ID cannot exist twice in the same result set. The hashing algorithm should
                    // guarantee this.
                    try {
                        List<MovieDTO> providerMovies = specificConnectors.get(providerType).getMovies(type, args);
                        for (final MovieDTO movie : providerMovies) {
                            final String id = id(movie);
                            final MovieDTO storedMovie = localDB.putIfAbsent(id, movie);
                            if (storedMovie != null) { /* already another movie with the same id*/
                                if(storedMovie != movie) {
                                    synchronized (storedMovie) { // avoid data races, don't want to update at the same time. We place the results on the stored movie.
                                        storedMovie.setDescription(mergeDescriptions(storedMovie.getDescription(), movie.getDescription()));
                                        storedMovie.setNumberOfReviews(mergeReviews(storedMovie.getNumberOfReviews(), movie.getNumberOfReviews()));
                                    }
                                    LOGGER.info("Merged movies : " + movie.getTitle() + " with another movie " + storedMovie.getTitle());
                                } // else no action
                            } else {
                                LOGGER.info("Added movies : " + movie.getTitle());
                            }
                        }

                    } catch (ProviderException e) {
                        LOGGER.error("Subprovider exception", e);
                    } finally {
                        latch.countDown();
                    }
                }

                private String mergeDescriptions(String descr1, String descr2) {
                    descr1 = org.apache.commons.lang3.StringUtils.isEmpty(descr1) ? "" : descr1.trim();
                    descr2 = org.apache.commons.lang3.StringUtils.isEmpty(descr2) ? "" : descr2.trim();
                    return descr1.length() > descr2.length() ? descr1 : descr2;
                }

                private Integer mergeReviews(Integer rev1, Integer rev2) {
                    rev1 = rev1 == null ? 0 : rev1;
                    rev2 = rev2 == null ? 0 : rev2;
                    return rev1 + rev2;
                }
            });
        }

        /* spawn the requests async */
        for(Runnable provider : providers) {
            Environment.env().pool().add(provider);
        }
        /* and wait */
        int waitTime = Environment.env().getConfigDTO().getRestServiceTimeoutMillis();
        LOGGER.info("Waiting for results ... timeout = " + (waitTime < 1000? (waitTime + " millis"):(((waitTime / 1000) + (" seconds")))));
        // wait for the requests to be concurrently executed
        try {
            latch.await(waitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            LOGGER.info("Request thread interrupted (wait) ", e);
        }
        LOGGER.info("Returned from wait");
        // and use the results from our localDB
        MovieList retVal = new MovieListImpl();
        for (MovieDTO movie : localDB.values()) {
            retVal.add(movie);
        }
        return retVal;
    }

    private String id(MovieDTO movieDTO) {
        String originalId = movieDTO.getId();
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(originalId)) {
            return originalId;
        }
        String summary = MovieDTO.id(movieDTO);
        movieDTO.setId(summary);
        return summary;
    }
}
