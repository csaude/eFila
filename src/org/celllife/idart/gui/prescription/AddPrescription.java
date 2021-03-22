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
package org.celllife.idart.gui.prescription;

import model.manager.*;
import model.manager.reports.PatientHistoryReport;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.celllife.function.AndRule;
import org.celllife.function.DateRuleFactory;
import org.celllife.function.IRule;
import org.celllife.idart.commonobjects.*;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.doctor.AddDoctor;
import org.celllife.idart.gui.misc.iDARTChangeListener;
import org.celllife.idart.gui.packaging.NewPatientPackaging;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.search.PatientSearch;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.gui.widget.*;
import org.celllife.idart.integration.eKapa.gui.SearchPatientGui;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.FloatValidator;
import org.celllife.idart.misc.PatientBarcodeParser;
import org.celllife.idart.misc.iDARTUtil;
import org.celllife.idart.rest.ApiAuthRest;
import org.celllife.idart.rest.utils.RestClient;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static org.celllife.idart.commonobjects.CommonObjects.enableContentProposal;
import static org.celllife.idart.rest.ApiAuthRest.getServerStatus;

public class AddPrescription extends GenericFormGui implements
        iDARTChangeListener {

    private Button btnAddDrug;

    private Button btnAddAssociatedDrug;

    private Button btnSearch;

    private Button btnEkapaSearch;

    private TableColumn clmAmt;

    private TableColumn clmDrugName;

    private TableColumn clmSpace;

    private TableColumn clmTake;

    private Text txtClinic;

    private CCombo cmbDoctor;

    private CCombo cmbDuration;

    private CCombo cmbUpdateReason;

    private CCombo cmbMotivoMudanca;

    private Composite compButtonsMiddle;

    private Group grpDrugs;

    private Group grpParticulars;

    private Group grpPatientID;

    private Group grpPatientMDS;

    private int intDrugTableSize = 1;

    private Label lblNewPrescriptionId;

    private Label lblPicChild;

    private Label lblUpdateReason;

    private Label lblDataInicioNoutroServico;

    private Label lblMotivoMudanca;

    private Label lblDispensaTrimestral;

    private Label lblDispensaSemestral;

    private CCombo cmbDispensaTrimestral;

    private CCombo cmbDispensaSemestral;

    private CCombo cmbTipoDispensaSemestral;

    private CCombo cmbLinha;

    private Label lblTipoDispensaSemestral;

    private Label lblTipoDispensaTrimestral;

    private CCombo cmbTipoDispensaTrimestral;

    private DateButton btnDataInicioNoutroServico;

    private Button chkBtnPPE;

    private Button chkBtnPrEP;

    private Button chkBtnCE;

    private Button chkBtnTB;

    private Button chkBtnCPN;

    private Button chkBtnCCR;

    private Button chkBtnSAAJ;

    private Button chkBtnGAAC;

    private Button chkBtnAF;

    private Button chkBtnCA;

    private Button chkBtnFR;

    private Button chkBtnDC;

    private Button chkBtnPrescicaoEspecial;

    private CCombo cmbMotivoCriacao;

    private Patient thePatient;

    private TableColumn tblDescription;

    private TableColumn tblQnty;

    private Table tblDrugs;

    private TableColumn tblPerDay;

    private TableColumn tblTPD;

    private TableEditor editor;

    private Text txtAge;

    private Text txtDOB;

    private Text txtName;

    private Text txtPatientId;

    private Text txtSurname;

    private boolean isInitialPrescription;

    private Composite compInstructions;

    public Prescription localPrescription;

    private Set<Prescription> patientsPrescriptions;

    private Label lblPicAddDrug;

    private Composite compDispense;

    private Button btnDispenseDrugs;

    private Label lblPicDispenseDrugs;

    private Button btnAddDoctor;

    private Text txtAreaNotes;

    private Button btnPatientHistoryReport;

    private Text txtWeight;

    private Combo cmbRegime;

    private Button btnRemoveDrug;

    private Button btnMoveUp;

    private Button btnMoveDown;

    boolean fromShortcut;

    RegimeTerapeutico localRegime;

    private DateButton btnCaptureDate;

    /**
     * Constructor
     *
     * @param patient      String
     * @param theParent    Shell
     * @param fromShortcut boolean
     */
    // this method happens when loading up this screen
    public AddPrescription(Patient patient, Shell theParent,
                           boolean fromShortcut) {
        super(theParent, HibernateUtil.getNewSession());
        this.fromShortcut = fromShortcut;
        localPrescription = new Prescription();

        if (patient != null) {

            Patient pat = PatientManager.getPatient(getHSession(), patient.getId());
            localPrescription.setPatient(pat);

            if (!pat.getAccountStatusWithCheck()) {
                MessageBox noPatient = new MessageBox(getShell(),
                        SWT.ICON_ERROR | SWT.OK);

                noPatient.setText(Messages.getString("patient.prescription.dialog.noactualepisode"));
                noPatient
                        .setMessage("O Paciente "
                                + (txtPatientId.getText()).toUpperCase()
                                + " não tem episódio actual. \n\nVocê precisa começar um novo episódio para este paciente (Usando a tela 'Actualizar Paciente Existente') antes de registar a sua Prescrição.");
                noPatient.open();
                txtPatientId.setFocus();
                txtPatientId.setText("");
            } else {
                thePatient = pat;
                checkFirstPrescription();
                loadPatientDetails();
                enableFields(true);

                txtPatientId.setEnabled(false);
                btnSearch.setEnabled(false);
                btnEkapaSearch.setEnabled(false);

                if (isInitialPrescription) {
                    setFormToInitialPrescription();
                } else {
                    Prescription script = thePatient
                            .getMostRecentPrescription();
                    if (script != null) {
                        localPrescription = script;
                        loadPrescriptionDetails();
                        avaliaMDSSelectionActual();
                        cmbUpdateReason.setEnabled(true);
                    } else {
                        setFormToInitialPrescription();
                    }
                }
            }
        }
    }

    /**
     * This method initializes getMyShell()
     */
    @Override
    protected void createShell() {
        String shellTxt = Messages.getString("addPrescription.title"); //$NON-NLS-1$
        Rectangle bounds = new Rectangle(50, 0, 900, 700);
        // Parent Generic Methods ------
        buildShell(shellTxt, bounds); // generic shell build
    }

    @Override
    protected void createContents() {
        createCompInstructions();
        createGrpPatientID();
        createParticularsGroup();
        createCompButtonsMiddle();
        createGrpDrugs();
        createCompDispense();
        clearForm();

    }

    /**
     * Method addDisposeListener.
     *
     * @param dl DisposeListener
     */
    public void addDisposeListener(DisposeListener dl) {
        getShell().addDisposeListener(dl);
    }

    /**
     * This method initializes compHeader
     */
    @Override
    protected void createCompHeader() {
        String headerTxt = "Prescrição do Paciente";
        iDartImage icoImage = iDartImage.PRESCRIPTIONNEW;
        buildCompHeader(headerTxt, icoImage);
    }

    @Override
    protected void createCompButtons() {
        // Parent Class generic call
        buildCompButtons();
        btnSave.setText("Salar esta Prescrição");
        // btnSavePrescription

    }

    /**
     * This method initializes compInstructions
     */
    private void createCompInstructions() {

        compInstructions = new Composite(getShell(), SWT.NONE);
        compInstructions.setBounds(new Rectangle(327, 58, 400, 20));

        Label lblInstructions = new Label(compInstructions, SWT.CENTER);
        lblInstructions.setBounds(new Rectangle(0, 2, 400, 18));
        lblInstructions.setText(Messages.getString("adddruggroup.label.instructions.title"));
        lblInstructions.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_10_ITALIC));
    }

    /**
     * This method creates the group containing the patient's ID, search button
     * & reason for prescription update
     */
    private void createGrpPatientID() {

        // grpPatientID
        grpPatientID = new Group(getShell(), SWT.NONE);
        grpPatientID.setBounds(new Rectangle(40, 71, 480, 148));

        grpPatientMDS = new Group(getShell(), SWT.NONE);
        grpPatientMDS.setBounds(new Rectangle(530, 68, 220, 140));

        // Patient ID
        Label lblPatientId = new Label(grpPatientID, SWT.NONE);
        lblPatientId.setBounds(new Rectangle(10, 15, 110, 20));
        lblPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblPatientId.setText(Messages.getString("patient.label.patientid")); //$NON-NLS-1$

        // Patient MDS
        Label lblPatientMDS = new Label(grpPatientMDS, SWT.NONE);
        lblPatientMDS.setBounds(new Rectangle(10, 10, 200, 20));
        lblPatientMDS.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        //lblPatientMDS.setBackground(ResourceUtils.getColor(iDartColor.GREEN));
        lblPatientMDS.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_BOLD));
        lblPatientMDS.setText("Modelos Diferenciados de Serviços"); //$NON-NLS-1$

        txtPatientId = new Text(grpPatientID, SWT.BORDER);
        txtPatientId.setBounds(new Rectangle(160, 8, 150, 20));
        txtPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtPatientId.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if ((e.character == SWT.CR)
                        || (e.character == (char) iDartProperties.intValueOfAlternativeBarcodeEndChar)) {
                    cmdSearchWidgetSelected();
                }
            }
        });

        btnSearch = new Button(grpPatientID, SWT.NONE);
        btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnSearch.setBounds(new Rectangle(320, 9, 108, 30));
        btnSearch.setText("Procurar Paciente");
        btnSearch
                .setToolTipText("Pressione este botão para procurar um paciente existente");
        btnSearch
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdSearchWidgetSelected();
                    }
                });

        btnEkapaSearch = new Button(grpPatientID, SWT.NONE);
        btnEkapaSearch.setBounds(new Rectangle(310, 36, 105, 30));
        btnEkapaSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnEkapaSearch.setText("Procurar eKapa");
        if (!iDartProperties.isEkapaVersion) {
            btnEkapaSearch.setVisible(false);
        }
        if (thePatient != null) {
            btnEkapaSearch.setEnabled(false);
        }

        btnEkapaSearch
                .setToolTipText("Pressione este botão para importar um paciente de eKapa.");
        btnEkapaSearch
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {

                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdEkapaSearchWidgetSelected();
                    }
                });

        if (thePatient != null) {
            btnSearch.setEnabled(false);
            btnEkapaSearch.setEnabled(false);
        }

        lblUpdateReason = new Label(grpPatientID, SWT.NONE);
        lblUpdateReason.setBounds(new Rectangle(10, 40, 140, 20));
        lblUpdateReason.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblUpdateReason.setText("* Tipo Tarv:");

        cmbUpdateReason = new CCombo(grpPatientID, SWT.BORDER | SWT.READ_ONLY);
        cmbUpdateReason.setBounds(new Rectangle(160, 32, 150, 20));
        cmbUpdateReason.setEditable(false);
        cmbUpdateReason.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbUpdateReason.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbUpdateReason.setForeground(ResourceUtils.getColor(iDartColor.BLACK));

        cmbUpdateReason.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (cmbUpdateReason.getText().equals("Transfer de")) {
                    btnDataInicioNoutroServico.setEnabled(true);
                    cmbMotivoMudanca.removeAll();
                    cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
                    cmbMotivoMudanca.setEnabled(false);
                } else if (cmbUpdateReason.getText().equals("Alterar")) {
                    cmbMotivoMudanca.setEnabled(true);
                    cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

                } else {
                    btnDataInicioNoutroServico.setDate(null);
                    btnDataInicioNoutroServico.setText("Seleccione a data");
                    btnDataInicioNoutroServico.setEnabled(false);
                    cmbMotivoMudanca.removeAll();
                    cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
                    cmbMotivoMudanca.setEnabled(false);
                    ;
                }

            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {

                // TODO Auto-generated method stub
                if (cmbUpdateReason.getText().equals("Transfer de")) {

                    btnDataInicioNoutroServico.setEnabled(true);
                    cmbMotivoMudanca.removeAll();
                    cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
                    cmbMotivoMudanca.setEnabled(false);
                } else if (cmbUpdateReason.getText().equals("Alterar")) {
                    cmbMotivoMudanca.setEnabled(true);
                    cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
                    cmbMotivoMudanca.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
                } else {
                    btnDataInicioNoutroServico.setDate(null);
                    btnDataInicioNoutroServico.setText("Seleccione a data");
                    btnDataInicioNoutroServico.setEnabled(false);
                    cmbMotivoMudanca.removeAll();
                    cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
                    cmbMotivoMudanca.setEnabled(false);
                }
            }
        });

        lblDataInicioNoutroServico = new Label(grpPatientID, SWT.NONE);
        lblDataInicioNoutroServico.setBounds(new Rectangle(10, 62, 150, 20));
        lblDataInicioNoutroServico.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDataInicioNoutroServico.setText(Messages.getString("patient.prescription.dialog.startdate.anotherservice"));

        btnDataInicioNoutroServico = new DateButton(grpPatientID, DateButton.NONE, null);
        btnDataInicioNoutroServico.setBounds(160, 55, 150, 20);
        btnDataInicioNoutroServico.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDataInicioNoutroServico.setText("Seleccione a data");
        btnDataInicioNoutroServico.setEnabled(false);

        lblMotivoMudanca = new Label(grpPatientID, SWT.NONE);
        lblMotivoMudanca.setBounds(new Rectangle(10, 82, 150, 20));
        lblMotivoMudanca.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblMotivoMudanca.setText(Messages.getString("patient.prescription.dialog.startdate.reason.change"));

        lblDispensaTrimestral = new Label(grpPatientID, SWT.NONE);
        lblDispensaTrimestral.setBounds(new Rectangle(10, 108, 150, 20));
        lblDispensaTrimestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDispensaTrimestral.setText("Dispensa Trimestral:");

        cmbDispensaTrimestral = new CCombo(grpPatientID, SWT.BORDER | SWT.READ_ONLY);
        cmbDispensaTrimestral.setBounds(new Rectangle(160, 101, 150, 20));
        cmbDispensaTrimestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbDispensaTrimestral.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        //POPULA combo dispensa trimestral
        cmbDispensaTrimestral.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        CommonObjects.populateDispensaTrimestral(getHSession(), cmbDispensaTrimestral);

        lblDispensaSemestral = new Label(grpPatientID, SWT.NONE);
        lblDispensaSemestral.setBounds(new Rectangle(10, 128, 150, 20));
        lblDispensaSemestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDispensaSemestral.setText("Dispensa Semestral:");

        cmbDispensaSemestral = new CCombo(grpPatientID, SWT.BORDER | SWT.READ_ONLY);
        cmbDispensaSemestral.setBounds(new Rectangle(160, 124, 150, 20));
        cmbDispensaSemestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbDispensaSemestral.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbDispensaTrimestral.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        //POPULA combo dispensa semestral
        CommonObjects.populateDispensaSemestral(getHSession(), cmbDispensaSemestral);

        lblTipoDispensaTrimestral = new Label(grpPatientID, SWT.NONE);
        lblTipoDispensaTrimestral.setBounds(new Rectangle(320, 102, 50, 20));
        lblTipoDispensaTrimestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblTipoDispensaTrimestral.setText("* Tipo DT:");

        cmbTipoDispensaTrimestral = new CCombo(grpPatientID, SWT.BORDER | SWT.READ_ONLY);
        cmbTipoDispensaTrimestral.setBounds(new Rectangle(370, 100, 100, 20));
        cmbTipoDispensaTrimestral.setEditable(true);
        cmbTipoDispensaTrimestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbTipoDispensaTrimestral.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbTipoDispensaTrimestral.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        cmbTipoDispensaTrimestral.setEnabled(true);

        //POPULA combo tipo dispensa trimestral
        CommonObjects.populateTipoDispensaTrimestral(getHSession(), cmbTipoDispensaTrimestral);

        lblTipoDispensaSemestral = new Label(grpPatientID, SWT.NONE);
        lblTipoDispensaSemestral.setBounds(new Rectangle(320, 123, 50, 20));
        lblTipoDispensaSemestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblTipoDispensaSemestral.setText("* Tipo DS:");

        cmbTipoDispensaSemestral = new CCombo(grpPatientID, SWT.BORDER | SWT.READ_ONLY);
        cmbTipoDispensaSemestral.setBounds(new Rectangle(370, 123, 100, 20));
        cmbTipoDispensaSemestral.setEditable(true);
        cmbTipoDispensaSemestral.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbTipoDispensaSemestral.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbTipoDispensaSemestral.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        cmbTipoDispensaSemestral.setEnabled(true);

        //POPULA combo dispensa semestral
        cmbTipoDispensaSemestral.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        CommonObjects.populateTipoDispensaSemestral(getHSession(), cmbTipoDispensaSemestral);

        cmbMotivoMudanca = new CCombo(grpPatientID, SWT.BORDER | SWT.READ_ONLY);
        cmbMotivoMudanca.setBounds(new Rectangle(160, 78, 150, 20));
        cmbMotivoMudanca.setEditable(false);
        cmbMotivoMudanca.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbMotivoMudanca.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        cmbMotivoMudanca.setEnabled(false);

        cmbDispensaTrimestral.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (cmbDispensaTrimestral.getText().equals("Sim")) {
                    lblTipoDispensaTrimestral.setVisible(true);
                    cmbTipoDispensaTrimestral.setVisible(true);
                    cmbDispensaSemestral.setEnabled(false);
                    lblTipoDispensaSemestral.setVisible(false);
                    cmbTipoDispensaSemestral.setVisible(false);
                } else {
                    lblTipoDispensaTrimestral.setVisible(false);
                    cmbTipoDispensaTrimestral.setVisible(false);
                    cmbDispensaSemestral.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        cmbDispensaSemestral.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (cmbDispensaSemestral.getText().equals("Sim")) {
                    lblTipoDispensaSemestral.setVisible(true);
                    cmbTipoDispensaSemestral.setVisible(true);
                    cmbDispensaTrimestral.setEnabled(false);
                    lblTipoDispensaTrimestral.setVisible(false);
                    cmbTipoDispensaTrimestral.setVisible(false);
                    cmbTipoDispensaTrimestral.setText("");
                } else {
                    lblTipoDispensaSemestral.setVisible(false);
                    cmbTipoDispensaSemestral.setVisible(false);
                    cmbDispensaTrimestral.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        //POPULA OS Motivos de Mudanca
        cmbMotivoMudanca.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        CommonObjects.populateMotivoMudanca(getHSession(), cmbMotivoMudanca, false);
        cmbMotivoMudanca.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cmbMotivoMudanca.removeAll();
                CommonObjects.populateMotivoMudanca(getHSession(), cmbMotivoMudanca, false);
                cmbMotivoMudanca.setVisibleItemCount(Math.min(
                        cmbMotivoMudanca.getItemCount(), 25));
            }
        });
        //cmbMotivoMudanca.setFocus();

        CommonObjects.populatePrescriptionUpdateReasons(getHSession(), cmbUpdateReason);
        cmbUpdateReason.setVisibleItemCount(cmbUpdateReason.getItemCount());

        chkBtnPrEP = new Button(grpPatientID, SWT.CHECK);
        chkBtnPrEP.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1));
        chkBtnPrEP.setBounds(new Rectangle(320, 40, 75, 20));
        chkBtnPrEP.setText("PrEP");
        chkBtnPrEP.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnPrEP.setSelection(false);

        chkBtnPPE = new Button(grpPatientID, SWT.CHECK);
        chkBtnPPE.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnPPE.setBounds(new Rectangle(320, 60, 50, 20));
        chkBtnPPE.setText("PPE");
        chkBtnPPE.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnPPE.setSelection(false);

        chkBtnCE = new Button(grpPatientID, SWT.CHECK);
        chkBtnCE.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnCE.setBounds(new Rectangle(320, 80, 90, 20));
        chkBtnCE.setText("Criança Exposta");
        chkBtnCE.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnCE.setSelection(false);

        //MDS
        chkBtnCPN = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnCPN.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1));
        chkBtnCPN.setBounds(new Rectangle(10, 30, 40, 20));
        chkBtnCPN.setText("CPN");
        chkBtnCPN.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnCPN.setSelection(false);

        chkBtnTB = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnTB.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnTB.setBounds(new Rectangle(10, 50, 50, 20));
        chkBtnTB.setText("TB");
        chkBtnTB.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnTB.setSelection(false);

        chkBtnCCR = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnCCR.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnCCR.setBounds(new Rectangle(10, 70, 50, 20));
        chkBtnCCR.setText("CCR");
        chkBtnCCR.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnCCR.setSelection(false);

        chkBtnSAAJ = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnSAAJ.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnSAAJ.setBounds(new Rectangle(10, 90, 50, 20));
        chkBtnSAAJ.setText("SAAJ");
        chkBtnSAAJ.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnSAAJ.setSelection(false);

        chkBtnGAAC = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnGAAC.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnGAAC.setBounds(new Rectangle(70, 30, 120, 20));
        chkBtnGAAC.setText("GAAC");
        chkBtnGAAC.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnGAAC.setSelection(false);

        chkBtnAF = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnAF.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnAF.setBounds(new Rectangle(70, 50, 120, 20));
        chkBtnAF.setText("Abordagem Familiar");
        chkBtnAF.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnAF.setSelection(false);

        chkBtnCA = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnCA.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnCA.setBounds(new Rectangle(70, 70, 120, 20));
        chkBtnCA.setText("Clube de Adesao");
        chkBtnCA.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnCA.setSelection(false);

        chkBtnFR = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnFR.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnFR.setBounds(new Rectangle(70, 90, 120, 20));
        chkBtnFR.setText("Fluxo Rapido");
        chkBtnFR.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnFR.setSelection(false);

        chkBtnDC = new Button(grpPatientMDS, SWT.CHECK);
        chkBtnDC.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        chkBtnDC.setBounds(new Rectangle(70, 110, 120, 20));
        chkBtnDC.setText("Dispensa Comunitaria");
        chkBtnDC.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnDC.setSelection(false);

        avaliaMDSSelection();
    }

    @SuppressWarnings("unchecked")
    private void setCaptureDateRestrictions() throws DateException {
        IRule<Date> rule = DateRuleFactory.beforeNowInclusive(false);
        if (thePatient != null) {
            Episode latestEpi = PatientManager.getMostRecentEpisode(thePatient);
            Date dteEpisodeStart = latestEpi.getStartDate();
            IRule<Date> afterEpisodeStart = DateRuleFactory.afterInclusive(
                    dteEpisodeStart, false);
            afterEpisodeStart.setDescription("Este paciente foi marcado como "
                    + latestEpi.getStartReason()
                    + " em <date>. A data da Prescrição deve estar em,"
                    + " ou depois da data deste episódio.");
            rule = new AndRule<Date>(rule, afterEpisodeStart);
        }

        btnCaptureDate.setValidator(new DateInputValidator(rule));
        btnCaptureDate.setDate(new Date());
    }

    /**
     * This method initializes grpParticulars
     */
    private void createParticularsGroup() {

        // grpParticulars
        grpParticulars = new Group(getShell(), SWT.NONE);
        grpParticulars.setBounds(new Rectangle(40, 218, 810, 150));
        grpParticulars.setText(Messages.getString("patient.prescription.dialog.info"));
        grpParticulars.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // Name
        Label lblName = new Label(grpParticulars, SWT.NONE);
        lblName.setBounds(new Rectangle(10, 20, 110, 20));
        lblName.setText("Prim. Nomes:");
        lblName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtName = new Text(grpParticulars, SWT.BORDER);
        txtName.setBounds(new Rectangle(120, 20, 130, 20));
        txtName.setEnabled(false);
        txtName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // Surname
        Label lblSurname = new Label(grpParticulars, SWT.NONE);
        lblSurname.setBounds(new Rectangle(10, 45, 110, 20));
        lblSurname.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblSurname.setText("Apelido:");

        txtSurname = new Text(grpParticulars, SWT.BORDER);
        txtSurname.setBounds(new Rectangle(120, 45, 130, 20));
        txtSurname.setEnabled(false);
        txtSurname.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblPicPatientHistoryReport
        btnPatientHistoryReport = new Button(grpParticulars, SWT.NONE);
        btnPatientHistoryReport.setBounds(new Rectangle(270, 48, 40, 40));
        btnPatientHistoryReport
                .setToolTipText("Pressione este botão para visualizar e / ou imprimir relatórios \n do histórico das prescrições do paciente.");
        btnPatientHistoryReport.setImage(ResourceUtils
                .getImage(iDartImage.REPORT_PATIENTHISTORY_30X26));

        btnPatientHistoryReport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseUp(MouseEvent mu) {
                cmdPatientHistoryWidgetSelected();
            }
        });
        btnPatientHistoryReport.setEnabled(false);

        // Age
        Label lblAge = new Label(grpParticulars, SWT.NONE);
        lblAge.setBounds(new Rectangle(10, 70, 110, 20));
        lblAge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblAge.setText("Idade && Data Nasc:");
        txtAge = new Text(grpParticulars, SWT.BORDER);
        txtAge.setBounds(new Rectangle(120, 70, 40, 20));
        txtAge.setEnabled(false);
        txtAge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtDOB = new Text(grpParticulars, SWT.BORDER);
        txtDOB.setBounds(new Rectangle(170, 70, 80, 20));
        txtDOB.setEnabled(false);
        txtDOB.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // Picture of a child
        lblPicChild = new Label(grpParticulars, SWT.BORDER);
        lblPicChild.setBounds(new Rectangle(275, 90, 30, 26));
        lblPicChild.setImage(ResourceUtils.getImage(iDartImage.CHILD_30X26));
        lblPicChild.setVisible(false);

        // Down Referral Clinic
        Label lblClinic = new Label(grpParticulars, SWT.NONE);
        lblClinic.setBounds(new Rectangle(10, 95, 110, 20));
        lblClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblClinic.setText("US:");

        txtClinic = new Text(grpParticulars, SWT.BORDER);
        txtClinic.setBounds(new Rectangle(120, 95, 130, 20));
        txtClinic.setText("");
        txtClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtClinic.setEditable(false);
        txtClinic.setEnabled(false);

        // Capture Date
        Label lblCaptureDate = new Label(grpParticulars, SWT.NONE);
        lblCaptureDate.setBounds(new Rectangle(10, 120, 110, 20));
        lblCaptureDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblCaptureDate.setText("Data Registo:");

        btnCaptureDate = new DateButton(grpParticulars, DateButton.NONE, null);
        btnCaptureDate.setBounds(120, 120, 130, 25);
        btnCaptureDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnCaptureDate.setEnabled(false);
        btnCaptureDate.addDateChangedListener(new DateChangedListener() {
            @Override
            public void dateChanged(DateChangedEvent event) {
                cmdUpdatePrescriptionId();
                cmdUpdateClinic();
            }
        });

        // Doctor
        Label lblDoctor = new Label(grpParticulars, SWT.NONE);
        lblDoctor.setBounds(new Rectangle(350, 20, 90, 20));
        lblDoctor.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDoctor.setText(Messages.getString("patient.prescription.dialog.pharmacist"));

        cmbDoctor = new CCombo(grpParticulars, SWT.BORDER | SWT.READ_ONLY
                | SWT.V_SCROLL);
        cmbDoctor.setBounds(new Rectangle(450, 20, 130, 20));
        cmbDoctor.setEditable(false);
        cmbDoctor.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbDoctor.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        cmbDoctor.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        CommonObjects.populateDoctors(getHSession(), cmbDoctor, false);
        cmbDoctor.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cmbDoctor.removeAll();
                CommonObjects.populateDoctors(getHSession(), cmbDoctor, false);
                cmbDoctor.setVisibleItemCount(Math.min(
                        cmbDoctor.getItemCount(), 25));
            }
        });
        cmbDoctor.setFocus();

        btnAddDoctor = new Button(grpParticulars, SWT.NONE);
        btnAddDoctor.setBounds(new Rectangle(560, 45, 40, 40));
        btnAddDoctor.setImage(ResourceUtils.getImage(iDartImage.DOCTOR_30X26));
        btnAddDoctor
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdAddDoctorWidgetSelected();
                    }
                });
        btnAddDoctor.setToolTipText("Pressionar este botão para adicionar um novo clínico");

        // Duration
        Label lblDuration = new Label(grpParticulars, SWT.NONE);
        lblDuration.setBounds(new Rectangle(350, 45, 90, 20));
        lblDuration.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblDuration.setText(Messages.getString("patient.prescription.dialog.duration"));

        cmbDuration = new CCombo(grpParticulars, SWT.BORDER | SWT.READ_ONLY);
        cmbDuration.setBounds(new Rectangle(450, 45, 100, 20));
        cmbDuration.setEditable(false);
        cmbDuration.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbDuration.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbDuration.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        CommonObjects.populatePrescriptionDuration(getHSession(), cmbDuration);
        cmbDuration.setVisibleItemCount(cmbDuration.getItemCount());

        // Weight
        Label lblWeight = new Label(grpParticulars, SWT.NONE);
        lblWeight.setBounds(new Rectangle(350, 72, 90, 20));
        lblWeight.setText("   Peso:");
        lblWeight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtWeight = new Text(grpParticulars, SWT.BORDER);
        txtWeight.setBounds(new Rectangle(450, 70, 46, 20));
        txtWeight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        Label lblKg = new Label(grpParticulars, SWT.NONE);
        lblKg.setBounds(new Rectangle(500, 72, 30, 20));
        lblKg.setText("kg");
        lblKg.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // Regime Terapeutico
        Label lblRegime = new Label(grpParticulars, SWT.NONE);
        lblRegime.setBounds(new Rectangle(350, 95, 90, 20));
        lblRegime.setText("*  Regime:");
        lblRegime.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbRegime = new Combo(grpParticulars, SWT.BORDER);
        cmbRegime.setBounds(new Rectangle(450, 95, 130, 20));
        cmbRegime.setVisibleItemCount(10);
        cmbRegime.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbRegime.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbRegime.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
