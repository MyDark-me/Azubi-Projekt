package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.databsehelper.GroupDatabaseHelper;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.devcloud.ap.utils.apihelper.exeption.WrongInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Group {
    private Group() {}
    private static final Logger logger = LoggerFactory.getLogger(Group.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/group/create", new Create());
        httpServer.createContext("/api/group/delete", new Delete());
        httpServer.createContext("/api/group/users", new Users());
    }

    private static class Create implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkGroupName();
                inputHelper.checkGroupColor();
                inputHelper.checkUserToken();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }
            GroupDatabaseHelper databaseHelper = new GroupDatabaseHelper(inputHelper);
            try {
                databaseHelper.checkGroupExist(true);
                databaseHelper.addGroup();
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
                inputHelper.checkUserName();
                inputHelper.checkUserToken();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }
            GroupDatabaseHelper databaseHelper = new GroupDatabaseHelper(inputHelper);
            try {
                databaseHelper.removeGroup();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Users implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkGroupName();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }
            GroupDatabaseHelper databaseHelper = new GroupDatabaseHelper(inputHelper);
            try {
                databaseHelper.fetchUsers();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }
}
