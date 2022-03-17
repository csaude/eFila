package org.celllife.idart.gui.user;

import model.manager.AdministrationManager;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.hibernate.User;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.search.Search;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.*;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;

public class UserStatePasswordManage extends GenericFormGui {

	private Text txtUser;

	private Text txtPassword;

	private Text txtPassConfirm;
	
	private Combo cbxEstadoUser;
	
	private Group grpUserInfo;
	
	private Button rdBtnActive;

	private Button rdBtnInactive;

	private Button btnSearch;

	private User user;

	private User localUser;

	public UserStatePasswordManage(Shell parent) {
		super(parent, HibernateUtil.getNewSession());
	}

	@Override
	protected void clearForm() {
		txtUser.setText("");
		txtPassword.setText("");
		txtPassConfirm.setText("");

		btnSearch.setEnabled(true);
		rdBtnInactive.setEnabled(false);
		rdBtnActive.setEnabled(false);

		btnSave.setEnabled(false);
		
	}

	@Override
	protected boolean submitForm() {
		return false;
	}

	@Override
	protected boolean fieldsOk() {
		if (txtPassword.getText() != null && (txtPassword.getText().equals(txtPassConfirm.getText()))) return true;

		if (txtPassword.getText() == null && txtPassConfirm.getText() == null) return true;

		return false;
	}

	@Override
	protected void createCompHeader() {
		String headerTxt = ("Redefinção da senha e estado do Utilizador");
		iDartImage icoImage = iDartImage.PHARMACYUSER;
		buildCompHeader(headerTxt, icoImage);
		
	}

	@Override
	protected void createCompButtons() {
		buildCompButtons();
		
	}

