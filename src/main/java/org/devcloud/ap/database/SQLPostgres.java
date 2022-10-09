package org.devcloud.ap.database;

import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;

public class SQLPostgres {
    @Getter SessionFactory sessionFactory;

    public SQLPostgres(String host, String user, String password, String database) {
        String url = "jdbc:postgresql://%host%/%database%?ApplicationName=azubiprojekt";
        url = url.replace("%host%", host).replace("%database%", database);

        StandardServiceRegistryBuilder standardServiceRegistryBuilder = new StandardServiceRegistryBuilder()
                .configure()
                .applySetting(AvailableSettings.DIALECT, "org.hibernate.dialect.PostgreSQLDialect")
                .applySetting(AvailableSettings.DRIVER, "org.postgresql.Driver")
                .applySetting(AvailableSettings.URL, url)
                .applySetting(AvailableSettings.USER, user)
                .applySetting(AvailableSettings.PASS, password)
                ;
        try {
            this.sessionFactory = new MetadataSources(
                    standardServiceRegistryBuilder.build()
            ).buildMetadata().buildSessionFactory();
            System.out.println("SQL: SessionFactory wurde Erstellt");
        } catch (Exception e) {
            System.out.println("SQL: SessionFactory konnte nicht erstellt werden. Error: ");
            e.printStackTrace();
        }
    }

    public Session openSession() {
        return this.sessionFactory.openSession();
    }

    public void closeSession(Session session) {
        session.close();
    }
}
