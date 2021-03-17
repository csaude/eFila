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


import model.manager.reports.HHistoricoLevantamentos;
import model.manager.reports.HistoricoLevantamentoXLS;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.Prescription;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.*;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 */
public class HistoricoLevantamentos extends GenericReportGui {
	
	private Group grpDateRange;
	
	private Group grpTipoTarv;

	private SWTCalendar calendarStart;

	private SWTCalendar calendarEnd;

	private Button chkBtnInicio;

	private Button chkBtnManutencao;
	
	private Button chkBtnAlteraccao;

	private Button chkBtnTransfereDe;

	private Button chkBtnReinicio;

	private Button chkBtnFim;
	
	private List<HistoricoLevantamentoXLS> historicoLevantamentoXLS;
	
	private final Shell parent;
	
    private FileOutputStream out = null; 

    private String diseaseType;
	/**
	 * Constructor
	 *
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public HistoricoLevantamentos(Shell parent, boolean activate, String diseaseType) {
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
		buildShell((this.diseaseType.equals(Prescription.TIPO_DOENCA_TB) ? REPORT_LIVRO_ELETRONICO_TB : REPORT_LIVRO_ELETRONICO_ARV), bounds);
		// create the composites
		createMyGroups();
	}

	private void createMyGroups() {


		createGrpDateInfo();
	}

	/**
	 * This method initializes compHeader
	 *
	 */
	@Override
	protected void createCompHeader() {
		iDartImage icoImage = iDartImage.REPORT_STOCKCONTROLPERCLINIC;
		buildCompdHeader((this.diseaseType.equals(Prescription.TIPO_DOENCA_TB) ? REPORT_LIVRO_ELETRONICO_TB : REPORT_LIVRO_ELETRONICO_ARV), icoImage);
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

		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
			showMessage(MessageDialog.ERROR, "Data de término antes da data de início","Você selecionou uma data de término anterior à data de início.\nSelecione uma data de término após a data de início.");
			return;
		}

		if (!this.diseaseType.equalsIgnoreCase(Prescription.TIPO_DOENCA_TB)){
			if (!chkBtnInicio.getSelection() && !chkBtnManutencao.getSelection() && !chkBtnAlteraccao.getSelection() &&
					!chkBtnTransfereDe.getSelection() && !chkBtnReinicio.getSelection()) {
				showMessage(MessageDialog.ERROR, "Seleccionar Tipo Tarv","Seleccione pelo menos um tipo TARV.");
				return;

			}
		}else {
			if (!chkBtnInicio.getSelection() && !chkBtnManutencao.getSelection() && !chkBtnFim.getSelection()) {
				showMessage(MessageDialog.ERROR, "Seleccionar Tipo TB","Seleccione pelo menos um tipo TB.");
				return;

			}
		}

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

			Date theStartDate = calendarStart.getCalendar().getTime();

			Date theEndDate=  calendarEnd.getCalendar().getTime();

			Calendar c = Calendar.getInstance(Locale.US);
			c.setLenient(true);
			c.setTime(theStartDate);

