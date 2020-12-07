package org.celllife.idart.gui.reportParameters;

import model.manager.AdministrationManager;
import model.manager.reports.OpenmrsErrorLog;
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
import java.util.Date;

public class OpenmrsErrorLogReport extends GenericReportGui {

    private Group grpDateRange;

    private Group grpPharmacySelection;

    private CCombo cmbStockCenter;

    private SWTCalendar calendarStart;

    private SWTCalendar calendarEnd;
    
    private final Shell parent;

    /**
     * Constructor
     *
     * @param parent Shell
     * @param activate boolean
     */
    public OpenmrsErrorLogReport(Shell parent, boolean activate) {
        super(parent, REPORTTYPE_MONITORINGANDEVALUATION, activate);
        this.parent = parent;
    }

    /**
     * This method initializes newMonthlyStockOverview
     */
    @Override
    protected void createShell() {
        Rectangle bounds = new Rectangle(100, 50, 600, 510);
        buildShell(REPORT_OPENMRS_LOG, bounds);
        // create the composites
        createMyGroups();
    }

    private void createMyGroups() {
        createGrpClinicSelection();

    }

    /**
     * This method initializes compHeader
     *
     */
    @Override
    protected void createCompHeader() {
        iDartImage icoImage = iDartImage.REPORT_OUTGOINGPACKAGES;
        buildCompdHeader(REPORT_OPENMRS_LOG, icoImage);
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
        lblPharmacy.setText("Selecione a farmácia");
        lblPharmacy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        cmbStockCenter = new CCombo(grpPharmacySelection, SWT.BORDER);
        cmbStockCenter.setBounds(new Rectangle(156, 24, 160, 20));
        cmbStockCenter.setEditable(false);
        cmbStockCenter.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbStockCenter.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

        CommonObjects.populateStockCenters(getHSession(), cmbStockCenter);

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

        if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
            showMessage(MessageDialog.ERROR, "Data de término antes da data de início","Você selecionou uma data de término anterior à data de início.\nSelecione uma data de término após a data de início.");
            return;
        }else if (cmbStockCenter.getText().equals("")) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Nenhuma Farmacia foi seleccionada");
            missing
                    .setMessage("Nenhuma Farmacia foi seleccionada. Por favor, seleccione uma farmacia.");
            missing.open();

        } else if (pharm == null) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Nenhuma Farmacia foi encontrada");
            missing
                    .setMessage("Nao existe nenhuma com nome '"
                            + cmbStockCenter.getText()
                            + "' foi encontada.");
            missing.open();

        }else {
            try {

                Date theStartDate = calendarStart.getCalendar().getTime();

                Date theEndDate=  calendarEnd.getCalendar().getTime();

                OpenmrsErrorLog report = new OpenmrsErrorLog(getShell(),theStartDate,theEndDate);

                viewReport(report);
            } catch (Exception e) {
                getLog()
                        .error(
                                "Exception while running Monthly Receipts and Issues report",
                                e);

            }
        }

    }

    @Override
    protected void cmdViewReportXlsWidgetSelected() {
        StockCenter pharm = AdministrationManager.getStockCenter(getHSession(),
                cmbStockCenter.getText());

        if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
            showMessage(MessageDialog.ERROR, "Data de término antes da data de início","Você selecionou uma data de término anterior à data de início.\nSelecione uma data de término após a data de início.");
            return;
        }else if (cmbStockCenter.getText().equals("")) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Nenhuma Farmacia foi seleccionada");
            missing
                    .setMessage("Nenhuma Farmacia foi seleccionada. Por favor, seleccione uma farmacia.");
            missing.open();

        } else if (pharm == null) {

            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
                    | SWT.OK);
            missing.setText("Nenhuma Farmacia foi encontrada");
            missing
                    .setMessage("Nao existe nenhuma com nome '"
                            + cmbStockCenter.getText()
                            + "' foi encontada.");
            missing.open();

        }else {
            Date theStartDate = calendarStart.getCalendar().getTime();

            Date theEndDate=  calendarEnd.getCalendar().getTime();

            String reportNameFile = "Reports/OpenmrsErrorLog.xls";
            try {
                OpenmrsErrorLogExcel op = new OpenmrsErrorLogExcel(parent, reportNameFile, pharm, theStartDate, theEndDate);
                new ProgressMonitorDialog(parent).run(true, true, op);

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

    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }


}
