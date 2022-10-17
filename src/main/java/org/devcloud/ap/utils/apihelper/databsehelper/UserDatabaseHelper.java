package org.devcloud.ap.utils.apihelper.databsehelper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.ResponseMessage;
import org.devcloud.ap.utils.apihelper.Token;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class UserDatabaseHelper extends DatabaseHelper {

    public UserDatabaseHelper(InputHelper inputHelper) {
        super(inputHelper);
    }
    @RequiredArgsConstructor
    private enum EMessages implements ResponseMessage {
        LIST_EMPTY(400, "Die Result liste ist Leer!"),
        USER_EXIST(400, "Der Benutzer existiert bereits!"),
        USER_NOT_EXIST(400, "Der Benutzer existiert nicht!"),
        USER_REMOVED(201,"User wurde erfolgreich gelöscht!"),
        USER_WRONG_USERNAME_PASSWORD_GENERAL(400, "Der Username oder das Passwort ist falsch."),
        TOKEN_INVALID(400, "Der Token ist nicht gültig."),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    /**
     * @param throwByExist if true throw exception if user exist
     */
    public void checkUserExist(boolean throwByExist) throws DatabaseException {
        if(this.getInputHelper().isCalled()) return;
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            Query<Long> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_COUNT", Long.class);
            queryUser.setParameter("name", this.getInputHelper().getUserName());

            if(queryUser.getResultList().isEmpty()) {
                this.getInputHelper().getResponse().writeResponse(EMessages.LIST_EMPTY);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.LIST_EMPTY.getMessage());
            }

            Long count = queryUser.uniqueResult();

            this.getLogger().debug("Es wurden {} Benutzer gefunden.", count);

            if(throwByExist) {
                if (count > 0) {
                    this.getResponse().writeResponse(EMessages.USER_EXIST);
                    this.getInputHelper().setCalled(true);
                    throw new DatabaseException(EMessages.USER_EXIST.getMessage());
                }
            } else {
                if(count < 0) {
                    this.getResponse().writeResponse(EMessages.USER_NOT_EXIST);
                    this.getInputHelper().setCalled(true);
                    throw new DatabaseException(EMessages.USER_NOT_EXIST.getMessage());
                }
            }

        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Suchen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void addUser() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            // Ersellen des Benutzers
            String randomToken = Token.getRandomToken();
            APUser apUser = new APUser(
                    this.getInputHelper().getUserName(),
                    this.getInputHelper().getUserPassword(),
                    this.getInputHelper().getUserEMail(),
                    randomToken);

            // Adden des Benutzers in die Datenbank
            session.beginTransaction();

            session.persist(apUser);

            session.getTransaction().commit();
            this.getLogger().debug("Benuter: ID {}, Name {} wurde erfolgreich erstellt.", apUser.getId(), apUser.getName());

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put(EUser.NAME.toString(), apUser.getName());
            jsonCreator.put(EUser.EMAIL.toString(), apUser.getEmail());
            jsonCreator.put(EUser.TOKEN.toString(), apUser.getToken());

            this.getResponse().writeResponse(jsonCreator);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Ersellen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void removeUser() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            APUser apUser = searchUserToken(session);
            session.beginTransaction();

            session.remove(apUser);
            session.getTransaction().commit();
            this.getLogger().debug("Der Benutzer {} wurde gelöscht.", getInputHelper().getUserName());

            this.getResponse().writeResponse(EMessages.USER_REMOVED);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Löschen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public void editUser() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            APUser apUser = searchUserToken(session);

            if(this.getInputHelper().getUserName() != null) {
                apUser.setName(this.getInputHelper().getUserName());
            }
            if(this.getInputHelper().getUserPassword() != null) {
                apUser.setPassword(this.getInputHelper().getUserPassword());
                // Weil neues Passwort, neues Token
                apUser.setToken(Token.getRandomToken());
            }
            if(this.getInputHelper().getUserEMail() != null) {
                apUser.setEmail(this.getInputHelper().getUserEMail());
            }

            session.beginTransaction();

            session.merge(apUser);
            this.getLogger().debug("Benutzer {}:{} wurde erfolgreich Aktualisiert.", apUser.getName(), apUser.getId());

            sendUserData(session, apUser);

        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Bearbeiten des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private APUser searchUserToken(Session session) throws DatabaseException {
        Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_TOKEN", APUser.class);
        queryUser.setParameter("token", this.getInputHelper().getUserToken());

        if(queryUser.list().isEmpty()) {
            this.getResponse().writeResponse(EMessages.TOKEN_INVALID);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.TOKEN_INVALID.getMessage());
        }

        APUser apUser = queryUser.uniqueResult();
        this.getLogger().debug("Es wurde der Benutzer {}:{} gefunden.", apUser.getName(), apUser.getId());
        return apUser;
    }

    private void sendUserData(Session session, APUser apUser) {
        session.getTransaction().commit();

        JSONCreator jsonCreator = new JSONCreator();
        jsonCreator.put(EUser.NAME.toString(), apUser.getName());
        jsonCreator.put(EUser.EMAIL.toString(), apUser.getEmail());
        jsonCreator.put(EUser.TOKEN.toString(), apUser.getToken());

        this.getResponse().writeResponse(jsonCreator);
    }

    public void loginUser() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            Query<APUser> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_NAME", APUser.class);
            queryUser.setParameter("name", getInputHelper().getUserName());

            if(queryUser.list().isEmpty()) {
                this.getResponse().writeResponse(EMessages.USER_WRONG_USERNAME_PASSWORD_GENERAL);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.USER_WRONG_USERNAME_PASSWORD_GENERAL.getMessage());
            }

            APUser apUser = queryUser.uniqueResult();
            this.getLogger().debug("Es wurde der Benutzer {}:{} gefunden.", apUser.getName(), apUser.getId());

            // Passwort prüfen
            if(!getInputHelper().getUserPassword().equals(apUser.getPassword())) {
                this.getResponse().writeResponse(EMessages.USER_WRONG_USERNAME_PASSWORD_GENERAL);
                this.getInputHelper().setCalled(true);
                throw new DatabaseException(EMessages.USER_WRONG_USERNAME_PASSWORD_GENERAL.getMessage());
            }

            // new random token
            String randomToken = Token.getRandomToken();
            apUser.setToken(randomToken);

            session.beginTransaction();

            session.merge(apUser);
            this.getLogger().debug("Benutzer {}:{} wurde erfolgreich eingeloggt.", apUser.getName(), apUser.getId());
            sendUserData(session, apUser);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Einloggen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

}
