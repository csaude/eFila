/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.entidadesHibernate.ExportDao;

import migracao.connection.hibernateConectionRemote;
import migracao.entidadesHibernate.Interfaces.GlobalPropertyDaoInterface;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author colaco
 */
public class GeralRemoteDao implements GlobalPropertyDaoInterface<String, String> {
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
    
    public List findAllAbandonos(Date dataInicial, Date dataFim, Integer locationId) {
                    
        GregorianCalendar calInicial = new GregorianCalendar();
		calInicial.setTime(dataInicial);
		calInicial.add(Calendar.DATE,0);
                
        GregorianCalendar calFinal = new GregorianCalendar();
		calFinal.setTime(dataFim);
		calFinal.add(Calendar.DATE,0);
                
	SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
     //  log.trace(locationId+"Location ID");
        String queryOpenmrs = "select * from " +
                            "(select distinct identifier as NID " +
                            "from " +
                            "	(	Select 	p.patient_id,max(encounter_datetime) encounter_datetime " +
                            "		from 	patient p  " +
                            "				inner join encounter e on e.patient_id=p.patient_id " +
                            "		where 	p.voided=0 and e.voided=0 and e.encounter_type=18 and  " +
                            "		e.encounter_datetime<='" +format1.format(calFinal.getTime())+"'"+
                            "		group by p.patient_id " +
                            "	) max_frida  " +
                            "	inner join obs o on o.person_id=max_frida.patient_id " +
                            "	left join " +
                            "	(   select pid1.* " +
                            "		from patient_identifier pid1 " +
                            "		inner join " +
                            "		( " +
                            "			select patient_id,min(patient_identifier_id) id " +
                            "			from patient_identifier " +
                            "			where voided=0 " +
                            "			group by patient_id " +
                            "		) pid2 " +
                            "		where pid1.patient_id=pid2.patient_id and pid1.patient_identifier_id=pid2.id " +
                            "	) pid on pid.patient_id=max_frida.patient_id " +
                            "where max_frida.encounter_datetime=o.obs_datetime  " +
                            "and o.voided=0 and o.concept_id=5096 and " +
                            "max_frida.patient_id not in  " +
                            "( " +
                            "	select 	pg.patient_id " +
                            "	from 	patient p  " +
                            "			inner join patient_program pg on p.patient_id=pg.patient_id " +
                            "			inner join patient_state ps on pg.patient_program_id=ps.patient_program_id " +
                            "	where 	pg.voided=0 and ps.voided=0 and p.voided=0 and  " +
                            "			pg.program_id=2 and ps.state in (7,8,10) and ps.end_date is null and  " +
                            "			ps.start_date<='"+format1.format(calFinal.getTime())+"') " +
                            "and datediff('"+format1.format(calFinal.getTime())+"',o.value_datetime)>60 " +
                            ") abandonos " +
                            "group by 1";
        
        SQLQuery sqlquery = getCurrentSession().createSQLQuery(queryOpenmrs);
    //    query.addEntity(List.class);
        List lostfollowupList = sqlquery.list();
        
   //    log.trace(lostfollowupList);
        return lostfollowupList;
    }

    @Override
    public void persist(String var1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update(String var1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void delete(String var1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String findById(String var1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> findAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  
}
