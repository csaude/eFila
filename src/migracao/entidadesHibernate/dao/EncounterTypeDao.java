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
import migracao.entidades.EncounterType;
import migracao.entidadesHibernate.Interfaces.EncounterTypeDaoInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class EncounterTypeDao
implements EncounterTypeDaoInterface<EncounterType, String> {
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
    public void persist(EncounterType entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(EncounterType entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public EncounterType findById(String id) {
        EncounterType concept = (EncounterType)this.getCurrentSession().get((Class)EncounterType.class, (Serializable) Integer.valueOf(Integer.parseInt(id)));
        return concept;
    }

    @Override
    public void delete(EncounterType entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<EncounterType> findAll() {
        List concepts = this.getCurrentSession().createQuery("from EncounterType").list();
        return concepts;
    }

    @Override
    public void deleteAll() {
        List<EncounterType> entityList = this.findAll();
        for (EncounterType entity : entityList) {
            this.delete(entity);
        }
    }
}

