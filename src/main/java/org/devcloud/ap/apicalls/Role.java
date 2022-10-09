package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

public class Role {
    private static final Logger logger = LoggerFactory.getLogger(Role.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/role/create", new Create());
        httpServer.createContext("/api/role/delete", new Delete());
        httpServer.createContext("/api/role/users", new Users());
    }

    private Role() {}

    private static JSONCreator getJSONCreator() {
        return new JSONCreator().addKeys("statuscode");
    }

    private static void addResponseHeaders(HttpExchange httpExchange) {
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
    }

    private static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        httpExchange.sendResponseHeaders(200, response.length());

        OutputStream outputStream = httpExchange.getResponseBody();
        for(char write : response.toCharArray())
            outputStream.write(write);
        outputStream.close();
    }

    private static void debugRequest(URI requestURI) {
        logger.debug("{} - was requested", requestURI);
    }

    private static class Create implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // name / color / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator()
                    .addKeys("response")
                    .addValue(201, "User API is not implemented yet!").toString();

            writeResponse(httpExchange, response);
        }
    }

    private static class Delete implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // name / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator()
                    .addKeys("response")
                    .addValue(201, "User API is not implemented yet!").toString();

            writeResponse(httpExchange, response);
        }
    }

    private static class Users implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // name / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator()
                    .addKeys("response")
                    .addValue(201, "User API is not implemented yet!").toString();

            writeResponse(httpExchange, response);
        }
    }
}
