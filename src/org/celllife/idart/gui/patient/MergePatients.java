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

package org.celllife.idart.gui.patient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import model.manager.PackageManager;
import model.manager.PatientManager;
import model.manager.TemporaryRecordsManager;
import model.manager.reports.PatientHistoryReport;

import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.AlternatePatientIdentifier;
import org.celllife.idart.database.hibernate.Appointment;
import org.celllife.idart.database.hibernate.Packages;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.PatientIdentifier;
import org.celllife.idart.database.hibernate.Pregnancy;
import org.celllife.idart.database.hibernate.Prescription;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.reportParameters.PatientHistory;
import org.celllife.idart.gui.search.PatientSearch;
import org.celllife.idart.gui.utils.ComboUtils;
import org.celllife.idart.gui.utils.LayoutUtils;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.PatientBarcodeParser;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

/**
 */
/**
 * @author melissa
 *
 */
public class MergePatients extends GenericFormGui {

	private Composite compLeftPatient;

	private Composite compRightPatient;

	private Composite compMergePatient;

	// LEFT PATIENT
	private Button btnPatientSearchLeft;

	private Label lblPatientIdLeft;

	private Text txtPatientIdLeft;

	private Label lblPrimaryPatient;

	private Button btnPatientHistoryReportLeft;

	private Label lblFirstnamesLeft;

	private Label lblSurnameLeft;

	private Label lblDateOfBirthLeft;

	private Label lblAgeLeft;

	private Label lblSexLeft;

	private Label lblAddressLeft;

	private Text txtFirstnamesLeft;

	private Text txtSurnameLeft;

	private Text txtDateOfBirthLeft;

	private Text txtAgeLeft;

	private Text txtSexLeft;

	private Text txtAddressLeft;

	private Table tblPatientHistoryLeft;

	Patient leftPatient;

	// RIGHT PATIENT
	private Button btnPatientSearchRight;

	private Label lblPatientIdRight;

	private Text txtPatientIdRight;

	private Label lblSecondaryPatient;

	private Label lblFirstnamesRight;

	private Label lblSurnameRight;

	private Button btnPatientHistoryReportRight;

	private Label lblDateOfBirthRight;

	private Label lblAgeRight;

	private Label lblSexRight;

	private Label lblAddressRight;

	private Text txtFirstnamesRight;

	private Text txtSurnameRight;

	private Text txtDateOfBirthRight;

	private Text txtAgeRight;

	private Text txtSexRight;

	private Text txtAddressRight;

	private Table tblPatientHistoryRight;

	Patient rightPatient;

	// MERGE Patient

	private Label lblMergedPatient;

	private Label lblPatientIdMerge;

	private Text txtPatientIdMerge;

	private Label lblFirstnamesMerge;

	private Label lblSurnameMerge;

	private Label lblDateOfBirthMerge;

	private Label lblSexMerge;

	private Label lblAgeMerge;

	private Label lblAddress1Merge;

	private Label lblAddress2Merge;

	private Label lblAddress3Merge;

	private Text txtFirstnamesMerge;

	private Text txtSurnameMerge;

	private Combo cmbDOBDay;

	private Combo cmbDOBMonth;

	private Combo cmbDOBYear;

	private Combo cmbSex;

	private Text txtAgeMerge;

	private Text txtAddress1Merge;

	private Text txtAddress2Merge;

	private Text txtAddress3Merge;

	private Label lblInstructionsMerge;

	private Composite compAppointment;

	private Label lblAppointment;

	private Button rdBtnAppointmentFromLeft;

	private Button rdBtnAppointmentFromRight;

	private Label lblWarning;

	private Locale localeEn = new Locale("en", "US");

	/**
	 */
	private enum PatientSide {
		LEFT, RIGHT
	}

	boolean patientHistoryLoadingFinished = true;

	/**
	 * Constructor for MergePatients.
	 *
	 * @param parent
	 *            Shell
	 */
	public MergePatients(Shell parent) {
		super(parent, HibernateUtil.getNewSession());
	}

	/**
	 * This method initializes mergePatients
	 */
	@Override
	protected void createShell() {
		String shellTxt = Messages.getString("MergePatients.title"); //$NON-NLS-1$
		Rectangle bounds = new Rectangle(25, 0, 900, 700);
		buildShell(shellTxt, bounds);
	}

	@Override
	protected void createContents() {
		createCompLeftPatient();
		createCompRightPatient();
		createCompMergePatient();
	}

	/**
	 * This method initializes compHeader
	 *
	 */
	@Override
	protected void createCompHeader() {
		String headerTxt = "União de Pacientes";
		iDartImage icoImage = iDartImage.PATIENTDUPLICATES;
		buildCompHeader(headerTxt, icoImage);
	}

	/**
	 * This method initializes compButtons
	 *
	 */
	@Override
	protected void createCompButtons() {
		buildCompButtons();
	}

