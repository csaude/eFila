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

import migracao.entidadesHibernate.ExportDao.PatientExportDao;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.iDartProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

public class hibernateConection {
    private static SessionFactory sessionFactoryLocal;
     private static final ThreadLocal<Session> threadLocal;
    final static Logger log = Logger.getLogger(hibernateConection.class);

    public static org.hibernate.Session getInstanceLocal() {
        org.hibernate.Session session = threadLocal.get();
        session = sessionFactoryLocal.openSession();
        threadLocal.set(session);
        return session;
    }
    static {
        threadLocal = new ThreadLocal();
        try {

            Configuration config = new AnnotationConfiguration().configure("migracao/connection/hibernate.cfg.xml");

            config.setProperty("hibernate.connection.driver_class", iDartProperties.hibernateDriver);
            config.setProperty("hibernate.connection.url",iDartProperties.hibernateConnectionUrl);
            config.setProperty("hibernate.connection.username",iDartProperties.hibernateUsername);
            config.setProperty("hibernate.connection.password", iDartProperties.hibernatePassword);
            config.setProperty("hibernate.dialect", iDartProperties.hibernateDialect);
            config.setProperty("hibernate.current_session_context_class", "thread");
            config.setProperty("hibernate.show_sql", String.valueOf(false));
            config.setProperty("hibernate.jdbc.fetch_size", String.valueOf(1));

            sessionFactoryLocal = config.buildSessionFactory();

           log.trace("Reiniciando a configuracao do hibernate para IDART");
        }
        catch (Throwable e) {
            System.err.println("Failed to create sessionFactory object." + e);
            throw new ExceptionInInitializerError(e);
        }
       
    }
     
}

