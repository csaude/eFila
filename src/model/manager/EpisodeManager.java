package model.manager;

import org.celllife.idart.database.hibernate.SyncEpisode;
import org.celllife.idart.database.hibernate.SyncOpenmrsDispense;
import org.celllife.idart.database.hibernate.SyncTempPatient;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import java.util.List;

public class EpisodeManager {

    public static List<SyncEpisode> getAllSyncEpisodesReadyToSave(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery("from SycEpisode sync where sync.syncstatus = 'P'").list();
        return result;
    }

    public static void saveSyncTempEpisode(Session s, SyncEpisode syncEpisode) throws HibernateException {
        s.saveOrUpdate(syncEpisode);
    }
}
