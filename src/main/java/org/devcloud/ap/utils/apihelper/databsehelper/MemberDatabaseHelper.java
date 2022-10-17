package org.devcloud.ap.utils.apihelper.databsehelper;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APGroup;
import org.devcloud.ap.database.APMember;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.ResponseMessage;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

public class MemberDatabaseHelper extends DatabaseHelper {

    public MemberDatabaseHelper(InputHelper inputHelper) {
        super(inputHelper);
    }
    @RequiredArgsConstructor
    private enum EMessages implements ResponseMessage {
        LIST_EMPTY(400, "Die Result liste ist Leer!"),
        GROUP_NOT_EXIST(400, "Die Gruppe existiert nicht!"),
        TOKEN_INVALID(400, "Der Token ist nicht gültig."),
        MEMBER_SUCCESSFUL_JOINED(201, "Member ist erfolgreich der Gruppe beigetreten!"),
        MEMBER_SUCCESSFUL_LEAVED(201, "Member hat erfolgreich die Gruppe verliassen!"),
        MEMBER_SUCCESSFUL_EDITED(201, "Member hat erfolgreich bearbeitet!"),
        MEMBER_NOT_ACCESSED(400, "Du bist nicht Mitglied."),
        ROLE_NOT_ADMIN(400, "Du bist nicht Admin."),
        ROLE_NOT_EXIST(400, "Die Rolle existiert nicht."),
        USER_NOT_EXIST(400, "Der Benutzer existiert nicht!"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    public void joinGroup() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            // Hole Gruppe mit Namen
            Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
            queryGroup.setParameter("name", this.getInputHelper().getGroupName());

            if(queryGroup.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.GROUP_NOT_EXIST);
                throw new DatabaseException(EMessages.GROUP_NOT_EXIST.getMessage());
            }

            APGroup apGroup = queryGroup.uniqueResult();

            APUser apUser = searchUserByToken(session);

            // Hole den Member mit der Gruppe und dem User
            Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_SEARCH_MEMBER_USER_GROUP", APMember.class);
            queryMember.setParameter("user", apUser);
            queryMember.setParameter("group", apGroup);

            if(queryMember.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.MEMBER_NOT_ACCESSED);
                throw new DatabaseException(EMessages.MEMBER_NOT_ACCESSED.getMessage());
            }

            APMember apMember = queryMember.uniqueResult();

