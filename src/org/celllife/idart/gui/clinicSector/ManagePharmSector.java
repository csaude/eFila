package org.celllife.idart.gui.clinicSector;

import model.manager.AdministrationManager;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.ClinicSector;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.search.Search;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

public class ManagePharmSector extends GenericFormGui {

    private Button rdBtnAddSector;

    private Button rdBtnUpdateSector;

    private Text txtCode;

    private Text txtSectorName;

    private Text txtTelephone;

    private Group grpSectorInfo;

    private boolean isAddNotUpdate;

    private boolean isForClinicApp;

    private ClinicSector localClinicSector;

    private Button btnSearch;


    /**
     * Default Constructor
     *
     * @param parent Shell
     */
    public ManagePharmSector(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
    }

    @Override
    protected void createShell() {
        isAddNotUpdate = ((Boolean) getInitialisationOption(OPTION_isAddNotUpdate)).booleanValue();


        isForClinicApp = !LocalObjects.loggedInToMainClinic();
        String shellTxt = isAddNotUpdate ? "Adicionar Nova Parágem Única"
                : "Actalizar Parágem Única Corrente";

        Rectangle bounds = new Rectangle(25, 0, 800, 600);
        buildShell(shellTxt, bounds);
    }

    @Override
    protected void createContents() {
        createCompInstructions();
        createGrpAddOrConfigureSector();
        createGrpUserInfo();

    }

