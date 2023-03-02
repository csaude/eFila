/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 *  org.hibernate.SessionFactory
 *  org.hibernate.cfg.AnnotationConfiguration
 *  org.hibernate.cfg.Configuration
 *  org.hibernate.classic.Session
 */
package migracao.connection;

import migracao.entidades.*;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.JdbcProperties;
import org.celllife.idart.commonobjects.iDartProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

public class hibernateConectionRemote {
    private static SessionFactory sessionFactoryRemote;
    private static final ThreadLocal<Session> threadRemote;
    final static Logger log = Logger.getLogger(hibernateConectionRemote.class);


    public static org.hibernate.Session getInstanceRemote() {
        org.hibernate.Session session = threadRemote.get();
        session = sessionFactoryRemote.openSession();
        threadRemote.set(session);
        return session;
    }

     static {
        threadRemote = new ThreadLocal();
        try {

            Configuration config = new AnnotationConfiguration().configure("migracao/connection/hibernateRemote.cfg.xml");

            config.setProperty("hibernate.connection.driver_class", JdbcProperties.hibernateOpenMRSDriver);
            config.setProperty("hibernate.connection.url",JdbcProperties.hibernateOpenMRSConnectionUrl);
            config.setProperty("hibernate.connection.username",JdbcProperties.hibernateOpenMRSUsername);
            config.setProperty("hibernate.connection.password", JdbcProperties.hibernateOpenMRSPassword);
            config.setProperty("hibernate.dialect", JdbcProperties.hibernateOpenMRSDialect);
            config.setProperty("hibernate.current_session_context_class", "thread");
            config.setProperty("hibernate.show_sql", String.valueOf(false));
            config.setProperty("hibernate.jdbc.fetch_size", String.valueOf(1));

            sessionFactoryRemote = config.buildSessionFactory();

           log.trace("Reiniciando a configuracao do hibernate para OpenMRS");

        }
        catch (Throwable e) {
            System.err.println("Failed to create sessionFactory object." + e);
            throw new ExceptionInInitializerError(e);
        }
    }
}

