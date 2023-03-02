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
import migracao.entidadesHibernate.Interfaces.EncounterDaoInterface;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

public class EncounterDao
        implements EncounterDaoInterface<Encounter, String> {

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
    public void persist(Encounter entity) {
        this.getCurrentSession().save((Object) entity);
    }

    @Override
    public void update(Encounter entity) {
        this.getCurrentSession().update((Object) entity);
    }

    @Override
    public Encounter findById(String id) {
        Encounter encounter = (Encounter) this.getCurrentSession().get((Class) Encounter.class, (Serializable) ((Object) id));
        return encounter;
    }

    public Encounter findByVisitAndPickupDate(Integer id, Date pickupDate) {
        Encounter encounter = null;

        GregorianCalendar calDispense = new GregorianCalendar();
        calDispense.setTime(pickupDate);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        List<Encounter> listencounter = null;

        SQLQuery query = this.getCurrentSession().createSQLQuery("select * from encounter e "
                + " where e.visit_id = " + id
                + " and date_format(e.encounter_datetime, '%Y-%m-%d') = '" + format.format(pickupDate.getTime()) + "'");

        query.addEntity(Encounter.class);
        try {
            listencounter = query.list();
        } catch (HibernateException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }

        if (!listencounter.isEmpty()) {
            encounter = listencounter.get(0);
        }
        return encounter;
    }

    @Override
    public void delete(Encounter entity) {
        this.getCurrentSession().delete((Object) entity);
    }

    @Override
    public List<Encounter> findAll() {
        List encounters = this.getCurrentSession().createQuery("from Encounter").list();
        return encounters;
    }

    @Override
    public void deleteAll() {
        List<Encounter> entityList = this.findAll();
        for (Encounter entity : entityList) {
            this.delete(entity);
        }
    }
}
