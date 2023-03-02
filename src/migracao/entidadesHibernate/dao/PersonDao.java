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
import migracao.entidades.Person;
import migracao.entidadesHibernate.Interfaces.PersonDaoInterface;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class PersonDao
        implements PersonDaoInterface<Person, String> {

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
    public void persist(Person entity) {
        this.getCurrentSession().save((Object) entity);
    }

    @Override
    public void update(Person entity) {
        this.getCurrentSession().update((Object) entity);
    }

    @Override
    public Person findById(String id) {
        Person person = (Person) this.getCurrentSession().get((Class) Person.class, (Serializable) Integer.parseInt(id));
        //  Person person = (Person)this.getCurrentSession().createQuery("from Person p where p.personId = " + Integer.parseInt(id) ).uniqueResult();
        return person;
    }

    @Override
    public void delete(Person entity) {
        this.getCurrentSession().delete((Object) entity);
    }

    @Override
    public List<Person> findAll() {
        List persons = this.getCurrentSession().createQuery("from Person").list();
        return persons;
    }

    @Override
    public void deleteAll() {
        List<Person> entityList = this.findAll();
        for (Person entity : entityList) {
            this.delete(entity);
        }
    }

    public String findByCellphone(int identifier) {
        String phonenumber = "";
        List phonenumbers = null;
        SQLQuery query = this.getCurrentSession().createSQLQuery("select pat.value from person_attribute pat "
                + " where person_attribute_type_id = 9 and pat.person_id = " + identifier);

        phonenumbers = query.list();

        if (phonenumbers.size() != 0) {
            phonenumber = phonenumbers.get(0).toString();
        }

        return phonenumber;
    }
}
