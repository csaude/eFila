/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.celllife.idart.gui.reportParameters;

import model.manager.exports.DataExportFunctions;
import model.manager.exports.DrugDispensedObjectDT;
import model.manager.exports.PackageExportObjectDT;
import model.manager.exports.columns.DrugsDispensedEnumDT;
import model.manager.exports.excel.ExcelReportObjectDT;
import model.manager.exports.excel.RowPerPatientExcelExporterDT;
import model.nonPersistent.EntitySet;
import org.apache.log4j.Logger;
import org.celllife.idart.database.hibernate.Episode;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.SafeSaveDialog;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.SQLQuery;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.util.List;
import java.util.*;

/**
 *
 * @author colaco
 */
public class CohortDispensaTrimestral extends GenericReportGui {

	private static final String FORMULA_COLUMN = "Dias entre a data esperada e o levantamento de medicamentos";

	private Group grpDateRange;

	private SWTCalendar calendarStart;

	private SWTCalendar calendarEnd;

	private CheckboxTableViewer tblColumns;
	
	private Label lblColumnTableHeader;

	private CheckboxTableViewer tblPackageColumns;
	
	private Label lblDrugTableHeader;
	
	private Link lnkSelectAllColumns;

	private Link lnkSelectAllPackageColumns;
	
	private Group grpExplanation;
	
	/**
	 * Constructor
	 * 
	 * @param parent
	 *            Shell
	 * @param activate
	 *            boolean
	 */
	public CohortDispensaTrimestral(Shell parent, boolean activate) {
		super(parent, REPORTTYPE_MIA, activate);
	}

	/**
	 * This method initializes newDate_DrugOrClinic
	 */
	@Override
	protected void createShell() {
		String shellTxt = Messages.getString("reports.cohortDT");  //$NON-NLS-1$
		Rectangle bounds = new Rectangle(70, 50, 700, 680);
		buildShell(shellTxt, bounds);
		createMyGroups();
	}

	private void createMyGroups() {
		createGrpPackageColumnsSelection();
		createGrpDateRange();
		createGrpPharmacySelection();
	}

	/**
	 * This method initializes compHeader
	 * 
	 */
	@Override
	protected void createCompHeader() {
		String headerTxt = Messages.getString("reports.cohortDT");  //$NON-NLS-1$
		iDartImage icoImage = iDartImage.REPORT_STOCKCONTROLPERCLINIC;
		buildCompdHeader(REPORT_COHORT_DISPENSA_TRIMESTRAL, icoImage);
	}

	private void createGrpPharmacySelection() {

		grpExplanation = new Group(getShell(), SWT.NONE);
		grpExplanation.setText("Descrição do Relatório");
		grpExplanation.setFont(ResourceUtils
				.getFont(iDartFont.VERASANS_8));
		grpExplanation.setBounds(new org.eclipse.swt.graphics.Rectangle(
				79, 60, 520, 60));

		Label lblClinic = new Label(grpExplanation, SWT.WRAP);
		lblClinic.setBounds(new Rectangle(6, 20, 510, 30));
		lblClinic.setText("Este relatório mostra detalhes de levantamentos de frascos para " +
				"pacientes em dispensa trimestral com visitas dentro das datas inicio e fim especificadas.");
		lblClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
	}
	
