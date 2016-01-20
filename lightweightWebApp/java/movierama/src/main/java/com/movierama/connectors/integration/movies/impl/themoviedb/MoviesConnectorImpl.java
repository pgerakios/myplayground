package com.movierama.connectors.integration.movies.impl.themoviedb;

import com.movierama.config.Constants;
import com.movierama.config.Environment;
import com.movierama.connectors.integration.movies.MoviesConnector;
import com.movierama.connectors.integration.movies.impl.ProviderType;
import com.movierama.rest.dto.MovieDTO;
import com.movierama.rest.dto.QueryResultDTO;
import com.movierama.rest.dto.QueryType;
import com.movierama.rest.dto.RestParam;
import com.movierama.util.Var;
import com.movierama.util.io.JsonIO;
import com.movierama.util.io.RestIO;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class MoviesConnectorImpl implements MoviesConnector{
    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesConnectorImpl.class);
    @Override
    public List<MovieDTO> getMovies(com.movierama.rest.dto.QueryType type, String... args) throws ProviderException {
        List<MovieDTO> retVal = null;
        if(Constants.MOCK_MODE) {
            return QueryResultDTO.sampleQueryResult().getMovies();
        }
        try {
            switch (type) {
                case NOW_PLAYING:
                    retVal = getNowPlaying();
                    break;
                case BY_TITLE:
                    retVal = getMoviesByTitle(args[0]);
                    break;
                default:
                    throw new ProviderException(ErrorType.GENERIC,"Internal error");
            }
            return retVal;
        }catch(Throwable t) {
            LOGGER.error("An exception has occured", t);
            throw new ProviderException(ErrorType.GENERIC,"An exception has occurred");
        }

    }


    private static String getActors(JSONArray abridgedCast) throws Exception {
        String actors = "";
        List<String> names = new ArrayList<>();
        for (int i = 0; i < abridgedCast.length(); i++) {
            String name = abridgedCast.getJSONObject(i).getString("name");
            names.add(name);

        }
        names = Var.sortNames(names);
        for (String name : names) {
            actors +=  name + ", ";
        }

        int i = actors.lastIndexOf(", ");
        if (i > -1) {
            actors = actors.substring(0, i);
        }
        return actors;
    }

    private static String getActors(String id) throws Exception {
        String jsonString = RestIO.restGETString(ProviderType.THEMOVIEDB.requestURL(QueryType.CAST_BYID, RestParam.ID.getId(), id));
        JSONObject cast = JsonIO.toJsonObject(jsonString);
        return getActors(cast.getJSONArray("cast"));
    }

    private static MovieDTO getMovieById(String id){

        String jsonString = RestIO.restGETString(ProviderType.THEMOVIEDB.requestURL(QueryType.BY_ID, RestParam.ID.getId(), id));
        JSONObject movie = JsonIO.toJsonObject(jsonString);

        try {
            String title = movie.getString("title");
            Integer year = Integer.valueOf(movie.getString("release_date").trim().substring(0,4));
            Integer duration = movie.getInt("runtime");
            String cast = getActors(id);
            String description = movie.getString("overview");
            Integer reviews = movie.getInt("vote_count");

            MovieDTO movieDTO = new MovieDTO();
            movieDTO.setTitle(title);
            movieDTO.setYearOfProduction(year);
            movieDTO.setDuration(duration);
            movieDTO.setCast(cast);
            movieDTO.setDescription(description);
            movieDTO.setNumberOfReviews(reviews);
            movieDTO.setId(MovieDTO.id(movieDTO));
            return movieDTO;
        } catch (Throwable t) {
            Constants.LOGGER.info("Error!", t); // continue loop execution on parse error
        }
        return null;
    }

    public static List<MovieDTO> getMoviesByTitle(String query) {

        String jsonString = RestIO.restGETString(ProviderType.THEMOVIEDB.requestURL(QueryType.BY_TITLE, RestParam.QUERY.getId(), query));
        JSONObject obj = JsonIO.toJsonObject(jsonString);
        List<MovieDTO> movieDTOS = new ArrayList<>();
        try {
            JSONArray movies = obj.getJSONArray("results");
            for (int i = 0; i < movies.length(); i++) {
                try {
                    JSONObject movie = movies.getJSONObject(i);
                    String id = movie.getString("id");
                    MovieDTO movieDTO =  getMovieById(id);
                    String normalizedQuery = MovieDTO.normalize(query);
                    String normalizedTitle = MovieDTO.normalize(movieDTO.getTitle());
                    if (!MovieDTO.isMatchingTitle(normalizedQuery, normalizedTitle)) {
                        if (Constants.DEV_MODE) {
                            Constants.LOGGER.info("Rejected movie with title \"" + normalizedTitle + "\" for query: " +
                                    normalizedQuery);
                        }
                        continue; /* not relevant result */
                    }
                    movieDTOS.add(movieDTO);
                } catch(Throwable t) {
                    Constants.LOGGER.info("Error!", t); // continue loop execution on parse error
                }
            }
        } catch (JSONException e) {
            Constants.LOGGER.info("Error!", e);
        }
        return movieDTOS;
    }

    public static List<MovieDTO> getNowPlaying() {

        String jsonString = RestIO.restGETString(ProviderType.THEMOVIEDB.requestURL(QueryType.NOW_PLAYING));
        JSONObject obj = JsonIO.toJsonObject(jsonString);
        List<MovieDTO> movieDTOS = new ArrayList<>();
        try {
            JSONArray movies = obj.getJSONArray("results");
            for (int i = 0; i < movies.length(); i++) {
                try {
                    JSONObject movie = movies.getJSONObject(i);
                    String id = movie.getString("id");
                    MovieDTO movieDTO =  getMovieById(id);
                    movieDTOS.add(movieDTO);
                } catch(Throwable t) {
                    Constants.LOGGER.info("Error!", t); // continue loop execution on parse error
                }
            }
        } catch (JSONException e) {
            Constants.LOGGER.info("Error!", e);
        }
        return movieDTOS;
    }

    private static void lsMovies(MoviesConnector moviesConnector, com.movierama.rest.dto.QueryType queryType, String ...args) throws ProviderException {
        System.out.println("Query type " + queryType + " args = " + Var.toList(args));
        List<MovieDTO>  movies = moviesConnector.getMovies(queryType, args);
        System.out.println("Found " + movies.size() + " results");
        int i = 0;
        for(MovieDTO movieDTO : movies){
            System.out.println("#" + (++i) +" :" +  movieDTO);
        }
    }
}
