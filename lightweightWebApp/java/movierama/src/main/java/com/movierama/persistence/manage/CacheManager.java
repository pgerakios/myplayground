package com.movierama.persistence.manage;

import com.movierama.rest.dto.MovieDTO;

import java.util.List;
import java.util.Set;

public interface CacheManager {

    public interface MovieCache {
        Set<MovieDTO> moviesByTitle(String query);
        Set<MovieDTO> moviesByNowPlaying();
        void addMovie(MovieDTO movie, String...args); /* if args, then not now playing (we have a query)*/
    }
    public interface FileCache {
        byte[] getFile(String id);
        void putFile(String id, byte[] content);
    }
    public interface ThrottleCache {
        void track(String id, Integer count);
        Integer find(String id);
    }

    public MovieCache getMovieCache();
    public FileCache getFileCache();
    public ThrottleCache getThrottleCache();
}
