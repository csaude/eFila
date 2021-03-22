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

package org.celllife.idart.gui.user;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.manager.AdministrationManager;

import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.Role;
import org.celllife.idart.database.hibernate.SystemFunctionality;
import org.celllife.idart.database.hibernate.User;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.search.Search;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

/**
 *
 */
public class ManagePharmUsers extends GenericFormGui {

    private Button rdBtnAddUser;

    private Button rdBtnUpdateUser;

    private Button rdBtnStudy;

    private Button rdBtnReports;

    private Table tblClinicAccess;

    private Text txtUser;

    private Text txtPassword;

    private Text txtPassConfirm;

    private Group grpUserInfo;

    private boolean isAddNotUpdate;

    private boolean isForClinicApp;

    private User localUser;

    private Combo tipo_user;

    private Button btnSearch;

    /**
     * Default Constructor
     *
     * @param parent Shell
     */
    public ManagePharmUsers(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
    }

    /**
     * This method initializes newUser
     */
    @Override
    protected void createShell() {
        isAddNotUpdate = ((Boolean) getInitialisationOption(OPTION_isAddNotUpdate))
                .booleanValue();

        if (!LocalObjects.getUser(getHSession()).isAdmin())
            isAddNotUpdate = false;
        else
            isAddNotUpdate = true;


        isForClinicApp = !LocalObjects.loggedInToMainClinic();
        String shellTxt = isAddNotUpdate ? "Adicionar Novo Usuário"
                : "Actalizar Usuário Corrente";


        Rectangle bounds = new Rectangle(25, 0, 800, 600);
        buildShell(shellTxt, bounds);
    }

    @Override
    protected void createContents() {
        localUser = LocalObjects.getUser(getHSession());
        createCompInstructions();
        createGrpAddOrConfigureUser();
        createGrpUserInfo();
        if (isAddNotUpdate) {
            txtUser.setFocus();
        } else {
            txtPassword.setFocus();
        }
    }

    /**
     * This method initializes compHeader
     */
    @Override
    protected void createCompHeader() {
        String headerTxt = (isAddNotUpdate ? "Adicionar Novo Usuário"
                : "Actalizar Usuário Corrente");
        iDartImage icoImage = iDartImage.PHARMACYUSER;
        buildCompHeader(headerTxt, icoImage);
    }

