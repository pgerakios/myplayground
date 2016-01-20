$(document).ready(function() {

        function findByTitleJSONRequest(title) {
            return {
                'restUrl' : '/api/movies/find/bytitle',
                json : {
                    'title' : title
                }
            };

        };
        function findNowPlayingJSONRequest(title) {
            return {
                'restUrl' : '/api/movies/find/nowplaying',
                json : {
                }
            };
        };
        function translationJSONRequest() {
            return {
              'restUrl' : '/api/movies/translation',
              json : { }
            };
        };
        function jsonRequest(request, succ, fail) {
                $.ajax({
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json'
                    },
                    url: request.restUrl,
                    type: "POST",
                    async: true,
                    dataType: 'json',
                    data:  JSON.stringify(request.json),
                    success: function (data) {
                        if(succ) {
                            succ(data);
                        }
                    }, error: function () {
                        if(fail) {
                            fail();
                        }
                    }
                });
        };
        function registerHandlers() {
            $('#submitText').bind('keypress', {}, function (e) {
                 var code = (e.keyCode ? e.keyCode : e.which);
                 if (code == 13) { //Enter keycode
                   e.preventDefault();
                   handleSubmit($('#submitText').val().trim());
                 }
            });

            $('#submitButton').on('click',function() {
                handleSubmit($('#submitText').val().trim());
            });
        };

        var queryHasError = false;
        var queryTitle = null;
        var queryType = 'nowplaying'; // 'bytitle'
        var moviesFoundNumber = 0;
        var moviesFound = [];






        var data = {
                            'title' : function() {
                                return data['site.title'];
                             },
                            'searchText' : function() {
                                return data['query.search text'];
                            },
                            'resultDescription' : function(){
                                if(queryHasError == true) {
                                    return data['query.error'];
                                } else if(moviesFoundNumber > 0) {
                                    if(queryType == 'nowplaying') {
                                        return repl('query.results.thisweek.found', moviesFoundNumber);
                                    } else {
                                        return repl('query.results.bytitle.found', moviesFoundNumber, queryTitle);
                                    }
                                } else {
                                    return queryTitle ? repl('query.no.results.found', queryTitle) : ''; // if empty, no search
                                }

                                //'15 movies in theaters this week'
                            },

                            'query.results.thisweek.found' : '', // 1 param
                            'query.results.bytitle.found' : '', // 2 params
                            'query.no.results.found' : '', // 1 param
                            'site.title' : 'AHA!', // 0 params
                            'query.movie.reviews' : '', // 1 param
                            'query.movie.year.and.cast' : '', // 2 params
                            'query.movie.description' : '', //1 param
                            'query.movie.title' : '', //1 param
                            'query.error' : '', // 0 param
                            'query.search text' : '' // 0 param


         };

         function repl(str, pl1, pl2) { /* tiny templating function */
                     str = data[str];
                     if(pl1) {
                         str = str.replace(/{\{0}}/g, pl1);
                     }
                     if(pl2) {
                         str = str.replace(/{\{1}}/g, pl2);
                     }
                     return str;
        };

        function applyTemplate(data) {
           $('title').html(Mustache.to_html($('title').html(), data));
           $('#main_container').html(Mustache.to_html($('#main_container').html(), data));
           registerHandlers();
        };

        function makeTemplateParams(movieId, movieTitle, movieYearCast, movieDescription, numberOfReviews) {
           return function() {

                return {
                    'alertType' : 'info',
                    'movieId' : movieId,
                    'movieTitle' : movieTitle,
                    'movieYearCast' : movieYearCast,
                    'movieDescription' : movieDescription,
                    'numberOfReviews' : numberOfReviews
                };
            };
        };

        function renderTemplateParams(params) {
           $('#resultList').empty();
            for(i=0 ; i < params.length ; i++) {
                var alertType = (i % 2 == 0) ? 'info' : 'success';
                var param = params[i];
                param['alertType'] = alertType;
                 $('#resultList').append(Mustache.to_html($('#rowTpl').html(), param()));
            }
            $('#resultDescription').html(data['resultDescription']());
            // registerHandlers();
        }

        var movieSample = {
                      "id" : null,
                      "title" : "The Big Lebowski",
                      "description" : "Jeffrey \"The Dude\" Lebowski is the ultimate LA slacker, until one day his house is broken into and his rug is peed on by two angry gangsters who have mistaken him for Jeffrey Lebowski, the LA millionaire, whose wife owes some bad people some big money. The Dude becomes entangled in the plot when he goes to visit the real Lebowski in order to get some retribution for his soiled rug, and is recruited to be the liason between Lebowski and the captors of his now \"kidnapped\" wife",
                      "numberOfReviews" : 300,
                      "yearOfProduction" : 1998,
                      "duration" : null,
                      "cast" : " Staring: Jeff Bridges, John Goodman, Julianne Moore"
                    } ;

        function movieToTemplateParams(i, movie) {
            var movieId, movieTitle, movieYearCast, movieDescription, numberOfReviews;

            movieId = i;
            movieTitle = movie.title;
            movieYearCast = repl('query.movie.year.and.cast', movie.yearOfProduction, movie.cast);
            movieDescription = movie.description;
            numberOfReviews = movie.numberOfReviews;

           return function() {

                return {
                    'alertType' : 'info',
                    'movieId' : movieId,
                    'movieTitle' : movieTitle,
                    'movieYearCast' : movieYearCast,
                    'movieDescription' : movieDescription,
                    'numberOfReviews' : numberOfReviews
                };
            };
        };
        var data0 = data;
        function jsonSuccessResponseHandler(response) {

            moviesFound = response.movies;
            moviesFoundNumber = response.resultsFound;
            queryHasError = false;

            var templateParams = new Array();
            $.each(moviesFound, function (i, movie) {
              templateParams.push(movieToTemplateParams(i, movie));
            });

            renderTemplateParams(templateParams);
            // applyTemplate();
            $('body').removeClass('loading');
        };

        function jsonFailureResponseHandler() {

            moviesFoundNumber = 0;
            moviesFound = {};
            queryHasError = true;
            console.log('Failure handler!');
            renderTemplateParams([]);
            //applyTemplate();
            $('body').removeClass('loading');
         }

        function handleSubmit(text) {
             $('body').addClass('loading');
             if(text) {
                  queryType = 'bytitle';
                  queryTitle = text;
                  jsonRequest(findByTitleJSONRequest(text),jsonSuccessResponseHandler,jsonFailureResponseHandler);
             } else {
                    queryType = 'nowplaying';
                    queryTitle = '';
                    jsonRequest(findNowPlayingJSONRequest(),jsonSuccessResponseHandler,jsonFailureResponseHandler);

             }
        };


        // get translation map and apply translations

        jsonRequest(translationJSONRequest(), function(response){
            var target = response.data;
            for (var k in target){
               var pair = target[k];
               if (typeof pair !== 'function') {
                var key = pair['key'];
                var val_ = pair['val'];
                  data0[key] = val_;
               }
            }
            //console.log('translation-->data = ' + JSON.stringify(data0));
            applyTemplate(data0);
            handleSubmit('');
        });

        /* trigger dummy response to fill data */
        jsonSuccessResponseHandler({
                                     "queryType" : "nowplaying",
                                     "extraData" : null,
                                     "error" : false,
                                     "errorDescription" : null,
                                     "resultsFound" : 0,
                                     "movies" : []
                                     });
});
