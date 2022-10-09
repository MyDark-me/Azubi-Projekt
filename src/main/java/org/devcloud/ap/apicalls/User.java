package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private static final JSONCreator jsonCreator = new JSONCreator().addKeys("statuscode");

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/user/create", new Create());
        httpServer.createContext("/api/user/delete", new Delete());
        httpServer.createContext("/api/user/edit", new Edit());
        httpServer.createContext("/api/user/data", new Data());
        httpServer.createContext("/api/user/login", new Login());
    }

    private User() {}

    private static void addResponseHeaders(HttpExchange httpExchange) {
        String request = httpExchange.getRequestURI().toString();
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");

        debugRequest(logger, request);
    }

    private static void debugRequest(Logger logger, String request) {
        logger.debug("{} - was requested", request);
    }

    private static class Create implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            String response = new JSONCreator()
                    .addKeys("statuscode", "response")
                    .addValue(201, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private static class Delete implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            String response = new JSONCreator()
                    .addKeys("statuscode", "response")
                    .addValue(201, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private static class Edit implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            String response = new JSONCreator()
                    .addKeys("statuscode", "response")
                    .addValue(201, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private static class Data implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            String response = new JSONCreator()
                    .addKeys("statuscode", "response")
                    .addValue(201, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private static class Login implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            String response = new JSONCreator()
                    .addKeys("statuscode", "response")
                    .addValue(201, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }
}
