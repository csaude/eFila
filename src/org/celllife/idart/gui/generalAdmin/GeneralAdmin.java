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
package org.celllife.idart.gui.generalAdmin;

import migracao.swingreverse.MainPanel;
import model.manager.excel.reports.in.PatientSheet;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.SystemFunctionalityManager;
import org.celllife.idart.gui.clinic.AddClinic;
import org.celllife.idart.gui.clinic.DownloadClinic;
import org.celllife.idart.gui.clinicSector.ManagePharmSector;
import org.celllife.idart.gui.clinicSector.UploadClinicSector;
import org.celllife.idart.gui.doctor.AddDoctor;
import org.celllife.idart.gui.drug.AddDrug;
import org.celllife.idart.gui.drug.DownloadDrugs;
import org.celllife.idart.gui.patient.UploadPatient;
import org.celllife.idart.gui.platform.GenericAdminGui;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.regimeTerapeutico.AddRegimeTerapeutico;
import org.celllife.idart.gui.regimeTerapeutico.DownLoadRegimeTerapeutico;
import org.celllife.idart.gui.role.RoleManager;
import org.celllife.idart.gui.stockCenter.StockCenterInfo;
import org.celllife.idart.gui.user.ManagePharmUsers;
import org.celllife.idart.gui.user.UserStatePasswordManage;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.MessageUtil;
import org.celllife.idart.misc.SafeSaveDialog;
import org.celllife.idart.misc.SafeSaveDialog.FileType;
import org.celllife.idart.misc.task.Import;
import org.celllife.idart.misc.task.TaskException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.hibernate.Session;

import javax.help.HelpSet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;

import static org.celllife.idart.rest.ApiAuthRest.getServerStatus;

/**
 *
 */
public class GeneralAdmin extends GenericAdminGui {

    /**
     * Constructor for GeneralAdmin.
     *
     * @param parent Shell
     */
    public GeneralAdmin(Shell parent) {
        super(parent);

    }

    @Override
    protected void createShell() {
        getLog().info(Messages.getString("GeneralAdmin.title.header")); //$NON-NLS-1$
        String shellText = Messages.getString("GeneralAdmin.title.update"); //$NON-NLS-1$
        buildShell(shellText);
    }

    /**
     * This method initializes compHeader
     */
    @Override
    protected void createCompHeader() {
        String headerText = Messages.getString("GeneralAdmin.title.update"); //$NON-NLS-1$
        iDartImage icoImage = iDartImage.GENERALADMIN;
        // Building the component from the GenericAdminGui
        buildCompHeader(headerText, icoImage);
    }

    @Override
    protected void createCompOptions() {
        createMyGroups();
    }

