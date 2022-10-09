package org.devcloud.ap.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.apicalls.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class HTTPServer {
    private static HttpServer server;
    private static final Logger logger = LoggerFactory.getLogger(HTTPServer.class);

    /**
     * Starts the HTTP server
     *
     * @throws IOException for the server
     */
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8001), 0); //Create a new server on port 8001
        server.createContext("/", new APIHandler()); //Create a new context for the API

        Base.register(server);
        /*  // For Later Use
        server.createContext("/api", new Base()); //Create a new context for the base api call
        server.createContext("/api/group", new Group()); //Create a new context for the group api call
        server.createContext("/api/role", new Role());  //Create a new context for the role api call
        server.createContext("/api/member", new Member());  //Create a new context for the member api call
        server.createContext("/api/member", new User());    //Create a new context for the user api call */

        server.setExecutor(null);   //Create a new executor
        server.start(); //Start the server
    }

    /**
     * Stops the HTTP server
     */
    public void stopServer() {
        server.stop(0); //Stop the server
    }

    public static class APIHandler implements HttpHandler { //Create a new class for the HTTP handler and respond with the index.html
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            httpExchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            String request = httpExchange.getRequestURI().toString();

            File index = new File(Objects.requireNonNull(Azubiprojekt.class.getClassLoader().getResource("gui/index.html")).getPath());
            String response = Files.readString(Paths.get(index.getPath()));

            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            for(char write : response.toCharArray())
                outputStream.write(write);
            outputStream.close();

            logger.debug("{} - was requested", request);
        }
    }
}
