package com.movierama.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.movierama.config.Constants;
import com.movierama.util.Var;
import com.movierama.util.io.JsonIO;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE, include = JsonTypeInfo.As.PROPERTY)
@Data
@ToString
public class MovieDTO implements DTO, Comparable<MovieDTO> {

    @JsonIgnore
    private String id;

    private String title;

    private String description;

    private Integer numberOfReviews;

    private Integer yearOfProduction;

    private Integer duration;

    private String cast;

    @Override
    public int compareTo(MovieDTO o) {
        if (o == null) {
            return 1;
        }
        if (id == null) {
            throw new RuntimeException();
        }
        return id.compareTo(o.id);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof MovieDTO)) {
            return false;
        }
        return compareTo((MovieDTO) o) == 0;
    }

    @Override
    public int hashCode() {
        if (id == null) {
            throw new RuntimeException();
        }
        return id.hashCode();
    }

    public static String id(MovieDTO movieDTO) {
        String summary = MovieDTO.id(movieDTO.getTitle() + " " + movieDTO.getYearOfProduction() + " " + movieDTO.getDuration());
        return summary;
    }

    // global hash method for computing ids
    public static String id(String title) {
        String summary = normalize(title);
        int hash = summary.hashCode();
        if (hash < 0) {
            hash = -hash; //FIXME?
        }
        summary = String.valueOf(hash);
        return summary;
    }

    public static SortedSet<String> tokenize(String title) {
        List<String> tokens = Var.split(Var.symbolsToSpace(title.toLowerCase()), "\\s");
        SortedSet<String> interestingWords = new TreeSet<>();

        // exclude single letters and common words like "this" , "is" , "that"  etc. ?
        // take the conservative approach and include them in the summary
        //Constants.LOGGER.info("TOKENS of title=\"" + title + "\" are tokens=\"" + tokens + "\"");
        // ignore the order of words
        for (String token : tokens) {
            interestingWords.add(token.trim()); // ignore case, simplify search and hashing
        }
        return interestingWords;
    }

    public static String normalize(String title) {
        SortedSet<String> interestingWords = tokenize(title);
        String summary = "";
        for (String word : interestingWords) {
            summary += word;
        }
        return summary;
    }

    // decide if a result (haystack) matches our query (needle)
    public static boolean isMatchingTitle(String needle, String haystack) {
        return normalize(haystack).indexOf(normalize(needle)) > -1;
    }

    public static void main(String[] args) {
        String[] tokens = "bIg lebOwski".split("\\s");
        for (String token : tokens) {
            System.out.println("token -> " + token);
        }
        System.out.println("Normalize: " + Var.toList("big lebowski".split("\\s")));
        System.out.println("Normalize: " + normalize("big lebowski"));
    }
}

