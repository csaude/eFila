/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.entidadesHibernate.dao;

import migracao.connection.hibernateConectionRemote;
import migracao.entidades.PatientIdentifier;
import migracao.entidades.PatientProgram;
import migracao.entidadesHibernate.Interfaces.PatientProgramDaoInterface;
import org.celllife.idart.commonobjects.JdbcProperties;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author ColacoVM
 */
public class PatientProgramDao implements PatientProgramDaoInterface<PatientProgram, String> {
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
    public void persist(PatientProgram entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(PatientProgram entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public PatientProgram findById(String id) {
        PatientProgram concept = (PatientProgram)this.getCurrentSession().get((Class)PatientProgram.class, (Serializable) Integer.valueOf(Integer.parseInt(id)));
        return concept;
    }

    @Override
    public void delete(PatientProgram entity) {
        this.getCurrentSession().delete((Object)entity);
    }
  //TODO
  //Review - colaco
    @Override
    public List<PatientProgram> findAll() {

        String uuidLocation = JdbcProperties.location;
        List patientPrograms = this.getCurrentSession().createQuery("from PatientProgram p "
                                                                    + "where p.programId = 2 and "
                                                                    + "p.idart is null and "
                                                                    + "p.dateEnrolled is not null and "
                                                                    + "p.dateCompleted is null").list();

        SQLQuery query = this.getCurrentSession().createSQLQuery(" " +
                        "select pg.* from patient p "+
                        "inner join patient_program pg on pg.patient_id = p.patient_id "+
                        "inner join location lc on lc.location_id = pg.location_id "+
                        "where pg.voided=0 and ( pg.program_id = 2 OR pg.program_id = 25 ) and lc.uuid = '"+uuidLocation+"' "+
                        "and pg.idart is null "+
                        "and pg.date_enrolled is not null "+
                        "and pg.date_completed is null " +
                        "and p.patient_id not in ( " +
                            "select pg.patient_id " +
                            "from patient p " +
                            "inner join patient_program pg on pg.patient_id = p.patient_id " +
                            "inner join patient_state ps on ps.patient_program_id = pg.patient_program_id " +
                            "where pg.voided=0 and ps.voided=0 " +
                            "and p.voided=0 " +
                            "and ( pg.program_id = 2 OR pg.program_id = 25 )" +
                            "and ps.state in (7,8,9,10) " +
                            "and ps.end_date is null " +
                            "and ps.start_date <= NOW() " +
                        " ) ");
        query.addEntity(PatientProgram.class);
        patientPrograms = query.list();

        return patientPrograms;
    }

    @Override
    public void deleteAll() {
        List<PatientProgram> entityList = this.findAll();
        for (PatientProgram entity : entityList) {
            this.delete(entity);
        }
    }
}

