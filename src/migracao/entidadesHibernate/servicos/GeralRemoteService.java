/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.entidadesHibernate.servicos;

import migracao.entidadesHibernate.ExportDao.GeralRemoteDao;

import java.util.Date;
import java.util.List;

/**
 *
 * @author colaco
 */
public class GeralRemoteService {
 private static GeralRemoteDao geralRemoteDao;

    public GeralRemoteService() {
        geralRemoteDao = new GeralRemoteDao();
    }

    public void persist(String entity) {
        geralRemoteDao.openCurrentSessionwithTransaction();
        geralRemoteDao.persist(entity);
        geralRemoteDao.closeCurrentSessionwithTransaction();
    }

    public void update(String entity) {
        geralRemoteDao.openCurrentSessionwithTransaction();
        geralRemoteDao.update(entity);
        geralRemoteDao.closeCurrentSessionwithTransaction();
    }

     public List findAllAbandonos(Date dataInicial, Date dataFinal, Integer locationId) {
        geralRemoteDao.openCurrentSession();
        List nidAbandonoList = geralRemoteDao.findAllAbandonos(dataInicial, dataFinal,locationId);
        geralRemoteDao.closeCurrentSession();
        return nidAbandonoList;
    }
    
    public void deleteAll() {
        geralRemoteDao.openCurrentSessionwithTransaction();
        geralRemoteDao.deleteAll();
        geralRemoteDao.closeCurrentSessionwithTransaction();
    }

    public GeralRemoteDao geralRemoteDao() {
        return geralRemoteDao;
    }
}
