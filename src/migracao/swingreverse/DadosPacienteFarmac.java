/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import model.manager.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.JdbcProperties;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.packaging.NewPatientPackaging;
import org.celllife.idart.rest.utils.RestClient;
import org.celllife.idart.rest.utils.RestFarmac;
import org.celllife.idart.rest.utils.RestUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.celllife.idart.gui.packaging.NewPatientPackaging.saveErroLog;
import static org.celllife.idart.gui.packaging.NewPatientPackaging.saveOpenmrsPatientFila;
import static org.celllife.idart.rest.ApiAuthRest.getServerStatus;

/**
 * @author colaco
 */
public class DadosPacienteFarmac {

    public final static Logger log = Logger.getLogger(DadosPacienteFarmac.class);

    public static Patient InserePaciente(Session sess, SyncTempPatient patientSync) {

        Patient patient = null;
        PatientIdentifier patientIdentifier = null;
        PatientAttribute patientAttribute = null;
        Set<PatientIdentifier> oldIdentifiers = new HashSet<>();

        Clinic clinic = null;
        Session session = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) {
            clinic = AdministrationManager.getClinicbyUuid(sess, patientSync.getMainclinicuuid());

            if (clinic == null) {
                try {
                    clinic = new Clinic();
                    clinic.setUuid(patientSync.getMainclinicuuid());
                    clinic.setClinicName(patientSync.getMainclinicname());
                    clinic.setMainClinic(false);
                    clinic.setDistrict("");
                    clinic.setProvince("");
                    clinic.setSubDistrict("");
                    clinic.setNotes("");
                    clinic.setTelephone("");
                    clinic.setFacilityType("Unidade Sanitária");
                    clinic.setCode(patientSync.getPatientid().substring(0, 9));
                    AdministrationManager.saveClinic(sess, clinic);
                    tx.commit();
                    session.flush();
                    session.close();
                } catch (Exception e) {
                    if (tx != null) {
                        tx.rollback();
                        session.close();
                    }
                   log.trace("Error create or update clinic for patient :" +patientSync.getPatientid() +" clinic "+patientSync.getMainclinicname()+" error- "+ e);
                }
            }
        } else
            clinic = AdministrationManager.getMainClinic(sess);

        IdentifierType identifierType = AdministrationManager.getNationalIdentifierType(sess);
        AttributeType attributeType = PatientManager.getAttributeTypeObject(sess, "ARV Start Date");

        if (patientSync.getUuid() != null)
            patient = PatientManager.getPatientfromUuid(sess, patientSync.getUuid());
        else
            patient = PatientManager.getPatient(sess, patientSync.getPatientid());

        if (patient == null) {
            patient = new Patient();
            patientIdentifier = new PatientIdentifier();
            patientAttribute = new PatientAttribute();
        } else {
            patientIdentifier = patient.getIdentifier(identifierType);
            if (patientIdentifier == null)
                patientIdentifier = new PatientIdentifier();
            patientAttribute = patient.getAttributeByName(attributeType.getName());
            oldIdentifiers = patient.getPatientIdentifiers();
        }

        patientIdentifier.setPatient(patient);
        patientIdentifier.setType(identifierType);
        patientIdentifier.setValue(patientSync.getPatientid());

        oldIdentifiers.add(patientIdentifier);

        patientAttribute.setPatient(patient);
        patientAttribute.setValue(patientSync.getDatainiciotarv());
        patientAttribute.setType(attributeType);

        patient.setFirstNames(patientSync.getFirstnames());
        patient.setAccountStatus(Boolean.FALSE);
        patient.setAddress1(patientSync.getAddress1());
        patient.setAddress2(patientSync.getAddress2());
        patient.setAddress3(patientSync.getAddress3());
        patient.setCellphone(patientSync.getCellphone());
        patient.setDateOfBirth(patientSync.getDateofbirth());
        patient.setClinic(clinic);
        patient.setNextOfKinName(patientSync.getNextofkinname());
        patient.setNextOfKinPhone(patientSync.getNextofkinphone());
        patient.setHomePhone(patientSync.getHomephone());
        patient.setLastname(patientSync.getLastname());
        patient.setModified(patientSync.getModified());
        patient.setPatientId(patientSync.getPatientid());
        patient.setProvince(patientSync.getProvince());
        patient.setSex(patientSync.getSex());
        patient.setWorkPhone(null);
        patient.setRace(patientSync.getRace());
        patient.setUuidopenmrs(patientSync.getUuid());

