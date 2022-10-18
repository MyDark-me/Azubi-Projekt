package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.JSONCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Base {
    private static final Logger logger = LoggerFactory.getLogger(Base.class);

    private Base() {}

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/check", new Check());
        httpServer.createContext("/api/getservertime", new ServerTime());
        httpServer.createContext("/api/getserverdate", new ServerDate());
    }

    private static class Check implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("api", true);
            jsonCreator.put("database", Azubiprojekt.getSqlPostgres().isConnection());
            jsonCreator.put("version", "1.0-SNAPSHOT");

            inputHelper.getResponse().writeResponse(jsonCreator);
        }
    }

    private static class ServerTime implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);

            LocalTime localTime = LocalTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = localTime.format(dateTimeFormatter);

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("formatted", formattedTime);
            jsonCreator.put("raw", localTime.toString());

            inputHelper.getResponse().writeResponse(jsonCreator);
        }
    }

    private static class ServerDate implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);

            LocalDate localDateTime = LocalDate.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = localDateTime.format(dateTimeFormatter);

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("formatted", formattedDate);
            jsonCreator.put("raw", localDateTime.toString());

            inputHelper.getResponse().writeResponse(jsonCreator);
        }
    }
}
