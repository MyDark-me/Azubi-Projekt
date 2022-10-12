package org.devcloud.ap.apicalls;

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

public class Group {
    private static final Logger logger = LoggerFactory.getLogger(Group.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/group/create", new Create());
        httpServer.createContext("/api/group/delete", new Delete());
        httpServer.createContext("/api/group/users", new Users());
    }

    private Group() {}



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

    private static class Create implements HttpHandler {
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

            if(!query.containsKey(EGroup.NAME.toString()) || !query.containsKey(EGroup.COLOR.toString()) || !query.containsKey(EUser.TOKEN.toString())) {
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

            // erstelle group
            PgGroup pgGroup = new PgGroup(
                    query.get(EGroup.NAME.toString()),
                    query.get(EGroup.COLOR.toString())
            );

            logger.debug("Suche Role Admin");
            // hole role
            String queryString = "FROM PgRole pgrole WHERE pgrole.rolename= :rolename";
            Query queryDatabase = session.createQuery(queryString, PgRole.class);
            queryDatabase.setParameter("rolename", "Admin");
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

            logger.debug("Erstelle Member");
            // erstelle member
            PgMember pgMember = new PgMember(pgUser, pgGroup, pgRole);

            // add group
            session.persist(pgGroup);
            // add member
            session.persist(pgMember);

            session.getTransaction().commit();
            logger.debug("ID {} wurde mit dem Group {} erfolgreich erstellt.", pgGroup.getGroupid(), pgGroup.getGroupname());
            logger.debug("ID {} Member wurde erfolgreich erstellt.", pgMember.getMemberid());
            session.close();


            String response = getJSONCreator(201)
                    .addKeys("success", "name")
                    .addValue( "Gruppe wurde Erfolgreich erstellt!", query.get(EGroup.NAME.toString())).toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Delete implements HttpHandler {
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

            if(!query.containsKey(EGroup.NAME.toString()) || !query.containsKey(EUser.TOKEN.toString())) {
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
                        .addValue("Du bist nicht Mitglied.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(pgMember.getMemberrole().getRolename() != "Admin") {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(error)
                        .addValue("Du bist nicht Admin.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }


            // remove group
            session.remove(pgGroup);

            session.getTransaction().commit();
            session.close();


            String response = getJSONCreator(201)
                    .addKeys("success", "name")
                    .addValue( "Gruppe wurde Erfolgreich gelöscht!", query.get(EGroup.NAME.toString())).toString();

            writeResponse(httpExchange, response, 201);
        }
    }

    private static class Users implements HttpHandler {
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

            if(!query.containsKey(EGroup.NAME.toString())) {
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

            Session session = Azubiprojekt.getSqlPostgres().openSession();

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

            queryString = "FROM PgMember pgmember WHERE pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, PgRole.class);
            queryDatabase.setParameter("group", pgGroup);
            List<PgMember> pgMembers = queryDatabase.list();
            logger.debug("Lese member records...");

            ArrayList memberList = new ArrayList<>();
            for (PgMember rawMembers : pgMembers) {
                memberList.add(rawMembers.getMemberuser().getUsername());
            }

            String response = getJSONCreator(201)
                    .addKeys("response", "members")
                    .addValue("Du hast erfolgreich die Members abgefragt!", new JSONObject(memberList).toString()).toString();

            writeResponse(httpExchange, response, 201);
        }
    }
}
