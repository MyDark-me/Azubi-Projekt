package org.devcloud.ap.utils.helper;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

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

    public HashMap<String, String> getEntities() {
        HashMap<String, String> feedback = new HashMap<>();
        URI requestURI = httpExchange.getRequestURI();
        String query = requestURI.getQuery();
        if (query == null) {
            logger.debug("Nichts gefunden in der Liste");
            return feedback;
        }

        String[] list = query.split("&");
        logger.debug("Länge der gefundenen Liste {}", list.length);

        for (String raw : list) {
            String[] splitter = raw.split("=");
            if(splitter.length == 2) {
                logger.debug("Schlüssel {} mit Wert {} gefunden", splitter[0], splitter[1]);
                feedback.put(splitter[0], splitter[1]);
            }
            else
                logger.debug("Kein Schlüssel und Wert gefunden!");

        }
        return feedback;
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