			if(Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)){
				c.add(Calendar.DAY_OF_WEEK, -2);
				theStartDate = c.getTime();
			}

			HHistoricoLevantamentos report = new HHistoricoLevantamentos(getShell(), theStartDate, theEndDate,chkBtnInicio.getSelection(),chkBtnManutencao.getSelection(),chkBtnAlteraccao.getSelection(), chkBtnTransfereDe.getSelection(), chkBtnReinicio.getSelection(), chkBtnFim.getSelection(),this.diseaseType);
			viewReport(report);
		} catch (Exception e) {
			getLog().error("Exception while running Historico levantamento report",e);
		}


	}
	
	@Override
	protected void cmdViewReportXlsWidgetSelected() {
				
		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
			showMessage(MessageDialog.ERROR, "Data de término antes da data de início","Você selecionou uma data de término anterior à data de início.\\nSelecione uma data de término após a data de início.");
			return;
		}

		if (!this.diseaseType.equalsIgnoreCase(Prescription.TIPO_DOENCA_TB)){
			if (!chkBtnInicio.getSelection() && !chkBtnManutencao.getSelection() && !chkBtnAlteraccao.getSelection() &&
					!chkBtnTransfereDe.getSelection() && !chkBtnReinicio.getSelection()) {
				showMessage(MessageDialog.ERROR, "Seleccionar Tipo Tarv","Seleccione pelo menos um tipo TARV.");
				return;

			}
		}else {
			if (!chkBtnInicio.getSelection() && !chkBtnManutencao.getSelection() && !chkBtnFim.getSelection()) {
				showMessage(MessageDialog.ERROR, "Seleccionar Tipo TB","Seleccione pelo menos um tipo TB.");
				return;

			}
		}
				Date theStartDate = calendarStart.getCalendar().getTime();

				Date theEndDate = calendarEnd.getCalendar().getTime();

				Calendar c = Calendar.getInstance(Locale.US);
				c.setLenient(true);
				c.setTime(theStartDate);

				if(Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)){
					c.add(Calendar.DAY_OF_WEEK, -2);
					theStartDate = c.getTime();
				}

				String reportNameFile = this.diseaseType.equals(Prescription.TIPO_DOENCA_TB) ? "Reports/HistoricoLevantamentoTB.xls" : "Reports/HistoricoLevantamento.xls";
				try {
					HistoricoLevantamentosExcel op = new HistoricoLevantamentosExcel(chkBtnInicio.getSelection(), chkBtnManutencao.getSelection(),
							chkBtnAlteraccao.getSelection(), chkBtnTransfereDe.getSelection(), chkBtnReinicio.getSelection(), chkBtnFim.getSelection(), parent, reportNameFile, theStartDate, theEndDate, this.diseaseType);
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
	 *            int
	 * @return String
	 */



	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	
	private void createGrpDateRange() {
		
		//Group tipo tarv
		grpTipoTarv = new Group(getShell(), SWT.NONE);
		grpTipoTarv.setText((this.diseaseType.equals(Prescription.TIPO_DOENCA_TB) ? "Tipo TB:" : "Tipo Tarv:"));
		grpTipoTarv.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpTipoTarv.setBounds(new Rectangle(55, 90, 520, 50));
		grpTipoTarv.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		//chk button Inicio
		chkBtnInicio = new Button(grpTipoTarv, SWT.CHECK);
		chkBtnInicio.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
		chkBtnInicio.setBounds(new Rectangle(10, 20, 100, 20));
		chkBtnInicio.setText("Início");
		chkBtnInicio.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		chkBtnInicio.setSelection(false);

		//chk button Alterar
		chkBtnAlteraccao = new Button(grpTipoTarv, SWT.CHECK);
		chkBtnAlteraccao.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
		chkBtnAlteraccao.setBounds(new Rectangle(115, 20, 100, 20));
		chkBtnAlteraccao.setText("Alteração");
		chkBtnAlteraccao.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		chkBtnAlteraccao.setSelection(false);
		chkBtnAlteraccao.setVisible(!this.diseaseType.equals(Prescription.TIPO_DOENCA_TB));

		//chk button  Manter
		chkBtnManutencao = new Button(grpTipoTarv, SWT.CHECK);
		chkBtnManutencao.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
		chkBtnManutencao.setBounds(new Rectangle(215, 20, 100, 20));
		chkBtnManutencao.setText("Manutenção");
		chkBtnManutencao.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		chkBtnManutencao.setSelection(false);

		//chk button Re-Inicio
		chkBtnReinicio = new Button(grpTipoTarv, SWT.CHECK);
		chkBtnReinicio.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
		chkBtnReinicio.setBounds(new Rectangle(315, 20, 100, 20));
		chkBtnReinicio.setText("Re-Inicio");
		chkBtnReinicio.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		chkBtnReinicio.setSelection(false);
		chkBtnReinicio.setVisible(!this.diseaseType.equals(Prescription.TIPO_DOENCA_TB));

		//chk button  Transfere de
		chkBtnTransfereDe = new Button(grpTipoTarv, SWT.CHECK);
		chkBtnTransfereDe.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
		chkBtnTransfereDe.setBounds(new Rectangle(415, 20, 100, 20));
		chkBtnTransfereDe.setText("Transferido De");
		chkBtnTransfereDe.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		chkBtnTransfereDe.setSelection(false);
		chkBtnTransfereDe.setVisible(!this.diseaseType.equals(Prescription.TIPO_DOENCA_TB));

		//chk button  FIm
		chkBtnFim = new Button(grpTipoTarv, SWT.CHECK);
		chkBtnFim.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1));
		chkBtnFim.setBounds(new Rectangle(410, 20, 100, 20));
		chkBtnFim.setText("Fim");
		chkBtnFim.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		chkBtnFim.setSelection(false);
		chkBtnFim.setVisible(this.diseaseType.equals(Prescription.TIPO_DOENCA_TB));

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
				Date date = calendarEvent.getCalendar().getTime();	}
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
