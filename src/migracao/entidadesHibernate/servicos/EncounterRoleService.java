/*
 * Decompiled with CFR 0_114.
 * 
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidades.EncounterRole;
import migracao.entidadesHibernate.dao.EncounterRoleDao;

import java.util.List;

public class EncounterRoleService {
    private static EncounterRoleDao encounterRoleDao;

    public EncounterRoleService() {
        encounterRoleDao = new EncounterRoleDao();
    }

    public void persist(EncounterRole entity) {
        encounterRoleDao.openCurrentSessionwithTransaction();
        encounterRoleDao.persist(entity);
        encounterRoleDao.closeCurrentSessionwithTransaction();
    }

    public void update(EncounterRole entity) {
        encounterRoleDao.openCurrentSessionwithTransaction();
        encounterRoleDao.update(entity);
        encounterRoleDao.closeCurrentSessionwithTransaction();
    }

    public EncounterRole findById(String id) {
        encounterRoleDao.openCurrentSession();
        EncounterRole obs = encounterRoleDao.findById(id);
        encounterRoleDao.closeCurrentSession();
        return obs;
    }

    public void delete(String id) {
        encounterRoleDao.openCurrentSessionwithTransaction();
        EncounterRole obs = encounterRoleDao.findById(id);
        encounterRoleDao.delete(obs);
        encounterRoleDao.closeCurrentSessionwithTransaction();
    }

    public List<EncounterRole> findAll() {
        encounterRoleDao.openCurrentSession();
        List<EncounterRole> obss = encounterRoleDao.findAll();
        encounterRoleDao.closeCurrentSession();
        return obss;
    }

    public void deleteAll() {
        encounterRoleDao.openCurrentSessionwithTransaction();
        encounterRoleDao.deleteAll();
        encounterRoleDao.closeCurrentSessionwithTransaction();
    }

    public EncounterRoleDao encounterRoleDao() {
        return encounterRoleDao;
    }
}

