package com.movierama.persistence.manage.impl;

import com.movierama.config.Constants;
import com.movierama.config.Environment;
import com.movierama.config.dto.CacheConfigDTO;
import com.movierama.config.dto.PersistenceConfigDTO;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.persistence.manage.CacheManager;
import com.movierama.rest.dto.MovieDTO;
import com.movierama.util.Var;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.*;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class CacheManagerImpl implements CacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheManagerImpl.class);
    final static String NOW_PLAYING_ID = "SDASl01wsdnfD34e435435039djsmasnd123@#^#%";
    final EmbeddedCacheManager manager;
    private Cache<String, Long> throttleCacheImpl;
    private Cache<String, Set<MovieDTO>> moviesCacheImpl;
    private Cache<String, byte[]> fileCacheImpl;
    private Cache<String, Set<String>> keywordCacheImpl;


    final static Set<String> commonWordsDictionary = new TreeSet<String>() {{
        add("this");
        add("that");
        add("is");
        add("the");
        add("for");
        // add more here
    }};

    // simple heuristics for removing noisy tokens
    // we assume that tokens contain no symbols
    static SortedSet<String> filterTokens(SortedSet<String> tokens) {
        if (tokens.size() < 2) {
            return tokens;
        }
        int numberOfSingleLetterTokens = 0;

        int totalTokens = 0;
        SortedSet<String> retVal = new TreeSet<>();
        for (String token : tokens) {
            if (token.length() == 1) numberOfSingleLetterTokens++;
            totalTokens++;
        }
        if (numberOfSingleLetterTokens <= totalTokens / 2) { // we don't loose too much information
            for (String token : tokens) {
                if (token.length() > 1) {
                    retVal.add(token);
                }
            }
        } else {
            retVal.addAll(tokens);
        }
        // we repeat the same code ... this is the same logic and must be placed in different function
        tokens = new TreeSet<>(retVal);
        retVal.clear();
        totalTokens = 0;
        int numberOfCommonTokens = 0;
        for (String token : tokens) {
            if (commonWordsDictionary.contains(token)) numberOfCommonTokens++;
            totalTokens++;
        }
        if (numberOfCommonTokens <= totalTokens / 2) { // we don't loose too much information
            for (String token : tokens) {
                if (!commonWordsDictionary.contains(token)) {
                    retVal.add(token);
                }
            }
        } else {
            retVal.addAll(tokens);
        }
        final int K_TOKENS = 4;
        // also maybe limit the size of tokens: choose the k longest words
        tokens = new TreeSet<>(retVal);
        retVal.clear();
        SortedMap<Integer, Set<String>> sorted = new TreeMap<>();
        for (String token : tokens) {
            int len = token.length();
            Set<String> other = sorted.get(len);
            if (other == null) {
                other = new HashSet<>();
                sorted.put(-len, other);
            }
            other.add(token);
        }
        final int max = Math.min(tokens.size(), K_TOKENS);
        Iterator<Integer> keys = sorted.keySet().iterator();
        Iterator<String> vals = null;

        for (int i = 0; i < max; i++) {
            if (vals == null || !vals.hasNext()) {
                vals = sorted.get(keys.next()).iterator();
            }
            retVal.add(vals.next());
        }
        return retVal;
    }

    /* use more advanced search built on top of the original cache */
    void trackKeywords(String query, MovieDTO movie) {
        SortedSet<String> tokens = MovieDTO.tokenize(query);
        SortedSet<String> bestTokens = filterTokens(tokens);
        String movieId = MovieDTO.normalize(movie.getTitle());
        // create an inverse index
        for (String bestToken : bestTokens) {
            Set<String> empty = new ConcurrentSkipListSet<>();
            Set<String> movieIds = keywordCacheImpl.putIfAbsent(bestToken, empty);
            if (movieIds == null) {
                movieIds = empty;
            }
            movieIds.add(movieId);
        }
    }

    static double ratio(int missCount, int totalCount) {
        if (missCount < 0 || totalCount < 0) return 1.0;
        return (double) missCount / (double) totalCount;
    }


    static boolean tooManyMisses(int missCount, int totalCount) {
        return ratio(missCount, totalCount) >= 0.3;
    }

    /* TODO: there could be a second-level cache, caching the bestCandidates matches.*/
    Set<MovieDTO> bestCandidates(String query) {
        SortedSet<String> tokens = MovieDTO.tokenize(query);
        SortedSet<String> bestTokens = filterTokens(tokens);
        String id = MovieDTO.normalize(query);

        Set<String> intersection = null; //new HashSet<>();
        int allTokens = bestTokens.size();
        int misses = 0;
        int bestTokensLen = 0;
        for (String bestToken : bestTokens) {
            bestTokensLen += bestToken.length();
            Set<String> items = keywordCacheImpl.get(bestToken);
            if (items == null) {
                misses++;
                if (tooManyMisses(misses, allTokens)) {
                    return Collections.EMPTY_SET;
                }
                continue;
            }

            if (intersection == null) {
                intersection = new HashSet<>(items);
            } else {
                intersection.retainAll(items);
            }
            if (intersection.size() < 1)
                return Collections.emptySet();

        }
        if (intersection == null || tooManyMisses(misses, allTokens)) {
            return Collections.EMPTY_SET;
        }
        // collect the movies that seem more appropriate using another heuristic
        // on the normalized title length
        final int MAX_RESULTS = 10;
        PriorityQueue<MovieDTO> movies = new PriorityQueue<>();
        for (String movieId : intersection) {
            Set<MovieDTO> movies_ = moviesCacheImpl.get(movieId);
            if (movies_ == null)
                continue;
            for (MovieDTO movieDTO : movies_) {
                String norm = MovieDTO.normalize(movieDTO.getTitle());
                int normLen = norm.length();
                if (!tooManyMisses(normLen - bestTokensLen, normLen)) {
                    if (movies.size() < MAX_RESULTS) {
                        movies.add(movieDTO);
                    } else {
                        MovieDTO candidate = movies.peek();
                        String candidateId = MovieDTO.normalize(candidate.getTitle());
                        int candidateIdLen = candidateId.length();
                        boolean evict = candidateIdLen > normLen;
                        if (evict) {
                            movies.remove();
                            movies.add(movieDTO);
                        }
                    }
                }
            }
        }
        // collect top X movies having the largest number of tokens
        return new ConcurrentSkipListSet<>(movies);
    }

    final MovieCache moviesCache = new MovieCache() {

        public Set<MovieDTO> find(String query) {
            if (query == null) {
                if (Constants.DEV_MODE) {
                    LOGGER.info("[CACHE] NULL Query  !!");
                }
                return Collections.EMPTY_SET;
            }
            String id = MovieDTO.normalize(query);
            Set<MovieDTO> movies = moviesCacheImpl.get(id);
            if (movies == null) {
                movies = new ConcurrentSkipListSet<>();
                moviesCacheImpl.put(id, movies);
                if (Constants.DEV_MODE) {
                    LOGGER.info("[CACHE] Query with \"" + id + "\" NOT successful.");
                }
            } else {
                if (Constants.DEV_MODE) {
                    LOGGER.info("[CACHE] Query with \"" + id + "\" successful size=" + movies.size());
                }

            }
            return movies;
        }

        @Override
        public Set<MovieDTO> moviesByTitle(String query) {
            Set<MovieDTO> retVal = find(query);
            if (retVal.size() < 1) {
                try { /* This is an experimental feature and has not been tested */
                    retVal = CacheManagerImpl.this.bestCandidates(query); /* create inverse indeces */
                    if (Constants.DEV_MODE) {
                        LOGGER.info("Invoked best candidates and found " + retVal.size() + " results");
                    }
                } catch(Throwable t) {
                }
            }
            return retVal;
        }

        @Override
        public Set<MovieDTO> moviesByNowPlaying() {
            Set<MovieDTO> retVal = find(NOW_PLAYING_ID);
            return retVal;
        }

        @Override
        public void addMovie(MovieDTO movie, String... args) {
            if (args.length == 0) {
                Set<MovieDTO> movies = find(NOW_PLAYING_ID);
                movies.add(movie);
                args = new String[]{movie.getTitle()};
            }
            Set<MovieDTO> movies = find(args[0]);
            movies.add(movie);
            try { /* This is an experimental feature and has not been tested */
                CacheManagerImpl.this.trackKeywords(args[0], movie); /* create inverse indeces */
            } catch(Throwable t) {

            }
        }
    };

    final FileCache fileCache = new FileCache() {
        @Override
        public byte[] getFile(String id) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public void putFile(String id, byte[] content) {
            throw new RuntimeException("Not implemented");
        }
    };

    final ThrottleCache throttleCache = new ThrottleCache() {
        @Override
        public void track(String id, Integer count) {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public Integer find(String id) {
            throw new RuntimeException("Not implemented");
        }
    };

    private <K, V> Cache<K, V> prepareCache(CacheConfigDTO cacheConfig) {
        String cacheType = cacheConfig.getCacheType().toValue();
        String path = Environment.env().rootPath() + cacheConfig.getDataFile();
        final PersistenceConfigurationBuilder config = new ConfigurationBuilder().persistence();
        final SingleFileStoreConfigurationBuilder fileStore = new SingleFileStoreConfigurationBuilder(config).location(path);

        Long lifespanValue = cacheConfig.getDefaultLifespan();
        Long maxIdleValue = cacheConfig.getMaxIdle();
        Integer maxEntriesValue = cacheConfig.getMaxEntries();

        if (lifespanValue != null)
            config.expiration().lifespan(lifespanValue, TimeUnit.MILLISECONDS);
        if (maxIdleValue != null)
            config.expiration().maxIdle(maxIdleValue, TimeUnit.MILLISECONDS);
        if (maxEntriesValue != null)
            config.eviction().maxEntries(maxEntriesValue);

        config.eviction().strategy(EvictionStrategy.LRU);

        manager.defineConfiguration(cacheType, config.addStore(fileStore).compatibility().enable().build());
        return manager.getCache(cacheType);
    }

    public CacheManagerImpl() {
        try {
            final Map<CacheConfigDTO.CacheType, CacheConfigDTO> caches = CacheConfigDTO.toMap(Environment.env().getConfigDTO().getPersistenceInfo().getCaches());

            manager = new DefaultCacheManager();
            throttleCacheImpl = prepareCache(caches.get(CacheConfigDTO.CacheType.THROTTLE_CACHE));
            moviesCacheImpl = prepareCache(caches.get(CacheConfigDTO.CacheType.MOVIE_CACHE));
            fileCacheImpl = prepareCache(caches.get(CacheConfigDTO.CacheType.FILE_CACHE));
            keywordCacheImpl = prepareCache(caches.get(CacheConfigDTO.CacheType.KEYWORD_CACHE));
            LOGGER.info("Initialized Cache service");

        } catch (Throwable e) {
            LOGGER.error("Could not initialize cache", e);
            throw new RuntimeException();
        }
    }

    @Override
    public MovieCache getMovieCache() {
        return moviesCache;
    }

    @Override
    public FileCache getFileCache() {
        return fileCache;
    }

    @Override
    public ThrottleCache getThrottleCache() {
        return throttleCache;
    }
}
