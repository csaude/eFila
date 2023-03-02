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
import migracao.entidades.Patient;
import migracao.entidadesHibernate.Interfaces.PatientDaoInterface;
import org.celllife.idart.database.hibernate.SyncTempDispense;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PatientDao
implements PatientDaoInterface<Patient, String> {
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
    public void persist(Patient entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(Patient entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public Patient findById(String id) {
        Patient patient = (Patient)this.getCurrentSession().get((Class)Patient.class, (Serializable)((Object)id));
        return patient;
    }

    @Override
    public void delete(Patient entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<Patient> findAll() {
        List patients = this.getCurrentSession().createQuery("from Patient").list();
        return patients;
    }

      @Override
    public List findAllImport() {
         SQLQuery query = getCurrentSession().createSQLQuery("select * from sync_temp_patients ");
       
        List syncPatientImport = query.list();

        return syncPatientImport;
    }
    
    @Override
    public void deleteAll() {
        List<Patient> entityList = this.findAll();
        for (Patient entity : entityList) {
            this.delete(entity);
        }
    }

    @Override
    public List<SyncTempDispense> findAllExportedFromPatient(String T, org.celllife.idart.database.hibernate.Patient p, Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