    private void createMyGroups() {

        // create the 4 groups
        createGrpPharmacy();
        createGrpDrug();
        createGrpClinic();
        createGrpDoctor();
        createGrpDrugGroup();
        createGrpRoles();
    }

    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }

    /**
     * This method initializes grpClinics
     */
    private void createGrpClinic() {

        // grpClinics
        Group grpClinics = new Group(getCompOptions(), SWT.NONE);
        grpClinics.setBounds(new Rectangle(50, 165, 305, 150));
        //   grpClinics.setBounds(new Rectangle(495, 13, 305, 150));
        grpClinics.setText(Messages.getString("GeneralAdmin.group.title")); //$NON-NLS-1$
        grpClinics.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));

        // lblPicClinics
        Label lblPicClinics = new Label(grpClinics, SWT.NONE);
        lblPicClinics.setBounds(new org.eclipse.swt.graphics.Rectangle(6, 0, 50, 43));
        lblPicClinics.setText(EMPTY);
        lblPicClinics.setImage(ResourceUtils.getImage(iDartImage.CLINIC));

        if (CentralizationProperties.centralization.equalsIgnoreCase("on")) {
            if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) {
                // btnClinicsAdd
                Button btnClinicsAdd = new Button(grpClinics, SWT.NONE);
                btnClinicsAdd.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 55, 235, 30));
                btnClinicsAdd.setToolTipText(Messages.getString("GeneralAdmin.button.tooltip")); //$NON-NLS-1$
                btnClinicsAdd.setText(Messages.getString("GeneralAdmin.button.title")); //$NON-NLS-1$
                btnClinicsAdd.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                btnClinicsAdd.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmd_clinicsAdd();
                    }
                });

                // btnClinicsUpdate
                Button btnClinicsUpdate = new Button(grpClinics, SWT.NONE);
                btnClinicsUpdate.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 100, 235, 30));
                btnClinicsUpdate.setToolTipText(Messages.getString("GeneralAdmin.clinic.button.tooltip")); //$NON-NLS-1$

                btnClinicsUpdate.setText(Messages.getString("GeneralAdmin.clinic.button.title")); //$NON-NLS-1$
                btnClinicsUpdate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                btnClinicsUpdate.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmd_clinicsUpdate();
                    }
                });
            } else {

                // btnClinicSectorUpload
                Button btnClinicSectorUpload = new Button(grpClinics, SWT.NONE);
                btnClinicSectorUpload.setBounds(new org.eclipse.swt.graphics.Rectangle(60, 20, 210, 25));
                btnClinicSectorUpload.setToolTipText(Messages.getString("GeneralAdmin.upload.clinic.sector.button.tooltip")); //$NON-NLS-1$

                btnClinicSectorUpload.setText(Messages.getString("GeneralAdmin.upload.clinic.sector.button.title")); //$NON-NLS-1$
                btnClinicSectorUpload.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                btnClinicSectorUpload.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmd_clinicSectorExport();
                    }
                });

                // btnPatientUpload
                Button btnPatientUpload = new Button(grpClinics, SWT.NONE);
                btnPatientUpload.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 53, 235, 25));
                btnPatientUpload.setToolTipText(Messages.getString("GeneralAdmin.upload.patient.clinic.sector.button.tooltip")); //$NON-NLS-1$

                btnPatientUpload.setText(Messages.getString("GeneralAdmin.upload.patient.clinic.sector.button.title")); //$NON-NLS-1$
                btnPatientUpload.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                btnPatientUpload.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmd_patientsExport();
                    }
                });

                // btnClinicsDownload
                Button btnClinicsDownload = new Button(grpClinics, SWT.NONE);
                btnClinicsDownload.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 86, 235, 25));
                btnClinicsDownload.setToolTipText(Messages.getString("GeneralAdmin.download.clinic.button.tooltip")); //$NON-NLS-1$

                btnClinicsDownload.setText(Messages.getString("GeneralAdmin.download.clinic.button.title")); //$NON-NLS-1$
                btnClinicsDownload.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                btnClinicsDownload.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmd_clinicsImport();
                    }
                });
                
                  if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
                    Button btnImportPatientsOpenMRS = new Button(grpClinics, SWT.NONE);
                    btnImportPatientsOpenMRS.setText(Messages.getString("GeneralAdmin.button.openmrs.importPatients.title")); //$NON-NLS-1$
                    btnImportPatientsOpenMRS.setToolTipText(Messages.getString("GeneralAdmin.button.openmrs.importPatients.tooltip")); //$NON-NLS-1$
                    btnImportPatientsOpenMRS.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 119, 235, 25));
                    btnImportPatientsOpenMRS.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                    btnImportPatientsOpenMRS.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                        @Override
                        public void widgetSelected(
                                org.eclipse.swt.events.SelectionEvent e) {
                            MainPanel importPatients = new MainPanel();
                            MainPanel.createAndShowGUI();
                        }
                    });
                    btnImportPatientsOpenMRS.setEnabled(true);
                }
            }
        } else {
            Button btnImportPatientsOpenMRS = new Button(grpClinics, SWT.NONE);
            btnImportPatientsOpenMRS.setText(Messages.getString("GeneralAdmin.button.openmrs.importPatients.title")); //$NON-NLS-1$
            btnImportPatientsOpenMRS.setToolTipText(Messages.getString("GeneralAdmin.button.openmrs.importPatients.tooltip")); //$NON-NLS-1$
            btnImportPatientsOpenMRS.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 100, 235, 27));
            btnImportPatientsOpenMRS.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            btnImportPatientsOpenMRS.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @Override
                public void widgetSelected(
                        org.eclipse.swt.events.SelectionEvent e) {
                    MainPanel importPatients = new MainPanel();
                    MainPanel.createAndShowGUI();
                }
            });
            btnImportPatientsOpenMRS.setEnabled(true);
        }
    }

    /**
     * This method initializes grpDoctors
     */
    private void createGrpDoctor() {

        // grpDoctors
        Group grpDoctors = new Group(getCompOptions(), SWT.NONE);
        grpDoctors.setBounds(new Rectangle(495, 329, 305, 150));
        grpDoctors.setText(Messages.getString("GeneralAdmin.doctors.group.title")); //$NON-NLS-1$
        grpDoctors.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));

        // lblPicDoctors
        Label lblPicDoctors = new Label(grpDoctors, SWT.NONE);
        lblPicDoctors.setBounds(new org.eclipse.swt.graphics.Rectangle(6, 0, 50, 43));
        lblPicDoctors.setText(EMPTY);
        lblPicDoctors.setImage(ResourceUtils.getImage(iDartImage.DOCTOR));

        // btnDoctorsAdd
        Button btnDoctorsAdd = new Button(grpDoctors, SWT.NONE);
        btnDoctorsAdd.setText(Messages.getString("GeneralAdmin.doctors.button.title")); //$NON-NLS-1$
        btnDoctorsAdd.setToolTipText(Messages.getString("GeneralAdmin.doctors.button.tooltip")); //$NON-NLS-1$
        btnDoctorsAdd.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 55, 235, 30));
        btnDoctorsAdd.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDoctorsAdd.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_doctorAdd();
            }
        });

        //Desactivar add clinico para user normal
        if (!LocalObjects.getUser(getHSession()).isAdmin()) {
            btnDoctorsAdd.setEnabled(false);
        }

        // btnDoctorsUpdate
        Button btnDoctorsUpdate = new Button(grpDoctors, SWT.NONE);
        btnDoctorsUpdate.setText(Messages.getString("GeneralAdmin.button.docupdate.title")); //$NON-NLS-1$
        btnDoctorsUpdate.setToolTipText(Messages.getString("GeneralAdmin.button.docupdate.tooltip")); //$NON-NLS-1$
        btnDoctorsUpdate.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 100, 235, 30));
        btnDoctorsUpdate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDoctorsUpdate.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_doctorUpdate();
            }
        });
        //Desactivar UPDATE clinico para user normal
        if (!LocalObjects.getUser(getHSession()).isAdmin()) {
            btnDoctorsUpdate.setEnabled(false);
        }
    }

    /**
     * This method initializes grpDrugs
     */
    private void createGrpDrug() {

        boolean checkOpenmrs = true;

        if (CentralizationProperties.centralization.equalsIgnoreCase("off"))
            checkOpenmrs = true;
        else if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("F")
                || CentralizationProperties.pharmacy_type.equalsIgnoreCase("P"))
            checkOpenmrs = false;

        // grpDrugs
        Group grpDrugs = new Group(getCompOptions(), SWT.NONE);
        grpDrugs.setBounds(new Rectangle(50, 329, 305, 150));
        grpDrugs.setText(Messages.getString("GeneralAdmin.group.drug.title")); //$NON-NLS-1$
        grpDrugs.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));

        // lblPicDrugs
        Label lblPicDrugs = new Label(grpDrugs, SWT.NONE);
        lblPicDrugs.setBounds(new org.eclipse.swt.graphics.Rectangle(6, 0, 50, 43));
        lblPicDrugs.setText(EMPTY);
        lblPicDrugs.setImage(ResourceUtils.getImage(iDartImage.PRESCRIPTIONADDDRUG));

        // btnDrugsAdd
        Button btnDrugsAdd = new Button(grpDrugs, SWT.NONE);
        if ((!checkOpenmrs && CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) || CentralizationProperties.centralization.equalsIgnoreCase("off")) {
            btnDrugsAdd.setText(Messages.getString("GeneralAdmin.button.drug.title")); //$NON-NLS-1$
        } else {
            btnDrugsAdd.setText(Messages.getString("GeneralAdmin.button.import.drug.title")); //$NON-NLS-1$
        }
        btnDrugsAdd.setToolTipText(Messages.getString("GeneralAdmin.button.drug.tooltip")); //$NON-NLS-1$

        btnDrugsAdd.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 55, 235, 30));
        btnDrugsAdd.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        final boolean finalCheckOpenmrs = checkOpenmrs;
        btnDrugsAdd.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                if ((!finalCheckOpenmrs && CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) || CentralizationProperties.centralization.equalsIgnoreCase("off"))
                    cmd_drugsAdd();
                else
                    cmd_drugImport();
            }
        });

        //Desactivar a adicao de DRUG
        btnDrugsAdd.setEnabled(true);

        //Desactivar add Medicamento para user normal
        if (!LocalObjects.getUser(getHSession()).isAdmin()) {
            btnDrugsAdd.setEnabled(false);
        }

        // btnDrugsUpdate
        Button btnDrugsUpdate = new Button(grpDrugs, SWT.NONE);
        btnDrugsUpdate.setText(Messages.getString("GeneralAdmin.button.drugupdate.title")); //$NON-NLS-1$
        btnDrugsUpdate.setToolTipText(Messages.getString("GeneralAdmin.button.drugupdate.tooltip")); //$NON-NLS-1$
        btnDrugsUpdate.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 100, 235, 30));
        btnDrugsUpdate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnDrugsUpdate.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_drugsUpdate();
            }
        });

        //Desactivar a actualizar DRUG
        btnDrugsUpdate.setEnabled(true);

        //Desactivar add Medicamento para user normal
        if (!LocalObjects.getUser(getHSession()).isAdmin()) {
            btnDrugsUpdate.setEnabled(false);
        }
    }

    /**
     * This method initializes grpDoctors
     */
    private void createGrpDrugGroup() {

        // grpDrugGroups
        Group grpDrugGroups = new Group(getCompOptions(), SWT.NONE);
        grpDrugGroups.setBounds(new Rectangle(495, 171, 305, 150));
        grpDrugGroups.setText(Messages.getString("GeneralAdmin.group.drugupdate.title")); //$NON-NLS-1$
        grpDrugGroups.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));

        // lblPicDoctors
        Label lblPicDrugGroups = new Label(grpDrugGroups, SWT.NONE);
        lblPicDrugGroups.setBounds(new org.eclipse.swt.graphics.Rectangle(6, 0, 50, 43));
        lblPicDrugGroups.setText(EMPTY);
        lblPicDrugGroups.setImage(ResourceUtils.getImage(iDartImage.DRUGGROUP));

        // btnRegimenAdd
        Button btnRegimenAdd = new Button(grpDrugGroups, SWT.NONE);
        if ((CentralizationProperties.centralization.equalsIgnoreCase("on") && CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) || CentralizationProperties.centralization.equalsIgnoreCase("off"))
            btnRegimenAdd.setText(Messages.getString("GeneralAdmin.button.regimen.title")); //$NON-NLS-1$
        else
            btnRegimenAdd.setText(Messages.getString("GeneralAdmin.button.import.regimen.title")); //$NON-NLS-1$
        btnRegimenAdd.setToolTipText(Messages.getString("GeneralAdmin.button.regimen.tooltip")); //$NON-NLS-1$
        btnRegimenAdd.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 55, 235, 30));
        btnRegimenAdd.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnRegimenAdd.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                if ((CentralizationProperties.centralization.equalsIgnoreCase("on") && CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) || CentralizationProperties.centralization.equalsIgnoreCase("off"))
                    cmd_regimenAdd();
                else
                    cmd_regimenImport();
            }
        });
        btnRegimenAdd.setEnabled(true);
        //Desactivar add Regime Terapeutico para user normal
        if (!LocalObjects.getUser(getHSession()).isAdmin()) {
            btnRegimenAdd.setEnabled(false);
        }

        // btnRegimenUpdate
        Button btnRegimenUpdate = new Button(grpDrugGroups, SWT.NONE);
        btnRegimenUpdate.setText(Messages.getString("GeneralAdmin.button.regimenupdate.title")); //$NON-NLS-1$
        btnRegimenUpdate.setToolTipText(Messages.getString("GeneralAdmin.button.regimenupdate.tooltip")); //$NON-NLS-1$
        btnRegimenUpdate.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 100, 235, 30));
        btnRegimenUpdate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnRegimenUpdate.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_regimenUpdate();
            }
        });

        btnRegimenUpdate.setEnabled(true);

        //Desactivar add Regime Terapeutico para user normal
        if (!LocalObjects.getUser(getHSession()).isAdmin()) {
            btnRegimenAdd.setEnabled(false);
        }
    }

    /**
     * This method initializes grpDoctors
     */
