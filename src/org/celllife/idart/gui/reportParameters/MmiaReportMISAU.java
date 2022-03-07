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
import model.manager.reports.MiaReportMISAU;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.Clinic;
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

/**
 */
public class MmiaReportMISAU extends GenericReportGui {
	
	private Group grpDateRange;

	private SWTCalendar calendarStart;

	private SWTCalendar calendarEnd;

	private Group grpPharmacySelection;

	private CCombo cmbStockCenter;

	private Shell parent;

	/**
	 * Constructor
	 *
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public MmiaReportMISAU(Shell parent, boolean activate) {
		super(parent, REPORTTYPE_MIA, activate);
		this.parent = parent;
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
		buildCompdHeader("MMIA MISAU", icoImage);
	}

	/**
	 * This method initializes grpClinicSelection
	 *
	 */
	private void createGrpClinicSelection() {

		grpPharmacySelection = new Group(getShell(), SWT.NONE);
		grpPharmacySelection.setText("Farmácia");
		grpPharmacySelection.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpPharmacySelection.setBounds(new org.eclipse.swt.graphics.Rectangle(140, 90, 320, 65));

		Label lblPharmacy = new Label(grpPharmacySelection, SWT.NONE);
		lblPharmacy.setBounds(new Rectangle(10, 25, 140, 20));
		lblPharmacy.setText("Seleccione a Farmácia");
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
		
		createGrpDateRange();
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

		StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),cmbStockCenter.getText());

		if (cmbStockCenter.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			missing.setText("No Pharmacy Was Selected");
			missing.setMessage("No pharmacy was selected. Please select a pharmacy by looking through the list of available pharmacies.");
			missing.open();

		} else if (pharm == null) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			missing.setText("Pharmacy not found");
			missing.setMessage("There is no pharmacy called '"
					+ cmbStockCenter.getText()
					+ "' in the database. Please select a pharmacy by looking through the list of available pharmacies.");
			missing.open();

		}
		
		else
			
		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
			showMessage(MessageDialog.ERROR, "End date before start date","You have selected an end date that is before the start date.\nPlease select an end date after the start date.");
			return;
		}

		else {
			try {
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");
				 
				Date theStartDate = calendarStart.getCalendar().getTime();
			
				Date theEndDate=  calendarEnd.getCalendar().getTime(); 
				
				MiaReportMISAU report = new MiaReportMISAU(getShell(), pharm, theStartDate, theEndDate);
				viewReport(report);
			} catch (Exception e) {
				getLog().error("Exception while running Monthly Receipts and Issues report",e);
			}
		}

	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {
		StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),cmbStockCenter.getText());
		Clinic c = AdministrationManager.getMainClinic(getHSession());

		if (cmbStockCenter.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			missing.setText("No Pharmacy Was Selected");
			missing.setMessage("No pharmacy was selected. Please select a pharmacy by looking through the list of available pharmacies.");
			missing.open();

		} else if (pharm == null) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			missing.setText("Pharmacy not found");
			missing.setMessage("There is no pharmacy called '"
					+ cmbStockCenter.getText()
					+ "' in the database. Please select a pharmacy by looking through the list of available pharmacies.");
			missing.open();

		}

		else

		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
			showMessage(MessageDialog.ERROR, "End date before start date","You have selected an end date that is before the start date.\nPlease select an end date after the start date.");
			return;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

			Date theStartDate = calendarStart.getCalendar().getTime();

			Date theEndDate=  calendarEnd.getCalendar().getTime();
			String reportNameFile = "Reports/MmiaReportMISAU.xls";
			try {
				MmiaReportMISAUExcel op = new MmiaReportMISAUExcel(parent, reportNameFile, theStartDate, theEndDate,pharm, c);
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

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	
	private void createGrpDateRange() {
		grpDateRange = new Group(getShell(), SWT.NONE);
		grpDateRange.setText("Período:");
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpDateRange.setBounds(new Rectangle(55, 160, 520, 201));
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		Label lblStartDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblStartDate.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 30,
				180, 20));
		lblStartDate.setText("Data Início:");
		lblStartDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		Label lblEndDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblEndDate.setBounds(new org.eclipse.swt.graphics.Rectangle(300, 30,
				180, 20));
		lblEndDate.setText("Data Fim:");
		lblEndDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		calendarStart = new SWTCalendar(grpDateRange);
		calendarStart.setBounds(20, 55, 220, 140);

		calendarEnd = new SWTCalendar(grpDateRange);
		calendarEnd.setBounds(280, 55, 220, 140);
		calendarEnd.addSWTCalendarListener(new SWTCalendarListener() {
			@Override
			public void dateChanged(SWTCalendarEvent calendarEvent) {
				Date date = calendarEvent.getCalendar().getTime();
			}
		});
	}
	
	/**
	 * Method getCalendarEnd.
	 * 
	 * @return Calendar
	 */
	public Calendar getCalendarEnd() {
		return calendarEnd.getCalendar();
	}
	
	/**
	 * Method setEndDate.
	 * 
	 * @param date
	 *            Date
	 */
	public void setEndtDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendarEnd.setCalendar(calendar);
	}
	
	/**
	 * Method addEndDateChangedListener.
	 * 
	 * @param listener
	 *            SWTCalendarListener
	 */
	public void addEndDateChangedListener(SWTCalendarListener listener) {

		calendarEnd.addSWTCalendarListener(listener);
	}
	
	/**
	 * Method getCalendarStart.
	 * 
	 * @return Calendar
	 */
	public Calendar getCalendarStart() {
		return calendarStart.getCalendar();
	}
	
	/**
	 * Method setStartDate.
	 * 
	 * @param date
	 *            Date
	 */
	public void setStartDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendarStart.setCalendar(calendar);
	}
	
	/**
	 * Method addStartDateChangedListener.
	 * 
	 * @param listener
	 *            SWTCalendarListener
	 */
	public void addStartDateChangedListener(SWTCalendarListener listener) {

		calendarStart.addSWTCalendarListener(listener);
	}


}
