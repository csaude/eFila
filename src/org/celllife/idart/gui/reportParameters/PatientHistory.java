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

package org.celllife.idart.gui.reportParameters;

import model.manager.PatientManager;
import model.manager.SearchManager;
import model.manager.reports.PatientHistoryReport;
import model.nonPersistent.PatientIdAndName;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.misc.PatientBarcodeParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import java.util.List;
import java.util.Vector;

/**
 */
public class PatientHistory extends GenericReportGui {

	private Table tblWaitingPatients;

	private TableColumn tblColPatId;

	private TableColumn tblColPatName;

	private Vector<PatientIdAndName> allPatientsAtClinic;

	private Group grpPatientSelection;

	private Text searchBar;

	private String patientHistoryType;

	private Button rdBtnTARVPatient;

	private Button rdBtnTBPatient;

	private static String tipoPaciente = null;

	private boolean radioButtosEnabled;
	/**
	 * Constructor
	 *  @param parent
	 *            Shell
	 * @param activate
	 */
	public PatientHistory(Shell parent, boolean activate) {
		super(parent, REPORTTYPE_PATIENT, activate);

		tipoPaciente = iDartProperties.SERVICOTARV;

		this.radioButtosEnabled = true;

		this.patientHistoryType = PatientHistoryReport.PATIENT_HISTORY_FILA;
	}

	public PatientHistory(Shell parent, boolean activate, String patientHistoryType) {
		super(parent, REPORTTYPE_PATIENT, activate);
		this.patientHistoryType = patientHistoryType;

		if (this.patientHistoryType.equalsIgnoreCase(PatientHistoryReport.PATIENT_HISTORY_FILA)){

			tipoPaciente = iDartProperties.SERVICOTARV;
		}else {

			tipoPaciente = iDartProperties.PNCT;
		}

		this.radioButtosEnabled = false;
	}

	/**
	 * This method initializes newStockReceipt
	 */
	@Override
	protected void createShell() {
		// The GenericFormsGui class needs
		// Header text, icon URL, shell bounds
		Rectangle bounds = new Rectangle(100, 50, 500, 650);
		// Parent Generic Methods ------
		buildShell(REPORT_PATIENT_HISTORY, bounds); // generic shell build
		// create the composites
		createMyComposites();
	}

	private void createMyComposites() {
		createTableAndSearchBar();
		populatePatients();
	}

	/**
	 * This method initializes compHeader
	 * 
	 */
	@Override
	protected void createCompHeader() {
		iDartImage icoImage = iDartImage.REPORT_PATIENTHISTORY;
		buildCompdHeader(REPORT_PATIENT_HISTORY, icoImage);
	}

	/**
	 * This method initializes compButtons
	 * 
	 */
	@Override
	protected void createCompButtons() {
	}