//			   POPULA OS REGIMES
        CommonObjects.populateRegimesTerapeuticosCombo(getHSession(), cmbRegime, false);
        cmbRegime.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                cmbRegime.removeAll();
                CommonObjects.populateRegimesTerapeuticosCombo(getHSession(), cmbRegime, false);
                cmbRegime.setVisibleItemCount(Math.min(cmbRegime.getItemCount(), 25));
            }
        });

        enableContentProposal(cmbRegime);

        // Linha Terapeutica
        Label lblLinha = new Label(grpParticulars, SWT.NONE);
        lblLinha.setBounds(new Rectangle(350, 120, 90, 20));
        lblLinha.setText("* Linha:");
        lblLinha.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbLinha = new CCombo(grpParticulars, SWT.BORDER | SWT.READ_ONLY);
        cmbLinha.setBounds(new Rectangle(450, 120, 130, 20));
        cmbLinha.setVisibleItemCount(10);
        cmbLinha.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbLinha.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbLinha.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        //popula o ccombo de linhas
        CommonObjects.populateLinha(getHSession(), cmbLinha, false);
        cmbLinha.setFocus();

        // Prescription Notes
        chkBtnPrescicaoEspecial = new Button(grpParticulars, SWT.CHECK);
        chkBtnPrescicaoEspecial.setBounds(new Rectangle(620, 10, 170, 20));
        chkBtnPrescicaoEspecial.setText("Prescrição Especial");
        chkBtnPrescicaoEspecial.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        chkBtnPrescicaoEspecial.setSelection(false);
        chkBtnPrescicaoEspecial.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                if (chkBtnPrescicaoEspecial.getSelection()) {
                    cmbMotivoCriacao.setEnabled(true);
                    cmbMotivoCriacao.setText("");
                    cmbMotivoCriacao.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
                } else {
                    cmbMotivoCriacao.setEnabled(false);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent selectionEvent) {
                if (chkBtnPrescicaoEspecial.getSelection()) {
                    cmbMotivoCriacao.setEnabled(true);
                    cmbMotivoCriacao.setText("");
                    cmbMotivoCriacao.setBackground(ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND));
                } else {
                    cmbMotivoCriacao.setEnabled(false);
                }
            }
        });

        // Motivo Prescricao
        Label lblMotivoPrescriacao = new Label(grpParticulars, SWT.NONE);
        lblMotivoPrescriacao.setBounds(new Rectangle(620, 35, 90, 20));
        lblMotivoPrescriacao.setText("Motivo de Criação:");
        lblMotivoPrescriacao.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbMotivoCriacao = new CCombo(grpParticulars, SWT.BORDER | SWT.READ_ONLY);
        cmbMotivoCriacao.setBounds(new Rectangle(620, 50, 130, 20));
        cmbMotivoCriacao.setVisibleItemCount(10);
        cmbMotivoCriacao.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbMotivoCriacao.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbMotivoCriacao.setForeground(ResourceUtils.getColor(iDartColor.BLACK));

        //popula o ccombo motivos de criacao da prescricao
        CommonObjects.populateMotivosPrescricao(getHSession(), cmbMotivoCriacao);
        cmbMotivoCriacao.setEnabled(false);

        Label lblNotes = new Label(grpParticulars, SWT.BORDER);
        lblNotes.setBounds(new Rectangle(620, 75, 170, 20));
        lblNotes.setText(Messages.getString("patient.prescription.dialog.prescription.notes"));
        lblNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtAreaNotes = new Text(grpParticulars, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        txtAreaNotes.setBounds(new Rectangle(620, 90, 170, 30));
        txtAreaNotes.setText("");
        txtAreaNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // New Prescription ID
        lblNewPrescriptionId = new Label(grpParticulars, SWT.CENTER | SWT.BORDER);
        lblNewPrescriptionId.setBounds(new Rectangle(620, 120, 170, 20));
        lblNewPrescriptionId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

    }

    /**
     * This method initializes compButtonsMiddle
     */
    private void createCompButtonsMiddle() {

        // compButtonsMiddle
        compButtonsMiddle = new Composite(getShell(), SWT.NONE);
        compButtonsMiddle.setBounds(new Rectangle(200, 335, 600, 65));

        // Add Drug button and icon
        lblPicAddDrug = new Label(compButtonsMiddle, SWT.NONE);
        lblPicAddDrug.setBounds(new Rectangle(5, 38, 30, 26));
        lblPicAddDrug.setImage(ResourceUtils
                .getImage(iDartImage.PRESCRIPTIONADDDRUG_30X26));


        // Associated drugs with regimeTerapeutico
        btnAddAssociatedDrug = new Button(compButtonsMiddle, SWT.NONE);
        btnAddAssociatedDrug.setBounds(new Rectangle(50, 38, 185, 28));
        btnAddAssociatedDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnAddAssociatedDrug.setText(Messages.getString("patient.prescription.dialog.add.associated"));
        btnAddAssociatedDrug.setEnabled(false);
        btnAddAssociatedDrug
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdAddAssociatedDrugWidgetSelected();
                    }
                });
        btnAddAssociatedDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));


        btnRemoveDrug = new Button(compButtonsMiddle, SWT.NONE);
        btnRemoveDrug.setBounds(new Rectangle(247, 38, 181, 28));
        btnRemoveDrug.setText(Messages.getString("patient.prescription.dialog.remove"));
        lblPicAddDrug.setEnabled(false);
        btnRemoveDrug.setEnabled(false);
        btnRemoveDrug
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdRemoveDrugWidgetSelected();
                    }
                });
        btnRemoveDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        btnAddDrug = new Button(compButtonsMiddle, SWT.NONE);
        btnAddDrug.setBounds(new Rectangle(440, 38, 110, 28));
        btnAddDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnAddDrug.setText(Messages.getString("patient.prescription.dialog.add"));
        btnAddDrug.setEnabled(false);
        btnAddDrug
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdAddDrugWidgetSelected();
                    }
                });
        btnAddDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));


        // View Prescription History button and icon
    }

    /**
     * This method initializes grpDrugs
     */
    private void createGrpDrugs() {

        grpDrugs = new Group(getShell(), SWT.NONE);

        grpDrugs.setText("Medicamentos em Prescri;ao:");
        grpDrugs.setBounds(new Rectangle(100, 418, 700, 160));
        grpDrugs.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        btnMoveUp = new Button(grpDrugs, SWT.NONE);
        btnMoveUp.setBounds(5, 65, 40, 40);
        btnMoveUp.setImage(ResourceUtils.getImage(iDartImage.UPARROW_30X26));
        btnMoveUp
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {

                        if (tblDrugs.getSelectionIndex() != -1) {
                            cmdMoveDrug(-1);
                        } else {
                            MessageBox mb = new MessageBox(getShell(),
                                    SWT.ICON_QUESTION | SWT.OK);
                            mb.setText("No Drug Selected ");
                            mb.setMessage("Please select a drug to move.");
                            mb.open();
                        }
                    }
                });

        btnMoveDown = new Button(grpDrugs, SWT.NONE);
        btnMoveDown.setBounds(655, 60, 40, 40);
        btnMoveDown
                .setImage(ResourceUtils.getImage(iDartImage.DOWNARROW_30X26));
        btnMoveDown
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        if (tblDrugs.getSelectionIndex() != -1) {
                            cmdMoveDrug(1);
                        } else {
                            MessageBox mb = new MessageBox(getShell(),
                                    SWT.ICON_QUESTION | SWT.OK);
                            mb.setText("No Drug Selected ");
                            mb.setMessage("Please select a drug to move.");
                            mb.open();
                        }
                    }
                });

        createDrugsTable();

        /*tpi_tpc=new Label(grpDrugs, SWT.NONE);
         tpi_tpc.setBounds(new Rectangle(200, 135, 150, 20));
         tpi_tpc.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
         tpi_tpc.setText("O Paciente também levantou:");



         chkBtnTPC = new Button(grpDrugs, SWT.CHECK);
         chkBtnTPC.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1,1));
         chkBtnTPC.setBounds(new Rectangle(350, 135, 100, 20));
         chkBtnTPC.setText("Cotrimoxazol");
         chkBtnTPC.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
         chkBtnTPC.setSelection(false);


         chkBtnTPI = new Button(grpDrugs, SWT.CHECK);
         chkBtnTPI.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1,1));
         chkBtnTPI.setBounds(new Rectangle(450, 135, 100, 20));
         chkBtnTPI.setText("Isoniazida");
         chkBtnTPI.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
         chkBtnTPI.setSelection(false);*/
    }

    /**
     * This method initializes tblDrugs
     */
    private void createDrugsTable() {

        tblDrugs = new Table(grpDrugs, SWT.FULL_SELECTION);
        tblDrugs.setLinesVisible(true);
        tblDrugs.setBounds(new Rectangle(50, 25, 600, 126));
        tblDrugs.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        tblDrugs.setHeaderVisible(true);

        // 0 - clmSpace
        clmSpace = new TableColumn(tblDrugs, SWT.NONE);
        clmSpace.setWidth(28);
        clmSpace.setText("No");

        // clmDrugBarcode
        // clmDrugBarcode = new TableColumn(tblDrugs, SWT.NONE);
        // clmDrugBarcode.setText("Drug Barcode");
        // clmDrugBarcode.setWidth(120);
        // clmDrugBarcode.setResizable(false);
        // 1 - clmDrugName
        clmDrugName = new TableColumn(tblDrugs, SWT.NONE);
        clmDrugName.setText("Nome do Medicamento");
        clmDrugName.setWidth(235);
        clmDrugName.setResizable(false);

        // 2 - clmTake
        clmTake = new TableColumn(tblDrugs, SWT.NONE);
        clmTake.setWidth(60);
        clmTake.setText("Instrução");
        clmTake.setResizable(true);

        // 3 - clmAmt
        clmAmt = new TableColumn(tblDrugs, SWT.NONE);
        clmAmt.setText("Quant.");
        clmAmt.setWidth(46);
        clmAmt.setResizable(false);

        // 4 - tblDescription
        tblDescription = new TableColumn(tblDrugs, SWT.NONE);
        tblDescription.setText("Forma");
        tblDescription.setWidth(60);
        tblDescription.setResizable(false);

        // 5 - tblPerDay
        tblPerDay = new TableColumn(tblDrugs, SWT.NONE);
        tblPerDay.setText("Vez(es)");
        tblPerDay.setWidth(40);
        tblPerDay.setResizable(false);

        // 6 - tblTPD
        tblTPD = new TableColumn(tblDrugs, SWT.NONE);
        tblTPD.setText("Por Dia");
        tblTPD.setWidth(85);
        tblTPD.setResizable(false);

        // 7 - tblQnty
        tblQnty = new TableColumn(tblDrugs, SWT.NONE);
        tblQnty.setText("Frasco");
        tblQnty.setWidth(44);
        tblQnty.setResizable(true);

        editor = new TableEditor(tblDrugs);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;

        tblDrugs.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent event) {
                // Dispose any existing editor
                Control old = editor.getEditor();
                if (old != null) {
                    old.dispose();
                }

                // Determine where the mouse was clicked
                Point pt = new Point(event.x, event.y);

                // Determine which row was selected
                final TableItem item = tblDrugs.getItem(pt);
                if (item != null) {
                    // Determine which column was selected
                    int column = -1;
                    for (int i = 0, n = tblDrugs.getColumnCount(); i < n; i++) {
                        Rectangle rect = item.getBounds(i);
                        if (rect.contains(pt)) {
                            // This is the selected column
                            column = i;
                            break;
                        }

                    }

                    if (column == 3 || column == 5) {
                        // Create the Text Object for your editor
                        final Text text = new Text(tblDrugs, SWT.None);
                        text.setForeground(item.getForeground());

                        // Transfer any text from the cell to the text control
                        // and store a copy of the original text
                        text.setText(item.getText(column));
                        text.setForeground(item.getForeground());
                        text.setFont(ResourceUtils
                                .getFont(iDartFont.VERASANS_8));
                        text.selectAll();
                        text.setFocus();

                        // Recalculate the minimum width for the editor
                        editor.minimumWidth = text.getBounds().width;

                        // Set the control into the editor
                        editor.setEditor(text, item, column);

                        // Add a handler to transfer the text back to the cell
                        // any time its modified
                        final int col = column;
                        text.addModifyListener(new ModifyListener() {

                            @Override
                            public void modifyText(ModifyEvent event1) {
                                // Set the text of the editor back into the cell
                                String newVal = text.getText();

                                // case for backspace and other non text
                                // characters
                                if (0 == newVal.length()) {
                                    return;
                                }
                                if (iDARTUtil.isAlpha(newVal)
                                        || newVal.length() > 5) {
                                    return;
                                }

                                item.setText(col, text.getText());

                                if (col == 3) {
                                    if (!FloatValidator.isPositiveFloat(newVal)) {
                                        return;
                                    } else {
                                        // Set the text of the editor back into
                                        // the cell
                                        item.setText(col, newVal);
                                        PrescribedDrugs pd = (PrescribedDrugs) item
                                                .getData();
                                        pd.setAmtPerTime(Double
                                                .parseDouble(newVal));
                                        item.setData(pd);
                                    }
                                } else if (col == 5) {
                                    if (!iDARTUtil.isPositiveInteger(newVal)) {
                                        return;
                                    } else {
                                        // Set the text of the editor back into
                                        // the cell
                                        item.setText(col, newVal);
                                        PrescribedDrugs pd = (PrescribedDrugs) item
                                                .getData();
                                        pd.setTimesPerDay(Integer
                                                .parseInt(newVal));
                                        item.setData(pd);
                                    }
                                }

                            }

                        });
                    }
                }
            }
        });

    }

    /**
     * This method initializes compButtons
     *
     * @return boolean
     */
    /**
     * Checks the GUI for valid fields entries
     *
     * @return true if the requiResourceUtils.getColor(iDartColor.RED) fields
     * are filled in
     */
    @Override
    protected boolean fieldsOk() {

        boolean checkOpenmrs = true;
    	RestClient restClient = new RestClient();
        ConexaoJDBC conexao = new ConexaoJDBC();
        Prescription oldPrescription = localPrescription.getPatient().getCurrentPrescription();
        Patient patient = localPrescription.getPatient();
        String regimenomeespecificado = AdministrationManager.getRegimeTerapeutico(getHSession(), cmbRegime.getText()).getRegimenomeespecificado();

        if (CentralizationProperties.centralization.equalsIgnoreCase("off"))
            checkOpenmrs = true;
        else if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("F")
                || CentralizationProperties.pharmacy_type.equalsIgnoreCase("P"))
            checkOpenmrs = false;

        if (oldPrescription != null)
            if (Integer.parseInt(String.valueOf(cmbLinha.getText().charAt(0))) < Integer.parseInt(String.valueOf(oldPrescription.getLinha().getLinhanome().charAt(0)))) {

                MessageBox errorBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                errorBox.setText("Não foi possível salvar a prescrição");
                errorBox.setMessage("Não é possível alterar a linha do paciente de " + oldPrescription.getLinha().getLinhanome()
                        + " para " + cmbLinha.getText());
                errorBox.open();
                return false;
            }

        if(checkOpenmrs) {

            try {
                if (!getServerStatus(JdbcProperties.urlBase).contains("Red")) {

                    User currentUser = LocalObjects.getUser(HibernateUtil.getNewSession());

                    assert currentUser != null;
                    if (ApiAuthRest.loginOpenMRS(currentUser)) {

                    String strProvider = cmbDoctor.getText().split(",")[1].trim() + " " + cmbDoctor.getText().split(",")[0].trim();

                    String providerWithNoAccents = org.apache.commons.lang3.StringUtils.stripAccents(strProvider);

                    String response = restClient.getOpenMRSResource(iDartProperties.REST_GET_PROVIDER + StringUtils.replace(providerWithNoAccents, " ", "%20"));

                    Clinic clinic = AdministrationManager.getMainClinic(getHSession());

                    String facility = clinic.getClinicName().trim();

                    Episode episode = PatientManager.getLastEpisode(getHSession(), patient.getPatientId());

                    if (StringUtils.isEmpty(patient.getUuidopenmrs()) && !episode.getStartReason().contains("nsito")
                            && !episode.getStartReason().contains("aternidade") && !episode.getStartReason().contains("PrEP")
                            && !episode.getStartReason().contains("CRAM")) {
                        MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                        m.setText("Informação sobre estado do programa");
                        m.setMessage("O uuid do paciente " + patient.getPatientId().trim() + " está vazio na base de dados do iDART. Preencha o uuid deste paciente apartir da base de dados do OpenMRS.");
                        m.open();
                        return false;
                    }
                    // Location
                    String strFacility = restClient.getOpenMRSResource(iDartProperties.REST_GET_LOCATION + StringUtils.replace(facility, " ", "%20"));

                    if (StringUtils.isEmpty(restClient.getOpenMRSResource("concept/" + regimenomeespecificado))) {
                        MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                        m.setText("Informação sobre estado do programa");
                        m.setMessage("O uuid " + regimenomeespecificado + " parametrizado para o regime " + cmbRegime.getText() + " não existe no OpenMRS.");
                        m.open();

                        return false;
                    }

                    try {
                        response.substring(21, 57);
                    } catch (StringIndexOutOfBoundsException siobe) {
                        MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                        m.setText("Informação sobre estado do programa");
                        m.setMessage("Verifica se o nome do provedor " + cmbDoctor.getText().trim() + " existe no OpenMRS.");
                        m.open();

                        return false;
                    }

                    try {
                        strFacility.substring(21, 57);
                    } catch (StringIndexOutOfBoundsException siobe) {
                        MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                        m.setText("Informação sobre estado do programa");
                        m.setMessage("Verifica se o nome da Unidade Sanitaria " + facility + " existe no OpenMRS.");
                        m.open();
                        return false;
                    }

                    if (StringUtils.isEmpty(regimenomeespecificado)) {
                        MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                        m.setText("Informação sobre estado do programa");
                        m.setMessage("Verifica a parametrização do uuid do regime " + cmbRegime.getText() + " na base de dados do iDART.");
                        m.open();
                        return false;
                    }
                }else {
                    getLog().error("O Utilizador "+currentUser.getUsername()+" não se encontra no OpenMRS ou serviço rest no OpenMRS não está  em funcionamento.");
                }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if ((cmbLinha.getText().trim().equals("")) || (cmbRegime.getText().trim().equals("")) || (cmbDoctor.getText().trim().equals(""))
                || (lblNewPrescriptionId.getText().trim().equals(""))
                || (cmbDuration.getText().trim().equals(""))) {
            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Campos em falta");
            missing
                    .setMessage("Alguns Campos estão vazios. Inserir informação nos campos *.");
            missing.open();
            txtPatientId.setFocus();
            return false;
        } else if (btnDataInicioNoutroServico.getDate() != null && btnDataInicioNoutroServico.getDate().after(new Date())) {

            MessageBox dataNoutroServico = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            dataNoutroServico.setText("Data Inicio Noutro Serviço Inválido");
            dataNoutroServico
                    .setMessage("A data do inicio noutro serviço não pode ser depois da data de hoje.");
            dataNoutroServico.open();
            btnDataInicioNoutroServico.setFocus();
            return false;

        } else if (cmbUpdateReason.getText().equals("Alterar") && cmbMotivoMudanca.getText().length() == 0) {

            MessageBox dataNoutroServico = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            dataNoutroServico.setText("Motivo da Mudança");
            dataNoutroServico
                    .setMessage("Insira o motivo da mudança.");
            dataNoutroServico.open();
            cmbMotivoMudanca.setFocus();

            return false;
        } else if (cmbUpdateReason.getText().equals("Inicia") && conexao.jaTemFilaInicio(localPrescription.getPatient().getPatientId())) {

            MessageBox dataNoutroServico = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            dataNoutroServico.setText("Paciente ja iniciou tarv");
            dataNoutroServico
                    .setMessage("Este paciente já iniciou o tarv....mude o TIPO TARV para Manter");
            dataNoutroServico.open();
            cmbMotivoMudanca.setFocus();

            return false;
        } else if (lblUpdateReason.isVisible()
                && cmbUpdateReason.getText().trim().equals("")) {
            MessageBox missingUpdateReason = new MessageBox(getShell(),
                    SWT.ICON_ERROR | SWT.OK);
            missingUpdateReason.setText("Inserir motivo de mudança");
            missingUpdateReason
                    .setMessage("O campo 'Motivo de mudança' não foi inserido.");
            cmbUpdateReason.setFocus();
            missingUpdateReason.open();
            return false;
        } else if (!checkDispensaTrimestral()) {
            return false;
        } else if (!cheackDispensaSemestral()) {
            return false;
        } else if (!txtWeight.getText().equals("")) {
            try {
                Double.parseDouble(txtWeight.getText());
            } catch (NumberFormatException nfe) {
                MessageBox incorrectData = new MessageBox(getShell(),
                        SWT.ICON_ERROR | SWT.OK);
                incorrectData.setText("Valor do peso incorrecto");
                incorrectData
                        .setMessage("O peso inserido é incorrecto.Inserir um número");
                incorrectData.open();
                txtWeight.setFocus();
                return false;
            }
        } else if (cmbDispensaTrimestral.getText().equals("Sim") && cmbDispensaSemestral.getText().equals("Sim")) {
            MessageBox semestralTrimestral = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            semestralTrimestral.setText("Campos incorrectamente seleccionados");
            semestralTrimestral.setMessage("O paciente não pode fazer parte da dispensa trimestral e semestral ao mesmo tempo");
            semestralTrimestral.open();
            cmbDispensaTrimestral.setFocus();
            return false;
        } else if (lblTipoDispensaTrimestral.isVisible() && cmbTipoDispensaTrimestral.getText().trim().equals("")) {
            MessageBox missingTipoDispensaTrimestral = new MessageBox(getShell(),
                    SWT.ICON_ERROR | SWT.OK);
            missingTipoDispensaTrimestral
                    .setMessage("O campo 'Tipo DT: Deve ser preenchido(Novo ou Manutencao)?");
            cmbUpdateReason.setFocus();
            missingTipoDispensaTrimestral.open();
            return false;
        } else if (lblTipoDispensaSemestral.isVisible() && cmbTipoDispensaSemestral.getText().trim().equals("")) {
            MessageBox missingTipoDispensaSemestral = new MessageBox(getShell(),
                    SWT.ICON_ERROR | SWT.OK);
            missingTipoDispensaSemestral.setMessage("O campo 'Tipo DS: Deve ser preenchido(Novo ou Manutencao)?");
            cmbUpdateReason.setFocus();
            missingTipoDispensaSemestral.open();
            return false;
        } else if (chkBtnPrescicaoEspecial.getSelection() && cmbMotivoCriacao.getText().length() == 0) {

            MessageBox motivocriacaoprecricao = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            motivocriacaoprecricao.setText("Seleccione o motivo da prescrição especial");
            motivocriacaoprecricao.setMessage("Se seleccionou o campo Prescrição Especial deve escolher o motivo.");
            motivocriacaoprecricao.open();
            cmbMotivoCriacao.setFocus();

            return false;
        }
        if (cmbMotivoCriacao.getText().equalsIgnoreCase("Outro") && txtAreaNotes.getText().length() == 0) {

            MessageBox motivocriacaoprecricao = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            motivocriacaoprecricao.setText("Notas da prescrição vazia");
            motivocriacaoprecricao.setMessage("Se seleccionou o 'Outro' no motivo de criação, deve adicionar as notas da prescrição.");
            motivocriacaoprecricao.open();
            cmbMotivoCriacao.setFocus();

            return false;
        }

        return true;

    }

    /**
     * This method is called if it is found that this is the 1st prescription
     * for this patient. Certain areas of the GUI are updated to reflect this.
     */
    private void setFormToInitialPrescription() {
        getShell().redraw();

        cmbUpdateReason.setText("Manter");
        cmbUpdateReason.setEnabled(true);
        lblHeader.setText("Registar Prescrição Inicial");

        // btnDispenseDrugs.setEnabled(true);
        // lblPicDispenseDrugs.setEnabled(true);
        btnCaptureDate.setDate(new Date());
        btnSave.setText("Salvar Prescrição Inicial");
        btnSave.redraw();

        // Generate a new prescription id
        cmdUpdatePrescriptionId();
        cmbDuration.setText("1 mes");
        cmbDispensaTrimestral.select(1);
        cmbDispensaSemestral.select(1);

        cmbTipoDispensaTrimestral.setVisible(Boolean.FALSE);
        lblTipoDispensaTrimestral.setVisible(Boolean.FALSE);

        cmbTipoDispensaSemestral.setVisible(Boolean.FALSE);
        lblTipoDispensaSemestral.setVisible(Boolean.FALSE);
    }

    /**
     * This method is called if it is found that this is the not the 1st
     * prescription for this patient, but there's no current prescription.
     * Certain areas of the GUI are updated to reflect this.
     */
    private void setFormToReactivationPrescription() {

        btnSave.setText("Salvar Prescrição");
        btnSave.redraw();

        // Generate a new prescription id
        cmdUpdatePrescriptionId();
        cmbDuration.setText("1 mes");

        btnSearch.setEnabled(false);
        btnEkapaSearch.setEnabled(false);

    }

    /**
     * This method loads the GUI with details from the Prescription object
     * passed to it.
     */
    private void loadPrescriptionDetails() {

        /*
         * // set restrictions on the capture date try {
         * setCaptureDateRestrictions(); } catch (DateException e) {
         * e.printStackTrace(); }
         */
        cmbUpdateReason.setText("Manter");
        String tempAmtPerTime = "";
        cmbDoctor.setText("" + AdministrationManager.getDoctor(getHSession(), localPrescription.getDoctor().getFullname()).getFullname());

        try {
            cmbRegime.setText("" + AdministrationManager.loadRegime(localPrescription.getPatient().getId()));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            cmbLinha.setText(""
                    + AdministrationManager.loadLinha(localPrescription.getPatient().getId()));
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Dispensa Trimestral Semestral
        try {
            int estaNaDispensaTrimestral = AdministrationManager.loadDispensaTrimestral(localPrescription.getPatient().getId());
            int estaNaDispensaSemestral = AdministrationManager.loadDispensaSemestral(localPrescription.getPatient().getId());
            if (estaNaDispensaTrimestral == 1) {
                cmbDispensaTrimestral.select(0);
                lblTipoDispensaTrimestral.setVisible(true);
                cmbTipoDispensaTrimestral.setVisible(true);
                cmbDispensaSemestral.select(1);
//                lblDispensaSemestral.setEnabled(false);
                cmbDispensaSemestral.setEditable(false);
                cmbDispensaSemestral.setEnabled(false);
                lblTipoDispensaSemestral.setVisible(false);
                cmbTipoDispensaSemestral.setVisible(false);
                cmbTipoDispensaSemestral.setText("");
            } else {
                if (estaNaDispensaSemestral == 1) {
                    cmbDispensaSemestral.select(0);
                    lblTipoDispensaSemestral.setVisible(true);
                    cmbTipoDispensaSemestral.setVisible(true);
                    cmbDispensaTrimestral.select(1);
//                    lblDispensaTrimestral.setEnabled(false);
                    cmbDispensaTrimestral.setEnabled(false);
                    lblTipoDispensaTrimestral.setVisible(false);
                    cmbTipoDispensaTrimestral.setVisible(false);
                    cmbTipoDispensaTrimestral.setText("");
                } else {
                    cmbDispensaTrimestral.select(1);
                    cmbDispensaSemestral.select(1);
                }
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        if (localPrescription.getDuration() <= 2) {
            cmbDuration.setText(localPrescription.getDuration() + " semanas");
        } else {
            cmbDuration
                    .setText(localPrescription.getDuration() / 4 + " meses");
        }

        // cmbClinicalStage.setText("" + localPrescription.getClinicalStage());
        // set previous note details
        if (localPrescription.getNotes() != null) {
            txtAreaNotes.setText(localPrescription.getNotes());
        }
        // set the previous weight
        if (localPrescription.getWeight() != null) {
            txtWeight.setText(localPrescription.getWeight().toString());
        }

        // set the previous ppe
        try {

            String ppe = (AdministrationManager.loadPpe(localPrescription.getPatient().getId()));

//           log.trace(" PPE actual " + ppe);

            if (ppe.trim().equals("T")) {
                chkBtnPPE.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous TB
        try {

            String tb = (AdministrationManager.loadTb(localPrescription.getPatient().getId()));

//           log.trace(" TB actual " + tb);

            if (tb.trim().equals("T")) {
                chkBtnTB.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous SAAJ
        try {

            String saaj = (AdministrationManager.loadSAAJ(localPrescription.getPatient().getId()));

//           log.trace(" SAAJ actual " + saaj);

            if (saaj.trim().equals("T")) {
                chkBtnSAAJ.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous prep
        try {

            String prep = (AdministrationManager.loadPrEP(localPrescription.getPatient().getId()));

//           log.trace(" PREP actual " + prep);
            if (prep.trim().equals("T")) {
                chkBtnPrEP.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous ce
        try {

            String ce = (AdministrationManager.loadCE(localPrescription.getPatient().getId()));

//           log.trace(" CE actual " + ce);
            if (ce.trim().equals("T")) {
                chkBtnCE.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        // set the previous CCR
        try {

            String ccr = (AdministrationManager.loadCcr(localPrescription.getPatient().getId()));

//           log.trace(" CCR actual " + ccr);
            if (ccr.trim().equals("T")) {
                chkBtnCCR.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous CPN
        try {

            String cpn = (AdministrationManager.loadCpn(localPrescription.getPatient().getId()));

//           log.trace(" CPN actual " + cpn);
            if (cpn.trim().equals("T")) {
                chkBtnCPN.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous Abordagem Familiar
        try {

            String af = (AdministrationManager.loadAf(localPrescription.getPatient().getId()));

//           log.trace(" Abordagem Familiar actual " + af);
            if (af.trim().equals("T")) {
                chkBtnAF.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous Clube de Adesao
        try {

            String ca = (AdministrationManager.loadCa(localPrescription.getPatient().getId()));

//           log.trace(" CA actual " + ca);
            if (ca.trim().equals("T")) {
                chkBtnCA.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous Fluxo Rapido
        try {

            String fr = (AdministrationManager.loadFr(localPrescription.getPatient().getId()));

//           log.trace(" FR actual " + fr);
            if (fr.trim().equals("T")) {
                chkBtnFR.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous GAAC
        try {

            String gaac = (AdministrationManager.loadGaac(localPrescription.getPatient().getId()));

//           log.trace(" GAAC actual " + gaac);
            if (gaac.trim().equals("T")) {
                chkBtnGAAC.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous DC
        try {

            String dc = (AdministrationManager.loadDc(localPrescription.getPatient().getId()));

//           log.trace(" DC actual " + dc);
            if (dc.trim().equals("T")) {
                chkBtnDC.setSelection(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous PrescicaoEspecial
        try {

            String prescicaoespecial = (AdministrationManager.loadPrescricaoEspecial(localPrescription.getPatient().getId()));

//           log.trace(" PrescicaoEspecial actual " + prescicaoespecial);
            if (prescicaoespecial.trim().equals("T")) {
                chkBtnPrescicaoEspecial.setSelection(true);
                cmbMotivoCriacao.setEnabled(true);
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // set the previous Motivo Criacao
        try {

            String motivocriacaoespecial = (AdministrationManager.loadMotivoEspecial(localPrescription.getPatient().getId()));

//           log.trace(" Motivo Criacao " + motivocriacaoespecial);
            cmbMotivoCriacao.setText(motivocriacaoespecial);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        // Generate a new prescription id
        cmdUpdatePrescriptionId();

        List<PrescribedDrugs> drugs = localPrescription.getPrescribedDrugs();
        for (int i = 0; i < drugs.size(); i++) {

            PrescribedDrugs pd = drugs.get(i);
            Drug theDrug = pd.getDrug();
            Form theForm = theDrug.getForm();

            if (theForm.getFormLanguage1().equals("")) // is a cream - no amnt per time
            {
                tempAmtPerTime = "";
            } else {
                if (new BigDecimal(pd.getAmtPerTime()).scale() == 0) {
                    tempAmtPerTime = ""
                            + new BigDecimal(pd.getAmtPerTime())
                            .unscaledValue().intValue();
                } else {
                    tempAmtPerTime = "" + pd.getAmtPerTime();
                }
            }

            TableItem ti = new TableItem(tblDrugs, SWT.NONE);
            String[] temp = new String[8];
            temp[0] = "" + intDrugTableSize;
            // temp[1] = "0";
            temp[1] = theDrug.getName();
            temp[2] = theForm.getActionLanguage1();
            temp[3] = tempAmtPerTime;
            temp[4] = theForm.getFormLanguage1();
            temp[5] = "" + pd.getTimesPerDay();
            temp[6] = "Vezes por dia";

            ti.setText(temp);
            ti.setData(pd);
            intDrugTableSize += 1;

        }
    }

    /**
     * This method is called if the user clicks on a row in the drugs table. The
     * user is then asked if they want to delete the drug that they've selected.
     */
    private void cmdRemoveDrugWidgetSelected() {

        TableItem[] ti = tblDrugs.getSelection();

        if (ti != null) {
            try {
                String drug = ti[0].getText(1);
                MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION
                        | SWT.YES | SWT.NO);
                mb.setText("Remover o medicamento'" + drug + "'");
                mb.setMessage("Quer remover '" + drug
                        + "' desta Prescrição?");
                switch (mb.open()) {
                    case SWT.YES:
                        // Delete from Prescription
                        int index = tblDrugs.getSelectionIndex();
                        tblDrugs.remove(index);
                        for (int i = index; i < tblDrugs.getItemCount(); i++) {
                            TableItem oti = tblDrugs.getItem(i);
                            int number = Integer.parseInt(oti.getText(0));
                            number--;
                            oti.setText(0, "" + number);
                        }
                        intDrugTableSize--;
                        break;
                    case SWT.NO:
                        // Nothing
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION
                        | SWT.OK);
                mb.setText("Sem medicamento seleccionado ");
                mb.setMessage("Seleccionar um medicamento.");
                mb.open();

            }

        }

    }

    /**
     * This method swaps the drug selected with the one above it (moveAmount =
     * -1) or below it (moveAmount =1), if any
     *
     * @param moveAmount int
     */
    private void cmdMoveDrug(int moveAmount) {

        int index = tblDrugs.getSelectionIndex();

        if (((index + moveAmount) >= 0)
                && ((index + moveAmount) < tblDrugs.getItemCount())) {

            TableItem tmpTi = tblDrugs.getItem(index);
            TableItem tmpTiMovedTo = tblDrugs.getItem(index + moveAmount);

            String[] tempMoving = new String[8];
            tempMoving[0] = tmpTiMovedTo.getText(0);
            tempMoving[1] = tmpTi.getText(1);
            tempMoving[2] = tmpTi.getText(2);
            tempMoving[3] = tmpTi.getText(3);
            tempMoving[4] = tmpTi.getText(4);
            tempMoving[5] = tmpTi.getText(5);
            tempMoving[6] = tmpTi.getText(6);

            PrescribedDrugs pdMoving = (PrescribedDrugs) tmpTi.getData();

            String[] tempMovedTo = new String[8];
            tempMovedTo[0] = tmpTi.getText(0);
            tempMovedTo[1] = tmpTiMovedTo.getText(1);
            tempMovedTo[2] = tmpTiMovedTo.getText(2);
            tempMovedTo[3] = tmpTiMovedTo.getText(3);
            tempMovedTo[4] = tmpTiMovedTo.getText(4);
            tempMovedTo[5] = tmpTiMovedTo.getText(5);
            tempMovedTo[6] = tmpTiMovedTo.getText(6);

            PrescribedDrugs pdMovedTo = (PrescribedDrugs) tmpTiMovedTo
                    .getData();

            tblDrugs.getItem(index).setText(tempMovedTo);
            tblDrugs.getItem(index).setData(pdMovedTo);

            tblDrugs.getItem(index + moveAmount).setText(tempMoving);
            tblDrugs.getItem(index + moveAmount).setData(pdMoving);

            tblDrugs.setSelection(index + moveAmount);

        }
    }

    /**
     * This method loads the GUI with details of the global Patient object
     * ('thePatient').
     */
    public void loadPatientDetails() {

        // set restrictions on the capture date
        try {
            setCaptureDateRestrictions();
        } catch (DateException e) {
            e.printStackTrace();
        }

        try {

            txtPatientId.setText(thePatient.getPatientId());
            txtName.setText(thePatient.getFirstNames());
            txtSurname.setText(thePatient.getLastname());
            txtClinic.setText(thePatient.getCurrentClinic().getClinicName());
            txtAge.setText(String.valueOf(thePatient.getAge()));

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");
            txtDOB.setText(String.valueOf(sdf.format(thePatient
                    .getDateOfBirth())));

            btnDispenseDrugs.setEnabled(true);
            lblPicDispenseDrugs.setEnabled(true);

            // Show the child icon if age <= 12
            if (thePatient.getAge() <= 12) {
                lblPicChild.setVisible(true);
            } else {
                lblPicChild.setVisible(false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called when the user presses the "Add Drug to this
     * Prescription" button.
     */
    private void cmdAddDrugWidgetSelected() {
        if (fieldsOk()) {
            try {
                // Add a new table item
                TableItem ti = new TableItem(tblDrugs, SWT.NONE);
                ti.setText(0, (Integer.toString(tblDrugs.getItemCount())));
                new PrescriptionObject(getHSession(), ti, false, getShell());
                intDrugTableSize = tblDrugs.getItemCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void cmdAddAssociatedDrugWidgetSelected() {
        if (fieldsOk()) {
            try {
                // Add a new table item
                TableItem ti = new TableItem(tblDrugs, SWT.NONE);
                ti.setText(0, (Integer.toString(tblDrugs.getItemCount())));
                new PrescriptionObjectAssociated(getHSession(), ti, false, cmbRegime.getText(), getShell());
                intDrugTableSize = tblDrugs.getItemCount();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadPrescription() {
        enableFields(true);

        patientsPrescriptions = thePatient.getPrescriptions();

        /*
         * // set date restrictions try { setCaptureDateRestrictions(); } catch
         * (DateException e) { e.printStackTrace(); }
         */
        if (patientsPrescriptions.size() > 0) {
            // Patient has previous Prescription

            cmbUpdateReason.setEnabled(true);
            cmbUpdateReason.setText("");

            localPrescription = thePatient.getMostRecentPrescription();

            if (localPrescription == null) {
                // Patient has had a previous prescription, but doesn't
                // have an active one at the moment
                localPrescription = new Prescription();
                localPrescription.setPatient(thePatient);
                setFormToReactivationPrescription();
                // Generate a new prescription id
                cmdUpdatePrescriptionId();
            } else {

                loadPrescriptionDetails();

                lblHeader.setText("Actualizar a Prescrição");

                btnDispenseDrugs.setEnabled(true);
                lblPicDispenseDrugs.setEnabled(true);
            }

        } else {
            // New Prescription
            localPrescription = new Prescription();
            localPrescription.setPatient(thePatient);
            setFormToInitialPrescription();

            // Generate a new prescription id
            cmdUpdatePrescriptionId();
        }
        loadPatientDetails();
        avaliaMDSSelectionActual();
        btnSearch.setEnabled(false);
        btnEkapaSearch.setEnabled(false);
        txtPatientId.setEnabled(false);
    }

    private void populateGUI() {

        loadPatientDetails();

        patientsPrescriptions = thePatient.getPrescriptions();

        if (patientsPrescriptions != null && patientsPrescriptions.size() > 0) {
            // Patient has previous Prescription

            cmbUpdateReason.setEnabled(true);
            cmbUpdateReason.setText("");
            localPrescription = thePatient.getMostRecentPrescription();

            if (localPrescription == null) {
                // Patient has had a previous prescription, but doesn't have
                // an active one at the moment
                localPrescription = new Prescription();
                localPrescription.setPatient(thePatient);
                setFormToReactivationPrescription();
                // Generate a new prescription id
                cmdUpdatePrescriptionId();
            } else {

                loadPrescriptionDetails();

                lblHeader.setText("Actualizar a Prescrição");

                btnDispenseDrugs.setEnabled(true);
                lblPicDispenseDrugs.setEnabled(true);
            }

        } else {
            // New Prescription
            localPrescription.setPatient(thePatient);
            setFormToInitialPrescription();
            // Generate a new prescription id
            cmdUpdatePrescriptionId();
        }

        loadPatientDetails();

        enableFields(true);

        txtPatientId.setEnabled(false);
        btnSearch.setEnabled(false);
        btnEkapaSearch.setEnabled(false);

    }

    /*
     * This method called a "Search" GUI. Upon successful selection from the
     * user, the PackageManager loads the selected patient's information
     */
    private void cmdSearchWidgetSelected() {

        String parsedPatientId = PatientBarcodeParser.getPatientId(txtPatientId
                .getText());

        PatientSearch search = new PatientSearch(getShell(), getHSession());
        PatientIdentifier identifier = search.search(parsedPatientId);

        if (identifier != null) {
            thePatient = identifier.getPatient();
            txtPatientId.setText(thePatient.getPatientId());

            if (!thePatient.getAccountStatusWithCheck()) {
                MessageBox noPatient = new MessageBox(getShell(),
                        SWT.ICON_ERROR | SWT.OK);

                noPatient.setText("Paciente não possui um Episódio actual");
                noPatient
                        .setMessage("O Paciente "
                                + (txtPatientId.getText()).toUpperCase()
                                + " não possui um Episódio actual. \n\nPrecisa Iniciar um novo episódio para este Paciente (usando a tela 'Actualizar Paciente Existente') antes de registra a sua Prescrição.");
                noPatient.open();
                txtPatientId.setFocus();
                txtPatientId.setText("");
            } else {
                loadPrescription();
            }
        }
    }

    private void cmdEkapaSearchWidgetSelected() {

        SearchPatientGui ps = new SearchPatientGui(getHSession(), getShell(),
                true);

        Patient p = ps.getPatient();
        // check our local database if this patient already exists
        if (p != null) {
            Patient patient = PatientManager.getPatient(getHSession(), p
                    .getId());
            if (patient == null) {
                patient = p;

                MessageBox mSave = new MessageBox(getShell(), SWT.ICON_QUESTION
                        | SWT.YES | SWT.NO);
                mSave.setText("Importa Paciente eKapa?");
                mSave.setMessage("Quer importar o paciente '"
                        + patient.getPatientId() + "' ("
                        + patient.getLastname() + "," + patient.getFirstNames()
                        + ") para a base de dados iDART?");
                switch (mSave.open()) {
                    case SWT.YES:

                        PatientManager.savePatient(getHSession(), patient);
                        break;
                    case SWT.NO:
                        return;
                }

            }
            thePatient = PatientManager.getPatient(getHSession(), p.getId());
            populateGUI();

        }

    }

    private void cmdDispenseARVDrugsSelected() {
        if (submitForm()) {
            if (!fromShortcut) {
                new NewPatientPackaging(getParent(), localPrescription.getPatient());
            }
            closeShell(true);
        }
    }

    /**
     * This method looks at all this patient's prescriptions and determines if
     * this should be the 'Initial' Prescription. Set the global variable
     * 'isInitialPrescription' accordingly.
     */
    private void checkFirstPrescription() {

        patientsPrescriptions = thePatient.getPrescriptions();

        if (patientsPrescriptions == null || patientsPrescriptions.isEmpty()) {
            isInitialPrescription = true;
        } else {
            isInitialPrescription = false;
        }
    }

    /**
     * Method drugsAddedToPrescription.
     *
     * @return boolean
     */
    private boolean drugsAddedToPrescription() {

        boolean drugsAdded = true;

        if (tblDrugs.getItemCount() < 1) {
            MessageBox noDrugsAdded = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            noDrugsAdded.setText("Sem medicamentos na Prescrição");
            noDrugsAdded
                    .setMessage("Nenhum medicamento foi adicionado nesta Prescrição.");
            noDrugsAdded.open();
            drugsAdded = false;
        }

        return drugsAdded;

    }

    /**
     * Method submitForm.
     *
     * @return boolean
     */
    @Override
    protected boolean submitForm() {

        boolean saveSuccessful = false;
        boolean deletedPrescription = false;

        // Check if all the fields are filled in
        if (fieldsOk() && drugsAddedToPrescription()) {
            MessageDialog mSave = new MessageDialog(getShell(), isInitialPrescription ? "Registar a Prescrição inicial do paciente "
                    .concat(txtPatientId.getText())
                    : "Registar Nova Prescrição do paciente "
                    .concat(txtPatientId.getText()), null, isInitialPrescription ? "Quer registar esta Prescrição para o paciente "
                    .concat(txtPatientId.getText()).concat("?")
                    : "Quer actualizar as mudanças para o paciente "
                    .concat(txtPatientId.getText()).concat("?"), MessageDialog.QUESTION, new String[]{"Sim", "Não"}, 0);

            MessageDialog mSaveInicia = new MessageDialog(getShell(), isInitialPrescription ? "Registar a Prescrição inicial do paciente "
                    .concat(txtPatientId.getText())
                    : "Registar Nova Prescrição do paciente "
                    .concat(txtPatientId.getText()), null, isInitialPrescription ? "ATENÇÃO: SELECCIONOU O TIPO TARV INICIAL\nTEM A CERTEZA "
                    + "DE QUE ESTE PACIENTE ESTÁ A INICIAR O TARV? \n NID:  "
                    .concat(txtPatientId.getText()).concat("?")
                    : "ATENÇÃO: SELECCIONOU O TIPO TARV INICIAL\nTEM A CERTEZA DE QUE ESTE PACIENTE ESTÁ A INICIAR O TARV? \n NID: "
                    .concat(txtPatientId.getText()).concat(""), MessageDialog.QUESTION, new String[]{"Sim", "Não"}, 0);

            if (cmbUpdateReason.getText().trim().equals("Inicia")) {

                switch (mSaveInicia.open()) {
                    case 0:
                        Transaction tx = null;
                        try {
                            tx = getHSession().beginTransaction();
                            if (localPrescription.getPatient().getId() <= 0) {
                                PatientManager.savePatient(getHSession(), localPrescription.getPatient());
                            }
                            setLocalPrescription();

                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                            Prescription oldPrescription = localPrescription.getPatient().getCurrentPrescription();
                            // Check if any packages have been created for the
                            // prescription
                            if ((oldPrescription != null) && PackageManager.getPackagesForPrescription(getHSession(), oldPrescription).size() == 0) {
                                List<PrescribedDrugs> prescribedDrugs = oldPrescription.getPrescribedDrugs();
                                String drugs = "";
                                if (prescribedDrugs.size() == 0) {
                                    drugs = "\nNão há medicamentos nesta prescrição!";
                                }
                                for (int i = 0; i < prescribedDrugs.size(); i++) {
                                    drugs = drugs
                                            + "\n\t\t"
                                            + prescribedDrugs.get(i).getDrug().getName();
                                }

                                MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);

                                box.setText("Remover Prescrições anteriores, não usadas");
                                box.setMessage("Este paciente não recebeu nenhum medicamento desde "
                                        + "a sua Prescrição anterior com o id "
                                        + oldPrescription.getPrescriptionId()
                                        + " (detalhes abaixo). \n\n"
                                        + "Clínico: "
                                        + oldPrescription.getDoctor()
                                        .getFullname()
                                        + "\nRegime: "
                                        + oldPrescription.getRegimeTerapeutico()
                                        .getRegimeesquema()
                                        + "\nDuração: "
                                        + oldPrescription.getDuration()
                                        + " semanas "
                                        + "\nEstagio Clínico: "
                                        + oldPrescription.getClinicalStage()
                                        + "\nPeso: "
                                        + oldPrescription.getWeight()
                                        + "\nRegistado em: "
                                        + sdf.format(oldPrescription.getDate())
                                        + "\nMedicamentos na Prescrição:\t"
                                        + drugs
                                        + "\n\nGostaria de apagar esta Prescrição anterior não utilizada, "
                                        + "e substituí-lo com o que você acabou de criar?");
                                switch (box.open()) {
                                    case SWT.YES:

                                        deleteScript(oldPrescription, tx);
                                        deletedPrescription = true;
                                        break;
                                }
                            }

                            // de-normalise table to speed up reports
                            if (localPrescription.containsARVDrug()) {
                                localPrescription.setDrugTypes("ARV");
                            }

                            if (patientsPrescriptions.size() > 0) {
                                if (!localPrescription.getRegimeTerapeutico().getCodigoregime().equals(oldPrescription.getRegimeTerapeutico().getCodigoregime())) {
                                    MessageBox errorBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                                    errorBox.setText("Não foi possível salvar a prescrição, má alteração da linha terapêutica");
                                    errorBox.setMessage("Não é possível mudar o regime terapeutico do paciente de " + oldPrescription.getRegimeTerapeutico().getRegimeesquema()
                                            + " para " + localPrescription.getRegimeTerapeutico().getRegimeesquema());
                                    errorBox.open();
                                    saveSuccessful = false;
                                } else {
                                    PackageManager.saveNewPrescription(getHSession(), localPrescription, deletedPrescription);
                                    getHSession().flush();
                                    tx.commit();
                                    getLog().info("Saved Prescription for Patient: " + localPrescription.getPatient().getId());
                                    MessageBox done = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                                    done.setText("Base de dados actualizada");
                                    done.setMessage("Prescrição '".concat(localPrescription.getPrescriptionId()).concat("' foi adicionado ao paciente '").concat(localPrescription.getPatient().getPatientId()).concat("'."));
                                    done.open();
                                    saveSuccessful = true;
                                }
                            } else if (patientsPrescriptions.size() == 0) {
                                PackageManager.saveNewPrescription(getHSession(), localPrescription, deletedPrescription);
                                getHSession().flush();
                                tx.commit();
                                getLog().info("Saved Prescription for Patient: " + localPrescription.getPatient().getId());
                                MessageBox done = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                                done.setText("Base de dados actualizada");
                                done.setMessage("Prescrição '".concat(localPrescription.getPrescriptionId()).concat("' foi adicionado ao paciente '").concat(localPrescription.getPatient().getPatientId()).concat("'."));
                                done.open();
                                saveSuccessful = true;
                            }

                        } catch (IllegalArgumentException ie) {
                            MessageBox errorBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                            errorBox.setText("Não foi possível salvar: A data do registo da Prescrição é inválida");
                            errorBox.setMessage("A Prescrição '".concat(localPrescription.getPrescriptionId()).concat("' tem a data de registo antes da data de de registo da Prescrição anterior. A Prescrição não pode ser salva "));
                            errorBox.open();
                            if (tx != null) {
                                tx.rollback();
                            }
                            getLog().error(ie);
                            saveSuccessful = false;
                        } catch (HibernateException he) {

                            MessageBox errorBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                            errorBox.setText("Não pode salvar: Actualização da base de dados falhou");
                            errorBox.setMessage("Houve um problema ao salvar esta receita. Por favor, tente novamente.");
                            if (tx != null) {
                                tx.rollback();
                            }
                            getLog().error(he);
                            saveSuccessful = false;
                        }

                        break;
                    case SWT.NO:
                        // Close the GUI
                        // cmdCancelWidgetSelected();
                        saveSuccessful = false;
                }

            } else {

                switch (mSave.open()) {
                    case 0:
                        Transaction tx = null;
                        try {
                            tx = getHSession().beginTransaction();
                            if (localPrescription.getPatient().getId() <= 0) {
                                PatientManager.savePatient(getHSession(), localPrescription.getPatient());
                            }
                            setLocalPrescription();

                            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
                            Prescription oldPrescription = localPrescription.getPatient().getCurrentPrescription();
                            // Check if any packages have been created for the
                            // prescription
                            if ((oldPrescription != null) && PackageManager.getPackagesForPrescription(getHSession(), oldPrescription).size() == 0) {
                                List<PrescribedDrugs> prescribedDrugs = oldPrescription.getPrescribedDrugs();
                                String drugs = "";
                                if (prescribedDrugs.size() == 0) {
                                    drugs = "\nNão há medicamentos nesta Prescrição!";
                                }
                                for (int i = 0; i < prescribedDrugs.size(); i++) {
                                    drugs = drugs + "\n\t\t" + prescribedDrugs.get(i).getDrug().getName();
                                }
                                MessageBox box = new MessageBox(getShell(),
                                        SWT.ICON_QUESTION | SWT.YES | SWT.NO);

                                box.setText("Remover Prescrições anteriores, não usadas");
                                box.setMessage("Este paciente não recebeu nenhum medicamento desde "
                                        + "a sua Prescrição anterior com o id "
                                        + oldPrescription.getPrescriptionId()
                                        + " (detalhes abaixo). \n\n '"
                                        + "Clínico: "
                                        + oldPrescription.getDoctor()
                                        .getFullname()
                                        + "\nRegime: "
                                        + oldPrescription.getRegimeTerapeutico()
                                        .getRegimeesquema()
                                        + "\nDuração: "
                                        + oldPrescription.getDuration()
                                        + " semanas "
                                        + "\nEstágio Clínico: "
                                        + oldPrescription.getClinicalStage()
                                        + "\nPeso: "
                                        + oldPrescription.getWeight()
                                        + "\nRegistado em: "
                                        + sdf.format(oldPrescription.getDate())
                                        + "\nMedicamentos na Prescrição:\t"
                                        + drugs
                                        + "\n\nGostaria de apagar esta Prescrição anterior não utilizada, "
                                        + "e substituí-lo com o que você acabou de criar?");
                                switch (box.open()) {
                                    case SWT.YES:

                                        deleteScript(oldPrescription, tx);
                                        deletedPrescription = true;
                                        break;
                                }
                            }

                            // de-normalise table to speed up reports
                            if (localPrescription.containsARVDrug()) {
                                localPrescription.setDrugTypes("ARV");
                            }

                            if (patientsPrescriptions.size() > 0) {
                                PackageManager.saveNewPrescription(getHSession(), localPrescription, deletedPrescription);
                                getHSession().flush();
                                tx.commit();
                                getLog().info("Saved Prescription for Patient: " + localPrescription.getPatient().getId());
                                MessageBox done = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                                done.setText("Base de dados actualizada");
                                done.setMessage("Prescrição '".concat(localPrescription.getPrescriptionId()).concat("' foi adicionado ao paciente '").concat(localPrescription.getPatient().getPatientId()).concat("'."));
                                done.open();
                                saveSuccessful = true;
                            } else if (patientsPrescriptions.size() == 0) {
                                PackageManager.saveNewPrescription(getHSession(), localPrescription, deletedPrescription);
                                getHSession().flush();
                                tx.commit();
                                getLog().info("Saved Prescription for Patient: " + localPrescription.getPatient().getId());
                                MessageBox done = new MessageBox(getShell(), SWT.OK | SWT.ICON_INFORMATION);
                                done.setText("Base de dados actualizada");
                                done.setMessage("Prescrição '".concat(localPrescription.getPrescriptionId()).concat("' foi adicionado ao paciente '").concat(localPrescription.getPatient().getPatientId()).concat("'."));
                                done.open();
                                saveSuccessful = true;
                            }

                        } catch (IllegalArgumentException ie) {
                            MessageBox errorBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                            errorBox.setText("Não foi possível salvar: A data do registo da Prescrição é inválida");
                            errorBox.setMessage("A Prescrição '".concat(localPrescription.getPrescriptionId()).concat("' tem a data de registo antes da data de de registo da Prescrição anterior. A Prescrição não pode ser salva "));
                            errorBox.open();
                            if (tx != null) {
                                tx.rollback();
                            }
                            getLog().error(ie);
                            saveSuccessful = false;
                        } catch (HibernateException he) {

                            MessageBox errorBox = new MessageBox(getShell(), SWT.OK | SWT.ICON_ERROR);
                            errorBox.setText("Não pode salvar: Actualização da base de dados falhou");
                            errorBox.setMessage("Houve um problema ao salvar esta receita. Por favor, tente novamente.");
                            if (tx != null) {
                                tx.rollback();
                            }
                            getLog().error(he);
                            saveSuccessful = false;
                        }

                        break;
                    case SWT.NO:
                        // Close the GUI
                        // cmdCancelWidgetSelected();
                        saveSuccessful = false;
                }
            }

        }

        return saveSuccessful;
    }

    /**
     * Saves the new prescription to the database
     */
    private void cmdSavePrescriptionWidgetSelected() {

        if (submitForm()) {
            cmdCancelWidgetSelected();
        }

    }

    private void cmdAddDoctorWidgetSelected() {
        AddDoctor.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                true);
        AddDoctor ad = new AddDoctor(getShell());
        ad.addChangeListener(this);
        // cmbDoctor.removeAll();
        // CommonObjects.populateDoctors(cmbDoctor, false);
    }

    /**
     * This method sets the global variable 'localPrescription', using
     * information entered by user onto this GUI.
     */
    private void setLocalPrescription() {

        localPrescription = new Prescription();
        localPrescription.setPrescriptionId(lblNewPrescriptionId.getText());
        localPrescription.setPatient(thePatient);
        localPrescription.setReasonForUpdate(cmbUpdateReason.getText());
        localPrescription.setDatainicionoutroservico(btnDataInicioNoutroServico.getDate());
        localPrescription.setTipoDT(cmbTipoDispensaTrimestral.getText());
        localPrescription.setTipoDS(cmbTipoDispensaSemestral.getText());
        localPrescription.setDurationSentence(cmbDuration.getText());
        localPrescription.setMotivocriacaoespecial(cmbMotivoCriacao.getText());

        //PTV , PPE and Tb
        if (chkBtnPPE.getSelection()) {
            localPrescription.setPpe('T');

        } else {
            localPrescription.setPpe('F');
        }

        if (chkBtnPrEP.getSelection()) {
            localPrescription.setPrep('T');

        } else {
            localPrescription.setPrep('F');
        }

        if (chkBtnCE.getSelection()) {
            localPrescription.setCe('T');

        } else {
            localPrescription.setCe('F');
        }

        if (chkBtnTB.getSelection()) {
            localPrescription.setTb('T');

        } else {
            localPrescription.setTb('F');
        }

        if (chkBtnSAAJ.getSelection()) {
            localPrescription.setSaaj('T');
        } else {
            localPrescription.setSaaj('F');
        }

        if (chkBtnCPN.getSelection()) {
            localPrescription.setCpn('T');
        } else {
            localPrescription.setCpn('F');
        }

        if (chkBtnCCR.getSelection()) {
            localPrescription.setCcr('T');
        } else {
            localPrescription.setCcr('F');
        }

        if (chkBtnGAAC.getSelection()) {
            localPrescription.setGaac('T');
        } else {
            localPrescription.setGaac('F');
        }

        if (chkBtnAF.getSelection()) {
            localPrescription.setAf('T');
        } else {
            localPrescription.setAf('F');
        }

        if (chkBtnCA.getSelection()) {
            localPrescription.setCa('T');
        } else {
            localPrescription.setCa('F');
        }

        if (chkBtnFR.getSelection()) {
            localPrescription.setFr('T');
        } else {
            localPrescription.setFr('F');
        }

        if (chkBtnFR.getSelection()) {
            localPrescription.setFr('T');
        } else {
            localPrescription.setFr('F');
        }

        if (chkBtnDC.getSelection()) {
            localPrescription.setDc('T');
        } else {
            localPrescription.setDc('F');
        }

        if (chkBtnPrescicaoEspecial.getSelection()) {
            localPrescription.setPrescricaoespecial('T');
        } else {
            localPrescription.setPrescricaoespecial('F');
        }

        localPrescription.setTpc('F');

        localPrescription.setTpi('F');

        localPrescription.setPtv('F');

        // weight
        if (!txtWeight.getText().equals("")) {
            localPrescription
                    .setWeight(Double.parseDouble(txtWeight.getText()));
        } else {
            localPrescription.setWeight(0.0);
        }

        // clinical stage
        // if (cmbClinicalStage.getText() != null
        // && !(cmbClinicalStage.getText().equals(""))) {
        // localPrescription.setClinicalStage(Integer
        // .parseInt(cmbClinicalStage.getText()));
        // }
        // duration
        if (cmbDuration.getText().endsWith("semanas")) {
            String[] s = cmbDuration.getText().split(" ");

            localPrescription.setDuration(Integer.parseInt(s[0]));

        } else {
            String[] s = cmbDuration.getText().split(" ");

            localPrescription.setDuration(Integer.parseInt(s[0]) * 4);

        }

        Date scriptDate = btnCaptureDate.getDate();

        // if scriptDate is today, store the time too, else store 12am
        Calendar compareCal = Calendar.getInstance();
        Calendar scriptDateCal = Calendar.getInstance();
        scriptDateCal.setTime(scriptDate);

        if ((compareCal.get(Calendar.DAY_OF_MONTH) == scriptDateCal
                .get(Calendar.DAY_OF_MONTH))
                && (compareCal.get(Calendar.MONTH) == scriptDateCal
                .get(Calendar.MONTH))
                && (compareCal.get(Calendar.YEAR) == scriptDateCal
                .get(Calendar.YEAR))) {
            scriptDate = new Date();
        }

        localPrescription.setDate(scriptDate);
        localPrescription.setDoctor(AdministrationManager.getDoctor(
                getHSession(), cmbDoctor.getText()));

        localPrescription.setRegimeTerapeutico(AdministrationManager.getRegimeTerapeutico(
                getHSession(), cmbRegime.getText()));

        localPrescription.setLinha(AdministrationManager.getLinha(
                getHSession(), cmbLinha.getText()));

        localPrescription.setMotivoMudanca(cmbMotivoMudanca.getText());

        localPrescription.setModified('T');
        localPrescription.setCurrent('T');
        localPrescription.setNotes(txtAreaNotes.getText());

        List<PrescribedDrugs> prescribedDrugsList = new ArrayList<PrescribedDrugs>();

        // Save the Prescription Drugs
        for (int i = 0; i < tblDrugs.getItemCount(); i++) {

            TableItem tmpItem = tblDrugs.getItem(i);

            PrescribedDrugs oldPD = (PrescribedDrugs) tmpItem.getData();
            if (oldPD != null) {
                PrescribedDrugs newPD = new PrescribedDrugs();
                newPD.setAmtPerTime(oldPD.getAmtPerTime());
                newPD.setDrug(oldPD.getDrug());
                newPD.setModified(oldPD.getModified());
                newPD.setPrescription(localPrescription);
                newPD.setTimesPerDay(oldPD.getTimesPerDay());
                prescribedDrugsList.add(newPD);
            }
        }

        localPrescription.setPrescribedDrugs(prescribedDrugsList);

        // set dispensatrimestral
        if (cmbDispensaTrimestral.getText().contentEquals("Sim")) {
            localPrescription.setDispensaTrimestral(1);
        }
        if (cmbDispensaTrimestral.getText().contentEquals("Nao")) {
            localPrescription.setDispensaTrimestral(0);
        }

        // set dispensasemestral
        if (cmbDispensaSemestral.getText().contentEquals("Sim")) {
            localPrescription.setDispensaSemestral(1);
        }
        if (cmbDispensaSemestral.getText().contentEquals("Nao")) {
            localPrescription.setDispensaSemestral(0);
        }
    }

    /**
     * This method is called when the user presses the 'Cancel' button
     */
    @Override
    protected void cmdCancelWidgetSelected() {
        localPrescription = null;
        btnCaptureDate.setValidator(null);
        closeShell(false);

    }

    /**
     * This method is called when the user presses the 'Clear' button
     */
    @Override
    protected void cmdClearWidgetSelected() {
        clearForm();
        btnCaptureDate.setValidator(null);
        txtPatientId.setFocus();
    }

    /**
     * adds the drugs in a drug group selected by the user to the drugs table.
     *
     * @param DrugGroupName String
     */
    @SuppressWarnings("unused")
    private void populateDrugsFromDrugGroup(String DrugGroupName) {

        String tempAmtPerTime = "";

        if (tblDrugs.getItemCount() > 0) {
            MessageBox mSave = new MessageBox(getShell(), SWT.ICON_QUESTION
                    | SWT.YES | SWT.NO);
            mSave.setText("Remover medicamentos existentes?");
            mSave
                    .setMessage("Esta prescrição contém medicamentos. Pretende remover?");
            switch (mSave.open()) {
                case SWT.YES:
                    tblDrugs.clearAll();
                    tblDrugs.removeAll();
                    intDrugTableSize = 0;
                    break;
                case SWT.NO:
                    // Close the GUI

            }
        }

        if (!"".equals(DrugGroupName)) {

            RegimeTerapeutico regimeTerapeutico = DrugManager.getRegimeTerapeutico(getHSession(), DrugGroupName);
            Iterator<RegimenDrugs> it = regimeTerapeutico.getRegimenDrugs().iterator();

            while (it.hasNext()) {
                RegimenDrugs rd = it.next();

                Drug d = rd.getDrug();

                TableItem ti = new TableItem(tblDrugs, SWT.NONE);
                ti.setText(0, (Integer.toString(tblDrugs.getItemCount())));

                Form f = d.getForm();

                if (f.getFormLanguage1().equals("")) // is a cream - no amnt per time
                {
                    tempAmtPerTime = "";
                } else {
                    if (new BigDecimal(rd.getAmtPerTime()).scale() == 0) {
                        tempAmtPerTime = ""
                                + new BigDecimal(rd.getAmtPerTime())
                                .unscaledValue().intValue();
                    } else {
                        tempAmtPerTime = "" + rd.getAmtPerTime();
                    }
                }

                // Add the details to the table
                String[] temp = new String[8];
                temp[0] = ti.getText(0);
                temp[1] = d.getName();
                temp[2] = f.getActionLanguage1();
                temp[3] = tempAmtPerTime;
                temp[4] = f.getFormLanguage1();
                temp[5] = (new Integer(rd.getTimesPerDay())).toString();
                temp[6] = "vezes por dia";
                temp[7] = String.valueOf(d.getPackSize());
                ti.setText(temp);

                PrescribedDrugs pd = new PrescribedDrugs();
                pd.setAmtPerTime(rd.getAmtPerTime());
                pd.setDrug(d);
                pd.setModified('T');
                pd.setTimesPerDay(rd.getTimesPerDay());

                ti.setData(pd);

                intDrugTableSize = tblDrugs.getItemCount();
            }
        }
    }

    /**
     * Clears the patientForm and sets the default values
     */
    @Override
    public void clearForm() {

        try {
            //cmbDispensaTrimestral.setText("");
            //cmbDispensaTrimestral.setEnabled(false);
            chkBtnPrescicaoEspecial.setSelection(false);
            txtPatientId.setText("");
            cmbMotivoMudanca.setText("");
            txtName.setText("");
            txtSurname.setText("");
            txtClinic.setText("");
            cmbDoctor.setText("");
            cmbRegime.setText("");
            txtWeight.setText("");
            txtDOB.setText("");
            lblNewPrescriptionId.setText("");
            cmbDuration.setText("");
            cmbLinha.setText("");
            // cmbClinicalStage.setText("");
            lblPicChild.setVisible(false);
            txtAge.setText("");
            cmbUpdateReason.setText("");
            cmbUpdateReason.setEnabled(false);
            cmbMotivoMudanca.setEnabled(false);
            txtAreaNotes.setText("");
            cmbDispensaSemestral.setText("Nao");
            cmbTipoDispensaSemestral.setText(EMPTY);
            lblTipoDispensaSemestral.setVisible(false);
            cmbTipoDispensaSemestral.setVisible(false);
            cmbDispensaTrimestral.setText("Nao");
            cmbTipoDispensaTrimestral.setText(EMPTY);
            lblTipoDispensaTrimestral.setVisible(false);
            cmbTipoDispensaTrimestral.setVisible(false);
            chkBtnTB.setSelection(false);
            chkBtnPrEP.setSelection(false);
            chkBtnCE.setSelection(false);
            chkBtnPPE.setSelection(false);
            chkBtnSAAJ.setSelection(false);
            chkBtnCCR.setSelection(false);
            chkBtnAF.setSelection(false);
            chkBtnCA.setSelection(false);
            chkBtnFR.setSelection(false);
            chkBtnDC.setSelection(false);
            chkBtnCPN.setSelection(false);
            chkBtnGAAC.setSelection(false);
            btnDataInicioNoutroServico.setEnabled(false);
            lblHeader.setText(Messages.getString("patient.prescription.dialog.title"));

            btnSave.setText(Messages.getString("patient.prescription.dialog.update"));
            tblDrugs.clearAll();
            tblDrugs.setItemCount(0);
            intDrugTableSize = 1;

            enableFields(false);

            //chkBtnTPC.setSelection(false);
            //chkBtnTPI.setSelection(false);
            btnSearch.setEnabled(true);
            btnEkapaSearch.setEnabled(true);
            txtPatientId.setEnabled(true);

            tblDrugs.clearAll();
            tblDrugs.setItemCount(0);
            intDrugTableSize = 1;

            Control old = editor.getEditor();
            if (old != null) {
                old.dispose();
            }

            txtPatientId.setFocus();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param enable
     */
    @Override
    protected void enableFields(boolean enable) {

        btnAddDrug.setEnabled(enable);
        btnAddAssociatedDrug.setEnabled(enable);
        lblPicAddDrug.setEnabled(enable);

        btnRemoveDrug.setEnabled(enable);
        btnPatientHistoryReport.setEnabled(enable);

        cmbDoctor.setEnabled(enable);
        btnAddDoctor.setEnabled(enable);
        btnMoveUp.setEnabled(enable);
        btnMoveDown.setEnabled(enable);
        cmbLinha.setEnabled(enable);
        btnDispenseDrugs.setEnabled(enable);

        // cmbClinicalStage.setEnabled(enable);
        cmbDispensaSemestral.setEnabled(enable);
        cmbDispensaTrimestral.setEnabled(enable);
        cmbDuration.setEnabled(enable);
        cmbRegime.setEnabled(enable);
        txtWeight.setEnabled(enable);
        chkBtnPrescicaoEspecial.setEnabled(enable);
        txtAreaNotes.setEnabled(enable);

        btnCaptureDate.setEnabled(enable);

        btnSave.setEnabled(enable);
        btnDispenseDrugs.setEnabled(enable);
        lblPicDispenseDrugs.setEnabled(enable);
        chkBtnPPE.setEnabled(enable);
        chkBtnTB.setEnabled(enable);
        chkBtnSAAJ.setEnabled(enable);
        //chkBtnTPI.setEnabled(enable);
        //chkBtnTPC.setEnabled(enable);

        chkBtnPPE.setEnabled(enable);
        chkBtnTB.setEnabled(enable);
        chkBtnCCR.setEnabled(enable);
        chkBtnSAAJ.setEnabled(enable);
        chkBtnGAAC.setEnabled(enable);
        chkBtnAF.setEnabled(enable);
        chkBtnCA.setEnabled(enable);
        chkBtnFR.setEnabled(enable);
        chkBtnPrEP.setEnabled(enable);
        chkBtnCPN.setEnabled(enable);
        chkBtnCE.setEnabled(enable);
        chkBtnDC.setEnabled(enable);

        Color theColour;

        if (enable) {
            theColour = ResourceUtils.getColor(iDartColor.WHITE);
        } else {
            theColour = ResourceUtils.getColor(iDartColor.WIDGET_BACKGROUND);
        }

        cmbUpdateReason.setBackground(theColour);
        cmbDoctor.setBackground(theColour);
        cmbDuration.setBackground(theColour);
        cmbRegime.setBackground(theColour);
        cmbMotivoMudanca.setBackground(theColour);
    }

    /**
     * This method initializes compDispense
     */
    private void createCompDispense() {

        compDispense = new Composite(getShell(), SWT.NONE);
        compDispense.setBounds(new Rectangle(330, 567, 240, 50));

        lblPicDispenseDrugs = new Label(compDispense, SWT.NONE);
        lblPicDispenseDrugs.setBounds(new Rectangle(0, 15, 50, 43));
        lblPicDispenseDrugs.setImage(ResourceUtils
                .getImage(iDartImage.DISPENSEPACKAGES));

        lblPicDispenseDrugs.setEnabled(false);

        btnDispenseDrugs = new Button(compDispense, SWT.NONE);
        btnDispenseDrugs.setBounds(new Rectangle(60, 20, 180, 30));
        btnDispenseDrugs.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDispenseDrugs.setText(Messages.getString("patient.prescription.dialog.dispense"));
        btnDispenseDrugs
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdDispenseARVDrugsSelected();
                    }
                });
        btnDispenseDrugs.setEnabled(false);

    }

    private void cmdUpdatePrescriptionId() {

        Date scriptDate = btnCaptureDate.getDate();
        if (scriptDate == null) {
            btnCaptureDate.setDate(new Date());
            scriptDate = btnCaptureDate.getDate();
        }

        String prescriptionId = PackageManager.getNewPrescriptionId(
                getHSession(), thePatient, scriptDate);
        lblNewPrescriptionId.setText(prescriptionId);
    }

    /**
     * This method gets the clinic that the patient belongs to at the time given
     * by the captureDate.
     */
    private void cmdUpdateClinic() {
        if (localPrescription != null) {
            txtClinic.setText((localPrescription.getPatient()
                    .getClinicAtDate(btnCaptureDate.getDate())).getClinicName());
        }
    }

    /**
     * View the patient history report, or show a patient selection report
     * parameters screen if no patient is selected
     */
    private void cmdPatientHistoryWidgetSelected() {

        getLog().info(
                "New Patient Packaging: User chose 'Patient History Report'");

        if (localPrescription != null) {
            PatientHistoryReport report = new PatientHistoryReport(getShell(),
                    localPrescription.getPatient());
            viewReport(report);
        }
    }

    @Override
    protected void cmdSaveWidgetSelected() {
        cmdSavePrescriptionWidgetSelected();
    }

    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }

    /**
     * Method deleteScript.
     *
     * @param prescriptionToRemove Prescription
     * @param tx                   Transaction
     */
    private void deleteScript(Prescription prescriptionToRemove, Transaction tx) {

        DeletionsManager.removeUndispensedPrescription(getHSession(), prescriptionToRemove);
        getHSession().flush();

        MessageBox mb = new MessageBox(getShell());
        mb.setText("Prescrição apagada com sucesso");
        mb.setMessage("Essa Prescrição foi removida com sucesso da base de dados.");
        mb.open();

    }

    public Patient getPatient() {
        return thePatient;
    }

    @Override
    public void changed(Object o) {
        if (o instanceof Doctor) {
            Doctor doctor = (Doctor) o;
            cmbDoctor.setText(doctor.getFullname());
        }

        if (o instanceof RegimeTerapeutico) {
            RegimeTerapeutico r = (RegimeTerapeutico) o;
            cmbRegime.setText(r.getRegimeesquema());
        }

        if (o instanceof LinhaT) {
            LinhaT r = (LinhaT) o;
            cmbLinha.setText(r.getLinhanome());
        }
    }

    private boolean checkDispensaTrimestral() {
        try {
            String result = cmbDispensaTrimestral.getItem(cmbDispensaTrimestral.getSelectionIndex());
            String prescritionDuration = null;
            switch (result) {
                case "Sim": {
                    if (cmbDuration.getSelectionIndex() < 0) {
                        prescritionDuration = cmbDuration.getItem(2);
                    } else {
                        prescritionDuration = cmbDuration.getItem(cmbDuration.getSelectionIndex());
                    }

                    if (cmbTipoDispensaTrimestral.getSelectionIndex() < 0) {
                        MessageBox mb = new MessageBox(getShell(),
                                SWT.ICON_ERROR | SWT.OK);
                        mb.setText("Dispensa Trimestral");
                        mb.setMessage("Selecionou dispensa trimestral, especifique o tipo de Dispensa Trimestral do Paciente");
                        mb.open();
                        return false;
                    }

                    if (("3 meses".equals(prescritionDuration) || ("3 months".equals(prescritionDuration)))) {
                        return true;
                    } else {
                        MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                        mb.setText("Dispensa Trimestral");
                        mb.setMessage("Selecionou dispensa trimestral, mas a duracao da prescricao nao e de 3 meses. PRETENDE MESMO DISPENSAR ESTA PRESCRIÇÃO?");
                        int resposta = mb.open();
                        if (resposta == SWT.NO) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }

                case "Nao": {
                    if (cmbDuration.getSelectionIndex() < 0) {
                        prescritionDuration = cmbDuration.getItem(2);
                    } else {
                        prescritionDuration = cmbDuration.getItem(cmbDuration.getSelectionIndex());
                    }

                    if (prescritionDuration.contentEquals("3 meses") || prescritionDuration.contentEquals("3 months")) {
                        MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                        mb.setText("Dispensa Trimestral");
                        mb.setMessage("A duração da prescrição é de 3 meses e não está na Dispensa Trimestral. PRETENDE MESMO DISPENSAR ESTA PRESCRIÇÃO?");
                        int resposta = mb.open();
                        if (resposta == SWT.NO) {
                            return false;
                        } else {
                            return true;
                        }

                    } else {
                        return true;
                    }
                }

                default:
                    MessageBox mb = new MessageBox(getShell());
                    mb.setText("Dispensa Trimestral");
                    mb.setMessage("Especifique se é dispensa trimestral ou nao");
                    mb.open();
                    return false;
            }
        } catch (java.lang.IllegalArgumentException ex) {

            MessageBox mb = new MessageBox(getShell());
            mb.setText("Dispensa Trimestral");
            mb.setMessage("Dispensa trimestral nao foi Selecionada");
            mb.open();
            return false;

        }

    }

    private boolean cheackDispensaSemestral() {
        try {
            String result = cmbDispensaSemestral.getItem(cmbDispensaSemestral.getSelectionIndex());
            String prescritionDuration = null;
            switch (result) {
                case "Sim": {
                    if (cmbDuration.getSelectionIndex() < 0) {
                        prescritionDuration = cmbDuration.getItem(2);
                    } else {
                        prescritionDuration = cmbDuration.getItem(cmbDuration.getSelectionIndex());
                    }

                    if (cmbTipoDispensaSemestral.getSelectionIndex() < 0) {
                        MessageBox mb = new MessageBox(getShell(),
                                SWT.ICON_ERROR | SWT.OK);
                        mb.setText("Dispensa Semestral");
                        mb.setMessage("Selecionou dispensa semestral, especifique o tipo de Dispensa Semestral do Paciente");
                        mb.open();
                        return false;
                    }

                    if (("6 meses".equals(prescritionDuration) || ("6 months".equals(prescritionDuration)))) {
                        return true;
                    } else {
                        MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                        mb.setText("Dispensa Semestral");
                        mb.setMessage("Selecionou dispensa semestral, mas a duracao da prescricao nao e de 6 meses. PRETENDE MESMO DISPENSAR ESTA PRESCRIÇÃO?");
                        int resposta = mb.open();
                        if (resposta == SWT.NO) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                }

                case "Nao": {
                    if (cmbDuration.getSelectionIndex() < 0) {
                        prescritionDuration = cmbDuration.getItem(2);
                    } else {
                        prescritionDuration = cmbDuration.getItem(cmbDuration.getSelectionIndex());
                    }

                    if (prescritionDuration.contentEquals("6 meses") || prescritionDuration.contentEquals("6 months")) {
                        MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
                        mb.setText("Dispensa Semestral");
                        mb.setMessage("A duração da prescrição é de 6 meses e não está na Dispensa Semestral. PRETENDE MESMO DISPENSAR ESTA PRESCRIÇÃO?");
                        int resposta = mb.open();
                        if (resposta == SWT.NO) {
                            return false;
                        } else {
                            return true;
                        }

                    } else {
                        return true;
                    }
                }

                default:
                    MessageBox mb = new MessageBox(getShell());
                    mb.setText("Dispensa Semestral");
                    mb.setMessage("Especifique se é dispensa semestral ou nao");
                    mb.open();
                    return false;
            }
        } catch (java.lang.IllegalArgumentException ex) {

            MessageBox mb = new MessageBox(getShell());
            mb.setText("Dispensa Semestral");
            mb.setMessage("Dispensa semestral nao foi Selecionada");
            mb.open();
            return false;

        }

    }

    public void avaliaMDSSelection() {

        chkBtnCCR.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (chkBtnCCR.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
//                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setSelection(false);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnDC.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (chkBtnDC.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
//                    chkBtnFR.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnCPN.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                if (chkBtnCPN.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnTB.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnTB.getSelection()) {
                    chkBtnCCR.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnCCR.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnPrEP.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnPrEP.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
//                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);

                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnSAAJ.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnSAAJ.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnPPE.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnPPE.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnGAAC.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnGAAC.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    //    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setSelection(false);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnAF.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnAF.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    //    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnFR.setSelection(false);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnCA.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnCA.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnCE.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnFR.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnFR.getSelection()) {
                    chkBtnTB.setEnabled(false);
//                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    //    chkBtnGAAC.setEnabled(false);
                    //    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
//                    chkBtnCCR.setEnabled(false);
//                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCE.setEnabled(true);

                    chkBtnPrEP.setSelection(false);
                    chkBtnGAAC.setSelection(false);
                    chkBtnCCR.setSelection(false);
                    chkBtnDC.setSelection(false);
                    chkBtnAF.setSelection(false);

                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

        chkBtnCE.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent arg0) {
                // TODO Auto-generated method stub
                if (chkBtnCE.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
                    chkBtnCCR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnFR.setEnabled(false);
                } else {
                    chkBtnTB.setEnabled(true);
                    chkBtnPrEP.setEnabled(true);
                    chkBtnPPE.setEnabled(true);
                    chkBtnSAAJ.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnCA.setEnabled(true);
                    chkBtnCCR.setEnabled(true);
                    chkBtnDC.setEnabled(true);
                    chkBtnCPN.setEnabled(true);
                    chkBtnGAAC.setEnabled(true);
                    chkBtnAF.setEnabled(true);
                    chkBtnFR.setEnabled(true);
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
            }
        });

    }

    private void avaliaMDSSelectionActual() {
        if (chkBtnPrEP.getSelection()) {
            chkBtnTB.setEnabled(false);
            chkBtnCCR.setEnabled(false);
            chkBtnPPE.setEnabled(false);
            chkBtnSAAJ.setEnabled(false);
            chkBtnGAAC.setEnabled(false);
            chkBtnAF.setEnabled(false);
            chkBtnCA.setEnabled(false);
//            chkBtnFR.setEnabled(false);
            chkBtnDC.setEnabled(false);
            chkBtnCPN.setEnabled(false);
            chkBtnCE.setEnabled(false);
        } else {
            if (chkBtnTB.getSelection()) {
                chkBtnPrEP.setEnabled(false);
                chkBtnCCR.setEnabled(false);
                chkBtnPPE.setEnabled(false);
                chkBtnSAAJ.setEnabled(false);
                chkBtnGAAC.setEnabled(false);
                chkBtnAF.setEnabled(false);
                chkBtnCA.setEnabled(false);
                chkBtnFR.setEnabled(false);
                chkBtnDC.setEnabled(false);
                chkBtnCPN.setEnabled(false);
                chkBtnCE.setEnabled(false);
            } else {
                if (chkBtnCCR.getSelection()) {
                    chkBtnTB.setEnabled(false);
                    chkBtnPrEP.setEnabled(false);
                    chkBtnPPE.setEnabled(false);
                    chkBtnSAAJ.setEnabled(false);
                    chkBtnGAAC.setEnabled(false);
                    chkBtnAF.setEnabled(false);
                    chkBtnCA.setEnabled(false);
//                    chkBtnFR.setEnabled(false);
                    chkBtnDC.setEnabled(false);
                    chkBtnCPN.setEnabled(false);
                    chkBtnCE.setEnabled(false);
                } else {
                    if (chkBtnPPE.getSelection()) {
                        chkBtnTB.setEnabled(false);
                        chkBtnCCR.setEnabled(false);
                        chkBtnPrEP.setEnabled(false);
                        chkBtnSAAJ.setEnabled(false);
                        chkBtnGAAC.setEnabled(false);
                        chkBtnAF.setEnabled(false);
                        chkBtnCA.setEnabled(false);
                        chkBtnFR.setEnabled(false);
                        chkBtnDC.setEnabled(false);
                        chkBtnCPN.setEnabled(false);
                        chkBtnCE.setEnabled(false);
                    } else {
                        if (chkBtnSAAJ.getSelection()) {
                            chkBtnTB.setEnabled(false);
                            chkBtnCCR.setEnabled(false);
                            chkBtnPPE.setEnabled(false);
                            chkBtnPrEP.setEnabled(false);
                            chkBtnGAAC.setEnabled(false);
                            chkBtnAF.setEnabled(false);
                            chkBtnCA.setEnabled(false);
                            chkBtnFR.setEnabled(false);
                            chkBtnDC.setEnabled(false);
                            chkBtnCPN.setEnabled(false);
                            chkBtnCE.setEnabled(false);
                        } else {
                            if (chkBtnGAAC.getSelection()) {
                                chkBtnTB.setEnabled(false);
                                chkBtnCCR.setEnabled(false);
                                chkBtnPPE.setEnabled(false);
                                chkBtnSAAJ.setEnabled(false);
                                chkBtnPrEP.setEnabled(false);
                                chkBtnAF.setEnabled(false);
                                chkBtnCA.setEnabled(false);
                                //    chkBtnFR.setEnabled(false);
                                chkBtnDC.setEnabled(false);
                                chkBtnCPN.setEnabled(false);
                                chkBtnCE.setEnabled(false);
                            } else {
                                if (chkBtnAF.getSelection()) {
                                    chkBtnTB.setEnabled(false);
                                    chkBtnCCR.setEnabled(false);
                                    chkBtnPPE.setEnabled(false);
                                    chkBtnSAAJ.setEnabled(false);
                                    chkBtnGAAC.setEnabled(false);
                                    chkBtnPrEP.setEnabled(false);
                                    chkBtnCA.setEnabled(false);
                                    //    chkBtnFR.setEnabled(false);
                                    chkBtnDC.setEnabled(false);
                                    chkBtnCPN.setEnabled(false);
                                    chkBtnCE.setEnabled(false);
                                } else {
                                    if (chkBtnCA.getSelection()) {
                                        chkBtnTB.setEnabled(false);
                                        chkBtnCCR.setEnabled(false);
                                        chkBtnPPE.setEnabled(false);
                                        chkBtnSAAJ.setEnabled(false);
                                        chkBtnGAAC.setEnabled(false);
                                        chkBtnAF.setEnabled(false);
                                        chkBtnPrEP.setEnabled(false);
                                        chkBtnFR.setEnabled(false);
                                        chkBtnDC.setEnabled(false);
                                        chkBtnCPN.setEnabled(false);
                                        chkBtnCE.setEnabled(false);
                                    } else {
                                        if (chkBtnFR.getSelection()) {
                                                chkBtnTB.setEnabled(false);
                                                chkBtnCCR.setEnabled(true);
                                                chkBtnPPE.setEnabled(false);
                                                chkBtnSAAJ.setEnabled(false);
                                            chkBtnGAAC.setEnabled(true);
                                            chkBtnAF.setEnabled(true);
                                            chkBtnPrEP.setEnabled(true);
                                               chkBtnCA.setEnabled(false);
                                            chkBtnDC.setEnabled(true);
                                               chkBtnCPN.setEnabled(false);
                                               chkBtnCE.setEnabled(false);
                                        } else {
                                            if (chkBtnCPN.getSelection()) {
                                                chkBtnTB.setEnabled(false);
                                                chkBtnCCR.setEnabled(false);
                                                chkBtnPPE.setEnabled(false);
                                                chkBtnSAAJ.setEnabled(false);
                                                chkBtnGAAC.setEnabled(false);
                                                chkBtnAF.setEnabled(false);
                                                chkBtnPrEP.setEnabled(false);
                                                chkBtnCA.setEnabled(false);
                                                chkBtnDC.setEnabled(false);
                                                chkBtnFR.setEnabled(false);
                                                chkBtnCE.setEnabled(false);
                                            } else {
                                                if (chkBtnDC.getSelection()) {
                                                    chkBtnTB.setEnabled(false);
                                                    chkBtnCCR.setEnabled(false);
                                                    chkBtnPPE.setEnabled(false);
                                                    chkBtnSAAJ.setEnabled(false);
                                                    chkBtnGAAC.setEnabled(false);
                                                    chkBtnAF.setEnabled(false);
                                                    chkBtnPrEP.setEnabled(false);
                                                    chkBtnCA.setEnabled(false);
                                                    chkBtnFR.setEnabled(false);
                                                    chkBtnCPN.setEnabled(false);
                                                    chkBtnCE.setEnabled(false);
                                                } else {
                                                    if (chkBtnCE.getSelection()) {
                                                        chkBtnTB.setEnabled(false);
                                                        chkBtnCCR.setEnabled(false);
                                                        chkBtnPPE.setEnabled(false);
                                                        chkBtnSAAJ.setEnabled(false);
                                                        chkBtnGAAC.setEnabled(false);
                                                        chkBtnAF.setEnabled(false);
                                                        chkBtnPrEP.setEnabled(false);
                                                        chkBtnCA.setEnabled(false);
                                                        chkBtnFR.setEnabled(false);
                                                        chkBtnCPN.setEnabled(false);
                                                        chkBtnCE.setEnabled(false);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
