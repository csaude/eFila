package org.celllife.idart.rest.utils;

import model.manager.OpenmrsErrorLogManager;
import model.manager.PackageManager;
import model.manager.PatientManager;
import model.manager.PrescriptionManager;
import model.nonPersistent.Autenticacao;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.rest.ApiAuthRest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;


/**
 * @author helio.machabane
 */
public class RestClient {

    private static Logger log = Logger.getLogger(RestClient.class);

    Properties prop = new Properties();
    //InputStream input = null;

    File input = new File("jdbc.properties");
    File myFile = new File("jdbc_auto_generated.properties");

    Properties prop_dynamic = new Properties();


    //SET VALUE FOR CONNECT TO OPENMRS
    public RestClient() {

        try {
            prop_dynamic.load(new FileInputStream(myFile));
            prop.load(new FileInputStream(input));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        ApiAuthRest.setURLBase(prop.getProperty("urlBase"));
        ApiAuthRest.setUsername(prop_dynamic.getProperty("userName"));
        //ApiAuthRest.setPassword(prop_dynamic.getProperty("password"));
        ApiAuthRest.setPassword(Autenticacao.senhaTemporaria);
    }

    public boolean postOpenMRSEncounter(String encounterDatetime, String nidUuid, String encounterType, String strFacilityUuid,
                                        String filaUuid, String providerUuid, String regimeUuid,
                                        String strRegimenAnswerUuid, String dispensedAmountUuid, List<PrescribedDrugs> prescribedDrugs,
                                        List<PackagedDrugs> packagedDrugs, String dosageUuid, String returnVisitUuid, String strNextPickUp, String dispenseModeUuid, String answerDispenseModeUuid) throws Exception {

        StringEntity inputAddPerson = null;

        String packSize = null;

        String dosage;

        String dosage_1;

        String customizedDosage = null;

        if (prescribedDrugs.size() == 1) {

            //Dispensed amount
            packSize = String.valueOf(packagedDrugs.get(0).getAmount());

            //Dosage
            dosage = String.valueOf(prescribedDrugs.get(0).getTimesPerDay());

            customizedDosage = iDartProperties.TOMAR + String.valueOf((int) (prescribedDrugs.get(0).getAmtPerTime()))
                    + iDartProperties.COMP + dosage + iDartProperties.VEZES_DIA;

            inputAddPerson = new StringEntity(
                    "{\"encounterDatetime\": \"" + encounterDatetime + "\", \"patient\": \"" + nidUuid + "\", \"encounterType\": \"" + encounterType + "\", "
                            + "\"location\":\"" + strFacilityUuid + "\", \"form\":\"" + filaUuid + "\", \"encounterProviders\":[{\"provider\":\"" + providerUuid + "\", \"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\"}], "
                            + "\"obs\":["
                            + "{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + regimeUuid + "\",\"value\":\"" + strRegimenAnswerUuid + "\", \"comment\":\"IDART\"},"
                            + "{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dispensedAmountUuid + "\",\"value\":\"" + packSize + "\",\"comment\":\"IDART\"},"
                            + "{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dosageUuid + "\",\"value\":\"" + customizedDosage + "\",\"comment\":\"IDART\"},"
                            + "{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + returnVisitUuid + "\",\"value\":\"" + strNextPickUp + "\",\"comment\":\"IDART\"},"
                            + "{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dispenseModeUuid + "\",\"value\":\"" + answerDispenseModeUuid + "\",\"comment\":\"IDART\"}"
                            + "]"
                            + "}"
                    , "UTF-8");

            System.out.println(IOUtils.toString(inputAddPerson.getContent()));
        } else if (prescribedDrugs.size() > 1) {

            //Dosage
            dosage = String.valueOf(prescribedDrugs.get(0).getTimesPerDay());

            String customizedDosage_0 = iDartProperties.TOMAR + String.valueOf((int) (prescribedDrugs.get(0).getAmtPerTime()))
                    + iDartProperties.COMP + dosage + iDartProperties.VEZES_DIA;

            //Dosage
            dosage_1 = String.valueOf(prescribedDrugs.get(1).getTimesPerDay());

            String customizedDosage_1 = iDartProperties.TOMAR + String.valueOf((int) (prescribedDrugs.get(1).getAmtPerTime()))
                    + iDartProperties.COMP + dosage_1 + iDartProperties.VEZES_DIA;

            inputAddPerson = new StringEntity(
                    "{\"encounterDatetime\": \"" + encounterDatetime + "\", \"patient\": \"" + nidUuid + "\", \"encounterType\": \"" + encounterType + "\", "
                            + "\"location\":\"" + strFacilityUuid + "\", \"form\":\"" + filaUuid + "\", \"encounterProviders\":[{\"provider\":\"" + providerUuid + "\", \"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\"}], "
                            + "\"obs\":[{\"person\":\"" + nidUuid + "\",\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":"
                            + "\"" + regimeUuid + "\",\"value\":\"" + strRegimenAnswerUuid + "\",\"comment\":\"IDART\"},{\"person\":"
                            + "\"" + nidUuid + "\",\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dispensedAmountUuid + "\","
                            + "\"value\":\"" + String.valueOf(packagedDrugs.get(1).getAmount()) + "\",\"comment\":\"IDART\"},{\"person\":\"" + nidUuid + "\",\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dispensedAmountUuid + "\","
                            + "\"value\":\"" + String.valueOf(packagedDrugs.get(0).getAmount()) + "\",\"comment\":\"IDART\"},{\"person\":\"" + nidUuid + "\",\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":"
                            + "\"" + dosageUuid + "\",\"value\":\"" + customizedDosage_0 + "\",\"comment\":\"IDART\"},{\"person\":\"" + nidUuid + "\",\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":"
                            + "\"" + dosageUuid + "\",\"value\":\"" + customizedDosage_1 + "\",\"comment\":\"IDART\"},{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + returnVisitUuid + "\",\"value\":\"" + strNextPickUp + "\",\"comment\":\"IDART\"},"
                            + "{\"person\":\"" + nidUuid + "\","
                            + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dispenseModeUuid + "\",\"value\":\"" + answerDispenseModeUuid + "\",\"comment\":\"IDART\"}"
                            + "]"
                            + "}"
                    , "UTF-8");
        }

        inputAddPerson.setContentType("application/json");
        //log.info("AddPerson = " + ApiAuthRest.getRequestPost("encounter",inputAddPerson));
        return ApiAuthRest.getRequestPost("encounter", inputAddPerson);
    }


    public boolean postOpenMRSEncounterFILT(String encounterDatetime, String nidUuid, String encounterType, String strFacilityUuid,
                                            String filtUuid, String providerUuid, String regimeFiltUuid, String tipoDispensaUuid, String seguimentoFiltUuid,
                                            String strRegimenAnswerUuid, List<PrescribedDrugs> prescribedDrugs, String returnVisitUuid, String strNextPickUp,
                                            String dispenseModeUuid, String answerDispenseModeUuid) throws Exception {

        StringEntity inputAddPerson = null;

        String tipoDispensa = null;

        String tipoPrescricao = null;

        Prescription prescription = null;

        if (prescribedDrugs.size() > 0) {

            prescription = prescribedDrugs.get(0).getPrescription();

            if (prescription != null) {

                if (prescription.getDispensaTrimestral() == 1)
                    tipoDispensa = iDartProperties.FILT_QUARTERLY_DISPENSED_TYPE_UUID;
                else if (prescription.getDispensaSemestral() == 1)
                    tipoDispensa = iDartProperties.FILT_SEMESTRAL_DISPENSED_TYPE_UUID;
                else
                    tipoDispensa = iDartProperties.FILT_MONTHLY_DISPENSED_TYPE_UUID;

                if (prescription.getReasonForUpdate().startsWith("I"))
                    tipoPrescricao = iDartProperties.FILT_TPT_INITIAL_FOLLOW_UP_UUID;
                else if (prescription.getReasonForUpdate().startsWith("F"))
                    tipoPrescricao = iDartProperties.FILT_TPT_END_FOLLOW_UP_UUID;
                else if (prescription.getReasonForUpdate().startsWith("R"))
                    tipoPrescricao = iDartProperties.FILT_TPT_RESTART_FOLLOW_UP_UUID;
                else
                    tipoPrescricao = iDartProperties.FILT_TPT_CONTINUE_FOLLOW_UP_UUID;

                inputAddPerson = new StringEntity(
                        "{\"encounterDatetime\": \"" + encounterDatetime + "\", \"patient\": \"" + nidUuid + "\", \"encounterType\": \"" + encounterType + "\", "
                                + "\"location\":\"" + strFacilityUuid + "\", \"form\":\"" + filtUuid + "\", \"encounterProviders\":[{\"provider\":\"" + providerUuid + "\", \"encounterRole\":\"a0b03050-c99b-11e0-9572-0800200c9a66\"}], "
                                + "\"obs\":["
                                + "{\"person\":\"" + nidUuid + "\","
                                + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + regimeFiltUuid + "\",\"value\":\"" + strRegimenAnswerUuid + "\", \"comment\":\"IDART\"},"
                                + "{\"person\":\"" + nidUuid + "\","
                                + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + tipoDispensaUuid + "\",\"value\":\"" + tipoDispensa + "\",\"comment\":\"IDART\"},"
                                + "{\"person\":\"" + nidUuid + "\","
                                + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + seguimentoFiltUuid + "\",\"value\":\"" + tipoPrescricao + "\",\"comment\":\"IDART\"},"
                                + "{\"person\":\"" + nidUuid + "\","
                                + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + returnVisitUuid + "\",\"value\":\"" + strNextPickUp + "\",\"comment\":\"IDART\"},"
                                + "{\"person\":\"" + nidUuid + "\","
                                + "\"obsDatetime\":\"" + encounterDatetime + "\",\"concept\":\"" + dispenseModeUuid + "\",\"value\":\"" + answerDispenseModeUuid + "\",\"comment\":\"IDART\"}"
                                + "]"
                                + "}"
                        , "UTF-8");
                System.out.println(IOUtils.toString(inputAddPerson.getContent()));
            } else {
                log.error("Prescricao foi removida no iDART");
                return false;
            }
        }

        inputAddPerson.setContentType("application/json");
        //log.info("AddPerson = " + ApiAuthRest.getRequestPost("encounter",inputAddPerson));
        return ApiAuthRest.getRequestPost("encounter", inputAddPerson);
    }

    public boolean postOpenMRSPatient(String gender, String firstName, String middleName, String lastName, String birthDate, String nid) throws Exception {

        StringEntity inputAddPatient;

        String openmrsJSON = "";

        if (birthDate.isEmpty()) {

            openmrsJSON = "{\"person\":"
                    + "{"
                    + "\"gender\": \"" + gender + "\","
                    + "\"names\":"
                    + "[{\"givenName\": \"" + firstName + "\", \"middleName\": \"" + middleName + "\", \"familyName\": \"" + lastName + "\"}]"
                    + "},"
                    + "\"identifiers\":"
                    + "["
                    + "{"
                    + "\"identifier\": \"" + nid + "\", \"identifierType\": \"e2b966d0-1d5f-11e0-b929-000c29ad1d07\","
                    + "\"location\": \"" + prop.getProperty("location") + "\", \"preferred\": \"true\""
                    + "}"
                    + "]"
                    + "}";
        } else {
            openmrsJSON = "{\"person\":"
                    + "{"
                    + "\"gender\": \"" + gender + "\","
                    + "\"names\":"
                    + "[{\"givenName\": \"" + firstName + "\", \"middleName\": \"" + middleName + "\", \"familyName\": \"" + lastName + "\"}], \"birthdate\": \"" + birthDate + "\""
                    + "},"
                    + "\"identifiers\":"
                    + "["
                    + "{"
                    + "\"identifier\": \"" + nid + "\", \"identifierType\": \"e2b966d0-1d5f-11e0-b929-000c29ad1d07\","
                    + "\"location\": \"" + prop.getProperty("location") + "\", \"preferred\": \"true\""
                    + "}"
                    + "]"
                    + "}";
        }

        inputAddPatient = new StringEntity(openmrsJSON, "UTF-8");

        inputAddPatient.setContentType("application/json");
        //log.info("AddPerson = " + ApiAuthRest.getRequestPost("encounter",inputAddPerson));
        return ApiAuthRest.getRequestPost("patient", inputAddPatient);
    }

    public String getOpenMRSResource(String resourceParameter) {
        String resource = null;
        try {
            resource = ApiAuthRest.getRequestGet(resourceParameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resource;
    }

    public String getOpenMRSReportingRest(String resourceParameter) {
        ApiAuthRest.setURLReportingBase(prop.getProperty("urlBaseReportingRest"));
        String resource = null;
        try {
            resource = ApiAuthRest.getReportingRequestGet(resourceParameter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resource;
    }

    public static void saveOpenmrsPatient(Patient patient, Session session) {
        // Adiciona paciente referido para a sincronizacao.
        SyncOpenmrsPatient syncOpenmrsPatient = null;

        if (patient.getPatientId() != null)
            syncOpenmrsPatient = PatientManager.getSyncOpenmrsPatienByNID(session, patient.getPatientId());


        if (syncOpenmrsPatient == null)
            syncOpenmrsPatient = new SyncOpenmrsPatient();

        syncOpenmrsPatient.setAddress1(patient.getAddress1());
        syncOpenmrsPatient.setAddress2(patient.getAddress2());
        syncOpenmrsPatient.setAddress3(patient.getAddress3());
        syncOpenmrsPatient.setCellphone(patient.getCellphone());
        syncOpenmrsPatient.setDateofbirth(patient.getDateOfBirth());
        syncOpenmrsPatient.setNextofkinname(patient.getNextOfKinName());
        syncOpenmrsPatient.setNextofkinphone(patient.getNextOfKinPhone());
        syncOpenmrsPatient.setFirstnames(patient.getFirstNames());
        syncOpenmrsPatient.setHomephone(patient.getHomePhone());
        syncOpenmrsPatient.setLastname(patient.getLastname());
        syncOpenmrsPatient.setPatientid(patient.getPatientId());
        syncOpenmrsPatient.setProvince(patient.getProvince());
        syncOpenmrsPatient.setSex(patient.getSex());
        syncOpenmrsPatient.setWorkphone(patient.getWorkPhone());
        syncOpenmrsPatient.setRace(patient.getRace());
        syncOpenmrsPatient.setUuid(patient.getUuidopenmrs());

        syncOpenmrsPatient.setSyncstatus('P');

        PatientManager.saveSyncOpenmrsPatien(session, syncOpenmrsPatient);

    }

    public static void setOpenmrsPatients(Session sess) {

        RestClient restClient = new RestClient();

        boolean postOpenMrsEncounterStatus;

        String name = "";
        String middleName = "";


        try {

            List<SyncOpenmrsPatient> syncOpenmrsPatients = PatientManager.getAllSyncOpenmrsPatientReadyToSave(sess);
            if (!syncOpenmrsPatients.isEmpty()) {

                for (SyncOpenmrsPatient patientToSave : syncOpenmrsPatients) {
                    Session session = HibernateUtil.getNewSession();

                    try {
                        String uuid = null;
                        session.beginTransaction();
                        Patient patient = PatientManager.getPatient(session, patientToSave.getPatientid());

                        if (patient != null) {
                            uuid = getUUidFromOpenmrs(patient.getPatientId());

                            if (uuid == null) {
                                name = patient.getFirstNames();

                                if (name.contains(" ")) {
                                    middleName = name.substring(name.indexOf(" "), name.length() - 1);
                                    name = name.substring(0, name.indexOf(" ") - 1);
                                }

                                postOpenMrsEncounterStatus = restClient.postOpenMRSPatient(patient.getSex() + "", name, middleName, patient.getLastname(),
                                        RestUtils.castDateToString(patient.getDateOfBirth()), patient.getPatientId());

                                if (postOpenMrsEncounterStatus) {
                                    uuid = getUUidFromOpenmrs(patient.getPatientId());
                                }
                            }
                            patient.setUuidopenmrs(uuid);
                            PatientManager.savePatient(session, patient);
                        } else
                            log.trace(new Date() + ": O Paciente [" + patientToSave.getFirstnames() + " " + patientToSave.getLastname() + " com NID: " + patientToSave.getPatientid() + "] foi removido");

                        patientToSave.setSyncstatus('E');
                        PatientManager.saveSyncOpenmrsPatien(session, patientToSave);

                        session.getTransaction().commit();
                        session.flush();
                        session.clear();
                        session.close();
                        break;
                    } catch (Exception e) {
                        session.getTransaction().rollback();
                        session.close();
                        log.trace(new Date() + ": Erro ao gravar informacao do Paciente [" + patientToSave.getFirstnames() + " " + patientToSave.getLastname() + " com NID: " + patientToSave.getPatientid() + "] verifique o acesso do user ao openmrs ou contacte o administrador");
                    } finally {
                        continue;
                    }
                }
            } else {
                log.trace(new Date() + ": INFO - Nenhumm paciente por enviar foi encontrado");
            }
        } catch (Exception e) {
            log.trace("Error :" + e);
        }
    }

    public static void setOpenmrsPatientFila(Session sess) {

        try {
            List<SyncOpenmrsDispense> syncOpenmrsDispenses = PrescriptionManager.getAllSyncOpenmrsDispenseReadyToSave(sess);
            if (!syncOpenmrsDispenses.isEmpty()) {
                for (SyncOpenmrsDispense dispense : syncOpenmrsDispenses) {
                    Session session = HibernateUtil.getNewSession();

                    try {
                        session.beginTransaction();
                        Prescription prescription = PackageManager.getPrescription(session, dispense.getPrescription().getPrescriptionId());

                        if (prescription != null) {
                            restFilaToOpenMRS(session, dispense);
                        } else {
                            log.trace(new Date() + ": INFO - A Prescrição com o codigo: [" + dispense.getPrescription().getPrescriptionId() + "] foi removido");
                            dispense.setSyncstatus('E');
                        }
                        PrescriptionManager.saveSyncOpenmrsPatienFila(session, dispense);
                        session.getTransaction().commit();
                        session.flush();
                        session.clear();
                        session.close();
                        break;
                    } catch (Exception e) {
                        session.getTransaction().rollback();
                        session.close();
                        log.trace(new Date() + ": INFO - Erro ao gravar levantamento do Paciente com NID: [" + dispense.getPrescription().getPatient().getPatientId() + "], verifique o acesso do user ao openmrs ou contacte o administrador");
                    } finally {
                        continue;
                    }
                }
            } else {
                log.trace(new Date() + ": INFO - Nenhumm levantamento enviado para Openmrs foi encontrado");
            }
        } catch (Exception e) {
            log.trace("Error :" + e);
        }
    }

    public static String getUUidFromOpenmrs(String patientId) {

        String resource = new RestClient().getOpenMRSResource(iDartProperties.REST_GET_PATIENT + StringUtils.replace(patientId.toUpperCase(), " ", "%20"));

        JSONObject _jsonObject = new JSONObject(resource);

        String personUuid = null;

        JSONArray _jsonArray = (JSONArray) _jsonObject.get("results");

        for (int i = 0; i < _jsonArray.length(); i++) {
            JSONObject results = (JSONObject) _jsonArray.get(i);
            personUuid = (String) results.get("uuid");
        }

        return personUuid;
    }


    public static void restFilaToOpenMRS(Session session, SyncOpenmrsDispense dispense) {

        // Add interoperability with OpenMRS through Rest Web Services
        RestClient restClient = new RestClient();
        boolean postOpenMrsEncounterStatus = false;
        boolean hasError = false;
        String msgError = "";
        String strFacilityUuid = "";
        String providerUuid = "";

        String nidRest = restClient.getOpenMRSResource(iDartProperties.REST_GET_PATIENT + StringUtils.replace(dispense.getNid(), " ", "%20"));

        Packages newPack = PackageManager.getLastPackageOnScript(dispense.getPrescription());

        Patient patient = PatientManager.getPatient(session, dispense.getNid());

        JSONObject jsonObject = new JSONObject(nidRest);
        JSONArray _jsonArray = (JSONArray) jsonObject.get("results");
        String nidUuid = null;

        for (int i = 0; i < _jsonArray.length(); i++) {
            JSONObject results = (JSONObject) _jsonArray.get(i);
            nidUuid = (String) results.get("uuid");
        }

        if (dispense.getUuid() == null)
            dispense.setUuid(nidUuid);

        String uuid = dispense.getUuid();
        if (uuid != null && !uuid.isEmpty()) {
            uuid = dispense.getUuid();
        } else {
            msgError = " O NID [" + dispense.getNid() + "] foi alterado no OpenMRS ou não possui UUID."
                    + " Por favor actualize o NID na Administração do Paciente usando a opção Atualizar um Paciente Existente.";
            log.trace(new Date() + msgError);
            saveErroLog(newPack, RestUtils.castStringToDatePattern(dispense.getStrNextPickUp()), msgError);
            return;
        }

        if (!patient.getUuidopenmrs().equals(uuid)) {
            msgError = " O paciente [" + patient.getPatientId() + " ] "
                    + " Tem um UUID [" + patient.getUuidopenmrs() + "] diferente ou inactivo no OpenMRS " + nidUuid + "]. Por favor actualize o UUID correspondente .";
            log.trace(new Date() + msgError);
            saveErroLog(newPack, RestUtils.castStringToDatePattern(dispense.getStrNextPickUp()), msgError);
            return;
        }

        uuid = patient.getUuidopenmrs();

        String openrsMrsReportingRest = restClient.getOpenMRSReportingRest(iDartProperties.REST_GET_REPORTING_REST + uuid);

        JSONObject jsonReportingRest = new JSONObject(openrsMrsReportingRest);
        JSONArray jsonReportingRestArray = (JSONArray) jsonReportingRest.get("members");


        if (jsonReportingRestArray.length() < 1) {

            msgError = " O NID [" + dispense.getNid() + " com o uuid (" + dispense.getUuid() + ")]  não se encontra no estado ACTIVO NO PROGRAMA/TRANSFERIDO DE. " +
                    " ou contem o UUID inactivo/inexistente. Actualize primeiro o estado do paciente no OpenMRS..";
            log.trace(new Date() + msgError);

            saveErroLog(newPack, RestUtils.castStringToDatePattern(dispense.getStrNextPickUp()), msgError);
            return;
        }

        String response = restClient.getOpenMRSResource(iDartProperties.REST_GET_PROVIDER + StringUtils.replace(dispense.getProvider(), " ", "%20"));

        String strFacility = restClient.getOpenMRSResource(iDartProperties.REST_GET_LOCATION + StringUtils.replace(dispense.getStrFacility(), " ", "%20"));

        if (strFacility.length() < 50) {
            msgError = " O UUID DA UNIDADE SANITARIA NAO CONTEM O PADRAO RECOMENDADO PARA O NID [" + dispense.getNid() + " ].";
            log.trace(new Date() + msgError);
            saveErroLog(newPack, RestUtils.castStringToDatePattern(dispense.getStrNextPickUp()), msgError);
            return;
        } else strFacilityUuid = strFacility.substring(21, 57);

        if (response.length() < 50) {
            msgError = " O UUID DO PROVEDOR NAO CONTEM O PADRAO RECOMENDADO OU NAO EXISTE NO OPENMRS PARA O NID [" + dispense.getNid() + " ].";
            log.trace(new Date() + msgError);
            saveErroLog(newPack, RestUtils.castStringToDatePattern(dispense.getStrNextPickUp()), msgError);
            return;
        } else providerUuid = response.substring(21, 57);

        try {

            if (newPack.getPrescription().getTipoDoenca().equalsIgnoreCase(iDartProperties.PNCT)) {
                postOpenMrsEncounterStatus = restClient.postOpenMRSEncounterFILT(dispense.getStrPickUp(), uuid, iDartProperties.ENCOUNTER_TYPE_FILT,
                        strFacilityUuid, iDartProperties.FORM_FILT_UUID, providerUuid, iDartProperties.REGIME_TPT_UUID, iDartProperties.FILT_DISPENSED_TYPE_UUID,
                        iDartProperties.FILT_TPT_FOLLOW_UP_UUID, dispense.getRegimenAnswer(), dispense.getPrescription().getPrescribedDrugs(), iDartProperties.FILT_NEXT_APOINTMENT_UUID,
                        dispense.getStrNextPickUp(), iDartProperties.DISPENSEMODE_UUID, dispense.getDispenseModeAnswer());
            } else if (newPack.getPrescription().getTipoDoenca().equalsIgnoreCase("Prep")) {
                // to add
            } else {
                postOpenMrsEncounterStatus = restClient.postOpenMRSEncounter(dispense.getStrPickUp(), uuid, iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                        strFacilityUuid, iDartProperties.FORM_FILA, providerUuid, iDartProperties.REGIME, dispense.getRegimenAnswer(),
                        iDartProperties.DISPENSED_AMOUNT, dispense.getPrescription().getPrescribedDrugs(), newPack.getPackagedDrugs(), iDartProperties.DOSAGE,
                        iDartProperties.VISIT_UUID, dispense.getStrNextPickUp(), iDartProperties.DISPENSEMODE_UUID, dispense.getDispenseModeAnswer());
            }

            log.trace("Criou o fila no openmrs para o paciente " + dispense.getNid() + ": " + postOpenMrsEncounterStatus);

            if (postOpenMrsEncounterStatus) {
                dispense.setSyncstatus('E');
                dispense.setUuid(uuid);
                PrescriptionManager.setUUIDSyncOpenmrsPatienFila(session, dispense);
                OpenmrsErrorLog errorLog = OpenmrsErrorLogManager.getErrorLog(session, newPack.getPrescription());
                if (errorLog != null)
                    OpenmrsErrorLogManager.removeErrorLog(session, errorLog);
            }
        } catch (Exception e) {
            msgError = "Nao foi criado o fila no openmrs para o paciente " + dispense.getNid() + ": " + e.getMessage() +
                    "\nHouve um problema ao salvar o pacote de medicamentos para o paciente " + dispense.getNid() +
                    ". " + "Por favor contacte o Administrador.";
            log.trace("Nao foi criado o fila no openmrs para o paciente " + dispense.getNid() + ": " + postOpenMrsEncounterStatus);
            saveErroLog(newPack, RestUtils.castStringToDatePattern(dispense.getStrNextPickUp()), msgError);
        }
    }

    public static void saveErroLog(Packages newPack, Date dtNextPickUp, String error) {
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();
        try {
            OpenmrsErrorLog errorLog = OpenmrsErrorLogManager.getErrorLog(sess, newPack.getPrescription());
            if (errorLog == null) {
                errorLog = new OpenmrsErrorLog();
                errorLog.setPatient(newPack.getPrescription().getPatient());
                errorLog.setPrescription(newPack.getPrescription());
                errorLog.setPickupdate(newPack.getPickupDate());
                errorLog.setReturnpickupdate(dtNextPickUp);
                errorLog.setErrordescription("[" + newPack.getPrescription().getTipoDoenca() + "] - " + error);
                errorLog.setDatacreated(new Date());
                OpenmrsErrorLogManager.saveOpenmrsRestLog(sess, errorLog);
            }
            sess.flush();
            tx.commit();
            sess.close();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
                sess.close();
            }
        }
    }
}
