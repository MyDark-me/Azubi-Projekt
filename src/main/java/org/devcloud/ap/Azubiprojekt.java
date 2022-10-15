package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.database.APRole;
import org.devcloud.ap.database.APUser;
import org.devcloud.ap.utils.SQLPostgres;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.devcloud.ap.utils.HTTPServer;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.devcloud.ap.utils.SentryLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Azubiprojekt {

    @Getter static SQLPostgres sqlPostgres;
    private static final Logger logger = LoggerFactory.getLogger(Azubiprojekt.class);
    
    public static void main(String[] args) {
        SentryLogger.startSentry();
        logger.info("Starting Azubiprojekt Server");
        try {
            HTTPServer.startServer();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        sqlPostgres = new SQLPostgres("localhost:5432", "postgres", "password", "azubiprojekt");
        autoCreate();
    }

    private static void autoCreate() {
        if(!getSqlPostgres().isConnection()) return;

        try {
            Session session = getSqlPostgres().openSession();

            logger.debug("Erstelle rollen falls nötig!");
            Query<APRole> queryRole = session.createNamedQuery("@HQL_GET_ALL_ROLES", APRole.class);
            List<APRole> apRoles = queryRole.list();
            logger.debug("Lese Role records...");

            List<String> pgRolesName = new ArrayList<>();
            apRoles.forEach(pgRole -> pgRolesName.add(pgRole.getName()));

            session.beginTransaction();

            if(!pgRolesName.contains("Member")) {
                APRole memberRole = new APRole("Member", "GRAY");
                session.persist(memberRole);
                logger.debug("Role Member wird erstellt");
            }
            if(!pgRolesName.contains("Mod")) {
                APRole moderatorRole = new APRole("Mod", "BLUE");
                session.persist(moderatorRole);
                logger.debug("Role Mod wird erstellt");
            }
            if(!pgRolesName.contains("Admin")) {
                APRole administratorRole = new APRole("Admin", "RED");
                session.persist(administratorRole);
                logger.debug("Role Admin wird erstellt");
            }

            session.getTransaction().commit();

            session.close();
        } catch (HibernateException ex) {
            ex.printStackTrace();
        }
    }
}