//    private void createGrpImport() {
//
//        Group grpImport = new Group(getCompOptions(), SWT.NONE);
//        grpImport.setBounds(new Rectangle(50, 215, 305, 100));
//        grpImport.setText(Messages.getString("GeneralAdmin.group.import.title")); //$NON-NLS-1$
//        grpImport.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));
//
//        Label lblPicImport = new Label(grpImport, SWT.NONE);
//        lblPicImport.setBounds(new org.eclipse.swt.graphics.Rectangle(6, 0, 50, 43));
//        lblPicImport.setText(EMPTY);
//        lblPicImport.setImage(ResourceUtils.getImage(iDartImage.PATIENTADMIN));
//    }

    /**
     * This method initializes grpRoles
     */
    private void createGrpRoles() {


        Group grpPharmacy = new Group(getCompOptions(), SWT.NONE);
        grpPharmacy.setBounds(new Rectangle(495, 13, 305, 150));
        grpPharmacy.setText(Messages.getString("GeneralAdmin.group.pharmacy.roles")); //$NON-NLS-1$
        grpPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));

        // lblPicPharmacy
        Label lblPicPharmacy = new Label(grpPharmacy, SWT.NONE);
        lblPicPharmacy.setBounds(new org.eclipse.swt.graphics.Rectangle(10, 0, 50, 43));
        lblPicPharmacy.setText(EMPTY);
        lblPicPharmacy.setImage(ResourceUtils.getImage(iDartImage.PHARMACYUSER));

        // btnManagePharmUsers
        Button btnManagePharmUsers = new Button(grpPharmacy, SWT.NONE);
        btnManagePharmUsers.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 100, 235, 30));
        btnManagePharmUsers.setToolTipText(Messages.getString("GeneralAdmin.button.pharmacy.tooltip")); //$NON-NLS-1$
        btnManagePharmUsers.setText("Gerir perfies"); //$NON-NLS-1$
        btnManagePharmUsers.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnManagePharmUsers.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_manageRoles();
            }
        });

        // btnPharmDetailsUpdate
        Button btnPharmDetailsUpdate = new Button(grpPharmacy, SWT.NONE);
        btnPharmDetailsUpdate.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 55, 235, 30));
        btnPharmDetailsUpdate.setToolTipText(Messages.getString("GeneralAdmin.button.pharmdetails.tooltip")); //$NON-NLS-1$
        btnPharmDetailsUpdate.setText("Gerir funcionalidades"); //$NON-NLS-1$
        btnPharmDetailsUpdate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnPharmDetailsUpdate.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_manageSysFunctionalities();
            }
        });


    }

    private void cmd_manageSysFunctionalities() {
        new SystemFunctionalityManager(getShell());
    }

    private void cmd_manageRoles() {
        new RoleManager(getShell());
    }

    /**
     * This method initializes grpPharmacy
     */
    private void createGrpPharmacy() {

        // grpPharmacy
        Group grpPharmacy = new Group(getCompOptions(), SWT.NONE);
        grpPharmacy.setBounds(new Rectangle(50, 13, 305, 150));
        grpPharmacy.setText(Messages.getString("GeneralAdmin.group.pharmacy.title")); //$NON-NLS-1$
        grpPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));

        // lblPicPharmacy
        Label lblPicPharmacy = new Label(grpPharmacy, SWT.NONE);
        lblPicPharmacy.setBounds(new org.eclipse.swt.graphics.Rectangle(10, 0, 50, 43));
        lblPicPharmacy.setText(EMPTY);
        lblPicPharmacy.setImage(ResourceUtils.getImage(iDartImage.PHARMACYUSER));

        if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
            // btnPharmSector
            Button btnPharmSector = new Button(grpPharmacy, SWT.NONE);
            btnPharmSector.setBounds(new org.eclipse.swt.graphics.Rectangle(65, 20, 210, 25));
            btnPharmSector.setToolTipText(Messages.getString("GeneralAdmin.button.pharmsector.tooltip")); //$NON-NLS-1$
            btnPharmSector.setText(Messages.getString("GeneralAdmin.button.pharmsector.title")); //$NON-NLS-1$
            btnPharmSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            btnPharmSector.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @Override
                public void widgetSelected(
                        org.eclipse.swt.events.SelectionEvent e) {
                    cmd_pharmSector();
                }
            });
        }

        // btnUserPasswordStateManage
        Button btnUserPasswordStateManage = new Button(grpPharmacy, SWT.NONE);
        btnUserPasswordStateManage.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 53, 235, 25));
        btnUserPasswordStateManage.setToolTipText(Messages.getString("GeneralAdmin.button.UserPasswordStateManage.tooltip")); //$NON-NLS-1$
        btnUserPasswordStateManage.setText(Messages.getString("GeneralAdmin.button.UserPasswordStateManage.title")); //$NON-NLS-1$
        btnUserPasswordStateManage.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnUserPasswordStateManage.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_passStateReset();
            }
        });

        // btnManagePharmUsers
        Button btnManagePharmUsers = new Button(grpPharmacy, SWT.NONE);
        btnManagePharmUsers.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 86, 235, 25));
        btnManagePharmUsers.setToolTipText(Messages.getString("GeneralAdmin.button.pharmacy.tooltip")); //$NON-NLS-1$
        btnManagePharmUsers.setText(Messages.getString("GeneralAdmin.button.pharmacy.title")); //$NON-NLS-1$
        btnManagePharmUsers.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnManagePharmUsers.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_managePharmUsers();
            }
        });

        // btnPharmDetailsUpdate
        Button btnPharmDetailsUpdate = new Button(grpPharmacy, SWT.NONE);
        btnPharmDetailsUpdate.setBounds(new org.eclipse.swt.graphics.Rectangle(35, 119, 235, 25));
        btnPharmDetailsUpdate.setToolTipText(Messages.getString("GeneralAdmin.button.pharmdetails.tooltip")); //$NON-NLS-1$
        btnPharmDetailsUpdate.setText(Messages.getString("GeneralAdmin.button.pharmdetails.title")); //$NON-NLS-1$
        btnPharmDetailsUpdate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        btnPharmDetailsUpdate.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                cmd_pharmStockCenter();
            }
        });
    }

    public void cmd_clinicsAdd() {
        // AddClinic(true) to ADD new clinic
        AddClinic.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                true);
        new AddClinic(getShell());
    }

    public void cmd_clinicsUpdate() {
        // AddClinic(false) to UPDATE existing clinic
        AddClinic.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                false);
        new AddClinic(getShell());
    }

    public void cmd_clinicsImport() {
        // AddClinic(false) to UPDATE existing clinic
        AddClinic.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                false);
        String url = CentralizationProperties.centralized_server_url;

        try {
            if (getServerStatus(url).contains("Red")) {
                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                b.setText("Servidor Rest offline.");
                b.setMessage(" Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                b.open();
            } else
                new DownloadClinic(getShell());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cmd_clinicSectorExport() {
        String url = CentralizationProperties.centralized_server_url;
        try {
            if (getServerStatus(url).contains("Red")) {
                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                b.setText("Servidor Rest offline.");
                b.setMessage(" Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                b.open();
            } else
                new UploadClinicSector(getShell());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cmd_patientsExport() {
        String url = CentralizationProperties.centralized_server_url;
        try {
            if (getServerStatus(url).contains("Red")) {
                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                b.setText("Servidor Rest offline.");
                b.setMessage(" Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                b.open();
            } else
                new UploadPatient(getShell());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cmd_drugImport() {
        // AddClinic(false) to UPDATE existing clinic
        AddClinic.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                false);
        String url = CentralizationProperties.centralized_server_url;

        try {
            if (getServerStatus(url).contains("Red")) {
                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                b.setText("Servidor Rest offline.");
                b.setMessage(" Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                b.open();
            } else
                new DownloadDrugs(getShell());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cmd_doctorAdd() {
        // AddDoctor(true) to ADD new doctor
        AddDoctor.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                true);
        new AddDoctor(getShell());
    }

    public void cmd_doctorUpdate() {
        // AddDoctor(false) to UPDATE existing doctor
        AddDoctor.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                false);
        new AddDoctor(getShell());
    }

    public void cmd_drugsAdd() {
        // AddDrug(true) to ADD new drug
        AddDrug.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                true);
        new AddDrug(getShell());
    }

    public void cmd_drugsUpdate() {
        // AddDrug(false) to UPDATE existing drug
        AddDrug.addInitialisationOption(GenericFormGui.OPTION_isAddNotUpdate,
                false);
        new AddDrug(getShell());
    }

    public void cmd_managePharmUsers() {
        ManagePharmUsers.addInitialisationOption(
                GenericFormGui.OPTION_isAddNotUpdate, true);
        new ManagePharmUsers(getShell());
    }

    public void cmd_pharmStockCenter() {
        new StockCenterInfo(getShell());
    }

    public void cmd_pharmSector() {
        ManagePharmSector.addInitialisationOption(
                GenericFormGui.OPTION_isAddNotUpdate, true);
        new ManagePharmSector(getShell());
    }

    public void cmd_passStateReset() {
        new UserStatePasswordManage(getShell());
    }

    public void cmd_regimenAdd() {

        AddRegimeTerapeutico.addInitialisationOption(
                GenericFormGui.OPTION_isAddNotUpdate, true);
        new AddRegimeTerapeutico(getShell());
    }

    public void cmd_regimenImport() {

        AddRegimeTerapeutico.addInitialisationOption(
                GenericFormGui.OPTION_isAddNotUpdate, true);
        String url = CentralizationProperties.centralized_server_url;

        try {
            if (getServerStatus(url).contains("Red")) {
                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                b.setText("Servidor Rest offline.");
                b.setMessage(" Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                b.open();
            } else
                new DownLoadRegimeTerapeutico(getShell());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cmd_regimenUpdate() {
        AddRegimeTerapeutico.addInitialisationOption(
                GenericFormGui.OPTION_isAddNotUpdate, false);

        new AddRegimeTerapeutico(getShell());
    }

    protected void cmd_importPatients() {
        FileDialog dlg = new FileDialog(getShell(), SWT.SAVE);
        dlg.setFilterExtensions(FileType.EXCEL.getFilterExtensions());
        dlg.setFilterNames(FileType.EXCEL.getFilterNames());
        String fileName = dlg.open();
        if (fileName == null) {
            return;
        }

        String sheet = "Sheet1";
        InputDialog sheetInput = new InputDialog(getShell(),
                "Nome do sheeet Excel",
                "Por favor inserir o nome do sheet Excel para importação",
                sheet, null);
        if (sheetInput.open() == Window.OK) {
            sheet = sheetInput.getValue();
        } else {
            return;
        }

        final Import task = new Import();
        task.setSheet(sheet);
        task.setFileName(fileName);
        try {
            new ProgressMonitorDialog(null).run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException,
                        InterruptedException {
                    try {
                        task.run(monitor);
                    } catch (TaskException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
            int errorCount = task.getErrorCount();
            if (errorCount > 0) {
                boolean open = MessageDialog.openQuestion(null, "Completado com erros",
                        "A importação foi completado com sucesso mas houve "
                                + +errorCount + " erros. Quer abrir o ficheiro log de erros?");
                if (open) {
                    Program.launch(task.getErrorFile().getAbsolutePath());
                }
            } else {
                MessageDialog
                        .openInformation(null, "Completado", "Importação completeda com successo sem"
                                + " erros");
            }
        } catch (InvocationTargetException e) {
            MessageUtil.showError(e, "Erro ao correr a tarefa",
                    e.getMessage());
        } catch (InterruptedException e) {
            MessageDialog
                    .openInformation(null, "Cancelado", e.getMessage());
        }

    }

    protected void cmd_generateTemplate() {
        SafeSaveDialog dialog = new SafeSaveDialog(getShell(), FileType.EXCEL);
        String fileName = dialog.open();
        if (fileName == null) {
            return;
        }
        PatientSheet sheet = new PatientSheet("Sheet1");
        Session session = HibernateUtil.getNewSession();
        sheet.setSession(session);
        sheet.init();
        sheet.writeTemplateSheet(fileName);
        session.close();
        Program.launch(fileName);
    }

    @Override
    protected void cmdCloseSelectedWidget() {
        cmdCloseSelected();
    }


    //sync sesp patients
    protected void cmd_importPatientsSESP() {

        //ConexaoODBC odbc=new ConexaoODBC();
        Connection c = null;

        ConexaoJDBC jdbc = new ConexaoJDBC();
        jdbc.delete_sync_temp_patients();
        jdbc.insere_sync_temp_patients();
        try {

            //c=odbc.getConnection();
        } catch (Exception e) {

            e.printStackTrace();
        }
        ConexaoJDBC conn = new ConexaoJDBC();
        //se não houver conexao
        MessageBox conexaoACCESS = new MessageBox(new Shell(), SWT.ICON_ERROR
                | SWT.OK);
        conexaoACCESS.setText("Conexão com Base de Dados SESP");
        conexaoACCESS
                .setMessage("O iDART não está a se conectar com o SESP.\n Por favor verifique se os cabos da rede estão ligados no seu \ncomputador ou se o computador com SESP esta ligado!\n Saia do iDART e verifique estes apectos depois volte a entrar,\n se o problema persistir, não será possivel importar os pacientes do SESP");
        conexaoACCESS.open();

    }
}
