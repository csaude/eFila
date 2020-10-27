package model.manager;

import org.celllife.idart.database.hibernate.*;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.transform.Transformers;

import java.util.List;

import static com.sun.javafx.fxml.expression.Expression.add;

public class EpisodeManager {

    public static List<SyncEpisode> getAllSyncEpisodesReadyToSave(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery("from SycEpisode sync where sync.syncStatus = 'S'").list();
        return result;
    }

    public static void saveSyncTempEpisode(Session s, SyncEpisode syncEpisode) throws HibernateException {
        s.saveOrUpdate(syncEpisode);
    }

    public static void saveEpisode(Session s, Episode episode) throws HibernateException {
        s.saveOrUpdate(episode);
    }

    public static List<SyncEpisode> getAllSyncTempEpiReadyToSend(Session sess) throws HibernateException {
        /*List result;
        result = sess.createQuery("startDate, stopDate, startReason, stopReason, startNotes, stopNotes, patientUUID, syncStatus, usuuid, clinicuuid  from SyncEpisode sync where sync.syncStatus = 'R' or sync.syncStatus is null").list();

        return result;*/

        Criteria cr = sess.createCriteria(SyncEpisode.class)
                .setProjection(Projections.projectionList()
                        .add(Projections.property("startDate"), "startDate")
                        .add(Projections.property("stopDate"), "stopDate")
                        .add(Projections.property("startReason"), "startReason")
                        .add(Projections.property("stopReason"), "stopReason")
                        .add(Projections.property("startNotes"), "startNotes")
                        .add(Projections.property("stopNotes"), "stopNotes")
                        .add(Projections.property("patientUUID"), "patientUUID")
                        .add(Projections.property("syncStatus"), "syncStatus")
                        .add(Projections.property("usuuid"), "usuuid")
                        .add(Projections.property("clinicuuid"), "clinicuuid"))
                .setResultTransformer(Transformers.aliasToBean(SyncEpisode.class));

        return cr.list();
    }
}
