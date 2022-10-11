package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.PgUser;
import org.devcloud.ap.utils.JSONCreator;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/user/create", new Create());
        httpServer.createContext("/api/user/delete", new Delete());
        httpServer.createContext("/api/user/edit", new Edit());
        httpServer.createContext("/api/user/data", new Data());
        httpServer.createContext("/api/user/login", new Login());
    }

    private User() {}

    private static JSONCreator getJSONCreator() {
        return new JSONCreator().addKeys("statuscode");
    }

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

    private enum EUser {
        USERNAME("username"), PASSWORD("password"), EMAIL("email");
        final String name;
        EUser(String name) { this.name = name; }
        @Override
        public String toString() {return name.toLowerCase(); }
    }

    private static class Create implements HttpHandler {

        private enum EUserPattern {
            /*
            * Name:
            * mindestens 5 zeichen
            * groß und klein buchstaben
            * 0-9, _ und -
            * Password:
            * mindestens 8 zeichen
            * ein Großbuchstabe, Kleinbuchstabe, ., spezial character
             */
            NAME("^[a-zA-Z0-9-_]{5,}$"),
            PASSWORD("^(?=.*?[A-Z])(?=(.*[a-z]){1,})(?=(.*[\\W])*)(?!.*\\s).{8,}$"),
            EMAIL("^[a-zA-Z0-9.!#$%&'*+=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

            final String aPattern;
            EUserPattern(String pattern) { this.aPattern = pattern; }
            @Override
            public String toString() {return aPattern; }

            public boolean match(CharSequence input) {
                Pattern pattern = Pattern.compile(aPattern);
                Matcher matcher = pattern.matcher(input);
                return matcher.find();
            }
        }

        private static final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        private static String generateToken() {
            StringBuilder sb = new StringBuilder();
            SecureRandom random = new SecureRandom();
            for (int i = 0; i < 64; i++) {
                sb.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
            }
            return sb.toString();
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(500, "Datenbank ist nicht Erreichbar!").toString();

                writeResponse(httpExchange, response, 500);
                return;
            }

            // name / password / mail
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Es wurden keine Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EUser.USERNAME.toString()) || !query.containsKey(EUser.PASSWORD.toString()) || !query.containsKey(EUser.EMAIL.toString())) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Es wurden nicht die richtigen Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!EUserPattern.NAME.match(query.get(EUser.USERNAME.toString()))) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Der Name entspricht nicht den Vorgaben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!EUserPattern.PASSWORD.match(query.get(EUser.PASSWORD.toString()))) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Das Passwort entspricht nicht den Vorgaben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!EUserPattern.EMAIL.match(query.get(EUser.EMAIL.toString()))) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Die E-Mail entspricht nicht den Vorgaben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            String randomToken = generateToken();
            Session session = Azubiprojekt.getSqlPostgres().openSession();

            session.beginTransaction();
            PgUser pgUser = new PgUser(query.get(EUser.USERNAME.toString()), query.get(EUser.PASSWORD.toString()), query.get(EUser.EMAIL.toString()), randomToken);
            Long count = 1L;
            try {
                String queryString = "select count(*) from PgUser pguser where pguser.user_name=:name";
                Query queryDatabase = session.createQuery(queryString, Integer.class);
                queryDatabase.setParameter("name", pgUser.getName());
                count = (Long) queryDatabase.uniqueResult();
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.debug("Es wurden {} user gefunden.", count);

            if(count > 0) {
                session.close();
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Der Username wurde schon vergeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            session.persist(pgUser);

            session.getTransaction().commit();
            logger.debug("ID {} wurde mit dem User {} erfolgreich erstellt.", pgUser.getId(), pgUser.getName());
            session.close();


            String response = getJSONCreator()
                    .addKeys("message", "name", "email", "token")
                    .addValue(201, "User wurde Erfolgreich erstellt!", query.get(EUser.USERNAME.toString()), query.get(EUser.EMAIL.toString()), randomToken ).toString();

            writeResponse(httpExchange, response, 200);
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

            writeResponse(httpExchange, response, 200);
        }
    }

    private static class Edit implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // name / password / mail / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator()
                    .addKeys("response")
                    .addValue(201, "User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 200);
        }
    }

    private static class Data implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // name / email
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator()
                        .addKeys("message")
                        .addValue(400, "Es wurden keine Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(query.containsKey(EUser.USERNAME.toString())) {

                Session session = Azubiprojekt.getSqlPostgres().openSession();
                session.beginTransaction();
                PgUser pgUser = session.find(PgUser.class, query.get(EUser.USERNAME.toString()));

                session.persist(pgUser);

                session.getTransaction().commit();
                logger.debug("ID {} wurde mit dem User {} erfolgreich abgefragt. Suche Name", pgUser.getId(), pgUser.getName());
                session.close();


                String response = getJSONCreator()
                        .addKeys("message", "name")
                        .addValue(201, "User wurde Erfolgreich abgefragt!", pgUser.getName()).toString();

                writeResponse(httpExchange, response, 201);
                return;
            } else

            if(query.containsKey(EUser.EMAIL.toString())) {
                Session session = Azubiprojekt.getSqlPostgres().openSession();
                session.beginTransaction();
                PgUser pgUser = session.find(PgUser.class, query.get(EUser.USERNAME.toString()));

                session.persist(pgUser);

                session.getTransaction().commit();
                logger.debug("ID {} wurde mit dem User {} erfolgreich abgefragt. Suche email", pgUser.getId(), pgUser.getName());
                session.close();


                String response = getJSONCreator()
                        .addKeys("message", "email")
                        .addValue(201, "User wurde Erfolgreich abgefragt!", pgUser.getName()).toString();

                writeResponse(httpExchange, response, 201);
                return;
            }

            String response = getJSONCreator()
                    .addKeys("message")
                    .addValue(400, "Es wurden nicht die richtigen Informationen mitgegeben.").toString();

            writeResponse(httpExchange, response, 400);
        }
    }

    private static class Login implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // name / mail / password
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator()
                    .addKeys("response")
                    .addValue(201, "User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 200);
        }
    }
}
