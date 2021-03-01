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

package org.celllife.idart.gui.drug;

import model.manager.AdministrationManager;
import model.manager.DrugManager;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.AtcCode;
import org.celllife.idart.database.hibernate.ChemicalCompound;
import org.celllife.idart.database.hibernate.Drug;
import org.celllife.idart.database.hibernate.Form;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.search.Search;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import java.util.Set;

/**
 *
 */
public class AddDrug extends GenericFormGui {

    private static final String ID = "id";

    private Button btnSearch;

    private Button rdBtnActive;

    private Button rdBtnInactive;

    private Text txtName;

    private Text txtPacksize;

    private Text txtDispensingInstructions1;

    private Text txtDispensingInstructions2;

    boolean isAddnotUpdate;

    private Drug localDrug;

    private Text txtAmountPerTime;

    private Text txtTimesPerDay;

    private Text txtAtc;

    private Combo cmbForm;

    private Composite compInstructions;

    private Label lblTablets;

    private Label lblTake;

    private Label lblPackDescription;

    private Group grpDrugInfo;

    private Label lblEstadoDrug;

    private CCombo cmbtipoDoenca;

    /**
     * Use true if you want to add a new drug, use false if you are updating an
     * existing drug
     *
     * @param parent Shell
     */
    public AddDrug(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
    }

    /**
     * This method initializes newDrug
     */
    @Override
    protected void createShell() {
        isAddnotUpdate = (Boolean) getInitialisationOption(OPTION_isAddNotUpdate);
        // The GenericFormsGui class needs
        // Header text, icon URL, shell bounds
        String shellTxt = isAddnotUpdate ? "Registar Novo Medicamento"
                : "Actualizar um Medicamento";
        Rectangle bounds = new Rectangle(25, 0, 800, 600);
        // Parent Generic Methods ------
        buildShell(shellTxt, bounds); // generic shell build
    }

    @Override
    protected void createContents() {
        createCompDrugInfo();
        createGrpStandardDosages();
        createCompInstructions();
//        createGrpChemicalCompounds();

        if (isAddnotUpdate) {
            enableFields(true);
            txtName.setFocus();
        } else {
            enableFields(false);
            btnSearch.setFocus();
        }
    }

    /**
     * This method initializes compHeader
     */
    @Override
    protected void createCompHeader() {
        String headerTxt = (isAddnotUpdate ? "Registar novo Medicamento"
                : "Actualizar ARV existente");
        iDartImage icoImage = iDartImage.PRESCRIPTIONADDDRUG;
        buildCompHeader(headerTxt, icoImage);
    }

    @Override
    protected void createCompButtons() {
        // Parent Class generic call
        buildCompButtons();
    }

