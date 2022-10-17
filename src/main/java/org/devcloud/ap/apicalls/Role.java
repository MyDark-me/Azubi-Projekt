package org.devcloud.ap.apicalls;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.apihelper.Response;
import org.devcloud.ap.utils.apihelper.ResponseMessage;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class Role {
    private static final Logger logger = LoggerFactory.getLogger(Role.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/role/roles", new Roles());
    }

    private enum EMessages implements ResponseMessage {
        DATABASE_NOT_AVAILABLE(500, "Datenbank ist nicht Erreichbar!"),
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

    private static class Roles implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) {
            Response response = new Response(logger, httpExchange);
            response.addResponseHeaders().debugRequest();

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                response.writeResponse(EMessages.DATABASE_NOT_AVAILABLE);
                return;
            }

            try {
                Session session = Azubiprojekt.getSqlPostgres().openSession();

                Query<APRole> query = session.createNamedQuery("@HQL_GET_ALL_ROLES", APRole.class);
                List<APRole> apRoles = query.list();

                session.close();

                HashMap<Integer, HashMap<String, String>> roles = new HashMap<>();
                for (APRole rawRole : apRoles) {
                    HashMap<String, String> infos = new HashMap<>();

                    infos.put("name", rawRole.getName());
                    infos.put("color", rawRole.getColor());
                    roles.put(rawRole.getId(), infos);
                }

                JSONCreator jsonCreator = new JSONCreator();
                jsonCreator.put("roles", new Gson().toJson(roles));

                response.writeResponse(jsonCreator);
            } catch (HibernateException ex) {
                logger.error("Fehler bei einer Datenbanksitzung", ex);
                response.writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
