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
import model.manager.DrugManager;
import model.manager.excel.conversion.exceptions.ReportException;
import model.manager.reports.ARVDrugUsageReport;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.Drug;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;

/**
 */
public class ARVDrugUsage extends GenericReportGui {

	private Group grpDrugSelection;

	private Group grpDateRange;

	private Label lblStartDate;

	private Label lblEndDate;

	private SWTCalendar calendarStart;

	private SWTCalendar calendarEnd;

	private Table tblDrugs;

	private TableColumn tblColDrugName;

	private TableColumn tblColPacksize;

	private Label lblSelectDrugs;

	private Group grpPharmacySelection;

	private CCombo cmbPharmacy;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public ARVDrugUsage(Shell parent, boolean activate) {
		super(parent, REPORTTYPE_STOCK, activate);
	}

	/**
	 * This method initializes newDate_DrugOrClinic
	 */
	@Override
	protected void createShell() {
		Rectangle bounds = new Rectangle(70, 50, 700, 680);
		buildShell(REPORT_ARV_DRUG_USAGE, bounds);
		createMyGroups();
	}

	private void createMyGroups() {
		createGrpDrugSelection();
		createGrpDateRange();
		createGrpPharmacySelection();
	}

	/**
	 * This method initializes compHeader
	 * 
	 */
	@Override
	protected void createCompHeader() {
		iDartImage icoImage = iDartImage.REPORT_OUTGOINGPACKAGES;
		buildCompdHeader(REPORT_ARV_DRUG_USAGE, icoImage);
	}

	private void createGrpPharmacySelection() {

		grpPharmacySelection = new Group(getShell(), SWT.NONE);
		grpPharmacySelection.setText("Unidade Sanitária");
		grpPharmacySelection.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		grpPharmacySelection.setBounds(new org.eclipse.swt.graphics.Rectangle(
				180, 70, 320, 50));

		Label lblClinic = new Label(grpPharmacySelection, SWT.NONE);
		lblClinic.setBounds(new Rectangle(6, 25, 100, 20));
		lblClinic.setText("Seleccione a US:");
		lblClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		cmbPharmacy = new CCombo(grpPharmacySelection, SWT.BORDER);
		cmbPharmacy.setBounds(new Rectangle(110, 23, 200, 20));
		cmbPharmacy.setEditable(false);
		cmbPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbPharmacy.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

		CommonObjects.populateStockCenters(getHSession(), cmbPharmacy);

	}

