package org.celllife.idart.gui.clinicSector;

import model.manager.AdministrationManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.ClinicSector;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.rest.utils.RestFarmac;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class UploadClinicSector extends GenericFormGui {


    private Group grpClinicSearch;

    private Group grpClinics;

    private CCombo cmbClinicSectorType;

    private Link lnkSelectAllColumns;

    private CheckboxTableViewer tblColumns;

    private Button btnSearch;

    private Label lblColumnTableHeader;

    private Label lblClinicTableHeader;

    List<Clinic> restClinics = null;


    /**
     * Constructor for GenericFormGui.
     *
     * @param parent Shell
     */
    public UploadClinicSector(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
    }

    /**
     * This method initializes newClinic
     */
    @Override
    protected void createShell() {

        String shellTxt = "Exportar Sectores Clínicos";
        Rectangle bounds = new Rectangle(100, 100, 600, 560);

        // Parent Generic Methods ------
        buildShell(shellTxt, bounds); // generic shell build
    }

    @Override
    protected void createContents() {
        createGrpClinicSearch();
        createGrpClinicColumnsSelection();
    }

    @Override
    protected void createCompHeader() {
        String headerTxt = "Exportar Sectores Clínicos";
        iDartImage icoImage = iDartImage.CLINIC;
        buildCompHeader(headerTxt, icoImage);
        Rectangle bounds = getCompHeader().getBounds();
        bounds.width = 720;
        bounds.x -= 40;
        getCompHeader().setBounds(bounds);
    }

    @Override
    protected void createCompButtons() {
        buildCompButtons();
    }


    /**
     * This method initializes grpClinicInfo
     */
    private void createGrpClinicSearch() {

        // grpClinicInfo
        grpClinicSearch = new Group(getShell(), SWT.NONE);
        grpClinicSearch.setBounds(new Rectangle(33, 70, 500, 120));

        Label lblInstructions = new Label(grpClinicSearch, SWT.CENTER);
        lblInstructions.setBounds(new Rectangle(70,
                15, 260, 20));
        lblInstructions.setText("Todos campos marcados com * são obrigatorios");
        lblInstructions.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8_ITALIC));

        cmbClinicSectorType = new CCombo(grpClinicSearch, SWT.BORDER | SWT.READ_ONLY);
        cmbClinicSectorType.setBounds(new Rectangle(145, 50, 220, 20));
        cmbClinicSectorType.setEditable(false);
        cmbClinicSectorType.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbClinicSectorType.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbClinicSectorType.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        CommonObjects.populateClinicSectorType(getHSession(), cmbClinicSectorType);
        cmbClinicSectorType.setVisibleItemCount(cmbClinicSectorType.getItemCount());
        cmbClinicSectorType.setText("Selecione ...");

        // btnSearch
        btnSearch = new Button(grpClinicSearch, SWT.NONE);
        btnSearch.setBounds(new Rectangle(157, 80, 152, 30));
        btnSearch.setText("Procurar");
        btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(
                    SelectionEvent e) {
                populateClinicSector();
            }
        });
        btnSearch.setToolTipText("Pressione para procurar uma Farmácia.");
    }

    @Override
    protected void clearForm() {
        cmbClinicSectorType.setText("Selecione ...");
        tblColumns.setAllChecked(false);

    }

    @Override
    protected boolean submitForm() {
        return false;
    }

    @Override
    protected boolean fieldsOk() {
        boolean fieldsOkay = true;

         if (cmbClinicSectorType.getText().trim().equals("") || cmbClinicSectorType.getText().trim().equals("Selecione ...")) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" O campo tipo de sector nao pode estar em branco.");
            b.setText("Campos em branco");
            b.open();
            cmbClinicSectorType.setFocus();
            fieldsOkay = false;
        } else if(tblColumns.getCheckedElements().length <= 0){
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" Nenhum sector selecionado na lista ");
            b.setText("Nenhum sector selecionado na lista");
            b.open();
            fieldsOkay = false;
        }

        return fieldsOkay;
    }

    @Override
    protected void cmdSaveWidgetSelected() {
        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
        String url = CentralizationProperties.centralized_server_url;
        ArrayList<ClinicSector> clinicSectorList = new ArrayList<>();
        String result = "";

        if (fieldsOk()) {

            try {
                Object[] obj = tblColumns.getCheckedElements();
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] instanceof ClinicSector) {

                        ClinicSector clinicSector = (ClinicSector) obj[i];
                        clinicSectorList.add(clinicSector);
                    }
                }
                if(clinicSectorList.isEmpty()){
                    showMessage( MessageDialog.INFORMATION, "Lista de Sector vazia",
                            "A lista de Sectores clinicos esta vazia, por favor, registe um ou mais sectores clinicos.");
                } else {
                    RestFarmac.restPostLocalClinic(hSession, url, pool);
                    result = RestFarmac.restPostLocalClinicSector(hSession, url, pool, clinicSectorList);

                    if(result.trim().isEmpty())
                        result = "Carregamento de sectores clinicos efectuado com sucesso";

                    showMessage(MessageDialog.INFORMATION, "Carregamento de sectores clinicos",
                                result);
                }
            } catch (Exception he) {
                showMessage(
                        MessageDialog.ERROR,
                        "Problemas ao gravar a informação",
                        "Problemas ao gravar a informação. Por favor, tente novamente.");
            }
            cmdCancelWidgetSelected();
        }
    }

    @Override
    protected void cmdClearWidgetSelected() {
        clearForm();
    }

    @Override
    protected void cmdCancelWidgetSelected() {
        cmdCloseSelected();
    }

    @Override
    protected void enableFields(boolean enable) {
        cmbClinicSectorType.setEnabled(enable);
        btnSave.setEnabled(enable);
    }

    @Override
    protected void setLogger() {

    }

    private void createGrpClinicColumnsSelection() {

        lblClinicTableHeader = new Label(getShell(), SWT.BORDER);
        lblClinicTableHeader.setBounds(new Rectangle(200, 200, 200, 20));
        lblClinicTableHeader.setText("Lista de Sectores Clínicos");
        lblClinicTableHeader.setAlignment(SWT.CENTER);
        lblClinicTableHeader.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));


        lnkSelectAllColumns = new Link(getShell(), SWT.NONE);
        lnkSelectAllColumns.setBounds(new Rectangle(70, 230, 450, 20));
        lnkSelectAllColumns
                .setText("Por favor, seleccione os sectores clínicos que pretende exportar " +
                        "ou <A>Seleccionar todas</A> colunas");
        lnkSelectAllColumns
                .setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
        lnkSelectAllColumns.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                tblColumns.setAllChecked(true);
            }
        });

        createTblClinic();
    }

    private void createTblClinic() {

        tblColumns = CheckboxTableViewer.newCheckList(getShell(), SWT.BORDER);
        tblColumns.getTable().setBounds(
                new Rectangle(85, 250, 420, 150));
        tblColumns.getTable().setFont(
                ResourceUtils.getFont(iDartFont.VERASANS_8));
        tblColumns.setContentProvider(new ArrayContentProvider());
    }

    private void populateClinicSector() {
        List<ClinicSector> clinicSectors = AdministrationManager.getClinicSectorBySectorType(getHSession(),cmbClinicSectorType.getText());
        tblColumns.setInput(clinicSectors);

        if(clinicSectors.isEmpty()){
            btnSave.setEnabled(false);
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" Nenhum resultado foi encontrado ");
            b.setText("Nenhum resultado foi encontrado");
            b.open();
        }else
            btnSave.setEnabled(true);
    }

}
