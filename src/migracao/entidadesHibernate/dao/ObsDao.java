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
import migracao.entidades.Encounter;
import migracao.entidades.Obs;
import migracao.entidadesHibernate.Interfaces.ObsDaoInterface;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class ObsDao
implements ObsDaoInterface<Obs, String> {
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
    public void persist(Obs entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(Obs entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public Obs findById(String id) {
        Obs patientsObs = (Obs)this.getCurrentSession().get((Class)Obs.class, (Serializable)((Object)id));
        return patientsObs;
    }

    public Obs findByUuId(String id) {
        Obs obs = (Obs)this.getCurrentSession().createQuery("from Obs o where o.uuid = " + Integer.parseInt(id)).uniqueResult();
        return obs;
    }

    public Obs findByEncounterAndConcept(Encounter encounter, Concept concept) {
        Obs obs = (Obs)this.getCurrentSession().createQuery("from Obs o where o.encounterId = " + encounter.getEncounterId() + " AND o.conceptId = " + concept.getConceptId()).uniqueResult();
        return obs;
    }

    @Override
    public void delete(Obs entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<Obs> findAll() {
        List patientsObs = this.getCurrentSession().createQuery("from Obs").list();
        return patientsObs;
    }

    @Override
    public List<Obs> findAllByConcept(String id) {
        
        SQLQuery query = getCurrentSession().createSQLQuery("SELECT * FROM obs o WHERE "
                                                                   + "(o.concept_Id = " + Integer.parseInt(id) + " )" + " AND "
                                                                   + "(o.comments <> 'Imported' OR o.comments IS NULL) "
                                                                   + "GROUP by o.person_Id");
         query.addEntity(Obs.class);
        List<Obs> patientsObs = query.list();
//        List<Obs> patientsObs = getCurrentSession().createQuery("FROM Obs o WHERE "
//                                                                   + "(o.conceptId = " + Integer.parseInt(id) + " )" + " AND "
//                                                                   + "(o.comments <> 'Imported' OR o.comments IS NULL) "
//                                                                   + "GROUP by o.personId").list();
//       
        return patientsObs;
    }

    @Override
    public void deleteAll() {
        List<Obs> entityList = this.findAll();
        for (Obs entity : entityList) {
            this.delete(entity);
        }
    }
}

