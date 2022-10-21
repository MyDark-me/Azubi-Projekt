package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.databsehelper.UserDatabaseHelper;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.devcloud.ap.utils.apihelper.exeption.WrongInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User {
    private static final Logger logger = LoggerFactory.getLogger(User.class);

    private User() {}

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/user/create", new Create());
        httpServer.createContext("/api/user/delete", new Delete());
        httpServer.createContext("/api/user/edit", new Edit());
        httpServer.createContext("/api/user/login", new Login());
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
            } catch (DatabaseException e) {
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
            } catch (DatabaseException e) {
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
                inputHelper.checkUserToken();

                // Hier werden die Eingaben überprüft von drei möglichen Eingaben
                // 1. Username Passwort Email
                // 2. Username Passwort
                // 3. Username Email
                // 4. Passwort Email
                // 5. Username
                // 6. Passwort
                // 7. Email

                // Prüfe ob der Username, Passwort und E-Mail Adresse geändert werden soll
                if(inputHelper.isUserName() && inputHelper.isUserPassword() && inputHelper.isUserEMail()) {
                    inputHelper.checkUserName();
                    inputHelper.checkUserPassword();
                    inputHelper.checkUserEMail();
                }

                // Prüfe ob der Username und Passwort geändert werden soll
                else if(inputHelper.isUserName() && inputHelper.isUserPassword()) {
                    inputHelper.checkUserName();
                    inputHelper.checkUserPassword();
                }

                // Prüfe ob der Username und E-Mail Adresse geändert werden soll
                else if(inputHelper.isUserName() && inputHelper.isUserEMail()) {
                    inputHelper.checkUserName();
                    inputHelper.checkUserEMail();
                }

                // Prüfe ob das Passwort und die E-Mail Adresse geändert werden soll
                else if(inputHelper.isUserPassword() && inputHelper.isUserEMail()) {
                    inputHelper.checkUserPassword();
                    inputHelper.checkUserEMail();
                }

                // Prüfe ob der Username geändert werden soll
                else if(inputHelper.isUserName()) {
                    inputHelper.checkUserName();
                }

                // Prüfe ob das Passwort geändert werden soll
                else if(inputHelper.isUserPassword()) {
                    inputHelper.checkUserPassword();
                }

                // Prüfe ob die E-Mail Adresse geändert werden soll
                else if(inputHelper.isUserEMail()) {
                    inputHelper.checkUserEMail();
                }
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }

            UserDatabaseHelper databaseHelper = new UserDatabaseHelper(inputHelper);
            try {
                databaseHelper.checkUserExist(false);
                databaseHelper.editUser();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Login implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
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
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }
}
