package org.devcloud.ap.apicalls;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.PgGroup;
import org.devcloud.ap.database.PgMember;
import org.devcloud.ap.database.PgRole;
import org.devcloud.ap.database.PgUser;
import org.devcloud.ap.utils.JSONCreator;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.json.JSONObject;
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

    private Member() {}

    private final static String error = "error";

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

    private static JSONCreator getJSONCreator(int statusCode) {
        return new JSONCreator().addKeys("statuscode").addValue(statusCode);
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

    private enum EGroup {
        NAME("name"), COLOR("color");
        final String name;
        EGroup(String name) { this.name = name; }
        @Override
        public String toString() {return name.toLowerCase(); }
    }

    private enum EUser {
        USERNAME("username"), PASSWORD("password"), EMAIL("email"), TOKEN("token");
        final String name;
        EUser(String name) { this.name = name; }
        @Override
        public String toString() {return name.toLowerCase(); }
    }

    private enum ERole {
        NAME("rolename");
        final String name;
        ERole(String name) { this.name = name; }
        @Override
        public String toString() {return name.toLowerCase(); }
    }

    private enum EGroupPattern {
        /*
         * Name:
         * mindestens 3 zeichen
         * erlaubt sind:
         * groß und klein buchstaben
         * 0-9, _ und -
         */
        NAME("^[a-zA-Z0-9-_]{3,}$");

        final String aPattern;
        EGroupPattern(String pattern) { this.aPattern = pattern; }
        @Override
        public String toString() {return aPattern; }

        public boolean isMatch(CharSequence input) {
            Pattern pattern = Pattern.compile(aPattern);
            Matcher matcher = pattern.matcher(input);
            return !matcher.find();
        }
    }

    private static class Join implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            addResponseHeaders(httpExchange);

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                String response = getJSONCreator(500)
                        .addKeys(error)
                        .addValue("Datenbank ist nicht Erreichbar!").toString();

                writeResponse(httpExchange, response, 500);
                return;
            }

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            // Prüfe ob alles den syntax vorgibt

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Es wurden keine Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EUser.USERNAME.toString()) ||!query.containsKey(EGroup.NAME.toString()) || !query.containsKey(EUser.TOKEN.toString())) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Es wurden nicht die richtigen Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(EGroupPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der Name entspricht nicht den Vorgaben.").toString();

                writeResponse(httpExchange, response, 400);
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
                        .addKeys(error)
                        .addValue("Die Gruppe existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche User mit dem Token");
            // hole user
            queryString = "FROM PgUser pguser WHERE pguser.usertoken= :usertoken";
            queryDatabase = session.createQuery(queryString, PgUser.class);
            queryDatabase.setParameter("usertoken", query.get(EUser.TOKEN.toString()));
            PgUser pgUser = (PgUser) queryDatabase.uniqueResult();

            if(pgUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der Token ist nicht gültig.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Member mit dem User und Gruppe");
            // hole member
            queryString = "FROM PgMeber pgmember WHERE pgmember.memberuser= :user AND pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, PgMember.class);
            queryDatabase.setParameter("user", pgUser);
            queryDatabase.setParameter("group", pgGroup);
            PgMember pgMember = (PgMember) queryDatabase.uniqueResult();

            if(pgMember == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Du bist nicht Admin.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Role Admin");
            // hole role
            queryString = "FROM PgRole pgrole WHERE pgrole.rolename= :rolename";
            queryDatabase = session.createQuery(queryString, PgRole.class);
            queryDatabase.setParameter("rolename", "Member");
            PgRole pgRole = (PgRole) queryDatabase.uniqueResult();

            if(pgRole == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Die Rolle Admin existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // add user
            queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            queryDatabase = session.createQuery(queryString, PgUser.class);
            queryDatabase.setParameter("username", query.get(EUser.USERNAME.toString()));
            PgUser pgUserAdd = (PgUser) queryDatabase.uniqueResult();

            if(pgUserAdd == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der User existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // erstelle member
            PgMember pgMemberAdd = new PgMember(pgUserAdd, pgGroup, pgRole);

            // add member
            session.persist(pgMemberAdd);

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
            addResponseHeaders(httpExchange);

            if(!Azubiprojekt.getSqlPostgres().isConnection()) {
                String response = getJSONCreator(500)
                        .addKeys(error)
                        .addValue("Datenbank ist nicht Erreichbar!").toString();

                writeResponse(httpExchange, response, 500);
                return;
            }

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Es wurden keine Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EUser.USERNAME.toString())) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Es wurden nicht die richtigen Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            Session session = Azubiprojekt.getSqlPostgres().openSession();

            logger.debug("Suche User mit dem Namen");
            // hole user
            String queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            Query queryDatabase = session.createQuery(queryString, PgUser.class);
            queryDatabase.setParameter("username", query.get(EUser.USERNAME.toString()));
            PgUser pgUser = (PgUser) queryDatabase.uniqueResult();

            if(pgUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der User existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            queryString = "FROM PgMember pgmember WHERE pgmember.memberuser= :user";
            queryDatabase = session.createQuery(queryString, PgMember.class);
            queryDatabase.setParameter("user", pgUser);
            List<PgMember> pgMembers = queryDatabase.list();
            logger.debug("Lese member records...");

            ArrayList<String> memberList = new ArrayList<>();
            for (PgMember rawMembers : pgMembers) {
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
                        .addKeys(error)
                        .addValue("Datenbank ist nicht Erreichbar!").toString();

                writeResponse(httpExchange, response, 500);
                return;
            }

            URI requestURI = httpExchange.getRequestURI();
            debugRequest(requestURI);

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Es wurden keine Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EUser.USERNAME.toString()) || !query.containsKey(EUser.TOKEN.toString()) || !query.containsKey(EGroup.NAME.toString()) || !query.containsKey(ERole.NAME.toString())) {
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Es wurden nicht die richtigen Informationen mitgegeben.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            Session session = Azubiprojekt.getSqlPostgres().openSession();

            logger.debug("Suche User mit dem Namen");
            // hole user
            String queryString = "FROM PgUser pguser WHERE pguser.username= :username";
            Query queryDatabase = session.createQuery(queryString, PgUser.class);
            queryDatabase.setParameter("username", query.get(EUser.USERNAME.toString()));
            PgUser pgUser = (PgUser) queryDatabase.uniqueResult();

            if(pgUser == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der User existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche User mit dem Token");
            // hole user
            queryString = "FROM PgUser pguser WHERE pguser.token= :token";
            queryDatabase = session.createQuery(queryString, PgUser.class);
            queryDatabase.setParameter("token", query.get(EUser.TOKEN.toString()));
            PgUser pgUserToken = (PgUser) queryDatabase.uniqueResult();

            if(pgUserToken == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der User existiert nicht.").toString();

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
                        .addKeys(error)
                        .addValue("Die Gruppe existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            // prüfe ob pgUserToken rolle admin hat
            queryString = "FROM PgMember pgmember WHERE pgmember.memberuser= :user AND pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, PgMember.class);
            queryDatabase.setParameter("user", pgUserToken);
            queryDatabase.setParameter("group", pgGroup);
            PgMember pgMemberToken = (PgMember) queryDatabase.uniqueResult();

            if(pgMemberToken == null) {
                session.close();
                // user ist nicht in der gruppe
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der User ist nicht in der Gruppe.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(pgMemberToken.getMemberrole().getRolename().equals("Admin")) {
                session.close();
                // user ist nicht in der gruppe
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Hat nicht die richtige Rolle.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Rolle mit dem Namen");
            // hole rolle
            queryString = "FROM PgRole pgrole WHERE pgrole.rolename= :rolename";
            queryDatabase = session.createQuery(queryString, PgRole.class);
            queryDatabase.setParameter("rolename", query.get(ERole.NAME.toString()));
            PgRole pgRole = (PgRole) queryDatabase.uniqueResult();

            if(pgRole == null) {
                session.close();
                // rolle existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Die Rolle existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche Member mit dem User und der Gruppe");
            // hole member
            queryString = "FROM PgMember pgmember WHERE pgmember.memberuser= :user AND pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, PgMember.class);
            queryDatabase.setParameter("user", pgUser);
            queryDatabase.setParameter("group", pgGroup);
            PgMember pgMember = (PgMember) queryDatabase.uniqueResult();

            if(pgMember == null) {
                session.close();
                // member existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Der Member existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Setze Rolle des Members");
            pgMember.setMemberrole(pgRole);

            logger.debug("Speichere Member");
            session.save(pgMember);

            session.close();

            String response = getJSONCreator(201)
                    .addKeys("success")
                    .addValue("Du hast erfolgreich die Rolle geändert!").toString();

            writeResponse(httpExchange, response, 201);
        }
    }
}