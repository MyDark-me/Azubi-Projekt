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
import java.util.HashMap;

public class Member {
    private static final Logger logger = LoggerFactory.getLogger(Member.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/member/join", new Join());
        httpServer.createContext("/api/member/leave", new Leave());
        httpServer.createContext("/api/member/groups", new Groups());
        httpServer.createContext("/api/member/edit", new Edit());
    }

    private Member() {}



    private final static String error = "error";

    private static void addResponseHeaders(HttpExchange httpExchange) {
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

    private static void writeResponse(HttpExchange httpExchange, String response, int statusCode) throws IOException {
        httpExchange.sendResponseHeaders(statusCode, response.length());

        OutputStream outputStream = httpExchange.getResponseBody();
        for(char write : response.toCharArray())
            outputStream.write(write);
        outputStream.close();
    }

    private static JSONCreator getJSONCreator(int statusCode) {
        return new JSONCreator().addKeys("statuscode").addValue(statusCode);
    }

    private static void debugRequest(URI requestURI) {
        logger.debug("{} - was requested", requestURI);
    }

    private static HashMap<String, String> getEntities(URI uri) {
        HashMap<String, String> feedback = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) {
            logger.debug("Nothing found in the List");
            return feedback;
        }

        String[] list = query.split("&");
        logger.debug("Found list length {}", list.length);

        for (String raw : list) {
            String[] splitter = raw.split("=");
            if(splitter.length == 2) {
                logger.debug("Found key {} with value {}", splitter[0], splitter[1]);
                feedback.put(splitter[0], splitter[1]);
            }
            else
                logger.debug("No key and value found!");

        }
        return feedback;
    }

    private static class Join implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // user / group / role / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator(201)
                    .addKeys("response")
                    .addValue("User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Leave implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // user / group / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator(201)
                    .addKeys("response")
                    .addValue("User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Groups implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // user
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator(201)
                    .addKeys("response")
                    .addValue("User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Edit implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // user / group / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator(201)
                    .addKeys("response")
                    .addValue("User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }
}