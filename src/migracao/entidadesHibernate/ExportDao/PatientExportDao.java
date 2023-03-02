/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Query
 *  org.hibernate.Session
 *  org.hibernate.Transaction
 */
package migracao.entidadesHibernate.ExportDao;

import migracao.connection.hibernateConectionRemote;
import migracao.entidades.Patient;
import migracao.entidades.PatientIdentifier;
import migracao.entidadesHibernate.Interfaces.PatientDaoInterface;
import migracao.swingreverse.TestePBE;
import org.apache.log4j.Logger;
import org.celllife.idart.database.hibernate.SyncTempDispense;
import org.celllife.idart.database.hibernate.SyncTempPatient;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PatientExportDao
implements PatientDaoInterface<Patient, String> {
    public Session currentSession;
    public Transaction currentTransaction;
    final static Logger log = Logger.getLogger(PatientExportDao.class);

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

    public Patient findByPatientId(String id) {
        PatientIdentifier patientIdentifier = (PatientIdentifier)this.getCurrentSession().createQuery("from PatientIdentifier p where p.identifier = '" + id + "'").uniqueResult();
     
        Patient patient = null;
        if (patientIdentifier != null) {
            patient = (Patient)this.getCurrentSession().createQuery("from Patient p where p.patientId = " + patientIdentifier.getPatientId().getPatientId()).uniqueResult();
           System.err.println("****************************************************");
            System.err.println(" Dispensa do NID " + id + " Enviado para o OpenMRS.");
           System.err.println("****************************************************");
        } else {
            System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.err.println("Este NID " + id + " nao foi encontrado no OpenMRS. Verifique o NID no OpenMRS ou Contacte o Administrador");
            System.err.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        }
       log.trace(patient);
        return patient;
    }

    @Override
    public Patient findById(String id) {
        Patient patient = (Patient)this.getCurrentSession().get((Class)Patient.class, (Serializable)((Object) Integer.parseInt(id)));
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
    public void deleteAll() {
        List<Patient> entityList = this.findAll();
        for (Patient entity : entityList) {
            this.delete(entity);
        }
    }

    @Override
    public List<SyncTempPatient> findAllImport() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncTempDispense> findAllExportedFromPatient(String T, org.celllife.idart.database.hibernate.Patient p, Date d) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

  
}

