package org.celllife.idart.gui.reportParameters;

import model.manager.reports.FarmaciasRegistadas;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.gui.platform.GenericReportGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.lang.reflect.InvocationTargetException;

public class FarmaciasRegistadasReport  extends GenericReportGui {

    private Group grpPharmacySelection;

    private CCombo cmbStockCenter;

    private Shell parent;

    /**
     * Constructor
     *
     * @param parent Shell
     * @param activate boolean
     */
    public FarmaciasRegistadasReport(Shell parent, boolean activate) {
        super(parent, REPORTTYPE_MIA, activate);
    }

    /**
     * This method initializes newMonthlyStockOverview
     */
    @Override
    protected void createShell() {
        Rectangle bounds = new Rectangle(100, 50, 600, 510);
        buildShell(REPORT_FARMACIAS_REGISTADAS, bounds);
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
        iDartImage icoImage = iDartImage.REPORT_STOCKCONTROLPERCLINIC;
        buildCompdHeader(REPORT_FARMACIAS_REGISTADAS, icoImage);
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
        cmbStockCenter.setText(LocalObjects.currentClinic.getClinicName());
        cmbStockCenter.setEnabled(false);
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

            try {

                FarmaciasRegistadas report = new FarmaciasRegistadas( getShell());
                viewReport(report);

            } catch (Exception e) {
                e.printStackTrace();
                getLog().error("Exception while running Monthly Receipts and Issues report",e);

            }

    }

    @Override
    protected void cmdViewReportXlsWidgetSelected() {

        String reportNameFile = "Reports/FarmaciasRegistadas.xls";
        try {
            FarmaciasRegistadasExcel op = new FarmaciasRegistadasExcel(parent, reportNameFile);
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
