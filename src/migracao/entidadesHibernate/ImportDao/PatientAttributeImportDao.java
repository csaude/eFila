/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Query
 *  org.hibernate.Session
 *  org.hibernate.Transaction
 */
package migracao.entidadesHibernate.ImportDao;

import migracao.connection.hibernateConection;
import migracao.entidadesHibernate.Interfaces.PatientAttributeDaoInterface;
import org.celllife.idart.database.hibernate.PatientAttribute;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class PatientAttributeImportDao
implements PatientAttributeDaoInterface<PatientAttribute, String> {
    public Session currentSession;
    public Transaction currentTransaction;

    public Session openCurrentSession() {
        this.currentSession = hibernateConection.getInstanceLocal();
        return this.currentSession;
    }

    public Session openCurrentSessionwithTransaction() {
        this.currentSession = hibernateConection.getInstanceLocal();
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
    public void persist(PatientAttribute entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(PatientAttribute entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public PatientAttribute findById(String id) {
        PatientAttribute patientIdentifier = (PatientAttribute)this.getCurrentSession().get((Class) PatientAttribute.class, (Serializable)((Object)id));
        return patientIdentifier;
    }

    public PatientAttribute findByPatientId(String id) {
        PatientAttribute patientAttribute = (PatientAttribute)this.getCurrentSession().createQuery("from PatientAttribute p where p.patient = " + Integer.parseInt(id)).uniqueResult();
        return patientAttribute;
    }

    @Override
    public void delete(PatientAttribute entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<PatientAttribute> findAll() {
        List patientAttribute = this.getCurrentSession().createQuery("from PatientAttribute").list();
        return patientAttribute;
    }

    @Override
    public void deleteAll() {
        List<PatientAttribute> entityList = this.findAll();
        for (PatientAttribute entity : entityList) {
            this.delete(entity);
        }
    }
}

