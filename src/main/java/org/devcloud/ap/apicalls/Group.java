package org.devcloud.ap.apicalls;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APGroup;
import org.devcloud.ap.database.enumeration.EGroup;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.database.enumeration.pattern.EPattern;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.apihelper.Response;
import org.devcloud.ap.utils.apihelper.ResponseMessage;
import org.devcloud.ap.database.APMember;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.database.APUser;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Group {
    private static final Logger logger = LoggerFactory.getLogger(Group.class);

    public static void register(HttpServer httpServer) {
        httpServer.createContext("/api/group/create", new Create());
        httpServer.createContext("/api/group/delete", new Delete());
        httpServer.createContext("/api/group/users", new Users());
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
        GROUP_NOT_EXIST(400, "Die Gruppe existiert nicht."),
        TOKEN_NOT_VALID(400, "Der Token ist nicht gültig."),
        NOT_MEMBER(400, "Du bist nicht Mitglied."),
        NOT_ADMIN(400, "Du bist nicht Admin."),
        GROUP_SUCCESSFUL_DELETED(201, "Gruppe wurde Erfolgreich gelöscht!"),
        NO_MEMBER(400, "Es wurden keine Member mitgegeben."),
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

            if(!EPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
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

            if(!query.containsKey(EGroup.NAME.toString()) || !query.containsKey(EUser.TOKEN.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            if(!EPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            // Öffnen der Datenbank
            try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
                Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
                queryGroup.setParameter("name", query.get(EUser.NAME.toString()));

                if(queryGroup.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.GROUP_NOT_EXIST);
                    return;
                }

                APGroup apGroup = queryGroup.uniqueResult();
                logger.debug("Es wurde die Gruppe {} gefunden.", apGroup.getName());

                Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_TOKEN", APUser.class);
                queryUser.setParameter("token", query.get(EUser.TOKEN.toString()));

                if(queryUser.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.TOKEN_NOT_VALID);
                    return;
                }

                APUser apUser = queryUser.uniqueResult();
                logger.debug("Es wurde der User {} gefunden.", apUser.getName());

                Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_SEARCH_MEMBER_USER_GROUP", APMember.class);
                queryMember.setParameter("user", apUser);
                queryMember.setParameter("group", apGroup);

                if(queryMember.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.NOT_MEMBER);
                    return;
                }

                APMember apMember = queryMember.uniqueResult();
                logger.debug("Es wurde der Member {} gefunden.", apMember.getId());

                if(apMember.getRole().getName().equals("Admin")) {
                    session.close();
                    response.writeResponse(EMessages.NOT_ADMIN);
                    return;
                }

                session.beginTransaction();

                session.remove(apGroup);
                session.getTransaction().commit();
                logger.debug("Die Gruppe {} wurde gelöscht.", apGroup.getName());

                response.writeResponse(EMessages.GROUP_SUCCESSFUL_DELETED);

            } catch (HibernateException e) {
                e.printStackTrace();
                logger.error("Es konnte keine Verbindung zur Datenbank hergestellt werden.");
                response.writeResponse(EMessages.DATABASE_NOT_AVAILABLE);
            }
        }
    }

    private static class Users implements HttpHandler {
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
            if(!query.containsKey(EUser.TOKEN.toString())) {
                response.writeResponse(EMessages.WRONG_INFORMATION);
                return;
            }

            if(!EPattern.NAME.isMatch(query.get(EGroup.NAME.toString()))) {
                response.writeResponse(EMessages.WRONG_NAME);
                return;
            }

            // Öffnen der Datenbank
            try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
                Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
                queryGroup.setParameter("name", query.get(EGroup.NAME.toString()));

                if(queryGroup.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.GROUP_NOT_EXIST);
                    return;
                }

                APGroup apGroup = queryGroup.uniqueResult();
                logger.debug("Es wurde die Gruppe {} gefunden.", apGroup.getId());

                Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_ALL_MEMBERS_GROUP", APMember.class);
                queryMember.setParameter("name", query.get(EGroup.NAME.toString()));

                if(queryGroup.list().isEmpty()) {
                    session.close();
                    response.writeResponse(EMessages.NO_MEMBER);
                    return;
                }

                logger.debug("Lese member records...");
                List<APMember> apMemberList = queryMember.list();

                ArrayList<String> memberNameList = new ArrayList<>();
                for (APMember rawMembers : apMemberList) {
                    memberNameList.add(rawMembers.getUser().getName());
                }

                JSONCreator jsonCreator = new JSONCreator();
                jsonCreator.put("members", new Gson().toJson(memberNameList));

                response.writeResponse(jsonCreator);
            } catch (HibernateException ex) {
                logger.error("Fehler bei einer Datenbanksitzung", ex);
                response.writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
