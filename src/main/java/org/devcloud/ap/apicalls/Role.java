package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.databsehelper.RoleDatabaseHelper;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.devcloud.ap.utils.apihelper.exeption.WrongInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Role {
    private static final Logger logger = LoggerFactory.getLogger(Role.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/role/roles", new Roles());
    }

    private Role() {}

    private static class Roles implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }

            RoleDatabaseHelper databaseHelper = new RoleDatabaseHelper(inputHelper);
            try {
                databaseHelper.allRoles();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }

        }
    }
}
