
This is a simple, unoptimized, lightweight webapp employing JERSEY for serving static content and implementing server-side and client-side REST calls. The dummy functionality is to provide movie summaries by aggregating results from movie providers and caching them.

The API keys have been removed. 

This project can be used as a template for other projects. Uber JAR functionality is provided. No containers or application servers are required.

Have fun. Comments and contributions are welcome

Prerequisites:
     Java version 
     Apache Maven

 Build instructions:
     mvn clean install

 Run instrunctions (using the default configuration in the src/main/webapp directory)
     java  -Dwebapp.dir=./src/main/webapp -jar target/movierama-1.0-SNAPSHOT.jar
     http://localhost:8085/movierama/index.html

 Development:
    - IDE: lombok plugin must be installed.

 Possible optimizations:
    - Find subsets of words and cache them
    - JS client could cache some of the movies or some of the previous results.
      There could be signature for repeated queries, so they don't have to be recomputed.
