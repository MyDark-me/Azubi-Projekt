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

public class GroupDatabaseHelper extends DatabaseHelper {

    public GroupDatabaseHelper(InputHelper inputHelper) {
        super(inputHelper);
    }
    @RequiredArgsConstructor
    private enum EMessages implements ResponseMessage {
        LIST_EMPTY(400, "Die Result liste ist Leer!"),
        GROUP_EXIST(400, "Die Gruppe existiert bereits!"),
        GROUP_NOT_EXIST(400, "Die Gruppe existiert nicht!"),
        GROUP_SUCCESSFUL_CREATED(201, "Gruppe wurde erfolgreich erstellt!"),
        GROUP_SUCCESSFUL_REMOVED(201, "Gruppe wurde erfolgreich gelöscht!"),
        MEMBER_NOT_ACCESSED(400, "Du bist nicht Mitglied."),
        ROLE_NOT_ADMIN(400, "Du bist nicht Admin."),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    /**
     * @param throwByExist if true throw exception if group exist
     */
    public void checkGroupExist(boolean throwByExist) throws DatabaseException {
        if(this.getInputHelper().isCalled()) return;
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            Query<Long> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_COUNT", Long.class);
            queryGroup.setParameter("name", this.getInputHelper().getGroupName());

            if(queryGroup.getResultList().isEmpty()) {
                this.getInputHelper().getResponse().writeResponse(EMessages.LIST_EMPTY);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.LIST_EMPTY.getMessage());
            }

            Long count = queryGroup.uniqueResult();

            this.getLogger().debug("Es wurden {} Gruppen gefunden.", count);

            if(throwByExist) {
                if (count > 0) {
                    this.getResponse().writeResponse(EMessages.GROUP_EXIST);
                    this.getInputHelper().setCalled(true);
                    throw new DatabaseException(EMessages.GROUP_EXIST.getMessage());
                }
            } else {
                if(count < 0) {
                    this.getResponse().writeResponse(EMessages.GROUP_NOT_EXIST);
                    this.getInputHelper().setCalled(true);
                    throw new DatabaseException(EMessages.GROUP_NOT_EXIST.getMessage());
                }
            }

        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Suchen der Gruppe.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void addGroup() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            // Ersellen der Gruppe
            APGroup apGroup = new APGroup(
                    this.getInputHelper().getGroupName(),
                    this.getInputHelper().getGroupColor()
            );

            APUser apUser = UserDatabaseHelper.searchUserByToken(session, this);
            APRole apRole = RoleDatabaseHelper.searchRoleByName(session, this, "Member");
            APMember apMember = new APMember(apUser, apGroup, apRole);

            // Speichere die Gruppe und den Member
            session.beginTransaction();

            session.persist(apGroup);
            session.persist(apMember);

            session.getTransaction().commit();
            this.getLogger().debug("Die Gruppe {}:{} wurde erfolgreich erstellt.", apGroup.getId(), apGroup.getName());
            this.getLogger().debug("Member {} wurde erfolgreich erstellt.", apMember.getId());

            this.getResponse().writeResponse(EMessages.GROUP_SUCCESSFUL_CREATED);
        } catch (HibernateException e) {
            e.printStackTrace();
            this.getLogger().error("Fehler beim Ersellen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void removeGroup() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            APGroup apGroup = GroupDatabaseHelper.searchGroupByName(session, this);
            APUser apUser = UserDatabaseHelper.searchUserByToken(session, this);

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

            session.beginTransaction();

            session.remove(apGroup);
            session.getTransaction().commit();
            this.getLogger().debug("Die Gruppe {}:{} wurde gelöscht.", apGroup.getName(), apGroup.getId());

            this.getResponse().writeResponse(EMessages.GROUP_SUCCESSFUL_REMOVED);

        } catch (HibernateException e) {
            e.printStackTrace();
            this.getLogger().error("Fehler beim Löschen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void fetchUsers() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
            queryGroup.setParameter("name", this.getInputHelper().getGroupName());

            if(queryGroup.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.GROUP_NOT_EXIST);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.GROUP_NOT_EXIST.getMessage());
            }

            APGroup apGroup = queryGroup.uniqueResult();
            this.getLogger().debug("Es wurde die Gruppe {}:{} gefunden.", apGroup.getName(), apGroup.getId());

            Query<APMember> queryMember = session.createNamedQuery("@HQL_GET_ALL_MEMBERS_GROUP", APMember.class);
            queryMember.setParameter("name", this.getInputHelper().getGroupName());

            this.getLogger().debug("Lese Member records von der Gruppe {}:{}", apGroup.getName(), apGroup.getId());
            List<APMember> apMemberList = queryMember.list();

            ArrayList<String> memberNameList = new ArrayList<>();
            for (APMember rawMembers : apMemberList) {
                memberNameList.add(rawMembers.getUser().getName());
            }

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("members", new Gson().toJson(memberNameList));

            this.getResponse().writeResponse(jsonCreator);
        } catch (HibernateException e) {
            e.printStackTrace();
            this.getLogger().error("Fehler beim Abfragen der Users in einer Guruppe.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }


    public static APGroup searchGroupByName(Session session, DatabaseHelper databaseHelper) throws DatabaseException {
        // Hole Gruppe mit Namen
        Query<APGroup> queryGroup = session.createNamedQuery("@HQL_GET_SEARCH_GROUP_NAME", APGroup.class);
        queryGroup.setParameter("name", databaseHelper.getInputHelper().getGroupName());

        if(queryGroup.list().isEmpty()) {
            databaseHelper.getResponse().writeResponse(EMessages.GROUP_NOT_EXIST);
            databaseHelper.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.GROUP_NOT_EXIST.getMessage());
        }

        APGroup apGroup = queryGroup.uniqueResult();
        databaseHelper.getLogger().debug("Es wurde die Gruppe {}:{} gefunden.", apGroup.getName(), apGroup.getId());
        return apGroup;
    }
}