	@Override
	protected void cmdSaveWidgetSelected() {
		if (fieldsOk()) {

			//First we check access selected
			int option = SWT.YES;


			MessageBox msg = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
			msg.setText("Redefinição da senha e estado Utilizador");
			msg.setMessage("Tem certeza de que deseja redefinir a senha e estado deste usuário?");
			option = msg.open();

			if(option == SWT.YES)
			{
				Transaction tx = null;

				try {
					tx = getHSession().beginTransaction();

					// if new password has been filled in, change password
					if (!txtPassword.getText().trim().equals("")) {
						AdministrationManager.updateUserPassword(getHSession(), user, txtPassword.getText());

						AdministrationManager.updateUserState(getHSession(), user);
					}


					getHSession().flush();
					tx.commit();
					MessageBox m = new MessageBox(getShell(), SWT.OK
							| SWT.ICON_INFORMATION);
					m.setText("Senha e estado redefinido");
					m.setMessage("User '".concat(txtUser.getText()).concat(
							"' foi atualizado com sucesso."));
					m.open();
					cmdCancelWidgetSelected();

				}

				catch (HibernateException he) {
					if (tx != null) {
						tx.rollback();
					}
					MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_WARNING);
					m.setText("Problem Saving To Database");
					m.setMessage("A senha não pode ser alterado. Por favor, tente novamente");
					m.open();
					getLog().error(he);
				}
			}
		}else {
			MessageBox m = new MessageBox(getShell(), SWT.OK | SWT.ICON_WARNING);
			m.setText("Preenchimento dos campos");
			m.setMessage("O campo [Senha] e [Confirmação da senha] devem estar preenchidos pelo mesmo valor");
			m.open();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createContents() {
		localUser = LocalObjects.getUser(getHSession());

		createGrpUserInfo();
		
	}

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	@Override
	protected void createShell() {
		String shellText = "Redefinção da senha e estado do Utilizador";
		Rectangle bounds = new Rectangle(25, 0, 800, 400);
		buildShell(shellText, bounds);
		
	}

	/**
	 * This method initializes grpUserInfo
	 * 
	 */
	private void createGrpUserInfo() {

		if (grpUserInfo != null) {
			grpUserInfo.dispose();
		}
		// grpUserInfo
		grpUserInfo = new Group(getShell(), SWT.NONE);
		grpUserInfo.setBounds(new Rectangle(100, 90, 600, 200));

		// lblUser & txtUser
		Label lblUser = new Label(grpUserInfo, SWT.NONE);
		lblUser.setBounds(new Rectangle(30, 20, 125, 20));
		lblUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblUser.setText("* Utilizador:");
		txtUser = new Text(grpUserInfo, SWT.BORDER);
		txtUser.setBounds(new Rectangle(185, 20, 130, 20));
		txtUser.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtUser.setEnabled(false);


		btnSearch = new Button(grpUserInfo, SWT.NONE);
		btnSearch.setBounds(new org.eclipse.swt.graphics.Rectangle(350, 20, 120, 30));
		btnSearch.setToolTipText("Pressione este botão para procurar um usuário.");
		btnSearch.setText("Procurar"); //$NON-NLS-1$
		btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnSearch.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						cmdSearchWidgetSelected();
					}
				});


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
		lblPasswordConfirm.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblPasswordConfirm.setText("* Repetir Senha:");
		txtPassConfirm = new Text(grpUserInfo, SWT.PASSWORD | SWT.BORDER);
		txtPassConfirm.setBounds(new Rectangle(185, 80, 130, 20));
		txtPassConfirm.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		
		// lblTipoUSER & txtTipoUSER
		addEstadoRdButtons(grpUserInfo);

		rdBtnActive.setEnabled(false);
		rdBtnInactive.setEnabled(false);
				
	}

	private void cmdSearchWidgetSelected() {

		Search userSearch = new Search(getHSession(), getShell(), CommonObjects.USER);

		if (userSearch.getValueSelected() != null) {

			user = AdministrationManager.getUserByName(getHSession(), userSearch.getValueSelected()[0]);
			loadUserDetails();
			btnSearch.setEnabled(false);

			enableFields(true);
			txtUser.setEnabled(false);
			txtPassword.setFocus();
			btnSave.setEnabled(true);

		} else {
			btnSearch.setEnabled(true);

			rdBtnActive.setEnabled(false);
			rdBtnInactive.setEnabled(false);
		}

	}

	private void loadUserDetails() {

		txtUser.setText(user.getUsername());

		if (user.isActive()) {
			rdBtnActive.setSelection(true);
			rdBtnInactive.setSelection(false);
		} else {
			rdBtnActive.setSelection(false);
			rdBtnInactive.setSelection(true);
		}

		rdBtnActive.setEnabled(true);
		rdBtnInactive.setEnabled(true);
	}
	
	private void addEstadoRdButtons(Group parent) {

		Group grpAddOrConfigureUser = new Group(parent, SWT.NONE);
		grpAddOrConfigureUser.setBounds(new Rectangle(185, 110, 300, 50));

		Label lblEstado= new Label(parent, SWT.NONE);
		lblEstado.setBounds(new Rectangle(30, 110, 125, 20));
		lblEstado.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblEstado.setText("* Estado:");

		rdBtnActive = new Button(grpAddOrConfigureUser, SWT.RADIO);
		rdBtnActive.setBounds(new Rectangle(20, 12, 100, 30));
		rdBtnActive.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		rdBtnActive.setText("Activo");
		
		if(!LocalObjects.getUser(getHSession()).isAdmin())
		rdBtnActive.setSelection(false);
		else
			rdBtnActive.setSelection(true);
			rdBtnActive.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						if (rdBtnActive.getSelection()) {
							user.changeStateToActive();
						}
					}
				});
		
		
		
		rdBtnInactive = new Button(grpAddOrConfigureUser, SWT.RADIO);
		rdBtnInactive.setBounds(new Rectangle(150, 12, 100, 30));
		rdBtnInactive.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		rdBtnInactive.setText("Não activo");
		
		if(!LocalObjects.getUser(getHSession()).isAdmin())
			rdBtnActive.setSelection(true);
			else
				rdBtnActive.setSelection(false);
				rdBtnInactive.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					@Override
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						if (rdBtnInactive.getSelection()) {
							user.changeStateToNotActive();
						}
					}
				});
		
	}
	
	protected void buildCompButtons() {

		Composite myCmp = new Composite(getShell(), SWT.NONE);

		RowLayout rowlyt = new RowLayout();
		rowlyt.justify = true;
		rowlyt.pack = false;
		rowlyt.spacing = 10;
		myCmp.setLayout(rowlyt);

		RowData rowD = new RowData(170, 30);

		setCompButtons(new Composite(myCmp, SWT.NONE));
		getCompButtons().setLayout(rowlyt);


		// btnSave
		btnSave = new Button(getCompButtons(), SWT.NONE);
		btnSave.setText("Gravar"); //$NON-NLS-1$
		btnSave.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnSave.setToolTipText(Messages.getString("genericformgui.button.save.tooltip")); //$NON-NLS-1$


		// btnClear
		btnClear = new Button(getCompButtons(), SWT.NONE);
		btnClear.setText(Messages.getString("genericformgui.button.clear.text")); //$NON-NLS-1$
		btnClear.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnClear.setToolTipText(Messages.getString("genericformgui.button.clear.tooltip")); //$NON-NLS-1$

		btnCancel = new Button(getCompButtons(), SWT.NONE);
		btnCancel.setText( "Cancelar"); //$NON-NLS-1$
		btnCancel.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnCancel.setToolTipText(Messages.getString("Clique para fechar a tela")); //$NON-NLS-1$
				
				
		// Adding button listener
		btnSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				cmdSaveWidgetSelected();
			}
		});

		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				cmdClearWidgetSelected();
			}
		});

		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				cmdCancelWidgetSelected();
			}
		});

		Control[] buttons = getCompButtons().getChildren();
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setLayoutData(rowD);
		}

		getCompButtons().pack();
		Rectangle b = getShell().getBounds();
		myCmp.setBounds(0, b.height - 79, b.width, 40);
	}
}