            if(apMember.getRole().getName().equals("Admin")) {
                this.getResponse().writeResponse(EMessages.ROLE_NOT_ADMIN);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.ROLE_NOT_ADMIN.getMessage());
            }

            // Hole den neuen Benutzer
            Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_NAME", APUser.class);
            queryUser.setParameter("name", this.getInputHelper().getUserName());

            if(queryUser.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.USER_NOT_EXIST);
                throw new DatabaseException(EMessages.USER_NOT_EXIST.getMessage());
            }

            APUser apUserTarget = queryUser.uniqueResult();

            APRole apRole = searchRoleByName(session, "Member");

            // Erstelle den Member

            APMember apMemberTarget = new APMember(apUserTarget, apGroup, apRole);


            // Speichere den neuen Member
            session.beginTransaction();

            session.persist(apMemberTarget);

            session.getTransaction().commit();
            this.getLogger().debug("Member {} wurde erfolgreich der Gruppe {}:{} hinzugefügt.", apMember.getId(), apGroup.getName(), apGroup.getId());

            this.getResponse().writeResponse(EMessages.MEMBER_SUCCESSFUL_JOINED);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Ersellen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private APRole searchRoleByName(Session session, String member) throws HibernateException, DatabaseException {
        // Hole Admin Role
        Query<APRole> queryRole = session.createNamedQuery("@HQL_GET_SEARCH_ROLE_NAME", APRole.class);
        queryRole.setParameter("name", member);

        if(queryRole.list().isEmpty())  {
            this.getResponse().writeResponse(EMessages.ROLE_NOT_EXIST);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.ROLE_NOT_EXIST.getMessage());
        }

        return queryRole.uniqueResult();
    }

    private APUser searchUserByToken(Session session) throws DatabaseException {
        Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_TOKEN", APUser.class);
        queryUser.setParameter("token", this.getInputHelper().getUserToken());

        if(queryUser.list().isEmpty()) {
            this.getResponse().writeResponse(EMessages.TOKEN_INVALID);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.TOKEN_INVALID.getMessage());
        }

        return queryUser.uniqueResult();
    }

    public void leaveGroup() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            this.getResponse().writeResponse(EMessages.MEMBER_SUCCESSFUL_LEAVED);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Löschen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void editRole() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            // Hole Gruppe mit Namen
            Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
            queryGroup.setParameter("name", this.getInputHelper().getGroupName());

            if(queryGroup.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.GROUP_NOT_EXIST);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.GROUP_NOT_EXIST.getMessage());
            }

            APGroup apGroup = queryGroup.uniqueResult();

            APUser apUser = searchUserByToken(session);

            // Hole den Member mit der Gruppe und dem User
            Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_SEARCH_MEMBER_USER_GROUP", APMember.class);
            queryMember.setParameter("user", apUser);
            queryMember.setParameter("group", apGroup);

            if(queryMember.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.MEMBER_NOT_ACCESSED);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.MEMBER_NOT_ACCESSED.getMessage());
            }

            APMember apMember = queryMember.uniqueResult();

            if(apMember.getRole().getName().equals("Admin")) {
                this.getResponse().writeResponse(EMessages.ROLE_NOT_ADMIN);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.ROLE_NOT_ADMIN.getMessage());
            }

            // Hole den Benuzter mit dem Namen
            Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_NAME", APUser.class);
            queryUser.setParameter("name", this.getInputHelper().getUserName());

            if(queryUser.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.USER_NOT_EXIST);
                throw new DatabaseException(EMessages.USER_NOT_EXIST.getMessage());
            }

            APUser apUserTarget = queryUser.uniqueResult();

            // Hole den Member mit der Gruppe und dem User
            Query<APMember> queryMemberTarget = session.createNamedQuery("@HQL_GET_SEARCH_MEMBER_USER_GROUP", APMember.class);
            queryMemberTarget.setParameter("user", apUserTarget);
            queryMemberTarget.setParameter("group", apGroup);

            if(queryMemberTarget.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.MEMBER_NOT_ACCESSED);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.MEMBER_NOT_ACCESSED.getMessage());
            }

            APMember apMemberTarget = queryMemberTarget.uniqueResult();

            // Hole die neue Gruppe
            APRole apRole = searchRoleByName(session, this.getInputHelper().getRoleName());

            apMemberTarget.setRole(apRole);

            session.beginTransaction();
            session.merge(apMemberTarget);
            session.getTransaction().commit();

            this.getResponse().writeResponse(EMessages.MEMBER_SUCCESSFUL_EDITED);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Löschen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void fetchGroups() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            // Hole Group
            Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
            queryGroup.setParameter("name", this.getInputHelper().getGroupName());

            if(queryGroup.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.GROUP_NOT_EXIST);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.GROUP_NOT_EXIST.getMessage());
            }

            APGroup apGroup = queryGroup.uniqueResult();
            this.getLogger().debug("Es wurde die Gruppe {}:{} gefunden.", apGroup.getName(), apGroup.getId());

            // Hole User
            Query<APUser> userQuery = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APUser.class);
            userQuery.setParameter("name", this.getInputHelper().getGroupName());

            if(userQuery.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.USER_NOT_EXIST);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.USER_NOT_EXIST.getMessage());
            }

            APUser apUser = userQuery.uniqueResult();
            this.getLogger().debug("Es wurde der User {}:{} gefunden.", apUser.getName(), apUser.getId());

            // Hole joined Member
            Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_SEARCH_MEMBER_USER_GROUP", APMember.class);
            queryMember.setParameter("group", apGroup);
            queryMember.setParameter("user", apUser);

            this.getLogger().debug("Lese Group records von dem Benutzer {}:{}", apUser.getName(), apUser.getId());
            List<APMember> apMemberList = queryMember.list();

            ArrayList<String> memberNameList = new ArrayList<>();
            for (APMember rawMembers : apMemberList) {
                memberNameList.add(rawMembers.getGroup().getName());
            }

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("groups", new Gson().toJson(memberNameList));

            this.getResponse().writeResponse(jsonCreator);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Abfragen der Gruppen des Benutzers.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

}
