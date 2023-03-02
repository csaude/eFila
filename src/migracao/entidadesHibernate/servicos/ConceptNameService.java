/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.ConceptName;
import migracao.entidadesHibernate.dao.ConceptNameDao;

import java.util.List;

public class ConceptNameService {
    private static ConceptNameDao conceptNameDao;

    public ConceptNameService() {
        conceptNameDao = new ConceptNameDao();
    }

    public void persist(ConceptName entity) {
        conceptNameDao.openCurrentSessionwithTransaction();
        conceptNameDao.persist(entity);
        conceptNameDao.closeCurrentSessionwithTransaction();
    }

    public void update(ConceptName entity) {
        conceptNameDao.openCurrentSessionwithTransaction();
        conceptNameDao.update(entity);
        conceptNameDao.closeCurrentSessionwithTransaction();
    }

    public ConceptName findById(String id) {
        conceptNameDao.openCurrentSession();
        ConceptName obs = conceptNameDao.findById(id);
        conceptNameDao.closeCurrentSession();
        return obs;
    }

    public ConceptName findByName(String regime) {
        conceptNameDao.openCurrentSession();
        ConceptName conceptName = conceptNameDao.findByName(regime);
        conceptNameDao.closeCurrentSession();
        return conceptName;
    }

    public ConceptName findByName2Line(String regime) {
        conceptNameDao.openCurrentSession();
        ConceptName conceptName = conceptNameDao.findByName2Line(regime);
        conceptNameDao.closeCurrentSession();
        return conceptName;
    }
    
    public ConceptName findByName3Line(String regime) {
        conceptNameDao.openCurrentSession();
        ConceptName conceptName = conceptNameDao.findByName3Line(regime);
        conceptNameDao.closeCurrentSession();
        return conceptName;
    }

    public ConceptName findByUuId(String uuid) {
        conceptNameDao.openCurrentSession();
        ConceptName conceptName = conceptNameDao.findByUuId(uuid);
        conceptNameDao.closeCurrentSession();
        return conceptName;
    }

    public void delete(String id) {
        conceptNameDao.openCurrentSessionwithTransaction();
        ConceptName conceptName = conceptNameDao.findById(id);
        conceptNameDao.delete(conceptName);
        conceptNameDao.closeCurrentSessionwithTransaction();
    }

    public List<ConceptName> findAll() {
        conceptNameDao.openCurrentSession();
        List<ConceptName> obss = conceptNameDao.findAll();
        conceptNameDao.closeCurrentSession();
        return obss;
    }

    public void deleteAll() {
        conceptNameDao.openCurrentSessionwithTransaction();
        conceptNameDao.deleteAll();
        conceptNameDao.closeCurrentSessionwithTransaction();
    }

    public ConceptNameDao conceptNameDao() {
        return conceptNameDao;
    }
}

