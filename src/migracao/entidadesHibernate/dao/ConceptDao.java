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
import migracao.entidades.Concept;
import migracao.entidadesHibernate.Interfaces.ConceptDaoInterace;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class ConceptDao
implements ConceptDaoInterace<Concept, String> {
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
    public void persist(Concept entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(Concept entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public Concept findById(String id) {
        Concept concept = (Concept)this.getCurrentSession().get((Class)Concept.class, (Serializable) Integer.valueOf(Integer.parseInt(id)));
        return concept;
    }

    @Override
    public void delete(Concept entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<Concept> findAll() {
        List concepts = this.getCurrentSession().createQuery("from Concept c where c.conceptId in (5096,1711,1715,1088) ORDER BY c.conceptId desc").list();
        return concepts;
    }

    @Override
    public void deleteAll() {
        List<Concept> entityList = this.findAll();
        for (Concept entity : entityList) {
            this.delete(entity);
        }
    }
}

