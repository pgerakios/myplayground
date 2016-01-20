package com.movierama.connectors.integration.movies.impl.rottentomatoes;

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
import org.apache.commons.lang3.StringUtils;
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


    public static int numberOfReviews(String url)  {
        if(StringUtils.isEmpty(url)) {
            return  0;
        }
        String baseURL = url;
        String jsonString = null;
        try {
            jsonString = RestIO.restGETString(baseURL);
            JSONObject obj = JsonIO.toJsonObject(jsonString);
            Integer retVal = obj.getInt("total");
            return retVal;
        } catch (Throwable t) {
            Constants.LOGGER.info("Error! --> " + jsonString, t);
            return 0; // allow execution to resume, we have less precise information
        }

    }

    public static List<MovieDTO> getNowPlaying() {

        String jsonString = RestIO.restGETString(ProviderType.ROTTENTOMATOES.requestURL(QueryType.NOW_PLAYING));
        JSONObject obj = JsonIO.toJsonObject(jsonString);
        List<MovieDTO> movieDTOS = new ArrayList<>();
        try {
            JSONArray movies = obj.getJSONArray("movies");
            for (int i = 0; i < movies.length(); i++) {
                try {
                    JSONObject movie = movies.getJSONObject(i);
                    String id = movie.getString("id");
                    String reviewsUrl = ProviderType.ROTTENTOMATOES.requestURL(QueryType.REVIEWS_BYID, RestParam.ID.getId(), id);
                    String title = movie.getString("title");
                    Integer year = movie.getInt("year");
                    Integer duration = movie.getInt("runtime"); // runtime usually throws an error, we consider this a parse exn and we do not add the movie
                    String cast = getActors(movie.getJSONArray("abridged_cast"));
                    String description = movie.getString("synopsis");
                    Integer reviews = numberOfReviews(reviewsUrl);

                    MovieDTO movieDTO = new MovieDTO();
                    movieDTO.setTitle(title);
                    movieDTO.setYearOfProduction(year);
                    movieDTO.setDuration(duration);
                    movieDTO.setCast(cast);
                    movieDTO.setDescription(description);
                    movieDTO.setNumberOfReviews(reviews);
                    movieDTO.setId(MovieDTO.id(movieDTO));
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


    public static List<MovieDTO> getMoviesByTitle(String query) {

        String jsonString = RestIO.restGETString(ProviderType.ROTTENTOMATOES.requestURL
                (QueryType.BY_TITLE, RestParam.QUERY.getId(), query));
        JSONObject obj = JsonIO.toJsonObject(jsonString);
        List<MovieDTO> movieDTOS = new ArrayList<>();
        try {
            JSONArray movies = obj.getJSONArray("movies");

            String nomrmalizedQuery = MovieDTO.normalize(query);
            for (int i = 0; i < movies.length(); i++) {
                try {
                    JSONObject movie = movies.getJSONObject(i);
                    String id = movie.getString("id");
                    String title = movie.getString("title");

                    if (!MovieDTO.isMatchingTitle(nomrmalizedQuery, title)) {
                        if (Constants.DEV_MODE) {
                            Constants.LOGGER.info("Rejected movie with title \"" + title + "\" for query: " +
                                    nomrmalizedQuery);
                        }
                        continue; /* not relevant result */
                    }

                    String cast = getActors(movie.getJSONArray("abridged_cast"));
                    Integer year = movie.getInt("year");
                    Integer duration = movie.getInt("runtime");
                    String reviewsUrlTemplate = ProviderType.ROTTENTOMATOES.requestURL(QueryType.REVIEWS_BYID, RestParam.ID.getId(), id);
                    String reviewsUrl = reviewsUrlTemplate != null?String.format(reviewsUrlTemplate, id):null;
                    String description = movie.getString("synopsis"); // synopsis is usually empty due to copyright
                    // issues
                    Integer reviews = numberOfReviews(reviewsUrl);

                    MovieDTO movieDTO = new MovieDTO();
                    movieDTO.setTitle(title);
                    movieDTO.setYearOfProduction(year);
                    movieDTO.setDuration(duration);
                    movieDTO.setCast(cast);
                    movieDTO.setDescription(description);
                    movieDTO.setNumberOfReviews(reviews);
                    movieDTO.setId(MovieDTO.id(movieDTO));
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
}
