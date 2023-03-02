/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.Form;
import migracao.entidadesHibernate.dao.FormDao;

import java.util.List;

public class FormService {
    private static FormDao formDao;

    public FormService() {
        formDao = new FormDao();
    }

    public void persist(Form entity) {
        formDao.openCurrentSessionwithTransaction();
        formDao.persist(entity);
        formDao.closeCurrentSessionwithTransaction();
    }

    public void update(Form entity) {
        formDao.openCurrentSessionwithTransaction();
        formDao.update(entity);
        formDao.closeCurrentSessionwithTransaction();
    }

    public Form findById(String id) {
        formDao.openCurrentSession();
        Form obs = formDao.findById(id);
        formDao.closeCurrentSession();
        return obs;
    }

    public void delete(String id) {
        formDao.openCurrentSessionwithTransaction();
        Form obs = formDao.findById(id);
        formDao.delete(obs);
        formDao.closeCurrentSessionwithTransaction();
    }

    public List<Form> findAll() {
        formDao.openCurrentSession();
        List<Form> obss = formDao.findAll();
        formDao.closeCurrentSession();
        return obss;
    }

    public void deleteAll() {
        formDao.openCurrentSessionwithTransaction();
        formDao.deleteAll();
        formDao.closeCurrentSessionwithTransaction();
    }

    public FormDao formDao() {
        return formDao;
    }
}

