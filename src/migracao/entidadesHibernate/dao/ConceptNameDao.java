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
import migracao.entidades.ConceptName;
import migracao.entidadesHibernate.Interfaces.ConceptNameDaoInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class ConceptNameDao
        implements ConceptNameDaoInterface<ConceptName, String> {

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
    public void persist(ConceptName entity) {
        this.getCurrentSession().save((Object) entity);
    }

    @Override
    public void update(ConceptName entity) {
        this.getCurrentSession().update((Object) entity);
    }

    @Override
    public ConceptName findById(String id) {
        ConceptName conceptName = (ConceptName) this.getCurrentSession().get((Class) ConceptName.class, (Serializable) ((Object) id));
        return conceptName;
    }

    public ConceptName findByName(String regime) {
        ConceptName conceptName = null; // (ConceptName)this.getCurrentSession().createQuery("from ConceptName cn where cn.locale = 'pt' AND cn.name = '" + regime + "'").uniqueResult();
        List<ConceptName> conceptNameList = (List<ConceptName>) this.getCurrentSession().createQuery("from ConceptName cn where cn.name = '" + regime + "'").list();

        if (!conceptNameList.isEmpty()) {
            conceptName = conceptNameList.get(0);
        }

        return conceptName;
    }

    public ConceptName findByName2Line(String regime) {

        ConceptName conceptName = null;
        List<ConceptName> conceptNameList = (List<ConceptName>) this.getCurrentSession().createQuery("from ConceptName cn where cn.name like '" + regime + "%2%inha%'").list();

        if (!conceptNameList.isEmpty()) {
            conceptName = conceptNameList.get(0);
        }

        return conceptName;
    }

    public ConceptName findByName3Line(String regime) {

        ConceptName conceptName = null;
        List<ConceptName> conceptNameList = (List<ConceptName>) this.getCurrentSession().createQuery("from ConceptName cn where cn.name like '" + regime + "%3%inha%'").list();

        if (!conceptNameList.isEmpty()) {
            conceptName = conceptNameList.get(0);
        }

        return conceptName;
    }

    public ConceptName findByUuId(String uuid) {

        ConceptName conceptName = null;
        List<ConceptName> conceptNameList = (List<ConceptName>) this.getCurrentSession().createQuery("from ConceptName cn where cn.concept_id = (select c.concept_id from concept where c.uuid = '"+uuid+"')").list();

        if (!conceptNameList.isEmpty()) {
            conceptName = conceptNameList.get(0);
        }

        return conceptName;
    }

    @Override
    public void delete(ConceptName entity) {
        this.getCurrentSession().delete((Object) entity);
    }

    @Override
    public List<ConceptName> findAll() {
        List conceptNames = this.getCurrentSession().createQuery("from ConceptName").list();
        return conceptNames;
    }

    @Override
    public void deleteAll() {
        List<ConceptName> entityList = this.findAll();
        for (ConceptName entity : entityList) {
            this.delete(entity);
        }
    }
}
