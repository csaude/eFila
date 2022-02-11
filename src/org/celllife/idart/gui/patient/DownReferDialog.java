package org.celllife.idart.gui.patient;

import model.manager.AdministrationManager;
import model.manager.PackageManager;
import org.apache.log4j.Logger;
import org.celllife.function.DateRuleFactory;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.gui.platform.GenericOthersGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.widget.DateButton;
import org.celllife.idart.gui.widget.DateInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static model.manager.TemporaryRecordsManager.savePackageDrugInfosFarmac;

public class DownReferDialog extends GenericOthersGui {

    private static final String infoText = "Quando uma farmacia refere um paciente, "
            + "ela ainda pode efectuar dispensas para o mesmo paciente. "
            + "Este paciente sera contabilizado no numero total de pacientes "
            + "em tratamento da farmacia de referencia. ";
    private final Patient patient;
    private CCombo cmbClinic;
    private CCombo cmbClinicSectorType;
    private CCombo cmbClinicSector;
    private Button rdBtnDownReferredDC;
    private Button rdBtnDownReferredDDD;
    private DateButton btnDownReferredDate;
    private Button btnYes;
    private final Date startDate;
    private Label label;
    private Label labelSectorType;
    private Label labelSector;
    private Label labelRefferedTimes;
    private Label labelReferredDate;

    public DownReferDialog(Shell parent, Session session, Patient patient) {
        super(parent, session);
        this.patient = patient;
        startDate = patient.getMostRecentEpisode().getStartDate();
    }


