package com.movierama.rest.resources;

import com.movierama.rest.boundary.AggregatorBoundary;
import com.movierama.rest.dto.*;
import com.movierama.rest.exception.InvalidQueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/api/movies")
public class MoviesResource {

    interface Fun extends com.movierama.util.datastructure.functional.Fun<DTO, InvalidQueryException> {
    }

    enum ResponseCode {
        OK(Response.Status.OK, false),
        VALIDATION_ERROR(Response.Status.NOT_ACCEPTABLE, true),
        FATAL_ERROR(Response.Status.INTERNAL_SERVER_ERROR, true),;

        Response.Status status;
        boolean isError;

        ResponseCode(Response.Status status, boolean isError) {
            this.status = status;
            this.isError = isError;

        }

        public boolean isError() {
            return this.isError;
        }

        public Response.Status getStatus() {
            return status;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MoviesResource.class);

    final AggregatorBoundary aggregatorBoundary = new AggregatorBoundary();


    @POST
    @Path("/translation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response translation() {
        return withMaskedExceptionsDo(QueryType.NOW_PLAYING, new Fun() {
            @Override
            public DTO apply() throws InvalidQueryException {
                LOGGER.info("/translation REST call");
                final FlatMapDTO responseDTO = aggregatorBoundary.handleTranslation();
                LOGGER.info("/translation REST call END");
                return responseDTO;
            }
        });

    }

    @POST
    @Path("/find/bytitle")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestMoviesByTitle(final FindByTitleDTO request) {
        return withMaskedExceptionsDo(QueryType.BY_TITLE, new Fun() {
            @Override
            public DTO apply() throws InvalidQueryException {
                LOGGER.info("/bytitle REST call");
                final QueryResultDTO responseDTO = aggregatorBoundary.handleFindMovieByTitleRequest(request);
                LOGGER.info("/bytitle REST call END");
                return responseDTO;
            }
        });
    }

    @POST
    @Path("/find/nowplaying")
    @Produces(MediaType.APPLICATION_JSON)
    public Response requestMoviesNowPlaying() {
        return withMaskedExceptionsDo(QueryType.NOW_PLAYING, new Fun() {
            @Override
            public DTO apply() throws InvalidQueryException {
                LOGGER.info("/nowplaying REST call");
                final QueryResultDTO responseDTO = aggregatorBoundary.handleNowPlayingRequest();
                LOGGER.info("/nowplaying REST call end");
                return responseDTO;
            }
        });

    }

    private Response.ResponseBuilder buildResponse(final QueryType queryType, final ResponseCode responseCode, Object... extraArgs) {
        QueryResultDTO retVal = new QueryResultDTO();

        retVal.setQueryType(queryType);
        retVal.setError(responseCode.isError());

        if (retVal.getError()) {
            String faultString = (String) extraArgs[0];
            retVal.setErrorDescription(faultString);
            retVal.setResultsFound(0);
            retVal.setMovies(new ArrayList<MovieDTO>());
        } else {
            List<MovieDTO> movies = (List<MovieDTO>) extraArgs[0];
            retVal.setResultsFound(movies.size());
            retVal.setMovies(movies);
        }
        final Response.ResponseBuilder responseBuilder = Response.status(responseCode.getStatus()).entity(retVal);
        return responseBuilder;
    }

    private Response withMaskedExceptionsDo(final QueryType queryType, final Fun fn) {
        LOGGER.info("withMaskedExceptionsDo BEGIN");
        Response.ResponseBuilder response;

        try {
            aggregatorBoundary.throttleFilter();
            final DTO retVal = fn.apply();
            response = Response.ok(retVal);
        } catch (InvalidQueryException iqe) {
            LOGGER.error("Invalid query", iqe);
            response = buildResponse(queryType, ResponseCode.VALIDATION_ERROR, "FIXME1");
        } catch (Throwable t) {
            LOGGER.error("An UNKNOWN exception was thrown", t);
            response = buildResponse(queryType, ResponseCode.FATAL_ERROR, "FIXME2");
        } finally {

        }
        final Response responseResult = response.build();
        LOGGER.info("withMaskedExceptionsDo END");
        return responseResult;
    }
}
