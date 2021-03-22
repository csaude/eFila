package org.celllife.idart.gui.patient.tabs;

import model.manager.PatientManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.JdbcProperties;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.Appointment;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.User;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.misc.GenericTab;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.widget.DateButton;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.iDARTUtil;
import org.celllife.idart.rest.ApiAuthRest;
import org.celllife.idart.rest.utils.RestClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.Session;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.celllife.idart.rest.ApiAuthRest.getServerStatus;

public class TreatmentManagementTab extends GenericTab implements IPatientTab {

    private DateButton btnNextAppointment;
    private Session hSession;
    private boolean isPatientActive;
    private final Logger log = Logger.getLogger(getClass());
    private TabFolder parent;
    private int style;
    private Text txtTreatmentSupporterName;
    private Text txtTreatmentSupporterPhone;

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.util.IPatientTab#changesMade(org.celllife.idart.database.hibernate.Patient)
     */
    @Override
    public boolean changesMade(Patient patient) {
        boolean noChangesMade = true;
        noChangesMade &= patient.getNextOfKinName().trim().equals(
                txtTreatmentSupporterName.getText().trim());
        noChangesMade &= patient.getNextOfKinPhone().trim().equals(
                txtTreatmentSupporterPhone.getText().trim());

        Date theNewAppointmentDate = btnNextAppointment.getDate();
        Appointment currentAppointment = PatientManager
                .getLatestActiveAppointmentForPatient(patient);

        if (currentAppointment != null) {
            Date theLatestAppointmentDate = currentAppointment
                    .getAppointmentDate();
            if (theLatestAppointmentDate == null) {
                log
                        .error("appointment extists, but next appointment date is null!");
                noChangesMade = false;
            } else if (theNewAppointmentDate != null
                    && theNewAppointmentDate
                    .compareTo(theLatestAppointmentDate) != 0) {
                noChangesMade = false;
            }
        } else if (theNewAppointmentDate != null) {
            noChangesMade = false;
        }

        return !noChangesMade;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.util.IPatientTab#clear()
     */
    @Override
    public void clear() {
        btnNextAppointment.setText(Messages.getString("patient.next.encounter"));
        txtTreatmentSupporterName.setText("");
        txtTreatmentSupporterPhone.setText("");
        btnNextAppointment.setDate(null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.misc.IGenericTab#create()
     */
    @Override
    public void create() {
        tabItem = new TabItem(parent, style);
        tabItem.setText("  " + Messages.getString("patient.treatment.management") + "  ");
        createGrpTreatmentManagement();
    }

    /**
     * This method initializes grpTreatmentSupporter
     */
    private void createGrpTreatmentManagement() {

        Group grpTreatmentSupporter = new Group(tabItem.getParent(), SWT.NONE);
        grpTreatmentSupporter.setBounds(new Rectangle(3, 3, 750, 140));
        grpTreatmentSupporter.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));
        tabItem.setControl(grpTreatmentSupporter);

        Label lblNextAppointment = new Label(grpTreatmentSupporter, SWT.NONE);
        lblNextAppointment.setBounds(new Rectangle(6, 94, 114, 18));
        lblNextAppointment.setText("  " + Messages.getString("next.encounter") + ":");
        lblNextAppointment.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        btnNextAppointment = new DateButton(grpTreatmentSupporter,
                DateButton.ZERO_TIMESTAMP,
                null);
        btnNextAppointment.setBounds(new Rectangle(195, 90, 200, 25));
        btnNextAppointment.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnNextAppointment.setText(Messages.getString("patient.next.encounter"));


        // Treatment Supporter Name
        Label lblTreatmentSupporterName = new Label(grpTreatmentSupporter,
                SWT.NONE);
        lblTreatmentSupporterName.setBounds(new Rectangle(6, 32, 179, 20));
        lblTreatmentSupporterName.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));
        lblTreatmentSupporterName.setText("  Nome do Confidente:");