    /**
     * This method initializes compButtonTab
     */
    private void createGrpSelectDownRefferal() {

        Group grpAddOrConfigureUser = new Group(getShell(), SWT.NONE);
        grpAddOrConfigureUser.setBounds(new Rectangle(70, 80, 400, 50));

        rdBtnDownReferredDDD = new Button(grpAddOrConfigureUser, SWT.RADIO);
        rdBtnDownReferredDDD.setBounds(new Rectangle(20, 12, 160, 30));
        rdBtnDownReferredDDD.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnDownReferredDDD.setText("Para Dispensa Discentralizada");
        rdBtnDownReferredDDD.setSelection(true);
        rdBtnDownReferredDDD.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                if (rdBtnDownReferredDDD.getSelection()) {
                    label.setVisible(true);
                    cmbClinic.setVisible(true);
                    labelSectorType.setVisible(false);
                    cmbClinicSectorType.setVisible(false);
                    labelSector.setVisible(false);
                    cmbClinicSector.setVisible(false);
                    cleanFields();
                }
            }
        });

        rdBtnDownReferredDC = new Button(grpAddOrConfigureUser, SWT.RADIO);
        rdBtnDownReferredDC.setBounds(new Rectangle(195, 12, 180, 30));
        rdBtnDownReferredDC.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnDownReferredDC.setText("Para Dispensa Comunitária");
        rdBtnDownReferredDC.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                if (rdBtnDownReferredDC.getSelection()) {
                    label.setVisible(false);
                    cmbClinic.setVisible(false);
                    labelSectorType.setVisible(true);
                    cmbClinicSectorType.setVisible(true);
                    labelSector.setVisible(true);
                    cmbClinicSector.setVisible(true);
                    cleanFields();
                }
            }
        });

    }

    @Override
    protected void createCompButtons() {
        Button btnSave = new Button(getCompButtons(), SWT.NONE);
        btnSave.setText("Gravar");
        btnSave.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnSave.setToolTipText("Pressione este botão para gravar a informação.");
        btnSave
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdSaveWidgetSelected();
                    }
                });

        Button btnCancel = new Button(getCompButtons(), SWT.NONE);
        btnCancel.setText("Cancelar");
        btnCancel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnCancel.setToolTipText("Pressione este botão para fechar esta janela.\n"
                + "A informação digitada será perdida.");
        btnCancel
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdCancelWidgetSelected();
                    }
                });
    }

    protected void cmdCancelWidgetSelected() {
        closeShell(false);
    }

    protected void cmdSaveWidgetSelected() {
        if (fieldsOk()) {
            doSave();
        }
    }

    private boolean fieldsOk() {

        String clinicName = cmbClinic.getText();
        String clinicSector = cmbClinicSector.getText();

        if (rdBtnDownReferredDDD.getSelection() && (clinicName == null || clinicName.isEmpty())) {
            showMessage(MessageDialog.ERROR, "Farmacia nao selecionada",
                    "Por Favor Seleccione a Farmacia.");
            return false;
        }

        if (rdBtnDownReferredDC.getSelection() && (clinicSector == null || clinicSector.isEmpty())) {
            showMessage(MessageDialog.ERROR, "Sector Clínico de referência não selecionado",
                    "Por Favor Seleccione o Sector Clínico de referência.");
            return false;
        }

        return true;
    }

    private void doSave() {
        Transaction tx = null;
        try {
            tx = getHSession().beginTransaction();
            Clinic mainClinic = AdministrationManager.getMainClinic(getHSession());
            Episode episode = patient.getMostRecentEpisode();
            Date date = btnDownReferredDate.getDate();
            episode.setStopDate(date);
            episode.setStopReason("Referido");

            String clinicName = "";
            String msgTxt = "";
            Clinic clinic = null;
            ClinicSector clinicSector = null;

            if(rdBtnDownReferredDDD.getSelection())
                clinicName = cmbClinic.getText();

            if(rdBtnDownReferredDC.getSelection())
                clinicName = cmbClinicSector.getText();

            episode.setStopNotes("Para " + clinicName);

            Episode newEpisode = new Episode();
            newEpisode.setPatient(patient);
            newEpisode.setStartDate(date);
            newEpisode.setStartNotes("Para " + clinicName);
            String startReason;
            if (btnYes.getSelection()) {
                startReason = "Referido para outra Farmacia";
            } else {
                startReason = "Voltou a ser referido para outra Farmacia";
            }
            newEpisode.setStartReason(startReason);
            patient.getEpisodes().add(newEpisode);

            if(rdBtnDownReferredDDD.getSelection()) {
                clinic = AdministrationManager.getClinic(getHSession(),clinicName);
                patient.setClinic(clinic);
                newEpisode.setClinic(clinic);
                msgTxt = " Referido para outra Farmacia " + clinicName;
            }

            if(rdBtnDownReferredDC.getSelection()){
                clinicSector =  AdministrationManager.getSectorByName(getHSession(), clinicName);
                newEpisode.setClinic(mainClinic);
                msgTxt = " Referido para Sector Clinico "+ clinicName;
            }

            saveReferredPatient(patient, clinic, clinicSector, mainClinic, getHSession(), "Activo");

            getHSession().flush();
            tx.commit();

            saveLastDispense(patient, getHSession());

            MessageBox m = new MessageBox(getShell(), SWT.OK
                    | SWT.ICON_INFORMATION);
            m.setText("Paciente " + msgTxt);
            m.setMessage("Paciente '".concat(patient.getPatientId()).concat(
                    msgTxt));
            m.open();

            closeShell(false);
        } catch (HibernateException he) {
            if (tx != null) {
                tx.rollback();
            }

            getLog().error("Erro ao gravar o paciente na base de dados.", he);
            MessageBox m = new MessageBox(getShell(), SWT.OK
                    | SWT.ICON_INFORMATION);
            m.setText("Problemas ao gravar a informação");
            m.setMessage("Problemas ao gravar a informação"
                    + ". Por favor, tente novamente.");
            m.open();
        }
    }

    @Override
    protected void createCompHeader() {
        // compHeader
        setCompHeader(new Composite(getShell(), SWT.NONE));
        getCompHeader().setLayout(new FormLayout());

        FormData fd = new FormData();
        fd.left = new FormAttachment(10, 0);
        fd.right = new FormAttachment(90, 0);
        fd.top = new FormAttachment(0, 5);

        // lblHeader
        lblHeader = new Label(getCompHeader(), SWT.BORDER | SWT.WRAP);
        lblHeader.setFont(ResourceUtils.getFont(iDartFont.VERASANS_10));
        lblHeader.setText(infoText);
        lblHeader.setLayoutData(fd);

        getCompHeader().pack();
        // Set bounds after pack, otherwise it resizes the composite
        Rectangle b = getShell().getBounds();
        getCompHeader().setBounds(0, 5, b.width, 60);
    }

    @Override
    protected void createCompOptions() {
    }

    @Override
    protected void createShell() {
        String shellTxt = "Referir este paciente";
        buildShell(shellTxt, new Rectangle(25, 0, 550, 400));

        createGrpSelectDownRefferal();
        createContents();
    }

    private void createContents() {
        Group grpReferredType = new Group(getShell(), SWT.NONE);
        grpReferredType.setBounds(new Rectangle(70, 130, 400, 180));

        labelSectorType = new Label(grpReferredType, SWT.CENTER);
        labelSectorType.setBounds(new Rectangle(10, 12, 80, 20));
        labelSectorType.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        labelSectorType.setText("Tipo Sector Clínico:");
        labelSectorType.setVisible(false);

        cmbClinicSectorType = new CCombo(grpReferredType, SWT.BORDER);
        cmbClinicSectorType.setEditable(false);
        cmbClinicSectorType.setBounds(new Rectangle(155, 12, 170, 20));
        cmbClinicSectorType.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbClinicSectorType.setVisible(false);
        CommonObjects.populateClinicSectorType(getHSession(), cmbClinicSectorType);
        cmbClinicSectorType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String newItems[] = {};
                cmbClinicSector.setItems(newItems);
                CommonObjects.populateClinicSectorBySectorType(getHSession(),cmbClinicSectorType.getText(), cmbClinicSector);
            }
        });

        labelSector = new Label(grpReferredType, SWT.CENTER);
        labelSector.setBounds(new Rectangle(10, 42, 125, 20));
        labelSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        labelSector.setText("Sector Clínico de Referência:");
        labelSector.setVisible(false);

        cmbClinicSector = new CCombo(grpReferredType, SWT.BORDER);
        cmbClinicSector.setBounds(new Rectangle(155, 42, 170, 20));
        cmbClinicSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbClinicSector.setEditable(false);
        cmbClinicSector.setVisible(false);


        label = new Label(grpReferredType, SWT.CENTER);
        label.setBounds(new Rectangle(10, 42, 100, 20));
        label.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        label.setText("Farmacia de Referência:");

        cmbClinic = new CCombo(grpReferredType, SWT.BORDER);
        cmbClinic.setBounds(new Rectangle(155, 42, 170, 20));
        cmbClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbClinic.setEditable(false);
        CommonObjects.populateClinics(getHSession(), cmbClinic, false);

        labelReferredDate = new Label(grpReferredType, SWT.CENTER);
        labelReferredDate.setBounds(new Rectangle(10, 72, 80, 20));
        labelReferredDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        labelReferredDate.setText("Data da Referência:");

        btnDownReferredDate = new DateButton(
                grpReferredType,
                DateButton.ZERO_TIMESTAMP,
                new DateInputValidator(DateRuleFactory.between(startDate,
                        true,
                        new Date(), true, true)));
        btnDownReferredDate.setBounds(new Rectangle(155, 72, 170, 20));
        btnDownReferredDate.setText("Data");
        btnDownReferredDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDownReferredDate.setToolTipText("Preccione o botão para seleccionar a data.");
        try {
            btnDownReferredDate.setDate(new Date());
        } catch (Exception e) {
            showMessage(MessageDialog.ERROR, "Error", e.getMessage());
        }

        labelRefferedTimes = new Label(grpReferredType, SWT.CENTER);
        labelRefferedTimes.setBounds(new Rectangle(10, 102, 175, 20));
        labelRefferedTimes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        labelRefferedTimes.setText("Primeira vez a referir para outra Farmacia?");

        Composite compRadio = new Composite(grpReferredType, SWT.NONE);
        compRadio.setBounds(new Rectangle(10, 132, 170, 20));

        btnYes = new Button(grpReferredType, SWT.RADIO);
        btnYes.setBounds(new Rectangle(190, 102, 80, 20));
        btnYes.setText("Sim");
        Button btnNo = new Button(grpReferredType, SWT.RADIO);
        btnNo.setBounds(new Rectangle(250, 102, 80, 20));
        btnNo.setText("Nao");
        btnYes.setSelection(true);
    }

    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }

    public void openAndWait() {
        activate();
        while (!getShell().isDisposed()) {
            if (!getShell().getDisplay().readAndDispatch()) {
                getShell().getDisplay().sleep();
            }
        }
    }

    public void saveReferredPatient(Patient patient, Clinic clinic, ClinicSector clinicSector, Clinic mainClinic, Session session, String estadoPaciente) {
        // Adiciona paciente referido para a sincronizacao.
        SyncTempPatient pacienteReferido = null;
        Prescription prescription = patient.getMostRecentPrescription("TARV");
        String clinicOrSectorUuid = "";
        String clinicOrSectorName = "";
        int clinicOrSectorId = 0;

        if(clinic != null) {
             clinicOrSectorUuid = clinic.getUuid();
             clinicOrSectorName = clinic.getClinicName();
             clinicOrSectorId = clinic.getId();
        } else {
            clinicOrSectorUuid = clinicSector.getUuid();
            clinicOrSectorName = clinicSector.getSectorname();
            clinicOrSectorId = clinicSector.getId();
        }

        if (prescription != null) {
            if (prescription.getPackages() != null) {
                int prescriptionDuration = 0;

                if (patient.getUuidopenmrs() != null)
                    pacienteReferido = AdministrationManager.getSyncTempPatienByUuidAndClinicUuid(getHSession(), patient.getUuidopenmrs(), clinicOrSectorUuid);
                else
                    pacienteReferido = AdministrationManager.getSyncTempPatienByNIDandClinicNameUuid(getHSession(), patient.getPatientId(), clinicOrSectorUuid);

                if (pacienteReferido == null)
                    pacienteReferido = AdministrationManager.getSyncTempPatienByNIDandClinicName(getHSession(), patient.getPatientId(), clinicOrSectorName);

                if (pacienteReferido == null)
                    pacienteReferido = new SyncTempPatient();

                pacienteReferido.setId(patient.getId());
                pacienteReferido.setAccountstatus(Boolean.FALSE);
                pacienteReferido.setAddress1(patient.getAddress1());
                pacienteReferido.setAddress2(patient.getAddress2());
                pacienteReferido.setAddress3(patient.getAddress3());
                pacienteReferido.setCellphone(patient.getCellphone());
                pacienteReferido.setDateofbirth(patient.getDateOfBirth());
                pacienteReferido.setClinic(clinicOrSectorId);
                pacienteReferido.setClinicname(clinicOrSectorName);
                pacienteReferido.setClinicuuid(clinicOrSectorUuid);
                pacienteReferido.setMainclinic(mainClinic.getId());
                pacienteReferido.setMainclinicname(mainClinic.getClinicName());
                pacienteReferido.setMainclinicuuid(mainClinic.getUuid());
                pacienteReferido.setNextofkinname(patient.getNextOfKinName());
                pacienteReferido.setNextofkinphone(patient.getNextOfKinPhone());
                pacienteReferido.setFirstnames(patient.getFirstNames());
                pacienteReferido.setHomephone(patient.getHomePhone());
                pacienteReferido.setLastname(patient.getLastname());
                pacienteReferido.setModified(patient.getModified());
                pacienteReferido.setPatientid(patient.getPatientId());
                pacienteReferido.setProvince(patient.getProvince());
                pacienteReferido.setSex(patient.getSex());
                pacienteReferido.setWorkphone(patient.getWorkPhone());
                pacienteReferido.setRace(patient.getRace());
                pacienteReferido.setUuid(patient.getUuidopenmrs());
                pacienteReferido.setEstadopaciente(estadoPaciente);
                pacienteReferido.setExclusaopaciente(false);

                prescriptionDuration = prescription.getDuration();

                for (Packages pack : prescription.getPackages()) {
                    prescriptionDuration = prescriptionDuration - pack.getWeekssupply();
                }
                pacienteReferido.setPrescriptiondate(prescription.getDate());
                pacienteReferido.setDuration(prescriptionDuration);
                pacienteReferido.setPrescriptionenddate(prescription.getEndDate());
                pacienteReferido.setRegimenome(prescription.getRegimeTerapeutico().getRegimeesquema());
                pacienteReferido.setLinhanome(prescription.getLinha().getLinhanome());
                pacienteReferido.setDispensatrimestral(prescription.getDispensaTrimestral());
                pacienteReferido.setDispensasemestral(prescription.getDispensaSemestral());
                pacienteReferido.setPrescriptionid(prescription.getPrescriptionId());
                pacienteReferido.setPrescricaoespecial(prescription.getPrescricaoespecial());
                pacienteReferido.setMotivocriacaoespecial(prescription.getMotivocriacaoespecial());


                if (!prescription.getPrescribedDrugs().isEmpty()) {

                    Map<String, Object> pd = new HashMap<String, Object>();
                    ArrayList listPD = new ArrayList();

                    for (PrescribedDrugs prescribedDrugs : prescription.getPrescribedDrugs()) {
                        pd.put("drugId", prescribedDrugs.getDrug().getId());
                        pd.put("drugcode", prescribedDrugs.getDrug().getAtccode());
                        pd.put("timesperday", prescribedDrugs.getTimesPerDay());
                        listPD.add(pd);
                    }
                    pacienteReferido.setJsonprescribeddrugs(listPD.toString());
                }

                if (patient.getAttributeByName("ARV Start Date") != null)
                    pacienteReferido.setDatainiciotarv(patient.getAttributeByName("ARV Start Date").getValue());
                pacienteReferido.setSyncstatus('P');

                AdministrationManager.saveSyncTempPatient(session, pacienteReferido);
            } else {
                MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                        | SWT.OK);
                missing.setText("Prescrição sem dispensa");
                missing
                        .setMessage("Este paciente contém uma prescriao sem dispensa. Por favor, remova a prescrição ou efectue a dispensa");
                missing.open();
            }
        } else {
            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Paciente sem Prescrição");
            missing
                    .setMessage("Este paciente não contém uma prescrição. Por favor, queira criar a prescrição e efectue a respetiva dispensa");
            missing.open();
        }
    }

    public void saveLastDispense(Patient patient, Session session) {

        Prescription prescription = patient.getMostRecentPrescription(iDartProperties.SERVICOTARV);
        Packages aPackage = PackageManager.getLastPackageOnScript(prescription);

        java.util.List<PackageDrugInfo> packagedDrugsList = PackageManager.getPackageDrugInfoForPatient(session, patient.getPatientId(), aPackage.getPackageId());
        // Last dispense status L
        for (PackageDrugInfo pdi : packagedDrugsList) {
            if (pdi.getId() != 0) {
                //Para farmac Insere dispensas para US
                if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
                    savePackageDrugInfosFarmac(pdi, 'L');
                }
            }
        }
    }

    public void cleanFields () {
        String newItems[] = {};
        cmbClinic.select(0);
        cmbClinicSectorType.select(0);
        cmbClinicSector.setItems(newItems);
    }
}