    /**
     * This method initializes compDrugInfo
     */
    private void createCompDrugInfo() {

        // compDrugInfo
        grpDrugInfo = new Group(getShell(), SWT.NONE);
        grpDrugInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpDrugInfo.setText("Detalhes do Medicamento");
        grpDrugInfo.setBounds(new Rectangle(150, 115, 485, 330));
        //16, 423, 485, 61));
        GridLayout layout = new GridLayout(3, false);
        layout.verticalSpacing = 10;
        grpDrugInfo.setLayout(layout);

        Label lblDrugSearch = new Label(grpDrugInfo, SWT.NONE);
        lblDrugSearch.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));

        if (isAddnotUpdate) {
            lblDrugSearch.setText("");
        } else {
            lblDrugSearch.setText("*Procurar ARV para Actualizar:");
        }
        lblDrugSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // btnSearch
        btnSearch = new Button(grpDrugInfo, SWT.NONE);
        btnSearch.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        btnSearch.setText("Procurar Medicamento");
        btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnSearch.setVisible(!isAddnotUpdate);
        btnSearch
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdSearchWidgetSelected();
                    }
                });
        btnSearch
                .setToolTipText("Pressione este botão para procurar um ARV existente.");

        // lblName & txtName
        Label lblName = new Label(grpDrugInfo, SWT.NONE);
        lblName.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblName.setText("* Nome:");
        lblName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtName = new Text(grpDrugInfo, SWT.BORDER);
        txtName.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        txtName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblNSN & txtNSN
        Label lblatc = new Label(grpDrugInfo, SWT.NONE);
        lblatc.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblatc.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblatc.setText("* Código FNM:");


        txtAtc = new Text(grpDrugInfo, SWT.BORDER);
        txtAtc.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
        txtAtc.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        txtAtc.setEditable(true);

        // lblFormLanguage1 & txtFormLanguage1
        Label lblFormLanguage1 = new Label(grpDrugInfo, SWT.NONE);
        lblFormLanguage1.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblFormLanguage1.setText("* Forma Farmacéutica:");
        lblFormLanguage1.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbForm = new Combo(grpDrugInfo, SWT.BORDER);
        cmbForm.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 2, 1));
        CommonObjects.populateForms(getHSession(), cmbForm);
        cmbForm.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbForm.setText("");
        cmbForm.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbForm.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        cmbForm.setVisibleItemCount(cmbForm.getItemCount());

        cmbForm.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                String theFormStr = cmbForm.getText();
                if (!"".equalsIgnoreCase(theFormStr)) {
                    populateGrpStandardDosages(theFormStr);
                    Form theForm = AdministrationManager.getForm(getHSession(),
                            theFormStr);
                    lblPackDescription.setText(theForm.getFormLanguage1()
                            .equals("drops") ? "ml" : theForm
                            .getFormLanguage1());

                    txtDispensingInstructions1.setText("");
                    txtDispensingInstructions2.setText("");
                }

            }
        });
        cmbForm.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String theText = cmbForm.getText();
                if (theText.length() > 2) {
                    String s = theText.substring(0, 1);
                    String t = theText.substring(1, theText.length());
                    theText = s.toUpperCase() + t;
                    String[] items = cmbForm.getItems();
                    for (int i = 0; i < items.length; i++) {
                        if (items[i].length() > 2
                                && items[i].substring(0, 3).equalsIgnoreCase(
                                theText)) {
                            cmbForm.setText(items[i]);
                            cmbForm.setFocus();
                        }
                    }
                }
            }
        });

        // lblPacksize & txtPacksize
        Label lblPacksize = new Label(grpDrugInfo, SWT.NONE);
        lblPacksize.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblPacksize.setText("* Quantidade no Frasco:");
        lblPacksize.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        txtPacksize = new Text(grpDrugInfo, SWT.BORDER);
        txtPacksize.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        txtPacksize.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        lblPackDescription = new Label(grpDrugInfo, SWT.NONE);
        lblPackDescription.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblPackDescription.setText("");
        lblPackDescription.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblDispensingInstructions1 & txtDispenseInstr
        Label lblDispensingInstructions1 = new Label(grpDrugInfo, SWT.NONE);
        lblDispensingInstructions1.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblDispensingInstructions1
                .setText("  Instrução de toma(linha 1):");
        lblDispensingInstructions1.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));
        lblDispensingInstructions1
                .setToolTipText("Isto aparece na etiqueta do medicamento");

        txtDispensingInstructions1 = new Text(grpDrugInfo, SWT.BORDER);
        txtDispensingInstructions1.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1));
        txtDispensingInstructions1.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));

        // lblDispensingInstructions2 & txtDispensingInstructions2
        Label lblDispensingInstructions2 = new Label(grpDrugInfo, SWT.NONE);
        lblDispensingInstructions2.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblDispensingInstructions2
                .setText("  Instrução de toma(linha 2):");
        lblDispensingInstructions2.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8));
        lblDispensingInstructions2
                .setToolTipText("Isto aparece na etiqueta do medicamento");

        txtDispensingInstructions2 = new Text(grpDrugInfo, SWT.BORDER);
        txtDispensingInstructions2.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1));
        txtDispensingInstructions2.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // Desease Type
        Label lblSideTreatment = new Label(grpDrugInfo, SWT.NONE);
        lblSideTreatment.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        lblSideTreatment.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblSideTreatment.setText("* O medicamento é para?:");

        cmbtipoDoenca = new CCombo(grpDrugInfo, SWT.NONE);
        cmbtipoDoenca.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 2, 1));
        cmbtipoDoenca.setEditable(false);
        cmbtipoDoenca.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbtipoDoenca.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbtipoDoenca.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        CommonObjects.populateDiseases(getHSession(), cmbtipoDoenca);
        cmbtipoDoenca.setText(cmbtipoDoenca.getItem(0));

        // Estado do Regime Terapeutico
        lblEstadoDrug = new Label(grpDrugInfo, SWT.NONE);
        lblEstadoDrug.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
