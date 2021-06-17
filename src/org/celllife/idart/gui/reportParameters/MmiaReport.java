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

import model.manager.AdministrationManager;
import model.manager.reports.MiaReport;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 */
public class MmiaReport extends GenericReportGui {
	
	private Group grpDateRange;

	private Group grpPharmacySelection;

	private CCombo cmbStockCenter;

	private CCombo cmbMonth;

	private CCombo cmbYear;

	private Group grpDateInfo;

	private Label lblInstructions;

	private Shell parent;

	private String diseaseType;
	/**
	 * Constructor
	 *
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public MmiaReport(Shell parent, boolean activate, String diseaseType) {
		super(parent, REPORTTYPE_MIA, activate);
		this.parent = parent;
		this.diseaseType = diseaseType;
	}

	/**
	 * This method initializes newMonthlyStockOverview
	 */
	@Override
	protected void createShell() {
		Rectangle bounds = new Rectangle(100, 50, 600, 510);
		buildShell(REPORT_MIA, bounds);
		// create the composites
		createMyGroups();
	}

	private void createMyGroups() {
		createGrpClinicSelection();
		createGrpDateInfo();
	}

	/**
	 * This method initializes compHeader
	 *
	 */
	@Override
	protected void createCompHeader() {
		iDartImage icoImage = iDartImage.REPORT_STOCKCONTROLPERCLINIC;
		buildCompdHeader(" MM TB", icoImage);
	}

	/**
	 * This method initializes grpClinicSelection
	 *
	 */
	private void createGrpClinicSelection() {

		grpPharmacySelection = new Group(getShell(), SWT.NONE);
		grpPharmacySelection.setText("Farmácia");
		grpPharmacySelection.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		grpPharmacySelection.setBounds(new org.eclipse.swt.graphics.Rectangle(
				140, 90, 320, 65));

		Label lblPharmacy = new Label(grpPharmacySelection, SWT.NONE);
		lblPharmacy.setBounds(new Rectangle(10, 25, 140, 20));
		lblPharmacy.setText("Selecione a Farmácia");
		lblPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		cmbStockCenter = new CCombo(grpPharmacySelection, SWT.BORDER);
		cmbStockCenter.setBounds(new Rectangle(156, 24, 160, 20));
		cmbStockCenter.setEditable(false);
		cmbStockCenter.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbStockCenter.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

		CommonObjects.populateStockCenters(getHSession(), cmbStockCenter);

	}

