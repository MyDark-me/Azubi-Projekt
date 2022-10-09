package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.JSONCreator;

import java.io.IOException;
import java.io.OutputStream;

public class Base {
    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/check", new Check());
        httpServer.createContext("/api/getservertime", new ServerTime());
        httpServer.createContext("/api/getserverdate", new ServerDate());
    }

    public static class Check implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestURI().toString();
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");


            String response = new JSONCreator()
                    .addKeys("statuscode", "request", "response")
                    .addValue(201, request, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }

    public static class ServerTime implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestURI().toString();
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");


            String response = new JSONCreator()
                    .addKeys("statuscode", "request", "response")
                    .addValue(201, request, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }


    public static class ServerDate implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String request = httpExchange.getRequestURI().toString();
            httpExchange.getResponseHeaders().add("Content-Type", "application/json");


            String response = new JSONCreator()
                    .addKeys("statuscode", "request", "response")
                    .addValue(201, request, "User API is not implemented yet!").toString();

            httpExchange.sendResponseHeaders(200, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(response.getBytes());
            outputStream.close();
        }
    }
}