	public void createCompLeftPatient() {
		compLeftPatient = new Composite(getShell(), SWT.BORDER);
		compLeftPatient.setBounds(new Rectangle(50, 70, 375, 260));

		lblPrimaryPatient = new Label(compLeftPatient, SWT.LEFT);
		lblPrimaryPatient.setBounds(new Rectangle(5, 2, 250, 20));
		lblPrimaryPatient.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_12_BOLDITALIC));
		lblPrimaryPatient.setText("Paciente Preferido");

		lblPatientIdLeft = new Label(compLeftPatient, SWT.LEFT);
		lblPatientIdLeft.setBounds(new Rectangle(5, 25, 84, 20));
		lblPatientIdLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblPatientIdLeft.setText(Messages.getString("patient.label.patientid")); //$NON-NLS-1$

		txtPatientIdLeft = new Text(compLeftPatient, SWT.BORDER);
		txtPatientIdLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				25, 150, 20));
		txtPatientIdLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtPatientIdLeft.setEditable(true);
		txtPatientIdLeft.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				if ((e.character == SWT.CR)
						|| (e.character == (char) iDartProperties.intValueOfAlternativeBarcodeEndChar)) {
					cmdSearchWidgetSelected(PatientSide.LEFT);
				}

			}
		});

		txtPatientIdLeft.setFocus();

		btnPatientSearchLeft = new Button(compLeftPatient, SWT.CENTER);
		btnPatientSearchLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(
				260, 23, 110, 25));
		btnPatientSearchLeft.setText("Procurar");
		btnPatientSearchLeft.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		btnPatientSearchLeft
				.setToolTipText("Clica no botão para seleccionar o paciente preferido para a união.");
		btnPatientSearchLeft
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {

					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						cmdSearchWidgetSelected(PatientSide.LEFT);
					}
				});

		lblFirstnamesLeft = new Label(compLeftPatient, SWT.NONE);
		lblFirstnamesLeft.setBounds(new Rectangle(5, 50, 84, 20));
		lblFirstnamesLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblFirstnamesLeft.setText("Nome:");

		txtFirstnamesLeft = new Text(compLeftPatient, SWT.BORDER);
		txtFirstnamesLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				50, 220, 20));
		txtFirstnamesLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtFirstnamesLeft.setEditable(false);

		lblSurnameLeft = new Label(compLeftPatient, SWT.NONE);
		lblSurnameLeft.setBounds(new Rectangle(5, 75, 84, 20));
		lblSurnameLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblSurnameLeft.setText("Apelido:");
		txtSurnameLeft = new Text(compLeftPatient, SWT.BORDER);
		txtSurnameLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				75, 220, 20));
		txtSurnameLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtSurnameLeft.setEditable(false);

		btnPatientHistoryReportLeft = new Button(compLeftPatient, SWT.NONE);
		btnPatientHistoryReportLeft.setBounds(new Rectangle(325, 50, 40, 40));
		btnPatientHistoryReportLeft
				.setToolTipText("Clica no botão para visualizar ou imprimir relatorios sobre a historia clinica do paciente.");
		btnPatientHistoryReportLeft.setImage(ResourceUtils
				.getImage(iDartImage.REPORT_PATIENTHISTORY_30X26));

		btnPatientHistoryReportLeft.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent dc) {
			}

			@Override
			public void mouseDown(MouseEvent md) {
			}

			@Override
			public void mouseUp(MouseEvent mu) {
				cmdPatientHistoryWidgetSelected(PatientSide.LEFT);
			}
		});

		lblDateOfBirthLeft = new Label(compLeftPatient, SWT.NONE);
		lblDateOfBirthLeft.setBounds(new Rectangle(5, 100, 84, 20));
		lblDateOfBirthLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblDateOfBirthLeft.setText("Data de Nascimento:");
		txtDateOfBirthLeft = new Text(compLeftPatient, SWT.BORDER);
		txtDateOfBirthLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(
				100, 100, 100, 20));
		txtDateOfBirthLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtDateOfBirthLeft.setEditable(false);

		lblAgeLeft = new Label(compLeftPatient, SWT.NONE);
		lblAgeLeft.setBounds(new Rectangle(205, 100, 24, 20));
		lblAgeLeft.setText("Idade:");
		lblAgeLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAgeLeft = new Text(compLeftPatient, SWT.BORDER);
		txtAgeLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(232, 100,
				30, 20));
		txtAgeLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAgeLeft.setEditable(false);
		txtAgeLeft.setEnabled(false);

		lblSexLeft = new Label(compLeftPatient, SWT.NONE);
		lblSexLeft.setBounds(new Rectangle(270, 100, 24, 20));
		lblSexLeft.setText("Sexo:");
		lblSexLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtSexLeft = new Text(compLeftPatient, SWT.BORDER);
		txtSexLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(297, 100,
				70, 20));
		txtSexLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtSexLeft.setEditable(false);

		lblAddressLeft = new Label(compLeftPatient, SWT.NONE);
		lblAddressLeft.setBounds(new Rectangle(5, 125, 84, 20));
		lblAddressLeft.setText("Endereço:");
		lblAddressLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddressLeft = new Text(compLeftPatient, SWT.BORDER);
		txtAddressLeft.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				125, 265, 20));
		txtAddressLeft.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddressLeft.setEditable(false);

		tblPatientHistoryLeft = new Table(compLeftPatient, SWT.FULL_SELECTION
				| SWT.BORDER);
		tblPatientHistoryLeft.setHeaderVisible(true);
		tblPatientHistoryLeft.setLinesVisible(true);
		tblPatientHistoryLeft.setBounds(new Rectangle(5, 150, 360, 100));
		tblPatientHistoryLeft.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		layoutPatientHistoryTable(tblPatientHistoryLeft);

	}

	public void createCompRightPatient() {
		compRightPatient = new Composite(getShell(), SWT.BORDER);
		compRightPatient.setBounds(new Rectangle(450, 70, 375, 260));

		lblSecondaryPatient = new Label(compRightPatient, SWT.LEFT);
		lblSecondaryPatient.setBounds(new Rectangle(5, 2, 250, 20));
		lblSecondaryPatient.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_12_BOLDITALIC));
		lblSecondaryPatient.setText("Paciente Duplicado");

		lblPatientIdRight = new Label(compRightPatient, SWT.LEFT);
		lblPatientIdRight.setBounds(new Rectangle(5, 25, 84, 20));
		lblPatientIdRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblPatientIdRight.setText(Messages.getString("patient.label.patientid")); //$NON-NLS-1$

		txtPatientIdRight = new Text(compRightPatient, SWT.BORDER);
		txtPatientIdRight.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				25, 150, 20));
		txtPatientIdRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtPatientIdRight.setEditable(true);
		txtPatientIdRight.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				if ((e.character == SWT.CR)
						|| (e.character == (char) iDartProperties.intValueOfAlternativeBarcodeEndChar)) {
					cmdSearchWidgetSelected(PatientSide.RIGHT);
				}

			}
		});

		btnPatientSearchRight = new Button(compRightPatient, SWT.CENTER);
		btnPatientSearchRight.setBounds(new org.eclipse.swt.graphics.Rectangle(
				260, 23, 110, 25));
		btnPatientSearchRight.setText("Procurar");
		btnPatientSearchRight.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		btnPatientSearchRight
				.setToolTipText("Clica no botão para seleccionar o paciente duplicado.");
		btnPatientSearchRight
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {

					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						cmdSearchWidgetSelected(PatientSide.RIGHT);
					}
				});
		lblFirstnamesRight = new Label(compRightPatient, SWT.NONE);
		lblFirstnamesRight.setBounds(new Rectangle(5, 50, 84, 20));
		lblFirstnamesRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblFirstnamesRight.setText("Name:");
		txtFirstnamesRight = new Text(compRightPatient, SWT.BORDER);
		txtFirstnamesRight.setBounds(new org.eclipse.swt.graphics.Rectangle(
				100, 50, 220, 20));
		txtFirstnamesRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtFirstnamesRight.setEditable(false);

		lblSurnameRight = new Label(compRightPatient, SWT.NONE);
		lblSurnameRight.setBounds(new Rectangle(5, 75, 84, 20));
		lblSurnameRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblSurnameRight.setText("Apelido:");
		txtSurnameRight = new Text(compRightPatient, SWT.BORDER);
		txtSurnameRight.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				75, 220, 20));
		txtSurnameRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtSurnameRight.setEditable(false);

		btnPatientHistoryReportRight = new Button(compRightPatient, SWT.NONE);
		btnPatientHistoryReportRight.setBounds(new Rectangle(325, 50, 40, 40));
		btnPatientHistoryReportRight
				.setToolTipText("Clica no botão para visualizar ou imprimir a historia clinica do paciente.");
		btnPatientHistoryReportRight.setImage(ResourceUtils
				.getImage(iDartImage.REPORT_PATIENTHISTORY_30X26));

		btnPatientHistoryReportRight.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent dc) {
			}

			@Override
			public void mouseDown(MouseEvent md) {
			}

			@Override
			public void mouseUp(MouseEvent mu) {
				cmdPatientHistoryWidgetSelected(PatientSide.RIGHT);
			}
		});

		lblDateOfBirthRight = new Label(compRightPatient, SWT.NONE);
		lblDateOfBirthRight.setBounds(new Rectangle(5, 100, 84, 20));
		lblDateOfBirthRight
				.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblDateOfBirthRight.setText("Data de Nascimento:");
		txtDateOfBirthRight = new Text(compRightPatient, SWT.BORDER);
		txtDateOfBirthRight.setBounds(new org.eclipse.swt.graphics.Rectangle(
				100, 100, 100, 20));
		txtDateOfBirthRight
				.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtDateOfBirthRight.setEditable(false);

		lblAgeRight = new Label(compRightPatient, SWT.NONE);
		lblAgeRight.setBounds(new Rectangle(205, 100, 24, 20));
		lblAgeRight.setText("Idade:");
		lblAgeRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAgeRight = new Text(compRightPatient, SWT.BORDER);
		txtAgeRight.setBounds(new org.eclipse.swt.graphics.Rectangle(232, 100,
				30, 20));
		txtAgeRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAgeRight.setEditable(false);
		txtAgeRight.setEnabled(false);

		lblSexRight = new Label(compRightPatient, SWT.NONE);
		lblSexRight.setBounds(new Rectangle(270, 100, 24, 20));
		lblSexRight.setText("Sexo:");
		lblSexRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtSexRight = new Text(compRightPatient, SWT.BORDER);
		txtSexRight.setBounds(new org.eclipse.swt.graphics.Rectangle(297, 100,
				70, 20));
		txtSexRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtSexRight.setEditable(false);

		lblAddressRight = new Label(compRightPatient, SWT.NONE);
		lblAddressRight.setBounds(new Rectangle(5, 125, 84, 20));
		lblAddressRight.setText("Endereço:");
		lblAddressRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddressRight = new Text(compRightPatient, SWT.BORDER);
		txtAddressRight.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				125, 265, 20));
		txtAddressRight.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddressRight.setEditable(false);

		tblPatientHistoryRight = new Table(compRightPatient, SWT.FULL_SELECTION
				| SWT.BORDER);
		tblPatientHistoryRight.setHeaderVisible(true);
		tblPatientHistoryRight.setLinesVisible(true);
		tblPatientHistoryRight.setBounds(new Rectangle(5, 150, 360, 100));
		tblPatientHistoryRight.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		layoutPatientHistoryTable(tblPatientHistoryRight);

	}

	public void createCompMergePatient() {
		compMergePatient = new Composite(getShell(), SWT.BORDER);
		compMergePatient.setBounds(new Rectangle(50, 340, 775, 250));

		lblMergedPatient = new Label(compMergePatient, SWT.CENTER);
		lblMergedPatient.setBounds(new Rectangle(5, 2, 765, 20));
		lblMergedPatient.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_12_BOLDITALIC));
		lblMergedPatient.setText("União de Paciente");

		lblPatientIdMerge = new Label(compMergePatient, SWT.LEFT);
		lblPatientIdMerge.setBounds(new Rectangle(5, 25, 84, 20));
		lblPatientIdMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblPatientIdMerge.setText(Messages.getString("patient.label.patientid")); //$NON-NLS-1$

		txtPatientIdMerge = new Text(compMergePatient, SWT.BORDER);
		txtPatientIdMerge.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				25, 275, 20));
		txtPatientIdMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtPatientIdMerge.setEditable(true);
		txtPatientIdMerge.setEnabled(false);

		lblFirstnamesMerge = new Label(compMergePatient, SWT.NONE);
		lblFirstnamesMerge.setBounds(new Rectangle(5, 50, 84, 20));
		lblFirstnamesMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblFirstnamesMerge.setText("Name:");
		txtFirstnamesMerge = new Text(compMergePatient, SWT.BORDER);
		txtFirstnamesMerge.setBounds(new org.eclipse.swt.graphics.Rectangle(
				100, 50, 275, 20));
		txtFirstnamesMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblSurnameMerge = new Label(compMergePatient, SWT.NONE);
		lblSurnameMerge.setBounds(new Rectangle(5, 75, 84, 20));
		lblSurnameMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblSurnameMerge.setText("Apelido:");
		txtSurnameMerge = new Text(compMergePatient, SWT.BORDER);
		txtSurnameMerge.setBounds(new org.eclipse.swt.graphics.Rectangle(100,
				75, 275, 20));
		txtSurnameMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblDateOfBirthMerge = new Label(compMergePatient, SWT.NONE);
		lblDateOfBirthMerge.setBounds(new Rectangle(5, 100, 84, 20));
		lblDateOfBirthMerge
				.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblDateOfBirthMerge.setText("Data de Nascimento:");

		cmbDOBDay = new Combo(compMergePatient, SWT.BORDER);
		cmbDOBDay.setBounds(new org.eclipse.swt.graphics.Rectangle(100, 100,
				50, 20));
		cmbDOBDay.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbDOBDay.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
		cmbDOBDay.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String theText = cmbDOBDay.getText();

				if (cmbDOBDay.getText().equals("")) {

				} else if (cmbDOBDay.indexOf(theText) == -1) {
					cmbDOBDay.setText("1");

				}
			}
		});

		cmbDOBMonth = new Combo(compMergePatient, SWT.BORDER);
		cmbDOBMonth.setBounds(new org.eclipse.swt.graphics.Rectangle(153, 100,
				97, 20));
		cmbDOBMonth.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbDOBMonth.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
		cmbDOBMonth.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {

				String theText = cmbDOBMonth.getText();

				if (theText.length() > 2) {

					String s = theText.substring(0, 1);
					String t = theText.substring(1, theText.length());
					theText = s.toUpperCase() + t;

					String[] items = cmbDOBMonth.getItems();

					for (int i = 0; i < items.length; i++) {
						if (items[i].substring(0, 3).equalsIgnoreCase(theText)) {
							cmbDOBMonth.setText(items[i]);
						}
					}

				}

			}
		});

		cmbDOBYear = new Combo(compMergePatient, SWT.BORDER);
		cmbDOBYear.setBounds(new org.eclipse.swt.graphics.Rectangle(255, 100,
				60, 20));
		cmbDOBYear.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbDOBYear.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
		cmbDOBYear.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String theText = cmbDOBYear.getText();
				if ((cmbDOBYear.indexOf(theText) == -1)
						&& (theText.length() >= 4)) {

					cmbDOBYear.setText("2000");

				}

			}
		});

		ComboUtils.populateDateCombos(cmbDOBDay, cmbDOBMonth, cmbDOBYear,
				false, false);
		cmbDOBDay.setVisibleItemCount(cmbDOBDay.getItemCount());
		cmbDOBMonth.setVisibleItemCount(cmbDOBMonth.getItemCount());
		cmbDOBYear.setVisibleItemCount(31);
		cmbDOBDay.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				cmdUpdateAge(txtAgeMerge);
			}
		});
		cmbDOBMonth.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				cmdUpdateAge(txtAgeMerge);
			}
		});
		cmbDOBYear.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				cmdUpdateAge(txtAgeMerge);
			}
		});

		lblAgeMerge = new Label(compMergePatient, SWT.NONE);
		lblAgeMerge.setBounds(new Rectangle(320, 100, 24, 20));
		lblAgeMerge.setText("Idade:");
		lblAgeMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		txtAgeMerge = new Text(compMergePatient, SWT.BORDER);
		txtAgeMerge.setBounds(new Rectangle(345, 100, 30, 20));
		txtAgeMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAgeMerge.setEditable(false);
		txtAgeMerge.setEnabled(false);

		lblSexMerge = new Label(compMergePatient, SWT.NONE);
		lblSexMerge.setBounds(new Rectangle(5, 125, 35, 20));
		lblSexMerge.setText("Sexo:");
		lblSexMerge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		cmbSex = new Combo(compMergePatient, SWT.BORDER);
		cmbSex.setBounds(new org.eclipse.swt.graphics.Rectangle(100, 125, 150,
				20));
		cmbSex.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbSex.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
		cmbSex.add("Feminino");
		cmbSex.add("Masculino");
		cmbSex.add("Desconhecido");
		cmbSex.select(0);
		cmbSex.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String theText = cmbSex.getText();

				if (theText.length() >= 1) {

					String s = theText.substring(0, 1);

					if (s.equalsIgnoreCase("F")) {
						cmbSex.setText("Feminino");
					} else if (s.equalsIgnoreCase("M")) {
						cmbSex.setText("Masculino");
					} else {
						cmbSex.setText("Desconhecido");
					}
				}

			}
		});

		lblAddress1Merge = new Label(compMergePatient, SWT.NONE);
		lblAddress1Merge.setBounds(new Rectangle(400, 25, 84, 20));
		lblAddress1Merge.setText("Local / No:");
		lblAddress1Merge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddress1Merge = new Text(compMergePatient, SWT.BORDER);
		txtAddress1Merge.setBounds(new org.eclipse.swt.graphics.Rectangle(495,
				25, 265, 20));
		txtAddress1Merge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblAddress2Merge = new Label(compMergePatient, SWT.NONE);
		lblAddress2Merge.setBounds(new Rectangle(400, 50, 84, 20));
		lblAddress2Merge.setText("Rua/Av:");
		lblAddress2Merge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddress2Merge = new Text(compMergePatient, SWT.BORDER);
		txtAddress2Merge.setBounds(new org.eclipse.swt.graphics.Rectangle(495,
				50, 265, 20));
		txtAddress2Merge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblAddress3Merge = new Label(compMergePatient, SWT.NONE);
		lblAddress3Merge.setBounds(new Rectangle(400, 75, 84, 20));
		lblAddress3Merge.setText("Bairro:");
		lblAddress3Merge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtAddress3Merge = new Text(compMergePatient, SWT.BORDER);
		txtAddress3Merge.setBounds(new org.eclipse.swt.graphics.Rectangle(495,
				75, 265, 20));
		txtAddress3Merge.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblInstructionsMerge = new Label(compMergePatient, SWT.WRAP
				| SWT.CENTER);
		lblInstructionsMerge.setBounds(new Rectangle(25, 158, 725, 70));
		lblInstructionsMerge.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_10_ITALIC));
		lblInstructionsMerge
				.setText("Após esta operação ser gravada ou executada com sussesso, "
						+ "o paciente preferido será o novo paciente a ser utilizado. "
						+ "Toda a informação dos dois pacentes em causa será alocada ao paciente preferido. NOTA: "
						+ " O Paciente Duplicado será REMOVIDO e a sua informação jamais será visualizada");

		createCompAppointment();

		lblWarning = new Label(compMergePatient, SWT.CENTER);
		lblWarning.setBounds(new Rectangle(5, 228, 765, 20));
		lblWarning.setText("AVISO: NÃO PODERA VOLTAR A INFORMAÇÃO ANTERIOR DEPOIS DE UNIR OS PACIENTES!");
		lblWarning.setFont(ResourceUtils.getFont(iDartFont.VERASANS_12));
		lblWarning.setForeground(ResourceUtils.getColor(iDartColor.RED));

		enableFields(false);

	}

	/**
	 * Method loadPatient.
	 *
	 * @param side
	 *            PatientSide
	 * @param p
	 *            Patient
	 */
	private void loadPatient(PatientSide side, Patient p) {
		if (side.equals(PatientSide.LEFT)) {
			if (p != rightPatient) {
				populateLeftPatientDetails(p);
				populatePatientHistoryTable(tblPatientHistoryLeft, p);
				leftPatient = p;
			} else {
				MessageBox mSamePatient = new MessageBox(getShell(),
						SWT.ICON_ERROR | SWT.OK);
				mSamePatient.setText("O Paciente já foi carregado");
				mSamePatient.setMessage("O Paciente '" + p.getPatientId()
						+ "' já foi carregado como o Paciente Duplicado");
				mSamePatient.open();
				txtPatientIdLeft.setText("");
			}
		} else {
			if (p != leftPatient) {
				populateRightPatientDetails(p);
				populatePatientHistoryTable(tblPatientHistoryRight, p);
				rightPatient = p;
			} else {
				getLog()
						.warn(
								"Colocou o mesmo paciente como preferido e duplicado para união");
				MessageBox mSamePatient = new MessageBox(getShell(),
						SWT.ICON_ERROR | SWT.OK);
				mSamePatient.setText("O Paciente já foi carregado");
				mSamePatient.setMessage("O Paciente '" + p.getPatientId()
						+ "' já foi carregado como o Paciente Preferido");
				mSamePatient.open();
				txtPatientIdRight.setText("");
			}

		}
	}

	/**
	 * Clears the patientForm and sets the default values
	 */
	@Override
	public void clearForm() {
		clearPatientDetails(PatientSide.LEFT);
		clearPatientDetails(PatientSide.RIGHT);
		clearMergePatientDetails();
		txtPatientIdLeft.setFocus();
	}

	/**
	 * Method clearPatientDetails.
	 *
	 * @param side
	 *            PatientSide
	 */
	public void clearPatientDetails(PatientSide side) {

		if (side.equals(PatientSide.LEFT)) {
			leftPatient = null;
			txtPatientIdLeft.setEnabled(true);
			txtPatientIdLeft.setText("");
			txtPatientIdLeft.setFocus();

			txtFirstnamesLeft.setText("");
			txtSurnameLeft.setText("");
			txtDateOfBirthLeft.setText("");
			txtAgeLeft.setText("");
			txtSexLeft.setText("");
			txtAddressLeft.setText("");
			tblPatientHistoryLeft.clearAll();
			tblPatientHistoryLeft.removeAll();
		} else {
			rightPatient = null;
			txtPatientIdRight.setEnabled(true);
			txtPatientIdRight.setText("");
			txtPatientIdRight.setFocus();

			txtFirstnamesRight.setText("");
			txtSurnameRight.setText("");
			txtDateOfBirthRight.setText("");
			txtSexRight.setText("");
			txtAgeRight.setText("");
			txtAddressRight.setText("");
			tblPatientHistoryRight.clearAll();
			tblPatientHistoryRight.removeAll();
		}

	}

	public void clearMergePatientDetails() {
		txtPatientIdMerge.setText("");
		txtFirstnamesMerge.setText("");
		txtSurnameMerge.setText("");
		cmbDOBDay.setText("");
		cmbDOBMonth.setText("");
		cmbDOBYear.setText("");
		txtAgeMerge.setText("");
		txtAddress1Merge.setText("");
		txtAddress2Merge.setText("");
		txtAddress3Merge.setText("");

		rdBtnAppointmentFromLeft.setText("");
		rdBtnAppointmentFromRight.setText("");

		enableFields(false);
	}

	/**
	 * Method enableFields.
	 *
	 * @param enable
	 *            boolean
	 */
	@Override
	public void enableFields(boolean enable) {
		txtFirstnamesMerge.setEnabled(enable);
		txtSurnameMerge.setEnabled(enable);
		cmbDOBDay.setEnabled(enable);
		cmbDOBMonth.setEnabled(enable);
		cmbDOBYear.setEnabled(enable);
		cmbSex.setEnabled(enable);
		txtAddress1Merge.setEnabled(enable);
		txtAddress2Merge.setEnabled(enable);
		txtAddress3Merge.setEnabled(enable);

		rdBtnAppointmentFromLeft.setEnabled(enable);
		rdBtnAppointmentFromRight.setEnabled(enable);
		rdBtnAppointmentFromLeft.setVisible(enable);
		rdBtnAppointmentFromRight.setVisible(enable);

	}

	/**
	 * checks the form for valid fields entries
	 *
	 * @return true if the requiResourceUtils.getColor(iDartColor.RED) fields
	 *         are filled in
	 */
	@Override
	protected boolean fieldsOk() {

		if (txtPatientIdLeft.getText().trim().equals("")) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo em branco");
			missing
					.setMessage("O campo NID do paciente não pode ser vazio. \n\n"
							+ "Por favor seleccione o Paciente Preferido");
			missing.open();
			txtPatientIdLeft.setFocus();
			return false;
		}

		else if (txtPatientIdRight.getText().trim().equals("")) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo em branco");
			missing
					.setMessage("O campo NID do paciente não pode ser vazio. \n\n"
							+ "Por favor seleccione o Paciente duplicado");
			missing.open();
			txtPatientIdRight.setFocus();
			return false;
		} else if (txtFirstnamesMerge.getText().trim().equals("")) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo em branco");
			missing.setMessage("Nome não pode ser vazio.");
			missing.open();
			txtFirstnamesMerge.setFocus();
			return false;

		}

		else if (txtSurnameMerge.getText().trim().equals("")) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo em branco");
			missing.setMessage("O Campo Apelido não pode ser vazio.");
			missing.open();
			txtSurnameMerge.setFocus();
			return false;
		}

		else if (cmbDOBDay.getText().equals("")
				|| cmbDOBMonth.getText().equals("")
				|| cmbDOBYear.getText().equals("")) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo em branco");
			missing.setMessage("o campo Data de Nascimento não pode ser vazio.");
			missing.open();
			cmbDOBDay.setFocus();
			return false;
		}

		else if (!dateOkay(cmbDOBDay.getText(), cmbDOBMonth.getText(),
				cmbDOBYear.getText())) {
			MessageBox incorrectData = new MessageBox(getShell(),
					SWT.ICON_ERROR | SWT.OK);
			incorrectData.setText("Campo Inválido");
			incorrectData.setMessage("A data de Nascimento é inválida.");
			incorrectData.open();
			cmbDOBDay.setFocus();
			return false;
		} else if (!iDARTUtil.validBirthDate(cmbDOBDay.getText(), cmbDOBMonth
				.getText(), cmbDOBYear.getText())) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo inválido");
			missing
					.setMessage("A data de nasciemnto é inválida. A data de nasciemnto não pode ser do futuro.");
			missing.open();
			cmbDOBDay.setFocus();
			return false;

		} else if (cmbSex.getText().equals("")) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Campo em branco");
			missing.setMessage("O campo Sexo não pode ser vazio.");
			missing.open();
			cmbSex.setFocus();
			return false;
		} else if ((PackageManager.patientHasUncollectedPackages(getHSession(),
				leftPatient))
				&& (PackageManager.patientHasUncollectedPackages(getHSession(),
				rightPatient))) {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("O pacientes por unir têm frascos não dispensados");
			missing
					.setMessage("O pacientes por unir têm frascos não dispensados.");
			missing.open();
			btnPatientSearchLeft.setFocus();
			return false;
		}

		return true;

	}

	/**
	 * checks if the given date is valid
	 *
	 * @param strDay
	 *            String
	 * @param strMonth
	 *            String
	 * @param strYear
	 *            String
	 * @return true if the date is valid else false
	 */
	// public boolean dateOkay(int day, int month, int year) {
	public boolean dateOkay(String strDay, String strMonth, String strYear) {

		boolean result = false;

		try {

			int day = Integer.parseInt(strDay);

			// check the year
			if (strYear.length() != 4)
				return result;
			int year = Integer.parseInt(strYear);

			// get the int value for the string month (e.g. January)
			// int month = Integer.parseInt(strMonth);
			int month = -1;
			for (int i = 0; i < cmbDOBMonth.getItemCount(); i++) {
				if (strMonth.equals(cmbDOBMonth.getItem(i))) {
					month = i;
				}
			}

			switch (month) {
				case -1:
					result = false;
					break;
				case Calendar.FEBRUARY:
					if (day <= 29) {
						GregorianCalendar greg = new GregorianCalendar();
						if (day == 29 & greg.isLeapYear(year)) {
							result = true;
						} else {
							if (day == 29) {
								result = false;
							} else {
								result = true;
							}
						}
					} else {
						result = false;
					}
					break;
				case Calendar.JANUARY | Calendar.MARCH | Calendar.MAY
						| Calendar.JULY | Calendar.AUGUST | Calendar.OCTOBER
						| Calendar.DECEMBER:
					if (day <= 31) {
						result = true;
					} else {
						result = false;
					}
					break;
				default:
					result = true;
					break;
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * Method cmdPatientHistoryWidgetSelected.
	 *
	 * @param side
	 *            PatientSide
	 */
	private void cmdPatientHistoryWidgetSelected(PatientSide side) {

		Patient patient = null;
		if ((side.equals(PatientSide.LEFT)) && (leftPatient != null)) {
			patient = leftPatient;
		} else if (rightPatient != null) {
			patient = rightPatient;
		}

		if (patient != null) {
			PatientHistoryReport report = new PatientHistoryReport(getShell(),
					patient, PatientHistoryReport.PATIENT_HISTORY_FILA);
			viewReport(report);
		} else {
			PatientHistory patHistory = new PatientHistory(getShell(), true);
			patHistory.openShell();
		}
	}

	/**
	 * Method cmdSearchWidgetSelected.
	 *
	 * @param side
	 *            PatientSide
	 */
	private void cmdSearchWidgetSelected(PatientSide side) {
		String patientId;
		if (side.equals(PatientSide.LEFT)) {
			patientId = PatientBarcodeParser
					.getPatientId(txtPatientIdLeft.getText());
		} else {
			patientId = PatientBarcodeParser
					.getPatientId(txtPatientIdRight.getText());
		}

		PatientSearch search = new PatientSearch(getShell(), getHSession());
		search.setShowInactive(true);
		PatientIdentifier identifier = search.search(patientId);

		if (identifier != null) {
			Patient p = identifier.getPatient();
			loadPatient(side, p);

			if ((leftPatient != null) && (rightPatient != null)) {
				populateMergePatientDetails();
			} else {
				clearMergePatientDetails();
			}
		} else {
			clearPatientDetails(side);
		}
	}

	/**
	 * Method populateLeftPatientDetails.
	 *
	 * @param p
	 *            Patient
	 */
	private void populateLeftPatientDetails(Patient p) {
		txtPatientIdLeft.setEnabled(false);
		txtPatientIdLeft.setText(p.getPatientId());
		txtSurnameLeft.setText(p.getLastname());
		txtFirstnamesLeft.setText(p.getFirstNames());
		txtDateOfBirthLeft.setText(new SimpleDateFormat("dd MMM yyyy", localeEn).format(p
				.getDateOfBirth()));
		txtAgeLeft.setText(p.getAge() + "");
		String addr = "";

		if ((p.getAddress1() != null) && (!p.getAddress1().equals(""))) {
			addr += p.getAddress1();
		}
		if ((p.getAddress2() != null) && (!p.getAddress2().equals(""))) {
			addr += ", ";
			addr += p.getAddress2();
		}
		if ((p.getAddress3() != null) && (!p.getAddress3().equals(""))) {
			addr += ", ";
			addr += p.getAddress3();
		}
		txtAddressLeft.setText(addr);

		switch (p.getSex()) {
			case 'F':
				txtSexLeft.setText("Feminino");
				break;
			case 'M':
				txtSexLeft.setText("Masculino");
				break;
			case 'U':
				txtSexLeft.setText("Desconhecido");
				break;
			default:
				txtSexLeft.setText("Desconhecido");
				break;
		}

	}

	/**
	 * Method populateRightPatientDetails.
	 *
	 * @param p
	 *            Patient
	 */
	private void populateRightPatientDetails(Patient p) {

		txtPatientIdRight.setEnabled(false);
		txtPatientIdRight.setText(p.getPatientId());

		txtSurnameRight.setText(p.getLastname());
		txtFirstnamesRight.setText(p.getFirstNames());
		txtDateOfBirthRight.setText(new SimpleDateFormat("dd MMM yyyy", localeEn)
				.format(p.getDateOfBirth()));
		txtAgeRight.setText(p.getAge() + "");

		String addr = "";

		if ((p.getAddress1() != null) && (!p.getAddress1().equals(""))) {
			addr += p.getAddress1();
		}
		if ((p.getAddress2() != null) && (!p.getAddress2().equals(""))) {
			addr += ", ";
			addr += p.getAddress2();
		}
		if ((p.getAddress3() != null) && (!p.getAddress3().equals(""))) {
			addr += ", ";
			addr += p.getAddress3();
		}
		txtAddressRight.setText(addr);

		switch (p.getSex()) {
			case 'F':
				txtSexRight.setText("Feminino");
				break;
			case 'M':
				txtSexRight.setText("Masculino");
				break;
			case 'U':
				txtSexRight.setText("Desconhecido");
				break;
			default:
				txtSexRight.setText("Desconhecido");
				break;
		}

	}

	private void populateMergePatientDetails() {
		enableFields(true);
		txtPatientIdMerge.setText(leftPatient.getPatientId());

		String surname = (leftPatient.getLastname().equals("") ? rightPatient
				.getLastname() : leftPatient.getLastname());
		String firstnames = (leftPatient.getFirstNames().equals("") ? rightPatient
				.getFirstNames()
				: leftPatient.getFirstNames());

		String address1 = (leftPatient.getAddress1().equals("") ? rightPatient
				.getAddress1() : leftPatient.getAddress1());
		String address2 = (leftPatient.getAddress2().equals("") ? rightPatient
				.getAddress2() : leftPatient.getAddress2());
		String address3 = (leftPatient.getAddress3().equals("") ? rightPatient
				.getAddress3() : leftPatient.getAddress3());

		Date dateOfBirth = (leftPatient.getDateOfBirth() == null ? rightPatient
				.getDateOfBirth() : leftPatient.getDateOfBirth());
		txtAgeMerge.setText(leftPatient.getAge() + "");
		char sex = leftPatient.getSex();

		txtFirstnamesMerge.setText(firstnames);
		txtSurnameMerge.setText(surname);

		Calendar theDOB = Calendar.getInstance();
		theDOB.setTime(dateOfBirth);
		cmbDOBDay.setText(String.valueOf(theDOB.get(Calendar.DAY_OF_MONTH)));
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM");
		cmbDOBMonth.setText(monthFormat.format(theDOB.getTime()));
		cmbDOBYear.setText(String.valueOf(theDOB.get(Calendar.YEAR)));

		if (sex == 'F' || sex == 'f') {
			cmbSex.setText("Feminino");
		}

		else if (sex == 'M' || sex == 'm') {
			cmbSex.setText("Masculino");
		} else {
			cmbSex.setText("Desconhecido");
		}

		txtAddress1Merge.setText(address1);
		txtAddress2Merge.setText(address2);
		txtAddress3Merge.setText(address3);

		populateCompAppointment();

	}

	private void createCompAppointment() {
		compAppointment = new Composite(compMergePatient, SWT.BORDER);
		compAppointment.setLayout(null);
		compAppointment.setBounds(new Rectangle(400, 100, 360, 45));

		lblAppointment = new Label(compAppointment, SWT.NONE);
		lblAppointment.setBounds(new Rectangle(5, 15, 150, 20));
		lblAppointment.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblAppointment.setText("Escolha a próxima consulta/Levantamento:");

		rdBtnAppointmentFromLeft = new Button(compAppointment, SWT.RADIO);
		rdBtnAppointmentFromLeft.setBounds(new Rectangle(160, 12, 95, 20));
		rdBtnAppointmentFromLeft.setSelection(false);
		rdBtnAppointmentFromLeft.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		rdBtnAppointmentFromLeft.setEnabled(false);

		rdBtnAppointmentFromRight = new Button(compAppointment, SWT.RADIO);
		rdBtnAppointmentFromRight.setBounds(new Rectangle(265, 12, 95, 20));
		rdBtnAppointmentFromRight.setSelection(true);
		rdBtnAppointmentFromRight.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		rdBtnAppointmentFromRight.setEnabled(false);

	}

	/**
	 * Method savePatientsInMerge.
	 *
	 * @return boolean
	 */
	public boolean savePatientsInMerge() {
		Transaction tx = null;
		try {
			tx = getHSession().beginTransaction();
			prepareMergePatientDemographics();

			if (!rdBtnAppointmentFromLeft.getSelection()) {
				Appointment leftApp = PatientManager
						.getLatestAppointmentForPatient(leftPatient, true);
				if (leftApp != null) {
					leftPatient.getAppointments().remove(leftApp);
					PatientManager.save(getHSession(), leftApp);
				}
			}
			if (!rdBtnAppointmentFromRight.getSelection()) {
				Appointment rightApp = PatientManager
						.getLatestAppointmentForPatient(rightPatient, true);
				if (rightApp != null) {
					rightPatient.getAppointments().remove(rightApp);
					PatientManager.save(getHSession(), rightApp);
				}

			}

			for (Object p : rightPatient.getPrescriptions()) {
				((Prescription) p).setPatient(leftPatient);
				leftPatient.getPrescriptions().add((Prescription) p);
			}
			getHSession().flush();
			for (Object p : rightPatient.getPregnancies()) {
				((Pregnancy) p).setPatient(leftPatient);
			}

			for (Object alt : rightPatient.getAlternateIdentifiers()) {
				((AlternatePatientIdentifier) alt).setPatient(leftPatient);
			}

			Set<PatientIdentifier> identifiers = rightPatient.getPatientIdentifiers();
			for (PatientIdentifier id : identifiers) {
				leftPatient.getAlternateIdentifiers()
						.add(new AlternatePatientIdentifier(
								id.getValue(),
								leftPatient, new Date(), false, id.getType()));
			}

			for (Object app : rightPatient.getAppointments()) {
				((Appointment) app).setPatient(leftPatient);
			}

			// Remove all pillcounts and accumulated drugs\
			boolean wasPillcountsremoved = false;
			for (Prescription p : leftPatient.getPrescriptions()) {
				for (Packages pack : p.getPackages()) {
					PackageManager.removeAccumulatedDrugs(getHSession(), pack
							.getAccumulatedDrugs());
					pack.getAccumulatedDrugs().clear();
				}

			}

			for (Prescription p : leftPatient.getPrescriptions()) {
				for (Packages pack : p.getPackages()) {
					wasPillcountsremoved = PackageManager.removePillcounts(
							getHSession(), pack.getPillCounts());
					pack.getPillCounts().clear();
				}
			}

			// remove all other fields for right Patient
			// rightPatient.getAttributes().clear();
			// rightPatient.getEpisodes().clear();

			PatientManager.logPatientMerge(getHSession(), leftPatient,
					rightPatient, wasPillcountsremoved);

			// save changes to the left (primary) patient and delete the right
			// (secondary) patient
			PatientManager.savePatient(getHSession(), leftPatient);

			// update Packagedruginfos : Unsubmitted  records m  to openmrs  due to patientid mismatch
			List<PackageDrugInfo> pdiList = TemporaryRecordsManager.getOpenmrsUnsubmittedPackageDrugInfos(getHSession(), rightPatient);

			if(!pdiList.isEmpty())
				TemporaryRecordsManager.updateOpenmrsUnsubmittedPackageDrugInfos(getHSession(), pdiList, leftPatient);

			// and delete the right
			PatientManager.deleteSecondaryPatient(getHSession(), rightPatient);

			getHSession().flush();
			tx.commit();
			return true;
		} catch (HibernateException he) {

			if (tx != null) {
				tx.rollback();
			}
			getLog().error(he);
			return false;
		}
	}

	public void prepareMergePatientDemographics() {

		leftPatient.setPatientId(txtPatientIdMerge.getText());
		leftPatient.setLastname(txtSurnameMerge.getText());
		leftPatient.setFirstNames(txtFirstnamesMerge.getText());

		SimpleDateFormat sdf = new SimpleDateFormat("d-MMMM-yyyy");

		try {
			Date theDate = sdf.parse(cmbDOBDay.getText() + "-"
					+ cmbDOBMonth.getText() + "-" + cmbDOBYear.getText());
			leftPatient.setDateOfBirth(theDate);
		} catch (ParseException p) {
			getLog().error(p);
		}

		if (cmbSex.getText().equals("Feminino")) {
			leftPatient.setSex('F');
		} else if (cmbSex.getText().equals("Masculino")) {
			leftPatient.setSex('M');
		} else {
			leftPatient.setSex('U');
		}

		leftPatient.setAddress1(txtAddress1Merge.getText());
		leftPatient.setAddress2(txtAddress2Merge.getText());
		leftPatient.setAddress3(txtAddress3Merge.getText());

		if (leftPatient.getCellphone().trim().equals("")) {
			if (!rightPatient.getCellphone().trim().equals("")) {
				leftPatient.setCellphone(rightPatient.getCellphone());
			}
		} else {
			if (!rightPatient.getCellphone().trim().equals("")) {
				leftPatient.setCellphone(leftPatient.getCellphone() + "/"
						+ rightPatient.getCellphone());
			} else {
				leftPatient.setCellphone(leftPatient.getCellphone());
			}
		}

		if (leftPatient.getHomePhone().trim().equals("")) {
			if (!rightPatient.getHomePhone().trim().equals("")) {
				leftPatient.setHomePhone(rightPatient.getHomePhone());
			}
		} else {
			if (!rightPatient.getHomePhone().trim().equals("")) {
				leftPatient.setHomePhone(leftPatient.getHomePhone() + "/"
						+ rightPatient.getHomePhone());
			} else {
				leftPatient.setHomePhone(leftPatient.getHomePhone());
			}
		}

		if (leftPatient.getWorkPhone().trim().equals("")) {
			if (!rightPatient.getWorkPhone().trim().equals("")) {
				leftPatient.setWorkPhone(rightPatient.getWorkPhone());
			}
		} else {
			if (!rightPatient.getWorkPhone().trim().equals("")) {
				leftPatient.setWorkPhone(leftPatient.getWorkPhone() + "/"
						+ rightPatient.getWorkPhone());
			} else {
				leftPatient.setWorkPhone(leftPatient.getWorkPhone());
			}
		}

		if (leftPatient.getNextOfKinName().trim().equals("")) {
			if (!rightPatient.getNextOfKinName().trim().equals("")) {
				leftPatient.setNextOfKinName(rightPatient.getNextOfKinName());
			}
		} else {
			if (!rightPatient.getNextOfKinName().trim().equals("")) {
				leftPatient.setNextOfKinName(leftPatient.getNextOfKinName()
						+ "/" + rightPatient.getNextOfKinName());
			} else {
				leftPatient.setNextOfKinName(leftPatient.getNextOfKinName());
			}
		}

		if (leftPatient.getNextOfKinPhone().trim().equals("")) {
			if (!rightPatient.getNextOfKinPhone().trim().equals("")) {
				leftPatient.setNextOfKinPhone(rightPatient.getNextOfKinPhone());
			}
		} else {
			if (!rightPatient.getNextOfKinPhone().trim().equals("")) {
				leftPatient.setNextOfKinPhone(leftPatient.getNextOfKinPhone()
						+ "/" + rightPatient.getNextOfKinPhone());
			} else {
				leftPatient.setNextOfKinPhone(leftPatient.getNextOfKinPhone());
			}
		}
	}

	private void populateCompAppointment() {
		final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yy");

		Calendar leftDate = null;
		Calendar rightDate = null;

		Calendar todayStart = Calendar.getInstance();
		todayStart.setTime(new Date());
		todayStart.set(Calendar.HOUR_OF_DAY, 0);
		todayStart.set(Calendar.MINUTE, 0);
		todayStart.set(Calendar.SECOND, 0);
		todayStart.set(Calendar.MILLISECOND, 0);

		if (leftPatient != null) {
			Appointment app = PatientManager
					.getLatestAppointmentForPatient(leftPatient, true);

			if ((app != null) && (app.getAppointmentDate() != null)) {
				leftDate = Calendar.getInstance();
				leftDate.setTime(app.getAppointmentDate());
				rdBtnAppointmentFromLeft.setEnabled(true);
				rdBtnAppointmentFromLeft
						.setText(sdf.format(leftDate.getTime()));

				if (app.getAppointmentDate().getTime() >= todayStart
						.getTimeInMillis()) {
					rdBtnAppointmentFromLeft.setSelection(true);
				} else {
					rdBtnAppointmentFromLeft.setSelection(false);

				}
			}
		}

		if (rightPatient != null) {
			Appointment app = PatientManager
					.getLatestAppointmentForPatient(rightPatient, true);

			if ((app != null) && (app.getAppointmentDate() != null)) {
				rightDate = Calendar.getInstance();
				rightDate.setTime(app.getAppointmentDate());
				rdBtnAppointmentFromRight.setEnabled(true);
				rdBtnAppointmentFromRight.setText(sdf.format(rightDate
						.getTime()));

				if (app.getAppointmentDate().getTime() >= todayStart
						.getTimeInMillis()) {
					if (!rdBtnAppointmentFromLeft.getSelection()) {
						rdBtnAppointmentFromRight.setSelection(true);
					} else {
						rdBtnAppointmentFromRight.setSelection(false);
					}

				} else {
					rdBtnAppointmentFromRight.setSelection(false);

				}

			}
		}

		if ((leftDate == null) || rightDate == null) {
			if ((leftDate == null)) {
				rdBtnAppointmentFromLeft.setText("(none)");
				rdBtnAppointmentFromLeft.setEnabled(false);
				rdBtnAppointmentFromLeft.setSelection(false);
			}
			if (rightDate == null) {
				rdBtnAppointmentFromRight.setText("(none)");
				rdBtnAppointmentFromRight.setEnabled(false);
				rdBtnAppointmentFromRight.setSelection(false);
			}
		}

	}

	@Override
	protected void cmdSaveWidgetSelected() {

		if (fieldsOk()) {

			MessageBox mSave = new MessageBox(getShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mSave.setText("Gravar a união destes pacientes?");
			mSave.setMessage("Tem certeza que deseja unir estes pacientes?");

			switch (mSave.open()) {
				case SWT.YES:
					// before we try anything, lets ask the user for their password
					String confirm = "AVISO: SO DEVERA EXECUTAR ESTA FUNCIONALIDADE SE TIVER CERTEZA QUE PRETENDE UNIR OS PACIENTES PERMANENTIMENTE.";

						if (!savePatientsInMerge()) {
							MessageBox mError = new MessageBox(getShell(),
									SWT.ICON_ERROR | SWT.OK);
							mError.setText("Problemas ao gravar");
							mError
									.setMessage("Houve um problema ao gravar esta união.");
							mError.open();
						} else {
							MessageBox mSuccess = new MessageBox(getShell(),
									SWT.ICON_INFORMATION | SWT.OK);
							mSuccess.setText("União de pacientes com sucesso");
							mSuccess.setMessage("Paciente '"
									+ txtPatientIdLeft.getText() + "' e '"
									+ rightPatient.getPatientId()
									+ "' unidos com sucesso.");
							mSuccess.open();
							getHSession().close();

							GenericFormGui.addInitialisationOption(
									GenericFormGui.OPTION_isAddNotUpdate, false);
							new AddPatient(getParent(), leftPatient);
							getShell().dispose();
						}
					break;
				case SWT.NO:
					break;
			}
		}
	}

	@Override
	protected void cmdCancelWidgetSelected() {
		cmdCancelSelected();

	}

	@Override
	protected void cmdClearWidgetSelected() {
		clearForm();
	}

	/**
	 * Method layoutPatientHistoryTable.
	 *
	 * @param table
	 *            Table
	 */
	private void layoutPatientHistoryTable(Table table) {
		TableColumn clmPackId = new TableColumn(table, SWT.NONE);
		clmPackId.setText("Pacote ID");
		clmPackId.setWidth(100);
		clmPackId.setResizable(true);

		TableColumn clmDate = new TableColumn(table, SWT.NONE);
		clmDate.setText("Data Rec.");
		clmDate.setWidth(60);
		clmDate.setResizable(true);

		TableColumn clmPackages = new TableColumn(table, SWT.NONE);
		clmPackages.setText("Medicamentos no Pacote");
		clmPackages.setWidth(180);
		clmPackages.setResizable(true);

	}

	/**
	 * Method populatePatientHistoryTable.
	 *
	 * @param table
	 *            Table
	 *            List<Patient>
	 */
	private void populatePatientHistoryTable(Table table, final Patient patient) {

		final Table tempTable = new Table(table.getParent(), SWT.BORDER
				| SWT.FULL_SELECTION);
		layoutPatientHistoryTable(tempTable);

		table.clearAll();
		table.removeAll();
		table.setItemCount(0);

		final Display display = Display.getCurrent();

		patientHistoryLoadingFinished = false;
		final Vector<Packages> packages = new Vector<Packages>(PackageManager
				.getAllPackagesForPatient(getHSession(), patient));

		if (packages.size() > 0) {
			Thread thread = new Thread() {
				@Override
				public void run() {
					try {
						display.syncExec(new Runnable() {
							@Override
							public void run() {
								final SimpleDateFormat sdf = new SimpleDateFormat(
										"dd MMM yy");
								for (Packages pack : packages) {
									TableItem ti = new TableItem(tempTable,
											SWT.NONE);
									Date pickupDate = pack.getPickupDate();
									ti.setText(0, pack.getPackageId());
									ti.setText(1, pickupDate != null ? sdf
											.format(pickupDate) : "");
									ti.setText(2, PackageManager
											.getShortPackageContentsString(
													getHSession(), pack));
								}
								patientHistoryLoadingFinished = true;
							}
						});

						while (!patientHistoryLoadingFinished) {
							sleep(10);
							patientHistoryLoadingFinished = true;
						}
					} catch (InterruptedException ex) {
						getLog().warn(
								"Carregamento de Pacotes do pacientes interrompido");
						patientHistoryLoadingFinished = true;
					}
				}
			};

			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();

			String loadingString = "";

			loadingString = " Paciente " + patient.getPatientId();

			createLoadingBar(5 + " segundos", loadingString);

			TableItem[] items = tempTable.getItems();

			for (int i = 0; i < items.length; i++) {
				TableItem ti = items[i];
				TableItem newTi = new TableItem(table, SWT.NONE);
				newTi.setText(0, ti.getText(0));
				newTi.setText(1, ti.getText(1));
				newTi.setText(2, ti.getText(2));
			}
		}

	}

	/**
	 * Method createLoadingBar.
	 *
	 * @param timeEst
	 *            String
	 * @param patientDescription
	 *            String
	 */
	public void createLoadingBar(String timeEst, String patientDescription) {

		Shell shell = new Shell(getShell(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText("Carregando " + patientDescription);

		Label label = new Label(shell, SWT.NONE);
		label.setSize(new Point(260, 20));
		label.setLocation(20, 30);
		label.setText("Por favor agrande, carregando dados históricos.");
		label.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		label.setAlignment(SWT.CENTER);

		Label timelabel = new Label(shell, SWT.NONE);
		timelabel.setSize(new Point(260, 20));
		timelabel.setLocation(20, 50);
		timelabel.setText("(approx " + timeEst + ")");
		timelabel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		timelabel.setAlignment(SWT.CENTER);

		ProgressBar progressBar = new ProgressBar(shell, SWT.HORIZONTAL
				| SWT.INDETERMINATE);
		progressBar.setSize(new Point(200, 20));
		progressBar.setLocation(50, 80);

		shell.setSize(new Point(300, 200));
		LayoutUtils.centerGUI(shell);
		shell.open();

		while (!patientHistoryLoadingFinished) {
			if (shell.getDisplay().readAndDispatch()) {
				shell.getDisplay().sleep();
			}
		}

		if (!shell.isDisposed()) {
			shell.close();
		}
	}

	/**
	 * Method cmdUpdateAge.
	 *
	 * @param txtAge
	 *            Text
	 */
	public void cmdUpdateAge(Text txtAge) {

		SimpleDateFormat sdf = new SimpleDateFormat("d-MMMM-yyyy");
		try {
			// Set the date of birth
			if ((!cmbDOBDay.getText().equals(""))
					&& (!cmbDOBMonth.getText().equals(""))
					&& (!cmbDOBYear.getText().equals(""))) {
				Date theDate = sdf.parse(cmbDOBDay.getText() + "-"
						+ cmbDOBMonth.getText() + "-" + cmbDOBYear.getText());

				Calendar today = Calendar.getInstance();
				Calendar dob = Calendar.getInstance();
				dob.setTime(theDate);
				// Get age based on year
				int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
				// Add the tentative age to the date of birth to get this year's
				// birthday
				dob.add(Calendar.YEAR, age);
				// If birthday hasn't happened yet, subtract one from
				// age
				if (today.before(dob)) {
					age--;
				}
				txtAge.setText(String.valueOf(age));
			}
		} catch (ParseException nbe) {

		}
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
		setLog(Logger.getLogger(this.getClass()));

	}

}
