package model.manager;

import model.manager.reports.LinhaTerapeuticaXLS;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpenmrsErrorLogManager {
	
	private static Logger log = Logger.getLogger(OpenmrsErrorLogManager.class);
	
	public static void saveOpenmrsRestLog(Session session, OpenmrsErrorLog errorLog) {
		if (errorLog!=null)
			session.save(errorLog);
	}


	public static OpenmrsErrorLog getErrorLog(Session sess, Prescription prescription) throws HibernateException {

		OpenmrsErrorLog error = (OpenmrsErrorLog) sess.createQuery(
				"select c from OpenmrsErrorLog as c where c.prescription = :prescription").setParameter("prescription",prescription)
				.uniqueResult();
		return error;
	}

	public static void removeErrorLog(Session session,  OpenmrsErrorLog error) {
		if(error != null)
			session.delete(error);
	}

	public static List<OpenmrsErrorLog> getOpenmrsErrorList(Session sess,String startDate, String endDate, StockCenter clinic) throws SQLException, ClassNotFoundException {

		String query = "select opl from OpenmrsErrorLog opl, Prescription pr where pr.id = opl.prescription and opl.datecreated >= '"+startDate+"' and opl.datecreated <='"+endDate+"' ";

		return (List<OpenmrsErrorLog>) sess.createQuery(query).list();
	}

}