        patient.setPatientIdentifiers(oldIdentifiers);
        patient.setPatientAttribute(patientAttribute);

        try {
            PatientManager.savePatient(sess, patient);
        } catch (Exception e) {
            log.trace("Erro ao gravar informacao do Paciente [" + patient.getFirstNames() + " " + patient.getLastname() + " com NID: " + patient.getPatientId() + "]");
        }

        return patient;

    }

    public static Prescription getPatientPrescritionFarmac(SyncTempDispense syncTempDispense) {

        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        Prescription prescription = null;
        Patient patient = null;
        try {

            if (syncTempDispense.getUuidopenmrs() != null)
                patient = PatientManager.getPatientfromUuid(sess, syncTempDispense.getUuidopenmrs());
            else
                patient = PatientManager.getPatient(sess, syncTempDispense.getPatientid());

            prescription = PackageManager.getPrescriptionFromPatient(sess, patient, syncTempDispense.getDate());

            if (prescription == null) {
                prescription = new Prescription();

                SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
                Doctor doctorProvider = PrescriptionManager.getProvider(sess);
                LinhaT linhat = AdministrationManager.getLinha(sess, syncTempDispense.getLinhanome());
                RegimeTerapeutico regimeTerapeutico = AdministrationManager.getRegimeTerapeutico(sess, syncTempDispense.getRegimenome());

                if (!regimeTerapeutico.getRegimeesquema().equalsIgnoreCase(syncTempDispense.getRegimenome()))
                    regimeTerapeutico = AdministrationManager.getRegimeTerapeuticoRest(sess, syncTempDispense.getRegimenome());

                prescription.setClinicalStage(0);
                prescription.setCurrent('T');
                prescription.setDate(syncTempDispense.getDate());
                prescription.setEndDate(syncTempDispense.getEnddate());
                prescription.setDoctor(doctorProvider);
                prescription.setDuration(syncTempDispense.getDuration());
                prescription.setModified(syncTempDispense.getModified());
                prescription.setPatient(patient);
                prescription.setPrescriptionId(patient.getPatientId() + "-" + df.format(syncTempDispense.getDate()) + " - Farmac ");
                prescription.setReasonForUpdate(syncTempDispense.getReasonforupdate());
                prescription.setNotes("FARMAC: " + syncTempDispense.getNotes());
                prescription.setRegimeTerapeutico(regimeTerapeutico);
                prescription.setLinha(linhat);
                prescription.setDatainicionoutroservico(syncTempDispense.getDatainicionoutroservico());
                prescription.setMotivoMudanca(syncTempDispense.getMotivomudanca());
                prescription.setPpe(syncTempDispense.getPpe());
                prescription.setPtv(syncTempDispense.getPtv());
                prescription.setTb(syncTempDispense.getTb());
                prescription.setGaac(syncTempDispense.getGaac());
                prescription.setAf(syncTempDispense.getAf());
                prescription.setFr(syncTempDispense.getFr());
                prescription.setCa(syncTempDispense.getCa());
                prescription.setSaaj(syncTempDispense.getSaaj());
                prescription.setCcr(syncTempDispense.getCcr());
                prescription.setDc(syncTempDispense.getDc());
                prescription.setPrep(syncTempDispense.getPrep());
                prescription.setCe(syncTempDispense.getCe());
                prescription.setCpn(syncTempDispense.getCpn());
                prescription.setPrescricaoespecial(syncTempDispense.getPrescricaoespecial());
                prescription.setMotivocriacaoespecial(syncTempDispense.getMotivocriacaoespecial());
                prescription.setTpc(syncTempDispense.getTpc());
                prescription.setTpi(syncTempDispense.getTpi());
                prescription.setDrugTypes(syncTempDispense.getDrugtypes());
                prescription.setTipoDS(syncTempDispense.getTipods());
                prescription.setDispensaSemestral(syncTempDispense.getDispensasemestral());
                prescription.setTipoDT(syncTempDispense.getTipodt());
                prescription.setDispensaTrimestral(syncTempDispense.getDispensatrimestral());
                prescription.setDurationSentence(syncTempDispense.getDurationsentence());

                List<PrescribedDrugs> prescribedDrugsList = new ArrayList<PrescribedDrugs>();

                // Save the Prescription Drugs

                Drug drug = DrugManager.getDrug(sess, syncTempDispense.getDrugname());

                if (drug == null)
                    drug = DrugManager.getDrugFromString(sess, syncTempDispense.getDrugname().replace("[", "").substring(0, 10));

                if (drug == null)
                    drug = DrugManager.getDrugFromString(sess, syncTempDispense.getDrugname().replace("[", "").substring(0, 10).replace("/", "+"));

                if (drug != null) {
                    PrescribedDrugs newPD = new PrescribedDrugs();

                    if (drug.getPackSize() > 30) {
                        newPD.setAmtPerTime(2);
                    } else {
                        newPD.setAmtPerTime(1);
                    }
                    newPD.setDrug(drug);
                    newPD.setModified(syncTempDispense.getModified());
                    newPD.setPrescription(prescription);
                    newPD.setTimesPerDay(syncTempDispense.getTimesperday());
                    prescribedDrugsList.add(newPD);

                    prescription.setPrescribedDrugs(prescribedDrugsList);

                    PackageManager.saveNewPrescription(sess, prescription, true);
                    tx.commit();
                    sess.flush();
                } else
                   log.trace("O medicamento prescrito para o paciente " + syncTempDispense.getPatientid() + " nao foi encontrado: " + syncTempDispense.getDrugname());
                sess.close();
            }
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
                sess.close();
            }
           log.trace("Erro ao gravar O medicamento prescrito para o paciente " + syncTempDispense.getPatientid() + " nao foi encontrado: " + syncTempDispense.getDrugname() + " Error :" + e);

        }
        return prescription;
    }

    public static void saveDispenseFarmacQty0(Prescription prescription, SyncTempDispense syncTempDispense) {

        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        Clinic clinic = null;

        if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("P"))
            clinic = AdministrationManager.getClinicbyUuid(sess, syncTempDispense.getMainclinicuuid());

        if (clinic == null)
            clinic = AdministrationManager.getMainClinic(sess);

        User user = AdministrationManager.getUserByName(sess, "admin");

        try {
            // Prescriotion Duration
            tx = sess.beginTransaction();
            java.util.List<PackageDrugInfo> allPackagedDrugsListTemp = new ArrayList<PackageDrugInfo>();

            Drug drug = DrugManager.getDrug(sess, syncTempDispense.getDrugname());
            List<Stock> stockList = null;
            Stock stock = null;

            if (drug == null)
                drug = DrugManager.getDrugFromString(sess, syncTempDispense.getDrugname().replace("[", "").substring(0, 10));

            if (drug == null)
                drug = DrugManager.getDrugFromString(sess, syncTempDispense.getDrugname().replace("[", "").substring(0, 10).replace("/", "+"));

            if (drug != null) {
                stockList = StockManager.getAllStockForDrug(sess, drug);
            }

            if (stockList == null) {
                stock = StockManager.getAllCurrentStock(sess).get(0);
            } else {
                if (stockList.size() == 0) {
                    stock = StockManager.getAllCurrentStock(sess).get(0);
                } else {
                    stock = stockList.get(0);
                }
            }

            PackageDrugInfo pditemp = new PackageDrugInfo();
            pditemp.setAmountPerTime(0);
            pditemp.setClinic(clinic.getClinicName());
            pditemp.setDispensedQty(0);
            pditemp.setBatchNumber("");
            pditemp.setFormLanguage1("");
            pditemp.setFormLanguage2("");
            pditemp.setFormLanguage3("");
            pditemp.setDrugName(syncTempDispense.getDrugname());
            pditemp.setExpiryDate(syncTempDispense.getExpirydate());
            pditemp.setPatientId(syncTempDispense.getPatientid());
            pditemp.setPatientFirstName(syncTempDispense.getPatientfirstname());
            pditemp.setPatientLastName(syncTempDispense.getPatientlastname());
            pditemp.setSpecialInstructions1("");
            pditemp.setSpecialInstructions2("");
            pditemp.setStockId(stock.getId());
            pditemp.setTimesPerDay(syncTempDispense.getTimesperday());
            pditemp.setNumberOfLabels(0);
            pditemp.setCluser(user);
            pditemp.setDispenseDate(syncTempDispense.getDispensedate());
            pditemp.setWeeksSupply(syncTempDispense.getWeekssupply());
            pditemp.setQtyInHand(syncTempDispense.getQtyinhand());
            pditemp.setSummaryQtyInHand(syncTempDispense.getSummaryqtyinhand());
            pditemp.setQtyInLastBatch(syncTempDispense.getQtyinlastbatch());
            pditemp.setPrescriptionDuration(syncTempDispense.getDuration());
            pditemp.setDateExpectedString(syncTempDispense.getDateexpectedstring());
            pditemp.setPickupDate(syncTempDispense.getPickupdate());
            pditemp.setNotes("");
            allPackagedDrugsListTemp.add(pditemp);

            savePackageAndPackagedDrugsWhithFarmacQty0(true, allPackagedDrugsListTemp, prescription, clinic, sess);
            TemporaryRecordsManager.savePackageDrugInfosToDB(sess, allPackagedDrugsListTemp);
            sess.flush();
            tx.commit();
            sess.close();

        } catch (HibernateException he) {

            MessageBox errorBox = new MessageBox(null, SWT.OK | SWT.ICON_ERROR);
            errorBox.setText("Não pode salvar: Verificar Prescricao");
            errorBox.setMessage("Houve um problema ao salvar a Prescricao. Por favor, tente novamente.");
            if (tx != null) {
                tx.rollback();
                sess.close();
            }
        }
    }

    public static void savePackageAndPackagedDrugsWhithFarmacQty0(boolean dispenseNow,
                                                                  java.util.List<PackageDrugInfo> allPackageDrugsList, Prescription prescription, Clinic clinic, Session sess) {

        // if pack date is today, store the time too, else store 12am
        PackageDrugInfo packageDrugInfo = allPackageDrugsList.get(0);
        Set<Packages> packageses = new HashSet();
        packageses.clear();
        Date packDate = new Date();

        Packages newPack = PackageManager.getLastPackageOnScript(prescription);

        if (newPack != null) {
            if (newPack.getPickupDate() != packageDrugInfo.getPickupDate())
                newPack = new Packages();
        } else
            newPack = new Packages();

        packDate.setTime(packageDrugInfo.getDispenseDate().getTime());
        newPack.setPickupDate(packageDrugInfo.getPickupDate());
        newPack.setPackDate(packageDrugInfo.getPickupDate());
        newPack.setPackageId(packageDrugInfo.getPackageId());
        newPack.setModified('T');
        newPack.setPrescription(prescription);
        newPack.getPrescription().setPackages(packageses);
        newPack.setPackageId(newPack.getPrescription().getPrescriptionId() + "- Farmac");
        newPack.setModified('T');
        newPack.setClinic(clinic);

        //int numPeriods = getSelectedWeekSupply();
        //getLog().info("getSelectedWeekSupply() called");
        // 1 mes tem 4 semanas
        newPack.setWeekssupply(4);
        /*
         * If the pharmacist is giving the drugs to the patient now, set the
         * dateLeft, dateReceived and pickupDate to today. Else ... set these
         * attributes to null (they will be set when the packages have left the
         * pharmacy, arrived at the remote clinic, and when the patient has
         * picked up their medications
         */
        if (dispenseNow) {
            newPack.setDateLeft(packageDrugInfo.getPickupDate());
            newPack.setDateReceived(packageDrugInfo.getPickupDate());
            newPack.setPickupDate(packageDrugInfo.getPickupDate());
        } else {
            if (iDartProperties.downReferralMode
                    .equalsIgnoreCase(iDartProperties.OFFLINE_DOWNREFERRAL_MODE)) {
                newPack.setDateLeft(packageDrugInfo.getPickupDate());
                newPack.setDateReceived(packageDrugInfo.getPickupDate());
                newPack.setPickupDate(null);
            } else {
                newPack.setDateLeft(null);
                newPack.setDateReceived(null);
                newPack.setPickupDate(null);
            }
        }

        // Make up a set of package drugs for this particular package
        java.util.List<PackagedDrugs> packagedDrugsList = new ArrayList<PackagedDrugs>();

        for (int ib = 0; ib < allPackageDrugsList.size(); ib++) {

            PackageDrugInfo pdi = allPackageDrugsList.get(ib);
            PackagedDrugs pd = new PackagedDrugs();
            pd.setAmount(pdi.getDispensedQty());
            pd.setParentPackage(newPack);
            pd.setStock(StockManager.getStock(sess, pdi.getStockId()));
            pd.setModified('T');
            packagedDrugsList.add(pd);
            pdi.setPackagedDrug(pd);
            pdi.setNotes(packageDrugInfo.getNotes());
            pdi.setPackageId(newPack.getPackageId());

        }

        newPack.setPackagedDrugs(packagedDrugsList);
        newPack.setDrugTypes("TARV");

        PackageManager.savePackageQty0(newPack);
    }

    public static boolean setDispenseRestOpenmrs(Session sess, Prescription prescription, SyncTempDispense syncTempDispense) {

        // Add interoperability with OpenMRS through Rest Web Services
        Clinic clinic = AdministrationManager.getMainClinic(sess);
        boolean result = true;
        boolean postOpenMrsEncounterStatus = false;

        Packages newPack = PackageManager.getLastPackageOnScript(prescription);
        Doctor doctorProvider = PrescriptionManager.getProvider(sess);
        List<PackagedDrugs> packagedDrugs = newPack.getPackagedDrugs();

        Date dtPickUp = syncTempDispense.getPickupdate();

        // EncounterDatetime
        String strPickUp = RestUtils.castDateToString(dtPickUp);

        // Patient NID
        String nid = prescription.getPatient().getPatientId().trim();

        String strProvider = doctorProvider.getFirstname().trim() + " "
                + doctorProvider.getLastname().trim();

        String providerWithNoAccents = org.apache.commons.lang3.StringUtils.stripAccents(strProvider);

        //provider
        String providerUuid = "";
        // Health Facility
        String strFacilityUuid = "";

        String facility = clinic.getClinicName().trim();

        // Regimen
        String regimenAnswer = prescription.getRegimeTerapeutico().getRegimenomeespecificado().trim();

        List<PrescribedDrugs> prescribedDrugs = prescription.getPrescribedDrugs();

        // Next pick up date
        Date dtNextPickUp = RestUtils.castStringToDate(syncTempDispense.getDateexpectedstring());

        String strNextPickUp = RestUtils.castDateToString(dtNextPickUp);

        try {
            if (getServerStatus(JdbcProperties.urlBase).contains("Red")) {

                log.trace("Servidor Rest offline, o aviamento do paciente [" + syncTempDispense.getPatientid() + " ] será armazenada para envio ao Openrms a posterior");
                saveOpenmrsPatientFila(prescription, nid, strPickUp, syncTempDispense.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                        facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                        iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);

                return  false;
            } else {

                RestClient restClient = new RestClient();

                String nidRest = restClient.getOpenMRSResource(iDartProperties.REST_GET_PATIENT + StringUtils.replace(nid, " ", "%20"));

                JSONObject jsonObject = new JSONObject(nidRest);
                JSONArray _jsonArray = (JSONArray) jsonObject.get("results");
                String nidUuid = null;

                for (int i = 0; i < _jsonArray.length(); i++) {
                    JSONObject results = (JSONObject) _jsonArray.get(i);
                    nidUuid = (String) results.get("uuid");
                }

                String nidvoided = restClient.getOpenMRSResource(iDartProperties.REST_GET_PERSON_GENERIC + nidUuid);
                JSONObject jsonObjectPerson = new JSONObject(nidvoided);
                Boolean voided = (Boolean) jsonObjectPerson.get("voided");

                String uuid = prescription.getPatient().getUuidopenmrs();
                if (uuid != null && !uuid.isEmpty()) {
                    uuid = prescription.getPatient().getUuidopenmrs();
                } else {
                    saveOpenmrsPatientFila(prescription, nid, strPickUp, syncTempDispense.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, "O NID deste paciente [" + nid + " ] foi alterado no OpenMRS ou não possui UUID."
                            + " Por favor actualize o NID na Administração do Paciente usando a opção Atualizar um Paciente Existente.");
                    return false;
                }


                if (nidUuid != null && !nidUuid.isEmpty()) {
                    if (nidUuid != uuid || voided) {

                        log.trace(" O aviamento do paciente [" + nid + " ] será armazenada para envio ao Openrms apos a verificação do erro");
                        saveOpenmrsPatientFila(prescription, nid, strPickUp, syncTempDispense.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                                facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                                iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                        saveErroLog(newPack, dtNextPickUp, "O paciente [" + nid + " ] "
                                + " Tem um UUID [" + uuid + "] diferente ou inactivo no OpenMRS " + nidUuid + "]. Por favor actualize o UUID correspondente .");

                        return false;
                    }
                }

                String openrsMrsReportingRest = restClient.getOpenMRSReportingRest(iDartProperties.REST_GET_REPORTING_REST + uuid);

                JSONObject jsonReportingRest = new JSONObject(openrsMrsReportingRest);
                JSONArray jsonReportingRestArray = (JSONArray) jsonReportingRest.get("members");

                if (jsonReportingRestArray.length() < 1) {
                    log.trace(" O aviamento do paciente [" + nid + " ] será armazenada para envio ao Openrms apos a verificacao do erro");
                    saveOpenmrsPatientFila(prescription, nid, strPickUp, syncTempDispense.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, "NID [" + nid + " ] inserido não se encontra no estado ACTIVO NO PROGRAMA/TRANSFERIDO DE. Actualize primeiro o estado do paciente no OpenMRS.");

                    return false;
                }

                String response = restClient.getOpenMRSResource(iDartProperties.REST_GET_PROVIDER + StringUtils.replace(providerWithNoAccents, " ", "%20"));

                // Location
                String strFacility = restClient.getOpenMRSResource(iDartProperties.REST_GET_LOCATION + StringUtils.replace(facility, " ", "%20"));

                if (strFacility.length() < 50) {

                    log.trace(" O aviamento do paciente [" + nid + " ] será armazenada para envio ao Openrms apos a verificacao do erro");
                    saveOpenmrsPatientFila(prescription, nid, strPickUp, syncTempDispense.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, " O UUID DA UNIDADE SANITARIA NAO CONTEM O PADRAO RECOMENDADO PARA O NID [" + nid + " ].");
                    return false;
                } else strFacilityUuid = strFacility.substring(21, 57);

                if (response.length() < 50) {

                    log.trace(" O aviamento do paciente [" + nid + " ] será armazenada para envio ao Openrms apos a verificacao do erro");
                    saveOpenmrsPatientFila(prescription, nid, strPickUp, syncTempDispense.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, " O UUID DO PROVEDOR NAO CONTEM O PADRAO RECOMENDADO OU NAO EXISTE NO OPENMRS PARA O NID [" + nid + " ].");

                    return false;
                } else providerUuid = response.substring(21, 57);

                try {
                    postOpenMrsEncounterStatus = restClient.postOpenMRSEncounter(strPickUp, uuid, iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            strFacilityUuid, iDartProperties.FORM_FILA, providerUuid, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, prescribedDrugs, packagedDrugs, iDartProperties.DOSAGE,
                            iDartProperties.VISIT_UUID, strNextPickUp);

                    result = true;
                    log.trace("Criou o fila no openmrs para o paciente " + prescription.getPatient().getPatientId() + ": " + postOpenMrsEncounterStatus);

                } catch (Exception e) {
                    result = true;
                    log.error("Nao foi criado o fila no openmrs para o paciente " + nid + ": " + postOpenMrsEncounterStatus);
                    saveErroLog(newPack, dtNextPickUp, "Nao foi criado o fila no openmrs para o paciente " + nid + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Erro agravar levantamento do paciente : " + syncTempDispense.getPatientid() + " erro: "+ e.getMessage());
        }

        return result;
    }

}
