package model.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.tmp.AdherenceRecord;
import org.celllife.idart.database.hibernate.tmp.DeletedItem;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

/**
 *
 */
public class TemporaryRecordsManager {

    private static Log log = LogFactory.getLog(TemporaryRecordsManager.class);

    //	/**
//	 * Method savePackageDrugInfosToDB.
//	 *
//	 * @param s Session
//	 * @param pdList List<PackageDrugInfo>
//	 * @return boolean
//	 * @throws HibernateException
//	 */
    public static boolean savePackageDrugInfosToDB(Session s, List<PackageDrugInfo> pdList) throws HibernateException {
        log.info("Saving package drug infos.");
        for (PackageDrugInfo pdi : pdList) {
            if (pdi.getId() == 0) {
                try {
                    s.save(pdi);
                } catch (Exception e) {
                    log.trace("Erro 1: " + e.getMessage());
                }
                //Para farmac Insere dispensas para US
                if (CentralizationProperties.centralization.equalsIgnoreCase("on") ) {
                    if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("F")){
                        savePackageDrugInfosFarmac(pdi, 'P');
                    } else if (pdi.getFlagIdentifier().equalsIgnoreCase("Sim")) {
                        savePackageDrugInfosFarmac(pdi, 'N');
                    }
                }
            }
        }
        return true;
    }

    public static boolean savePackageDrugInfosFarmac(PackageDrugInfo pdi, char syncStatus) throws HibernateException {
        log.info("Saving package drug infos FARMAC");

        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();
        SyncTempPatient patient = null;

        try {

            if (!pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getUuidopenmrs().isEmpty()
                    && pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getUuidopenmrs() != null)
                patient = AdministrationManager.getSyncTempPatienByUuid(sess, pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getUuidopenmrs());

            if(patient == null)
                patient = AdministrationManager.getSyncTempPatienByNID(sess, pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getPatientId());

            SyncTempDispense dispenseFarmac = AdministrationManager.getSyncTempDispenseById(sess, pdi.getId());

            if(dispenseFarmac == null)
             dispenseFarmac = new SyncTempDispense();


            dispenseFarmac.setId(pdi.getId());
            dispenseFarmac.setDate(pdi.getPackagedDrug().getParentPackage().getPrescription().getDate());
            dispenseFarmac.setClinicalstage(pdi.getPackagedDrug().getParentPackage().getPrescription().getClinicalStage());
            dispenseFarmac.setCurrent(pdi.getPackagedDrug().getParentPackage().getPrescription().getCurrent());
            dispenseFarmac.setReasonforupdate(pdi.getPackagedDrug().getParentPackage().getPrescription().getReasonForUpdate());
            dispenseFarmac.setDuration(pdi.getPackagedDrug().getParentPackage().getPrescription().getDuration());
            dispenseFarmac.setDoctor(pdi.getPackagedDrug().getParentPackage().getPrescription().getDoctor().getId());
            dispenseFarmac.setEnddate(pdi.getPackagedDrug().getParentPackage().getPrescription().getEndDate());
            dispenseFarmac.setRegimenome(pdi.getPackagedDrug().getParentPackage().getPrescription().getRegimeTerapeutico().getRegimeesquema());
            dispenseFarmac.setLinhanome(pdi.getPackagedDrug().getParentPackage().getPrescription().getLinha().getLinhanome());
            dispenseFarmac.setNotes(pdi.getPackagedDrug().getParentPackage().getPrescription().getNotes());
            dispenseFarmac.setMotivomudanca(pdi.getPackagedDrug().getParentPackage().getPrescription().getMotivoMudanca());
            dispenseFarmac.setWeight(pdi.getPackagedDrug().getParentPackage().getPrescription().getWeight());
            dispenseFarmac.setAf(pdi.getPackagedDrug().getParentPackage().getPrescription().getAf());
            dispenseFarmac.setCa(pdi.getPackagedDrug().getParentPackage().getPrescription().getCa());
            dispenseFarmac.setCcr(pdi.getPackagedDrug().getParentPackage().getPrescription().getCcr());
            dispenseFarmac.setPpe(pdi.getPackagedDrug().getParentPackage().getPrescription().getPpe());
            dispenseFarmac.setPtv(pdi.getPackagedDrug().getParentPackage().getPrescription().getPtv());
            dispenseFarmac.setTb(pdi.getPackagedDrug().getParentPackage().getPrescription().getTb());
            dispenseFarmac.setFr(pdi.getPackagedDrug().getParentPackage().getPrescription().getFr());
            dispenseFarmac.setGaac(pdi.getPackagedDrug().getParentPackage().getPrescription().getGaac());
            dispenseFarmac.setSaaj(pdi.getPackagedDrug().getParentPackage().getPrescription().getSaaj());
            dispenseFarmac.setTpc(pdi.getPackagedDrug().getParentPackage().getPrescription().getTpc());
            dispenseFarmac.setTpi(pdi.getPackagedDrug().getParentPackage().getPrescription().getTpi());
            dispenseFarmac.setPatient(pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getId());
            dispenseFarmac.setDispensatrimestral(pdi.getPackagedDrug().getParentPackage().getPrescription().getDispensaTrimestral());
            dispenseFarmac.setTipodt(pdi.getPackagedDrug().getParentPackage().getPrescription().getTipoDT());
            dispenseFarmac.setDrugtypes(pdi.getPackagedDrug().getParentPackage().getPrescription().getDrugTypes());
            dispenseFarmac.setDatainicionoutroservico(pdi.getPackagedDrug().getParentPackage().getPrescription().getDatainicionoutroservico());
            dispenseFarmac.setModified(pdi.getPackagedDrug().getParentPackage().getPrescription().getModified());
            dispenseFarmac.setPatientid(pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getPatientId());
            dispenseFarmac.setPatientfirstname(pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getFirstNames());
            dispenseFarmac.setPatientlastname(pdi.getPackagedDrug().getParentPackage().getPrescription().getPatient().getLastname());
            dispenseFarmac.setPickupdate(pdi.getDispenseDate());
            dispenseFarmac.setQtyinhand(pdi.getQtyInHand());
            dispenseFarmac.setQtyinlastbatch(pdi.getQtyInLastBatch());
            dispenseFarmac.setSummaryqtyinhand(pdi.getSummaryQtyInHand());
            dispenseFarmac.setTimesperday(pdi.getTimesPerDay());
            dispenseFarmac.setWeekssupply(pdi.getWeeksSupply());
            dispenseFarmac.setExpirydate(pdi.getExpiryDate());
            dispenseFarmac.setDateexpectedstring(pdi.getDateExpectedString());
            dispenseFarmac.setDrugname(pdi.getDrugName());
            dispenseFarmac.setDispensedate(pdi.getDispenseDate());
            dispenseFarmac.setMainclinic(patient.getMainclinic());
            dispenseFarmac.setMainclinicname(patient.getMainclinicname());
            dispenseFarmac.setMainclinicuuid(patient.getMainclinicuuid());
            dispenseFarmac.setSyncstatus(syncStatus);
            dispenseFarmac.setPrescriptionid(pdi.getPackagedDrug().getParentPackage().getPrescription().getPrescriptionId());
            dispenseFarmac.setTipods(pdi.getPackagedDrug().getParentPackage().getPrescription().getTipoDS());
            dispenseFarmac.setDispensasemestral(pdi.getPackagedDrug().getParentPackage().getPrescription().getDispensaSemestral());
            dispenseFarmac.setDurationsentence(pdi.getPackagedDrug().getParentPackage().getPrescription().getDurationSentence());
            dispenseFarmac.setDc(pdi.getPackagedDrug().getParentPackage().getPrescription().getDc());
            dispenseFarmac.setPrep(pdi.getPackagedDrug().getParentPackage().getPrescription().getPrep());
            dispenseFarmac.setCe(pdi.getPackagedDrug().getParentPackage().getPrescription().getCe());
            dispenseFarmac.setCpn(pdi.getPackagedDrug().getParentPackage().getPrescription().getCpn());
            dispenseFarmac.setPrescricaoespecial(pdi.getPackagedDrug().getParentPackage().getPrescription().getPrescricaoespecial());
            dispenseFarmac.setMotivocriacaoespecial(pdi.getPackagedDrug().getParentPackage().getPrescription().getMotivocriacaoespecial());
            dispenseFarmac.setUuidopenmrs(patient.getUuid());

            sess.save(dispenseFarmac);
            sess.flush();
            tx.commit();
            sess.close();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
                sess.close();
            }
            log.trace("Error Sync Farmac or FP dispense: " + e);
        }

        return true;

    }

    /**
     * Method saveAdherenceRecordsToDB.
     *
     * @param s      Session
     * @param adList List<AdherenceRecord>
     * @return boolean
     * @throws HibernateException
     */
    public static boolean saveAdherenceRecordsToDB(Session s,
                                                   List<AdherenceRecord> adList) throws HibernateException {
        log.info("Saving AdheranceRecords");
        for (int i = 0; i < adList.size(); i++) {
            s.save(adList.get(i));
        }

        return true;

    }

    /**
     * Method saveDeletedItemsToDB.
     *
     * @param s     Session
     * @param dList List<DeletedItem>
     * @throws HibernateException
     */
    public static void saveDeletedItemsToDB(Session s, List<DeletedItem> dList)
            throws HibernateException {
        log.info("Saving DeletedItems.");
        for (int i = 0; i < dList.size(); i++) {
            s.save(dList.get(i));
        }

    }

    /**
     * Method hasUnsubmittedRecords.
     *
     * @param s Session
     * @return boolean
     * @throws HibernateException
     */
    public static boolean hasUnsubmittedRecords(Session s)
            throws HibernateException {

        long numPdis = (Long) s.createQuery(
                "select count (pd.id) from PackageDrugInfo as pd where pd.sentToEkapa = false")
                .uniqueResult();

        long numAdh = (Long) s.createQuery(
                "select count (ad.id) from AdherenceRecord as ad")
                .uniqueResult();

        long numDel = (Long) s.createQuery(
                "select count (del.id) from DeletedItem as del").uniqueResult();

        return (numPdis > 0) || (numAdh > 0) || (numDel > 0);
    }

    /**
     * Method getUnsubmittedPackageDrugInfos.
     *
     * @param sess Session
     * @return List<PackageDrugInfo>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<PackageDrugInfo> getUnsubmittedPackageDrugInfos(
            Session sess) throws HibernateException {
        List<PackageDrugInfo> pdiList = sess
                .createQuery(
                        "from PackageDrugInfo as pd where pd.invalid = false "
                                + "and pd.sentToEkapa = false "
                                + "and pd.pickupDate is not null "
                                + "order by pd.id asc")
                .setMaxResults(10).list();

        return pdiList;
    }

    /**
     * Method getUnsubmittedAdherenceRecords.
     *
     * @param sess Session
     * @return List<AdherenceRecord>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<AdherenceRecord> getUnsubmittedAdherenceRecords(
            Session sess) throws HibernateException {
        List<AdherenceRecord> adhList = sess
                .createQuery(
                        "from AdherenceRecord as ad where order by ad.id asc")
                .setMaxResults(10).list();

        return adhList;
    }

    /**
     * Method getUnsubmittedDeletedItems.
     *
     * @param sess Session
     * @return List<DeletedItem>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<DeletedItem> getUnsubmittedDeletedItems(Session sess)
            throws HibernateException {
        List<DeletedItem> delList = sess
                .createQuery(
                        "from DeletedItem as del where order by del.id asc")
                .setMaxResults(10).list();

        return delList;
    }

    /**
     * Method deleteSubmittedPackageDrugInfos.
     *
     * @param s      Session
     * @param pdList List<PackageDrugInfo>
     * @throws HibernateException
     */
    public static void updateSubmittedPackageDrugInfos(Session s,
                                                       List<PackageDrugInfo> pdList) throws HibernateException {
        for (PackageDrugInfo pdi : pdList) {
            pdi.setSentToEkapa(true);
            s.saveOrUpdate(pdi);
        }
    }

    /**
     * Method deleteSubmittedAdherenceRecords.
     *
     * @param s      Session
     * @param adList List<AdherenceRecord>
     * @throws HibernateException
     */
    public static void deleteSubmittedAdherenceRecords(Session s,
                                                       List<AdherenceRecord> adList) throws HibernateException {

        String adDelete = "delete AdherenceRecord where id = :adId";

        for (int i = 0; i < adList.size(); i++) {

            s.createQuery(adDelete).setInteger("adId", adList.get(i).getId())
                    .executeUpdate();
        }

    }

    /**
     * Method deleteSubmittedDeletedItems.
     *
     * @param s       Session
     * @param delList List<DeletedItem>
     * @throws HibernateException
     */
    public static void deleteSubmittedDeletedItems(Session s,
                                                   List<DeletedItem> delList) throws HibernateException {

        String delDelete = "delete DeletedItem where id = :delId";

        for (int i = 0; i < delList.size(); i++) {

            s.createQuery(delDelete)
                    .setInteger("delId", delList.get(i).getId())
                    .executeUpdate();
        }

    }

    /**
     * Method getPDIforPackagedDrug.
     *
     * @param s  Session
     * @param pd PackagedDrugs
     * @return PackageDrugInfo
     * @throws HibernateException
     */
    public static PackageDrugInfo getPDIforPackagedDrug(Session s,
                                                        PackagedDrugs pd) throws HibernateException {
        PackageDrugInfo pdi = null;

        pdi = (PackageDrugInfo) s.createQuery(
                "from PackageDrugInfo as pdi where pdi.packagedDrug.id =:pdId")
                .setInteger("pdId", pd.getId()).setMaxResults(1).uniqueResult();

        return pdi;

    }

    /**
     * Method getPDIsForPackage.
     *
     * @param s Session
     * @param p Packages
     * @return List<PackageDrugInfo>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<PackageDrugInfo> getPDIsForPackage(Session s, Packages p)
            throws HibernateException {
        List<PackageDrugInfo> pdiList = s
                .createQuery(
                        "from PackageDrugInfo as pd where pd.packagedDrug.parentPackage.id =:packId")
                .setInteger("packId", p.getId()).list();

        return pdiList;
    }

    /**
     * Method getAdherenceRecordsForPillCount.
     *
     * @param s  Session
     * @param pc PillCount
     * @return AdherenceRecord
     * @throws HibernateException
     */
    public static AdherenceRecord getAdherenceRecordsForPillCount(Session s,
                                                                  PillCount pc) throws HibernateException {
        AdherenceRecord adh = null;

        adh = (AdherenceRecord) s.createQuery(
                "from AdherenceRecord as ad where ad.pillCountId =:pcId")
                .setInteger("pcId", pc.getId()).setMaxResults(1).uniqueResult();

        return adh;
    }

    public static void deletePackageDrugInfosForPackage(Session session, Packages pack) {
        session.createQuery("delete PackageDrugInfo where packageId = :id")
                .setString("id", pack.getPackageId())
                .executeUpdate();
    }

    /**
     * Method getOpenmrsUnsubmittedPackageDrugInfos.
     *
     * @param sess Session
     * @param pat  Patient
     * @return List<PackageDrugInfo>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<PackageDrugInfo> getOpenmrsUnsubmittedPackageDrugInfos(
            Session sess, Patient pat) throws HibernateException {
        String query = "from PackageDrugInfo as pd where "
                + " pd.patientId = '" + pat.getPatientId() + "'";

        List<PackageDrugInfo> pdiList = sess
                .createQuery(query).list();
        return pdiList;
    }

    /**
     * Method updateOpenmrsUnsubmittedPackageDrugInfos.
     *
     * @param s
     * @param pdList
     * @param pat    Patient
     * @return List<PackageDrugInfo>
     * @throws HibernateException
     */
    public static void updateOpenmrsUnsubmittedPackageDrugInfos(Session s,
                                                                List<PackageDrugInfo> pdList, Patient pat) throws HibernateException {
        for (PackageDrugInfo pdi : pdList) {
            log.info("Updating  PackageDrugInfo for patient: " + pdi.getPatientId());
            pdi.setPatientId(pat.getPatientId());
            pdi.setPatientLastName(pat.getLastname());
            pdi.setPatientFirstName(pat.getFirstNames());
            s.update(pdi);
        }
    }
}
