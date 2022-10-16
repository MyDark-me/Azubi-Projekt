package org.devcloud.ap.apicalls;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APMember;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.database.enumeration.EGroup;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.lang.ApiCallsLang;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.helper.Response;
import org.devcloud.ap.utils.helper.ResponseMessage;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Member {

    private static final Logger logger = LoggerFactory.getLogger(Member.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/member/join", new Join());
        httpServer.createContext("/api/member/leave", new Leave());
        httpServer.createContext("/api/member/groups", new Groups());
        httpServer.createContext("/api/member/edit", new Edit());
    }

    private enum EMessages implements ResponseMessage {
        DATABASE_NOT_AVAILABLE(500, "Datenbank ist nicht Erreichbar!"),
        NO_INFORMATION(400, "Es wurden keine Informationen mitgegeben."),
        WRONG_INFORMATION(400, "Es wurden nicht die richtigen Informationen mitgegeben."),
        WRONG_NAME(400, "Der Name entspricht nicht den Vorgaben."),
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

    private static class Join implements HttpHandler {
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

            if(!query.containsKey(EUser.NAME.toString()) || !query.containsKey(EGroup.NAME.toString()) || !query.containsKey(EUser.TOKEN.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            if(!EPattern.NAME.isMatch(query.get(EUser.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            if(!EPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            // öffne verbindung

            Session session = Azubiprojekt.getSqlPostgres().openSession();
            session.beginTransaction();

            logger.debug("Suche Group");
            // hole role
            String queryString = "FROM PgGroup pggroup WHERE pggroup.groupname= :groupname";
            Query queryDatabase = session.createQuery(queryString, PgGroup.class);
            queryDatabase.setParameter("groupname", query.get(EGroup.NAME.toString()));
            PgGroup pgGroup = (PgGroup) queryDatabase.uniqueResult();

            if(pgGroup == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Die Gruppe existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche User mit dem Token");
            // hole user
            queryString = "FROM PgUser pguser WHERE pguser.usertoken= :usertoken";
            queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter("usertoken", query.get(EUser.TOKEN.toString()));
            APUser APUser = (APUser) queryDatabase.uniqueResult();

            if(APUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Der Token ist nicht gültig.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Member mit dem User und Gruppe");
            // hole member
            queryString = "FROM PgMeber pgmember WHERE pgmember.memberuser= :user AND pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, APMember.class);
            queryDatabase.setParameter("user", APUser);
            queryDatabase.setParameter(ApiCallsLang.GROUP, pgGroup);
            APMember APMember = (APMember) queryDatabase.uniqueResult();

            if(APMember == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Du bist nicht Admin.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Role Admin");
            // hole role
            queryString = "FROM PgRole pgrole WHERE pgrole.rolename= :rolename";
            queryDatabase = session.createQuery(queryString, APRole.class);
            queryDatabase.setParameter(ApiCallsLang.ROLENAME, "Member");
            APRole APRole = (APRole) queryDatabase.uniqueResult();

            if(APRole == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Die Rolle Admin existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // add user
            queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter(ApiCallsLang.USERNAME, query.get(EUser.USERNAME.toString()));
            APUser APUserAdd = (APUser) queryDatabase.uniqueResult();

            if(APUserAdd == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_USER).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // erstelle member
            APMember APMemberAdd = new APMember(APUserAdd, pgGroup, APRole);

            // add member
            session.persist(APMemberAdd);

            session.getTransaction().commit();
            session.close();


            String response = getJSONCreator(201)
                    .addKeys("success", "name")
                    .addValue( "Member ist erfolgreich Beigetreten!", query.get(EGroup.NAME.toString())).toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Leave implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            // user / group / token
            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            String response = getJSONCreator(201)
                    .addKeys("response")
                    .addValue("User API is not implemented yet!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Groups implements HttpHandler {
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

            if(!query.containsKey(EUser.USERNAME.toString())) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            Session session = null;
            try{
                session = Azubiprojekt.getSqlPostgres().openSession();
            }catch (Exception ex){
                logger.error(ex.getMessage());
            }finally {
                assert session != null;
                Azubiprojekt.getSqlPostgres().closeSession(session);
            }

            logger.debug("Suche User mit dem Namen");
            // hole user
            String queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            Query queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter(ApiCallsLang.USERNAME, query.get(EUser.USERNAME.toString()));
            APUser APUser = (APUser) queryDatabase.uniqueResult();

            if(APUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_USER).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            queryString = "FROM PgMember pgmember WHERE pgmember.memberuser= :user";
            queryDatabase = session.createQuery(queryString, APMember.class);
            queryDatabase.setParameter("user", APUser);
            List<APMember> APMembers = queryDatabase.list();
            logger.debug("Lese member records...");

            ArrayList<String> memberList = new ArrayList<>();
            for (APMember rawMembers : APMembers) {
                memberList.add(rawMembers.getMembergroup().getGroupname());
            }

            String response = getJSONCreator(201)
                    .addKeys("response", "groups")
                    .addValue("Du hast erfolgreich die Gruppen abgefragt!", new Gson().toJson(memberList)).toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Edit implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
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
                        .addValue("Es wurden keine Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EUser.USERNAME.toString()) || !query.containsKey(EUser.TOKEN.toString()) || !query.containsKey(EGroup.NAME.toString()) || !query.containsKey(ERole.NAME.toString())) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            Session session = Azubiprojekt.getSqlPostgres().openSession();

            logger.debug("Suche User mit dem Namen");
            // hole user
            String queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            Query queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter(ApiCallsLang.USERNAME, query.get(EUser.USERNAME.toString()));
            APUser APUser = (APUser) queryDatabase.uniqueResult();

            if(APUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_USER).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche User mit dem Token");
            // hole user
            queryString = "FROM PgUser pguser WHERE pguser.token= :token";
            queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter("token", query.get(EUser.TOKEN.toString()));
            APUser APUserToken = (APUser) queryDatabase.uniqueResult();

            if(APUserToken == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_USER).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Gruppe mit dem Namen");
            // hole gruppe
            queryString = "FROM PgGroup pggroup WHERE pggroup.groupname= :groupname";
            queryDatabase = session.createQuery(queryString, PgGroup.class);
            queryDatabase.setParameter("groupname", query.get(EGroup.NAME.toString()));
            PgGroup pgGroup = (PgGroup) queryDatabase.uniqueResult();

            if(pgGroup == null) {
                session.close();
                // gruppe existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Die Gruppe existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // prüfe ob pgUserToken rolle admin hat
            queryString = "FROM PgMember pgmember WHERE pgmember.memberuser= :user AND pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, APMember.class);
            queryDatabase.setParameter("user", APUserToken);
            queryDatabase.setParameter(ApiCallsLang.GROUP, pgGroup);
            APMember APMemberToken = (APMember) queryDatabase.uniqueResult();

            if(APMemberToken == null) {
                session.close();
                // user ist nicht in der gruppe
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Der User ist nicht in der Gruppe.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(APMemberToken.getMemberrole().getRolename().equals("Admin")) {
                session.close();
                // user ist nicht in der gruppe
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Hat nicht die richtige Rolle.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Rolle mit dem Namen");
            // hole rolle
            queryString = "FROM PgRole pgrole WHERE pgrole.rolename= :rolename";
            queryDatabase = session.createQuery(queryString, APRole.class);
            queryDatabase.setParameter(ApiCallsLang.ROLENAME, query.get(ERole.NAME.toString()));
            APRole APRole = (APRole) queryDatabase.uniqueResult();

            if(APRole == null) {
                session.close();
                // rolle existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Die Rolle existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Member mit dem User und der Gruppe");
            // hole member
            queryString = "FROM PgMember pgmember WHERE pgmember.memberuser= :user AND pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, APMember.class);
            queryDatabase.setParameter("user", APUser);
            queryDatabase.setParameter(ApiCallsLang.GROUP, pgGroup);
            APMember APMember = (APMember) queryDatabase.uniqueResult();

            if(APMember == null) {
                session.close();
                // member existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Der Member existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Setze Rolle des Members");
            APMember.setMemberrole(APRole);

            logger.debug("Speichere Member");
            session.persist(APMember);

            session.close();

            String response = getJSONCreator(201)
                    .addKeys("success")
                    .addValue("Du hast erfolgreich die Rolle geändert!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }
}