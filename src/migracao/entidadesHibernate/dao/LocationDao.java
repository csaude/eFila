/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Query
 *  org.hibernate.Session
 *  org.hibernate.Transaction
 */
package migracao.entidadesHibernate.dao;

import migracao.connection.hibernateConectionRemote;
import migracao.entidades.Location;
import migracao.entidadesHibernate.Interfaces.LocationDaoInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class LocationDao
implements LocationDaoInterface<Location, String> {
    public Session currentSession;
    public Transaction currentTransaction;

    public Session openCurrentSession() {
        this.currentSession = hibernateConectionRemote.getInstanceRemote();
        return this.currentSession;
    }

    public Session openCurrentSessionwithTransaction() {
        this.currentSession = hibernateConectionRemote.getInstanceRemote();
        this.currentTransaction = this.currentSession.beginTransaction();
        return this.currentSession;
    }

    public void closeCurrentSession() {
        this.currentSession.close();
    }

    public void closeCurrentSessionwithTransaction() {
        this.currentTransaction.commit();
        this.currentSession.close();
    }

    public Session getCurrentSession() {
        return this.currentSession;
    }

    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    public Transaction getCurrentTransaction() {
        return this.currentTransaction;
    }

    public void setCurrentTransaction(Transaction currentTransaction) {
        this.currentTransaction = currentTransaction;
    }

    @Override
    public void persist(Location entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(Location entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public Location findById(String id) {
     //   Location concept = (Location)this.getCurrentSession().get((Class)Location.class, (Serializable)Integer.valueOf(Integer.parseInt(id)));
        Location concept = (Location) this.getCurrentSession().createQuery("from Location l where l.name = '"+id+"'").uniqueResult();
     // Location concept = (Location) this.getCurrentSession().createQuery("from Location l where l.name = (select gp.propertyValue FROM GlobalProperty gp where gp.property = 'defaultocation')").uniqueResult();
        return concept;
    }

    @Override
    public void delete(Location entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<Location> findAll() {
        List concepts = this.getCurrentSession().createQuery("from Location").list();
        return concepts;
    }

    @Override
    public void deleteAll() {
        List<Location> entityList = this.findAll();
        for (Location entity : entityList) {
            this.delete(entity);
        }
    }
}

