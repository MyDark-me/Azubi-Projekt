package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Base {
    private static final Logger logger = LoggerFactory.getLogger(Base.class);
    private static final JSONCreator jsonCreator = new JSONCreator() .addKeys("statuscode");

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/check", new Check());
        httpServer.createContext("/api/getservertime", new ServerTime());
        httpServer.createContext("/api/getserverdate", new ServerDate());
    }

    private Base() {}

    private static void addResponseHeaders(HttpExchange httpExchange) {
        String request = httpExchange.getRequestURI().toString();
        httpExchange.getResponseHeaders().add("Content-Type", "application/json");

        debugRequest(logger, request);
    }

    private static void debugRequest(Logger logger, String request) {
        logger.debug("{} - was requested", request);
    }

    private static class Check implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            String connection;
            Boolean bConnection = Azubiprojekt.getSqlPostgres().isConnection();
            if(bConnection)
                connection = "Database is running!";
            else
                connection = "Database is down!";


            String response = jsonCreator
                    .addKeys("api", "apiConnected", "database", "databaseConnected", "version")
                    .addValue(201, "API is running!", true, connection, bConnection, "1.0-SNAPSHOT").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private static class ServerTime implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            LocalTime localTime = LocalTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = localTime.format(dateTimeFormatter);

            String response = jsonCreator
                    .addKeys("formatted", "raw")
                    .addValue(201, formattedTime, localTime.toString()).toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    private static class ServerDate implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            LocalDate localDateTime = LocalDate.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = localDateTime.format(dateTimeFormatter);

            String response = jsonCreator
                    .addKeys("formatted", "raw")
                    .addValue(201, formattedDate, localDateTime.toString()).toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }
}
