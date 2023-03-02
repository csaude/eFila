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
import migracao.entidades.PersonName;
import migracao.entidadesHibernate.Interfaces.PersonNameDaoInterface;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class PersonNameDao
implements PersonNameDaoInterface<PersonName, String> {
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
    public void persist(PersonName entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(PersonName entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public PersonName findById(String id) {
        PersonName personName = (PersonName)this.getCurrentSession().get((Class)PersonName.class, (Serializable) Integer.valueOf(Integer.parseInt(id)));
        return personName;
    }

    @Override
    public PersonName findByPersonId(String id) {
        PersonName personName = null;     
        
        if(id.contains("="))
        id = id.substring(29).replace(']', ' ').trim();
          
        SQLQuery query = getCurrentSession().createSQLQuery("select * from person_name p where p.preferred = 1 AND p.person_id = " + Integer.parseInt(id));
        query.addEntity(PersonName.class);
        List<PersonName> personNameList = query.list();

        if (personNameList == null) {
            personName = (PersonName) this.getCurrentSession().createQuery("from PersonName p where p.personId = " + Integer.parseInt(id)).list().get(0);
        }else
            personName = personNameList.get(0);
        
        return personName;
    }

    @Override
    public List<PersonName> findByAllGivenNameLike(String name) {
        List personNames = this.getCurrentSession().createQuery("from PersonName p where p.preferred = 1 AND p.given_name like '%"+name+"%'").list();
        return personNames;
    }
    
         @Override
    public List<PersonName> findByAllFamilyNameLike(String surname) {
        List personNames = this.getCurrentSession().createQuery("from PersonName p where p.preferred = 1 AND p.family_name like '%"+surname+"%'").list();
        return personNames;
    }
    
    @Override
    public void delete(PersonName entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<PersonName> findAll() {
        List personNames = this.getCurrentSession().createQuery("from PersonName").list();
        return personNames;
    }

    @Override
    public void deleteAll() {
        List<PersonName> entityList = this.findAll();
        for (PersonName entity : entityList) {
            this.delete(entity);
        }
    }
}

