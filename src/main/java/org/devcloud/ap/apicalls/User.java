package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.lang.ApiCallsLang;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.helper.Response;
import org.devcloud.ap.utils.helper.ResponseMessage;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.security.SecureRandom;
import java.util.HashMap;


public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/user/create", new Create());
        httpServer.createContext("/api/user/delete", new Delete());
        httpServer.createContext("/api/user/edit", new Edit());
        httpServer.createContext("/api/user/login", new Login());
    }

    private enum EMessages implements ResponseMessage {
        DATABASE_NOT_AVAILABLE(500, "Datenbank ist nicht Erreichbar!"),
        NO_INFORMATION(400, "Es wurden keine Informationen mitgegeben."),
        WRONG_INFORMATION(400, "Es wurden nicht die richtigen Informationen mitgegeben."),
        WRONG_NAME(400, "Der Name entspricht nicht den Vorgaben."),
        WRONG_PASSWORD(400, "Das Passwort entspricht nicht den Vorgaben."),
        WRONG_EMAIL(400, "Die E-Mail entspricht nicht den Vorgaben."),
        ALREADY_NAME_USE(400, "Der Name wurde schon vergeben."),
        WRONG_LOGIN(400,"Der Username oder das Passwort ist falsch."),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        private final int rCode;
        private final String message;

        EMessages(int rCode, String message) {
            this.rCode = rCode;
            this.message = message;
        }

        @Override
        public int getRCode() {
            return rCode;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }

    // TOKEN generator

    private static final String TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    private static String createToken() {
        StringBuilder sb = new StringBuilder();
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 64; i++) {
            sb.append(TOKEN_CHARS.charAt(random.nextInt(TOKEN_CHARS.length())));
        }
        return sb.toString();
    }

    private static class Create implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) {
            Response response = new Response(logger, httpExchange);
            response.addResponseHeaders().debugRequest();

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                response.writeResponse(EMessages.DATABASE_NOT_AVAILABLE);
                return;
            }

            // Prüfen ob die URL Parameter vorhanden sind

            HashMap<String, String> query = response.getEntities();
            if(query.isEmpty()) {
                response.writeResponse(EMessages.NO_INFORMATION);
                return;
            }

            if(!query.containsKey(EUser.NAME.toString()) || !query.containsKey(EUser.PASSWORD.toString()) || !query.containsKey(EUser.EMAIL.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            if(!EPattern.USERNAME.isMatch(query.get(EUser.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            if(!EPattern.PASSWORD.isMatch(query.get(EUser.PASSWORD.toString()))) {
                response.writeResponse(EMessages.WRONG_PASSWORD);
                return;
            }

            if(!EPattern.EMAIL.isMatch(query.get(EUser.EMAIL.toString()))) {
                response.writeResponse(EMessages.WRONG_EMAIL);
                return;
            }

            // Öffnen der Datenbank

            try {
                Session session = Azubiprojekt.getSqlPostgres().openSession();

                // Prüfen ob der Benutzer schon existiert
                Query<Long> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_COUNT", Long.class);
                queryUser.setParameter("name", query.get(EUser.NAME.toString()));
                Long count = queryUser.uniqueResult();

                logger.debug("Es wurden {} Users gefunden.", count);
                if(count > 0) {
                    response.writeResponse(EMessages.ALREADY_NAME_USE);
                    return;
                }

                // Ersellen des Benutzers
                String randomToken = createToken();
                APUser apUser = new APUser(
                        query.get(EUser.NAME.toString()),
                        query.get(EUser.PASSWORD.toString()),
                        query.get(EUser.EMAIL.toString()),
                        randomToken);

                // Adden des Benutzers in die Datenbank
                session.beginTransaction();

                session.persist(apUser);

                session.getTransaction().commit();
                logger.debug("ID {} wurde mit dem User {} erfolgreich erstellt.", apUser.getId(), apUser.getName());
                session.close();

                JSONCreator jsonCreator = new JSONCreator();
                jsonCreator.put(EUser.NAME.toString(), apUser.getName());
                jsonCreator.put(EUser.EMAIL.toString(), apUser.getEmail());
                jsonCreator.put(EUser.TOKEN.toString(), apUser.getToken());

                response.writeResponse(jsonCreator);

            } catch (HibernateException ex) {
                logger.error("Fehler bei einer Datenbanksitzung", ex);
                response.writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private static class Delete implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            addResponseHeaders(httpExchange);

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                String response = getJSONCreator(500)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.DATABASE_NOT_AVAILABLE).toString();

                writeResponse(httpExchange, response, 500);
                return;
            }

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(ApiCallsLang.TOKEN)) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // öffne verbindung

            Session session = Azubiprojekt.getSqlPostgres().openSession();
            session.beginTransaction();

            // hole user informationen
            String queryString = "FROM PgUser pguser WHERE pguser.usertoken= :usertoken";
            Query<APUser> queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter("usertoken", query.get(ApiCallsLang.TOKEN));
            APUser APUser = queryDatabase.uniqueResult();

            if(APUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Der Token ist nicht gültig.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            session.remove(APUser);
            session.getTransaction().commit();
            session.close();

            logger.debug("Der benutzer dem den token {} gehört hatte wurde gelöscht.", query.get(ApiCallsLang.TOKEN));

            String response = getJSONCreator(201)
                    .addKeys(ApiCallsLang.SUCCESS)
                    .addValue( "User wurde erfolgreich gelöscht!").toString();


            writeResponse(httpExchange, response, 200);
        }
    }

    private static class Edit implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            addResponseHeaders(httpExchange);

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                String response = getJSONCreator(500)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.DATABASE_NOT_AVAILABLE).toString();

                writeResponse(httpExchange, response, 500);
                return;
            }

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            // Prüfe ob alles den syntax vorgibt

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(ApiCallsLang.USERNAME) || !query.containsKey(ApiCallsLang.PASSWORD) || !query.containsKey(ApiCallsLang.EMAIL)) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(EUserPattern.NAME.isMatch(query.get(ApiCallsLang.USERNAME))) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_NAME).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(EUserPattern.PASSWORD.isMatch(query.get(ApiCallsLang.PASSWORD))) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_PASSWORD).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(EUserPattern.EMAIL.isMatch(query.get(ApiCallsLang.EMAIL))) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Die E-Mail entspricht nicht den Vorgaben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // öffne verbindung

            Session session = Azubiprojekt.getSqlPostgres().openSession();
            session.beginTransaction();

            // hole mir die user informationen
            String queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            Query<APUser> queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter(ApiCallsLang.USERNAME, query.get(ApiCallsLang.USERNAME));
            APUser APUser = queryDatabase.uniqueResult();

            if(APUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Der Username existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Es wurden der User {} gefunden.", APUser.getUsername());

            // Passwort prüfen
            if(!query.get(ApiCallsLang.PASSWORD).equals(APUser.getUserpassword())) {
                session.close();
                // passwort falsch
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_LOGIN).toString();

                writeResponse(httpExchange, response, 400);
            }

            // new random token
            String randomToken = createToken();
            APUser.setUsertoken(randomToken);

            APUser.setUsermail(query.get(ApiCallsLang.EMAIL));
            APUser.setUserpassword(query.get(ApiCallsLang.PASSWORD));

            session.merge(APUser);
            session.getTransaction().commit();
            session.close();

            logger.debug("ID {} wurde mit dem User {} ein neuer Token gesetzt und Informationen aktualisiert.", APUser.getUserid(), APUser.getUsername());


            String response = getJSONCreator(201)
                    .addKeys(ApiCallsLang.SUCCESS, "name", ApiCallsLang.EMAIL, ApiCallsLang.TOKEN)
                    .addValue( "User hat sich Erfolgreich bearbeitet!", query.get(ApiCallsLang.USERNAME), query.get(ApiCallsLang.EMAIL), randomToken ).toString();

            writeResponse(httpExchange, response, 200);
        }
    }

    private static class Login implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            Response response = new Response(logger, httpExchange);
            response.addResponseHeaders().debugRequest();

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                response.writeResponse(EMessages.DATABASE_NOT_AVAILABLE);
                return;
            }

            // Prüfen ob die URL Parameter vorhanden sind

            HashMap<String, String> query = response.getEntities();
            if(query.isEmpty()) {
                response.writeResponse(EMessages.NO_INFORMATION);
                return;
            }

            if(!query.containsKey(EUser.NAME.toString()) || !query.containsKey(EUser.PASSWORD.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            if(!EPattern.USERNAME.isMatch(query.get(EUser.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            if(!EPattern.PASSWORD.isMatch(query.get(EUser.PASSWORD.toString()))) {
                response.writeResponse(EMessages.WRONG_PASSWORD);
                return;
            }

            // Öffnen der Datenbank

            try {
                Session session = Azubiprojekt.getSqlPostgres().openSession();
                Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_NAME", APUser.class);
                queryUser.setParameter("name", query.get(EUser.NAME.toString()));

                if(queryUser.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.WRONG_LOGIN);
                    return;
                }

                APUser apUser = queryUser.uniqueResult();
                logger.debug("Es wurde der User {} gefunden.", apUser.getName());

                // Passwort prüfen
                if(!query.get(EUser.PASSWORD.toString()).equals(apUser.getPassword())) {
                    session.close();
                    response.writeResponse(EMessages.WRONG_LOGIN);
                }

                // new random token
                String randomToken = createToken();
                apUser.setToken(randomToken);

                session.beginTransaction();

                session.merge(apUser);

                session.getTransaction().commit();
                session.close();

                JSONCreator jsonCreator = new JSONCreator();
                jsonCreator.put(EUser.NAME.toString(), apUser.getName());
                jsonCreator.put(EUser.EMAIL.toString(), apUser.getEmail());
                jsonCreator.put(EUser.TOKEN.toString(), apUser.getToken());

                response.writeResponse(jsonCreator);

            } catch (HibernateException e) {
                e.printStackTrace();
                logger.error("Es konnte keine Verbindung zur Datenbank hergestellt werden.");
                response.writeResponse(EMessages.DATABASE_NOT_AVAILABLE);
            }
        }
    }
}
