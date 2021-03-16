/*
 * iDART: The Intelligent Dispensing of Antiretroviral Treatment
 * Copyright (C) 2006 Cell-Life
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version
 * 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.celllife.idart.gui.packaging;

import model.manager.*;
import model.manager.reports.PatientHistoryReport;
import model.nonPersistent.PatientIdAndName;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.celllife.function.DateRuleFactory;
import org.celllife.idart.commonobjects.*;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.facade.PillCountFacade;
import org.celllife.idart.gui.composite.PillCountTable;
import org.celllife.idart.gui.deletions.DeleteStockPrescriptionsPackages;
import org.celllife.idart.gui.label.PrintEmergencyLabel;
import org.celllife.idart.gui.misc.iDARTChangeListener;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.prescription.AddPrescription;
import org.celllife.idart.gui.reportParameters.PatientHistory;
import org.celllife.idart.gui.reprintLabels.ReprintLabels;
import org.celllife.idart.gui.stockOnHand.StockOnHandGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.gui.widget.DateButton;
import org.celllife.idart.gui.widget.DateInputValidator;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.DateFieldComparator;
import org.celllife.idart.misc.PatientBarcodeParser;
import org.celllife.idart.misc.iDARTUtil;
import org.celllife.idart.rest.ApiAuthRest;
import org.celllife.idart.rest.utils.RestClient;
import org.celllife.idart.rest.utils.RestUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static org.celllife.idart.rest.ApiAuthRest.getServerStatus;

/**
 *
 */
public class NewPatientPackaging extends GenericFormGui implements iDARTChangeListener {