//        lblEstadoDrug.setBounds(new org.eclipse.swt.graphics.Rectangle(30, 70, 110, 20));
        lblEstadoDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblEstadoDrug.setText(Messages.getString("adddruggroup.estado.medicamento.title")); //$NON-NLS-1$

        rdBtnActive = new Button(grpDrugInfo, SWT.RADIO);
        rdBtnActive.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1));
        rdBtnActive.setLayoutData(new GridData(110, 20));
        rdBtnActive.setText("Activo");
        rdBtnActive.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnActive.setSelection(true);

        rdBtnInactive = new Button(grpDrugInfo, SWT.RADIO);
        rdBtnInactive.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
        rdBtnInactive.setLayoutData(new GridData(150, 20));
        rdBtnInactive.setText("Inactivo");
        rdBtnInactive.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnInactive.setSelection(false);

        grpDrugInfo.layout();
    }

    protected void cmdAtcSearchWidgetSelected() {
        Search atcSearch = new Search(getHSession(), getShell(),
                CommonObjects.ATC);

        if (atcSearch.getValueSelected() != null) {

            AtcCode atc = AdministrationManager.getAtccodeFromCode(getHSession(), atcSearch
                    .getValueSelected()[1]);

            if (atc == null) {
                return;
            }

            Set<ChemicalCompound> ccs = atc.getChemicalCompounds();
            String name = atc.getName();
            if (ccs != null && ccs.size() == 1) {
                String acronym = ccs.iterator().next().getAcronym();
                if (acronym != null && !acronym.isEmpty()) {
                    name = "[" + acronym + "] " + name;
                }
            }
            txtName.setText(name);
//			txtAtc.setText(atc.getCode());
            String mims = atc.getMims();
            if (mims != null)
                //txtMims.setText(mims);

                if (ccs == null || ccs.isEmpty()) {
                    return;
                }
        }

    }

    /**
     * This method initializes grpStandadDosages
     */
    private void createGrpStandardDosages() {

        // grpStandadDosages
        Group grpStandadDosages = new Group(getShell(), SWT.NONE);
        Rectangle bounds = new Rectangle(150, 450, 485, 61);
        grpStandadDosages.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpStandadDosages.setText("Posologia ");
        grpStandadDosages.setLayout(null);
//        grpStandadDosages.setBounds(new Rectangle(16, 450, 685, 61));
        grpStandadDosages.setBounds(bounds);

        // lblTake
        lblTake = new Label(grpStandadDosages, SWT.CENTER);
        lblTake.setBounds(new Rectangle(27, 28, 107, 22));
        lblTake.setText("Tomar");
        lblTake.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // txtAmountPerTime
        txtAmountPerTime = new Text(grpStandadDosages, SWT.BORDER);
        txtAmountPerTime.setBounds(new Rectangle(137, 26, 40, 22));
        txtAmountPerTime.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblTablets
        lblTablets = new Label(grpStandadDosages, SWT.CENTER);
        lblTablets.setBounds(new Rectangle(180, 28, 76, 22));
        lblTablets.setText("Comprimido(s)");
        lblTablets.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // txtTimesPerDay
        txtTimesPerDay = new Text(grpStandadDosages, SWT.BORDER);
        txtTimesPerDay.setBounds(new Rectangle(260, 26, 40, 22));
        txtTimesPerDay.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        // lblTimesPerDay
        Label lblTimesPerDay = new Label(grpStandadDosages, SWT.CENTER);
        lblTimesPerDay.setBounds(new Rectangle(298, 28, 126, 22));
        lblTimesPerDay.setText("vezes por dia");
        lblTimesPerDay.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
    }

    /**
     * This method initializes compButtons
     */

    @Override
    protected void cmdSaveWidgetSelected() {

        if (fieldsOk()) {
            MessageBox mSave = new MessageBox(getShell(), SWT.ICON_QUESTION
                    | SWT.YES | SWT.NO);
            mSave.setText(isAddnotUpdate ? "Registar novo medicamento" : "Actualizar Detalhes");
            mSave
                    .setMessage(isAddnotUpdate ? "Quer mesmo registar este medicamento na base de dados?"
                            : "Quer salvar as mudanças efectuadas sobres este medicamento?");

            switch (mSave.open()) {

                case SWT.YES:

                    Transaction tx = null;
                    String action = "";
                    try {
                        tx = getHSession().beginTransaction();
                        if (isAddnotUpdate) {
                            localDrug = new Drug();
                            setLocalDrug();
                            DrugManager.saveDrug(getHSession(), localDrug);
                            action = "added";
                        } else {
                            setLocalDrug();
                            action = "updated";
                        }

                        tx.commit();
                        getHSession().flush();

                        // Updating the drug list after being saved.
                        MessageBox m = new MessageBox(getShell(),
                                SWT.ICON_INFORMATION | SWT.OK);
                        m.setMessage("Medicamento '".concat(localDrug.getName()).concat(
                                "' has been " + action + "."));
                        m.setText("Base de Dados actualizado");
                        m.open();

                    } catch (HibernateException he) {
                        MessageBox m = new MessageBox(getShell(), SWT.OK
                                | SWT.ICON_INFORMATION);
                        m.setText("Problemas ao salvar na Base de Dados");
                        m
                                .setMessage("Houve problemas ao salvar informação do medicamento na base de dados. Tente de novo.");
                        m.open();
                        if (tx != null) {
                            tx.rollback();
                        }
                        getLog().error(he);
                    }
                    cmdCancelWidgetSelected(); // go back to previous screen
                    break;
                case SWT.NO:
                    // do nothing
            }

        }
    }

    /**
     * Clears the form
     */
    @Override
    public void clearForm() {

        try {

            txtName.setText("");
            cmbForm.setText("");
            txtPacksize.setText("");
            lblPackDescription.setText("");
            txtDispensingInstructions1.setText("");
            txtDispensingInstructions2.setText("");
            btnSearch.setEnabled(true);
            txtAtc.setText("");
//            chkBtnPediatric.setSelection(false);
//            chkBtnAdult.setSelection(false);
            //txtMims.setText("");
            txtTimesPerDay.setText("");
            txtAmountPerTime.setText("");
            rdBtnActive.setSelection(true);
            rdBtnInactive.setSelection(false);
            localDrug = null;
            enableFields(isAddnotUpdate);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void cmdCancelWidgetSelected() {
        cmdCloseSelected();
    }

//    private void cmdAddChemicalWidgetSelected(final ChemicalCompound cc) {
//
//        final AddChemicalCompound ac = new AddChemicalCompound(getShell(), getHSession(), cc);
//        ac.getShell().addDisposeListener(new DisposeListener() {
//            @Override
//            public void widgetDisposed(DisposeEvent e) {
//
//                if (AddChemicalCompound.compoundAdded != null) {
//                    ChemicalCompound ncc = DrugManager
//                            .getChemicalCompoundByName(getHSession(),
//                                    AddChemicalCompound.compoundAdded.getName());
//
//                    if (ncc != null) {
//                        if (cc == null) {
//                            // new cc
//                            TableItem ti = new TableItem(tblChemicalCompounds,
//                                    SWT.NONE);
//
//                            populateTableItem(ncc, ti);
//                        } else {
//                            // edit cc
//                            TableItem[] items = tblChemicalCompounds.getItems();
//                            for (TableItem ti : items) {
//                                if (ti.getData(ID).equals(ncc.getId())) {
//                                    // populate table
//                                    populateTableItem(ncc, ti);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        });
//    }

    private void cmdSearchWidgetSelected() {

        Search drugSearch = new Search(getHSession(), getShell(),
                CommonObjects.DRUG);

        if (drugSearch.getValueSelected() != null) {

            localDrug = DrugManager.getDrug(getHSession(), drugSearch
                    .getValueSelected()[0]);
            loadDrugDetails();
            btnSearch.setEnabled(false);

            enableFields(true);
            txtName.setFocus();

        } else {
            btnSearch.setEnabled(true);
        }

    }

    @Override
    protected void cmdClearWidgetSelected() {
        clearForm();
    }

    private void loadDrugDetails() {

        txtName.setText(localDrug.getName());
        txtAtc.setText(localDrug.getAtccode());
        cmbForm.setText(localDrug.getForm().getForm());
        txtPacksize.setText(String.valueOf(localDrug.getPackSize()));

        if (localDrug.isActive()) {
            rdBtnActive.setSelection(true);
            rdBtnInactive.setSelection(false);
        } else {
            rdBtnActive.setSelection(false);
            rdBtnInactive.setSelection(true);
        }

        Form theForm = localDrug.getForm();
        lblPackDescription
                .setText(theForm.getFormLanguage1().equals("drops") ? "ml"
                        : theForm.getFormLanguage1());
        txtDispensingInstructions1.setText(localDrug
                .getDispensingInstructions1());
        txtDispensingInstructions2.setText(localDrug
                .getDispensingInstructions2());

        populateGrpStandardDosages(cmbForm.getText());

        Object amntpertime = iDARTUtil.isInteger(""
                + localDrug.getDefaultAmnt());
        String tmp = (amntpertime == null) ? "" + localDrug.getDefaultAmnt()
                : "" + amntpertime.toString();
        txtAmountPerTime.setText(tmp);

        txtTimesPerDay.setText(String.valueOf(localDrug.getDefaultTimes()));

        btnSearch.setEnabled(false);
        cmbtipoDoenca.setText(localDrug.getTipoDoenca());

        grpDrugInfo.layout();

    }

    /**
     * Check if the necessary field names are filled in. Returns true if there
     * are fields missing
     *
     * @return boolean
     */
    @Override
    protected boolean fieldsOk() {

        // checking all simple fields

        if (txtName.getText().equals("")) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("Nome do medicamento não pode ser vazio");
            b.setText("informação em Falta");
            b.open();
            txtName.setFocus();
            return false;
        }

        if (DrugManager.drugNameExists(getHSession(), txtName.getText())
                && isAddnotUpdate) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b
                    .setMessage("Nome do Medicameento já existe. Inserir um nome diferente");
            b.setText("Nome do medicamento suplicado");
            b.open();
            txtName.setFocus();
            return false;
        }

        if (cmbForm.indexOf(cmbForm.getText()) == -1) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b
                    .setMessage("A forma do medicamento deve ser da lista.");
            b.setText("Informação incorrecta");
            b.open();
            cmbForm.setFocus();
            return false;
        }

        if (txtPacksize.getText().equals("")) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("Inserir quantidade no frasco");
            b.setText("Informação em Falta");
            b.open();

            return false;
        }

        try {
            Integer.parseInt(txtPacksize.getText());
        } catch (NumberFormatException nfe) {
            MessageBox incorrectData = new MessageBox(getShell(),
                    SWT.ICON_ERROR | SWT.OK);
            incorrectData.setText("Quantidade no Frasco incorrecto");
            incorrectData
                    .setMessage("A Quantidade no Frasco que inseriu é inválido. Inserir número.");
            incorrectData.open();
            txtPacksize.setFocus();
            return false;
        }

        if (txtAmountPerTime.isVisible()
                & (!txtAmountPerTime.getText().trim().equals(""))) {

            try {
                Double.parseDouble(txtAmountPerTime.getText().trim());
            } catch (NumberFormatException nfe) {
                MessageBox incorrectData = new MessageBox(getShell(),
                        SWT.ICON_ERROR | SWT.OK);
                incorrectData.setText("Valor do padrão da posologia incorrecto");
                incorrectData
                        .setMessage("A posologia padrão que inseriu é incorrecto. Inserir número.");
                incorrectData.open();
                txtAmountPerTime.setFocus();
                return false;
            }

        }

        if (txtAtc.getText().trim().isEmpty()) {
            showMessage(MessageDialog.ERROR, "Código FNM vazio", "O código FNM seleccionou não está na base de dados.");
            txtAtc.setFocus();

            return false;
        }

        if (cmbtipoDoenca.getText().trim().isEmpty()) {
            showMessage(MessageDialog.ERROR, "Tipo de Medicamento vazio", "Não foi indicado para que tipo de doença é o Medicamento.");
            cmbtipoDoenca.setFocus();
            return false;
        }

        if (!txtTimesPerDay.getText().trim().equals("")) {
            try {
                Integer.parseInt(txtTimesPerDay.getText());
            } catch (NumberFormatException nfe) {
                MessageBox incorrectData = new MessageBox(getShell(),
                        SWT.ICON_ERROR | SWT.OK);
                incorrectData.setText("o valor do padrão da posologia incorrecto");
                incorrectData
                        .setMessage("A posologia padrão que inseriu é incorrecto. Inserir número.");
                incorrectData.open();
                txtTimesPerDay.setFocus();
                return false;
            }
        }

        return true;
    }

    private void setLocalDrug() {

        try {

            localDrug.setName(txtName.getText());
            localDrug.setAtccode(txtAtc.getText());
            localDrug.setPackSize(Integer.parseInt(txtPacksize.getText()));
            localDrug.setDispensingInstructions1(txtDispensingInstructions1.getText());
            localDrug.setDispensingInstructions2(txtDispensingInstructions2.getText());
            localDrug.setModified('T');
            localDrug.setForm(AdministrationManager.getForm(getHSession(), cmbForm.getText()));
            localDrug.setTipoDoenca(cmbtipoDoenca.getText());
            if (rdBtnActive.getSelection())
                localDrug.setActive(true);
            else
                localDrug.setActive(false);
            double amnt = 0;
            int times = 0;

            if (!txtAmountPerTime.getText().equals("")) {
                amnt = Double.valueOf(txtAmountPerTime.getText());

            }

            if (!txtTimesPerDay.getText().equals("")) {
                times = Integer.parseInt(txtTimesPerDay.getText());
            }

            localDrug.setDefaultAmnt(amnt);
            localDrug.setDefaultTimes(times);

            if (cmbtipoDoenca.getText().contains("ARV")) {
                localDrug.setSideTreatment('T');
            } else {
                localDrug.setSideTreatment('F');
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Method enableFields.
     *
     * @param enable boolean
     */
    @Override
    protected void enableFields(boolean enable) {

        txtName.setEnabled(enable);
        cmbForm.setEnabled(enable);
        txtPacksize.setEnabled(enable);
        rdBtnInactive.setEnabled(enable);
        rdBtnActive.setEnabled(enable);
        txtDispensingInstructions1.setEnabled(enable);
        txtDispensingInstructions2.setEnabled(enable);
        cmbtipoDoenca.setEnabled(enable);
        txtAmountPerTime.setEnabled(enable);
        txtTimesPerDay.setEnabled(enable);
        txtAtc.setEnabled(enable);
        btnSave.setEnabled(enable);

        if (enable) {
            cmbForm.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        } else {
            cmbForm.setBackground(ResourceUtils
                    .getColor(iDartColor.WIDGET_BACKGROUND));
        }

    }

    /**
     * Method populateGrpStandardDosages.
     *
     * @param theFormString String
     */
    public void populateGrpStandardDosages(String theFormString) {
        Form form = AdministrationManager.getForm(getHSession(), theFormString);
        lblTake.setText(form.getActionLanguage1());
        lblTablets.setText(form.getFormLanguage1());

        if (lblTablets.getText().equals("")) {
            txtAmountPerTime.setVisible(false);
        } else {
            txtAmountPerTime.setVisible(true);
        }

        if ((form.getDispInstructions1() != null)
                && (!form.getDispInstructions1().equals(""))) {
            txtDispensingInstructions1.setText(form.getDispInstructions1());
        }
        if ((form.getDispInstructions2() != null)
                && (!form.getDispInstructions2().equals(""))) {
            txtDispensingInstructions2.setText(form.getDispInstructions2());
        }
    }

    /**
     * This method initializes compInstructions
     */
    private void createCompInstructions() {
        compInstructions = new Composite(getShell(), SWT.NONE);
        compInstructions.setLayout(null);
        compInstructions.setBounds(new Rectangle(270, 79, 360, 25));

        Label lblInstructions = new Label(compInstructions, SWT.CENTER);
        lblInstructions.setBounds(new Rectangle(0, 0, 360, 25));
        lblInstructions.setText("Todos campos marcados com * são obrigatórios");
        lblInstructions.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_10_ITALIC));
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

    @Override
    protected void setLogger() {
        Logger log = Logger.getLogger(this.getClass());
        setLog(log);
    }

}