	/**
	 * This method initializes the table and search bar components
	 * 
	 */
	private void createTableAndSearchBar() {

		grpPatientSelection = new Group(getShell(), SWT.NONE);
		grpPatientSelection
		.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpPatientSelection.setText("Seleccione o Paciente");
		grpPatientSelection.setBounds(new Rectangle(80, 80, 340, 470));

		tblWaitingPatients = new Table(grpPatientSelection, SWT.FULL_SELECTION);
		tblWaitingPatients.setBounds(new Rectangle(20, 35, 306, 365));
		tblWaitingPatients.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		tblWaitingPatients.setHeaderVisible(true);

		tblColPatId = new TableColumn(tblWaitingPatients, SWT.NONE);
		tblColPatId.setWidth(100);
		tblColPatId.setText("NID");

		tblColPatName = new TableColumn(tblWaitingPatients, SWT.NONE);
		tblColPatName.setWidth(150);
		tblColPatName.setText("Nome do Paciente");

		searchBar = new Text(grpPatientSelection, SWT.BORDER);
		searchBar.setBounds(new Rectangle(17, 409, 311, 20));
		searchBar.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		searchBar.setFocus();
		searchBar.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				minimiseSearch(tblWaitingPatients, searchBar.getText(),
						allPatientsAtClinic);

				if ((e.character == SWT.CR)
						|| (e.character == (char) iDartProperties.intValueOfAlternativeBarcodeEndChar)) {
					enterPressedInPatientSearchBar();
				}
			}
		});

		// rdBtnTBPrescription
		rdBtnTBPatient = new Button(grpPatientSelection, SWT.RADIO);
		//rdBtnTBPatient.setEnabled(this.radioButtosEnabled);
		rdBtnTBPatient.setBounds(new Rectangle(180, 430, 100, 30));
		rdBtnTBPatient.setText("TPT");
		if (patientHistoryType != null && patientHistoryType.equals(PatientHistoryReport.PATIENT_HISTORY_FILT)) {
			rdBtnTBPatient.setSelection(true);
		}

		rdBtnTBPatient.setToolTipText("Pressione este botão para criar/actualizar uma prescrição de medicamentos TB.");
		rdBtnTBPatient.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				tipoPaciente = "TB";
				patientHistoryType = PatientHistoryReport.PATIENT_HISTORY_FILT;
			}
		});
		rdBtnTBPatient.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		rdBtnTARVPatient = new Button(grpPatientSelection, SWT.RADIO);
		if (patientHistoryType == null || patientHistoryType.equals(PatientHistoryReport.PATIENT_HISTORY_FILA)) {
			rdBtnTARVPatient.setSelection(true);
		}
		rdBtnTARVPatient.setBounds(new Rectangle(80, 430, 100, 30));
		rdBtnTARVPatient.setText("TARV");
		//rdBtnTARVPatient.setEnabled(this.radioButtosEnabled);
		rdBtnTARVPatient.setToolTipText("Pressione este botão para criar/actualizar uma prescrição de medicamentos TARV.");
		rdBtnTARVPatient.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				tipoPaciente = "TARV";
				patientHistoryType = PatientHistoryReport.PATIENT_HISTORY_FILA;
			}
		});
		rdBtnTARVPatient.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
	}

	/**
	 * This method is called when the user presses the 'View Report' button. It
	 * calls the ReportManager.viewStockReceiptReport with the appropriate start
	 * and end dates, as given by the user.
	 * 
	 */
	@Override
	protected void cmdViewReportWidgetSelected() {
		if ((tblWaitingPatients.getSelectionIndex() != -1)) {
			String patientId = tblWaitingPatients.getSelection()[0].getText(0);
			Patient patient = PatientManager.getPatient(getHSession(), patientId);
			PatientHistoryReport report = new PatientHistoryReport(getShell(),patient, this.patientHistoryType);
			viewReport(report);
		}
		else if(!"".equals(searchBar.getText())) {
			enterPressedInPatientSearchBar();
		}
		else {
			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("Nenhum paciente foi seleccionado");
			missing.setMessage("Por favor, seleccione o paciente");
			missing.open();
		}
		searchBar.setText("");
		searchBar.setFocus();
	}

	private void enterPressedInPatientSearchBar() {

		String patientId = "";

		if (tblWaitingPatients.getItemCount() == 1) {
			patientId = tblWaitingPatients.getItem(0).getText();
		}

		searchBar.setText(searchBar.getText().toUpperCase());

		if(patientId.isEmpty()) {
			patientId = PatientBarcodeParser.getPatientId(searchBar
					.getText());

		}

		if(patientId == null){
			MessageBox mb = new MessageBox(getShell());
			mb.setText("Não foi introduzido o NID do paciente");
			mb
			.setMessage("Não foi introduzido o NID do Paciente. \n\nPor favor intruduzir o NID do Paciente.");
			mb.open();
			searchBar.setText("");
			searchBar.setFocus();
			minimiseSearch(tblWaitingPatients, "", allPatientsAtClinic);
			return;
		}

		Patient patient = PatientManager.getPatient(getHSession(), patientId);
		if (patient != null) {
			PatientHistoryReport report = new PatientHistoryReport(getShell(), patient, this.patientHistoryType);
			viewReport(report);
		} else {
			MessageBox mb = new MessageBox(getShell());
			mb.setText("O Paciente não existe");
			mb
			.setMessage("Não existe nenhum Paciente com o NID '"
					+ searchBar.getText()
					+ "' no IDART.");
			mb.open();
		}
		searchBar.setText("");
		searchBar.setFocus();
		minimiseSearch(tblWaitingPatients, "", allPatientsAtClinic);
	}

	/**
	 * This method is called when the user presses "Close" button
	 * 
	 */
	@Override
	protected void cmdCloseWidgetSelected() {
		cmdCloseSelected();
	}

	/**
	 * Method minimiseSearch.
	 * @param t Table
	 * @param searchString String
	 * @param fullList List<PatientIdAndName>
	 */
	public static void minimiseSearch(Table t, String searchString,
			List<PatientIdAndName> fullList) {
		t.removeAll();
		for (int i = 0; i < fullList.size(); i++) {
			int found1 = 0;
			int found2 = 0;

			PatientIdAndName p = fullList.get(i);
			found1 = p.getPatientId().toUpperCase().indexOf(
					searchString.toUpperCase());
			found2 = p.getNames().toUpperCase().indexOf(
					searchString.toUpperCase());

			if (found1 != -1 || found2 != -1) {
				TableItem tableItem = new TableItem(t, SWT.NONE);
				String[] newStrings = new String[2];
				newStrings[0] = p.getPatientId();
				newStrings[1] = p.getNames();
				tableItem.setText(newStrings);
			}
		}
	}

	private void populatePatients() {
		java.util.List<PatientIdAndName> l = SearchManager
		.getPatientIDsAndNames(getHSession(), true, false);

		tblWaitingPatients.removeAll();
		allPatientsAtClinic = new Vector<PatientIdAndName>();
		for (int i = 0; i < l.size(); i++) {
			PatientIdAndName patStr = l.get(i);
			TableItem ti = new TableItem(tblWaitingPatients, SWT.NONE);
			ti.setText(0, patStr.getPatientId());
			ti.setText(1, patStr.getNames());
			allPatientsAtClinic.add(patStr);
		}
	}

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {
	}

}
