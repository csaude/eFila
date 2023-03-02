/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.Person;
import migracao.entidadesHibernate.dao.PersonDao;

import java.util.List;

public class PersonService {
    private static PersonDao personDao;

    public PersonService() {
        personDao = new PersonDao();
    }

    public void persist(Person entity) {
        personDao.openCurrentSessionwithTransaction();
        personDao.persist(entity);
        personDao.closeCurrentSessionwithTransaction();
    }

    public void update(Person entity) {
        personDao.openCurrentSessionwithTransaction();
        personDao.update(entity);
        personDao.closeCurrentSessionwithTransaction();
    }

    public Person findById(String id) {
        personDao.openCurrentSession();
        Person person = personDao.findById(id);
        personDao.closeCurrentSession();
        return person;
    }
    
    public String findByCellphone(int id) {
        personDao.openCurrentSession();
        String cellphone = personDao.findByCellphone(id);
        personDao.closeCurrentSession();
        return cellphone;
    }
    
    public void delete(String id) {
        personDao.openCurrentSessionwithTransaction();
        Person person = personDao.findById(id);
        personDao.delete(person);
        personDao.closeCurrentSessionwithTransaction();
    }

    public List<Person> findAll() {
        personDao.openCurrentSession();
        List<Person> persons = personDao.findAll();
        personDao.closeCurrentSession();
        return persons;
    }

    public void deleteAll() {
        personDao.openCurrentSessionwithTransaction();
        personDao.deleteAll();
        personDao.closeCurrentSessionwithTransaction();
    }

    public PersonDao personDao() {
        return personDao;
    }
}

