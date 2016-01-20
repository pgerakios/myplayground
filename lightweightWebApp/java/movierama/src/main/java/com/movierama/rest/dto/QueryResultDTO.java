package com.movierama.rest.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movierama.util.io.JsonIO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
public class QueryResultDTO implements DTO {

    ;

    QueryType queryType;
    String extraData;
    Boolean error = false;
    String errorDescription;
    Integer resultsFound = 0;
    List<MovieDTO> movies = new ArrayList<>();


    public static QueryResultDTO sampleQueryResult() {
        QueryResultDTO qrs = new QueryResultDTO();
        MovieDTO movie1 = new MovieDTO();

        qrs.setQueryType(QueryType.NOW_PLAYING);
        movie1.setNumberOfReviews(55);
        movie1.setTitle("Star Wars: The Force Awakens");
        movie1.setYearOfProduction(2015);
        movie1.setCast("Jeff Bridges, John Goodman, Julianne Moore");
        movie1.setDescription("The Star Wars saga continues with this seventh entry -- the first under the Walt Disney Co. umbrella. The film will act as the start of a new trilogy set after the events of Return of the Jedi. J.J. Abrams directs from a script by Michael Arndt.");

        MovieDTO movie2 = new MovieDTO();
        movie2.setNumberOfReviews(300);
        movie2.setTitle("The Big Lebowski");
        movie2.setYearOfProduction(1998);
        movie2.setCast(" Staring: Jeff Bridges, John Goodman, Julianne Moore");
        movie2.setDescription("Jeffrey \"The Dude\" Lebowski is the ultimate LA slacker, until one day his house is broken into and his rug is peed on by two angry gangsters who have mistaken him for Jeffrey Lebowski, the LA millionaire, whose wife owes some bad people some big money. The Dude becomes entangled in the plot when he goes to visit the real Lebowski in order to get some retribution for his soiled rug, and is recruited to be the liason between Lebowski and the captors of his now \"kidnapped\" wife");

        qrs.getMovies().add(movie1);
        qrs.getMovies().add(movie2);
        qrs.setResultsFound(qrs.getMovies().size());
        return qrs;
    }
}