        txtTreatmentSupporterName = new Text(grpTreatmentSupporter, SWT.BORDER);
        txtTreatmentSupporterName.setBounds(new Rectangle(194, 32, 200, 20));
        txtTreatmentSupporterName.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));

        // Treatment Supporter Phone
        Label lblTreatmentSupporterPhone = new Label(grpTreatmentSupporter,
                SWT.NONE);
        lblTreatmentSupporterPhone.setBounds(new Rectangle(6, 62, 179, 20));
        lblTreatmentSupporterPhone.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));
        lblTreatmentSupporterPhone.setText("  Telefone do Confidente:");

        txtTreatmentSupporterPhone = new Text(grpTreatmentSupporter, SWT.BORDER);
        txtTreatmentSupporterPhone.setBounds(new Rectangle(194, 62, 200, 20));
        txtTreatmentSupporterPhone.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));

    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.util.IPatientTab#enable(boolean,
     *      org.eclipse.swt.graphics.Color)
     */
    @Override
    public void enable(boolean enable, Color color) {
        txtTreatmentSupporterName.setEnabled(enable);
        txtTreatmentSupporterPhone.setEnabled(enable);
        btnNextAppointment.setEnabled(enable);
        if (enable) {
            btnNextAppointment.setEnabled(isPatientActive);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.util.IPatientTab#populate(org.hibernate.Session,
     *      org.celllife.idart.database.hibernate.Patient, boolean)
     */
    @Override
    public void loadPatientDetails(Patient patient, @SuppressWarnings("hiding")
            boolean isPatientActive) {
        this.isPatientActive = isPatientActive;
        txtTreatmentSupporterName.setText(patient.getNextOfKinName());//get patient supporter name from rest
        txtTreatmentSupporterPhone.setText(patient.getNextOfKinPhone());//get patient contact from rest

        Date theDateExpected = null;
        Appointment latestApp = PatientManager
                .getLatestActiveAppointmentForPatient(patient);
        if (latestApp != null) {
            theDateExpected = latestApp.getAppointmentDate();
        }
        if (theDateExpected != null) {
            btnNextAppointment.setText(iDARTUtil.format(theDateExpected));
        } else {
            btnNextAppointment.setText("Next App Date");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.misc.IGenericTab#setParent(org.eclipse.swt.widgets.TabFolder)
     */
    @Override
    public void setParent(TabFolder parent) {
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.util.IPatientTab#setPatientDetails(org.celllife.idart.database.hibernate.Patient)
     */
    @Override
    public void setPatientDetails(Patient patient) {

        try {
            if (getServerStatus(JdbcProperties.urlBase).contains("Red"))
                log.trace(new Date() + " :Servidor OpenMRS offline, verifique a conexao com OpenMRS ou contacte o administrador");
            else {

                User currentUser = LocalObjects.getUser(HibernateUtil.getNewSession());

                assert currentUser != null;
                if (ApiAuthRest.loginOpenMRS(currentUser)) {

                RestClient restClient = new RestClient();

                String nidRest = restClient.getOpenMRSResource(iDartProperties.REST_GET_PATIENT + StringUtils.replace(patient.getPatientId(), " ", "%20"));

                JSONObject _jsonObject = new JSONObject(nidRest);

                String nidUuid = null;

                JSONArray _jsonArray = (JSONArray) _jsonObject.get("results");

                for (int i = 0; i < _jsonArray.length(); i++) {
                    JSONObject results = (JSONObject) _jsonArray.get(i);
                    nidUuid = (String) results.get("uuid");
                }

                String strNextOfKinName = restClient.getOpenMRSResource(iDartProperties.REST_OBS_PATIENT + nidUuid + iDartProperties.REST_CONCEPT +
                        iDartProperties.CONCEPT_CONFIDENT_NAME);

                JSONObject jSonNextOfKinName = new JSONObject(strNextOfKinName);

                JSONArray arrayNextOfKinName = (JSONArray) jSonNextOfKinName.get("results");

                String nomeDoConfidente = null;

                String strTreatmentSupporterName;

                for (int i = 0; i < arrayNextOfKinName.length(); i++) {
                    JSONObject results = (JSONObject) arrayNextOfKinName.get(i);
                    nomeDoConfidente = (String) results.get("display");
                }


                if (nomeDoConfidente == null) {
                    strTreatmentSupporterName = "";
                } else {
                    String[] arrayNomeDoConfidente = nomeDoConfidente.split(":");
                    strTreatmentSupporterName = arrayNomeDoConfidente[1].trim();
                }

                String strNextOfKinPhone = restClient.getOpenMRSResource(iDartProperties.REST_OBS_PATIENT + nidUuid + iDartProperties.REST_CONCEPT +
                        iDartProperties.CONCEPT_CONFIDENT_TELEPHONE_NUMBER);

                JSONObject jsonObject = new JSONObject(strNextOfKinPhone);

                JSONArray jsonArray = (JSONArray) jsonObject.get("results");

                String strTreatmentSupporterPhone = null;

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = (JSONObject) jsonArray.get(i);
                    strTreatmentSupporterPhone = (String) jsonObject2.get("display");
                }

                String cellNumber = null;
                cellNumber = strTreatmentSupporterPhone != null ? strTreatmentSupporterPhone.replaceAll("\\D+", "") : "";

                patient.setNextOfKinName(strTreatmentSupporterName);
                patient.setNextOfKinPhone(cellNumber);

            }else {
                log.error("O Utilizador "+currentUser.getUsername()+" não se encontra no OpenMRS ou serviço rest no OpenMRS não está  em funcionamento.");
            }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (btnNextAppointment.getDate() != null) {
            PatientManager.setNextAppointmentDate(hSession, patient,
                    btnNextAppointment.getDate());
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.misc.IGenericTab#setSession(org.hibernate.Session)
     */
    @Override
    public void setSession(Session session) {
        this.hSession = session;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.misc.IGenericTab#setStyle(int)
     */
    @Override
    public void setStyle(int SWTStyle) {
        this.style = SWTStyle;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.util.IPatientTab#submit(org.hibernate.Session,
     *      org.celllife.idart.database.hibernate.Patient)
     */
    @Override
    public void submit(Patient patient) {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.patient.IPatientTab#validateFields()
     */
    @Override
    public Map<String, String> validateFields(Patient patient) {
        // SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        // if (sdf.format(theNewAppointmentDate);
        Map<String, String> map = new HashMap<String, String>();
        if (btnNextAppointment.getDate() != null) {
            Date theNewAppointmentDate = btnNextAppointment.getDate();
            Calendar today = Calendar.getInstance();
            today.setTime(new Date());
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            boolean validNextAppointmentDate = (theNewAppointmentDate
                    .compareTo(today.getTime()) == 0 || theNewAppointmentDate
                    .after(today.getTime()));
            map.put("result", String.valueOf(validNextAppointmentDate));
            if (!validNextAppointmentDate) {
                map.put("title", Messages.getString("error.next.encounter.date"));
                map.put("message",
                        " " + Messages.getString("error.insert.future.next.encounter.date"));
            }
            return map;
        } else {
            map.put("result", String.valueOf(true));
            return map;
        }
    }
}