package org.devcloud.ap.utils.helper;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;

public class Response {

    private  final Logger logger;
    private final HttpExchange httpExchange;

    public Response(Logger logger, HttpExchange httpExchange) {
        this.logger = logger;
        this.httpExchange = httpExchange;
    }

    public Response debugRequest() {
        URI requestURI = httpExchange.getRequestURI();
        logger.debug("{} - was requested", requestURI);
        return this;
    }

    public Response addResponseHeaders() {
        this.httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        this.httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        return this;
    }

    public void writeResponse(JSONCreator jsonCreator) {
        writeResponse(201, jsonCreator.toString());
    }

    public void writeResponse(ResponseMessage response) {
        writeResponse(response.getRCode(), response.getMessage());
    }

    public void writeResponse(int rCode, String response) {
        try {
            httpExchange.sendResponseHeaders(rCode, response.length());

            OutputStream outputStream = httpExchange.getResponseBody();
            for(char write : response.toCharArray())
                outputStream.write(write);
            outputStream.close();
        } catch (IOException e) {
            logger.error("Fehler beim Schreiben der Antwort", e);
        }
    }
}
