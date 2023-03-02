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
import migracao.entidades.Users;
import migracao.entidadesHibernate.Interfaces.UserDaoInterface;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public class UsersDao
implements UserDaoInterface<Users, String> {
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
    public void persist(Users entity) {
        this.getCurrentSession().save((Object)entity);
    }

    @Override
    public void update(Users entity) {
        this.getCurrentSession().update((Object)entity);
    }

    @Override
    public Users findById(String id) {
        Users users = (Users)this.getCurrentSession().get((Class)Users.class, (Serializable) Integer.valueOf(Integer.parseInt(id)));
        return users;
    }

    @Override
    public void delete(Users entity) {
        this.getCurrentSession().delete((Object)entity);
    }

    @Override
    public List<Users> findAll() {
        List userses = this.getCurrentSession().createQuery("from Users").list();
        return userses;
    }

    @Override
    public void deleteAll() {
        List<Users> entityList = this.findAll();
        for (Users entity : entityList) {
            this.delete(entity);
        }
    }
}

