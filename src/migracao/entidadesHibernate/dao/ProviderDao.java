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
import migracao.entidades.Provider;
import migracao.entidadesHibernate.Interfaces.ProviderDaoInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class ProviderDao
implements ProviderDaoInterface<Provider, String> {
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
    public void persist(Provider entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(Provider entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public Provider findById(String id) {
        Provider provider = (Provider)this.getCurrentSession().get((Class)Provider.class, (Serializable) Integer.valueOf(Integer.parseInt(id)));
        return provider;
    }

    public Provider findByPersonId(String id) {
        Provider provider = (Provider)this.getCurrentSession().createQuery("from Provider p where p.person = " + Integer.parseInt(id)).uniqueResult();
        return provider;
    }

    @Override
    public void delete(Provider entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<Provider> findAll() {
        List providers = this.getCurrentSession().createQuery("from Provider").list();
        return providers;
    }

    @Override
    public void deleteAll() {
        List<Provider> entityList = this.findAll();
        for (Provider entity : entityList) {
            this.delete(entity);
        }
    }
}