	/**
	 * This method initializes grpDateInfo
	 *
	 */
	private void createGrpDateInfo() {

		grpDateInfo = new Group(getShell(), SWT.NONE);
		grpDateInfo.setBounds(new org.eclipse.swt.graphics.Rectangle(160, 180,
				280, 100));

		lblInstructions = new Label(grpDateInfo, SWT.NONE);
		lblInstructions.setBounds(new org.eclipse.swt.graphics.Rectangle(60,
				20, 160, 20));
		lblInstructions.setText("Seleccione o período:");
		lblInstructions.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		cmbMonth = new CCombo(grpDateInfo, SWT.BORDER);
		cmbMonth.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 50, 100,
				20));
		cmbMonth.setEditable(false);
		cmbMonth.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		String months[] = { "Janeiro", "Fevereiro", "Março", "Abril", "Maio",
				"Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro",
		"Dezembro" };
		for (int i = 0; i < 12; i++) {
			this.cmbMonth.add(months[i]);
		}

		int intMonth = Calendar.getInstance().get(Calendar.MONTH);
		cmbMonth.setText(mesPortugues(intMonth));
		cmbMonth.setEditable(false);
		cmbMonth.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
		cmbMonth.setVisibleItemCount(12);

		// cmdYear
		cmbYear = new CCombo(grpDateInfo, SWT.BORDER);
		cmbYear.setBounds(new org.eclipse.swt.graphics.Rectangle(160, 50, 80,
				20));
		cmbYear.setEditable(false);
		cmbYear.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
		cmbYear.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		// get the current date12
		Calendar rightNow = Calendar.getInstance();
		int currentYear = rightNow.get(Calendar.YEAR);
		for (int i = currentYear - 2; i <= currentYear + 1; i++) {
			this.cmbYear.add(Integer.toString(i));
		}
		cmbYear.setText(String.valueOf(Calendar.getInstance()
				.get(Calendar.YEAR)));
	}

	/**
	 * This method initializes compButtons
	 *
	 */
	@Override
	protected void createCompButtons() {
	}

	@SuppressWarnings("unused")
	@Override
	protected void cmdViewReportWidgetSelected() {

		StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),
				cmbStockCenter.getText());

		if (cmbStockCenter.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A Farmacia não foi seleccionada");
			missing
			.setMessage("Por favor, seleccione uma US apresentada na lista.");
			missing.open();

		} else if (pharm == null) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A Farmacia seleccionada não foi localizada");
			missing
					.setMessage("Não existe nenhuma US: '"
							+ cmbStockCenter.getText()
							+ "' na base de dados.");
			missing.open();

		} else {
			try {
				MiaReport report = new MiaReport(
						getShell(), pharm, cmbMonth.getText(), cmbYear.getText(), this.diseaseType);
				viewReport(report);
			} catch (Exception e) {
				getLog()
				.error(
						"Exception while running Monthly Receipts and Issues report",
						e);
			}
		}

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
	 * Method getMonthName.
	 *
	 * @param intMonth
	 *            int
	 * @return String
	 */
	private String getMonthName(int intMonth) {

		String strMonth = "unknown";

		SimpleDateFormat sdf1 = new SimpleDateFormat("MMMM");
		SimpleDateFormat sdf2 = new SimpleDateFormat("MM");

		try {
			Date theDate = sdf2.parse(intMonth + "");
			strMonth = sdf1.format(theDate);
		} catch (ParseException pe) {
			pe.printStackTrace();
		}

		return strMonth;

	}

	private String mesPortugues(int intMonth) {

		String mes = "";

		switch (intMonth) {
			case 0:
				mes = "Janeiro";
				break;
			case 1:
				mes = "Fevereiro";
				break;
			case 2:
				mes = "Março";
				break;
			case 3:
				mes = "Abril";
				break;
			case 4:
				mes = "Maio";
				break;
			case 5:
				mes = "Junho";
				break;
			case 6:
				mes = "Julho";
				break;
			case 7:
				mes = "Agosto";
				break;
			case 8:
				mes = "Setembro";
				break;
			case 9:
				mes = "Outubro";
				break;
			case 10:
				mes = "Novembro";
				break;
			case 11:
				mes = "Dezembro";
				break;
			default:
				mes = "";
				break;
		}

		return mes;
	}

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {

		StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),
				cmbStockCenter.getText());

		if (cmbStockCenter.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A Farmacia não foi seleccionada");
			missing
					.setMessage("Por favor, seleccione uma US apresentada na lista.");
			missing.open();

		} else if (pharm == null) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A Farmacia seleccionada não foi localizada");
			missing
					.setMessage("Não existe nenhuma US: '"
							+ cmbStockCenter.getText()
							+ "' na base de dados.");
			missing.open();

		} else {

			String reportNameFile = "Reports/MmiaReportMISAUActualizado.xls";
			try {
				MmiaReportExcel op = new MmiaReportExcel(parent, reportNameFile, cmbMonth.getText(), cmbYear.getText(),pharm);
				new ProgressMonitorDialog(parent).run(true, true, op);

				if (op.getList() == null ||
						op.getList().size() <= 0) {
					MessageBox mNoPages = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
					mNoPages.setText("O relatório não possui páginas");
					mNoPages.setMessage("O relatório que estás a gerar não contém nenhum dado.Verifique os valores de entrada que inseriu (como datas) para este relatório e tente novamente.");
					mNoPages.open();
				}

			} catch (InvocationTargetException ex) {
				ex.printStackTrace();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}

	}
}
