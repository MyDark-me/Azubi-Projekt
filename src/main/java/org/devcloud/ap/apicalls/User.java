package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.Response;
import org.devcloud.ap.utils.apihelper.ResponseMessage;
import org.devcloud.ap.utils.apihelper.databsehelper.UserDatabaseHelper;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.devcloud.ap.utils.apihelper.exeption.NoResultException;
import org.devcloud.ap.utils.apihelper.exeption.WrongInputException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;


public class User {
    private User() {}
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
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkUserName();
                inputHelper.checkUserPassword();
                inputHelper.checkUserEMail();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }

            UserDatabaseHelper databaseHelper = new UserDatabaseHelper(inputHelper);
            try {
                databaseHelper.checkUserExist(true);
                databaseHelper.addUser();
            } catch (DatabaseException | NoResultException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Delete implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkUserToken();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }

            UserDatabaseHelper databaseHelper = new UserDatabaseHelper(inputHelper);
            try {
                databaseHelper.checkUserExist(false);
                databaseHelper.removeUser();
            } catch (DatabaseException | NoResultException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Edit implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkUserName();
                inputHelper.checkUserPassword();
                inputHelper.checkUserEMail();
                inputHelper.checkUserToken();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }

            UserDatabaseHelper databaseHelper = new UserDatabaseHelper(inputHelper);
            try {
                databaseHelper.checkUserExist(true);
                databaseHelper.editUser();
            } catch (DatabaseException | NoResultException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Login implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkUserName();
                inputHelper.checkUserPassword();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }

            UserDatabaseHelper databaseHelper = new UserDatabaseHelper(inputHelper);
            try {
                databaseHelper.loginUser();
            } catch (DatabaseException | NoResultException e) {
                e.printStackTrace();
            }
        }
    }
}
