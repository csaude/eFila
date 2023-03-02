/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.Concept;
import migracao.entidadesHibernate.dao.ConceptDao;

import java.util.List;

public class ConceptService {
    private static ConceptDao conceptDao;

    public ConceptService() {
        conceptDao = new ConceptDao();
    }

    public void persist(Concept entity) {
        conceptDao.openCurrentSessionwithTransaction();
        conceptDao.persist(entity);
        conceptDao.closeCurrentSessionwithTransaction();
    }

    public void update(Concept entity) {
        conceptDao.openCurrentSessionwithTransaction();
        conceptDao.update(entity);
        conceptDao.closeCurrentSessionwithTransaction();
    }

    public Concept findById(String id) {
        conceptDao.openCurrentSession();
        Concept obs = conceptDao.findById(id);
        conceptDao.closeCurrentSession();
        return obs;
    }

    public void delete(String id) {
        conceptDao.openCurrentSessionwithTransaction();
        Concept obs = conceptDao.findById(id);
        conceptDao.delete(obs);
        conceptDao.closeCurrentSessionwithTransaction();
    }

    public List<Concept> findAll() {
        conceptDao.openCurrentSession();
        List<Concept> obss = conceptDao.findAll();
        conceptDao.closeCurrentSession();
        return obss;
    }

    public void deleteAll() {
        conceptDao.openCurrentSessionwithTransaction();
        conceptDao.deleteAll();
        conceptDao.closeCurrentSessionwithTransaction();
    }

    public ConceptDao conceptDao() {
        return conceptDao;
    }
}

