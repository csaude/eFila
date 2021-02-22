package org.celllife.idart.gui.patient;

import model.manager.AdministrationManager;
import model.manager.PackageManager;
import org.apache.log4j.Logger;
import org.celllife.function.DateRuleFactory;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.CommonObjects;
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
    private DateButton btnDownReferredDate;
    private Button btnYes;
    private final Date startDate;

    public DownReferDialog(Shell parent, Session session, Patient patient) {
        super(parent, session);
        this.patient = patient;
        startDate = patient.getMostRecentEpisode().getStartDate();
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
        if (clinicName == null || clinicName.isEmpty()) {
            showMessage(MessageDialog.ERROR, "Farmacia nao selecionada",
                    "Por Favor Seleccione a Farmacia.");
            return false;
        }
        return true;
    }

    private void doSave() {
        Transaction tx = null;
        try {
            tx = getHSession().beginTransaction();
            Episode episode = patient.getMostRecentEpisode();
            Date date = btnDownReferredDate.getDate();
            episode.setStopDate(date);
            episode.setStopReason("Referido");
            String clinicName = cmbClinic.getText();
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

            Clinic clinic = AdministrationManager.getClinic(getHSession(),
                    clinicName);

            Clinic mainClinic = AdministrationManager.getMainClinic(getHSession());

            patient.setClinic(clinic);
            newEpisode.setClinic(clinic);

            saveReferredPatient(patient, clinic, mainClinic, getHSession());

            getHSession().flush();
            tx.commit();

            saveLastDispense(patient,getHSession());

            MessageBox m = new MessageBox(getShell(), SWT.OK
                    | SWT.ICON_INFORMATION);
            m.setText("Paciente Referido para outra Farmacia");
            m.setMessage("O Paciente '".concat(patient.getPatientId()).concat(
                    "' foi referido para outra Farmacia."));
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
        getCompHeader().setBounds(0, 5, b.width, 100);
    }

    @Override
    protected void createCompOptions() {
    }

    @Override
    protected void createShell() {
        String shellTxt = "Referir este paciente";
        buildShell(shellTxt, new Rectangle(25, 0, 550, 300));

        createContents();
    }

    private void createContents() {
        Composite compContents = new Composite(getShell(), SWT.NONE);

        GridLayout gl = new GridLayout(2, true);
        gl.horizontalSpacing = 15;
        gl.verticalSpacing = 10;
        gl.marginLeft = 85;
        compContents.setLayout(gl);

        Label label = new Label(compContents, SWT.CENTER);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
                | GridData.VERTICAL_ALIGN_CENTER));
        label.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        label.setText("Farmacia de Referencia:");

        cmbClinic = new CCombo(compContents, SWT.BORDER);
        cmbClinic.setEditable(false);
        cmbClinic.setLayoutData(new GridData(150, 15));
        cmbClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        CommonObjects.populateClinics(getHSession(), cmbClinic, false);

        label = new Label(compContents, SWT.CENTER);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
                | GridData.VERTICAL_ALIGN_CENTER));
        label.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        label.setText("Data da referencia do Paciente:");

        btnDownReferredDate = new DateButton(
                compContents,
                DateButton.ZERO_TIMESTAMP,
                new DateInputValidator(DateRuleFactory.between(startDate,
                        true,
                        new Date(), true, true)));
        btnDownReferredDate.setLayoutData(new GridData(155, 20));
        btnDownReferredDate.setText("Data");
        btnDownReferredDate
                .setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDownReferredDate
                .setToolTipText("Preccione o botão para seleccionar a data.");
        try {
            btnDownReferredDate.setDate(new Date());
        } catch (Exception e) {
            showMessage(MessageDialog.ERROR, "Error", e.getMessage());
        }

        label = new Label(compContents, SWT.CENTER);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING
                | GridData.VERTICAL_ALIGN_CENTER));
        label.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        label.setText("Primeira vez a referir para outra Farmacia?");

        Composite compRadio = new Composite(compContents, SWT.NONE);
        compRadio.setLayout(new RowLayout());
        compRadio.setLayoutData(new GridData(150, 25));

        btnYes = new Button(compRadio, SWT.RADIO);
        btnYes.setText("Sim");
        Button btnNo = new Button(compRadio, SWT.RADIO);
        btnNo.setText("Nao");
        btnYes.setSelection(true);

        Rectangle b = getShell().getBounds();
        compContents.setBounds(0, 100, b.width, 100);
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

    public void saveReferredPatient(Patient patient, Clinic clinic, Clinic mainClinic, Session session) {
        // Adiciona paciente referido para a sincronizacao.
        SyncTempPatient pacienteReferido = null;
        Prescription prescription = patient.getMostRecentPrescription();
        Packages aPackage = PackageManager.getLastPackageOnScript(prescription);

        java.util.List<PackageDrugInfo> packagedDrugsList = PackageManager.getPackageDrugInfoForPatient(session, patient.getPatientId(), aPackage.getPackageId());


        int prescriptionDuration = 0;

        if (patient.getUuidopenmrs() != null)
            pacienteReferido = AdministrationManager.getSyncTempPatienByUuid(getHSession(), patient.getUuidopenmrs());
        else
            pacienteReferido = AdministrationManager.getSyncTempPatienByNIDandClinicNameUuid(getHSession(), patient.getPatientId(), mainClinic.getUuid());

        if (pacienteReferido == null)
            pacienteReferido = AdministrationManager.getSyncTempPatienByNIDandClinicName(getHSession(), patient.getPatientId(), mainClinic.getClinicName());

        if (pacienteReferido == null)
            pacienteReferido = new SyncTempPatient();

        pacienteReferido.setId(patient.getId());
        pacienteReferido.setAccountstatus(Boolean.FALSE);
        pacienteReferido.setAddress1(patient.getAddress1());
        pacienteReferido.setAddress2(patient.getAddress2());
        pacienteReferido.setAddress3(patient.getAddress3());
        pacienteReferido.setCellphone(patient.getCellphone());
        pacienteReferido.setDateofbirth(patient.getDateOfBirth());
        pacienteReferido.setClinic(clinic.getId());
        pacienteReferido.setClinicname(clinic.getClinicName());
        pacienteReferido.setClinicuuid(clinic.getUuid());
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

        if (prescription != null) {
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
        }

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

    }

    public void saveLastDispense(Patient patient, Session session){

        Prescription prescription = patient.getMostRecentPrescription();
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
}