	private void createGrpPackageColumnsSelection() {
		
		lnkSelectAllColumns = new Link(getShell(), SWT.NONE);
		lnkSelectAllColumns.setBounds(new Rectangle(115, 325, 220, 30));
		lnkSelectAllColumns
		.setText("Seleccione as colunas que pretende incluir " +
				"no relatório ou <A>seleccionar todas</A> colunas");
		lnkSelectAllColumns
		.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
		lnkSelectAllColumns.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				tblColumns.setAllChecked(true);
			}
		});

		lnkSelectAllPackageColumns = new Link(getShell(), SWT.NONE);
		lnkSelectAllPackageColumns.setBounds(new Rectangle(375, 325, 220, 30));
		lnkSelectAllPackageColumns
		.setText("Seleccione as colunas que pretende incluir " +
				" no relatório ou <A>Seleccionar tudo</A>");
		lnkSelectAllPackageColumns
		.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
		lnkSelectAllPackageColumns.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				tblPackageColumns.setAllChecked(true);
			}
		});
		
		createTblPackages();
	}

	private void createTblPackages() {
		
		lblColumnTableHeader = new Label(getShell(), SWT.BORDER);
		lblColumnTableHeader.setBounds(new Rectangle(120, 360, 200, 20));
		lblColumnTableHeader.setText("Nome da Coluna");
		lblColumnTableHeader.setAlignment(SWT.CENTER);
		lblColumnTableHeader.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		tblColumns = CheckboxTableViewer.newCheckList(getShell(), SWT.BORDER);
		tblColumns.getTable().setBounds(
				new org.eclipse.swt.graphics.Rectangle(120, 380, 200, 200));
		tblColumns.getTable().setFont(
				ResourceUtils.getFont(iDartFont.VERASANS_8));
		tblColumns.setContentProvider(new ArrayContentProvider());
		tblColumns.setInput(getColumns());
		tblColumns.setCheckedElements(DrugsDispensedEnumDT.getDefaults());

		lblDrugTableHeader = new Label(getShell(), SWT.BORDER);
		lblDrugTableHeader.setBounds(new Rectangle(370, 360, 200, 20));
		lblDrugTableHeader.setText("Colunas Extras");
		lblDrugTableHeader.setAlignment(SWT.CENTER);
		lblDrugTableHeader.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		
		tblPackageColumns = CheckboxTableViewer.newCheckList(getShell(), SWT.BORDER);
		tblPackageColumns.getTable().setBounds(
				new org.eclipse.swt.graphics.Rectangle(370, 380, 200, 200));
		tblPackageColumns.getTable().setFont(
				ResourceUtils.getFont(iDartFont.VERASANS_8));
		tblPackageColumns.setContentProvider(new ArrayContentProvider());
		tblPackageColumns.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent arg0) {
				Object element = arg0.getElement();
				if (!arg0.getChecked() && Arrays.asList(getDefaults()).contains(element)) {
					tblPackageColumns.setChecked(element, true);
				}
			}
		});

		tblPackageColumns.setInput(getPackageColumns());
		tblPackageColumns.setCheckedElements(getDefaults());
		tblPackageColumns.setGrayedElements(getDefaults());
	}

	private Object[] getDefaults() {
		return new Object[] { DrugsDispensedEnumDT.dateDispensed};
		}

	private Object getPackageColumns() {
		return new Object[] { DrugsDispensedEnumDT.dateDispensed};
	}

	private Object getColumns() {
		return new Object[] { DrugsDispensedEnumDT.patientId,
				DrugsDispensedEnumDT.patientFirstName,
				DrugsDispensedEnumDT.patientLastName, DrugsDispensedEnumDT.sex,
				DrugsDispensedEnumDT.age
                };
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

		Label lblStartDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblStartDate.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 30,
				180, 20));
		lblStartDate.setText("Seleccione a Data Inicial:");
		lblStartDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		Label lblEndDate = new Label(grpDateRange, SWT.CENTER | SWT.BORDER);
		lblEndDate.setBounds(new org.eclipse.swt.graphics.Rectangle(300, 30,
				180, 20));
		lblEndDate.setText("Seleccione a Data Final:");
		lblEndDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		calendarStart = new SWTCalendar(grpDateRange);
		calendarStart.setBounds(20, 55, 220, 140);

		calendarEnd = new SWTCalendar(grpDateRange);
		calendarEnd.setBounds(280, 55, 220, 140);
		calendarEnd.addSWTCalendarListener(new SWTCalendarListener() {
			@Override
			public void dateChanged(SWTCalendarEvent calendarEvent) {
				Date date = calendarEvent.getCalendar().getTime();
				DrugsDispensedEnumDT.setEndDate(date);
				tblColumns.refresh();
			}
		});
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

	@Override
	protected void cmdViewReportWidgetSelected() {

		if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
			showMessage(MessageDialog.ERROR, "Data Fim antes da Data Inicio","Seleccionou data fim menor que a data Inicio.");
			return;
		}
		
		SafeSaveDialog dialog = new SafeSaveDialog(getShell(), SafeSaveDialog.FileType.EXCEL);
		String path = "";
		dialog.setFileName("Cohort_Dispensa_Trimestral");
		path = dialog.open();

		if (path != null) {
			ExcelReportObjectDT reportObject = getColumnsFromTables(path);
			EntitySet patients = getPatientSet(reportObject);
			if (patients.size() <= 0){
				showMessage(
				MessageDialog.INFORMATION,"Sem Pacientes","Nenhum paciente com visita inicial neste periodo '");
				return;
			}
			 viewReport(new ExcelReportJobDT(reportObject, new RowPerPatientExcelExporterDT(patients)));
			showMessage(MessageDialog.INFORMATION, "Relatório executado com sucesso","Relatório executado com sucesso.\n\n" + reportObject.getPath());
		}
	}

	@Override
	protected void cmdViewReportXlsWidgetSelected() {

	}

	private EntitySet getPatientSet(ExcelReportObjectDT report) {
	//	String patientQuery = "select e.patient.id from Episode e where e.startDate between :startDate and :endDate" +
	//	" and e.startReason = :startReason";
                String patientQuery = "select episode.patient from episode \n" +
                                        "inner join prescription on prescription.patient = episode.patient \n" +
                                        "where prescription.dispensatrimestral = 1 and prescription.tipodoenca like '%ARV' "+
                                        "AND episode.startDate between :startDate and :endDate and episode.startReason = :startReason";
		//Query query = getHSession().createQuery(patientQuery);
                SQLQuery query = getHSession().createSQLQuery(patientQuery);
		query.setString("startReason", Episode.REASON_NEW_PATIENT);
		query.setTimestamp("startDate", report.getStartDate());
		query.setTimestamp("endDate", report.getEndDate());
		@SuppressWarnings("unchecked")
		List<Integer> patients = query.list();
		return new EntitySet(patients);
	}

	/**
	 * This method creates the report object from the selected 
	 * values from the table.
	 * @param path
	 */
	private ExcelReportObjectDT getColumnsFromTables(String path) {
		ExcelReportObjectDT exr = new ExcelReportObjectDT();
		List<PackageExportObjectDT> allColumns = new ArrayList<PackageExportObjectDT>();
		Object[] obj = tblColumns.getCheckedElements();
		for(int i = 0; i < obj.length; i++) {
			if (obj[i] instanceof DrugsDispensedEnumDT){
				DrugsDispensedEnumDT enu = (DrugsDispensedEnumDT) obj[i];
				DrugDispensedObjectDT ddo = new DrugDispensedObjectDT(enu);
				allColumns.add(ddo);
			} 
//                        else if (obj[i] instanceof EpisodeDetailsEnum){
//				EpisodeDetailsEnum enu = (EpisodeDetailsEnum) obj[i];
//				EpisodeObject ddo = new EpisodeObject(enu, calendarStart.getCalendar().getTime(),calendarEnd.getCalendar().getTime());
//				allColumns.add(ddo);
//			}
		}
		
		List<PackageExportObjectDT> endcolumns = new ArrayList<PackageExportObjectDT>();
		Object[] obj2 = tblPackageColumns.getCheckedElements();
		for(int i = 0; i < obj2.length; i++) {
			if (obj2[i] instanceof DrugsDispensedEnumDT){
				DrugsDispensedEnumDT enu = (DrugsDispensedEnumDT) obj2[i];
				DrugDispensedObjectDT ddo = new DrugDispensedObjectDT(enu);
				endcolumns.add(ddo);
			} else if (obj2[i] instanceof String){
				DrugDispensedObjectDT diff = new DrugDispensedObjectDT(){
					@Override
					public Object getData(DataExportFunctions functions, int index) {
						String previousColumn = iDARTUtil.columnIndexToLetterNotation(currentColumnIndex-1, true);
						String nextColumn = iDARTUtil.columnIndexToLetterNotation(currentColumnIndex+1, true);
						String nextCell = nextColumn + (rowCounter+1);
						String previousCell = previousColumn + (rowCounter+1);
						String formula = "IF(NOT(ISBLANK("+nextCell+")),ROUND(" + nextCell + "-" + previousCell + ",1),\"\")";
						return new jxl.write.Formula(currentColumnIndex, rowCounter, formula);
					}
					
				};
				diff.setColumnWidth(17);
				diff.setColumnIndex(-1);
				diff.setTitle("Dias de atraso");
				endcolumns.add(diff);
			}
		}
		
		exr.setColumns(allColumns);
		exr.setEndColumns(endcolumns);
		exr.setEndDate(iDARTUtil.getEndOfDay(calendarEnd.getCalendar().getTime()));
		exr.setPath(path);
		exr.setStartDate(iDARTUtil.getBeginningOfDay(calendarStart.getCalendar().getTime()));
		
		return exr;
		
	}

	/***************************************************************************
	 * This method is called when the user presses "Clear" button
	 * 
	 */
	private void cmdClearWidgetSelected() {
		setStartDate(new Date());
		setEndDate(new Date());
		tblPackageColumns.setAllChecked(false);
		tblColumns.setAllChecked(false);
		tblColumns.setGrayedElements(DrugsDispensedEnumDT.getCompulsory().toArray());
		tblColumns.setCheckedElements(DrugsDispensedEnumDT.getCompulsory().toArray());
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
	
	
}
