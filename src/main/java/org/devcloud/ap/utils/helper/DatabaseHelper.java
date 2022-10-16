package org.devcloud.ap.utils.helper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.database.enumeration.EUser;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.helper.exeption.DatabaseException;
import org.devcloud.ap.utils.helper.exeption.NoResultException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;

@RequiredArgsConstructor
public class DatabaseHelper {
    @Getter
    private final InputHelper inputHelper;

    @RequiredArgsConstructor
    private enum EMessages implements ResponseMessage {
        LIST_EMPTY(500, "Die Result liste ist Leer!"),
        USER_EXIST(500, "Der Benutzer existiert bereits!"),
        USER_NOT_EXIST(500, "Der Benutzer existiert nicht!"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    public Response getResponse() {
        return inputHelper.getResponse();
    }

    public Logger getLogger() {
        return inputHelper.getResponse().getLogger();
    }

    public Long checkUserExist() throws NoResultException, DatabaseException {
        if(this.inputHelper.isCalled()) return null;
        Long count = null;

        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {
            Query<Long> queryUser = session.createNamedQuery("@HQL_GET_SEARCH_USER_COUNT", Long.class);
            queryUser.setParameter("name", this.inputHelper.getUserName());

            if(queryUser.getResultList().isEmpty()) {
                this.inputHelper.getResponse().writeResponse(EMessages.LIST_EMPTY);
                throw new NoResultException(EMessages.LIST_EMPTY.getMessage());
            }

            count = queryUser.uniqueResult();

            this.getLogger().debug("Es wurden {} Benutzer gefunden.", count);
            if(count > 0) {
                this.getResponse().writeResponse(EMessages.USER_EXIST);
                throw new DatabaseException(EMessages.USER_EXIST.getMessage());
            }
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Suchen des Users.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
        return count;
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
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

}