    private void createCompInstructions() {
        Composite compInstructions = new Composite(getShell(), SWT.NONE);
        compInstructions.setLayout(null);
        compInstructions.setBounds(new Rectangle(200, 79, 530, 25));

        Label lblInstructions = new Label(compInstructions, SWT.CENTER);
        lblInstructions.setBounds(new Rectangle(0, 0, 600, 25));
        lblInstructions.setText("Todos campos com * são de preenchimento obrigatório");
        lblInstructions.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_10_ITALIC));
    }

    /**
     * This method initializes compButtonTab
     */
    private void createGrpAddOrConfigureSector() {

        Group grpAddOrConfigureUser = new Group(getShell(), SWT.NONE);
        grpAddOrConfigureUser.setBounds(new Rectangle(225, 130, 400, 50));

        rdBtnAddSector = new Button(grpAddOrConfigureUser, SWT.RADIO);
        rdBtnAddSector.setBounds(new Rectangle(20, 12, 160, 30));
        rdBtnAddSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnAddSector.setText("Adicionar nova Parágem Única");
        rdBtnAddSector.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        if (rdBtnAddSector.getSelection()) {
                            cmdAddWidgetSelected();
                        }
                    }
                });

        rdBtnUpdateSector = new Button(grpAddOrConfigureUser, SWT.RADIO);
        rdBtnUpdateSector.setBounds(new Rectangle(195, 12, 180, 30));
        rdBtnUpdateSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnUpdateSector.setText("Actualizar usuário actual");
        rdBtnUpdateSector.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        if (rdBtnUpdateSector.getSelection()) {
                            cmdUpdateWidgetSelected();
                        }
                    }
                });

    }

    /**
     * This method initializes grpUserInfo
     */
    private void createGrpUserInfo() {

        if (grpSectorInfo != null) {
            grpSectorInfo.dispose();
        }
        // grpSectorInfo
        grpSectorInfo = new Group(getShell(), SWT.NONE);
        grpSectorInfo.setBounds(new Rectangle(100, 200, 600, 280));

        if (!isAddNotUpdate) {
            btnSearch = new Button(grpSectorInfo, SWT.NONE);
            btnSearch.setBounds(new org.eclipse.swt.graphics.Rectangle(320, 17, 90, 30));
            btnSearch.setToolTipText("Pressione este botão para procurar um sector.");
            btnSearch.setText("Procurar"); //$NON-NLS-1$
            btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            btnSearch.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                @Override
                public void widgetSelected(
                        org.eclipse.swt.events.SelectionEvent e) {
                    cmdSearchSectorWidgetSelected();
                }
            });
        }
            // lblSectorCode & txtCode
            Label lblSectorCode = new Label(grpSectorInfo, SWT.NONE);
            lblSectorCode.setBounds(new Rectangle(30, 20, 125, 20));
            lblSectorCode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblSectorCode.setText("* Código:");
            txtCode = new Text(grpSectorInfo, SWT.BORDER);
            txtCode.setBounds(new Rectangle(185, 20, 130, 20));
            txtCode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

            // lblSectorName & txtSectorName
            Label lblSectorName = new Label(grpSectorInfo, SWT.NONE);
            lblSectorName.setBounds(new Rectangle(30, 50, 125, 20));
            lblSectorName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblSectorName.setText("* Nome do Sector:");
            txtSectorName = new Text(grpSectorInfo, SWT.BORDER);
            txtSectorName.setBounds(new Rectangle(185, 50, 130, 20));
            txtSectorName.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

            // lblTelephonne & txtTelephonne
            Label lblTelephonne = new Label(grpSectorInfo, SWT.NONE);
            lblTelephonne.setBounds(new Rectangle(30, 80, 125, 20));
            lblTelephonne.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblTelephonne.setText(" Telefone:");
            txtTelephone = new Text(grpSectorInfo, SWT.BORDER);
            txtTelephone.setBounds(new Rectangle(185, 80, 130, 20));
            txtTelephone.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

    }

    @Override
    protected boolean fieldsOk() {
        if (txtCode.getText().trim().equals("")) {

            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("O código da Parágem Única não pode ficar em branco");
            b.setText("Código da Parágem Única não pode ser vazio");
            b.open();
            txtCode.setFocus();
            return false;

        }
        if (txtSectorName.getText().trim().equals("")) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("O Nome da Parágem Única não pode ficar em branco");
            b.setText("Nome da Parágem Única não pode ser vazio");
            b.open();
            txtSectorName.setFocus();
            return false;

        }

        return true;
    }

    @Override
    protected void clearForm() {
        txtSectorName.setText("");
        txtCode.setText("");
        txtTelephone.setText("");
        txtCode.setFocus();
    }

    @Override
    protected boolean submitForm() {
        return false;
    }


    @Override
    protected void createCompHeader() {
        String headerTxt = (isAddNotUpdate ? "Adicionar Nova Parágem Única"
                : "Actalizar Parágem Única Corrente");
        iDartImage icoImage = iDartImage.PHARMACYUSER;
        buildCompHeader(headerTxt, icoImage);
    }

    @Override
    protected void createCompButtons() {
        // Parent Class generic call
        buildCompButtons();
    }

    @Override
    protected void cmdSaveWidgetSelected() {

        if (fieldsOk()) {

                Clinic clinic = AdministrationManager.getMainClinic(getHSession());
                Transaction tx = null;

                try {
                    tx = getHSession().beginTransaction();

                    if (isAddNotUpdate) {

                        if (isAddNotUpdate && AdministrationManager.saveSector(getHSession(), txtSectorName.getText(), txtCode.getText(), txtTelephone.getText(), clinic)) {
                            getHSession().flush();
                            tx.commit();

                            MessageBox m = new MessageBox(getShell(), SWT.OK
                                    | SWT.ICON_INFORMATION);
                            m.setText("Nova Parágem Única foi Adicionada");
                            m.setMessage("Uma nova Parágem Única '".concat(
                                    txtSectorName.getText()).concat(
                                    "' foi adicionada ao sistema."));
                            m.open();
                            cmdCancelWidgetSelected();

                        }else {
                            MessageBox m = new MessageBox(getShell(), SWT.OK
                                    | SWT.ICON_INFORMATION);
                            m.setText("A Parágem Única que pretende adicionar ja existe.");
                            m.setMessage("Existe uma Parágem Única com o nome '".concat(
                                    txtSectorName.getText()).concat(
                                    "' no sistema."));
                            m.open();
                            txtSectorName.setFocus();
                        }
                    } else if (!isAddNotUpdate) {

                        // if new password has been filled in, change password
                        AdministrationManager.updateSector(getHSession(),localClinicSector, txtCode.getText(),txtSectorName.getText(),txtTelephone.getText());

                        getHSession().flush();
                        tx.commit();
                        MessageBox m = new MessageBox(getShell(), SWT.OK
                                | SWT.ICON_INFORMATION);
                        m.setText("Parágem Única alterada");
                        m.setMessage("Sector '".concat(txtSectorName.getText()).concat(
                                "' foi atualizado com sucesso."));
                        m.open();
                        cmdCancelWidgetSelected();
                    } else {
                        if (tx != null) {
                            tx.rollback();
                        }
                        MessageBox m = new MessageBox(getShell(), SWT.OK
                                | SWT.ICON_WARNING);
                        m.setText(" Parágem Única Duplicada");
                        m.setMessage("A Parágem Única'".concat(txtSectorName.getText())
                                .concat("' já existe na base de dados. ")
                                .concat("\n\nPor favor, escolhe outro nome da Parágem Única."));
                        m.open();
                    }
                } catch (HibernateException he) {
                    if (tx != null) {
                        tx.rollback();
                    }
                    MessageBox m = new MessageBox(getShell(), SWT.OK
                            | SWT.ICON_WARNING);
                    m.setText("Problem Saving To Database");
                    m
                            .setMessage(isAddNotUpdate ? "A Parágem Única '".concat(
                                    txtSectorName.getText()).concat(
                                    "' não foi gravada. ").concat(
                                    "\n\nPor favor tente de novamente.")
                                    : "A Parágem Única não pode ser alterada. Por favor, tente novamente");
                    m.open();
                    getLog().error(he);
                }

        }

    }

    @Override
    protected void cmdClearWidgetSelected() {
        clearForm();
    }

    @Override
    protected void cmdCancelWidgetSelected() {
        closeShell(true);
    }

    @Override
    protected void enableFields(boolean enable) {

    }

    @Override
    protected void setLogger() {
        Logger log = Logger.getLogger(this.getClass());
        setLog(log);
    }

    private void cmdAddWidgetSelected() {
        isAddNotUpdate = true;
        getShell().setText("Adicionar Nova Parágem Única");
        createCompHeader();
        createGrpUserInfo();
        txtCode.setFocus();
    }

    private void cmdUpdateWidgetSelected() {
        isAddNotUpdate = false;
        getShell().setText("Actualizar Parágem Única corrente");
        createCompHeader();
        createGrpUserInfo();
        txtCode.setFocus();
    }

    private void cmdSearchSectorWidgetSelected() {

        Search sectorSearch = new Search(getHSession(), getShell(), CommonObjects.SECTOR);

        if (sectorSearch.getValueSelected() != null) {

            localClinicSector = AdministrationManager.getSectorByName(getHSession(), sectorSearch.getValueSelected()[1]);
            btnSearch.setEnabled(false);
            enableFields(true);
            txtCode.setText(localClinicSector.getCode());
            txtSectorName.setText(localClinicSector.getSectorname());
            txtTelephone.setText(localClinicSector.getTelephone());
            txtCode.setFocus();
            btnSave.setEnabled(true);
        }
    }

}
