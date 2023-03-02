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
import migracao.entidadesHibernate.Interfaces.PatientDaoInterface;
import org.apache.log4j.Logger;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.SyncTempDispense;
import org.celllife.idart.database.hibernate.SyncTempPatient;
import org.celllife.idart.rest.utils.RestFarmac;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class PatientImportDao
        implements PatientDaoInterface<Patient, String> {

    public Session currentSession;
    public Transaction currentTransaction;

    public final static Logger log = Logger.getLogger(PatientImportDao.class);

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
    public void persist(Patient entity) {
        this.getCurrentSession().save((Object) entity);
    }

    @Override
    public void update(Patient entity) {
        this.getCurrentSession().update((Object) entity);
    }

    public Patient findByPatientId(String id) {
        Patient patient = (Patient) this.getCurrentSession().createQuery("from Patient p where p.patientId = '" + id + "'").uniqueResult();
//       log.trace(patient);
        return patient;
    }

    public Patient findById(Integer id) {
        Patient patient = (Patient) this.getCurrentSession().get((Class) Patient.class, (Serializable) ((Object) id));
        return patient;
    }

    @Override
    public void delete(Patient entity) {
        this.getCurrentSession().delete((Object) entity);
    }

    @Override
    public List<Patient> findAll() {
        List patients = this.getCurrentSession().createQuery("from Patient").list();
        return patients;
    }

    @Override
    public List<SyncTempPatient> findAllImport() {
//        SQLQuery query = this.getCurrentSession().createSQLQuery("select * from sync_temp_patients ");
//        query.addEntity(SyncTempPatient.class);
//       log.trace(query.list());
//        List<SyncTempPatient> syncPatientImport = query.list();
//
        List syncPatientImport = null;
        try {
            syncPatientImport = this.getCurrentSession().createQuery("from SyncTempPatient").list();
        } catch (Exception e) {
            e.printStackTrace();
           log.trace(e.getMessage());
        }

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
    public Patient findById(String var1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncTempDispense> findAllExportedFromPatient(String clinicName, Patient patient, Date datadispensa) {
        List syncDispenseExport = null;
        try {
            syncDispenseExport = this.getCurrentSession().createQuery("from SyncTempDispense where syncTempDispenseid = '" + clinicName + "'"
                    + " AND patientid = '" + patient.getPatientId() + "' AND dispensedate = '"+datadispensa+"'").list();
        } catch (Exception e) {
            e.printStackTrace();
           log.trace(e.getMessage());
        }

        return syncDispenseExport;
    }

    public List findAllPatientFromClinic(String clinicName) {
        List patients = null;

        SQLQuery query = this.getCurrentSession().createSQLQuery("select distinct patientid,dispensedate from sync_temp_dispense "
                + " where sync_temp_dispenseid = '" + clinicName + "' order by dispensedate desc");

    //    query.addEntity(Patient.class);
        try {
            patients = query.list();
        } catch (HibernateException e) {
            e.printStackTrace();
           log.trace(e.getMessage());
        }

        return patients;
    }

    /*
    Modified by  Colaco 20/2018 
    Vamos usar um identificador unico Uuid dos pacientes
    para evitar a busca do paciente pelo Nid e apelido
     */
    public Patient findByPatientUuid(String uuid) {

        Patient patient = (Patient) this.getCurrentSession().createQuery("from Patient pa where pa.uuid = '" + uuid + "'").uniqueResult();

        return patient;
    }

}
