package com.movierama.rest.boundary;

import com.movierama.config.Constants;
import com.movierama.config.Environment;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.persistence.manage.CacheManager;
import com.movierama.rest.dto.*;
import com.movierama.rest.exception.InvalidQueryException;

import com.movierama.util.io.JsonIO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class AggregatorBoundary {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorBoundary.class);
    private static final Environment env = Environment.env();
    private static final FlatMapDTO translations = JsonIO.unmarshallJSONFile(FlatMapDTO.class, env.getConfigDTO().getPathOfTranslationJSON());
    final MoviesConnector moviesConnector = Environment.env().moviesConnector();
    final CacheManager cacheManager = Environment.env().cacheManager();


    public void throttleFilter() throws InvalidQueryException  {
        // TODO: implement a filter allowing X requests per second.
    }

    private List<MovieDTO> filterMovies(List<MovieDTO> movies) {
        List<MovieDTO> retVal = new ArrayList<>();
        // TODO: this logic should be placed in the Cache. There's no point storing bad results...
        // Except that we don't have to re-fetch them !!!!
        for(MovieDTO movieDTO : movies) {
            boolean isRejected = true;
            do {
                if (StringUtils.isEmpty(movieDTO.getCast()) || movieDTO.getCast().trim().isEmpty()) {
                    break;
                }
                if (StringUtils.isEmpty(movieDTO.getTitle())) {
                    break;
                }
                if (StringUtils.isEmpty(movieDTO.getDescription())) {
                    break;
                }
                if (movieDTO.getYearOfProduction() == null) {
                    break;
                }
                if (movieDTO.getDuration() == null) {
                    break;
                }
                // define more complext filters here
                isRejected = false;
            }while(false);
            if(!isRejected) {
                if (Constants.DEV_MODE) {
                    LOGGER.info("Movie passed the filter: " + movieDTO);
                }
                retVal.add(movieDTO);
            } else {
                if (Constants.DEV_MODE) {
                    LOGGER.info("Movie REJECTED by the filter: " + movieDTO);
                }
            }

        }
        return retVal;
    }

    private QueryResultDTO handleRequest(QueryType queryType, String ...args) throws InvalidQueryException {
        try {
            List<MovieDTO> movies = moviesConnector.getMovies(queryType, args);
            movies = filterMovies(movies);
            QueryResultDTO queryResultDTO = new QueryResultDTO();
            queryResultDTO.setMovies(movies);
            queryResultDTO.setResultsFound(movies.size());
            queryResultDTO.setQueryType(queryType);
            queryResultDTO.setError(false);
            return queryResultDTO;
        } catch (MoviesConnector.ProviderException e) {
            LOGGER.info("An error has occurred during the request", e);
            throw new InvalidQueryException();
        }
    }
    public QueryResultDTO handleNowPlayingRequest() throws InvalidQueryException {
        return handleRequest(QueryType.NOW_PLAYING);
    }

    public QueryResultDTO handleFindMovieByTitleRequest(FindByTitleDTO findByTitleDTO) throws InvalidQueryException {
        return handleRequest(QueryType.BY_TITLE, findByTitleDTO.getTitle());
    }

    /* TODO: config file listener for updates */
    public FlatMapDTO handleTranslation() {
        return translations;
    }
}
