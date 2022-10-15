package org.devcloud.ap.apicalls;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APGroup;
import org.devcloud.ap.database.enumeration.EGroup;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.utils.helper.Response;
import org.devcloud.ap.utils.helper.ResponseMessage;
import org.devcloud.ap.database.APMember;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.database.APUser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Group {
    private static final Logger logger = LoggerFactory.getLogger(Group.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/group/create", new Create());
        /*
        httpServer.createContext("/api/group/delete", new Delete());
        httpServer.createContext("/api/group/users", new Users());
        */
    }

    private enum EMessages implements ResponseMessage {
        DATABASE_NOT_AVAILABLE(500, "Datenbank ist nicht Erreichbar!"),
        NO_INFORMATION(400, "Es wurden keine Informationen mitgegeben."),
        WRONG_INFORMATION(400, "Es wurden nicht die richtigen Informationen mitgegeben."),
        WRONG_NAME(400, "Der Name entspricht nicht den Vorgaben."),
        ALREADY_NAME_USE(400, "Der Gruppenname wurde schon vergeben."),
        ROLE_NOT_EXIST(400, "Die Rolle Admin existiert nicht."),
        TOKEN_NOT_EXIST(400, "Der Token ist nicht gültig."),
        GROUP_SUCCESSFUL_CREATED(201, "Gruppe wurde Erfolgreich erstellt!"),
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

            if(!query.containsKey(EGroup.NAME.toString()) || !query.containsKey(EGroup.COLOR.toString()) || !query.containsKey(EUser.TOKEN.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            if(!EPattern.USERNAME.isMatch(query.get(EGroup.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            // Öffnen der Datenbank
            try {
                Session session = Azubiprojekt.getSqlPostgres().openSession();

                // Prüfen ob die Gruppe schon existiert
                Query<Long> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_COUNT", Long.class);
                queryGroup.setParameter("name", query.get(EGroup.NAME.toString()));
                Long count = queryGroup.uniqueResult();

                logger.debug("Es wurden {} Gruppen gefunden.", count);
                if(count > 0) {
                    response.writeResponse(EMessages.ALREADY_NAME_USE);
                    return;
                }

                // Ersellen der Gruppe
                APGroup apGroup = new APGroup(
                        query.get(EGroup.NAME.toString()),
                        query.get(EGroup.COLOR.toString())
                );

                // Hole Admin Rolle
                Query<APRole> queryRole = session.createNamedQuery("@HQL_GET_SEARCH_ROLE_NAME", APRole.class);
                queryRole.setParameter("name", "Admin");

                if(queryRole.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.ALREADY_NAME_USE);
                    return;
                }

                // Hole den User mit dem Token
                Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_TOKEN", APUser.class);
                queryUser.setParameter("token", query.get(EUser.TOKEN.toString()));

                if(queryUser.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.TOKEN_NOT_EXIST);
                    return;
                }

                // Erzeuge den Member
                APMember apMember = new APMember(
                        queryUser.getSingleResult(),
                        apGroup,
                        queryRole.getSingleResult()
                );

                // Speichere die Gruppe und den Member
                session.beginTransaction();
                session.persist(apGroup);
                session.persist(apMember);

                session.getTransaction().commit();
                logger.debug("ID {} wurde mit dem Group {} erfolgreich erstellt.", apGroup.getId(), apGroup.getName());
                logger.debug("ID {} Member wurde erfolgreich erstellt.", apMember.getId());
                session.close();

                response.writeResponse(EMessages.GROUP_SUCCESSFUL_CREATED);
            } catch (HibernateException ex) {
                logger.error("Fehler bei einer Datenbanksitzung", ex);
                response.writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private static class Delete implements HttpHandler {
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

            // Prüfe ob alles den syntax vorgibt

            HashMap<String, String> query = getEntities(requestURI);
            if(query.isEmpty()) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.NO_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EGroup.NAME.toString()) || !query.containsKey(ApiCallsLang.TOKEN)) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(EGroupPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_NAME).toString();

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
                        .addKeys(ERROR)
                        .addValue("Die Gruppe existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            logger.debug("Suche User mit dem Token");
            // hole user
            queryString = "FROM PgUser pguser WHERE pguser.usertoken= :usertoken";
            queryDatabase = session.createQuery(queryString, APUser.class);
            queryDatabase.setParameter("usertoken", query.get(ApiCallsLang.TOKEN));
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
            queryDatabase.setParameter("group", pgGroup);
            APMember APMember = (APMember) queryDatabase.uniqueResult();

            if(APMember == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Du bist nicht Mitglied.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!APMember.getMemberrole().getRolename().equals("Admin")) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
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
                        .addValue(ApiCallsLang.NO_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(!query.containsKey(EGroup.NAME.toString())) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_INFORMATION).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            if(EGroupPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue(ApiCallsLang.WRONG_NAME).toString();

                writeResponse(httpExchange, response, 400);
                return;
            }
            Session session = null;
            try{
                logger.debug("SQLSession \"Try StartSqlConnection");
                session = Azubiprojekt.getSqlPostgres().openSession();
            }
            catch (Exception ex){
                String logmsg = ex.getMessage();
                logger.error("{}", logmsg);
            }finally {
                assert session != null;
                Azubiprojekt.getSqlPostgres().closeSession(session);
            }

            logger.debug("Suche Group");
            // hole role
            String queryString = "FROM PgGroup pggroup WHERE pggroup.groupname= :groupname";
            Query<PgGroup> queryDatabase = session.createQuery(queryString, PgGroup.class);
            queryDatabase.setParameter("groupname", query.get(EGroup.NAME.toString()));
            PgGroup pgGroup = queryDatabase.uniqueResult();

            if(pgGroup == null) {
                session.close();
                // user existiert nicht
                String response = getJSONCreator(400)
                        .addKeys(ERROR)
                        .addValue("Die Gruppe existiert nicht.").toString();

                writeResponse(httpExchange, response, 400);
                return;
            }

            queryString = "FROM PgMember pgmember WHERE pgmember.membergroup= :group";
            queryDatabase = session.createQuery(queryString, APMember.class);
            queryDatabase.setParameter("group", pgGroup);
            List<APMember> APMembers = queryDatabase.list();
            logger.debug("Lese member records...");

            ArrayList<String> memberList = new ArrayList<>();
            for (APMember rawMembers : APMembers) {
                memberList.add(rawMembers.getMemberuser().getUsername());
            }

            String response = getJSONCreator(201)
                    .addKeys("response", "members")
                    .addValue("Du hast erfolgreich die Members abgefragt!", new Gson().toJson(memberList)).toString();

            writeResponse(httpExchange, response, 201);
        }
    }
}
