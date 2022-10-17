package org.devcloud.ap.utils.apihelper.databsehelper;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.devcloud.ap.Azubiprojekt;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.utils.JSONCreator;
import org.devcloud.ap.utils.apihelper.InputHelper;
import org.devcloud.ap.utils.apihelper.ResponseMessage;
import org.devcloud.ap.utils.apihelper.exeption.DatabaseException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.HashMap;
import java.util.List;

public class RoleDatabaseHelper extends DatabaseHelper {

    public RoleDatabaseHelper(InputHelper inputHelper) {
        super(inputHelper);
    }
    @RequiredArgsConstructor
    private enum EMessages implements ResponseMessage {
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");

        @Getter
        private final int rCode;
        @Getter
        private final String message;
    }

    public void allRoles() throws DatabaseException {
        try(Session session = Azubiprojekt.getSqlPostgres().openSession()) {

            Query<APRole> query = session.createNamedQuery("@HQL_GET_ALL_ROLES", APRole.class);
            List<APRole> apRoles = query.list();

            HashMap<Integer, HashMap<String, String>> roles = new HashMap<>();
            for (APRole rawRole : apRoles) {
                HashMap<String, String> infos = new HashMap<>();

                infos.put("name", rawRole.getName());
                infos.put("color", rawRole.getColor());
                roles.put(rawRole.getId(), infos);
            }

            JSONCreator jsonCreator = new JSONCreator();
            jsonCreator.put("roles", new Gson().toJson(roles));

            this.getResponse().writeResponse(jsonCreator);
        } catch (HibernateException e) {
            this.getLogger().error("Fehler beim Suchen des der Roles.", e);
            this.getResponse().writeResponse(EMessages.INTERNAL_SERVER_ERROR);
            this.getInputHelper().setCalled(true);
            throw new DatabaseException(EMessages.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

}
