package org.celllife.idart.rest.utils;

import com.google.gson.Gson;
import migracao.swingreverse.DadosPacienteFarmac;
import model.manager.AdministrationManager;
import model.manager.EpisodeManager;
import model.manager.PatientManager;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.misc.iDARTUtil;
import org.celllife.idart.rest.ApiAuthRest;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

public class RestFarmac {

    Properties prop = new Properties();

    public final static Logger log = Logger.getLogger(RestFarmac.class);

    File input = new File("centralization.properties");

    public RestFarmac() {

        try {
            prop.load(new FileInputStream(input));
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        ApiAuthRest.setURLBase(prop.getProperty("centralized_server_url"));
        ApiAuthRest.setUsername(prop.getProperty("rest_access_username"));
        ApiAuthRest.setPassword(prop.getProperty("rest_access_password"));
    }

    public static String restGetAllPatientsModified(String url, Clinic mainClinic, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;

        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        String path = url + "/sync_temp_patients?modified=eq.T&mainclinicuuid=eq." + mainClinic.getUuid();
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);
            ;

            response = ApiAuthRest.postgrestRequestGetAll(path, token, pool);

            InputStream in = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder str = new StringBuilder();
            SyncTempPatient syncTempPatient = null;
            String line = null;
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;


            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");

                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line;
                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        syncTempPatient = gson.fromJson(jsonObj.toString(), SyncTempPatient.class);

                        String updateStatus = "{\"modified\":\"F\"}";
                        AdministrationManager.updateSyncTempPatient(sess, syncTempPatient);
                        AdministrationManager.updateLastEpisode(sess, syncTempPatient);
                        restPatchPatient(url, syncTempPatient, updateStatus, pool);
                        log.trace(" Paciente [" + syncTempPatient + "] contra refrido para " + syncTempPatient.getClinicname() + " Actualizado com sucesso");
                        log.info(" Paciente [" + syncTempPatient + "] contra refrido para " + syncTempPatient.getClinicname() + " Actualizado com sucesso");
                        break;
                    } catch (Exception e) {
                        assert tx != null;
                        tx.rollback();
                        log.error(" Ocorreu um erro ao gravar a actualizacão do Paciente [" + syncTempPatient + "] contra refrido para " + syncTempPatient.getClinicname());
                        log.trace(" Ocorreu um erro ao gravar a actualizacão do Paciente [" + syncTempPatient + "] contra refrido para " + syncTempPatient.getClinicname());
                    } finally {

                        continue;
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert tx != null;
        tx.commit();
        sess.flush();
        sess.close();

        return response.getStatusLine().toString();
    }

    public static String restGeAllPatients(String url, Clinic refClinic, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;

        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        String path = url + "/sync_temp_patients?syncstatus=eq.P&clinicuuid=eq." + refClinic.getUuid();
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);
            ;

            response = ApiAuthRest.postgrestRequestGetAll(path, token, pool);

            InputStream in = response.getEntity().getContent();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder str = new StringBuilder();
            SyncTempPatient syncTempPatient = null;
            String line = null;
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;


            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");

                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line;
                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        syncTempPatient = gson.fromJson(jsonObj.toString(), SyncTempPatient.class);
                        String updateStatus = "{\"syncstatus\":\"I\"}";
                        AdministrationManager.saveSyncTempPatient(sess, syncTempPatient);

