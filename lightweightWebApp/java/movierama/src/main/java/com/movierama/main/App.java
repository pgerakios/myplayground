package com.movierama.main;


import com.google.common.collect.ImmutableMap;
import com.movierama.config.Constants;
import com.movierama.config.Environment;
import com.movierama.rest.resources.MoviesResource;
import com.movierama.util.Var;
import com.sun.jersey.api.container.httpserver.HttpServerFactory;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        Environment.init();
        runHttpServer();
    }

    private static void runHttpServer() throws IOException {
        final ResourceConfig config = new DefaultResourceConfig(MoviesResource.class);
        final HttpServer server = HttpServerFactory.create(getServerURI(), config);

        server.setExecutor(Executors.newCachedThreadPool());
        LOGGER.info("App root path:"  + urlRootPath());
        server.createContext(urlRootPath(), new HttpHandler() {
            @Override
            public void handle(HttpExchange arg0) throws IOException {
                App.handle(arg0);

            }
        });
        server.start();
    }


    private static URI getServerURI() {
        int port = Environment.env().getConfigDTO().getServiceInfo().getLocalServer().getPort();
        LOGGER.info("Initiating Jersey application at port " + port);
        URI retVal = UriBuilder.fromUri("http://" + Var.getHostName() + "/").port(port).build();
        return retVal;
    }

    private static String urlRootPath() {
        return Environment.env().getConfigDTO().getServiceInfo().getUrlPath();
    }

    private static String rootPath() {
        final String basePath = Environment.env().rootPath();
        final String contentPath = Environment.env().getConfigDTO().getServiceInfo().getStaticContentPaths().get(0).getPath();
        final String fullPath = basePath + contentPath;
        return fullPath;
    }

    private static boolean isChild(String requestPath, String rootPath) {
        Path request = Paths.get(requestPath).toAbsolutePath();
        Path root = Paths.get(rootPath).toAbsolutePath();
        return request.startsWith(root);
    }

    private static boolean isValidPath(String requestPath, String rootPath) {
        // The request path should not escape the root path
        // TODO: symbolic links ?
        return isChild(requestPath, rootPath);
    }

    /* Handle static content */
    private static void handle(HttpExchange t) throws IOException {
        String rootPath = rootPath();
        URI uri = t.getRequestURI();
        LOGGER.info("looking for: " + rootPath + uri.getPath());
        String path = uri.getPath();
        String fullPath = rootPath + path;
        File file = new File(fullPath).getCanonicalFile();

        if (!isChild(fullPath, fullPath) || !file.isFile()) {
            // Object does not exist or is not a file: reject with 404 error.
            String response = "404 (Not Found)\n";
            t.sendResponseHeaders(404, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } else {
            // Object exists and is a file: accept with response code 200.
            String mime = "text/html";
            if (path.substring(path.length() - 3).equals(".js")) mime = "application/javascript";
            if (path.substring(path.length() - 3).equals("css")) mime = "text/css";

            Headers h = t.getResponseHeaders();
            h.set("Content-Type", mime);
            t.sendResponseHeaders(200, 0);

            OutputStream os = t.getResponseBody();
            FileInputStream fs = new FileInputStream(file);
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
            fs.close();
            os.close();
        }
    }
}
