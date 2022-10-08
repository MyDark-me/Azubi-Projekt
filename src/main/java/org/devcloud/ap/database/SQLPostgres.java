package org.devcloud.ap.database;

import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class SQLPostgres {
    @Getter SessionFactory sessionFactory;
    @Getter StandardServiceRegistry standardServiceRegistry;

    public SQLPostgres() {
        standardServiceRegistry = new StandardServiceRegistryBuilder().configure().build();
        try {
            sessionFactory = new MetadataSources(standardServiceRegistry).buildMetadata().buildSessionFactory();
            System.out.println("SQL: SessionFactory wurde Erstellt");
        } catch (Exception e) {
            System.out.println("SQL: SessionFactory konnte nicht erstellt werden.");
        }
    }

    public Session openSession() {
        return sessionFactory.openSession();
    }

    public void closeSession(Session session) {
        session.close();
    }
}
