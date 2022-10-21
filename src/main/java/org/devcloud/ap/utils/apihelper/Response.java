package org.devcloud.ap.utils.apihelper;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import io.sentry.Sentry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class Response {

    @Getter
    private final Logger logger;
    @Getter
    private final HttpExchange httpExchange;

    public void debugRequest() {
        URI requestURI = httpExchange.getRequestURI();
        this.logger.debug("{} - was requested", requestURI);
    }

    public void addResponseHeaders() {
        this.httpExchange.getResponseHeaders().add("Content-Type", "application/json");
        this.httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
    }

    public Map<String, String> getEntities() {
        Map<String, String> feedback = new HashMap<>();
        URI requestURI = this.httpExchange.getRequestURI();
        String query = requestURI.getQuery();
        if (query == null) {
            this.logger.debug("Nichts gefunden in der Liste");
            return feedback;
        }

        String[] list = query.split("&");
        this.logger.debug("Länge der gefundenen Liste {}", list.length);

        for (String raw : list) {
            String[] splitter = raw.split("=");
            if(splitter.length == 2) {
                this.logger.debug("Schlüssel {} mit Wert {} gefunden", splitter[0], splitter[1]);
                feedback.put(splitter[0].toLowerCase(), splitter[1]);
            }
            else
                this.logger.debug("Kein Schlüssel und Wert gefunden!");

        }
        return feedback;
    }

    public void writeResponse(JSONCreator jsonCreator) {
        writeResponse(201, jsonCreator.toString());
    }

    public void writeResponse(ResponseMessage response) {
        JSONCreator jsonCreator = new JSONCreator();
        jsonCreator.put("message", response.getMessage());
        writeResponse(response.getRCode(), jsonCreator.toString());
    }

    public void writeResponse(int rCode, String response) {
        try {
            this.httpExchange.sendResponseHeaders(rCode, response.length());

            OutputStream outputStream = this.httpExchange.getResponseBody();
            for(char write : response.toCharArray())
                outputStream.write(write);
            outputStream.close();
        } catch (IOException e) {
            this.logger.error("Fehler beim Schreiben der Antwort", e);
            Sentry.captureException(e);
        }
    }
}
