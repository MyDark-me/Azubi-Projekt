package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class Role {
    private static final Logger logger = LoggerFactory.getLogger(Base.class);
    private static final JSONCreator jsonCreator = new JSONCreator().addKeys("statuscode");

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/role/delete", new Delete());
        httpServer.createContext("/api/role/users", new Users());
        httpServer.createContext("/api/role/login", new Login());
    }

    private Role() {}

    private static void addResponseHeaders(HttpExchange httpExchange) {
        String request = httpExchange.getRequestURI().toString();
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");

        debugRequest(logger, request);
    }

    private static void debugRequest(Logger logger, String request) {
        logger.debug("{} - was requested", request);
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

    private static class Users implements HttpHandler {
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
