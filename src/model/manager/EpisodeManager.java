package model.manager;

import org.celllife.idart.database.hibernate.Episode;
import org.celllife.idart.database.hibernate.SyncEpisode;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    public static void updateEpisode(Session s, Episode episode) throws HibernateException {
        s.update(episode);
    }

    public static void saveEpisode(Episode episode) throws HibernateException {
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        sess.save(episode);

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
}