    private ListViewer lstWaitingPatients;
    private Button btnDispense;
    private Label lblClinic;
    private CCombo cmbSupply;
    private CCombo cmbSelectStockCenter;
    private CCombo cmbDispenseMode;
    private TableEditor editorTblPrescriptionInfo;
    private boolean fieldsEnabled = false; // has a patient been selected to
    private Label lblDuration;
    private Label lblIndex;
    private Label lblPackageInfo1;
    private Label lblPackageInfo2;
    private Label lblPackageInfo3;
    private Label lblPicChild;
    private Label lblPicLeftArrow;
    private Label lblPicRightArrow;
    private Label lblNextAppointment;
    private Link lnkStockOnHand;
    private Button rdBtnDispenseNow;
    private Button rdBtnDispenseLater;
    private Button rdBtnNoAppointmentDate;
    private Button rdBtnYesAppointmentDate;
    private Button rdBtnPrintSummaryLabelNo;
    private Button rdBtnPrintSummaryLabelYes;
    private Button rdBtnTARVPrescription;
    private Button rdBtnTBPrescription;
    private ProgressBar pbLoading;
    private Text searchBar;
    private Table tblPrescriptionInfo;
    private Text txtAreaNotes;
    private Text txtDoctor;
    private Text txtNextAppDate;
    private Text txtPatientAge;
    private Text txtPatientDOB;
    private Text txtPatientId;
    private Text txtPatientName;
    private Text txtPrescriptionDate;
    private Text txtPrescriptionId;
    private Label lblDateOfLastPickupContents;
    private DateButton btnCaptureDate;
    private DateButton btnNextAppDate;
    private PillCountTable pillCountTable;
    private PillCountFacade pillFacade; // facade classes
    private StockCenter localPharmacy; // local persistent objects
    private Packages newPack; // the new package
    private Packages previousPack; // the last package picked up
    private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH); // local
    private boolean dateAlreadyDispensed;
    private Patient localPatient;
    private int dias = 0;
    private boolean postOpenMrsEncounterStatus;
    public String tipoPaciente = iDartProperties.SERVICOTARV;


    ConexaoJDBC conn = new ConexaoJDBC();
    private static Logger log = Logger.getLogger(NewPatientPackaging.class);

    /**
     * Constructor
     *
     * @param parent Shell
     */
    public NewPatientPackaging(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
        createScreen();
    }

    /**
     * Constructor for when the screen is accessed from a 'Dispense Now'
     * shortcut, and must be populated for a particular patient
     *
     * @param parent  Shell
     * @param patient
     */
    public NewPatientPackaging(Shell parent, Patient patient) {
        super(parent, HibernateUtil.getNewSession());
        createScreenForNewPatient(patient);
    }

    public NewPatientPackaging(Shell parent, Patient patient, String tipoPaciente) {
        super(parent, HibernateUtil.getNewSession());
        this.tipoPaciente = tipoPaciente;
        createScreenForNewPatient(patient);
    }

    private void createScreenForNewPatient(Patient patient) {
        Patient p = PatientManager.getPatient(getHSession(), patient.getId());
        dateAlreadyDispensed = false;

        if (p == null) {
            getLog().error("Patient number passed to packaging GUI does not belong to a patient");
        } else {
            populatePatientDetails(p.getId());
        }
    }

    /**
     * Change the pill count dates, package dates and next appointment date when
     * the user changes the dispense date
     *
     * @param theDispDate
     * @param theDispDate
     */
    private void adjustForNewDispDate(Date theDispDate) {
        if (previousPack != null && theDispDate != null) {

            Date lastPickupDate = previousPack.getPickupDate();
            int numOfDays = iDARTUtil.getDaysBetween(lastPickupDate, theDispDate);
            lblDateOfLastPickupContents.setText(numOfDays + " dias (" + sdf.format(lastPickupDate) + ")");
            dias = numOfDays;
        } else {
            lblDateOfLastPickupContents.setText("Levantamento inicial");
        }

        if (theDispDate != null) {
            pillCountTable.update(theDispDate);
        }
    }

    private void attachPharmacyComboListener() {
        cmbSelectStockCenter.setEditable(false);
        cmbSelectStockCenter.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbSelectStockCenter.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {

                localPharmacy = AdministrationManager.getStockCenter(getHSession(), cmbSelectStockCenter.getText());
                if (localPharmacy != null) {
                    if (newPack.getPrescription() != null) {
                        populateStockDetails();
                        prepopulateQuantities();
                    }
                } else {
                    getLog().warn("Tried to populate screen for update of pharmacy '" + cmbSelectStockCenter.getText()
                            + "', but no pharmacy with that name was found in the database.");
                }
            }

        });
    }

    private void attachPrescriptionInfoTableEditor() {
        // add a editor for the labels and inHand columns, otherwise open
        // batch information when col clicked
        editorTblPrescriptionInfo = new TableEditor(tblPrescriptionInfo);
        editorTblPrescriptionInfo.horizontalAlignment = SWT.LEFT;
        editorTblPrescriptionInfo.grabHorizontal = true;

        tblPrescriptionInfo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                // Dispose any existing editor
                Control old = editorTblPrescriptionInfo.getEditor();
                if (old != null) {
                    old.dispose();
                }


                // Determine where the mouse was clicked
                Point pt = new Point(event.x, event.y);

                // Determine which row was selected
                final TableItem item = tblPrescriptionInfo.getItem(pt);
                if (item != null) {
                    // Determine which column was selected
                    int column = -1;
                    for (int i = 0, n = tblPrescriptionInfo.getColumnCount(); i < n; i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            // This is the selected column
                            column = i;
                            break;
                        }
                    }

                    if (column == 4) {
                        // Create the Text object for label column

                        final Text text = new Text(tblPrescriptionInfo, SWT.NONE);
                        text.setForeground(item.getForeground());
                        text.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                        text.setBackground(ResourceUtils.getColor(iDartColor.GRAY));

                        text.setText(item.getText(column));
                        text.setForeground(item.getForeground());
                        text.selectAll();
                        text.setFocus();

                        editorTblPrescriptionInfo.minimumWidth = text.getBounds().width;

                        // Set the control into the editor
                        editorTblPrescriptionInfo.setEditor(text, item, column);

                        final int col = column;

                        text.addModifyListener(new ModifyListener() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public void modifyText(ModifyEvent event1) {

                                item.setText(col, text.getText());
                                // check user input

                                ArrayList<PackageDrugInfo> list = ((ArrayList<PackageDrugInfo>) item.getData());
                                if (list.size() > 0) {
                                    if (!text.getText().trim().equals("")) {
                                        try {
                                            int labels = Integer.parseInt(text.getText());
                                            list.get(0).setNumberOfLabels(labels);
                                        } catch (NumberFormatException e) {
                                            java.awt.Toolkit.getDefaultToolkit().beep();
                                            text.setText("1");
                                            item.setText(col, "1");
                                            text.selectAll();
                                        } catch (IndexOutOfBoundsException iobex) {
                                            getLog().error("No drugs in list in order to specify a label to print.",
                                                    iobex);
                                        }

                                    } else {
                                        text.setText("1");
                                        item.setText(col, "1");
                                        text.selectAll();
                                    }

                                } else {
                                    if (!text.getText().equals("0")) {
                                        java.awt.Toolkit.getDefaultToolkit().beep();
                                        MessageBox msg = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                                        msg.setText("Quantidade de Etiqueta inválido.");
                                        msg.setMessage(
                                                "Você não vai conseguir imprimir uma etiqueta para este medicamento "
                                                        + "uma vez que nenhuma quantidade foi especificado. "
                                                        + "\n\nPor favor, primeiro defina a quantidade de dispensar.");
                                        msg.open();
                                        text.setText("0");
                                        text.selectAll();
                                        return;
                                    }
                                }
                            }
                        });
                    } else if (column == 5) {
                        // Create the combo box for the in hand column

                        final CCombo combo = new CCombo(tblPrescriptionInfo, SWT.NONE);
                        combo.setForeground(item.getForeground());
                        combo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                        combo.setBackground(ResourceUtils.getColor(iDartColor.GRAY));

                        combo.setText(item.getText(column));
                        combo.setForeground(item.getForeground());
                        combo.setFocus();
                        combo.setEditable(false);

                        editorTblPrescriptionInfo.minimumWidth = combo.getBounds().width;

                        // Set the control into the editor
                        editorTblPrescriptionInfo.setEditor(combo, item, column);

                        final int col = column;

                        java.util.List<String> possibleValues = getPossibleInHandOnExitValues(item);
                        Iterator<String> itr = possibleValues.iterator();

                        while (itr.hasNext()) {
                            combo.add(itr.next());
                        }

                        combo.addModifyListener(new ModifyListener() {
                            @Override
                            public void modifyText(ModifyEvent event1) {

                                item.setText(col, combo.getText());
                            }
                        });

                        combo.addFocusListener(new FocusListener() {
                            @Override
                            public void focusGained(FocusEvent event1) {

                            }

                            @Override
                            public void focusLost(FocusEvent event1) {

                                item.setText(col, combo.getText());
                                combo.dispose();

                            }
                        });
                    } else {
                        cmdTblPrescriptionWidgetSelected();
                    }
                }
            }
        });
    }

    private void attachSearchBarListener() {
        searchBar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String currentText = searchBar.getText().trim();

                // first, parse the text in txtPatientId
                String patientId = PatientBarcodeParser.getPatientId(currentText);
                populatePatientDetails(patientId, e.character == SWT.CR);
            }
        });
    }

    private void attemptCaptureDateReset() {
        Date today = new Date();
        Date chosenDate = new Date();
        if (btnCaptureDate.getDate() != null) {
            chosenDate = btnCaptureDate.getDate();
        }

        if (chosenDate.after(today)) {
            MessageBox mb = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
            mb.setText("Data de dispensa não pode ser no futuro");
            mb.setMessage("A Data de dispensa não pode ser no futuro");
            mb.open();
            btnCaptureDate.setDate(today);
            chosenDate = today;
        }

        // Obtaining the currently selected capture date
        // if there is a previous pack date which is not
        // after the currently selected pack date.
        if (previousPack != null) {
            Date pickupDate = previousPack.getPickupDate();
            if (iDARTUtil.before(chosenDate, pickupDate)) {
                String msg = "O Paciente \"{0}\" teve sua última dispensa em {1,date,medium}. "
                        + "\n\nA data para a nova dispensa que está a criar deve estar depois desta data.";
                showMessage(MessageDialog.ERROR, "Erro da data de dispensa",
                        MessageFormat.format(msg, localPatient.getPatientId(), pickupDate));

                btnCaptureDate.setDate(today);
                chosenDate = today;
            }
        }

        if (iDARTUtil.before(chosenDate, today)) {
            chosenDate = iDARTUtil.zeroTimeStamp(chosenDate);
            btnCaptureDate.setDate(chosenDate);
        }

        if (fieldsEnabled) {
            String clinicName = localPatient.getClinicAtDate(chosenDate).getClinicName();
            // Check if clinic has changed. If so, show message
            if (!lblClinic.getText().equalsIgnoreCase(clinicName)) {
                MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                m.setText("US do paciente mudou");
                m.setMessage("Note que em " + sdf.format(btnCaptureDate.getDate()) + ", paciente '"
                        + localPatient.getPatientId() + "' foi dispensado seus medicamentos " + clinicName
                        + ".\n\n Todas dispensas feitas deste paciente" + "neste dia será destinado para esta US, "
                        + "não na US actual do paciente.");
                m.open();
            }

            lblClinic.setText(clinicName);
            setDateExpectedFields();
            populatePrescriptionDetails();
            updateDateOfLastPickup();
        }
        if (newPack != null) {
            newPack.setPackDate(chosenDate);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.celllife.idart.gui.misc.iDARTChangeListner#changed()
     */

    /**
     * Method changed.
     *
     * @param o Object
     * @see org.celllife.idart.gui.misc.iDARTChangeListener#changed(Object)
     */
    @Override
    public void changed(Object o) {
        if (o instanceof PillCount) {
            setInHandOnExit((PillCount) o);
        }

        if (o instanceof Date) {
            // Done if there has been a change in the ARV Start date
            // through a ARV Start Date Picker.
            Transaction tx = null;
            try {
                tx = getHSession().beginTransaction();
                String dte = iDARTUtil.toString(Date.class, o);
                newPack.getPrescription().getPatient().getAttributeByName(PatientAttribute.ARV_START_DATE)
                        .setValue(dte);
                PatientManager.savePatient(getHSession(), newPack.getPrescription().getPatient());
                getHSession().flush();
            } catch (Exception e) {
                if (tx == null) {
                    getLog().error("Could not change ARV Start date", e);
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method clears all textfields
     */
    @Override
    protected void clearForm() {
        lblClinic.setText("");
        txtPatientId.setText("");
        txtPatientAge.setText("");
        txtPatientDOB.setText("");
        searchBar.setText("");
        txtPatientName.setText("");
        btnDispense.setEnabled(false);
        lblDateOfLastPickupContents.setText("");
        txtPrescriptionDate.setText("");
        txtDoctor.setText("");
        txtPrescriptionId.setText("");
        cmbDispenseMode.setText("");
        txtAreaNotes.setText("");
        btnNextAppDate.setDate(new Date());
        tblPrescriptionInfo.clearAll();
        // tblLastPackageDrugs.clearAll();
        lblPicChild.setVisible(false);
        lblIndex.setText("");
        lblDuration.setText("");
        lblPicLeftArrow.setVisible(false);
        lblPicRightArrow.setVisible(false);
        highlightPackageInfo(ResourceUtils.getColor(iDartColor.BLACK));
        enableFields(false);
        pillCountTable.clearTable();

        closeAndReopenSession();
        searchBar.setFocus();
    }

    /**
     * This method closes and reopens a session. It should be used in screens
     * that don't close after a write
     */
    public void closeAndReopenSession() {
        try {
            if (getHSession() != null) {
                getHSession().close();
            }
            Session newSession = HibernateUtil.getNewSession();
            setHSession(newSession);
            pillCountTable.setHSession(newSession);
        } catch (HibernateException he) {
            getLog().error("Error closing and reopening session", he);
            he.printStackTrace();
        }
    }

    private void cmbWeeksSupplyChanged() {

        String theWeeks = cmbSupply.getText();
        int numPeriods = 4;

        try {
            numPeriods = Integer.parseInt(theWeeks.split(" ")[0]);
            if (theWeeks.endsWith("meses") || theWeeks.endsWith("mes")) {
                numPeriods = numPeriods * 4;
            }

        } catch (NumberFormatException ne) {
            getLog().warn("NumberFormatException while trying to get weeks supply from combo", ne);
            numPeriods = 4;
        }
        if (newPack != null) {
            newPack.setWeekssupply(numPeriods);

            if (newPack.getPrescription() != null) {

                prepopulateQuantities();
            }

            if (rdBtnDispenseNow.getSelection()) {
                Calendar theCal = Calendar.getInstance();

                newPack.setPackDate(btnCaptureDate.getDate() == null ? new Date() : btnCaptureDate.getDate());

                theCal.setTime(newPack.getPackDate());

                if (newPack.getWeekssupply() == 1 || newPack.getWeekssupply() == 2) {
                    theCal.add(Calendar.DATE, newPack.getWeekssupply() * 7);
                    adjustForNewAppointmentDate(theCal.getTime());
                } else if (newPack.getWeekssupply() == 4) {

                    theCal.add(Calendar.MONTH, 1);
                    adjustForNewAppointmentDate(theCal.getTime());
                } else if (newPack.getWeekssupply() == 8) {

                    theCal.add(Calendar.MONTH, 2);
                    adjustForNewAppointmentDate(theCal.getTime());
                } else if (newPack.getWeekssupply() == 12) {

                    theCal.add(Calendar.MONTH, 3);
                    adjustForNewAppointmentDate(theCal.getTime());
                } else if (newPack.getWeekssupply() == 16) {

                    theCal.add(Calendar.MONTH, 4);
                    adjustForNewAppointmentDate(theCal.getTime());
                } else if (newPack.getWeekssupply() == 20) {

                    theCal.add(Calendar.MONTH, 5);
                    adjustForNewAppointmentDate(theCal.getTime());
                } else if (newPack.getWeekssupply() == 24) {

                    theCal.add(Calendar.MONTH, 6);
                    adjustForNewAppointmentDate(theCal.getTime());
                }

            }
        }

        Prescription pre = localPatient.getCurrentPrescription(tipoPaciente);
        if(pre != null)
        if (pre.getMotivocriacaoespecial().contains("Perda")) {
            String dateExpected = PatientManager.lastNextPickup(getHSession(), localPatient.getId());
            if (dateExpected != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                Date dataproximolev = new Date(dateExpected);
                adjustForNewAppointmentDate(dataproximolev);
                btnNextAppDate.setText(sdf.format(dataproximolev));
                btnNextAppDate.setEnabled(false);
            }
        }

    }

    @Override
    protected void cmdCancelWidgetSelected() {
    }

    @Override
    protected void cmdClearWidgetSelected() {
    }

    /**
     * Method cmdDispenseDrugsSelected.
     *
     * @param dispenseNow boolean Quando o botao create package for clicadoo
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private void cmdDispenseDrugsSelected(boolean dispenseNow) throws Exception {

        String title = EMPTY;
        String message = EMPTY;

        if (!rdBtnNoAppointmentDate.getSelection() && !rdBtnYesAppointmentDate.getSelection()) { //$NON-NLS-1$
            showMessage(MessageDialog.ERROR, "Seleccione se o paciente levou ou não cotrimoxazol", "Seleccione se o paciente levou ou não cotrimoxazol.");
            return;
        } else if (!rdBtnPrintSummaryLabelNo.getSelection() && !rdBtnPrintSummaryLabelYes.getSelection()) { //$NON-NLS-1$
            showMessage(MessageDialog.ERROR, "Seleccione se o paciente levou ou não isoniazida", "Seleccione se o paciente levou ou não isoniazida.");
            return;
        } else {

            java.util.List<PackageDrugInfo> allPackagedDrugsList = new ArrayList<PackageDrugInfo>();
            // remove pdis with none dispensed
            for (int i = 0; i < tblPrescriptionInfo.getItemCount(); i++) {
                java.util.List<PackageDrugInfo> pdiList = (java.util.List<PackageDrugInfo>) tblPrescriptionInfo.getItem(i)
                        .getData();
                Iterator<PackageDrugInfo> it = pdiList.iterator();
                while (it.hasNext()) {
                    PackageDrugInfo pdi = it.next();
                    if (pdi.getDispensedQty() != 0) {
                        pdi.setDispenseDate(newPack.getPackDate());
                        pdi.setWeeksSupply(getSelectedWeekSupply());
                        pdi.setDispensedForLaterPickup(!dispenseNow);
                        pdi.setPickupDate(dispenseNow ? new Date() : null);
                        pdi.setModeDispense(cmbDispenseMode.getText());
                        if (rdBtnNoAppointmentDate.getSelection())
                            pdi.setInhPickup(false);
                        else
                            pdi.setInhPickup(true);

                        if(rdBtnPrintSummaryLabelNo.getSelection())
                            pdi.setCtzPickup(false);
                        else
                            pdi.setCtzPickup(true);
                        allPackagedDrugsList.add(pdi);
                    }
                }
            }

            Set<AccumulatedDrugs> accumDrugSet = getAccumDrugsToSave();
            if (fieldsOkay(allPackagedDrugsList) && ((allPackagedDrugsList.size() > 0) || (accumDrugSet.size() > 0))) {
                submitForm(dispenseNow, allPackagedDrugsList);
                getLog().info("submitForm() called");

                initialiseSearchList();
                clearForm();
            }
        }
    }

    private void initialiseSearchList() {
        java.util.List<PatientIdAndName> patients = null;
        patients = SearchManager.getActivePatientWithValidPrescriptionIDsAndNames(getHSession(),tipoPaciente);

        lstWaitingPatients.setInput(patients);
    }

    /**
     * View the patient history report, or show a patient selection report
     * parameters screen if no patient is selected
     */
    private void cmdPatientHistoryWidgetSelected() {

        getLog().info("New Patient Packaging: User chose 'Patient History Report'");

        if (localPatient != null) {
            PatientHistoryReport report = new PatientHistoryReport(getShell(), localPatient);
            viewReport(report);
        } else {
            PatientHistory patHistory = new PatientHistory(getShell(), true);
            patHistory.openShell();
        }
    }

    private void cmdPrintEmergencyLabelSelected() {

        new PrintEmergencyLabel(getShell());

    }

    private void cmdReprintLabelsSelected() {

        new ReprintLabels(getShell());
    }

    /**
     * Called when the user wants to ResourceUtils.getColor(iDartColor.RED)o an
     * arv package (ie. delete the most recent package for any patient)
     */

    private void cmdRedoPackageSelected() {
        closeAndReopenSession();
        if ((newPack.getPrescription() != null) && !(txtPatientId.getText().trim().equals(""))) {

            final Patient patient = newPack.getPrescription().getPatient();
            final DeleteStockPrescriptionsPackages myRedoPackage = new DeleteStockPrescriptionsPackages(getShell(),
                    patient);
            myRedoPackage.getShell().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    clearForm();

                    populatePatientDetails(patient.getId());
                }
            });
        } else {

            final DeleteStockPrescriptionsPackages myRedoPackage = new DeleteStockPrescriptionsPackages(getShell());
            myRedoPackage.getShell().addDisposeListener(new DisposeListener() {
                @Override
                public void widgetDisposed(DisposeEvent e) {
                    clearForm();
                }
            });
        }
    }

    @Override
    protected void cmdSaveWidgetSelected() {
    }

    private int getSelectedWeekSupply() {
        String theWeeks = cmbSupply.getText();
        int numPeriods = Integer.parseInt("" + theWeeks.charAt(0));
        if (theWeeks.endsWith("meses") || theWeeks.endsWith("mes")) {
            numPeriods *= 4;
        }
        return numPeriods;
    }

    /**
     * Passes the current list of pdis to a BatchInformation, where the pdis for
     * the selected drug can be entered.
     */
    @SuppressWarnings("unchecked")
    private void cmdTblPrescriptionWidgetSelected() {
        final java.util.List<PackageDrugInfo> finalPdisForThisDrug = (java.util.List<PackageDrugInfo>) tblPrescriptionInfo
                .getSelection()[0].getData();

        Drug drug = DrugManager.getDrug(getHSession(), tblPrescriptionInfo.getSelection()[0].getText(0));

        final BatchInformation myBatchInformation = new BatchInformation(getHSession(), getShell(), localPharmacy, drug,
                newPack, finalPdisForThisDrug);
        myBatchInformation.getShell().addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                if (myBatchInformation.infoChanged) {
                    // update the weekssupply
                    tblPrescriptionInfo.getSelection()[0].setData(myBatchInformation.getPdiList());
                    getDispensedQuantity(myBatchInformation.getStockList(), tblPrescriptionInfo.getSelection()[0]);
                } else {
                    tblPrescriptionInfo.getSelection()[0].setData(finalPdisForThisDrug);
                }
            }
        });

    }

    private void cmdUpdatePrescriptionWidgetSelected() {

        final AddPrescription myPrescription = new AddPrescription(localPatient ,getShell(), true,tipoPaciente);

        myPrescription.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                Patient patient = myPrescription.getPatient();
                closeAndReopenSession();

                if (patient != null)
                    patient = PatientManager.getPatient(getHSession(), patient.getId());
                else
                    clearForm();

                if ((patient != null) && (patient.getCurrentPrescription(tipoPaciente) != null)) {
                    populatePatientDetails(patient.getId());
                } else {
                    clearForm();
                }
            }
        });

    }

    /**
     * This method initializes comp
     *
     * @param parent Composite
     */
    private void createCompShowAppointmentOnLabels(Composite parent) {
        Composite compShowAppointmentOnLabels = new Composite(parent, SWT.NONE);
        compShowAppointmentOnLabels.setLayout(null);
        compShowAppointmentOnLabels.setBounds(new Rectangle(400, 173, 110, 22));

        rdBtnYesAppointmentDate = new Button(compShowAppointmentOnLabels, SWT.RADIO);
        rdBtnYesAppointmentDate.setBounds(new Rectangle(3, 4, 45, 20));
        rdBtnYesAppointmentDate.setText("Sim");
        rdBtnYesAppointmentDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        rdBtnNoAppointmentDate = new Button(compShowAppointmentOnLabels, SWT.RADIO);
        rdBtnNoAppointmentDate.setBounds(new Rectangle(57, 4, 50, 20));
        rdBtnNoAppointmentDate.setText("Nao");
        rdBtnNoAppointmentDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        if (iDartProperties.nextAppointmentDateOnLabels) {
            rdBtnYesAppointmentDate.setSelection(false);
            rdBtnNoAppointmentDate.setSelection(false);
        } else {
            rdBtnYesAppointmentDate.setSelection(false);
            rdBtnNoAppointmentDate.setSelection(false);
        }

    }

    /**
     * This method initializes getCompButtons()
     */
    @Override
    protected void createCompButtons() {

        setCompButtons(new Composite(getShell(), SWT.NONE));
        getCompButtons().setBounds(new Rectangle(10, 626, 864, 34));

        // btnPrintEmergencyLabel
        Button btnRerrintLabels = new Button(getCompButtons(), SWT.NONE);
        btnRerrintLabels.setBounds(new Rectangle(0, 2, 120, 30));
        btnRerrintLabels.setText("Reimprimir Etiqueta");
        btnRerrintLabels.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                cmdReprintLabelsSelected();
            }
        });

        // btnRePrintLabel
        Button btnPrintCustomLabel = new Button(getCompButtons(), SWT.NONE);
        btnPrintCustomLabel.setBounds(new Rectangle(130, 2, 120, 30));
        btnPrintCustomLabel.setText("Etiqueta personalizada");
        btnPrintCustomLabel.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                cmdPrintEmergencyLabelSelected();
            }
        });

        btnRerrintLabels.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnPrintCustomLabel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblPicDispense = new Label(getCompButtons(), SWT.NONE);
        lblPicDispense.setBounds(new Rectangle(508, 0, 40, 34));

        lblPicDispense.setImage(ResourceUtils.getImage(iDartImage.DISPENSEPACKAGENOW_40X34));

        btnDispense = new Button(getCompButtons(), SWT.NONE);
        btnDispense.setBounds(new Rectangle(554, 2, 146, 30));
        btnDispense.setText("Dispensar Frascos");
        btnDispense.setToolTipText(
                "Pressione este botão para dispensar esses medicamentos. \nA tela da impressora aparece para impressão de etiquetas de medicamento e etiqueta do frasco.");
        btnDispense.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                try {
                    cmdDispenseDrugsSelected(rdBtnDispenseNow.getSelection());
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                getLog().info("cmdDispenseDrugsSelected() called");
            }
        });
        btnDispense.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDispense.setEnabled(false);

        Button btnClose = new Button(getCompButtons(), SWT.NONE);
        btnClose.setBounds(new Rectangle(735, 2, 126, 30));
        btnClose.setText("Fechar");
        btnClose.setToolTipText("Pressione este botão para fechar essa tela.");
        btnClose.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                cmdCloseSelected();
            }
        });
        btnClose.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblPicRedoPackage = new Label(getCompButtons(), SWT.NONE);
        lblPicRedoPackage.setBounds(new Rectangle(274, 0, 40, 34));

        lblPicRedoPackage.setImage(ResourceUtils.getImage(iDartImage.REDOPACKAGE_40X34));

        // btnRedoPackage
        Button btnRedoPackage = new Button(getCompButtons(), SWT.NONE);
        btnRedoPackage.setBounds(new Rectangle(318, 2, 155, 30));
        btnRedoPackage.setText("Refazer Frascos dispesados");
        btnRedoPackage.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                cmdRedoPackageSelected();
            }
        });
        btnRedoPackage.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

    }

    /**
     * This method initializes CompDispensingTypeSelection
     */
    private void createCompDispensingTypeSelection() {

        Composite compDispensingTypeSelection = new Composite(getShell(), SWT.BORDER);
        compDispensingTypeSelection.setBounds(new Rectangle(242, 56, 643, 67));

        // lblPicDispenseNow
        Label lblPicDispenseNow = new Label(compDispensingTypeSelection, SWT.NONE);
        lblPicDispenseNow.setBounds(new Rectangle(13, 4, 40, 34));
        lblPicDispenseNow.setImage(ResourceUtils.getImage(iDartImage.DISPENSEPACKAGENOW_40X34));

        // btnDispenseNow
        rdBtnDispenseNow = new Button(compDispensingTypeSelection, SWT.RADIO);
        rdBtnDispenseNow.setBounds(new Rectangle(58, 9, 244, 30));
        rdBtnDispenseNow.setText("Dispensar directamente para pacientes");

        rdBtnDispenseNow.setToolTipText("Pressione este botão para imprimir etiquetas para todos medicamentos "
                + "nesta prescrição. \nEstas etiquetas devem ser colocadas no " + "Frasco dos medicamentos.");
        rdBtnDispenseNow.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                // cmdDispenseDrugsSelected(true);

                if (fieldsEnabled) {
                    resetGUIforDispensingType();
                } else {
                    lblNextAppointment.setText("Próximo Levantamento:");
                }

            }
        });
        rdBtnDispenseNow.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblPicDispenseLater
        Label lblPicDispenseLater = new Label(compDispensingTypeSelection, SWT.NONE);
        lblPicDispenseLater.setBounds(new Rectangle(345, 3, 40, 34));
        lblPicDispenseLater.setImage(ResourceUtils.getImage(iDartImage.PACKAGESAWAITINGPICKUP_40X34));

        // btnDispenseLater
        rdBtnDispenseLater = new Button(compDispensingTypeSelection, SWT.RADIO);
        rdBtnDispenseLater.setBounds(new Rectangle(390, 8, 251, 30));
        rdBtnDispenseLater.setText("Para dispensa Posterior");
        rdBtnDispenseLater.setToolTipText("Pressione este botão para imprimir as etiquetas dos frascos para o "
                + "paciente.\nHaverá uma etiqueta para cada um dos medicamento nesta "
                + "prescrição,\n e a etiqueta para o frasco.");
        rdBtnDispenseLater.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                if (fieldsEnabled) {
                    // change in hand values
                    clearInHandOnExit();
                    resetGUIforDispensingType();
                } else {
                    lblNextAppointment.setText("Levantamento Actual:");
                }
            }
        });
        rdBtnDispenseLater.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        if (iDartProperties.dispenseDirectlyDefault) {
            rdBtnDispenseNow.setSelection(true);
            rdBtnDispenseLater.setSelection(false);

        } else {
            rdBtnDispenseNow.setSelection(false);
            rdBtnDispenseLater.setSelection(true);
        }

        final Label lblSelectPharmacy = new Label(compDispensingTypeSelection, SWT.NONE);
        lblSelectPharmacy.setBounds(new Rectangle(51, 41, 200, 20));
        lblSelectPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
        lblSelectPharmacy.setAlignment(SWT.RIGHT);
        lblSelectPharmacy.setText("Farmácia:");

        cmbSelectStockCenter = new CCombo(compDispensingTypeSelection, SWT.BORDER);
        cmbSelectStockCenter.setBounds(new Rectangle(255, 40, 300, 20));
        cmbSelectStockCenter.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));

        CommonObjects.populateStockCenters(getHSession(), cmbSelectStockCenter);
        localPharmacy = AdministrationManager.getStockCenter(getHSession(), cmbSelectStockCenter.getText());

        attachPharmacyComboListener();

    }

    /**
     * This method initializes compHeader
     */
    @Override
    protected void createCompHeader() {
        String headerTxt = "Dispensa de Medicamentos";
        iDartImage icoImage = iDartImage.DISPENSEPACKAGES;
        buildCompHeader(headerTxt, icoImage);
    }

    /**
     * This method initializes composite
     */
    private void createCompLastPackage() {
        Composite compLastPackage = new Composite(getShell(), SWT.BORDER);
        compLastPackage.setLayout(null);
        compLastPackage.setBounds(new Rectangle(5, 412, 879, 207));

        createLastPackageTable(compLastPackage);
        createPrescriptionInfoTable(compLastPackage);


        Label lblSummaryLabel = new Label(compLastPackage, SWT.NONE);
        lblSummaryLabel.setBounds(new Rectangle(630, 177, 100, 18));
        lblSummaryLabel.setText("Leva Isoniazida?");
        lblSummaryLabel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblAppointmentDate = new Label(compLastPackage, SWT.NONE);
        lblAppointmentDate.setBounds(new Rectangle(270, 177, 120, 15));
        lblAppointmentDate.setText("Leva Cotrimoxazol?");
        lblAppointmentDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        createCompShowAppointmentOnLabels(compLastPackage);
        createCompSummaryLabel(compLastPackage);

    }

    /**
     * This method initializes compPatientsWaiting
     */
    private void createCompPatientsWaiting() {

        Group compPatientsWaiting = new Group(getShell(), SWT.NONE);
        compPatientsWaiting.setBounds(new Rectangle(6, 56, 213, 349));

        // rdBtnTARVPrescription
        rdBtnTARVPrescription = new Button(compPatientsWaiting, SWT.RADIO);
        rdBtnTARVPrescription.setBounds(new Rectangle(8, 8, 50, 20));
        rdBtnTARVPrescription.setText("TARV");
        rdBtnTARVPrescription.setToolTipText("Pressione este botão para prescriçõe de medicamentos TARV.");
        rdBtnTARVPrescription.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                clearForm();
                tipoPaciente = iDartProperties.SERVICOTARV;
                rdBtnTBPrescription.setSelection(false);
                rdBtnTARVPrescription.setSelection(true);

                initialiseSearchList();
            }
        });
        rdBtnTARVPrescription.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));


        // rdBtnTBPrescription
        rdBtnTBPrescription = new Button(compPatientsWaiting, SWT.RADIO);
        rdBtnTBPrescription.setBounds(new Rectangle(160, 8, 50, 20));
        rdBtnTBPrescription.setText("TPT");
        rdBtnTBPrescription.setToolTipText("Pressione este botão para criar/actualizar uma prescrição de medicamentos TB.");
        rdBtnTBPrescription.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                clearForm();
                tipoPaciente = iDartProperties.PNCT;
                rdBtnTBPrescription.setSelection(true);
                rdBtnTARVPrescription.setSelection(false);

                initialiseSearchList();
            }
        });

        rdBtnTBPrescription.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblSearch = new Label(compPatientsWaiting, SWT.NONE);
        lblSearch.setBounds(new Rectangle(10, 37, 187, 20));
        lblSearch.setText("Procurar Paciente:");
        lblSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        searchBar = new Text(compPatientsWaiting, SWT.BORDER);
        searchBar.setBounds(new Rectangle(10, 57, 187, 20));
        searchBar.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        searchBar.setFocus();

        attachSearchBarListener();

        Label lblWaitingPatients = new Label(compPatientsWaiting, SWT.BORDER);
        lblWaitingPatients.setBounds(new Rectangle(10, 77, 186, 20));
        lblWaitingPatients.setAlignment(SWT.CENTER);
        lblWaitingPatients.setText("1. Resultados da Pesquisa");
        lblWaitingPatients.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_BOLD));

        lstWaitingPatients = new ListViewer(compPatientsWaiting);
        lstWaitingPatients.getList().setBounds(new Rectangle(9, 97, 187, 179));
        lstWaitingPatients.getList().setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lstWaitingPatients.setContentProvider(new ArrayContentProvider());
        lstWaitingPatients.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                if (!lstWaitingPatients.getSelection().isEmpty()) {
                    pbLoading.setSelection(0);
                    lstWaitingPatientsWidgetSelected();
                }

            }
        });

        Label lblClinicName = new Label(compPatientsWaiting, SWT.BORDER);
        lblClinicName.setBounds(new Rectangle(9, 280, 187, 15));
        lblClinicName.setAlignment(SWT.CENTER);
        lblClinicName.setText("US do Paciente");
        lblClinicName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_BOLD));

        lblClinic = new Label(compPatientsWaiting, SWT.BORDER);
        lblClinic.setBounds(new Rectangle(9, 299, 187, 20));
        lblClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblClinic.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));

        pbLoading = new ProgressBar(compPatientsWaiting, SWT.SMOOTH);
        pbLoading.setBounds(new Rectangle(10, 322, 185, 20));

        initialiseSearchList();

    }

    /**
     * This method initializes compSummaryLabel
     *
     * @param parent Composite
     */
    private void createCompSummaryLabel(Composite parent) {

        Composite compSummaryLabel = new Composite(parent, SWT.NONE);
        compSummaryLabel.setLayout(null);
        compSummaryLabel.setBounds(new Rectangle(730, 173, 110, 21));

        rdBtnPrintSummaryLabelYes = new Button(compSummaryLabel, SWT.RADIO);
        rdBtnPrintSummaryLabelYes.setBounds(new Rectangle(5, 1, 49, 20));
        rdBtnPrintSummaryLabelYes.setText("Sim");
        rdBtnPrintSummaryLabelYes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        rdBtnPrintSummaryLabelNo = new Button(compSummaryLabel, SWT.RADIO);
        rdBtnPrintSummaryLabelNo.setBounds(new Rectangle(57, 1, 60, 20));
        rdBtnPrintSummaryLabelNo.setText("Não");
        rdBtnPrintSummaryLabelNo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        if (iDartProperties.summaryLabelDefault) {
            rdBtnPrintSummaryLabelYes.setSelection(false);
            rdBtnPrintSummaryLabelNo.setSelection(false);
        } else {
            rdBtnPrintSummaryLabelYes.setSelection(false);
            rdBtnPrintSummaryLabelNo.setSelection(false);
        }
    }

    @Override
    protected void createContents() {
        createCompDispensingTypeSelection();
        createCompPatientsWaiting();
        createGrpPatientInfo();
        createGrpPackageInfo();
        createGrpPrescriptionInfo();
        createCompLastPackage();
        clearForm();
    }

    /**
     * This method initializes grpNotes
     *
     * @param parent Composite
     */
    private void createGrpNotes(Composite parent) {

        Group grpNotes = new Group(parent, SWT.NONE);
//        grpNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
//        grpNotes.setText("Notas da Prescrição");
        grpNotes.setBounds(new Rectangle(351, 15, 279, 107));

        Label lblDispenseMode = new Label(grpNotes, SWT.BORDER);
        lblDispenseMode.setBounds(new Rectangle(15, 7, 170, 20));
        lblDispenseMode.setText(Messages.getString("patient.prescription.dialog.dispense.mode"));
        lblDispenseMode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbDispenseMode = new CCombo(grpNotes, SWT.BORDER | SWT.READ_ONLY);
        cmbDispenseMode.setBounds(new Rectangle(15, 19, 250, 20));
        cmbDispenseMode.setVisibleItemCount(10);
        cmbDispenseMode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbDispenseMode.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbDispenseMode.setForeground(ResourceUtils.getColor(iDartColor.BLACK));

        //popula o ccombo modo de dispensa
        CommonObjects.populateModoDispensa(getHSession(), cmbDispenseMode);

        Label lblNotes = new Label(grpNotes, SWT.BORDER);
        lblNotes.setBounds(new Rectangle(15, 44, 190, 25));
        lblNotes.setText(Messages.getString("patient.prescription.dialog.prescription.notes"));
        lblNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtAreaNotes = new Text(grpNotes, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        txtAreaNotes.setBounds(new Rectangle(15, 58, 250, 35));
        txtAreaNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtAreaNotes.setEditable(true);
        txtAreaNotes.setEnabled(false);

    }

    /**
     * This method initializes grpPackageInfo
     */
    private void createGrpPackageInfo() {

        Group grpPackageInfo = new Group(getShell(), SWT.NONE);
        grpPackageInfo.setBounds(new Rectangle(591, 129, 294, 137));
        grpPackageInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblPicLeftArrow = new Label(grpPackageInfo, SWT.NONE);
        lblPicLeftArrow.setBounds(new Rectangle(21, 20, 40, 34));
        lblPicLeftArrow.setImage(ResourceUtils.getImage(iDartImage.ARROWRIGHT_40X34));
        lblPicLeftArrow.setVisible(false);

        lblPackageInfo1 = new Label(grpPackageInfo, SWT.CENTER);
        lblPackageInfo1.setText("Este é o Levatamento Nº");
        lblPackageInfo1.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblPackageInfo1.setBounds(new Rectangle(71, 10, 150, 20));

        lblPicRightArrow = new Label(grpPackageInfo, SWT.NONE);
        lblPicRightArrow.setBounds(new Rectangle(231, 20, 40, 34));
        lblPicRightArrow.setImage(ResourceUtils.getImage(iDartImage.ARROWLEFT_40X34));
        lblPicRightArrow.setVisible(false);
        // txtPrescriptionDate.setText("Initial Pickup");
        lblIndex = new Label(grpPackageInfo, SWT.RIGHT);
        lblIndex.setText(" ");
        lblIndex.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblIndex.setBounds(new Rectangle(93, 30, 33, 20));

        lblPackageInfo2 = new Label(grpPackageInfo, SWT.CENTER);
        lblPackageInfo2.setText(" de ");
        lblPackageInfo2.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblPackageInfo2.setBounds(new Rectangle(130, 30, 40, 20));

        lblDuration = new Label(grpPackageInfo, SWT.NONE);
        lblDuration.setText(" ");
        lblDuration.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDuration.setBounds(new Rectangle(170, 30, 32, 21));

        lblPackageInfo3 = new Label(grpPackageInfo, SWT.CENTER);
        lblPackageInfo3.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblPackageInfo3.setText("mês(es) de prescrição");
        lblPackageInfo3.setBounds(new Rectangle(89, 50, 111, 20));

        Label lblSupply1 = new Label(grpPackageInfo, SWT.LEFT);
        lblSupply1.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblSupply1.setText("Contém medicamentos para:");
        lblSupply1.setBounds(new Rectangle(4, 75, 180, 20));

        cmbSupply = new CCombo(grpPackageInfo, SWT.BORDER);
        cmbSupply.setBounds(new Rectangle(180, 75, 105, 20));
        cmbSupply.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbSupply.setEditable(false);
        cmbSupply.setVisibleItemCount(10);
        CommonObjects.populatePrescriptionDuration(getHSession(), cmbSupply);
        cmbSupply.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbSupply.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {

                cmbWeeksSupplyChanged();

            }
        });

        lblSupply1 = new Label(grpPackageInfo, SWT.LEFT);
        lblSupply1.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblSupply1.setText("");
        lblSupply1.setBounds(new Rectangle(215, 76, 72, 20));

        // Capture Date
        Label lblCaptureDate = new Label(grpPackageInfo, SWT.NONE);
        lblCaptureDate.setBounds(new Rectangle(4, 101, 72, 20));
        lblCaptureDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblCaptureDate.setText("Data Dispensa:");

        btnCaptureDate = new DateButton(grpPackageInfo, DateButton.NONE,
                new DateInputValidator(DateRuleFactory.beforeNowInclusive(true)));
        btnCaptureDate.setBounds(new Rectangle(110, 100, 155, 25));
        btnCaptureDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnCaptureDate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                attemptCaptureDateReset();
                pillCountTable.update(btnCaptureDate.getDate());
            }
        });

        btnCaptureDate.setDate(new Date());

        // Why should this be here when the patient is loaded...
        // Are not the dates meant to be captured properly?
        // Therefore they should be loaded properly.
        attemptCaptureDateReset();
        if (fieldsEnabled) {
            setDateExpectedFields();
        }

    }

    /**
     * This method initializes grpPatientInfo
     */
    private void createGrpPatientInfo() {

        Group grpPatientInfo = new Group(getShell(), SWT.NONE);
        grpPatientInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpPatientInfo.setText("Informação do Paciente");
        grpPatientInfo.setBounds(new Rectangle(242, 129, 339, 137));

        Label lblPatientId = new Label(grpPatientInfo, SWT.NONE);
        lblPatientId.setBounds(new org.eclipse.swt.graphics.Rectangle(10, 30, 110, 20));
        lblPatientId.setText(Messages.getString("patient.label.patientid")); //$NON-NLS-1$
        lblPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPatientId = new Text(grpPatientInfo, SWT.BORDER);
        txtPatientId.setBounds(new Rectangle(135, 30, 160, 20));
        txtPatientId.setEnabled(false);
        txtPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblPatientName = new Label(grpPatientInfo, SWT.NONE);
        lblPatientName.setBounds(new org.eclipse.swt.graphics.Rectangle(10, 55, 110, 20));
        lblPatientName.setText("Nome:");
        lblPatientName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPatientName = new Text(grpPatientInfo, SWT.BORDER);
        txtPatientName.setBounds(new Rectangle(135, 55, 160, 20));
        txtPatientName.setEnabled(false);
        txtPatientName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblPatientAge = new Label(grpPatientInfo, SWT.NONE);
        lblPatientAge.setBounds(new org.eclipse.swt.graphics.Rectangle(10, 80, 110, 20));
        lblPatientAge.setText("Idade:");
        lblPatientAge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPatientAge = new Text(grpPatientInfo, SWT.BORDER);
        txtPatientAge.setBounds(new Rectangle(135, 80, 40, 20));
        txtPatientAge.setEnabled(false);
        txtPatientAge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPatientDOB = new Text(grpPatientInfo, SWT.BORDER);
        txtPatientDOB.setBounds(new org.eclipse.swt.graphics.Rectangle(180, 80, 114, 20));
        txtPatientDOB.setEnabled(false);
        txtPatientDOB.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        lblPicChild = new Label(grpPatientInfo, SWT.NONE);
        lblPicChild.setBounds(new Rectangle(300, 75, 30, 26));
        lblPicChild.setImage(ResourceUtils.getImage(iDartImage.CHILD_30X26));
        lblPicChild.setVisible(false);

        // next appointment date
        lblNextAppointment = new Label(grpPatientInfo, SWT.NONE);
        lblNextAppointment.setBounds(new Rectangle(10, 107, 120, 20));
        lblNextAppointment.setText(
                iDartProperties.dispenseDirectlyDefault == true ? "Próximo Levantamento:" : "Levantamento Actual:");
        lblNextAppointment.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtNextAppDate = new Text(grpPatientInfo, SWT.BORDER);
        txtNextAppDate.setBounds(new Rectangle(135, 105, 160, 20));
        txtNextAppDate.setEnabled(false);
        txtNextAppDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtNextAppDate.setVisible(false);

        btnNextAppDate = new DateButton(grpPatientInfo, DateButton.ZERO_TIMESTAMP, null);
        btnNextAppDate.snapToControl(false);
        btnNextAppDate.setBounds(new Rectangle(135, 105, 160, 25));
        btnNextAppDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnNextAppDate.setVisible(false);
    }

    /**
     * This method initializes grpPrescriptionInfo
     */
    private void createGrpPrescriptionInfo() {

        Group grpPrescriptionInfo = new Group(getShell(), SWT.NONE);
        grpPrescriptionInfo.setBounds(new Rectangle(242, 277, 642, 128));
        grpPrescriptionInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpPrescriptionInfo.setText("Informação da Prescrição");

        Label lblPrescriptionId = new Label(grpPrescriptionInfo, SWT.NONE);
        lblPrescriptionId.setBounds(new Rectangle(10, 22, 115, 20));
        lblPrescriptionId.setText("ID da Prescrição:");
        lblPrescriptionId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPrescriptionId = new Text(grpPrescriptionInfo, SWT.BORDER);
        txtPrescriptionId.setBounds(new Rectangle(126, 22, 150, 20));
        txtPrescriptionId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtPrescriptionId.setEnabled(false);

        Label lblDoctor = new Label(grpPrescriptionInfo, SWT.NONE);
        lblDoctor.setBounds(new Rectangle(10, 47, 115, 20));
        lblDoctor.setText("Clínico:");
        lblDoctor.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtDoctor = new Text(grpPrescriptionInfo, SWT.BORDER);
        txtDoctor.setBounds(new Rectangle(126, 47, 150, 20));
        txtDoctor.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtDoctor.setEnabled(false);

        Label lblDateOfLastPickup = new Label(grpPrescriptionInfo, SWT.NONE);
        lblDateOfLastPickup.setBounds(new Rectangle(10, 72, 114, 20));
        lblDateOfLastPickup.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDateOfLastPickup.setText("Último Levantamento:");

        lblDateOfLastPickupContents = new Label(grpPrescriptionInfo, SWT.BORDER);
        lblDateOfLastPickupContents.setBounds(new Rectangle(126, 72, 150, 20));
        lblDateOfLastPickupContents.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblPicPatientHistoryReport
        Button btnPatientHistoryReport = new Button(grpPrescriptionInfo, SWT.NONE);
        btnPatientHistoryReport.setBounds(new Rectangle(294, 73, 40, 40));
        btnPatientHistoryReport.setToolTipText(
                "Pressione este botão para visualizar e / ou imprimir relatórios \ndo Histórico da Prescrição do Paciente.");
        btnPatientHistoryReport.setImage(ResourceUtils.getImage(iDartImage.REPORT_PATIENTHISTORY_30X26));

        btnPatientHistoryReport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent mu) {
                cmdPatientHistoryWidgetSelected();
            }
        });

        // lblPicUpdatePrescription
        Button btnUpdatePrescription = new Button(grpPrescriptionInfo, SWT.NONE);
        btnUpdatePrescription.setBounds(new Rectangle(293, 23, 40, 40));
        btnUpdatePrescription.setToolTipText("Pressione este botão para actualizar a prescrição do paciente");
        btnUpdatePrescription.setImage(ResourceUtils.getImage(iDartImage.PRESCRIPTIONNEW_30X26));
        btnUpdatePrescription.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent mu) {
                cmdUpdatePrescriptionWidgetSelected();
            }
        });

        Label lblPrescriptionDate = new Label(grpPrescriptionInfo, SWT.NONE);
        lblPrescriptionDate.setBounds(new Rectangle(10, 97, 113, 20));
        lblPrescriptionDate.setText("Data da Prescrição:");
        lblPrescriptionDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPrescriptionDate = new Text(grpPrescriptionInfo, SWT.BORDER);
        txtPrescriptionDate.setBounds(new Rectangle(126, 97, 150, 20));
        txtPrescriptionDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtPrescriptionDate.setEnabled(false);

        createGrpNotes(grpPrescriptionInfo);
    }

    private void createLastPackageTable(Composite parent) {
        pillCountTable = new PillCountTable(parent, SWT.NONE, getHSession(), new Rectangle(8, 11, 245, 160));
        pillCountTable.getTable().setBounds(new Rectangle(0, 19, 244, 140));
        pillCountTable.setHeading("2. Contagem de Comprimidos");
        pillCountTable.setPillCountGroupHeading("");
        pillCountTable.addChangeListener(this);
    }

    /**
     * This method initializes tblPrescriptionInfo
     *
     * @param parent Composite
     */
    private void createPrescriptionInfoTable(Composite parent) {

        lnkStockOnHand = new Link(parent, SWT.BORDER);
        lnkStockOnHand.setBounds(new Rectangle(274, 11, 596, 21));
        lnkStockOnHand.setText("3. Medicamentos a dispensar:        " + "                                            "
                + "                                       <A>Stock Disponível</A>");
        lnkStockOnHand.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_BOLD));
        lnkStockOnHand.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                new StockOnHandGui(getShell(), cmbSelectStockCenter.getText());
            }
        });

        tblPrescriptionInfo = new Table(parent, SWT.FULL_SELECTION | SWT.BORDER);
        tblPrescriptionInfo.setBounds(new Rectangle(274, 31, 596, 140));
        tblPrescriptionInfo.setHeaderVisible(true);
        tblPrescriptionInfo.setLinesVisible(true);
        tblPrescriptionInfo.setToolTipText("clique em uma linha para visualizar as informações adicionais da dispensa");

        tblPrescriptionInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        TableColumn column = new TableColumn(tblPrescriptionInfo, SWT.NONE);
        column.setText("Nome do Medicamento");
        column.setWidth(195);
        column.setResizable(true);

        column = new TableColumn(tblPrescriptionInfo, SWT.NONE);
        column.setText("Posologia");
        column.setWidth(160);
        column.setResizable(true);

        column = new TableColumn(tblPrescriptionInfo, SWT.NONE);
        column.setText("Disp");
        column.setWidth(40);
        column.setResizable(true);

        column = new TableColumn(tblPrescriptionInfo, SWT.NONE);
        column.setText("Saldo de Frascos");
        column.setWidth(90);
        column.setResizable(true);

        column = new TableColumn(tblPrescriptionInfo, SWT.NONE);
        column.setText("Etiquetas");
        column.setWidth(53);
        column.setResizable(true);

        column = new TableColumn(tblPrescriptionInfo, SWT.NONE);
        column.setText("Em mão");
        column.setWidth(58);
        column.setResizable(true);


        attachPrescriptionInfoTableEditor();

    }

    /**
     * Create the GUI and populate it with all patients that have a valid script
     * at the mainclinic
     */
    private void createScreen() {
        setScreenToInitialState();
    }

    /**
     * This method initializes newPatientPackaging
     */
    @Override
    protected void createShell() {
        init();
        String shellTxt = "Frascos de Medicamentos para os Pacientes";
        buildShell(shellTxt, new Rectangle(25, 0, 900, 700));
    }

    /**
     * This method is used to check that at least 1 drug item has been
     * dispensed. The pharmacist doesn't have to dispense everything on this
     * prescription, but s/he does have to dispense at least 1 item.
     *
     * @return boolean
     */
    private boolean drugQuantitiesOkay() {
        boolean quantitiesEntered = false;
        for (int i = 0; i < tblPrescriptionInfo.getItemCount(); i++) {
            TableItem ti = tblPrescriptionInfo.getItem(i);
            if (!ti.getText(3).equals("0")) {
                quantitiesEntered = true;
            }
        }
        return quantitiesEntered;
    }

    /**
     * Method enableFields.
     *
     * @param enable boolean
     */
    @Override
    protected void enableFields(boolean enable) {
        txtAreaNotes.setEnabled(enable);
        cmbSupply.setEnabled(enable);
        cmbDispenseMode.setEnabled(enable);
        btnDispense.setEnabled(enable);
        btnNextAppDate.setEnabled(enable);
        btnCaptureDate.setEnabled(enable);
        tblPrescriptionInfo.setEnabled(enable);
        if (pillCountTable != null && rdBtnDispenseNow.getSelection()) {
            pillCountTable.getTable().setEnabled(enable);
        }

        fieldsEnabled = enable;
    }

    /**
     * Method fieldsOk.
     *
     * @return boolean
     */
    @Override
    protected boolean fieldsOk() {
        return false;
    }

    /**
     * Method fieldsOkay.
     *
     * @param allPackagedDrugsList java.util.List<PackageDrugInfo>
     * @return boolean
     */
    private boolean fieldsOkay(java.util.List<PackageDrugInfo> allPackagedDrugsList) {
        Patient patient = PatientManager.getPatient(getHSession(), localPatient.getId());
        int amountperPackage = (int) (allPackagedDrugsList.get(0).getDispensedQty() / (allPackagedDrugsList.get(0).getTimesPerDay() *
                Double.parseDouble(allPackagedDrugsList.get(0).getAmountPerTime()))) / 7;

        if (localPatient.getCurrentPrescription(tipoPaciente).getDuration() != newPack.getWeekssupply()) {
            MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            mb.setText("Dispensa Trimestral");
            mb.setMessage("A duração da prescrição é de " + localPatient.getCurrentPrescription(tipoPaciente).getDuration() / 4 + " mes(es) e não é a mesma que a da dispensa. "
                    + "PRETENDE MESMO DISPENSAR ESTA PRESCRIÇÃO?");
            int resposta = mb.open();
            if (resposta == SWT.NO) {
                return false;
            }
        }

        if (amountperPackage > newPack.getWeekssupply()) {
            MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
            mb.setText("Quantidade ");
            mb.setMessage("A quantidade de medicamentos por dispensar " + ((allPackagedDrugsList.get(0).getDispensedQty())) + " serve para mais tempo que o selecionado. "
                    + "PRETENDE MESMO DISPENSAR ESTA PRESCRIÇÃO?");
            int resposta = mb.open();
            if (resposta == SWT.NO) {
                return false;
            }
        }

        if (patient == null || txtPatientId.getText().equals("")) {
            showMessage(MessageDialog.ERROR, "Nenhum Paciente Seleccionado",
                    "Nenhum Paciente foi Seleccionado. Precisa de seleccionar um.");
            return false;
        }

        if (!drugQuantitiesOkay()) {
            showMessage(MessageDialog.ERROR, "Nenhuma quantidade de medicamento foi Dispensado",
                    "Você não inseriu quantidades para qualquer um dos medicamentos.");
            return false;
        }

        //Se tiver ja dispensado INICIO nao pode dispensar mais inicio
        if (newPack.getPrescription().getReasonForUpdate().contains("nici") && conn.jaTemFilaInicio(localPatient.getPatientId(),tipoPaciente)) { //$NON-NLS-1$

            if(tipoPaciente.equalsIgnoreCase(iDartProperties.PNCT)){
                showMessage(MessageDialog.ERROR, "Por favor actualize a prescricao do paciente para CONTINUA (C) PROFILAXIA (INH) ",
                        "Por favor actualize a prescricao do paciente para CONTINUA (C) PROFILAXIA (INH).");
            }else {
                showMessage(MessageDialog.ERROR, "Por favor actualize a prescricao do paciente para TIPO TARV MANTER ",
                        "Por favor actualize a prescricao do paciente para TIPO TARV MANTER.");
            }
            return false;
        }

        if (btnCaptureDate.getDate().before(newPack.getPrescription().getDate())
                && !(sdf.format(btnCaptureDate.getDate()).equals(sdf.format(newPack.getPrescription().getDate())))) {
            showMessage(MessageDialog.ERROR, "Data de Dispensa Inválida!",
                    "A Data de dispensa não pode estar antes da data da prescrição "
                            + sdf.format(newPack.getPrescription().getDate()) + " ");
            return false;
        }

        if (btnNextAppDate.getDate() != null && btnNextAppDate.getDate().before(btnCaptureDate.getDate())
                && !(sdf.format(btnCaptureDate.getDate()).equals(sdf.format(btnNextAppDate.getDate())))) {
            showMessage(MessageDialog.ERROR, "A data do proximo levantamento esta antes da data deste levantamento",
                    "A data do próximo levantamento não pode ser antes da data deste levantamento.");
            return false;
        }

        if (PackageManager.patientHasUncollectedPackages(getHSession(), patient)) {

            Date oldPackDate = PackageManager.getPackDateForUncollectedPackage(getHSession(),
                    PatientManager.getPatient(getHSession(), txtPatientId.getText()));
            showMessage(MessageDialog.ERROR, "Frasco de medicamento já foi criado para Patient",
                    "O Frasco já foi criado para este paciente'" + txtPatientId.getText() + "' em "
                            + sdf.format(oldPackDate) + " e ainda não foi entregue ao paciente. "
                            + "\n\nSó podes criar outro frasco para este paciente "
                            + "Quando o frasco anterior for entregue ao paciente ou "
                            + "devolvido a farmácia (usando a tela 'Devolver Frascos não Entregues ao Paciente').");
            return false;
        }

        if (dateAlreadyDispensed) {
            Packages lastPackageMade = PackageManager.getLastPackageMade(getHSession(), patient);
            // if package hasn't been collected yet or date is not today
            // http://dev.cell-life.org/jira/browse/IDART-14
            Date pickupDate = lastPackageMade.getPickupDate();
            if (pickupDate == null || iDARTUtil.hasZeroTimestamp(pickupDate)) {
                Date oldPackDate = lastPackageMade.getPackDate();

                showMessage(MessageDialog.ERROR, "Frasco já foi criado neste dia",
                        "O Frasco já foi criado para o paciente '" + txtPatientId.getText() + "' em "
                                + sdf.format(oldPackDate)
                                + ".\n\niDART não aceita criar outro frasco para este paciente no mesmo dia, "
                                + "assim como o tempo em que eles foram criados será o mesmo. "
                                + "Isso causa problemas na contagem de comprimidos. ");
                return false;
            }
        }

        // open temporary session
        Session sess = HibernateUtil.getNewSession();
        for (PackageDrugInfo pdi : allPackagedDrugsList) {
            if (!checkStockStillAvailable(sess, pdi)) {
                showMessage(MessageDialog.ERROR, "O Stock seleccionado está vazio.",
                        "O lote do stock seleccionado para " + pdi.getDrugName()
                                + " não contém mais stock.\n\nSeleccione outro lote.");
                return false;
            }
        }

        // close temp session
        sess.close();

        for (PackageDrugInfo pdi : allPackagedDrugsList) {
            if (!checkForBreakingPacks(pdi)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that there is still stock available.
     *
     * @param sess
     * @param pdi
     * @return
     */
    private boolean checkStockStillAvailable(Session sess, PackageDrugInfo pdi) {
        Stock stock = StockManager.getStock(sess, pdi.getStockId());
        if ('T' == stock.getHasUnitsRemaining()) {
            StockLevel sl = StockManager.getCurrentStockLevel(sess, stock);
            if (sl == null) {
                return false;
            }
            int unitsRemaining = sl.getFullContainersRemaining() * stock.getDrug().getPackSize()
                    + sl.getLoosePillsRemaining();
            if (unitsRemaining >= pdi.getDispensedQty())
                return true;
        }
        return false;
    }

    private boolean checkForBreakingPacks(PackageDrugInfo pdi) {
        boolean fieldsOk = true;
        if ((pdi.getFormLanguage1().toUpperCase().startsWith("ML")) && ((pdi.getDispensedQty()
                % DrugManager.getDrug(getHSession(), pdi.getDrugName()).getPackSize()) != 0)) {
            MessageBox m = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            m.setMessage("Você está prestes a dispensar medicamentos " + pdi.getDispensedQty() + " ml de "
                    + pdi.getDrugName() + ". Note que isto significa que um frasco está incompleto. "
                    + "\n\nTem certeza de que quer quebrar um pacote?");
            m.setText("Dispensa de Frasco Incompleto");

            switch (m.open()) {
                case SWT.YES:
                    fieldsOk = true;
                    break;
                case SWT.NO:
                    fieldsOk = false;
                    break;
            }
        }
        return fieldsOk;
    }

    /**
     * Method getAccumDrugsToSave.
     *
     * @return Set<AccumulatedDrugs>
     */
    private Set<AccumulatedDrugs> getAccumDrugsToSave() {

        java.util.Set<AccumulatedDrugs> adsToSave = new HashSet<AccumulatedDrugs>();
        for (int i = 0; i < tblPrescriptionInfo.getItemCount(); i++) {

            TableItem item = tblPrescriptionInfo.getItem(i);

            try {
                int inHand = Integer.parseInt(item.getText(5));
                int disp = Integer.parseInt(item.getText(2));

                if (inHand > disp) {

                    AccumulatedDrugs ad = new AccumulatedDrugs();

                    ad.setWithPackage(newPack);

                    // find the pillcount
                    String drugName = item.getText(0);
                    Set<PillCount> pillCounts = pillCountTable.getPillCounts();
                    for (PillCount count : pillCounts) {
                        if (count.getDrug().getName().equals(drugName)) {
                            ad.setPillCount(count);
                        }
                    }
                    if (ad.getPillCount() == null) {
                        getLog().error("Did not save accumulated drug " + drugName + " because it had no pillcount");
                    } else {
                        adsToSave.add(ad);
                    }
                }
            } catch (NumberFormatException p) {
                getLog().error("NumberFormatError saving accumulated drug for " + item.getText(0));
            }

        }
        return adsToSave;
    }

    /**
     * Method getDispensedQuantity.
     *
     * @param theStockList java.util.List<PackageDrugInfo>
     * @param ti           TableItem
     */


    private void getDispensedQuantity(java.util.List<PackageDrugInfo> theStockList, TableItem ti) {

        int totalDispensedQty = 0;
        int totalNumberOfLabels = 0;

        // also going to reset inHand
        int previousDispensedQuantity = Integer.parseInt(ti.getText(2));
        int previousInHand = Integer.parseInt(ti.getText(5));
        for (int i = 0; i < theStockList.size(); i++) {

            PackageDrugInfo pdi = theStockList.get(i);
            if (ti.getText(0).equals(pdi.getDrugName())) {

                totalDispensedQty += pdi.getDispensedQty();
                totalNumberOfLabels += pdi.getNumberOfLabels();
            }

        }
        ti.setText(2, String.valueOf(totalDispensedQty));
        ti.setText(4, String.valueOf(totalNumberOfLabels));
        ti.setText(5, String.valueOf(totalDispensedQty + (previousInHand - previousDispensedQuantity)));

        /*
         * set the foreground colour to ResourceUtils.getColor(iDartColor.BLACK)
         * since the user has indicated what batch the drug should come from
         */
        if (totalDispensedQty != 0) {
            ti.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        } else {
            ti.setForeground(ResourceUtils.getColor(iDartColor.RED));

        }
    }

    /**
     * Method getPossibleInHandOnExitValues.
     * <p>
     * TableItem
     *
     * @return java.util.List<String>
     */
    private void savePillCounts() {

        Set<PillCount> pcnts = pillCountTable.getPillCounts();
        // Setting the proper date for the pill counts
        if (pcnts != null) {
            for (PillCount pc : pcnts) {
                pc.setDateOfCount(btnCaptureDate.getDate());
            }
            pillFacade.save(pcnts);

        }

    }

    private java.util.List<String> getPossibleInHandOnExitValues(TableItem ti) {
        String drugName = ti.getText(0);
        java.util.List<String> possibleValuesList = new ArrayList<String>();
        if (rdBtnDispenseNow.getSelection()) {
            try {
                int disp = Integer.parseInt(ti.getText(2));

                Set<PillCount> pillCounts = pillCountTable.getPillCounts();
                for (PillCount count : pillCounts) {
                    if (count.getDrug().getName().equals(drugName)) {
                        int accum = count.getAccum();
                        if (accum != -1) {
                            possibleValuesList.add("" + (disp + accum));
                        }
                    }
                }
                possibleValuesList.add(disp + "");
            } catch (NumberFormatException n) {
                getLog().error("NumberFormatException parsing qty to dispense");
            }
        }
        return possibleValuesList;

    }

    /**
     * This method is used to highlight the text describing this particular
     * package (i.e. "This is package x of a n month's prescription"). If it is
     * the last package of a prescription, the text would be displayed in
     * ResourceUtils.getColor(iDartColor.RED) to highlight to the pharmacist
     * that this patient is in need of another prescription
     *
     * @param colorToHighlight
     */
    private void highlightPackageInfo(Color colorToHighlight) {

        lblPackageInfo1.setForeground(colorToHighlight);
        lblIndex.setForeground(colorToHighlight);
        lblPackageInfo2.setForeground(colorToHighlight);
        lblDuration.setForeground(colorToHighlight);
        lblPackageInfo3.setForeground(colorToHighlight);

    }

    private void init() {
        sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        fieldsEnabled = false;
    }

    private void lstWaitingPatientsWidgetSelected() {
        clearForm();
        Runnable longJob = new Runnable() {
            @Override
            public void run() {
                StructuredSelection selection = (StructuredSelection) lstWaitingPatients.getSelection();
                if (selection != null && !selection.isEmpty()) {
                    populatePatientDetails(((PatientIdAndName) selection.getFirstElement()).getId());
                    cmbWeeksSupplyChanged();
                }
            }
        };
        BusyIndicator.showWhile(getShell().getDisplay(), longJob);
    }

    protected void populatePatientDetails(String patientID, boolean forceSelect) {
        java.util.List<PatientIdAndName> patients = null;
        if (patientID == null || patientID.trim().isEmpty()) {
            patients = SearchManager.getActivePatientWithValidPrescriptionIDsAndNames(getHSession(),tipoPaciente);
        } else {
            patients = SearchManager.findPatientsWithIdLike(getHSession(), patientID);
        }

        lstWaitingPatients.setInput(patients);

        // Only try to load the patient if the user pressed enter.
        if (forceSelect) {
            if (patients.size() == 1) {
                populatePatientDetails(patients.get(0).getId());
            } else {
                String tmp = searchBar.getText();
                setScreenToInitialState();
                clearForm();
                searchBar.setText(tmp);
            }
        }

    }

    /**
     * Checks to see the paitent belonging to the prescription has a package
     * that has not yet been collected.
     *
     * @return true if patient has package awaiting collection
     */
    private boolean patientHasPackageAwaitingPickup() {

        if (previousPack != null) {
            // Check if package was created but not picked up
            if (previousPack.getPickupDate() == null)
                return true;
        }
        return false;
    }

    /**
     * This method populates the Patient Details and then runs the Populate
     * Prescription Details details This method is run on startup and when the
     * patient Id is clicked.
     *
     * @param patientID String
     */
    private void populatePatientDetails(int patientID) {
        localPatient = PatientManager.getPatient(getHSession(), patientID);
        previousPack = null;
        searchBar.setText(localPatient.getPatientId());
        searchBar.setSelection(0, localPatient.getPatientId().length());
        lstWaitingPatients.setSelection(new StructuredSelection(new PatientIdAndName(localPatient.getId(),
                localPatient.getPatientId(), localPatient.getFirstNames() + "," + localPatient.getLastname())));
        if (!localPatient.getAccountStatusWithCheck() || localPatient.getCurrentPrescription(tipoPaciente) == null) {
            showMessage(MessageDialog.ERROR, "O Paciente não pode ser dispensado.",
                    "Paciente está inativo ou não tem uma prescrição válida.");
            initialiseSearchList();
            clearForm();
            return;
        }

        Clinic clinic = localPatient.getCurrentClinic();
        setDispenseTypeFromClinic();
        lblClinic.setText(clinic.getClinicName());
        txtPatientId.setText(localPatient.getPatientId());
        txtPatientName.setText(localPatient.getFirstNames() + " " + localPatient.getLastname());
        txtPatientAge.setText("" + localPatient.getAge());
        if (localPatient.getDateOfBirth() != null) {
            txtPatientDOB
                    .setText(new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(localPatient.getDateOfBirth()));
        }

        if (localPatient.getAge() < 15) {
            lblPicChild.setVisible(true);

        } else {
            lblPicChild.setVisible(false);

        }

        if (!iDartProperties.summaryLabelDefault) {
            rdBtnPrintSummaryLabelYes.setSelection(false);
            rdBtnPrintSummaryLabelNo.setSelection(true);
        } else {
            rdBtnPrintSummaryLabelYes.setSelection(false);
            rdBtnPrintSummaryLabelNo.setSelection(true);
        }

        if(tipoPaciente.equalsIgnoreCase(iDartProperties.SERVICOTARV)) {
            rdBtnTARVPrescription.setSelection(true);
            rdBtnTBPrescription.setSelection(false);
        } else if(tipoPaciente.equalsIgnoreCase(iDartProperties.PNCT)){
            rdBtnTARVPrescription.setSelection(false);
            rdBtnTBPrescription.setSelection(true);
        }

        Prescription pre = localPatient.getCurrentPrescription(tipoPaciente);
        if (pre == null) {
            MessageBox noScript = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
            noScript.setText("O Paciente não tem uma prescrição válida");
            noScript.setMessage(
                    "Patient '".concat(localPatient.getPatientId()).concat("' não tem uma prescrição válida."));
            noScript.open();
            initialiseSearchList();
            clearForm();
        } else {
            newPack = new Packages();
            newPack.setPrescription(pre);
            newPack.setClinic(localPatient.getCurrentClinic());

            int numPeriods = Math.min(pre.getDuration(), 4);

            if (pre.getDispensaTrimestral() == 1) {
                if (pre.getDuration() <= (2 * 4)) {
                    numPeriods = pre.getDuration();
                } else {
                    numPeriods = 12; // (means 3 monnths)
                }
            } else if (pre.getDispensaSemestral() == 1) {
                if (pre.getDuration() < (6 * 4)) {
                    numPeriods = pre.getDuration();
                } else {
                    numPeriods = 24; // (means 6 monnths)
                }
            }

            newPack.setWeekssupply(numPeriods);

            btnCaptureDate.setDate(new Date());
            newPack.setPackDate(btnCaptureDate.getDate());

            previousPack = PackageManager.getLastPackagePickedUp(getHSession(), localPatient, tipoPaciente);

            if (patientHasPackageAwaitingPickup()) {
                MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                m.setText("O Paciente não pode ser dispensado");
                m.setMessage("Não pode dispensar medicamento para " + txtPatientId.getText()
                        + " uma vez que um frasco já foi criado em " + sdf.format(previousPack.getPackDate())
                        + " para este paciente. " + "\nEste frasco ainda não foi colectado pelo paciente. "
                        + "\n\n Se o frasco está correcto, porfavor de scanar "
                        + "para e entrega o paciente usando a tela 'Scanar frascos para os pacientes.' "
                        + "\n\nSe o frasco não está correcto, "
                        + "por favor apague-o usando a tela 'Apagar Stock, Prescription & Frascos' " + "");

                m.open();
                clearForm();
                return;
            } else {
                populatePrescriptionDetails();
                populatePresciptionTable();
                prepopulateQuantities();
            }

            enableFields(true);

            resetGUIforDispensingType();

            pillFacade = new PillCountFacade(getHSession());

            if (previousPack != null && rdBtnDispenseNow.getSelection()) {
                pillCountTable.populateLastPackageDetails(previousPack, newPack.getPackDate());
            }

            if (rdBtnDispenseNow.getSelection()) {
                // Display the next appointment date if there is one,
                // or else base next appointment on todays date.
                Appointment nextApp = PatientManager.getLatestAppointmentForPatient(localPatient, true);

                if (nextApp == null) {
                    Calendar theCal = Calendar.getInstance();
                    attemptCaptureDateReset();
                    theCal.setTime(newPack.getPackDate());

                    if (numPeriods <= 2) {
                        theCal.add(Calendar.DATE, numPeriods * 7);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    } else if (numPeriods == 4) {

                        theCal.add(Calendar.MONTH, 1);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    } else if (numPeriods == 8) {

                        theCal.add(Calendar.MONTH, 2);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    } else if (numPeriods == 12) {

                        theCal.add(Calendar.MONTH, 3);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    } else if (numPeriods == 16) {

                        theCal.add(Calendar.MONTH, 4);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    } else if (numPeriods == 20) {

                        theCal.add(Calendar.MONTH, 5);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    } else if (numPeriods == 24) {

                        theCal.add(Calendar.MONTH, 6);
                        adjustForNewDispDate(btnCaptureDate.getDate());
                        adjustForNewAppointmentDate(theCal.getTime());
                    }

                }


            }
        }
        if (pre.getMotivocriacaoespecial().contains("Perda")) {
            String dateExpected = PatientManager.lastNextPickup(getHSession(), patientID);
            if (dateExpected != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                Date dataproximolev = new Date(dateExpected);
                adjustForNewAppointmentDate(dataproximolev);
                btnNextAppDate.setText(sdf.format(dataproximolev));
                btnNextAppDate.setEnabled(false);

            }
        }
    }

    private void setDispenseTypeFromClinic() {
        Clinic clinic = localPatient
                .getClinicAtDate(btnCaptureDate.getDate() != null ? btnCaptureDate.getDate() : new Date());
        if (clinic == null) {
            showMessage(MessageDialog.ERROR, "Paciente não activo na data seleccionada ",
                    "O paciente não tem um episódio em aberto na data que você especificou então você não é capaz de dispensar a eles nesta data.\n\n "
                            + "Por favor, seleccione uma data em que o paciente foi ativo.\n\n "
                            + "Para ver quando o paciente esteve ativo, olhar para os seus episódios na tela 'Relatório do Histórico do Paciente' (pressione o botão no meio da tela para carregar este relatório).");
            btnCaptureDate.setDate(new Date());
        } else {
            boolean isMainClinic = clinic.isMainClinic();

            rdBtnDispenseNow.setSelection(isMainClinic & iDartProperties.dispenseDirectlyDefault);
            rdBtnDispenseLater.setSelection(!(isMainClinic & iDartProperties.dispenseDirectlyDefault));

            resetGUIforDispensingType();
        }
    }

    /**
     * Populates the prescription details on the form. This includes notes.
     */
    private void populatePrescriptionDetails() {

        int item = 2;

        if (localPharmacy == null) {
            getLog().error("Tried to populate prescription details, but localPharmacy is null");
            return;
        }
        if (newPack == null) {
            getLog().error("Tried to populate prescription details, but pack is null");
            return;
        }
        if (newPack.getPrescription() == null) {
            getLog().error("Tried to populate prescription details, but pack.prescription is null");
            return;
        }

        txtPrescriptionId.setText(newPack.getPrescription().getPrescriptionId());

        txtDoctor.setText(newPack.getPrescription().getDoctor().getFirstname() + " "
                + newPack.getPrescription().getDoctor().getLastname());

        txtPrescriptionDate.setText(sdf.format(newPack.getPrescription().getDate()));

        updateDateOfLastPickup();

        if (newPack.getPrescription().getNotes() != null) {
            txtAreaNotes.setText(newPack.getPrescription().getNotes());
        }
        int dur = newPack.getPrescription().getDuration();

        // Set the default index to 1
        int index = 1;

        // Make sure that it is not a new prescription.
        if (previousPack != null && (previousPack.getPrescription().getId() == newPack.getPrescription().getId()))
            index = (previousPack.getNextIssueNo());

        // if the prescription is valid less than one month
        if (dur < 4) {
            lblDuration.setText(String.valueOf(dur));
            lblPackageInfo3.setText("prescrição/semana");
            cmbSupply.setText(dur + " semana");
            lblIndex.setText(String.valueOf(index));

            highlightPackageInfo(ResourceUtils.getColor(iDartColor.RED));
            lblPicLeftArrow.setVisible(true);
            lblPicRightArrow.setVisible(true);
        }
        // else, the prescription is valid for at least 1 month
        else {
            lblDuration.setText(String.valueOf(dur / 4));
            lblPackageInfo3.setText("prescrição/mes");
            item = 1 + (newPack.getWeekssupply() / 4);
            cmbSupply.setText(cmbSupply.getItem(item));
            lblIndex.setText(String.valueOf(index));

            // check is prescription has been used up or if this is the last
            // repeat
            if ((dur / 4) <= index) {
                highlightPackageInfo(ResourceUtils.getColor(iDartColor.RED));
                lblPicLeftArrow.setVisible(true);
                lblPicRightArrow.setVisible(true);
            } else {
                lblPicLeftArrow.setVisible(false);
                lblPicRightArrow.setVisible(false);
            }
        }
    }

    /**
     *
     */
    private void updateDateOfLastPickup() {
        dateAlreadyDispensed = false;
        if (previousPack != null) {

            // Date of the [previous package] to be compared to the
            // current date, or the date set in the date picker
            Date lastPickupDate = previousPack.getPickupDate();

            // Date [selected using the package screen date picker].
            // This is the pack date, or the date captured as this
            // package is given to the patient.
            // A date before the previous package [is not allowed],
            // and this process should not continue.
            Date myPackDate = btnCaptureDate.getDate();

            // Test for pack date consistency.
            if (myPackDate != null && lastPickupDate != null && iDARTUtil.before(myPackDate, lastPickupDate))
                // do not perform this procedure, just break,
                // since an earlier pack date is not allowed
                // pack date must be greater then last pickup date.
                return;

            int numOfDays = iDARTUtil.getDaysBetween(lastPickupDate, (myPackDate == null ? new Date() : myPackDate));

            lblDateOfLastPickupContents.setText(numOfDays + " dias ("
                    + new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(lastPickupDate) + ")");

            if (numOfDays > ((previousPack.getWeekssupply() * 7) + 3)) {
                lblDateOfLastPickupContents.setForeground(ResourceUtils.getColor(iDartColor.RED));
                lblDateOfLastPickupContents.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
            } else if (numOfDays == 0) {
                lblDateOfLastPickupContents.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
                lblDateOfLastPickupContents.setBackground(ResourceUtils.getColor(iDartColor.YELLOW));
                dateAlreadyDispensed = true;
            } else {
                lblDateOfLastPickupContents.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
                lblDateOfLastPickupContents.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
            }

        } else {

            lblDateOfLastPickupContents.setText("Levantamento inicial");
            lblDateOfLastPickupContents.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
            lblDateOfLastPickupContents.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));

        }

    }

    /**
     *
     */
    private void populatePresciptionTable() {
        tblPrescriptionInfo.removeAll();
        String tempAmtPerTime = "";

        for (PrescribedDrugs pd : newPack.getPrescription().getPrescribedDrugs()) {

            if (new BigDecimal(pd.getAmtPerTime()).scale() == 0) {
                tempAmtPerTime = "" + new BigDecimal(pd.getAmtPerTime()).unscaledValue().intValue();
            } else {
                tempAmtPerTime = "" + pd.getAmtPerTime();
            }

            Form theForm = pd.getDrug().getForm();

            TableItem ti = new TableItem(tblPrescriptionInfo, SWT.NONE);
            String[] tableEntry = new String[6];

            tableEntry[0] = pd.getDrug().getName();

            if (theForm.getFormLanguage1().equals("")) // is a cream, no amount
            // per time
            {
                tableEntry[1] = theForm.getActionLanguage1() + " " + pd.getTimesPerDay() + " vezes por dia.";
            } else {
                tableEntry[1] = theForm.getActionLanguage1() + " " + tempAmtPerTime + " " + theForm.getFormLanguage1()
                        + " " + pd.getTimesPerDay() + " vezes por dia.";
            }
            tableEntry[2] = "0";
            tableEntry[4] = "0";
            tableEntry[5] = "0";

            /*
             * Highlight if drug isn't in stock
             */
            if (1 <= 10) {
                ti.setForeground(ResourceUtils.getColor(iDartColor.RED));
            }

            ti.setText(tableEntry);
            ti.setData(new ArrayList<PackageDrugInfo>());

        }

        populateStockDetails();
    }

    /**
     * Populates the units in stock column
     */
    private void populateStockDetails() {
        if (localPharmacy == null) {
            getLog().error("Tried to populate stock details, but localPharmacy is null");
            return;
        }
        if (newPack == null) {
            getLog().error("Tried to populate stock details, but pack is null");
            return;
        }
        if (newPack.getPrescription() == null) {
            getLog().error("Tried to populate stock details, but pack.prescription is null");
            return;
        }

        int count = 0;

        for (PrescribedDrugs pd : newPack.getPrescription().getPrescribedDrugs()) {

            TableItem ti = tblPrescriptionInfo.getItem(count);
            int[] unitsInStock = StockManager.getTotalStockLevelsForDrug(getHSession(), pd.getDrug(), localPharmacy);
            ti.setText(3, unitsInStock[0] + (unitsInStock[1] > 0 ? " (" + unitsInStock[1] + " loose)" : ""));
            count++;
        }

    }

    /**
     * This method is used to prepopulate the drugs in a prescription with a
     * default supply for the supply duration of the current pack eg. 60 tablets
     * for a month. The default is one pack per month.
     */
    private void prepopulateQuantities() {
        if (localPharmacy == null) {
            getLog().warn("Tried to prepopulate quantities, but localPharmacy is null");
            return;
        }

        if (newPack == null) {
            getLog().warn("Tried to prepopulate quantities, but pack is null");
            return;
        }

        if (newPack.getPrescription() == null) {
            getLog().warn("Tried to prepopulate quantities, but prescription is null");
            return;
        }

        Clinic theClinic = newPack.getPrescription().getPatient().getCurrentClinic();

        int count = 0;

        pbLoading.setMinimum(0);
        pbLoading.setMaximum(newPack.getPrescription().getPrescribedDrugs().size());

        for (PrescribedDrugs pd : newPack.getPrescription().getPrescribedDrugs()) {

            Drug theDrug = pd.getDrug();

            int repeatNumber = Integer.parseInt(lblIndex.getText().equalsIgnoreCase("") ? "0" : lblIndex.getText());

            int units = 0;
            int numlabels = 1;
            int weeksperUnits = 0;

            double unitsPerMonth;

            // for most items, which have an amountpertime, work out the
            // quanitity to prepopulates
            int packSize = theDrug.getPackSize();
            if (pd.getAmtPerTime() != 0.0) {

                if ((packSize % 28) == 0) {
                    unitsPerMonth = pd.getAmtPerTime() * pd.getTimesPerDay() * 28;
                } else {
                    unitsPerMonth = pd.getAmtPerTime() * pd.getTimesPerDay() * 30;
                }

                // round units up to multiple of packSize
                if (iDartProperties.roundUpForms.contains(theDrug.getForm().getForm())
                        && unitsPerMonth % packSize != 0) {
                    unitsPerMonth = (Math.floor(unitsPerMonth / packSize) + 1) * packSize;
                }

                switch (newPack.getWeekssupply()) {

                    case 1: // 1 week supply

                        // First, calculate the number of units required for the
                        // total supply
                        units = (int) (pd.getAmtPerTime() * pd.getTimesPerDay() * 7);

                        // round units up to multiple of packSize
                        if (iDartProperties.roundUpForms.contains(theDrug.getForm().getForm())) {

                            if (units % packSize != 0) {
                                int noOfPacks = units / packSize;
                                units = (noOfPacks + 1) * packSize;

                            }
                        }

                        break;

                    case 2: // half a month (14 or 15 days) supply

                        // First, calculate the number of units required for the
                        // total supply
                        units = (int) Math.ceil(unitsPerMonth / 2);

                        // round units up to multiple of packSize
                        if (iDartProperties.roundUpForms.contains(theDrug.getForm().getForm())) {
                            if (units % packSize != 0) {
                                int noOfPacks = units / packSize;
                                units = (noOfPacks + 1) * packSize;

                            }
                        }

                        break;

                    default:
                        // First, calculate the number of units required for the
                        // total supply
                        if ((packSize % 28) == 0) {
                            units = (int) ((newPack.getWeekssupply() / 4) * (pd.getAmtPerTime() * pd.getTimesPerDay() * 28));
                        } else {
                            units = (int) ((newPack.getWeekssupply() / 4) * (pd.getAmtPerTime() * pd.getTimesPerDay() * 30));
                        }

                        if (packSize > units)
                            units = packSize;

                        // Next round up syrup if required.
                        if (iDartProperties.roundUpForms.contains(theDrug.getForm().getForm())) {

                            if (units % packSize != 0) {
                                int noOfPacks = units / packSize;
                                units = (noOfPacks + 1) * packSize;

                            }
                        }

                }

                numlabels = (int) Math.ceil(((double) units / packSize));

            } else { // for side treatment items that don't have an
                // amountpertime e.g. creams (Apply 3 times a day), set
                // to 1 pack and 1 label
                units = packSize;
                numlabels = 1;
            }

            Stock theSock = StockManager.getSoonestExpiringStock(getHSession(), theDrug, units, localPharmacy);
            TableItem ti = tblPrescriptionInfo.getItem(count);

            if (theSock != null) {
                DecimalFormat df = new DecimalFormat();
                df.setDecimalSeparatorAlwaysShown(false);
                String amtPerTimeString = df.format(pd.getAmtPerTime());

                PackageDrugInfo pdi = new PackageDrugInfo(amtPerTimeString, theSock.getBatchNumber(),
                        theClinic.getClinicName(), units, theDrug.getForm().getFormLanguage1(),
                        theDrug.getForm().getFormLanguage2(), theDrug.getForm().getFormLanguage3(), theDrug.getName(),
                        theSock.getExpiryDate(), theClinic.getNotes(), txtPatientId.getText(),
                        localPatient.getFirstNames(), localPatient.getLastname(), theDrug.getDispensingInstructions1(),
                        theDrug.getDispensingInstructions2(), theSock.getId(), pd.getTimesPerDay(), numlabels,
                        theDrug.getSideTreatment() == 'T' ? true : false, LocalObjects.getUser(getHSession()),
                        new Date(), repeatNumber, newPack.getWeekssupply(), null,
                        PackageManager.getQuantityDispensedForLabel(newPack.getAccumulatedDrugs(), units,
                                theSock.getDrug().getName(), theSock.getDrug().getPackSize(), false, true),
                        PackageManager.getQuantityDispensedForLabel(newPack.getAccumulatedDrugs(), units,
                                theSock.getDrug().getName(), units, false, true),
                        PackageManager.getQuantityDispensedForLabel(newPack.getAccumulatedDrugs(), units,
                                theSock.getDrug().getName(), theSock.getDrug().getPackSize(), true, true),
                        newPack.getPrescription().getDuration(), txtNextAppDate.getText(), newPack.getPackageId());

                ti.setText(2, (new Integer(units)).toString());

                ti.setText(4, (new Integer(numlabels)).toString());

                ti.setText(5, (new Integer(units)).toString());

                ti.setForeground(ResourceUtils.getColor(iDartColor.BLACK));

                java.util.List<PackageDrugInfo> pdiList = new ArrayList<PackageDrugInfo>();
                pdiList.add(pdi);
                ti.setData(pdiList);

            } else {
                ti.setData(new ArrayList<PackageDrugInfo>());

                ti.setText(2, "0");

                ti.setText(4, "0");

                ti.setText(5, "0");

                ti.setForeground(ResourceUtils.getColor(iDartColor.RED));
            }
            count++;
            pbLoading.setSelection(count);

        }

    }

    /**
     * Method resetGUIforDispensingType.
     * <p>
     * boolean
     */
    private void resetGUIforDispensingType() {
        if (!rdBtnDispenseNow.getSelection()) {
            pillCountTable.clearTable();
            Control old2 = editorTblPrescriptionInfo.getEditor();
            if (old2 != null) {
                old2.dispose();
            }
            pillCountTable.getTable().setEnabled(false);
            // Disabling the label printing.
            rdBtnYesAppointmentDate.setEnabled(true);
            rdBtnYesAppointmentDate.setSelection(false);
            rdBtnNoAppointmentDate.setEnabled(true);
            rdBtnNoAppointmentDate.setSelection(false);
        } else {
            if (previousPack != null) {
                pillCountTable.getTable().setEnabled(true);
                pillCountTable.populateLastPackageDetails(previousPack, newPack.getPackDate());
            }
            // Re-enabling the printing of the next appointment date.
            if (iDartProperties.nextAppointmentDateOnLabels) {
                rdBtnYesAppointmentDate.setEnabled(true);
                rdBtnYesAppointmentDate.setSelection(false);
                rdBtnNoAppointmentDate.setEnabled(true);
                rdBtnNoAppointmentDate.setSelection(true);
            } else {
                rdBtnYesAppointmentDate.setEnabled(true);
                rdBtnYesAppointmentDate.setSelection(false);
                rdBtnNoAppointmentDate.setEnabled(true);
                rdBtnNoAppointmentDate.setSelection(true);
            }

        }
        setDateExpectedFields();
    }

    public void savePackageAndPackagedDrugsWhithQty0(boolean dispenseNow,
                                                     java.util.List<PackageDrugInfo> allPackageDrugsList, Prescription prescription, int i) {

        // if pack date is today, store the time too, else store 12am
        PackageDrugInfo packageDrugInfo = allPackageDrugsList.get(0);
        Set<Packages> packageses = new HashSet();
        packageses.clear();
        Date packDate = new Date();

        Packages newPack = new Packages();

        packDate.setTime(packageDrugInfo.getDispenseDate().getTime());
        newPack.setPickupDate(prescription.getDate());
        newPack.setPackDate(prescription.getDate());
        newPack.setPackageId(packageDrugInfo.getPackageId());
        newPack.setModified('T');
        newPack.setPrescription(prescription);
        newPack.getPrescription().setPackages(packageses);
        newPack.setPackageId(newPack.getPrescription().getPrescriptionId() + "-" + lblIndex.getText());
        newPack.setModified('T');
        newPack.setClinic(localPatient.getCurrentClinic());

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
            newPack.setDateLeft(prescription.getDate());
            newPack.setDateReceived(prescription.getDate());
            newPack.setPickupDate(prescription.getDate());
        } else {
            if (iDartProperties.downReferralMode
                    .equalsIgnoreCase(iDartProperties.OFFLINE_DOWNREFERRAL_MODE)) {
                newPack.setDateLeft(prescription.getDate());
                newPack.setDateReceived(prescription.getDate());
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
            pd.setStock(StockManager.getStock(getHSession(), pdi.getStockId()));
            pd.setModified('T');
            packagedDrugsList.add(pd);
            pdi.setPackagedDrug(pd);
            pdi.setNotes(packageDrugInfo.getNotes());
            pdi.setPackageId(newPack.getPackageId());

            if (rdBtnNoAppointmentDate.getSelection())
                pdi.setInhPickup(false);
            else
                pdi.setInhPickup(true);

            if(rdBtnPrintSummaryLabelNo.getSelection())
                pdi.setCtzPickup(false);
            else
                pdi.setCtzPickup(true);

        }

        newPack.setPackagedDrugs(packagedDrugsList);
        newPack.setDrugTypes("ARV");

        PackageManager.savePackageQty0(newPack);

    }

    /**
     * Method savePackageAndPackagedDrugs.
     *
     * @param dispenseNow         boolean
     * @param allPackageDrugsList java.util.List<PackageDrugInfo>
     * @throws Exception
     */
    private void savePackageAndPackagedDrugs(boolean dispenseNow, java.util.List<PackageDrugInfo> allPackageDrugsList)
            throws Exception {

        // if pack date is today, store the time too, else store 12am
        Date today = new Date();
        Date packDate = new Date();
        packDate.setTime(newPack.getPackDate().getTime());
        boolean checkOpenmrs = true;

        if (CentralizationProperties.centralization.equalsIgnoreCase("off"))
            checkOpenmrs = true;
        else if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("F")
                || CentralizationProperties.pharmacy_type.equalsIgnoreCase("P"))
            checkOpenmrs = false;

        if (DateFieldComparator.compare(today, packDate, Calendar.DAY_OF_MONTH) == 0) {
            newPack.setPickupDate(new Date());
        }

        newPack.setPackageId(newPack.getPrescription().getPrescriptionId() + "-" + lblIndex.getText());
        newPack.setModified('T');

        int numPeriods = getSelectedWeekSupply();
        getLog().info("getSelectedWeekSupply() called");
        newPack.setWeekssupply(numPeriods);
        /*
         * If the pharmacist is giving the drugs to the patient now, set the
         * dateLeft, dateReceived and pickupDate to today. Else ... set these
         * attributes to null (they will be set when the packages have left the
         * pharmacy, arrived at the remote clinic, and when the patient has
         * picked up their medications
         */
        if (dispenseNow) {
            newPack.setDateLeft(newPack.getPackDate());
            newPack.setDateReceived(newPack.getPackDate());
            newPack.setPickupDate(newPack.getPackDate());
        } else {
            if (iDartProperties.downReferralMode.equalsIgnoreCase(iDartProperties.OFFLINE_DOWNREFERRAL_MODE)) {
                newPack.setDateLeft(newPack.getPackDate());
                newPack.setDateReceived(newPack.getPackDate());
                newPack.setPickupDate(null);
            } else {
                newPack.setDateLeft(null);
                newPack.setDateReceived(null);
                newPack.setPickupDate(null);
            }
        }

        // Make up a set of package drugs for this particular package
        java.util.List<PackagedDrugs> packagedDrugsList = new ArrayList<PackagedDrugs>();

        for (int i = 0; i < allPackageDrugsList.size(); i++) {

            PackageDrugInfo pdi = allPackageDrugsList.get(i);
            PackagedDrugs pd = new PackagedDrugs();
            pd.setAmount(pdi.getDispensedQty());
            pd.setParentPackage(newPack);
            pd.setStock(StockManager.getStock(getHSession(), pdi.getStockId()));
            pd.setModified('T');
            packagedDrugsList.add(pd);
            if (rdBtnDispenseNow.getSelection()) {
                pdi.setDateExpectedString(sdf.format(btnNextAppDate.getDate()));
            } else {

                Appointment nextApp = PatientManager.getLatestAppointmentForPatient(newPack.getPrescription().getPatient(), true);

                if (nextApp != null)
                    pdi.setDateExpectedString(sdf.format(nextApp.getAppointmentDate()));
            }
            pdi.setPackagedDrug(pd);
            pdi.setNotes(localPatient.getCurrentClinic().getNotes());
            pdi.setPackageId(newPack.getPackageId());

            if (rdBtnNoAppointmentDate.getSelection())
                pdi.setInhPickup(false);
            else
                pdi.setInhPickup(true);

            if(rdBtnPrintSummaryLabelNo.getSelection())
                pdi.setCtzPickup(false);
            else
                pdi.setCtzPickup(true);
        }

        newPack.setPackagedDrugs(packagedDrugsList);
        newPack.setAccumulatedDrugs(getAccumDrugsToSave());

        // de-normalise table to speed up reports
        if (newPack.hasARVDrug())
            newPack.setDrugTypes("ARV");

        //PackageManager.savePackage(getHSession(), newPack);

        //
        int patientId = newPack.getPrescription().getPatient().getId();
        boolean aviado = false;
        Set<Episode> episodes = newPack.getClinic().getEpisodes();

        Episode patientEpisode = PatientManager.getLastEpisode(getHSession(), newPack.getPrescription().getPatient().getPatientId());

        if (patientEpisode == null) {
            for (Episode episode : episodes) {
                if (episode.getPatient().getId() == patientId) {
                    patientEpisode = episode;
//                   log.trace(episode.getStartReason());
                }
            }
        }

        if (patientEpisode.getStartReason().contains("nsito") || patientEpisode.getStartReason().contains("ternidade") || !checkOpenmrs) {
            PackageManager.savePackage(getHSession(), newPack);
            aviado = true;
        } else {

            Date dtPickUp = newPack.getPickupDate();

            Clinic clinic = AdministrationManager.getMainClinic(hSession);

            // EncounterDatetime
            String strPickUp = RestUtils.castDateToString(dtPickUp);

            // Patient NID
            String nid = newPack.getPrescription().getPatient().getPatientId().trim();

            // Regimen
            String regimenAnswer = newPack.getPrescription().getRegimeTerapeutico().getRegimenomeespecificado().trim();

            List<PrescribedDrugs> prescribedDrugs = newPack.getPrescription().getPrescribedDrugs();

            List<PackagedDrugs> packagedDrugs = newPack.getPackagedDrugs();

            // Next pick up date
            Date dtNextPickUp = btnNextAppDate.getDate();

            String strNextPickUp = RestUtils.castDateToString(dtNextPickUp);

            String strProvider = newPack.getPrescription().getDoctor().getFirstname().trim() + " "
                    + newPack.getPrescription().getDoctor().getLastname().trim();

            String providerWithNoAccents = org.apache.commons.lang3.StringUtils.stripAccents(strProvider);

            String facility = clinic.getClinicName().trim();

            if (getServerStatus(JdbcProperties.urlBase).contains("Red")) {

                PackageManager.savePackage(getHSession(), newPack);
                aviado = true;
                log.trace("Servidor Rest offline, o aviamento do paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] será armazenada para envio ao Openrms a posterior");
                saveOpenmrsPatientFila(newPack.getPrescription(), nid, strPickUp, localPatient.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                        facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                        iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
            } else {

                User currentUser = LocalObjects.getUser(HibernateUtil.getNewSession());

                assert currentUser != null;
                if (ApiAuthRest.loginOpenMRS(currentUser)) {
                // Add interoperability with OpenMRS through Rest Web Services
                RestClient restClient = new RestClient();

                String nidRest = restClient.getOpenMRSResource(iDartProperties.REST_GET_PATIENT + StringUtils.replace(nid, " ", "%20"));

                JSONObject jsonObject = new JSONObject(nidRest);
                JSONArray _jsonArray = (JSONArray) jsonObject.get("results");
                String nidUuid = null;

                for (int i = 0; i < _jsonArray.length(); i++) {
                    JSONObject results = (JSONObject) _jsonArray.get(i);
                    nidUuid = (String) results.get("uuid");
                }

//                String nidvoided = restClient.getOpenMRSResource(iDartProperties.REST_GET_PERSON_GENERIC + nidUuid);
//                JSONObject jsonObjectPerson = new JSONObject(nidvoided);
//                Boolean voided = (Boolean) jsonObjectPerson.get("voided");

                String uuid = localPatient.getUuidopenmrs();
                if (uuid != null && !uuid.isEmpty()) {
                    uuid = localPatient.getUuidopenmrs();
                } else {

                    PackageManager.savePackage(getHSession(), newPack);
                    aviado = true;
                    log.trace(" O aviamento do paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] será armazenada para envio ao Openrms apos a verificação do erro");
                    saveOpenmrsPatientFila(newPack.getPrescription(), nid, strPickUp, localPatient.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, "O NID deste paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] foi alterado no OpenMRS ou não possui UUID."
                            + " Por favor actualize o NID na Administração do Paciente usando a opção Atualizar um Paciente Existente.");
                    MessageBox m = new MessageBox(getShell(), SWT.OK |
                            SWT.ICON_ERROR);
                    m.setText("Problema dispensando o pacote de medicamentos");
                    m.setMessage("O NID deste paciente foi alterado no OpenMRS ou não possui UUID."
                            + " Por favor actualize o NID na Administração do Paciente usando a opção Atualizar um Paciente Existente." +
                            "\nEste aviamento será enviado ao Openrms após a verificacao do erro.");
                    m.open();

                    return;

                }

                if (nidUuid != null && !nidUuid.isEmpty()) {
                    if (!nidUuid.equals(uuid)) {

                        PackageManager.savePackage(getHSession(), newPack);
                        aviado = true;
                        log.trace(" O aviamento do paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] será armazenada para envio ao Openrms apos a verificação do erro");
                        saveOpenmrsPatientFila(newPack.getPrescription(), nid, strPickUp, localPatient.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                                facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                                iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                        saveErroLog(newPack, dtNextPickUp, "O paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] "
                                + " Tem um UUID [" + uuid + "] diferente ou inactivo no OpenMRS " + nidUuid + "]. Por favor actualize o UUID correspondente .");
                        MessageBox m = new MessageBox(getShell(), SWT.OK |
                                SWT.ICON_ERROR);
                        m.setText("Problema dispensando o pacote de medicamentos");
                        m.setMessage("O paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] " +
                                " Tem um UUID [" + uuid + "] diferente ou inactivo no OpenMRS " + nidUuid + "]. Por favor actualize o UUID correspondente no bloco de Administração" +
                                " do Paciente usando a opção Atualizar um Paciente Existente." +
                                "\nEste aviamento será enviado ao Openrms após a verificação do erro.");
                        m.open();

                        return;
                    }
                }

                String openrsMrsReportingRest = restClient.getOpenMRSReportingRest(iDartProperties.REST_GET_REPORTING_REST + uuid);

                JSONObject jsonReportingRest = new JSONObject(openrsMrsReportingRest);
                JSONArray jsonReportingRestArray = (JSONArray) jsonReportingRest.get("members");


                if (jsonReportingRestArray.length() < 1) {

                    PackageManager.savePackage(getHSession(), newPack);
                    aviado = true;
                    log.trace(" O aviamento do paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] será armazenada para envio ao Openrms apos a verificacao do erro");
                    saveOpenmrsPatientFila( newPack.getPrescription(), nid, strPickUp, localPatient.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, "NID [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + "com o uuid ( "+localPatient.getUuidopenmrs()+" )] inserido não se encontra no estado ACTIVO NO PROGRAMA/TRANSFERIDO DE" +
                            ". Actualize primeiro o estado do paciente no OpenMRS.");
                    MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                    m.setText("Informação sobre estado do programa");
                    m.setMessage("NID inserido não se encontra no estado ACTIVO NO PROGRAMA/TRANSFERIDO DE. Actualize primeiro o estado do paciente no OpenMRS." +
                            "\nEste aviamento será enviado ao Openrms após a verificacao do erro.");
                    m.open();

                    return;
                }

                String response = restClient.getOpenMRSResource(iDartProperties.REST_GET_PROVIDER + StringUtils.replace(providerWithNoAccents, " ", "%20"));
                String providerUuid = "";

                // Location
                String strFacility = restClient.getOpenMRSResource(iDartProperties.REST_GET_LOCATION + StringUtils.replace(facility, " ", "%20"));
                String strFacilityUuid = "";
                // Health Facility

                if (strFacility.length() < 50) {

                    PackageManager.savePackage(getHSession(), newPack);
                    aviado = true;
                    log.trace(" O aviamento do paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] será armazenada para envio ao Openrms apos a verificacao do erro");
                    saveOpenmrsPatientFila(newPack.getPrescription(), nid, strPickUp, localPatient.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, " O UUID DA UNIDADE SANITARIA NAO CONTEM O PADRAO RECOMENDADO PARA O NID [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ].");
                    MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                    m.setText("Informação sobre estado da Unidade Sanitaria");
                    m.setMessage("O UUID da Unidade Sanitaria nao contem o pradrao recomendado." + "\nEste aviamento será enviado ao Openrms após a verificacao do erro.");
                    m.open();

                    return;
                } else strFacilityUuid = strFacility.substring(21, 57);

                if (response.length() < 50) {

                    PackageManager.savePackage(getHSession(), newPack);
                    aviado = true;
                    log.trace(" O aviamento do paciente [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ] será armazenada para envio ao Openrms apos a verificacao do erro");
                    saveOpenmrsPatientFila(newPack.getPrescription(), nid, strPickUp, localPatient.getUuidopenmrs(), iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            facility, iDartProperties.FORM_FILA, providerWithNoAccents, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, iDartProperties.DOSAGE, iDartProperties.VISIT_UUID, strNextPickUp);
                    saveErroLog(newPack, dtNextPickUp, " O UUID DO PROVEDOR NAO CONTEM O PADRAO RECOMENDADO OU NAO EXISTE NO OPENMRS PARA O NID [" + localPatient.getPatientId() + " - " + localPatient.getFirstNames() + " " + localPatient.getLastname() + " ].");
                    MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                    m.setText("Informação sobre estado do Provedor");
                    m.setMessage("O UUID do PRovedor nao contem o pradrao recomendado ou nao existe no OpenMRS." + "\nEste aviamento será enviado ao Openrms após a verificacao do erro.");
                    m.open();

                    return;
                } else providerUuid = response.substring(21, 57);

                try {

                    postOpenMrsEncounterStatus = restClient.postOpenMRSEncounter(strPickUp, uuid, iDartProperties.ENCOUNTER_TYPE_PHARMACY,
                            strFacilityUuid, iDartProperties.FORM_FILA, providerUuid, iDartProperties.REGIME, regimenAnswer,
                            iDartProperties.DISPENSED_AMOUNT, prescribedDrugs, packagedDrugs, iDartProperties.DOSAGE,
                            iDartProperties.VISIT_UUID, strNextPickUp);

                    log.trace("Criou o fila no openmrs para o paciente " + patientId + ": " + postOpenMrsEncounterStatus);

                    if (postOpenMrsEncounterStatus) {
                        PackageManager.savePackage(getHSession(), newPack);
                        aviado = true;
                        OpenmrsErrorLog errorLog = OpenmrsErrorLogManager.getErrorLog(getHSession(), newPack.getPrescription());
                        if (errorLog != null)
                            OpenmrsErrorLogManager.removeErrorLog(getHSession(), errorLog);
                    }

                } catch (Exception e) {
                    log.trace("Nao foi criado o fila no openmrs para o paciente " + patientId + ": " + postOpenMrsEncounterStatus);
                    getLog().info(e.getMessage());
                    saveErroLog(newPack, dtNextPickUp, "Nao foi criado o fila no openmrs para o paciente " + patientId + ": " + e.getMessage());
                    MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                    m.setText("Problema salvando o pacote de medicamentos");
                    m.setMessage("Houve um problema ao salvar o pacote de medicamentos para o paciente " + nid + ". " + "Por favor contacte o SIS.");

                    m.open();
                }
                }else {
                    log.error("O Utilizador "+currentUser.getUsername()+" não se encontra no OpenMRS ou serviço rest no OpenMRS não está  em funcionamento.");
                }
            }
        }

        if(aviado){
            MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_WORKING);
            m.setText("Aviamento efectuado com sucesso");
            m.setMessage("O pacote de medicamentos foi aviado para o paciente [ " + newPack.getPrescription().getPatient().getPatientId()+ " ]");
            m.open();
        }
    }

    /**
     * Method setARVStartDate.
     *
     * @param startDate String
     * @return PatientAttribute
     */
    private PatientAttribute setARVStartDate(String startDate) {
        PatientAttribute pa = newPack.getPrescription().getPatient()
                .getAttributeByName(PatientAttribute.ARV_START_DATE);

        if (pa == null) {

            pa = new PatientAttribute();
            pa.setType(PatientManager.getAttributeTypeObject(getHSession(), PatientAttribute.ARV_START_DATE));
            pa.setPatient(newPack.getPrescription().getPatient());

        }

        pa.setValue(startDate);
        return pa;
    }

    private void setDateExpectedFields() {

        if (rdBtnDispenseNow.getSelection()) {

            lblNextAppointment.setText("Próximo Levantamento:");
            btnNextAppDate.setEnabled(true);
            btnNextAppDate.setVisible(true);
            txtNextAppDate.setVisible(false);

            if (!cmbSupply.getText().equals("")) {
                int numPeriods = getSelectedWeekSupply();

                Calendar theCal = Calendar.getInstance();

                theCal.setTime(btnCaptureDate.getDate());
                //theCal.add(Calendar.DATE, numPeriods * 7);
                //	adjustForNewAppointmentDate(theCal.getTime());
                //adjustForNewDispDate(btnCaptureDate.getDate());


                if (numPeriods == 1 || numPeriods == 2) {
                    theCal.add(Calendar.DATE, numPeriods * 7);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                } else if (numPeriods == 4) {

                    theCal.add(Calendar.MONTH, 1);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                } else if (numPeriods == 8) {

                    theCal.add(Calendar.MONTH, 2);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                } else if (numPeriods == 12) {

                    theCal.add(Calendar.MONTH, 3);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                } else if (numPeriods == 16) {

                    theCal.add(Calendar.MONTH, 4);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                } else if (numPeriods == 20) {

                    theCal.add(Calendar.MONTH, 5);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                } else if (numPeriods == 24) {

                    theCal.add(Calendar.MONTH, 6);
                    adjustForNewAppointmentDate(theCal.getTime());
                    adjustForNewDispDate(btnCaptureDate.getDate());
                }

                adjustForNewDispDate(btnCaptureDate.getDate());
            }
        }
        // else, patient is inactive
        else {
            // set the colours for all the active fields
            lblNextAppointment.setText("Levantamento Actual:");
            btnNextAppDate.setVisible(false);
            txtNextAppDate.setVisible(true);

            if ((newPack != null) && (newPack.getPrescription() != null)) {
                Appointment nextApp = PatientManager
                        .getLatestAppointmentForPatient(newPack.getPrescription().getPatient(), true);

                if (nextApp != null) {
                    adjustForNewDispDate(btnCaptureDate.getDate());
                    adjustForNewAppointmentDate(nextApp.getAppointmentDate());
                } else {
                    txtNextAppDate.setText("");
                }
            }
        }

        Prescription pre = localPatient.getCurrentPrescription(tipoPaciente);

        if (pre.getMotivocriacaoespecial().contains("Perda")) {
            String dateExpected = PatientManager.lastNextPickup(getHSession(), localPatient.getId());
            if (dateExpected != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                Date dataproximolev = new Date(dateExpected);
                adjustForNewAppointmentDate(dataproximolev);
                btnNextAppDate.setText(sdf.format(dataproximolev));
                btnNextAppDate.setEnabled(false);
            }
        }

    }

    private void adjustForNewAppointmentDate(Date theNextAppDate) {
        Calendar theNextAppCal = Calendar.getInstance();
        theNextAppCal.setTime(theNextAppDate);

        if (rdBtnDispenseNow.getSelection()) {
            btnNextAppDate.setDate(theNextAppDate);
        } else {
            txtNextAppDate.setText(sdf.format(theNextAppDate));
        }
    }

    /**
     * Sets the in hand on exit column in the current package table.
     *
     * @param pc
     */
    public void setInHandOnExit(PillCount pc) {
        Drug theDrug = pc.getDrug();

        for (int i = 0; i < tblPrescriptionInfo.getItemCount(); i++) {

            String drugName = tblPrescriptionInfo.getItem(i).getText(0);

            if (drugName.equals(theDrug.getName())) {
                try {
                    int qty = Integer.parseInt(tblPrescriptionInfo.getItem(i).getText(2));

                    if (pc.getAccum() == -1) {
                        tblPrescriptionInfo.getItem(i).setText(5, "" + qty);

                    } else {
                        if (iDartProperties.accumByDefault) {
                            tblPrescriptionInfo.getItem(i).setText(5, "" + (pc.getAccum() + qty));
                        } else {
                            tblPrescriptionInfo.getItem(i).setText(5, "" + qty);
                        }

                    }

                } catch (NumberFormatException e) {
                    getLog().error("NumberFormatException parsing qty to dispense");
                    tblPrescriptionInfo.getItem(i).setText(5, "");
                }

            }

        }
    }

    /**
     * Clears the in hand on exit column in the current package table.
     */
    public void clearInHandOnExit() {

        for (int i = 0; i < tblPrescriptionInfo.getItemCount(); i++) {

            try {
                int qty = Integer.parseInt(tblPrescriptionInfo.getItem(i).getText(2));
                tblPrescriptionInfo.getItem(i).setText(5, "" + qty);

            } catch (NumberFormatException e) {
                getLog().error("NumberFormatException parsing qty to dispense");
                tblPrescriptionInfo.getItem(i).setText(5, "");
            }

        }
    }

    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }

    public void setNextAppointmentDate() {
        Date theNextAppDate = btnNextAppDate.getDate();
        Date captureDate = btnCaptureDate.getDate();

        Patient pat = newPack.getPrescription().getPatient();

        /*
         * NOTE: we only get the current appointment date if the package is
         * created for late pickup. If the package is dispensed directly, we
         * create a new appointment
         */
        if (rdBtnDispenseNow.getSelection()) {
            // if the next appointment is in the future, set it to the disp date
            PatientManager.setNextAppointmentDateAtVisit(getHSession(), pat, captureDate, theNextAppDate);
        }
    }

    private void setScreenToInitialState() {
        newPack = new Packages();
        newPack.setPackDate(new Date());
        btnCaptureDate.setDate(new Date());
        btnNextAppDate.setDate(new Date());
        lblDateOfLastPickupContents.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        lblDateOfLastPickupContents.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
    }

    /**
     * Method submitForm.
     *
     * @return boolean
     */
    @Override
    protected boolean submitForm() {
        return false;
    }

    /**
     * Once the Submit button has been pressed, create the package, add in the
     * packaged stock entries
     *
     * @param dispenseNow          boolean
     * @param allPackagedDrugsList java.util.List<PackageDrugInfo>
     * @throws Exception
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void submitForm(boolean dispenseNow, java.util.List<PackageDrugInfo> allPackagedDrugsList)
            throws Exception {

        Transaction tx = null;
        Map<Object, Integer> labelQuantities = new HashMap<Object, Integer>();
        try {
            ConexaoJDBC conn = new ConexaoJDBC();
            if (conn.dispensadonoperiodo(allPackagedDrugsList.get(0).getPatientId())) {
                log.trace("Num of Days: " + dias);
                MessageBox mbox = new MessageBox(new Shell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
                mbox.setText("Levantamento de ARVs");

                mbox.setMessage("ATENÇÃO:\nO PACIENTE " + txtPatientId.getText()
                        + " JÁ FORAM DISPENSADOS MEDICAMENTOS NESTE PERÍODO!\n \nA ÚLTIMA DISPENSA FOI HÁ " + dias
                        + " DIA(s)\n \nQUER DISPENSAR DE NOVO?");

                switch (mbox.open()) {

                    case SWT.YES: {
                        // ***************Ainda a configurar a informacao mais
                        // correcta

                        tx = getHSession().beginTransaction();
                        if (newPack.getPrescription() != null) {
                            if (previousPack != null) {
                                savePillCounts();
                            }

                            // Check if package contains ARV, and
                            // do ARV Start date check.
                            // Should be moved to a facade or manager class
                            for (PackageDrugInfo info : allPackagedDrugsList) {
                                if (!info.isSideTreatment()) {
                                    // Check the ARV Start Date if an
                                    // ARV package is present.
                                    checkARVStartDate();
                                    break;
                                }
                            }

                            savePackageAndPackagedDrugs(dispenseNow, allPackagedDrugsList);
                            getLog().info("savePackageAndPackagedDrugs() called");
                            setNextAppointmentDate();

                            if (allPackagedDrugsList.size() > 0) {

                                // This map keeps a track of drugs dispensed in
                                // separate
                                // batches
                                Map<String, String> drugNames = new HashMap<String, String>();

                                // Update pdi's to include accum drugs
                                for (PackageDrugInfo info : allPackagedDrugsList) {

                                    info.setQtyInHand(PackageManager.getQuantityDispensedForLabel(
                                            newPack.getAccumulatedDrugs(), info.getDispensedQty(), info.getDrugName(),
                                            info.getPackagedDrug().getStock().getDrug().getPackSize(), false, true));

                                    labelQuantities.put(info, 1);
                                    // set the String that will print out on each
                                    // drug label
                                    // to indicate
                                    // (<amount dispensed> + <accumulated amount>)

                                    if (drugNames.containsKey(info.getDrugName())) {
                                        // not first batch, exclude pillcount value
                                        info.setSummaryQtyInHand(PackageManager.getQuantityDispensedForLabel(
                                                newPack.getAccumulatedDrugs(), info.getDispensedQty(), info.getDrugName(),
                                                info.getDispensedQty(), false, false));
                                    } else {

                                        info.setSummaryQtyInHand(PackageManager.getQuantityDispensedForLabel(
                                                newPack.getAccumulatedDrugs(), info.getDispensedQty(), info.getDrugName(),
                                                info.getDispensedQty(), false, true));
                                    }
                                    drugNames.put(info.getDrugName(), "test");

                                    TemporaryRecordsManager.savePackageDrugInfosToDB(getHSession(), allPackagedDrugsList);

                                }
                            }
                            getHSession().flush();
                            tx.commit();


                            Vector<String> vMedicamentos = new Vector<String>();

                            // Inicializa as variaveis para a insercao n acess

                            Date dispenseDate = allPackagedDrugsList.get(0).getDispenseDate();

                            String dateExpectedString = allPackagedDrugsList.get(0).getDateExpectedString();

                            List<PrescriptionToPatient> lp = conn.listPtP(allPackagedDrugsList.get(0).getPatientId());

                            Date dataInicioNoutroServico = lp.get(0).getDataInicionoutroservico();

                            for (int i = 0; i < allPackagedDrugsList.size(); i++) {
                                // adiciona os medicantos no vector
                                vMedicamentos.add(new String(allPackagedDrugsList.get(i).getDrugName()));

                            }

                            if (vMedicamentos != null)
                                for (int i = 0; i < vMedicamentos.size(); i++)
                                    log.trace("Medicamento " + i + " " + vMedicamentos.get(i));

                            // convertendo a data para adequar com a coluna datatarv do
                            // ms access - t_tarv
                            SimpleDateFormat parseFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                            Date datatarv = parseFormat.parse(dispenseDate.toString());
                            SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                            String resultdatatarv = format.format(datatarv);

                            // Convertendo a data de String para Date. proxima visita
                            Date dateproximavisita = conn.converteData(dateExpectedString);
                            log.trace(" Date proxima: " + dateproximavisita);

                            SimpleDateFormat parseFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            Date dateproxima = parseFormat2.parse(dateproximavisita.toString());
                            SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                            String resultdataproximaconsulta = format2.format(dateproxima);

                            String dataNoutroServico = "";
                            if (dataInicioNoutroServico != null)
                                dataNoutroServico = format2.format(dataInicioNoutroServico);
                        }

                        break;
                    }
                }

            } else {
                tx = getHSession().beginTransaction();
                if (newPack.getPrescription() != null) {
                    if (previousPack != null) {
                        savePillCounts();
                    }

                    // Check if package contains ARV, and
                    // do ARV Start date check.
                    // Should be moved to a facade or manager class
                    for (PackageDrugInfo info : allPackagedDrugsList) {
                        if (!info.isSideTreatment()) {
                            // Check the ARV Start Date if an
                            // ARV package is present.
                            checkARVStartDate();
                            break;
                        }
                    }

                    savePackageAndPackagedDrugs(dispenseNow, allPackagedDrugsList);
                    getLog().info("savePackageAndPackagedDrugs() called");
                    setNextAppointmentDate();

                    if (allPackagedDrugsList.size() > 0) {

                        // This map keeps a track of drugs dispensed in separate
                        // batches
                        Map<String, String> drugNames = new HashMap<String, String>();

                        // Update pdi's to include accum drugs
                        for (PackageDrugInfo info : allPackagedDrugsList) {

                            info.setQtyInHand(PackageManager.getQuantityDispensedForLabel(newPack.getAccumulatedDrugs(),
                                    info.getDispensedQty(), info.getDrugName(),
                                    info.getPackagedDrug().getStock().getDrug().getPackSize(), false, true));

                            labelQuantities.put(info, 1);
                            // set the String that will print out on each drug
                            // label
                            // to indicate
                            // (<amount dispensed> + <accumulated amount>)

                            if (drugNames.containsKey(info.getDrugName())) {
                                // not first batch, exclude pillcount value
                                info.setSummaryQtyInHand(PackageManager.getQuantityDispensedForLabel(
                                        newPack.getAccumulatedDrugs(), info.getDispensedQty(), info.getDrugName(),
                                        info.getDispensedQty(), false, false));
                            } else {

                                info.setSummaryQtyInHand(PackageManager.getQuantityDispensedForLabel(
                                        newPack.getAccumulatedDrugs(), info.getDispensedQty(), info.getDrugName(),
                                        info.getDispensedQty(), false, true));
                            }
                            drugNames.put(info.getDrugName(), "test");

                            TemporaryRecordsManager.savePackageDrugInfosToDB(getHSession(), allPackagedDrugsList);

                        }
                    }
                    getHSession().flush();
                    tx.commit();
                }


                Vector<String> vMedicamentos = new Vector<String>();


                Date dispenseDate = allPackagedDrugsList.get(0).getDispenseDate();
                String dateExpectedString = allPackagedDrugsList.get(0).getDateExpectedString();
                List<PrescriptionToPatient> lp = conn.listPtP(allPackagedDrugsList.get(0).getPatientId());
                Date dataInicioNoutroServico = lp.get(0).getDataInicionoutroservico();

                for (int i = 0; i < allPackagedDrugsList.size(); i++) {
                    // adiciona os medicantos no vector
                    vMedicamentos.add(new String(allPackagedDrugsList.get(i).getDrugName()));

                }

                if (vMedicamentos != null)
                    for (int i = 0; i < vMedicamentos.size(); i++)
                        log.trace("Medicamento " + i + " " + vMedicamentos.get(i));

                SimpleDateFormat parseFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                Date datatarv = parseFormat.parse(dispenseDate.toString());
                SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                String resultdatatarv = format.format(datatarv);

                // Convertendo a data de String para Date. proxima visita
                Date dateproximavisita = conn.converteData(dateExpectedString);
//               log.trace(" Date proxima: " + dateproximavisita);

                SimpleDateFormat parseFormat2 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date dateproxima = parseFormat2.parse(dateproximavisita.toString());
                SimpleDateFormat format2 = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
                String resultdataproximaconsulta = format2.format(dateproxima);

                String dataNoutroServico = "";
                if (dataInicioNoutroServico != null)
                    dataNoutroServico = format2.format(dataInicioNoutroServico);

            }

        } catch (HibernateException he) {// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
            getLog().error("Problem with Saving this package", he);
            if (tx != null) {
                tx.rollback();
            }

            MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
            m.setText("Problema salvando o pacote de medicamentos");
            m.setMessage("Houve um problema ao salvar este pacote de medicamentos");

            m.open();
        }

        setScreenToInitialState();

        tblPrescriptionInfo.clearAll();
        pillCountTable.clearTable();

    }

    /**
     * If the patient meets the following requirements the user is prompted
     * whether or not to add an ARVstartDate to the patient.
     * <ul>
     * <li>Patient does not have any previous packages that contain ARV drugs.
     * </li>
     * <li>Patient does not already have an ARVStartDate attribute.</li>
     * <li>Pharmacist is dispesing for direct pickup.</li>
     * <li>The start reason for the patient's most recent episode is 'New
     * Patient'</li>
     * </ul>
     *
     * @throws HibernateException
     */
    private void checkARVStartDate() throws HibernateException {
        Patient pat = newPack.getPrescription().getPatient();
        if (PackageManager.getMostRecentARVPackage(getHSession(), pat) == null
                && (pat.getAttributeByName(PatientAttribute.ARV_START_DATE)) == null && rdBtnDispenseNow.getSelection()
                && (Episode.REASON_NEW_PATIENT
                .equalsIgnoreCase(PatientManager.getMostRecentEpisode(pat).getStartReason()))) {

            getLog().info("ARV start date not set, asking user if it should be set to this package's pack date");

            MessageBox mbox = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            mbox.setText("A data de início TARV não foi especificada");
            mbox.setMessage("A data de início TARV não foi especificada e esta é a primeira vez que o paciente '"
                    + txtPatientId.getText()
                    + "'está a receber ARV. \n\nGostaria de definir a data de início de TARV para "
                    + btnCaptureDate.getText() + " agora?");

            switch (mbox.open()) {
                case SWT.YES:
                    PatientManager.addPatientAttributeToPatient(getHSession(), pat,
                            setARVStartDate(btnCaptureDate.getText()));
                    getLog().info("ARV start date has been set");
                    break;
            }
        }
    }


    public static void saveOpenmrsPatientFila(Prescription prescribed, String nid, String encounterDatetime, String nidUuid, String encounterType, String strFacility,
                                              String filaUuid, String provider, String regimeUuid,
                                              String strRegimenAnswerUuid, String dispensedAmountUuid, String dosageUuid, String returnVisitUuid, String strNextPickUp) {
        // Adiciona FILA para a sincronizacao.
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        SyncOpenmrsDispense syncOpenmrsDispense = null;

        try {
            Prescription prescription = PackageManager.getPrescription(sess, prescribed.getPrescriptionId());

            if (prescription != null) {

                syncOpenmrsDispense = PrescriptionManager.getSyncOpenmrsDispenseByPrescription(sess, prescription, encounterDatetime);

                if (syncOpenmrsDispense == null)
                    syncOpenmrsDispense = new SyncOpenmrsDispense();

                syncOpenmrsDispense.setStrPickUp(encounterDatetime);
                syncOpenmrsDispense.setNid(nid);
                syncOpenmrsDispense.setUuid(nidUuid);
                syncOpenmrsDispense.setEncounterType(encounterType);
                syncOpenmrsDispense.setStrFacility(strFacility);
                syncOpenmrsDispense.setFilaUuid(filaUuid);
                syncOpenmrsDispense.setProvider(provider);
                syncOpenmrsDispense.setRegimeUuid(regimeUuid);
                syncOpenmrsDispense.setRegimenAnswer(strRegimenAnswerUuid);
                syncOpenmrsDispense.setDispensedAmountUuid(dispensedAmountUuid);
                syncOpenmrsDispense.setDosageUuid(dosageUuid);
                syncOpenmrsDispense.setReturnVisitUuid(returnVisitUuid);
                syncOpenmrsDispense.setStrNextPickUp(strNextPickUp);
                syncOpenmrsDispense.setPrescription(prescription);

                syncOpenmrsDispense.setSyncstatus('P');

                PrescriptionManager.saveSyncOpenmrsPatienFila(sess, syncOpenmrsDispense);
            }
            sess.flush();
            tx.commit();
            sess.close();
        }catch (Exception e){
            if (tx != null) {
                tx.rollback();
                sess.close();
            }
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
                errorLog.setErrordescription(error);
                errorLog.setDatacreated(new Date());
                OpenmrsErrorLogManager.saveOpenmrsRestLog(sess, errorLog);
            }
            sess.flush();
            tx.commit();
            sess.close();
        }catch (Exception e){
            if (tx != null) {
                tx.rollback();
                sess.close();
            }
        }
    }

}
