package org.celllife.idart.gui.regimeTerapeutico;

import model.manager.DeletionsManager;
import model.manager.DrugManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.Drug;
import org.celllife.idart.database.hibernate.RegimeTerapeutico;
import org.celllife.idart.database.hibernate.RegimenDrugs;
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
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class DownLoadRegimeTerapeutico extends GenericFormGui {


    private Group grpRegimenInfo;

    private Label lblStatusRegimen;

    private Button rdBtnActive;

    private Button rdBtnInactive;

    private Link lnkSelectAllColumns;

    private CheckboxTableViewer tblColumns;

    private Label lblRegimenTableHeader;

    private Button btnSearch;

    private List<RegimeTerapeutico> restRegimeTerapeutico = new ArrayList<RegimeTerapeutico>();


    /**
     * Constructor for GenericFormGui.
     *
     * @param parent Shell
     */
    public DownLoadRegimeTerapeutico(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
    }

    /**
     * This method initializes newClinic
     */
    @Override
    protected void createShell() {

        String shellTxt = "Importar Regime Terapeutico";
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
        String headerTxt = "Importar Regime Terapeutico";
        iDartImage icoImage = iDartImage.DRUGGROUP;
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

        // grpDrugInfo
        grpRegimenInfo = new Group(getShell(), SWT.NONE);
        grpRegimenInfo.setBounds(new Rectangle(33, 70, 500, 170));
        // grpDrugInfo = new Group(getShell(), SWT.NONE);
        // grpDrugInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        // grpDrugInfo.setText("Drug Details");
        // grpDrugInfo.setBounds(new Rectangle(18, 110, 483, 293));

        Label lblInstructions = new Label(grpRegimenInfo, SWT.CENTER);
        lblInstructions.setBounds(new org.eclipse.swt.graphics.Rectangle(70,
                15, 260, 20));
        lblInstructions.setText("Todos campos marcados com * são obrigatorios");
        lblInstructions.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8_ITALIC));


        // lblEstadoDrug, rdBtnActive & rdBtnInactive
        Label lblEstadoDrug = new Label(grpRegimenInfo, SWT.NONE);
        lblEstadoDrug.setBounds(new org.eclipse.swt.graphics.Rectangle(40, 80, 80, 20));
        lblEstadoDrug.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblEstadoDrug.setText("* Estado :");

        rdBtnActive = new Button(grpRegimenInfo, SWT.RADIO);
        rdBtnActive.setBounds(new Rectangle(150, 80, 50, 20));
        rdBtnActive.setText("Activo");
        rdBtnActive.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnActive.setSelection(true);

        rdBtnInactive = new Button(grpRegimenInfo, SWT.RADIO);
        rdBtnInactive.setBounds(new Rectangle(257, 80, 80, 20));
        rdBtnInactive.setText("Inactivo");
        rdBtnInactive.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnInactive.setSelection(false);

        // btnSearch
        btnSearch = new Button(grpRegimenInfo, SWT.NONE);
        btnSearch.setBounds(new Rectangle(157, 110, 152, 30));
        btnSearch.setText("Procurar");
        btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        btnSearch.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(
                    org.eclipse.swt.events.SelectionEvent e) {
                populateRestRegimen();
            }
        });
        btnSearch.setToolTipText("Pressione para procurar Medicamentos.");
    }

    @Override
    protected void clearForm() {
        rdBtnActive.setSelection(true);
        rdBtnInactive.setSelection(false);
        tblColumns.setAllChecked(false);

    }

    @Override
    protected boolean submitForm() {
        return false;
    }

    @Override
    protected boolean fieldsOk() {
        boolean fieldsOkay = true;

        if (tblColumns.getCheckedElements().length <= 0) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" Nenhum Regime Terapêutico foi seleccionado na lista ");
            b.setText("Nenhum Regime Terapêutico foi seleccionado na lista");
            b.open();
            fieldsOkay = false;
        }

        return fieldsOkay;
    }

    @Override
    protected void cmdSaveWidgetSelected() {

        if (fieldsOk()) {


            Object[] obj = tblColumns.getCheckedElements();
            for (int i = 0; i < obj.length; i++) {
                if (obj[i] instanceof RegimeTerapeutico) {
                    Transaction tx = null;
                    Session session = HibernateUtil.getNewSession();
                    tx = session.beginTransaction();
                    try {
                        RegimeTerapeutico newRegimen = (RegimeTerapeutico) obj[i];
                        RegimeTerapeutico newRegimenSave = DrugManager.getRegimeTerapeutico(session, newRegimen.getRegimeesquema());

                        List<RegimenDrugs> regimenDrugsList = newRegimen.getRegimenDrugs();
                        List<RegimenDrugs> regimenDrugsListNew = new ArrayList<RegimenDrugs>();

                        if (newRegimenSave != null) {
                            newRegimenSave.getRegimenDrugs().clear();
                            DeletionsManager.removeRegimeTerapeuicoDrugs(session, newRegimenSave);
                        } else {
                            newRegimenSave = new RegimeTerapeutico();
                        }

                        if (!regimenDrugsList.isEmpty()) {
                            for (RegimenDrugs regimenDrugs : regimenDrugsList) {
                                Drug drug = DrugManager.getDrugyAtccode(session, regimenDrugs.getDrug().getAtccode());

                                if (drug == null) {
                                    drug = DrugManager.getDrug(session, regimenDrugs.getDrug().getName());
                                    if (drug == null) {
                                        drug = regimenDrugs.getDrug();
                                        drug.setId(null);
                                        DrugManager.saveDrug(session, drug);
                                    } else {
                                        drug.setActive(regimenDrugs.getDrug().isActive());
                                        drug.setAtccode(regimenDrugs.getDrug().getAtccode());
                                        DrugManager.updateDrug(session, drug);
                                    }
                                }
                                RegimenDrugs newRD = new RegimenDrugs();
                                newRD.setAmtPerTime(drug.getDefaultAmnt());
                                newRD.setDrug(drug);
                                newRD.setModified(regimenDrugs.getModified());
                                newRD.setRegimen(newRegimenSave);
                                newRD.setTimesPerDay(drug.getDefaultTimes());
                                regimenDrugsListNew.add(newRD);
                            }
                        }

                        newRegimenSave.setActive(newRegimen.isActive());
                        newRegimenSave.setRegimeesquema(newRegimen.getRegimeesquema());
                        newRegimenSave.setCodigoregime(newRegimen.getCodigoregime());
                        newRegimenSave.setRegimeesquemaidart(newRegimen.getRegimeesquemaidart());
                        newRegimenSave.setRegimenomeespecificado(newRegimen.getRegimenomeespecificado());
                        newRegimenSave.getRegimenDrugs().addAll(regimenDrugsListNew);
                        DrugManager.saveRegimeTerapeutico(session, newRegimenSave);
                        session.flush();
                        tx.commit();
                        session.close();
                        break;
                    } catch (HibernateException he) {
                        showMessage(
                                MessageDialog.ERROR,
                                "Problemas ao gravar a informação",
                                "Problemas ao gravar a informação. Por favor, tente novamente.");
                        if (tx != null) {
                            tx.rollback();
                            session.close();
                        }
                        getLog().error(he);
                    } finally {
                        continue;
                    }
                }

            }
            showMessage(MessageDialog.INFORMATION, "Carregamento com Sucesso",
                    "O processo de carregamento ocorreu com Sucesso.");

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
        rdBtnActive.setEnabled(enable);
        rdBtnInactive.setEnabled(enable);

        btnSave.setEnabled(enable);
    }

    @Override
    protected void setLogger() {

    }

    private void createGrpClinicColumnsSelection() {

        lblRegimenTableHeader = new Label(getShell(), SWT.BORDER);
        lblRegimenTableHeader.setBounds(new Rectangle(200, 270, 200, 20));
        lblRegimenTableHeader.setText("Lista de Regimes Terapeuticos");
        lblRegimenTableHeader.setAlignment(SWT.CENTER);
        lblRegimenTableHeader.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        lnkSelectAllColumns = new Link(getShell(), SWT.NONE);
        lnkSelectAllColumns.setBounds(new Rectangle(70, 300, 450, 20));
        lnkSelectAllColumns
                .setText("Por favor, seleccione os Regimes que pretende importar " +
                        "ou <A>Seleccionar todas</A> colunas");
        lnkSelectAllColumns
                .setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
        lnkSelectAllColumns.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                tblColumns.setAllChecked(true);
            }
        });

        createTblRegimen();
    }

    private void createTblRegimen() {

     
        tblColumns = CheckboxTableViewer.newCheckList(getShell(), SWT.BORDER);
        tblColumns.getTable().setBounds(
                new org.eclipse.swt.graphics.Rectangle(85, 320, 420, 150));
        tblColumns.getTable().setFont(
                ResourceUtils.getFont(iDartFont.VERASANS_8));
        tblColumns.setContentProvider(new ArrayContentProvider());
    }

    private void populateRestRegimen() {
        PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();

        String url = CentralizationProperties.centralized_server_url;
        boolean status = rdBtnActive.getSelection();
        List<RegimeTerapeutico> restRegimeTerapeuticoRest = RestFarmac.restGeAllRegimenByStatus(url, status, hSession,pool);

        for (RegimeTerapeutico regimeTerapeutico : restRegimeTerapeuticoRest) {
            List<RegimenDrugs> regimenDrugs = RestFarmac.restGeAllRegimenDrugsByRegimen(url, regimeTerapeutico, hSession,pool);
            if (!regimenDrugs.isEmpty())
                regimeTerapeutico.setRegimenDrugs(regimenDrugs);
            restRegimeTerapeutico.add(regimeTerapeutico);
        }

        tblColumns.setInput(restRegimeTerapeutico);

        if (restRegimeTerapeutico.isEmpty()) {
            btnSave.setEnabled(false);
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" Nenhuma actualização foi encontrada ");
            b.setText("Nenhuma actualização foi encontrada");
            b.open();
        } else
            btnSave.setEnabled(true);
    }

}
