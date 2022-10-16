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
        TOKEN_NOT_VALID(400,"Der Token ist nicht gültig."),
        USER_DELETED(201,"User wurde erfolgreich gelöscht!"),
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
            if(!query.containsKey(EUser.TOKEN.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            // Öffnen der Datenbank

            try {
                Session session = Azubiprojekt.getSqlPostgres().openSession();

                Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_TOKEN", APUser.class);
                queryUser.setParameter("token", query.get(EUser.TOKEN.toString()));

                if(queryUser.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.TOKEN_NOT_VALID);
                    return;
                }

                APUser apUser = queryUser.uniqueResult();
                logger.debug("Es wurde der User {} gefunden.", apUser.getName());

                session.remove(apUser);
                session.getTransaction().commit();
                logger.debug("Der benutzer dem den token {} gehört hatte wurde gelöscht.", query.get(ApiCallsLang.TOKEN));
                session.close();

                response.writeResponse(EMessages.USER_DELETED);

            } catch (HibernateException ex) {
                logger.error("Fehler bei einer Datenbanksitzung", ex);
                response.writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private static class Edit implements HttpHandler {
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
                apUser.setName(query.get(EUser.NAME.toString()));
                apUser.setPassword(query.get(EUser.PASSWORD.toString()));

                session.beginTransaction();

                session.merge(apUser);

                session.getTransaction().commit();
                logger.debug("ID {} wurde mit dem User {} ein neuer Token gesetzt und Informationen aktualisiert.", apUser.getId(), apUser.getName());
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
