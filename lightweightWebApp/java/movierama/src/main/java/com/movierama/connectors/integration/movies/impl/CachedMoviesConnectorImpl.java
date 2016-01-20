package com.movierama.connectors.integration.movies.impl;

import com.movierama.config.Environment;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.persistence.manage.CacheManager;
import com.movierama.rest.dto.MovieDTO;
import com.movierama.rest.dto.QueryType;
import com.movierama.util.Var;

import java.util.List;
import java.util.Set;

public class CachedMoviesConnectorImpl implements MoviesConnector {

    final CacheManager cacheManager = Environment.env().cacheManager();
    final MoviesConnector physicalConnector = new MoviesConnectorImpl();

    /* This is a pseudo-connector, adding a level of abstraction (caching) to the physical connector (requres net I/O)*/
    @Override
    public List<MovieDTO> getMovies(QueryType type, String... args) throws ProviderException {
        switch(type){
            case NOW_PLAYING: {
                Set<MovieDTO> movies = cacheManager.getMovieCache().moviesByNowPlaying();
                if (movies.size() < 1) {
                    List<MovieDTO> moviesWeb = physicalConnector.getMovies(type, args);
                    for(MovieDTO movieDTO : moviesWeb){
                        cacheManager.getMovieCache().addMovie(movieDTO);
                    }
                    return moviesWeb;
                } else {
                    return Var.toList(movies);
                }
            }
            case BY_TITLE: {
                Set<MovieDTO> movies = cacheManager.getMovieCache().moviesByTitle(args[0]);
                if (movies.size() < 1) {
                    List<MovieDTO> moviesWeb = physicalConnector.getMovies(type, args);
                    for(MovieDTO movieDTO : moviesWeb){
                        cacheManager.getMovieCache().addMovie(movieDTO,args[0]);
                    }
                    return moviesWeb;
                } else {
                    return Var.toList(movies);
                }
            }
            default:
                throw new ProviderException();
        }
    }
}
