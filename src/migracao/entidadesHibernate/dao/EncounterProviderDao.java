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
import migracao.entidades.Encounter;
import migracao.entidades.EncounterProvider;
import migracao.entidadesHibernate.Interfaces.EncounterDaoInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;

public class EncounterProviderDao
implements EncounterDaoInterface<EncounterProvider, String> {
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
    public void persist(EncounterProvider entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(EncounterProvider entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public EncounterProvider findById(String id) {
        EncounterProvider encounterProvider = (EncounterProvider)this.getCurrentSession().get((Class)Encounter.class, (Serializable)((Object)id));
        return encounterProvider;
    }

    public EncounterProvider findByEncounterAndPickupDate(Integer id, Date pickupDate) {
        
        EncounterProvider encounterProvider = null;
        List<EncounterProvider> listencounterProvider= this.getCurrentSession().createQuery("from EncounterProvider ep where ep.encounterId = " + id+" AND ep.dateCreated = '"+pickupDate+"'").list();
        
        if(!listencounterProvider.isEmpty()){
           encounterProvider = listencounterProvider.get(0);
        }
        return encounterProvider;
    }

    @Override
    public void delete(EncounterProvider entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<EncounterProvider> findAll() {
        List encounterProviders = this.getCurrentSession().createQuery("from EncounterProvider").list();
        return encounterProviders;
    }

    @Override
    public void deleteAll() {
        List<EncounterProvider> entityList = this.findAll();
        for (EncounterProvider entity : entityList) {
            this.delete(entity);
        }
    }
}