    /**
     * This method initializes compButtons
     */
    @Override
    protected void createCompButtons() {
        // Parent Class generic call
        buildCompButtons();
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
    private void createGrpAddOrConfigureUser() {

        Group grpAddOrConfigureUser = new Group(getShell(), SWT.NONE);
        grpAddOrConfigureUser.setBounds(new Rectangle(225, 130, 400, 50));

        rdBtnAddUser = new Button(grpAddOrConfigureUser, SWT.RADIO);
        rdBtnAddUser.setBounds(new Rectangle(20, 12, 160, 30));
        rdBtnAddUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnAddUser.setText("Adicionar novo usuário");

//        if (!LocalObjects.getUser(getHSession()).isAdmin())
//            rdBtnAddUser.setSelection(false);
//        else
        rdBtnAddUser.setSelection(true);
        rdBtnAddUser
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        if (rdBtnAddUser.getSelection()) {
                            cmdAddWidgetSelected();
                        }
                    }
                });

        rdBtnUpdateUser = new Button(grpAddOrConfigureUser, SWT.RADIO);
        rdBtnUpdateUser.setBounds(new Rectangle(195, 12, 180, 30));
        rdBtnUpdateUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnUpdateUser.setText("Actualizar usuário actual");
        rdBtnUpdateUser.setSelection(false);
        rdBtnUpdateUser
                .addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        if (rdBtnUpdateUser.getSelection()) {
                            cmdUpdateWidgetSelected();
                        }
                    }
                });


        if (!localUser.isAdmin()) {
            rdBtnAddUser.setSelection(false);
            rdBtnAddUser.setEnabled(false);
            isAddNotUpdate = false;
            rdBtnUpdateUser.setSelection(true);

        }
    }

    /**
     * This method initializes grpUserInfo
     */
    private void createGrpUserInfo() {

        if (grpUserInfo != null) {
            grpUserInfo.dispose();
        }
        // grpUserInfo
        grpUserInfo = new Group(getShell(), SWT.NONE);
        grpUserInfo.setBounds(new Rectangle(100, 200, 600, 280));

        if (isAddNotUpdate) {

            // lblUser & txtUser
            Label lblUser = new Label(grpUserInfo, SWT.NONE);
            lblUser.setBounds(new Rectangle(30, 20, 125, 20));
            lblUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblUser.setText("* Usuário:");
            txtUser = new Text(grpUserInfo, SWT.BORDER);
            txtUser.setBounds(new Rectangle(185, 20, 130, 20));
            txtUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

            // lblPassword & txtPass
            Label lblPassword = new Label(grpUserInfo, SWT.NONE);
            lblPassword.setBounds(new Rectangle(30, 50, 125, 20));
            lblPassword.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblPassword.setText("* Senha:");
            txtPassword = new Text(grpUserInfo, SWT.PASSWORD | SWT.BORDER);
            txtPassword.setBounds(new Rectangle(185, 50, 130, 20));
            txtPassword.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

            // lblPasswordConfirm & txtPassConfirm
            Label lblPasswordConfirm = new Label(grpUserInfo, SWT.NONE);
            lblPasswordConfirm.setBounds(new Rectangle(30, 80, 125, 20));
            lblPasswordConfirm.setFont(ResourceUtils
                    .getFont(iDartFont.VERASANS_8));
            lblPasswordConfirm.setText("* Repetir Senha:");
            txtPassConfirm = new Text(grpUserInfo, SWT.PASSWORD | SWT.BORDER);
            txtPassConfirm.setBounds(new Rectangle(185, 80, 130, 20));
            txtPassConfirm.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

            // lblTipoUSER & txtTipoUSER
            List<Role> roles = AdministrationManager.getRoles(getHSession());
            Label lblTipoUser = new Label(grpUserInfo, SWT.NONE);
            lblTipoUser.setBounds(new Rectangle(30, 110, 125, 20));
            lblTipoUser.setFont(ResourceUtils
                    .getFont(iDartFont.VERASANS_8));
            lblTipoUser.setText("* Perfil do Usuário:");
            tipo_user = new Combo(grpUserInfo, SWT.BORDER);
            tipo_user.setBounds(new Rectangle(185, 110, 125,
                    20));
            tipo_user.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            tipo_user.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

            for (Role role : roles) {
                tipo_user.add(role.getDescription());

                tipo_user.setData(role);
            }
            tipo_user.select(1);

        } else {

            // se for Admin permitir seleccionar o user.
            if (LocalObjects.getUser(getHSession()).isAdmin()) {
                btnSearch = new Button(grpUserInfo, SWT.NONE);
                btnSearch.setBounds(new org.eclipse.swt.graphics.Rectangle(320, 17, 90, 30));
                btnSearch.setToolTipText("Pressione este botão para procurar um utilizador.");
                btnSearch.setText("Procurar"); //$NON-NLS-1$
                btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                btnSearch.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
                    @Override
                    public void widgetSelected(
                            org.eclipse.swt.events.SelectionEvent e) {
                        cmdSearchUserWidgetSelected();
                    }
                });

                // lblTipoUSER & txtTipoUSER
                List<Role> roles = AdministrationManager.getRoles(getHSession());
                Label lblTipoUser = new Label(grpUserInfo, SWT.NONE);
                lblTipoUser.setBounds(new Rectangle(30, 110, 125, 20));
                lblTipoUser.setFont(ResourceUtils
                        .getFont(iDartFont.VERASANS_8));
                lblTipoUser.setText("* Perfil do Usuário:");
                tipo_user = new Combo(grpUserInfo, SWT.BORDER);
                tipo_user.setBounds(new Rectangle(185, 110, 125,
                        20));
                tipo_user.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
                tipo_user.setBackground(ResourceUtils.getColor(iDartColor.WHITE));

                for (Role role : roles) {
                    tipo_user.add(role.getDescription());
                    tipo_user.setData(role);
                }
                tipo_user.select(1);
            }


            // lblUser & txtUser
            Label lblUser = new Label(grpUserInfo, SWT.NONE);
            lblUser.setBounds(new Rectangle(30, 20, 125, 20));
            lblUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblUser.setText("* Usuário:");
            txtUser = new Text(grpUserInfo, SWT.BORDER);
            txtUser.setBounds(new Rectangle(185, 20, 130, 20));
            txtUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            txtUser.setText("");
            txtUser.setEditable(false);
            txtUser.setEnabled(false);

            // lblPassword & txtPass
            Label lblPassword = new Label(grpUserInfo, SWT.NONE);
            lblPassword.setBounds(new Rectangle(30, 50, 125, 20));
            lblPassword.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            lblPassword.setText("  Nova Senha:");
            txtPassword = new Text(grpUserInfo, SWT.PASSWORD | SWT.BORDER);
            txtPassword.setBounds(new Rectangle(185, 50, 130, 20));
            txtPassword.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            txtPassword.setEnabled(false);

            // lblPasswordConfirm & txtPassConfirm
            Label lblPasswordConfirm = new Label(grpUserInfo, SWT.NONE);
            lblPasswordConfirm.setBounds(new Rectangle(30, 80, 145, 20));
            lblPasswordConfirm.setFont(ResourceUtils
                    .getFont(iDartFont.VERASANS_8));
            lblPasswordConfirm.setText("  Repetir Nova Senha:");
            txtPassConfirm = new Text(grpUserInfo, SWT.PASSWORD | SWT.BORDER);
            txtPassConfirm.setBounds(new Rectangle(185, 80, 130, 20));
            txtPassConfirm.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
            txtPassConfirm.setEnabled(false);


        }

        Label lblClinicAccess = new Label(grpUserInfo, SWT.BORDER);
        lblClinicAccess.setBounds(new Rectangle(370, 40, 200, 20));

        lblClinicAccess.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        lblClinicAccess.setText("Acesso a US:");

        lblClinicAccess.setAlignment(SWT.CENTER);

        tblClinicAccess = new Table(grpUserInfo, SWT.CHECK | SWT.BORDER
                | SWT.FULL_SELECTION);
        tblClinicAccess.setBounds(370, 60, 200, 200);
        tblClinicAccess.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        TableColumn tblColClinicName = new TableColumn(tblClinicAccess,
                SWT.NONE);
        tblColClinicName.setText("Nome da US");
        tblColClinicName.setWidth(195);
        populateClinicAccessList();
        if (iDartProperties.isCidaStudy) {//check if the cida property exists
            createUserRolesGroup();
        }

        if (isForClinicApp) {
            tblClinicAccess.setEnabled(false);
            tblClinicAccess.setBackground(ResourceUtils
                    .getColor(iDartColor.GRAY));
        }
    }

    private void createUserRolesGroup() {

        Label noteLabel = new Label(grpUserInfo, SWT.WRAP | SWT.CENTER | SWT.NONE);
        noteLabel.setBounds(new Rectangle(50, 110, 250, 30));
        noteLabel.setText("Note que este é apenas para fins de estudo. Deixe em branco para o pessoal da farmácia");
        noteLabel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));

        Group grpUserRoles = new Group(grpUserInfo, SWT.NONE);
        grpUserRoles.setBounds(new Rectangle(50, 150, 250, 100));

        Label confLabel = new Label(grpUserRoles, SWT.NONE);
        confLabel.setBounds(new Rectangle(40, 10, 150, 15));
        confLabel.setText("Configure o tipo de usuário:");
        confLabel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        rdBtnStudy = new Button(grpUserRoles, SWT.RADIO);
        rdBtnStudy.setBounds(new Rectangle(20, 25, 150, 30));
        rdBtnStudy.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnStudy.setText("trabalhador do estudo");
        rdBtnStudy.setSelection(false);

        rdBtnReports = new Button(grpUserRoles, SWT.RADIO);
        rdBtnReports.setBounds(new Rectangle(20, 50, 150, 30));
        rdBtnReports.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        rdBtnReports.setText("acesso a relatários");
        rdBtnReports.setSelection(false);

    }

    private void populateClinicAccessList() {
        for (Clinic clinic : AdministrationManager.getClinics(getHSession())) {
            TableItem ti = new TableItem(tblClinicAccess, SWT.None);
            ti.setText(0, clinic.getClinicName());
            ti.setData(clinic);

            if ((!isAddNotUpdate) && localUser.getClinics().contains(clinic)) {
                ti.setChecked(true);
            } else if (isAddNotUpdate) {
                if (clinic.getClinicName().equals(
                        LocalObjects.currentClinic.getClinicName())) {
                    ti.setChecked(true);
                }

            }

        }
    }

    /**
     * Method fieldsOk.
     *
     * @return boolean
     */
    @Override
    protected boolean fieldsOk() {

        // check the clinic table
        boolean checkedClinic = false;
        for (TableItem ti : tblClinicAccess.getItems()) {
            if (ti.getChecked()) {
                checkedClinic = true;
            }
        }

        if (!checkedClinic) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("Todos os usuários precisam ter acesso a pelo menos uma unidade sanitária. \n\n"
                    + "Por favor, selecione pelo menos uma unidade sanitária e tentar salvar novamente.");
            b.setText("Nenhum acesso a US concedido");

            b.open();
            return false;

        }

        if ((tipo_user.getText() == null || tipo_user.getText().isEmpty())) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("Seleccione o Perfil do usuário. ");
            b.setText("Seleccione o perfil do usuário");

            b.open();
            return false;

        }
        if (txtUser.getText().trim().equals("")) {

            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("O nome de usuário não pode ficar em branco");
            b.setText("Faltando Informação");
            b.open();
            txtUser.setFocus();
            return false;

        }
        if (txtPassword.getText().trim().equals("")) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage("A senha não pode ficar em branco");
            b.setText("Faltando Informação");
            b.open();
            txtPassword.setFocus();
            return false;

        }
        if ((!isAddNotUpdate)
                && !((txtPassword.getText().trim().equals("")) && (txtPassConfirm
                .getText().trim().equals("")))) {
            // user has filled in a password field - they are trying to
            // change the password, so it must be checked
            if (LocalObjects.getUser(getHSession()).getPassword()
                    .equals(txtPassword.getText().trim())) {
                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR
                        | SWT.OK);
                b.setMessage("A nova senha é a mesma que a senha antiga");
                b.setText("Nova senha inválida");
                b.open();
                txtPassword.setFocus();

                txtPassword.setText("");
                txtPassConfirm.setText("");

                return false;
            }
        }
        return confirmPasswordMatches();
    }

    /**
     * Method confirmPasswordMatches.
     *
     * @return boolean
     */
    private boolean confirmPasswordMatches() {
        if (!txtPassword.getText().equals(txtPassConfirm.getText())) {
            MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_WARNING);
            m.setText("Senhas inconsistentes");
            m.setMessage("As senhas não coincidem. Digite novamente ambas as senhas");
            m.open();
            txtPassword.setText("");
            txtPassConfirm.setText("");

            txtPassword.setFocus();
            return false;
        }
        return true;
    }

    /**
     * clears the current form
     */
    @Override
    protected void clearForm() {

        if (isAddNotUpdate) {
            txtUser.setText("");
        }
        if (iDartProperties.isCidaStudy) {//Check if the cida property exists
            rdBtnStudy.setSelection(false);
            rdBtnReports.setSelection(false);
        }

        txtPassword.setText("");
        txtPassConfirm.setText("");
        tipo_user.setText("");
        txtUser.setFocus();
    }

    @Override
    protected void cmdSaveWidgetSelected() {

        if (fieldsOk()) {

            //First we check access selected
            int option = SWT.YES;
            if (checkMainClinicAccessOnlySelected()) {

                MessageBox m = new MessageBox(getShell(), SWT.YES | SWT.NO
                        | SWT.ICON_QUESTION);
                m.setText("Adicioar Usuário");
                m.setMessage("Tem certeza de que deseja adicionar este usuário sem acesso a qualquer uma das unidade sanitárias ?");
                option = m.open();
            }
            if (option == SWT.YES) {
                Transaction tx = null;

                try {
                    tx = getHSession().beginTransaction();

                    Set<Clinic> sitesSet = new HashSet<Clinic>();
                    for (TableItem ti : tblClinicAccess.getItems()) {

                        if (ti.getChecked()) {
                            sitesSet.add((Clinic) ti.getData());
                        }
                    }

                    Set<Role> roles = new HashSet<>();
                    roles.add(AdministrationManager.getRoleByDescription(getHSession(), tipo_user.getText()));

                    // before we try anything, lets ask the user for their password
                    String confirm = "ATENÇÃO:Vocé só deve executar esta acção se tiver certeza de que vocé deseja "
                            + (isAddNotUpdate ? "adicionar" : "actualizar")
                            + " este usuário. O usuário que realizou esta acção, bem como a hora atual, será gravado no log de transações.";

                    if (isAddNotUpdate) {

                        if (isAddNotUpdate
                                && AdministrationManager.saveUser(getHSession(), txtUser.getText(), txtPassword.getText(), roles, sitesSet)) {
                            getHSession().flush();
                            tx.commit();

                            MessageBox m = new MessageBox(getShell(), SWT.OK
                                    | SWT.ICON_INFORMATION);
                            m.setText("Novo Usuário Adicionado");
                            m.setMessage("Um novo utilizador '".concat(
                                    txtUser.getText()).concat(
                                    "' foi adicionado ao sistema."));
                            m.open();
                            cmdCancelWidgetSelected();

                        }
                    } else if (!isAddNotUpdate) {

                        if (!sitesSet.equals(localUser.getClinics())) {
                            AdministrationManager.updateUserClinics(
                                    getHSession(), localUser, sitesSet);
                        }

                        if (!roles.equals(localUser.getRoleSet())) {
                            AdministrationManager.updateUserRoles(
                                    getHSession(), localUser, roles);
                        }

                        getHSession().flush();
                        tx.commit();
                        MessageBox m = new MessageBox(getShell(), SWT.OK
                                | SWT.ICON_INFORMATION);
                        m.setText("Senha alterada");
                        m.setMessage("Usuário '".concat(txtUser.getText()).concat(
                                "' foi atualizada com sucesso."));
                        m.open();
                        cmdCancelWidgetSelected();
                    } else {
                        if (tx != null) {
                            tx.rollback();
                        }
                        MessageBox m = new MessageBox(getShell(), SWT.OK
                                | SWT.ICON_WARNING);
                        m.setText(" Usuário Duplicado");
                        m.setMessage("O usuário'".concat(txtUser.getText())
                                .concat("' já existe na base de dados. ")
                                .concat("\n\nPor favor, escolhe outro nome do usuário."));
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
                            .setMessage(isAddNotUpdate ? "O Usuário '".concat(
                                    txtUser.getText()).concat(
                                    "' não foi gravado. ").concat(
                                    "\n\nPor favor tente de novo.")
                                    : "A senha não pode ser alterado. Por favor, tente novamente");
                    m.open();
                    getLog().error(he);
                }
            }
        }

    }

    @Override
    protected void cmdCancelWidgetSelected() {
        closeShell(true);
    }

    @Override
    protected void cmdClearWidgetSelected() {

        clearForm();
    }

    private void cmdAddWidgetSelected() {
        isAddNotUpdate = true;
        getShell().setText("Adicionar Novo Usuário");
        createCompHeader();
        createGrpUserInfo();
        txtUser.setFocus();
    }

    private void cmdUpdateWidgetSelected() {
        isAddNotUpdate = false;
        getShell().setText("Actualizar o Usuário corrente");
        createCompHeader();
        createGrpUserInfo();
        txtPassword.setFocus();
    }

    /**
     * Method enableFields.
     *
     * @param enable boolean
     */
    @Override
    protected void enableFields(boolean enable) {
    }

    /**
     * Method submitForm.
     *
     * @return boolean
     */
    @Override
    protected boolean submitForm() {
        return false;
    }

    @Override
    protected void setLogger() {
        Logger log = Logger.getLogger(this.getClass());
        setLog(log);
    }

    /**
     * This method checks if only the main clinic was selected even if there are
     * other clinics
     *
     * @return
     */
    private boolean checkMainClinicAccessOnlySelected() {

        boolean checkedMainClinic = false;
        int noOfClinics = 0;
        for (TableItem ti : tblClinicAccess.getItems()) {
            if (ti.getChecked()) {
                noOfClinics++;
                Clinic c = (Clinic) ti.getData();
                if (c.isMainClinic()) {
                    checkedMainClinic = true;
                }
            }
        }

        if (checkedMainClinic && noOfClinics == 1)
            return true;
        else
            return false;
    }

    private Set<Role> getSelectedRole() {
        Set<Role> roles = new HashSet<>();

        if (rdBtnStudy != null && rdBtnStudy.getSelection()) {
            roles.clear();
            roles.add(AdministrationManager.getRoleByCode(getHSession(), Role.STUDYWORKER));
        } else if (rdBtnReports != null && rdBtnReports.getSelection()) {
            roles.clear();
            roles.add(AdministrationManager.getRoleByCode(getHSession(), Role.MEA));
        } else {
            roles.clear();
            roles.add(AdministrationManager.getRoleByCode(getHSession(), Role.PHARMACIST));
        }
        return roles;

    }


    private void cmdSearchUserWidgetSelected() {

        Search userSearch = new Search(getHSession(), getShell(), CommonObjects.USER);

        if (userSearch.getValueSelected() != null) {

            localUser = AdministrationManager.getUserByName(getHSession(), userSearch.getValueSelected()[0]);
            btnSearch.setEnabled(false);
            enableFields(true);
            txtUser.setText(localUser.getUsername());
            txtUser.setEnabled(false);
            txtPassword.setText(localUser.getPassword());
            txtPassConfirm.setText(localUser.getPassword());
            tipo_user.setFocus();
            btnSave.setEnabled(true);
        }
    }
}