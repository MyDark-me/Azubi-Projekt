package org.devcloud.ap.utils.apihelper.databsehelper;

import com.google.gson.Gson;
import io.sentry.Sentry;
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
        MEMBER_SUCCESSFUL_JOINED(201, "Member ist erfolgreich der Gruppe beigetreten!"),
        MEMBER_SUCCESSFUL_LEAVED(201, "Member hat erfolgreich die Gruppe verliassen!"),
        MEMBER_SUCCESSFUL_EDITED(201, "Member hat erfolgreich bearbeitet!"),
        MEMBER_NOT_ACCESSED(400, "Du bist nicht Mitglied."),
        ROLE_NOT_ADMIN(400, "Du bist nicht Admin."),
        USER_NOT_EXIST(400, "Der Benutzer existiert nicht!"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    private static final String ADMIN_GROUP = "Admin";

    public void joinGroup() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            APGroup apGroup = GroupDatabaseHelper.searchGroupByName(session, this);
            APUser apUser = UserDatabaseHelper.searchUserByToken(session, this);
            APMember apMember = searchMemberByUserGroup(session, this, apGroup, apUser);

            if(apMember.getRole().getName().equals(ADMIN_GROUP)) {
                this.getResponse().writeResponse(EMessages.ROLE_NOT_ADMIN);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.ROLE_NOT_ADMIN.getMessage());
            }

            APUser apUserTarget = UserDatabaseHelper.searchUserByName(session, this);

            APRole apRole = RoleDatabaseHelper.searchRoleByName(session, this, "Member");

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
            Sentry.captureException(e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void leaveGroup() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            APGroup apGroup = GroupDatabaseHelper.searchGroupByName(session, this);

            APUser apUserTarget = UserDatabaseHelper.searchUserByName(session, this);
            APMember apMemberTarget = searchMemberByUserGroup(session, this, apGroup, apUserTarget);
            APUser apUser = UserDatabaseHelper.searchUserByToken(session, this);

            if(!apMemberTarget.getUser().equals(apUser)) {
                APMember apMember = searchMemberByUserGroup(session, this, apGroup, apUser);

                if(apMember.getRole().getName().equals(ADMIN_GROUP)) {
                    this.getResponse().writeResponse(EMessages.ROLE_NOT_ADMIN);
                    this.getInputHelper().setCalled(true);
                    throw new DatabaseException(EMessages.ROLE_NOT_ADMIN.getMessage());
                }
            }

            // Lösche Member
            session.beginTransaction();
            session.remove(apMemberTarget);
            session.getTransaction().commit();

            this.getResponse().writeResponse(EMessages.MEMBER_SUCCESSFUL_LEAVED);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Löschen des Users.", e);
            Sentry.captureException(e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void editRole() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            APGroup apGroup = GroupDatabaseHelper.searchGroupByName(session, this);
            APUser apUser = UserDatabaseHelper.searchUserByName(session, this);
            APMember apMember = searchMemberByUserGroup(session, this, apGroup, apUser);

            if(apMember.getRole().getName().equals(ADMIN_GROUP)) {
                this.getResponse().writeResponse(EMessages.ROLE_NOT_ADMIN);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.ROLE_NOT_ADMIN.getMessage());
            }

            APUser apUserTarget = UserDatabaseHelper.searchUserByName(session, this);
            APMember apMemberTarget = searchMemberByUserGroup(session, this, apGroup, apUserTarget);

            // Hole die neue Gruppe
            APRole apRole = RoleDatabaseHelper.searchRoleByName(session, this, this.getInputHelper().getRoleName());
            apMemberTarget.setRole(apRole);

            session.beginTransaction();
            session.merge(apMemberTarget);
            session.getTransaction().commit();

            this.getResponse().writeResponse(EMessages.MEMBER_SUCCESSFUL_EDITED);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Löschen des Users.", e);
            Sentry.captureException(e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void fetchGroups() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            APUser apUser = UserDatabaseHelper.searchUserByName(session, this);

            // Hole joined Member
            Query<APMember> queryMembers = session.createNamedQuery("@HQL_GET_ALL_MEMBERS_USER", APMember.class);
            queryMembers.setParameter("user", apUser);

            if(queryMembers.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
            }

            List<APMember> apMembers = queryMembers.list();

            this.getLogger().debug("Lese Group records von dem Benutzer {}:{}", apUser.getName(), apUser.getId());

            ArrayList<String> groupNameList = new ArrayList<>();
            for (APMember rawMember : apMembers) {
                groupNameList.add(rawMember.getGroup().getName());
            }

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("groups", new Gson().toJson(groupNameList));

            this.getResponse().writeResponse(jsonCreator);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Abfragen der Gruppen des Benutzers.", e);
            Sentry.captureException(e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public static APMember searchMemberByUserGroup(Session session, DatabaseHelper databaseHelper, APGroup apGroup, APUser apUser) throws DatabaseException {
        // Hole den Member mit der Gruppe und dem User
        Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_SEARCH_MEMBER_USER_GROUP", APMember.class);
        queryMember.setParameter("user", apUser);
        queryMember.setParameter("group", apGroup);

        if(queryMember.list().isEmpty()) {
            databaseHelper.getResponse().writeResponse(EMessages.MEMBER_NOT_ACCESSED);
            databaseHelper.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.MEMBER_NOT_ACCESSED.getMessage());
        }
        return queryMember.uniqueResult();
    }

    public static List<APMember> getGroupMemberOfGroupName(Session session, DatabaseHelper databaseHelper, APGroup apGroup) throws DatabaseException {
        Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_ALL_MEMBERS_GROUP", APMember.class);
        queryMember.setParameter("group", apGroup);

        if(queryMember.list().isEmpty()) {
            databaseHelper.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            databaseHelper.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }

        databaseHelper.getLogger().debug("Lese Member records von der Gruppe {}:{}", apGroup.getName(), apGroup.getId());
        return queryMember.list();
    }

}
