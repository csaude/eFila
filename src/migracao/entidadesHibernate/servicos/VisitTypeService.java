/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.VisitType;
import migracao.entidadesHibernate.dao.VisitTypeDao;

import java.util.List;

public class VisitTypeService {
    private static VisitTypeDao visitTypeDao;

    public VisitTypeService() {
        visitTypeDao = new VisitTypeDao();
    }

    public void persist(VisitType entity) {
        visitTypeDao.openCurrentSessionwithTransaction();
        visitTypeDao.persist(entity);
        visitTypeDao.closeCurrentSessionwithTransaction();
    }

    public void update(VisitType entity) {
        visitTypeDao.openCurrentSessionwithTransaction();
        visitTypeDao.update(entity);
        visitTypeDao.closeCurrentSessionwithTransaction();
    }

    public VisitType findById(String id) {
        visitTypeDao.openCurrentSession();
        VisitType obs = visitTypeDao.findById(id);
        visitTypeDao.closeCurrentSession();
        return obs;
    }

    public void delete(String id) {
        visitTypeDao.openCurrentSessionwithTransaction();
        VisitType obs = visitTypeDao.findById(id);
        visitTypeDao.delete(obs);
        visitTypeDao.closeCurrentSessionwithTransaction();
    }

    public List<VisitType> findAll() {
        visitTypeDao.openCurrentSession();
        List<VisitType> obss = visitTypeDao.findAll();
        visitTypeDao.closeCurrentSession();
        return obss;
    }

    public void deleteAll() {
        visitTypeDao.openCurrentSessionwithTransaction();
        visitTypeDao.deleteAll();
        visitTypeDao.closeCurrentSessionwithTransaction();
    }

    public VisitTypeDao visitTypeDao() {
        return visitTypeDao;
    }
}

