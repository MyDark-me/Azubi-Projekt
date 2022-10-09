package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Base {
    private static final Logger logger = LoggerFactory.getLogger(Base.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/check", new Check());
        httpServer.createContext("/api/getservertime", new ServerTime());
        httpServer.createContext("/api/getserverdate", new ServerDate());
    }

    private Base() {}

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

    private static class Check implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String connection;
            Boolean bConnection = Azubiprojekt.getSqlPostgres().isConnection();
            if(Boolean.TRUE.equals(bConnection))
                connection = "Database is running!";
            else
                connection = "Database is down!";


            String response = getJSONCreator()
                    .addKeys("api", "apiConnected", "database", "databaseConnected", "version")
                    .addValue(201, "API is running!", true, connection, bConnection, "1.0-SNAPSHOT").toString();

            writeResponse(httpExchange, response);
        }
    }

    private static class ServerTime implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            LocalTime localTime = LocalTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = localTime.format(dateTimeFormatter);

            String response = getJSONCreator()
                    .addKeys("formatted", "raw")
                    .addValue(201, formattedTime, localTime.toString()).toString();

            writeResponse(httpExchange, response);
        }
    }

    private static class ServerDate implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            LocalDate localDateTime = LocalDate.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = localDateTime.format(dateTimeFormatter);

            String response = getJSONCreator()
                    .addKeys("formatted", "raw")
                    .addValue(201, formattedDate, localDateTime.toString()).toString();

            writeResponse(httpExchange, response);
        }
    }
}
