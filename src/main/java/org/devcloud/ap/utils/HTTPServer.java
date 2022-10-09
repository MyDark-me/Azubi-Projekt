package org.devcloud.ap.utils;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.devcloud.ap.Azubiprojekt.logger;

public class HTTPServer {
    private static HttpServer server;

    /**
     * Starts the HTTP server
     *
     * @throws IOException for the server
     */
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8001), 0); //Create a new server on port 8001
        server.createContext("/", new APIHandler()); //Create a new context for the API

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
            String reqest = httpExchange.getRequestURI().toString();
            httpExchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");

            File index = new File(Azubiprojekt.class.getClassLoader().getResource("gui/index.html").getPath());
            String response = new String(Files.readAllBytes(Paths.get(index.getPath())), StandardCharsets.UTF_8);

            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();

            logger.debug("{} - was requested", reqest);
        }
    }
}
