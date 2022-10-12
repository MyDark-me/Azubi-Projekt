package org.devcloud.ap;

import lombok.Getter;
import org.devcloud.ap.database.PgRole;
import org.devcloud.ap.utils.SQLPostgres;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.devcloud.ap.utils.HTTPServer;
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
        if(!sqlPostgres.isConnection()) return;

        Session session = sqlPostgres.openSession();
        session.beginTransaction();

        logger.debug("Erstelle Roles falls nötig!");

        Query query = session.createQuery("FROM PgRole", PgRole.class);
        List<PgRole> pgRoles = query.list();
        logger.debug("Lese Role records...");

        List<String> pgRolesName = new ArrayList<>();
        pgRoles.forEach(pgRole -> pgRolesName.add(pgRole.getRolename()));

        if(!pgRolesName.contains("Member")) {
            PgRole memberRole = new PgRole("Member", "GRAY");
            session.persist(memberRole);
            logger.debug("Role Member wird erstellt");
        }
        if(!pgRolesName.contains("Mod")) {
            PgRole moderatorRole = new PgRole("Mod", "BLUE");
            session.persist(moderatorRole);
            logger.debug("Role Mod wird erstellt");
        }
        if(!pgRolesName.contains("Admin")) {
            PgRole administratorRole = new PgRole("Admin", "RED");
            session.persist(administratorRole);
            logger.debug("Role Admin wird erstellt");
        }

        session.getTransaction().commit();
        session.close();
    }
}