	/**
	 * This method initializes grpDrugSelection
	 * 
	 */
	private void createGrpDrugSelection() {

		grpDrugSelection = new Group(getShell(), SWT.NONE);
		grpDrugSelection.setText("Medicamento:");
		grpDrugSelection.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpDrugSelection.setBounds(new Rectangle(79, 330, 517, 275));

		lblSelectDrugs = new Label(grpDrugSelection, SWT.CENTER);
		lblSelectDrugs.setBounds(new Rectangle(26, 14, 456, 20));
		lblSelectDrugs.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8_ITALIC));
		lblSelectDrugs
		.setText("Por favor, seleccione no máximo 11 medicamentos");

		createTblDrugs();
	}

	private void createTblDrugs() {
		tblDrugs = new Table(grpDrugSelection, SWT.BORDER | SWT.FULL_SELECTION
				| SWT.CHECK);
		tblDrugs.setBounds(new Rectangle(30, 33, 470, 235));
		tblDrugs.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		tblDrugs.setHeaderVisible(true);

		tblColDrugName = new TableColumn(tblDrugs, SWT.NONE);
		tblColDrugName.setText("Nome do Medicamento");
		tblColDrugName.setWidth(325);

		tblColPacksize = new TableColumn(tblDrugs, SWT.NONE);
		tblColPacksize.setWidth(60);
		tblColPacksize.setText("Tamanho do frasco");

		populateTblDrugs();

	}

	private void populateTblDrugs() {
		List<Drug> drugList = DrugManager.getAllDrugsByTipoDoenca(getHSession(), iDartProperties.SERVICOTARV);

		Collections.sort(drugList);

		Iterator<Drug> iter = new ArrayList<Drug>(drugList).iterator();
		TableItem[] t = new TableItem[drugList.size()];

		String[] itemText;

		int i = 0;
		while (iter.hasNext()) {
			Drug drug = iter.next();
			t[i] = new TableItem(tblDrugs, SWT.NONE);
			itemText = new String[2];
			itemText[0] = drug.getName();
			itemText[1] = (new Integer(drug.getPackSize())).toString();
			t[i].setText(itemText);
			t[i].setData(drug);
			i++;
		}
	}

	/**
	 * This method initializes grpDateRange
	 * 
	 */
	private void createGrpDateRange() {

		grpDateRange = new Group(getShell(), SWT.NONE);
		grpDateRange.setText("Período:");
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpDateRange.setBounds(new Rectangle(79, 120, 520, 201));
		grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblStartDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblStartDate.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 30,
				180, 20));
		lblStartDate.setText("Data Início:");
		lblStartDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		lblEndDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblEndDate.setBounds(new org.eclipse.swt.graphics.Rectangle(300, 30,
				180, 20));
		lblEndDate.setText("Data Fim:");
		lblEndDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		calendarStart = new SWTCalendar(grpDateRange);
		calendarStart.setBounds(20, 55, 220, 140);

		calendarEnd = new SWTCalendar(grpDateRange);
		calendarEnd.setBounds(280, 55, 220, 140);

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
	 * Method getCalendarEnd.
	 * 
	 * @return Calendar
	 */
	public Calendar getCalendarEnd() {
		return calendarEnd.getCalendar();
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
	 * Method setEndDate.
	 * 
	 * @param date
	 *            Date
	 */
	public void setEndDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendarEnd.setCalendar(calendar);
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
	 * This method initializes compButtons
	 * 
	 */
	@Override
	protected void createCompButtons() {

		// Extra button added for clearing values from parameters
		Button btnClear = new Button(getCompButtons(), SWT.NONE);
		btnClear.setText("Limpar");
		btnClear.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnClear
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			@Override
			public void widgetSelected(
					org.eclipse.swt.events.SelectionEvent e) {
				cmdClearWidgetSelected();
			}
		});
	}

	/**
	 * Method getCheckedDrugs.
	 * 
	 * @param table
	 *            Table
	 * @return List<Drug>
	 */
	private List<Drug> getCheckedDrugs(Table table) {
		List<Drug> drugList = new ArrayList<Drug>();

		TableItem[] items = table.getItems();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				drugList.add((Drug) (items[i].getData()));
			}
		}

		return drugList;
	}

	@Override
	protected void cmdViewReportWidgetSelected() {

		StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),
				cmbPharmacy.getText());

		if (cmbPharmacy.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A US não foi seleccionada");
			missing
			.setMessage("Por favor, seleccione uma US apresentada na lista.");
			missing.open();

		} else if (pharm == null) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A US seleccionada não foi localizada");
			missing
			.setMessage("Não existe nenhuma US: '"
					+ cmbPharmacy.getText()
					+ "' na base de dados.");
			missing.open();

		} else {

			List<Drug> drugList = getCheckedDrugs(tblDrugs);
			if (drugList.size() == 0) {
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setText("Nenhumm medicamento foi adicionado");
				mb.setMessage("Por favor, seleccione no máximo 11 medicamentos. Use a caixa de selecção");
				mb.open();

			} else if (drugList.size() > 11) {
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setText("Seleccionou mais de 11 medicamentos.");
				mb.setMessage("Por favor, seleccione no máximo 11 medicamentos. Use a caixa de selecção");
				mb.open();

			} else if (calendarStart.getCalendar().getTime().after(
					calendarEnd.getCalendar()
					.getTime())) {

				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setText("Data Fim inválida");
				mb.setMessage("A data Fim não pode ser menor que a data Inicio");
				mb.open();
			}
			else {
				ARVDrugUsageReport report = new ARVDrugUsageReport(getShell(),
						cmbPharmacy.getText(),
						drugList,
						calendarStart
						.getCalendar().getTime(), calendarEnd.getCalendar()
						.getTime());
				viewReport(report);
			}


		}


	}

	/***************************************************************************
	 * This method is called when the user presses "Clear" button
	 * 
	 */
	private void cmdClearWidgetSelected() {

		TableItem[] items = tblDrugs.getItems();

		for (int i = 0; i < items.length; i++) {
			if (items[i].getChecked()) {
				items[i].setChecked(false);
			}
		}

		setStartDate(new Date());
		setEndDate(new Date());

	}

	/**
	 * This method is called when the user presses "Close" button
	 * 
	 */
	@Override
	protected void cmdCloseWidgetSelected() {
		cmdCloseSelected();
	}

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {

		StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),
				cmbPharmacy.getText());

		if (cmbPharmacy.getText().equals("")) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A US não foi seleccionada");
			missing
					.setMessage("Por favor, seleccione uma US apresentada na lista.");
			missing.open();

		} else if (pharm == null) {

			MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
					| SWT.OK);
			missing.setText("A US seleccionada não foi localizada");
			missing
					.setMessage("Não existe nenhuma US: '"
							+ cmbPharmacy.getText()
							+ "' na base de dados.");
			missing.open();

		} else {

			List<Drug> drugList = getCheckedDrugs(tblDrugs);
			if (drugList.size() == 0) {
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setText("Nenhumm medicamento foi adicionado");
				mb.setMessage("Por favor, seleccione no máximo 11 medicamentos. Use a caixa de selecção");
				mb.open();

			} else if (drugList.size() > 11) {
				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setText("Seleccionou mais de 11 medicamentos.");
				mb.setMessage("Por favor, seleccione no máximo 11 medicamentos. Use a caixa de selecção");
				mb.open();

			} else if (calendarStart.getCalendar().getTime().after(
					calendarEnd.getCalendar()
							.getTime())) {

				MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR);
				mb.setText("Data Fim inválida");
				mb.setMessage("A data Fim não pode ser menor que a data Inicio");
				mb.open();
			}
			else {

				String reportNameFile = "Reports/ARVDrugsUsage.xls";
				try {
					ARVDrugUsageExcel op = new ARVDrugUsageExcel(getShell(),cmbPharmacy.getText(),drugList,calendarStart.getCalendar().getTime(), calendarEnd.getCalendar().getTime(),reportNameFile);

					new ProgressMonitorDialog(getShell()).run(true, true, op);

					if (op.getList() == null ||
							op.getList().size() <= 0) {
						MessageBox mNoPages = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
						mNoPages.setText("O relatório não possui páginas");
						mNoPages.setMessage("O relatório que estás a gerar não contém nenhum dado.Verifique os valores de entrada que inseriu (como datas) para este relatório e tente novamente.");
						mNoPages.open();
					}

				} catch (InvocationTargetException ex) {
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}


//
//
//				List<String[]> todosDrugs = report.getARVDrugUsagePerDay();
//				List<Drug> drugsList = report.getTotalForDrugForDay()
//				System.out.println(todosDrugs);
//
//				Iterator it2 = drugList.iterator();
//				int count = 1;
//				while (it2.hasNext()) {
//					map.put("drug" + count + "Name", ((Drug) it2.next()).getName());
//					map.put("drug" + count + "Count", totalString[count]);
//					count++;
//				}

			}


		}

	}

}
