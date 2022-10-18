package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.databsehelper.MemberDatabaseHelper;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.devcloud.ap.utils.apihelper.exeption.WrongInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Member {
    private static final Logger logger = LoggerFactory.getLogger(Member.class);

    private Member() {}

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/member/join", new Join());
        httpServer.createContext("/api/member/leave", new Leave());
        httpServer.createContext("/api/member/groups", new Groups());
        httpServer.createContext("/api/member/edit", new Edit());
    }

    private static class Join implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkUserName();
                inputHelper.checkUserToken();
                inputHelper.checkGroupName();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }
            MemberDatabaseHelper databaseHelper = new MemberDatabaseHelper(inputHelper);
            try {
                databaseHelper.joinGroup();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Leave implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            InputHelper inputHelper = new InputHelper(logger, httpExchange);
            try {
                inputHelper.checkConnection();
                inputHelper.checkUserName();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }
            MemberDatabaseHelper databaseHelper = new MemberDatabaseHelper(inputHelper);
            try {
                databaseHelper.leaveGroup();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Groups implements HttpHandler {
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
            MemberDatabaseHelper databaseHelper = new MemberDatabaseHelper(inputHelper);
            try {
                databaseHelper.fetchGroups();
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
                inputHelper.checkUserName();
                inputHelper.checkGroupName();
                inputHelper.checkRoleName();
                inputHelper.checkUserToken();
            } catch (WrongInputException e) {
                e.printStackTrace();
                return;
            }
            MemberDatabaseHelper databaseHelper = new MemberDatabaseHelper(inputHelper);
            try {
                databaseHelper.editRole();
            } catch (DatabaseException e) {
                e.printStackTrace();
            }
        }
    }
}