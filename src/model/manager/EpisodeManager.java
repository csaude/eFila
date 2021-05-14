package model.manager;

import org.celllife.idart.database.hibernate.Episode;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.SyncEpisode;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.sql.Date;
import java.util.List;

public class EpisodeManager {

    public static List<SyncEpisode> getAllSyncEpisodesReadyToSave(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery("from SyncEpisode sync where sync.syncStatus = 'S'").list();
        return result;
    }

    public static void saveSyncTempEpisode(SyncEpisode syncEpisode) throws HibernateException {
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        sess.save(syncEpisode);

        assert tx != null;
        sess.flush();
        tx.commit();
        sess.close();
    }

    public static void updateSyncTempEpisode(SyncEpisode syncEpisode) throws HibernateException {
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        sess.update(syncEpisode);

        assert tx != null;
        sess.flush();
        tx.commit();
        sess.close();
    }

    public static void saveEpisode(Session s, Episode episode) throws HibernateException {
        s.saveOrUpdate(episode);
    }

    public static void deleteEpisode(Episode episode) throws HibernateException {
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        sess.delete(episode);

        assert tx != null;
        sess.flush();
        tx.commit();
        sess.close();
    }

    public static List<SyncEpisode> getAllSyncTempEpiReadyToSend(Session sess) throws HibernateException {

        List result;
        result = sess.createQuery("from SyncEpisode sync where sync.syncStatus = 'R'").list();
        return result;
    }

    public static List<SyncEpisode> getAllSyncTempEpiReadyToSendForPacient(Session sess, Patient patient) throws HibernateException {

        List result;
        result = sess.createQuery("from SyncEpisode sync where sync.syncStatus = 'R' AND sync.patientUUID = '"+patient.getUuidopenmrs()+"' AND sync.clinicuuid = '"+patient.getCurrentClinic().getUuid()+"'").list();
        return result;
    }
}
