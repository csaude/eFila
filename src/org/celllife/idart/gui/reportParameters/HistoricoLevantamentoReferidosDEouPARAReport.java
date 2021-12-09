package org.celllife.idart.gui.reportParameters;

import model.manager.reports.HistoricoLevantamentoReferidosDEouPARA;
import org.apache.log4j.Logger;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.misc.iDARTUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarEvent;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HistoricoLevantamentoReferidosDEouPARAReport extends GenericReportGui {

    private Group grpDateRange;

    private Group grpTipoTarv;

    private SWTCalendar calendarStart;

    private SWTCalendar calendarEnd;

    private final Shell parent;

    private FileOutputStream out = null;

    /**
     * Constructor
     *
     * @param parent
     *            Shell
     * @param activate
     *            boolean
     */
    public HistoricoLevantamentoReferidosDEouPARAReport(Shell parent, boolean activate) {
        super(parent, REPORTTYPE_MIA, activate);
        this.parent = parent;
    }

    /**
     * This method initializes newMonthlyStockOverview
     */
    @Override
    protected void createShell() {
        Rectangle bounds = new Rectangle(100, 50, 600, 510);
        buildShell(REPORT_REFERIDOS_LEVANTAMENTOS_ARV, bounds);
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
        buildCompdHeader(REPORT_REFERIDOS_LEVANTAMENTOS_ARV, icoImage);
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

                HistoricoLevantamentoReferidosDEouPARA report = new HistoricoLevantamentoReferidosDEouPARA(getShell(), theStartDate, theEndDate);
                viewReport(report);
            } catch (Exception e) {
                e.printStackTrace();
                getLog().error("Exception while running Historico levantamento report",e);
            }
        }

    @Override
    protected void cmdViewReportXlsWidgetSelected() {

        if (iDARTUtil.before(calendarEnd.getCalendar().getTime(), calendarStart.getCalendar().getTime())){
            showMessage(MessageDialog.ERROR, "Data de término antes da data de início","Você selecionou uma data de término anterior à data de início.\\nSelecione uma data de término após a data de início.");
            return;
        }else
        {
            Date theStartDate = calendarStart.getCalendar().getTime();

            Date theEndDate = calendarEnd.getCalendar().getTime();

            String reportNameFile = "Reports/HistoricoLevantamentoPacientesReferidos.xls";
            try {
                HistoricoLevantamentoReferidosExcel op = new HistoricoLevantamentoReferidosExcel(parent, reportNameFile, theStartDate, theEndDate);
                new ProgressMonitorDialog(parent).run(true, true, op);

                if (op.getList() == null ||
                        op.getList().size() <= 0) {
                    MessageBox mNoPages = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);
                    mNoPages.setText("O relatório não possui páginas");
                    mNoPages.setMessage("O relatório que estás a gerar não contém nenhum dado. \\ n \\ n Verifique os valores de entrada que inseriu (como datas) para este relatório e tente novamente.");
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
     *            int
     * @return String
     */



    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }


    private void createGrpDateRange() {

        grpDateRange = new Group(getShell(), SWT.NONE);
        grpDateRange.setText("Período:");
        grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpDateRange.setBounds(new Rectangle(55, 100, 520, 201));
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
