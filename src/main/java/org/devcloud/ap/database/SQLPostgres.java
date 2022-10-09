package org.devcloud.ap.database;

import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

public class SQLPostgres {
    @Getter SessionFactory sessionFactory;

    public SQLPostgres() {
        StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder()
                .configure()
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting(AvailableSettings.DRIVER, "org.postgresql.Driver")
                .applySetting(AvailableSettings.URL, "jdbc:postgresql://localhost:5432/azubiprojekt?ApplicationName=azubiprojekt")
                .applySetting(AvailableSettings.USER, "postgres")
                .applySetting(AvailableSettings.PASS, "postgres")
                ;
        try {
            sessionFactory = new MetadataSources(
                    standardServiceRegistryBuilder.build()
            ).buildMetadata().buildSessionFactory();
            System.out.println("SQL: SessionFactory wurde Erstellt");
        } catch (Exception e) {
            System.out.println("SQL: SessionFactory konnte nicht erstellt werden. Error: ");
            System.out.printf(e.getMessage());
        }
    }

    public Session openSession() {
        return sessionFactory.openSession();
    }

    public void closeSession(Session session) {
        session.close();
    }
}
