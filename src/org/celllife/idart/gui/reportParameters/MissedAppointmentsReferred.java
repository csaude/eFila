package org.celllife.idart.gui.reportParameters;

import model.manager.AdministrationManager;
import model.manager.reports.MissedAppointmentsReportNew;
import model.manager.reports.MissedAppointmentsReportReferred;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.platform.GenericReportGuiInterface;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.vafada.swtcalendar.SWTCalendar;
import org.vafada.swtcalendar.SWTCalendarListener;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MissedAppointmentsReferred extends GenericReportGui {

    private Group grpClinicSelection;

    private Label lblClinic;

    private CCombo cmbClinic;

    private Label lblMinimumDaysLate;

    private Text txtMinimumDaysLate;

    private Text txtMaximumDaysLate;

    private Label lblMaximumDaysLate;

    private Group grpDateRange;

    private final Shell parent;

    private SWTCalendar swtCal;

    /**
     * Constructor
     *
     * @param parent
     *            Shell
     * @param activate
     *            boolean
     */
    public MissedAppointmentsReferred(Shell parent, boolean activate) {
        super(parent, GenericReportGuiInterface.REPORTTYPE_CLINICMANAGEMENT,
                activate);
        this.parent = parent;
    }

    /**
     * This method initializes newMonthlyStockOverview
     */
    @Override
    protected void createShell() {
        buildShell(REPORT_MISSED_APPOINTMENTS_FROM_OTHER_PHARM, new Rectangle(100, 50, 600,
                510));
        // create the composites
        createMyGroups();
    }

    private void createMyGroups() {
        createGrpClinicSelection();
        createGrpDateRange();
    }

    /**
     * This method initializes compHeader
     *
     */
    @Override
    protected void createCompHeader() {
        iDartImage icoImage = iDartImage.REPORT_PATIENTDEFAULTERS;
        buildCompdHeader(REPORT_MISSED_APPOINTMENTS_FROM_OTHER_PHARM, icoImage);
    }

    /**
     * This method initializes grpClinicSelection
     *
     */
    private void createGrpClinicSelection() {

        grpClinicSelection = new Group(getShell(), SWT.NONE);
        grpClinicSelection.setText("Configuração do Relatório de Pacientes Referidos e Faltosos ao Levantamento de ARV");
        grpClinicSelection.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpClinicSelection.setBounds(new Rectangle(60, 79, 465, 114));

//        lblClinic = new Label(grpClinicSelection, SWT.NONE);
//        lblClinic.setBounds(new Rectangle(30, 25, 167, 20));
//        lblClinic.setText("US");
//        lblClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
//
//        cmbClinic = new CCombo(grpClinicSelection, SWT.BORDER);
//        cmbClinic.setBounds(new Rectangle(202, 25, 160, 20));
//        cmbClinic.setEditable(false);
//        cmbClinic.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
//        cmbClinic.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
//
//        CommonObjects.populateClinics(getHSession(), cmbClinic);

        lblMinimumDaysLate = new Label(grpClinicSelection, SWT.NONE);
        lblMinimumDaysLate.setBounds(new Rectangle(31, 57, 147, 21));
        lblMinimumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblMinimumDaysLate.setText("Dias Mínimos de Atraso:");

        txtMinimumDaysLate = new Text(grpClinicSelection, SWT.BORDER);
        txtMinimumDaysLate.setBounds(new Rectangle(201, 56, 45, 20));
        txtMinimumDaysLate.setText("5");
        txtMinimumDaysLate.setEditable(true);
        txtMinimumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        lblMaximumDaysLate = new Label(grpClinicSelection, SWT.NONE);
        lblMaximumDaysLate.setBounds(new Rectangle(31, 86, 150, 21));
        lblMaximumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblMaximumDaysLate.setText("Dias Máxímos de Atraso:");

        txtMaximumDaysLate = new Text(grpClinicSelection, SWT.BORDER);
        txtMaximumDaysLate.setBounds(new Rectangle(202, 86, 43, 19));
        txtMaximumDaysLate.setText("9");
        txtMaximumDaysLate.setEditable(true);
        txtMaximumDaysLate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

    }

    /**
     * This method initializes grpDateRange
     *
     */
    private void createGrpDateRange() {

        grpDateRange = new Group(getShell(), SWT.NONE);
        grpDateRange.setText("Seleccione a data de reporte:");
        grpDateRange.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        grpDateRange.setBounds(new Rectangle(142, 214, 309, 211));

        swtCal = new SWTCalendar(grpDateRange);
        swtCal.setBounds(40, 40, 220, 160);

    }

    /**
     * Method getCalendarDate.
     *
     * @return Calendar
     */
    public Calendar getCalendarDate() {
        return swtCal.getCalendar();
    }

    /**
     * Method setCalendarDate.
     *
     * @param date
     *            Date
     */
    public void setCalendarDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        swtCal.setCalendar(calendar);
    }

    /**
     * Method addDateChangedListener.
     *
     * @param listener
     *            SWTCalendarListener
     */
    public void addDateChangedListener(SWTCalendarListener listener) {

        swtCal.addSWTCalendarListener(listener);
    }

    /**
     * This method initializes compButtons
     *
     */
    @Override
    protected void createCompButtons() {
    }

    @Override
    protected void cmdViewReportWidgetSelected() {

        boolean viewReport = true;
        int max = 0;
        int min = 0;

//        if (cmbClinic.getText().equals("")) {
//
//            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR
//                    | SWT.OK);
//            missing.setText("Nenhuma US selecionada");
//            missing
//                    .setMessage("Nenhuma US selecionada. Por favor selecione a Unidade Sanitaria.");
//            missing.open();
//            viewReport = false;
//
//        }

        if (txtMinimumDaysLate.getText().equals("")
                || txtMaximumDaysLate.getText().equals("")) {
            MessageBox incorrectData = new MessageBox(getShell(),
                    SWT.ICON_ERROR | SWT.OK);
            incorrectData.setText("Informacao Invalida");
            incorrectData
                    .setMessage("Os dias Maximo e Minimo devem ser numeros.");
            incorrectData.open();
            txtMinimumDaysLate.setText("");
            txtMinimumDaysLate.setFocus();
            viewReport = false;
        } else if (!txtMinimumDaysLate.getText().equals("")
                && !txtMaximumDaysLate.getText().equals("")) {
            try {
                min = Integer.parseInt(txtMinimumDaysLate.getText());
                max = Integer.parseInt(txtMaximumDaysLate.getText());

                if ((min < 0) || (max < 0)) {
                    MessageBox incorrectData = new MessageBox(getShell(),
                            SWT.ICON_ERROR | SWT.OK);
                    incorrectData.setText("Informacao Invalida");
                    incorrectData
                            .setMessage("Os dias Maximo e Minimo devem ser numeros.");
                    incorrectData.open();
                    txtMinimumDaysLate.setText("");
                    txtMinimumDaysLate.setFocus();

                    viewReport = false;
                }

                if (min >= max) {
                    MessageBox incorrectData = new MessageBox(getShell(),
                            SWT.ICON_ERROR | SWT.OK);
                    incorrectData.setText("Informacao Invalida");
                    incorrectData
                            .setMessage("O Minimo dia deve ser menor que o maximo dia.");
                    incorrectData.open();
                    txtMinimumDaysLate.setFocus();

                    viewReport = false;
                }

            } catch (NumberFormatException nfe) {
                MessageBox incorrectData = new MessageBox(getShell(),
                        SWT.ICON_ERROR | SWT.OK);
                incorrectData.setText("Informacao Invalida");
                incorrectData
                        .setMessage("Os dias Maximo e Minimo devem ser numeros.");
                incorrectData.open();
                txtMinimumDaysLate.setText("");
                txtMinimumDaysLate.setFocus();

                viewReport = false;

            }
        }

        if (viewReport) {

            Clinic c = AdministrationManager.getMainClinic(HibernateUtil.getNewSession());

            MissedAppointmentsReportReferred report = new MissedAppointmentsReportReferred(
                    getShell(),c.getClinicName(),
                    Integer.parseInt(txtMinimumDaysLate.getText()),
                    Integer.parseInt(txtMaximumDaysLate.getText()),
                    swtCal.getCalendar().getTime());
            viewReport(report);
        }

    }

    @Override
    protected void cmdViewReportXlsWidgetSelected() {

        boolean viewReport = true;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");

        //	ConexaoJDBC con = new ConexaoJDBC();

        int max = 0;
        int min = 0;

        if (cmbClinic.getText().equals("")) {
            MessageBox missing = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            missing.setText("Nenhuma US selecionada");
            missing.setMessage("Nenhuma US selecionada. Por favor selecione a Unidade Sanitaria.");
            missing.open();
            viewReport = false;
        }


        if (txtMinimumDaysLate.getText().equals("") || txtMaximumDaysLate.getText().equals("")) {
            MessageBox incorrectData = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            incorrectData.setText("Informacao Invalida");
            incorrectData.setMessage("Os dias Maximo e Minimo devem ser numeros.");
            incorrectData.open();
            txtMinimumDaysLate.setText("");
            txtMinimumDaysLate.setFocus();
            viewReport = false;
        } else if (!txtMinimumDaysLate.getText().equals("") && !txtMaximumDaysLate.getText().equals("")) {

            try {

                min = Integer.parseInt(txtMinimumDaysLate.getText());
                max = Integer.parseInt(txtMaximumDaysLate.getText());

                if ((min < 0) || (max < 0)) {
                    MessageBox incorrectData = new MessageBox(getShell(),
                            SWT.ICON_ERROR | SWT.OK);
                    incorrectData.setText("Informacao Invalida");
                    incorrectData.setMessage("Os dias Maximo e Minimo devem ser numeros.");
                    incorrectData.open();
                    txtMinimumDaysLate.setText("");
                    txtMinimumDaysLate.setFocus();
                    viewReport = false;
                }

                if (min >= max) {
                    MessageBox incorrectData = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                    incorrectData.setText("Informacao Invalida");
                    incorrectData.setMessage("O Minimo dia deve ser menor que o maximo dia.");
                    incorrectData.open();
                    txtMinimumDaysLate.setFocus();
                    viewReport = false;
                }

            } catch (NumberFormatException nfe) {
                MessageBox incorrectData = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                incorrectData.setText("Informacao Invalida");
                incorrectData.setMessage("Os dias Maximo e Minimo devem ser numeros.");
                incorrectData.open();
                txtMinimumDaysLate.setText("");
                txtMinimumDaysLate.setFocus();
                viewReport = false;
            }
        }

        if (viewReport) {
            String	reportNameFile = "Reports/FaltosoLevantamentoARV.xls";
            try {
                MissedAppointmentsNewExcel op = new MissedAppointmentsNewExcel(swtCal, parent, reportNameFile, txtMinimumDaysLate.getText(), txtMaximumDaysLate.getText());
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

    @Override
    protected void setLogger() {
        setLog(Logger.getLogger(this.getClass()));
    }
}
