package com.movierama.connectors.integration.movies;

import com.movierama.rest.dto.MovieDTO;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

public interface MoviesConnector {

    public enum ErrorType {
        TIMEOUT,
        INVALIDHOST,
        CLOSED,
        JSONFORMAT,
        GENERIC;
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    public static class ProviderException extends Exception {
        private ErrorType errorType;
        private String message;

        public ProviderException() {

        }
    }

    public List<MovieDTO> getMovies(com.movierama.rest.dto.QueryType type, String ... args) throws ProviderException;

}
