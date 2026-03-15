package com.skillbarter.util;

import com.skillbarter.exception.DatabaseException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * CONCEPT 12: Hibernate utility — builds and provides the SessionFactory.
 * SessionFactory is heavy; create once and reuse (singleton pattern).
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .buildSessionFactory();
            System.out.println("✅ Hibernate SessionFactory initialized — connected to AWS RDS");
        } catch (Exception e) {
            System.err.println("❌ Failed to create Hibernate SessionFactory: " + e.getMessage());
            throw new DatabaseException("Hibernate initialization failed", e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            System.out.println("Hibernate SessionFactory closed.");
        }
    }
}
