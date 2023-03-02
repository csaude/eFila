/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.Concept;
import migracao.entidades.Encounter;
import migracao.entidades.Obs;
import migracao.entidadesHibernate.dao.ObsDao;

import java.util.List;

public class ObsService {
    private static ObsDao obsDao;

    public ObsService() {
        obsDao = new ObsDao();
    }

    public void persist(Obs entity) {
        obsDao.openCurrentSessionwithTransaction();
        obsDao.persist(entity);
        obsDao.closeCurrentSessionwithTransaction();
    }

    public void update(Obs entity) {
        obsDao.openCurrentSessionwithTransaction();
        obsDao.update(entity);
        obsDao.closeCurrentSessionwithTransaction();
    }

    public Obs findById(String id) {
        obsDao.openCurrentSession();
        Obs obs = obsDao.findById(id);
        obsDao.closeCurrentSession();
        return obs;
    }

    public void delete(String id) {
        obsDao.openCurrentSessionwithTransaction();
        Obs obs = obsDao.findById(id);
        obsDao.delete(obs);
        obsDao.closeCurrentSessionwithTransaction();
    }

    public List<Obs> findAll() {
        obsDao.openCurrentSession();
        List<Obs> obss = obsDao.findAll();
        obsDao.closeCurrentSession();
        return obss;
    }

    public Obs findByUuId(String id) {
        obsDao.openCurrentSession();
        Obs obs = obsDao.findByUuId(id);
        obsDao.closeCurrentSession();
        return obs;
    }

    public Obs findByEncounterAndConcept(Encounter encounter, Concept concept) {
        obsDao.openCurrentSession();
        Obs obs = obsDao.findByEncounterAndConcept(encounter, concept);
        obsDao.closeCurrentSession();
        return obs;
    }

    public List<Obs> findAllByConcept(String concept) {
        obsDao.openCurrentSession();
        List<Obs> obss = obsDao.findAllByConcept(concept);
        obsDao.closeCurrentSession();
        return obss;
    }

    public void deleteAll() {
        obsDao.openCurrentSessionwithTransaction();
        obsDao.deleteAll();
        obsDao.closeCurrentSessionwithTransaction();
    }

    public ObsDao obsDao() {
        return obsDao;
    }
}