                        restPatchPatient(url, syncTempPatient, updateStatus, pool);
                        log.trace(" Paciente [" + syncTempPatient + "] Refrido de " + syncTempPatient.getMainclinicname() + " carregado com sucesso");
                        log.info(" Paciente [" + syncTempPatient + "] Refrido de " + syncTempPatient.getMainclinicname() + " carregado com sucesso");
                        break;
                    } catch (Exception e) {
                        assert tx != null;
                        tx.rollback();
                        log.error(" Ocorreu um erro ao gravar a informacao do Paciente [" + syncTempPatient + "] Refrido de " + syncTempPatient.getMainclinicname());
                        log.trace(" Ocorreu um erro ao gravar a informacao do Paciente [" + syncTempPatient + "] Refrido de " + syncTempPatient.getMainclinicname());
                    } finally {

                        continue;
                    }
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert tx != null;
        tx.commit();
        sess.flush();
        sess.close();

        return response.getStatusLine().toString();
    }

    public static String restGetPatient(SyncTempPatient syncTempPatient, String url, PoolingHttpClientConnectionManager pool) throws Exception {

        String pathuuid = url + "/sync_temp_patients?uuid=eq." + syncTempPatient.getUuid();
        String pathnidandclinic = url + "/sync_temp_patients?patientid=eq." + syncTempPatient.getPatientid() + "&mainclinic=eq." + syncTempPatient.getMainclinic();
        HttpResponse httpResponse = null;
        String response = null;

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestGet(pathuuid, token, pool);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                    httpResponse = ApiAuthRest.postgrestRequestGet(pathnidandclinic, token, pool);
            }

            if (httpResponse.getStatusLine().getStatusCode() != 200)
                response = "Falha no POSTGREST GET - Code:" + httpResponse.getStatusLine().getStatusCode();
            else
                response = "POSTGREST GET efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restPostPatient(String url, SyncTempPatient syncTempPatient, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/sync_temp_patients";
        HttpResponse httpResponse = null;
        String response = null;

        Gson g = new Gson();
        String restObject = g.toJson(syncTempPatient);
        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPost(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST POST - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST POST efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restPostEpisode(String url, SyncEpisode syncEpisode, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/sync_temp_episode";
        HttpResponse httpResponse = null;
        String response = null;

        Gson g = new Gson();

        String restObject = g.toJson(syncEpisode);
        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPost(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST POST - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST POST efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restPutPatient(String url, SyncTempPatient syncTempPatient, PoolingHttpClientConnectionManager pool) throws Exception {

        String path = url + "/sync_temp_patients?id=eq." + syncTempPatient.getId() + "&mainclinicname=eq." + syncTempPatient.getMainclinicname().replaceAll(" ","%20");
        HttpResponse httpResponse = null;
        String response = null;

        Gson g = new Gson();
        String restObject = g.toJson(syncTempPatient);
        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPut(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST PUT - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST PUT efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String restPatchPatient(String url, SyncTempPatient syncTempPatient, String restObject, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/sync_temp_patients?id=eq." + syncTempPatient.getId() + "&mainclinic=eq." + syncTempPatient.getMainclinic();
        HttpResponse httpResponse = null;
        String response = null;

        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPatch(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                    if (httpResponse.getStatusLine().getStatusCode() != 200)
                        response = "Falha no POSTGREST PATCH - Code:" + httpResponse.getStatusLine().getStatusCode();
                    else
                        response = " POSTGREST PATCH efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }
            response = "Nao foi executado o POSTGREST PATCH request";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String restPatchDispense(String url, SyncTempDispense syncTempDispense, String restObject, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/sync_temp_dispense?id=eq." + syncTempDispense.getId() + "&mainclinic=eq." + syncTempDispense.getMainclinic();
        HttpResponse httpResponse = null;
        String response = null;

        //  Gson g = new Gson();
        //  String restObject = g.toJson(syncTempDispense);
        StringEntity inputAddDispense = new StringEntity(restObject, "UTF-8");
        inputAddDispense.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPatch(path, inputAddDispense, token, pool);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                    if (httpResponse.getStatusLine().getStatusCode() != 200)
                        response = "Falha no POSTGREST PATCH - Code:" + httpResponse.getStatusLine().getStatusCode();
                    else
                        response = " POSTGREST PATCH efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }
            response = "Nao foi executado o POSTGREST PATCH request";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String restPatchEpisode(String url, SyncEpisode syncEpisode, String restObject, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/sync_temp_episode?id=eq." + syncEpisode.getId() + "&clinicuuid=eq." + syncEpisode.getClinicuuid();
        HttpResponse httpResponse = null;
        String response = null;

        //  Gson g = new Gson();
        //  String restObject = g.toJson(syncTempDispense);
        StringEntity stringEntity = new StringEntity(restObject, "UTF-8");
        stringEntity.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPatch(path, stringEntity, token, pool);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                    if (httpResponse.getStatusLine().getStatusCode() != 200)
                        response = "Falha no POSTGREST PATCH - Code:" + httpResponse.getStatusLine().getStatusCode();
                    else
                        response = " POSTGREST PATCH efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }
            response = "Nao foi executado o POSTGREST PATCH request";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public String restDeletePatient(String url, SyncTempPatient syncTempPatient, PoolingHttpClientConnectionManager pool) {

        String pathuuid = url + "/sync_temp_patients?uuid=eq." + syncTempPatient.getUuid();
        String pathnidandclinic = url + "/sync_temp_patients?patientid=eq." + syncTempPatient.getPatientid() + "&mainclinic=eq." + syncTempPatient.getMainclinic();
        HttpResponse httpResponse = null;
        String response = null;

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestDelete(pathuuid, token, pool);

            if (httpResponse != null) {
                if (httpResponse.getStatusLine().getStatusCode() != 200)
                    httpResponse = ApiAuthRest.postgrestRequestDelete(pathnidandclinic, token, pool);
            }

            if (httpResponse.getStatusLine().getStatusCode() != 200)
                response = "Falha no POSTGREST DELETE - Code:" + httpResponse.getStatusLine().getStatusCode();
            else
                response = "POSTGREST DELETE efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restGeAllEpisodes(String url, Clinic mainClinic, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;

        String path = url + "/sync_temp_episode?syncstatus=eq.S&usuuid=eq." + mainClinic.getUuid();
        //String path = url + "/sync_temp_episode?id=eq.596409";
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            response = ApiAuthRest.postgrestRequestGetAll(path, token, pool);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            SyncEpisode syncEpisode = null;
            String line = null;
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;
            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");

                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line;
                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        syncEpisode = gson.fromJson(jsonObj.toString(), SyncEpisode.class);
                        String updateStatus = "{\"syncstatus\":\"U\"}";

                        int centralId = syncEpisode.getId();

                        EpisodeManager.saveSyncTempEpisode(syncEpisode);

                        syncEpisode.setId(centralId);

                        restPatchEpisode(url, syncEpisode, updateStatus, pool);
                        log.trace(" Informacao do episodio do Paciente [" + syncEpisode.getPatientUUID() + "]  carregada/actualizada com sucesso");
                        log.info(" Informacao do episodio do Paciente [" + syncEpisode.getPatientUUID() + "]  carregada/actualizada com sucesso");
                        break;
                    } catch (Exception e) {
                        log.trace(" Ocorreu um erro ao carregar o episodio do Paciente [" + syncEpisode.getPatientUUID() + "] ERRO: " + e.getMessage());
                        log.error(" Ocorreu um erro ao carregar o episodio do Paciente [" + syncEpisode.getPatientUUID() + "] ERRO: " + e.getMessage());
                    } finally {

                        continue;
                    }
                } else {
                    log.trace(new Date() + " [FARMAC] INFO - Nenhumm Episodio do paciente referido foi encontrado");
                    log.info(new Date() + " [FARMAC] INFO - Nenhumm Episodio do paciente referido foi encontrado");
                }
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.getStatusLine().toString();
    }

    public static String restGeAllDispenses(String url, Clinic mainClinic, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();
        String path = url + "/sync_temp_dispense?syncstatus=eq.P&mainclinicuuid=eq." + mainClinic.getUuid();
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            response = ApiAuthRest.postgrestRequestGetAll(path, token, pool);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            SyncTempDispense syncTempDispense = null;
            String line = null;
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;
            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");

                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line;
                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        syncTempDispense = gson.fromJson(jsonObj.toString(), SyncTempDispense.class);
                        String updateStatus = "{\"syncstatus\":\"I\"}";

                        AdministrationManager.saveSyncTempDispense(sess, syncTempDispense);
                        restPatchDispense(url, syncTempDispense, updateStatus, pool);
                        log.trace(" Informacao de Levantamento do Paciente [" + syncTempDispense.getPatientid() + "] referido de " + syncTempDispense.getMainclinicname() + " carregada/actualizado com sucesso");
                        log.info(" Informacao de Levantamento do Paciente [" + syncTempDispense.getPatientid() + "] referido de " + syncTempDispense.getMainclinicname() + " carregada/actualizado com sucesso");
                        break;
                    } catch (Exception e) {
                        log.trace(" Ocorreu um erro ao carregar a informacao do Paciente [" + syncTempDispense.getPatientid() + "] Refrido de " + syncTempDispense.getMainclinicname() + " ERRO: " + e.getMessage());
                        log.error(" Ocorreu um erro ao carregar a informacao do Paciente [" + syncTempDispense.getPatientid() + "] Refrido de " + syncTempDispense.getMainclinicname() + " ERRO: " + e.getMessage());
                    } finally {

                        continue;
                    }
                } else {
                    log.trace(new Date() + " [FARMAC] INFO - Nenhumm Levantamento do paciente referido foi encontrado");
                    log.info(new Date() + " [FARMAC] INFO - Nenhumm Levantamento do paciente referido foi encontrado");
                }
            }

            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert tx != null;
        tx.commit();
        sess.flush();
        sess.close();

        return response.getStatusLine().toString();
    }

    public static String restPostDispense(String url, SyncTempDispense syncTempDispense, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/sync_temp_dispense";
        HttpResponse httpResponse = null;
        String response = null;

        Gson g = new Gson();
        String restObject = g.toJson(syncTempDispense);

        StringEntity inputAddDispense = new StringEntity(restObject, "UTF-8");
        inputAddDispense.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPost(path, inputAddDispense, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST POST - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST POST efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restPostClinic(String url, Clinic clinic, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/clinic";
        HttpResponse httpResponse = null;
        String response = null;
        String facilitytype = "Unidade Sanitária";

        Random r = new Random();
        int low = 1000;
        int high = 50000;
        int genId = r.nextInt(high-low) + low;

        String clinicJSONObject = "{\"id\": \""+genId+"\", \"mainclinic\": \"" + false + "\", \"notes\": \"" + clinic.getNotes() + "\", "
                + "\"code\":\"" + clinic.getCode() + "\", \"telephone\":\"" + clinic.getTelephone() + "\", \"facilitytype\":\"" + facilitytype + "\", "
                + "\"clinicname\":\"" + clinic.getClinicName() + "\",\"province\":\"" + clinic.getProvince() + "\",\"district\":\"" + clinic.getDistrict() + "\", "
                + "\"subdistrict\":\"" + clinic.getSubDistrict() + "\",\"uuid\":\"" + clinic.getUuid() + "\"}";

        StringEntity inputAddDispense = new StringEntity(clinicJSONObject,"UTF-8");

        inputAddDispense.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPost(path, inputAddDispense, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST POST - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST POST efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restPostSectorClinico(String url, ClinicSector clinicSector, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        String path = url + "/clinicsector";
        HttpResponse httpResponse = null;
        String response = null;

        String clinicJSONObject = "{\"id\": \""+clinicSector.getId()+"\", \"code\": \"" + clinicSector.getCode() + "\", \"sectorname\": \"" + clinicSector.getSectorname() + "\", "
                + "\"clinicsectortype\":\"" + clinicSector.getClinicSectorType().getId() + "\", \"telephone\":\"" + clinicSector.getTelephone() + "\", "
                + "\"clinic\":\"" + clinicSector.getClinic().getUuid() + "\",\"clinicuuid\":\"" + clinicSector.getClinicuuid() + "\",\"uuid\":\"" + clinicSector.getUuid() + "\"}";

        StringEntity inputAddDispense = new StringEntity(clinicJSONObject, "UTF-8");
        inputAddDispense.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPost(path, inputAddDispense, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST POST - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST POST efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static String restPutDispense(String url, SyncTempDispense syncTempDispense, PoolingHttpClientConnectionManager pool) throws Exception {

        String path = url + "/sync_temp_dispense?id=eq." + syncTempDispense.getId() + "&mainclinicname=eq." + syncTempDispense.getMainclinicname().replaceAll(" ","%20");
        HttpResponse httpResponse = null;
        String response = null;

        Gson g = new Gson();
        String restObject = g.toJson(syncTempDispense);
        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPut(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST PUT - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST PUT efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String restPutClinic(String url, Clinic clinic, PoolingHttpClientConnectionManager pool) throws Exception {

        String path = url + "/clinic?id=eq." + clinic.getId() + "&uuid=eq." + clinic.getUuid().replaceAll(" ","%20");
        HttpResponse httpResponse = null;
        String response = null;

        Gson g = new Gson();
        String restObject = g.toJson(clinic);
        StringEntity inputAddPatient = new StringEntity(restObject, "UTF-8");
        inputAddPatient.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPut(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST PUT - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST PUT efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static String restPutClinicSector(String url, ClinicSector clinicSector, PoolingHttpClientConnectionManager pool) throws Exception {

        String path = url + "/clinicsector?id=eq." + clinicSector.getId() + "&uuid=eq." + clinicSector.getUuid().replaceAll(" ","%20");
        HttpResponse httpResponse = null;
        String response = null;

        String clinicJSONObject = "{\"code\": \"" + clinicSector.getCode() + "\", \"sectorname\": \"" + clinicSector.getSectorname() + "\", "
                + "\"clinicsectortype\":\"" + clinicSector.getClinicSectorType().getId() + "\", \"telephone\":\"" + clinicSector.getTelephone() + "\"}";

        StringEntity inputAddPatient = new StringEntity(clinicJSONObject, "UTF-8");
        inputAddPatient.setContentType("application/json");

        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            httpResponse = ApiAuthRest.postgrestRequestPatch(path, inputAddPatient, token, pool);

            if (httpResponse != null) {
                if (((float) httpResponse.getStatusLine().getStatusCode() / 200) >= 1.5)
                    response = "Falha no POSTGREST PUT - Code:" + httpResponse.getStatusLine().getStatusCode();
                else
                    response = "POSTGREST PUT efectiado com sucesso - Code:" + httpResponse.getStatusLine().getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    public static void restPostPatients(Session sess, String url, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        List<SyncTempPatient> syncTempPatients = AdministrationManager.getAllSyncTempPatientReadyToSend(sess);
        String result = "";
        String resultPut = "";
        boolean confirmed = false;
        if (syncTempPatients.isEmpty()) {
            log.trace(new Date() + " [FARMAC] INFO - Nenhumm paciente foi encontrado para referir");
            log.info(new Date() + " [FARMAC] INFO - Nenhumm paciente foi encontrado para referir");
        } else
            for (SyncTempPatient patientSync : syncTempPatients) {
                Session session = HibernateUtil.getNewSession();
                confirmed = false;
                try {
                    session.beginTransaction();
                    result = restPostPatient(url, patientSync,pool);
                        if(result.contains("409")) {
                            resultPut = restPutPatient(url, patientSync, pool);
                            if (resultPut.contains("Falha")) {
                                log.error(new Date() + ": Ocorreu um erro ao gravar o paciente com nid " + patientSync.getPatientid() + " Erro: " + resultPut);
                            } else {
                                confirmed = true;
                                log.info(new Date() + ": Actualizou o paciente com nid " + patientSync.getPatientid() + " - Resultado: " + resultPut);
                            }
                        } else {
                            if (result.contains("Falha")) {
                                log.error(new Date() + ": Ocorreu um erro ao gravar o paciente com nid " + patientSync.getPatientid() + " Erro: " + result);
                            } else {
                                confirmed = true;
                                log.info(new Date() + ":Paciente com nid " + patientSync.getPatientid() + " enviado com sucesso (" + result + ")");
                            }
                        }

                        if(confirmed){
                            patientSync.setSyncstatus('E');
                            AdministrationManager.saveSyncTempPatient(sess, patientSync);
                            session.getTransaction().commit();
                            session.flush();
                            session.clear();
                            session.close();
                        }
                    break;
                } catch (Exception e) {
                    session.getTransaction().rollback();
                    session.close();
                    log.error(e);
                } finally {
                    continue;
                }
            }
    }

    public static void restPostEpisodes(Session sess, String url, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        List<SyncEpisode> syncTempEpisodes = EpisodeManager.getAllSyncTempEpiReadyToSend(sess);
        String result = "";
        if (!iDARTUtil.arrayHasElements(syncTempEpisodes))
            log.trace(new Date() + " [FARMAC] INFO - Nenhumm episodio foi encontrado para enviar");
        else
            for (SyncEpisode syncEpisode : syncTempEpisodes) {
                Session session = HibernateUtil.getNewSession();
                try {
                    session.beginTransaction();
                    result = restPostEpisode(url, syncEpisode, pool);
                    if (result.contains("Falha") && !result.contains("409")) {
                        log.error(new Date() + ": Ocorreu um erro ao gravar o episodio do paciente com uuid " + syncEpisode.getPatientUUID() + " Erro: " + result);
                    } else {
                        log.info(new Date() + ":Episodio do Paciente com uuid " + syncEpisode.getPatientUUID() + " enviado com sucesso (" + result + ")");
                        log.trace(new Date() + ":Episodio do Paciente com uuid " + syncEpisode.getPatientUUID() + " enviado com sucesso (" + result + ")");
                        syncEpisode.setSyncStatus('S');
                        EpisodeManager.updateSyncTempEpisode(syncEpisode);
                        session.getTransaction().commit();
                        session.flush();
                        session.clear();
                        session.close();
                    }
                    break;
                } catch (Exception e) {
                    session.getTransaction().rollback();
                    session.close();
                    log.error(e);
                } finally {
                    continue;
                }

            }
    }

    public static void restPostDispenses(Session sess, String url, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        List<SyncTempDispense> syncTempDispenses = AdministrationManager.getAllSyncTempDispenseReadyToSend(sess);

        String result = "";

        if (syncTempDispenses.isEmpty())
            log.trace(new Date() + " [FARMAC] INFO - Nenhum Levantamento de ARV de paciente foi encontrado para enviar");
        else
            for (SyncTempDispense dispenseSync : syncTempDispenses) {
                Session session = HibernateUtil.getNewSession();
                try {
                    session.beginTransaction();
                    result = restPostDispense(url, dispenseSync, pool);
                    if (result.contains("Falha")) {
                        log.error(new Date() + ": Ocorreu um erro ao enviar o Levantamento do paciente com nid " + dispenseSync.getPatientid() + " Erro: " + result);
                    } else {
                        log.info(new Date() + ": Levantamento do Paciente com nid " + dispenseSync.getPatientid() + " enviado com sucesso (" + result + ")");
                        log.trace(new Date() + ": Levantamento do Paciente com nid " + dispenseSync.getPatientid() + " enviado com sucesso (" + result + ")");
                        dispenseSync.setSyncstatus('E');
                        AdministrationManager.saveSyncTempDispense(sess, dispenseSync);
                        session.getTransaction().commit();
                        session.flush();
                        session.clear();
                        session.close();
                    }
                    break;
                } catch (Exception e) {
                    session.getTransaction().rollback();
                    session.close();
                    log.error(e.getMessage());
                } finally {

                    continue;
                }

            }
    }

    public static void restPostLocalDispenses(Session sess, String url, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        List<SyncTempDispense> syncTempDispenses = AdministrationManager.getAllLocalSyncTempDispenseReadyToSend(sess);

        String result = "";
        String resultPut = "";
        boolean conformed = false;
        if (syncTempDispenses.isEmpty())
            log.trace(new Date() + " [US] INFO - Nenhum último levantamento de ARV de paciente foi encontrado para enviar");
        else
            for (SyncTempDispense dispenseSync : syncTempDispenses) {
                conformed = false;
                Session session = HibernateUtil.getNewSession();
                try {
                    session.beginTransaction();
                    result = restPostDispense(url, dispenseSync, pool);
                    if(result.contains("409")) {
                        resultPut = restPutDispense(url, dispenseSync, pool);
                        if (resultPut.contains("Falha")) {
                            log.error(new Date() + ": Ocorreu um erro ao gravar o Levantamento do Paciente com nid " + dispenseSync.getPatientid() + " Erro: " + resultPut);
                        } else {
                            conformed = true;
                            log.info(new Date() + ": Actualizou o Levantamento do paciente com nid " + dispenseSync.getPatientid() + " - Resultado: " + resultPut);
                        }
                    } else
                    if (result.contains("Falha")) {
                        log.error(new Date() + ": Ocorreu um erro ao enviar o Levantamento do paciente com nid " + dispenseSync.getPatientid() + " Erro: " + result);
                    } else {
                        conformed = true;
                        log.info(new Date() + ": Levantamento do Paciente com nid " + dispenseSync.getPatientid() + " enviado com sucesso (" + result + ")");
                    }

                    if(conformed){
                        dispenseSync.setSyncstatus('M');
                        AdministrationManager.saveSyncTempDispense(sess, dispenseSync);
                        session.getTransaction().commit();
                        session.flush();
                        session.clear();
                        session.close();
                    }
                    break;
                } catch (Exception e) {
                    session.getTransaction().rollback();
                    session.close();
                    log.error(e.getMessage());
                } finally {

                    continue;
                }

            }
    }

    public static void restPostLocalClinic(Session sess, String url, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {

        Clinic clinic = AdministrationManager.getMainClinic(sess);

        String result = "";
        String resultPut = "";
                Session session = HibernateUtil.getNewSession();
                try {
                    session.beginTransaction();
                    result = restPostClinic(url, clinic, pool);
                    if(result.contains("409")) {
                            log.info(new Date() + ": Farmacia " + clinic.getClinicName() + " ja existe - Resultado: " + resultPut);
                    } else
                    if (result.contains("Falha")) {
                        log.error(new Date() + ": Ocorreu um erro ao enviar a farmacia " + clinic.getClinicName() + " Erro: " + result);
                    } else {
                        log.info(new Date() + ": Farmacia " + clinic.getClinicName() + " enviado com sucesso (" + result + ")");
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
    }

    public static String restPostLocalClinicSector(Session sess, String url, PoolingHttpClientConnectionManager pool, List<ClinicSector> clinicSectors ) throws UnsupportedEncodingException {

        String result = "";
        String resultPut = "";
        String devolveResultado = "";
        if (clinicSectors.isEmpty())
            log.trace(new Date() + " [US] INFO - Nenhum sector clinico encontrado para enviar");
        else {
            for (ClinicSector clinicSector : clinicSectors) {
                try {
                    result = restPostSectorClinico(url, clinicSector, pool);
                    if (result.contains("409")) {
                        resultPut = restPutClinicSector(url, clinicSector, pool);
                        if (resultPut.contains("Falha")) {
                            devolveResultado.concat(new Date() + ": Ocorreu um erro ao gravar o Sector Clinico " + clinicSector.getSectorname() + " Erro: " + resultPut + "\n");
                            log.error(new Date() + ": Ocorreu um erro ao gravar o Sector Clinico " + clinicSector.getSectorname() + " Erro: " + resultPut);
                        } else {
                            devolveResultado.concat(new Date() + ": Actualizou o Sector Clinico " + clinicSector.getSectorname() + " - Resultado: " + resultPut + "\n");
                            log.info(new Date() + ": Actualizou o Sector Clinico " + clinicSector.getSectorname() + " - Resultado: " + resultPut);
                        }
                    } else if (result.contains("Falha")) {
                        devolveResultado.concat(new Date() + ": Ocorreu um erro ao enviar o Sector Clinico " + clinicSector.getSectorname() + " Erro: " + result + "\n");
                        log.error(new Date() + ": Ocorreu um erro ao enviar o Sector Clinico " + clinicSector.getSectorname() + " Erro: " + result);
                    } else {
                        devolveResultado.concat(new Date() + ": Sector Clinico " + clinicSector.getSectorname() + " enviado com sucesso (" + result + ") \n");
                        log.info(new Date() + ": Sector Clinico " + clinicSector.getSectorname() + " enviado com sucesso (" + result + ")");
                    }
                    break;
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {

                    continue;
                }

            }
        }
            return devolveResultado;
    }

    public static void setPatientsFromRest(Session session) {


            List<SyncTempPatient> syncTempPatients = AdministrationManager.getAllSyncTempPatientReadyToSend(session);

            if (!syncTempPatients.isEmpty()) {

                for (SyncTempPatient patient : syncTempPatients) {
                    Session sess = HibernateUtil.getNewSession();
                    try {
                        sess.beginTransaction();
                        DadosPacienteFarmac.InserePaciente(sess, patient);
                        patient.setSyncstatus('I');
                        AdministrationManager.saveSyncTempPatient(sess, patient);
                        sess.getTransaction().commit();
                        sess.flush();
                        sess.clear(); // Clearing the session object
                        sess.close();
                        break;
                    } catch (Exception e) {
                        sess.getTransaction().rollback();
                        sess.close();
                        log.error("Erro ao gravar informacao do Paciente [" + patient.getFirstnames() + " " + patient.getLastname() + " com NID: " + patient.getPatientid() + "]");
                    } finally {
                        continue;
                    }
                }
            } else {
                log.info(new Date() + ": [FARMAC] INFO - Nenhumm paciente referido para esta FARMAC foi encontrado");
                log.trace(new Date() + ": [FARMAC] INFO - Nenhumm paciente referido para esta FARMAC foi encontrado");
            }
    }

    public static void setDispensesFromRest(Session session) {

        List<SyncTempDispense> syncTempDispenses = AdministrationManager.getAllSyncTempDispenseReadyToSend(session);

        if (!syncTempDispenses.isEmpty()) {

            for (SyncTempDispense dispense : syncTempDispenses) {
                Session sess = HibernateUtil.getNewSession();
                try {
                    sess.beginTransaction();
                    Prescription prescription = DadosPacienteFarmac.getPatientPrescritionFarmac(dispense);
                    SyncTempPatient patient = AdministrationManager.getSyncTempPatienByUuidAndClinicUuid(sess, dispense.getUuidopenmrs(), dispense.getMainclinicuuid());
                    DadosPacienteFarmac.saveDispenseFarmacQty0(prescription, dispense);
                    DadosPacienteFarmac.setDispenseRestOpenmrs(sess, prescription, dispense);
                    dispense.setSyncstatus('I');

                    AdministrationManager.saveSyncTempDispense(sess, dispense);
                    if(patient != null)
                        if(patient.getEstadopaciente().contains("Faltoso") || patient.getEstadopaciente().contains("Abandono")){
                            patient.setExclusaopaciente(true);
                            AdministrationManager.saveSyncTempPatient(sess, patient);
                        }
                    sess.getTransaction().commit();
                    sess.flush();
                    sess.clear(); // Clearing the session object
                    sess.close();
                    break;
                } catch (Exception e) {
                    sess.getTransaction().rollback();
                    sess.close();
                    e.printStackTrace();
                    log.error("Erro ao gravar levantamento do Paciente com NID: [" + dispense.getPatientid() + "]");
                } finally {
                    continue;
                }
            }
        } else {
            log.info(new Date() + ": [US] INFO - Nenhumm levantamento enviado para esta US foi encontrado");
            log.trace(new Date() + ": [US] INFO - Nenhumm levantamento enviado para esta US foi encontrado");
        }
    }

    public static void setEpisodesFromRest(Session session, Clinic clinic) {

        List<SyncEpisode> syncTempEpisodes = EpisodeManager.getAllSyncEpisodesReadyToSave(session);

        if (iDARTUtil.arrayHasElements(syncTempEpisodes)) {

            for (SyncEpisode syncEpisode : syncTempEpisodes) {
                Session sess = HibernateUtil.getNewSession();
                sess.beginTransaction();

                Patient relatedPatient = PatientManager.getPatientfromUuid(sess, syncEpisode.getPatientUUID());

                if (relatedPatient != null) {
                    try {
                        Episode mostRecentEpisode = relatedPatient.getMostRecentEpisode();

                        if (mostRecentEpisode.isOpen()) {
                            mostRecentEpisode.closeFromSyncEpisode(syncEpisode);
                            EpisodeManager.saveEpisode(sess, mostRecentEpisode);
                        }

                        Episode newEpisode = Episode.generateFromSyncEpisode(syncEpisode, relatedPatient, clinic);
                        syncEpisode.setSyncStatus('U');
                        EpisodeManager.saveEpisode(sess, newEpisode);
                        EpisodeManager.updateSyncTempEpisode(syncEpisode);

                        relatedPatient.addEpisode(newEpisode);
                        relatedPatient.updateClinic();
                        PatientManager.updatePatient(sess, relatedPatient);

                        sess.getTransaction().commit();
                        sess.flush();
                        sess.clear();
                        sess.close();
                    } catch (Exception e) {
                        sess.getTransaction().rollback();
                        sess.close();
                        log.error("Erro ao gravar episodio do Paciente com NID: [" + relatedPatient.getPatientId() + "]");
                    }
                } else {
                    log.error("Erro paciente nao encontrado: [" + syncEpisode.getPatientUUID() + "]");
                }
            }
        } else {
            log.info(new Date() + ": [US] INFO - Nenhumm episodio enviado para esta US foi encontrado");
            log.trace(new Date() + ": [US] INFO - Nenhumm episodio enviado para esta US foi encontrado");
        }
    }

    public static List<Clinic> restGeAllClinicByProvinceAndDistrictAndFacilityType(String url, String province, String district, String facilitytype, Session session, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;
        List<Clinic> clinicList = new ArrayList<>();
        List<Clinic> localClinics = AdministrationManager.getClinics(session);
        String path = url + "/clinic?province=eq." + province + "&district=eq." + district + "&facilitytype=eq." + facilitytype;
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            response = ApiAuthRest.postgrestRequestGetAll(path, token, pool);
            InputStream in = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();

            Clinic clinic = null;
            String line = null;
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;


            while ((line = reader.readLine()) != null) {
                str.append(line + "\n");
                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line;

                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        clinic = gson.fromJson(jsonObj.toString(), Clinic.class);
                        clinic.setFacilityType(jsonObj.getString("facilitytype"));
                        clinic.setClinicName(jsonObj.getString("clinicname"));
                        clinic.setSubDistrict(jsonObj.getString("subdistrict"));
                        boolean existClinic = false;
                        for (Clinic localClinic : localClinics) {
                            if (localClinic.getUuid().equals(clinic.getUuid())) {
                                existClinic = true;
                                break;
                            }
                        }

                        if (!existClinic)
                            clinicList.add(clinic);
                       // break;
                    } catch (Exception e) {
                        log.error(" Ocorreu um erro ao adicionar a clinic [" + clinic.getClinicName() + "]");
                    } finally {
                       continue;
                    }
                }
            }
            reader.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clinicList;
    }

    public static List<Drug> restGeAllDrugsByDeseaseTypeAndStatus(String url, String deseaseType, boolean status, Session session, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;
        List<Drug> drugList = new ArrayList<>();
        List<Drug> localDrugs = AdministrationManager.getDrugs(session);
        String path = url + "/drug?select=*,form(*)&tipodoenca=eq." + deseaseType + "&active=eq." + status;
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            StringBuilder str = ApiAuthRest.postgrestRequestGetBuffer(path, token, pool);
            Drug drug = new Drug();
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;

            String[] lines = str.toString().split("\\n");

            for (String line : lines) {
                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line.replaceFirst("form", "formid");

                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        drug = gson.fromJson(jsonObj.toString(), Drug.class);
                        drug.setDispensingInstructions1(jsonObj.getString("dispensinginstructions1"));
                        drug.setDispensingInstructions2(jsonObj.getString("dispensinginstructions2"));
                        drug.setPackSize(jsonObj.getInt("packsize"));
                        drug.setSideTreatment(jsonObj.getString("sidetreatment").charAt(0));
                        drug.setDefaultAmnt(jsonObj.getInt("defaultamnt"));
                        drug.setTipoDoenca(jsonObj.getString("tipodoenca"));
                        drug.setDefaultTimes(jsonObj.getInt("defaulttimes"));

                        boolean existDrug = false;
                        for (Drug localDrug : localDrugs) {
                            if (localDrug.getAtccode().equals(drug.getAtccode())) {
                                existDrug = true;
                                break;
                            }
                        }
                        if (!existDrug)
                            drugList.add(drug);
                    } catch (Exception e) {
                        log.error(" Ocorreu um erro ao adicionar o Medicamento [" + drug.getName() + "] " + e.getMessage());
                    } finally {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return drugList;
    }

    public static List<RegimeTerapeutico> restGeAllRegimenByStatus(String url, boolean status, Session session, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;
        List<RegimeTerapeutico> regimeTerapeuticoList = new ArrayList<>();
        List<RegimeTerapeutico> localRegimeTerapeutico = AdministrationManager.getRegimeTerapeutico(session);
        String path = url + "/regimeterapeutico?active=eq." + status;
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            StringBuilder str = ApiAuthRest.postgrestRequestGetBuffer(path, token, pool);
            RegimeTerapeutico regimeTerapeutico = new RegimeTerapeutico();
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;

            String[] lines = str.toString().split("\\n");

            for (String line : lines) {
                str.append(line + "\n");
                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line;

                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        regimeTerapeutico = gson.fromJson(jsonObj.toString(), RegimeTerapeutico.class);

                        boolean existRegimen = false;
                        for (RegimeTerapeutico localRegimen : localRegimeTerapeutico) {
                            if (localRegimen.getCodigoregime().equalsIgnoreCase(regimeTerapeutico.getCodigoregime())) {
                                existRegimen = true;
                                break;
                            }
                        }

                        if (!existRegimen)
                            regimeTerapeuticoList.add(regimeTerapeutico);
                        break;
                    } catch (Exception e) {
                        log.error(" Ocorreu um erro ao adicionar o Medicamento [" + regimeTerapeutico.getRegimeesquema() + "] " + e.getMessage());
                    } finally {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return regimeTerapeuticoList;
    }

    public static List<RegimenDrugs> restGeAllRegimenDrugsByRegimen(String url, RegimeTerapeutico regimeTerapeutico, Session session, PoolingHttpClientConnectionManager pool) {
        HttpResponse response = null;
        List<RegimenDrugs> regimenDrugsList = new ArrayList<>();
        String path = url + "/regimendrugs?select=*,drug(*,form(*))&regimen=eq." + regimeTerapeutico.getRegimeid();
        try {
            String token = restGetpermission(url, CentralizationProperties.rest_access_username, CentralizationProperties.rest_access_password, pool);

            StringBuilder str = ApiAuthRest.postgrestRequestGetBuffer(path, token, pool);
            RegimenDrugs regimenDrugs = new RegimenDrugs();
            String objectString = null;
            JSONObject jsonObj = null;
            Gson gson = null;

            String[] lines = str.toString().split("\\n");

            for (String line : lines) {
                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                objectString = line.replaceFirst("drug", "drugid");
                objectString = objectString.replaceFirst("regimen", "regimenid");
                objectString = objectString.replaceFirst("form", "formid");

                if (objectString.contains("{")) {
                    jsonObj = new JSONObject(objectString);
                    gson = new Gson();
                    try {
                        regimenDrugs = gson.fromJson(jsonObj.toString(), RegimenDrugs.class);
                        regimenDrugs.getDrug().setDispensingInstructions1(jsonObj.getJSONObject("drug").getString("dispensinginstructions1"));
                        regimenDrugs.getDrug().setDispensingInstructions2(jsonObj.getJSONObject("drug").getString("dispensinginstructions2"));
                        regimenDrugs.getDrug().setPackSize(jsonObj.getJSONObject("drug").getInt("packsize"));
                        regimenDrugs.getDrug().setSideTreatment(jsonObj.getJSONObject("drug").getString("sidetreatment").charAt(0));
                        regimenDrugs.getDrug().setDefaultAmnt(jsonObj.getJSONObject("drug").getInt("defaultamnt"));
                        regimenDrugs.getDrug().setTipoDoenca(jsonObj.getJSONObject("drug").getString("tipodoenca"));
                        regimenDrugs.getDrug().setDefaultTimes(jsonObj.getJSONObject("drug").getInt("defaulttimes"));
                        regimenDrugsList.add(regimenDrugs);
                    } catch (Exception e) {
//                       log.trace(" Ocorreu um erro ao adicionar o Medicamento [" + regimenDrugs.getDrug().getName() + "] do Regime Terapeutico [" +regimeTerapeutico.getRegimeesquema()+" ]"+ e.getMessage());
                        log.error(e.getMessage());
                    } finally {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        return regimenDrugsList;
    }


    public static void setCentralPatients(Session session) {

            List<SyncTempPatient> syncTempPatients = AdministrationManager.getAllSyncTempPatientReadyToSave(session);

            if (!syncTempPatients.isEmpty()) {

                for (SyncTempPatient patient : syncTempPatients) {
                    Session sess = HibernateUtil.getNewSession();
                    try {
                        sess.beginTransaction();
                        DadosPacienteFarmac.InserePaciente(sess, patient);
                        patient.setSyncstatus('E');
                        AdministrationManager.saveSyncTempPatient(sess, patient);
                        sess.getTransaction().commit();
                        sess.flush();
                        sess.clear();
                        sess.close();
                        break;
                    } catch (Exception e) {
                        sess.getTransaction().rollback();
                        sess.close();
                        log.error(new Date() + ": [Central] INFO - Erro ao gravar informacao do Paciente [" + patient.getFirstnames() + " " + patient.getLastname() + " com NID: " + patient.getPatientid() + "] provrniente de " + patient.getMainclinicname());
                    } finally {
                        continue;
                    }
                }
            } else {
                log.trace(new Date() + ": [Central] INFO - Nenhumm paciente referido para FARMAC foi encontrado");
            }
    }

    public static void setCentralDispenses(Session sess) {

        List<SyncTempDispense> syncTempDispenses = AdministrationManager.getAllSyncTempDispenseReadyToSave(sess);

        if (!syncTempDispenses.isEmpty()) {

            for (SyncTempDispense dispense : syncTempDispenses) {
                Session session = HibernateUtil.getNewSession();

                try {
                    session.beginTransaction();
                    Prescription prescription = DadosPacienteFarmac.getPatientPrescritionFarmac(dispense);
                    if (prescription != null) {
                        DadosPacienteFarmac.saveDispenseFarmacQty0(prescription, dispense);
                        dispense.setSyncstatus('E');
                    } else
                        dispense.setSyncstatus('W');
                    AdministrationManager.saveSyncTempDispense(session, dispense);
                    session.getTransaction().commit();
                    session.flush();
                    session.clear();
                    session.close();
                    break;
                } catch (Exception e) {
                    session.getTransaction().rollback();
                    session.close();
                    log.error(new Date() + ": [Central] INFO - Erro ao gravar levantamento do Paciente com NID: [" + dispense.getPatientid() + "] proveniente de " + dispense.getMainclinicname());
                } finally {
                    continue;
                }
            }
        } else {
            log.trace(new Date() + ": [Central] INFO - Nenhumm levantamento enviado para US foi encontrado");
        }
    }

    public static void setPatientFromClinicSector(Session session) {
        Patient localPatient = null;
        try {
            List<SyncMobilePatient> mobilePatients = AdministrationManager.getAllSyncMobilePatientReadyToSave(session);

            if (!mobilePatients.isEmpty()) {

                for (SyncMobilePatient patient : mobilePatients) {
                    Session sess = HibernateUtil.getNewSession();

                    ClinicSector clinicSector = AdministrationManager.getClinicSectorFromUUID(sess, patient.getClinicsectoruuid());

                    if (patient.getUuid() != null)
                        localPatient = PatientManager.getPatientfromUuid(sess, patient.getUuid());
                    else
                        localPatient = PatientManager.getPatient(sess, patient.getPatientid());

                    try {
                        sess.beginTransaction();

                        if (localPatient == null) {
                            localPatient = DadosPacienteFarmac.setPatientFromClinicSector(patient);
                            RestClient.saveOpenmrsPatient(localPatient, sess);
                        }

                        List<PatientSector> patientClinicSector = PatientManager.patientIsOpenInClinicSector(sess, localPatient);

                        if (patientClinicSector.size() > 0) {
                            log.trace(new Date() + ": [US] INFO - Paciente esta sendo atendido na Paragem Unica" + patientClinicSector.get(0).getClinicsector().getSectorname());
                            log.error(new Date() + ": [US] INFO - Paciente esta sendo atendido na Paragem Unica" + patientClinicSector.get(0).getClinicsector().getSectorname());
                        } else {
                            setPatientToSector(sess, localPatient, clinicSector, patient.getEnrolldate());
                            setEpisodeToPatientSector(sess, localPatient, clinicSector, patient.getEnrolldate());
                            patient.setSyncstatus('S');
                            PatientManager.saveSyncMobilePatient(sess, patient);
                        }
                        sess.getTransaction().commit();
                        sess.flush();
                        sess.clear();
                        sess.close();
                        break;
                    } catch (Exception e) {
                        sess.getTransaction().rollback();
                        sess.close();
                        log.info(new Date() + ": [US] INFO - Erro ao gravar informacao do Paciente [" + patient.getFirstnames() + " " + patient.getLastname() + " com NID: " + patient.getPatientid() + "] provrniente de " + clinicSector.getSectorname());
                        log.error(new Date() + ": [US] INFO - Erro ao gravar informacao do Paciente [" + patient.getFirstnames() + " " + patient.getLastname() + " com NID: " + patient.getPatientid() + "] provrniente de " + clinicSector.getSectorname());
                    } finally {
                        continue;
                    }
                }
            } else {
                log.info(new Date() + ": [US] INFO - Nenhumm paciente enviado da Paragem Unica foi encontrado");
                log.trace(new Date() + ": [US] INFO - Nenhumm paciente enviado da Paragem Unica foi encontrado");
            }
        } catch (Exception e) {
            log.error(new Date() + ": [US] INFO - ERRO GERAL ao gravar pacientes enviado da Paragem Unica");
            log.trace(new Date() + ": [US] INFO - ERRO GERAL ao gravar pacientes enviado da Paragem Unica");
        }

    }

    private static void setPatientToSector(Session sess, Patient patientToSector, ClinicSector clinicSector, Date enrollDate) {

        PatientSector patientSector = new PatientSector();
        patientSector.setPatient(patientToSector);
        patientSector.setClinicsector(clinicSector);
        patientSector.setStartdate(enrollDate);
        sess.save(patientSector);

    }

    private static void setEpisodeToPatientSector(Session sess, Patient patientToSector, ClinicSector clinicSector, Date enrollDate){
        Clinic clinic = AdministrationManager.getMainClinic(sess);
        Episode episode = new Episode();
        episode.setPatient(patientToSector);
        episode.setClinic(clinic);
        episode.setStartReason("Referrido para P.U");
        episode.setStartNotes("Referrido para Paragem Unica: ["+ clinicSector.getSectorname()+"]");
        episode.setStartDate(enrollDate);
        sess.save(episode);
    }

    public static String restGetpermission(String url, String username, String pass, PoolingHttpClientConnectionManager pool) throws UnsupportedEncodingException {
        String path = url + "/rpc/login";
        StringBuilder httpResponse = null;
        JSONObject jsonObj = null;
        String result = null;
        String updateStatus = "{\"username\":\"" + username + "\"," +
                "\"pass\":\"" + pass + "\"}";

        StringEntity inputCheckAccess = new StringEntity(updateStatus, "UTF-8");
        inputCheckAccess.setContentType("application/json");

        try {
            httpResponse = ApiAuthRest.postgrestRequestPostBuffer(path, inputCheckAccess, pool);

            String[] lines = httpResponse.toString().split("\\n");

            for (String line : lines) {
                if (line.startsWith("[{"))
                    line = line.replace("[{", "{");
                if (line.endsWith("}]"))
                    line = line.replace("}]", "}");

                if (line.contains("{")) {
                    jsonObj = new JSONObject(line);
                    try {
                        result = jsonObj.get("token").toString();
                    } catch (Exception e) {
                        log.error(e.getMessage());
                    } finally {
                        continue;
                    }
                }

            }
//           log.trace(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}

