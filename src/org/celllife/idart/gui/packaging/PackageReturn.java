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
package org.celllife.idart.gui.packaging;

import java.text.SimpleDateFormat;
import java.util.*;

import model.manager.PackageManager;
import model.manager.PatientManager;

import org.apache.log4j.Logger;
import org.celllife.function.DateRuleFactory;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.Episode;
import org.celllife.idart.database.hibernate.Packages;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.PatientIdentifier;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.facade.PackageReturnFacade;
import org.celllife.idart.gui.patient.EpisodeViewer;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.search.PatientSearch;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.gui.widget.DateButton;
import org.celllife.idart.gui.widget.DateInputValidator;
import org.celllife.idart.messages.Messages;
import org.celllife.idart.misc.PatientBarcodeParser;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
 */
public class PackageReturn extends GenericFormGui {

	// Fields which can be manipulated
	private Locale localeEn = new Locale("en", "US");
	private Text txtPatientId;
	private Button btnSearchPatient;
	private Button rbtnReturnToStock;
	private Button rbtnDestroyStock;
	private DateButton btnCaptureDate;
	private Table tblPackages;
	private TableColumn[] tblPackageCols;
	private CCombo cmbReturnReason;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", localeEn);
	private CCombo cmbStopEpisode;
	private Text txtStopNotes;
	private DateButton btnStopDate;
	private Button btnPreviousEpisodes;
	private Patient localPatient;
	private EpisodeViewer epiView;
	private final PackageReturnFacade packReturnFacade;
	private List<Packages> returnPacks;

	boolean episodeStopResonChanged = false;
	boolean episodeStopDateChanged = false;

	private static final String NOLONGER_TREATED_AT_CLINIC = "já não recebe mais tratamento na US";
	private static final String DRUG_CHANGE = "Mudança de medicamentos";
	private static final String DRUG_LOST = "Frasco perdido no transito";
	private static final String APPOINTMENT_MISSED = "Falta no Levantamento";

	/**
	 * Constructor for PackageReturn.
	 * 
	 * @param parent
	 *            Shell
	 */
	public PackageReturn(Shell parent) {
		super(parent, HibernateUtil.getNewSession());
		packReturnFacade = new PackageReturnFacade(getHSession());
	}

	@Override
	protected void createShell() {

		sdf = new SimpleDateFormat("dd MMM yy  hh:mm");
		String shellTxt = "devolução na farmácia de Frascos não entregues";
		Rectangle bounds = new Rectangle(0, 0, 800, 690);
		buildShell(shellTxt, bounds);
		createGrpScreenInfo();
		createGrpPatientId();
		createGrpPatientPrescriptions();
		createGrpPackageToReturn();
		txtPatientId.setFocus();
		// clearWidgetSelected();
		enableFields(false);
	}

	@Override
	protected void createCompHeader() {
		String headerTxt = "devolução na farmácia de Frascos não entregues";
		iDartImage icoImage = iDartImage.PACKAGERETURN;
		buildCompHeader(headerTxt, icoImage);
	}

	@Override
	protected void setLogger() {
		setLog(Logger.getLogger(this.getClass()));
	}

	private void createGrpScreenInfo() {
		Composite grpScrInfo = new Composite(getShell(), SWT.BORDER);
		grpScrInfo.setBounds(75, 60, 645, 60);
		Label lblInfo = new Label(grpScrInfo, SWT.NONE);
		lblInfo.setBounds(5, 10, 630, 45);
		lblInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
		// lblInfo.setAlignment(SWT.CENTER);
		String infoTxt = "   Use esta tela para devolver um frasco não entregue que foi criado para um paciente. Note que você não Está a APAGAr o registo,"
			+ "\nEstá a marcar o frasco como  \"não entregue.\" Se quer apagar este frasco permanentemente  "
			+ "\n  (Ex. Cometeu um erro na tela de dispensa de ARV), Por favor vai na tela \"A apagar Stock, Prescrição & Frascos\".";
		lblInfo.setText(infoTxt);
	}

	private void createGrpPatientId() {
		Group grpPatientId = new Group(getShell(), SWT.NONE);
		grpPatientId.setBounds(155, 139, 450, 50);
		grpPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpPatientId
		.setText(" Pesquisa do Paciente (apenas aqueles com frascos aguardando entrega)");
		grpPatientId.setForeground(ResourceUtils.getColor(iDartColor.BLUE));

		Label lblPatientId = new Label(grpPatientId, SWT.NONE);
		lblPatientId.setBounds(30, 25, 60, 20);
		lblPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblPatientId.setText(Messages.getString("patient.label.patientid")); //$NON-NLS-1$

		txtPatientId = new Text(grpPatientId, SWT.BORDER);
		txtPatientId.setBounds(100, 20, 150, 20);
		txtPatientId.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtPatientId.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent e) {
				if ((btnSearchPatient != null)
						&& (btnSearchPatient.getEnabled())) {
					if ((e.character == SWT.CR)
							|| (e.character == (char) iDartProperties.intValueOfAlternativeBarcodeEndChar)) {
						cmdSearchSelectedWidget();
					}
				}
			}
		});

		btnSearchPatient = new Button(grpPatientId, SWT.None);
		btnSearchPatient.setBounds(260, 17, 150, 25);
		btnSearchPatient.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnSearchPatient.setText("Patient Search");
		btnSearchPatient.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent evt) {
				// Ticket #647
				cmdSearchSelectedWidget();
			}
		});
	}

	private void createGrpPatientPrescriptions() {

		Group grpPatientPackage = new Group(getShell(), SWT.NONE);
		grpPatientPackage.setBounds(20, 208, 750, 158);
		grpPatientPackage.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpPatientPackage.setText("  Frasco do Paciente  ");

		Label lblInfo = new Label(grpPatientPackage, SWT.NONE);
		lblInfo.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8_ITALIC));
		lblInfo.setBounds(10, 25, 720, 35);
		String infoTxt = "O Frasco que se segue foi criado "
			+ "para este paciente mas não foi ainda entregue ao mesmo. \n "
			+ "Para devolver um frasco, clique na linha na tabela abaixo.";
		lblInfo.setText(infoTxt);
		lblInfo.setAlignment(SWT.CENTER);

		tblPackages = new Table(grpPatientPackage, SWT.MULTI
				| SWT.FULL_SELECTION | SWT.BORDER);
		tblPackages.setBounds(10, 62, 728, 90);
		tblPackages.setHeaderVisible(true);
		tblPackages.setLinesVisible(true);
		tblPackages.setItemCount(3);
		tblPackages.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

		String[] titles = { "Frasco ID", "medicamentos no Frasco", "Data de Criação",
				"Deixou a farmácia", "Recebido na US" };
		tblPackageCols = new TableColumn[titles.length];
		for (int i = 0; i < titles.length; i++) {
			tblPackageCols[i] = new TableColumn(tblPackages, SWT.NONE, i);
			tblPackageCols[i].setText(titles[i]);
			tblPackageCols[i].setResizable(true);
		}

		// Creating just 3 table items for starters.
		tblPackages.removeAll();
		tblPackages.clearAll();
		tblPackages.redraw();
		tblPackages.setItemCount(0);
		tblPackageCols[0].setWidth(125);
		tblPackageCols[1].setWidth(220);
		tblPackageCols[2].setWidth(120);
		tblPackageCols[3].setWidth(120);
		tblPackageCols[4].setWidth(120);
	}

	private void createGrpPackageToReturn() {
		Group grpPatientPackage = new Group(getShell(), SWT.NONE);
		grpPatientPackage.setBounds(20, 380, 750, 228);
		grpPatientPackage.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		grpPatientPackage.setText("   Frasco a Devolver  ");

		Label lblReturnReason = new Label(grpPatientPackage, SWT.NONE);
		lblReturnReason.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblReturnReason.setText("Motivo da devolução : ");
		lblReturnReason.setBounds(10, 28, 110, 25);

		cmbReturnReason = new CCombo(grpPatientPackage, SWT.BORDER);
		cmbReturnReason.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbReturnReason.setBounds(120, 25, 250, 20);
		cmbReturnReason.setEditable(false);
		cmbReturnReason.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent ev) {

				// As Default, the return date is set to today.
				String selection = cmbReturnReason.getItem(
						cmbReturnReason.getSelectionIndex()).trim();

				if (selection.equalsIgnoreCase(NOLONGER_TREATED_AT_CLINIC)) {
					// Enable the Episode group;
					cmbStopEpisode.setText("");
					btnPreviousEpisodes.setEnabled(true);
					txtStopNotes.setText("");
					btnStopDate.setText("Data Fim");
					rbtnDestroyStock.setEnabled(true);
					rbtnReturnToStock.setEnabled(true);
					rbtnDestroyStock.setSelection(false);
					rbtnReturnToStock.setSelection(false);
					if (localPatient.getAccountStatusWithCheck()) {
						txtStopNotes.setEnabled(true);
						btnStopDate.setEnabled(true);
						cmbStopEpisode.setEnabled(true);
						cmbStopEpisode.setBackground(ResourceUtils
								.getColor(iDartColor.WHITE));
						cmbReturnReason.setBackground(ResourceUtils
								.getColor(iDartColor.WHITE));
					}
					return;
				} else if (selection.equalsIgnoreCase(DRUG_CHANGE)) {
					txtStopNotes.setEnabled(false);
					btnStopDate.setEnabled(false);
					cmbStopEpisode.setEnabled(false);
					cmbStopEpisode.setBackground(ResourceUtils
							.getColor(iDartColor.WIDGET_BACKGROUND));
					btnPreviousEpisodes.setEnabled(false);
					txtStopNotes.setText("");
					cmbStopEpisode.setText("");
					btnStopDate.setText("Data Fim");
					rbtnDestroyStock.setEnabled(true);
					rbtnReturnToStock.setEnabled(true);
					rbtnDestroyStock.setSelection(false);
					rbtnReturnToStock.setSelection(true);
					return;
				} else if (selection.equalsIgnoreCase(DRUG_LOST)) {
					txtStopNotes.setEnabled(false);
					btnStopDate.setEnabled(false);
					cmbStopEpisode.setEnabled(false);
					cmbStopEpisode.setBackground(ResourceUtils
							.getColor(iDartColor.WIDGET_BACKGROUND));
					btnPreviousEpisodes.setEnabled(false);
					txtStopNotes.setText("");
					cmbStopEpisode.setText("");
					btnStopDate.setText("Data Fim");
					rbtnDestroyStock.setEnabled(true);
					rbtnReturnToStock.setEnabled(false);
					rbtnDestroyStock.setSelection(true);
					rbtnReturnToStock.setSelection(false);

					return;
				} else if (selection.equalsIgnoreCase(APPOINTMENT_MISSED)) {
					txtStopNotes.setEnabled(false);
					btnStopDate.setEnabled(false);
					cmbStopEpisode.setEnabled(false);
					cmbStopEpisode.setBackground(ResourceUtils
							.getColor(iDartColor.WIDGET_BACKGROUND));
					btnPreviousEpisodes.setEnabled(false);
					txtStopNotes.setText("");
					cmbStopEpisode.setText("");
					btnStopDate.setText("Data Fime");
					rbtnDestroyStock.setEnabled(true);
					rbtnDestroyStock.setSelection(false);
					rbtnReturnToStock.setSelection(true);
					rbtnReturnToStock.setEnabled(true);
					return;

				} else if (selection.equalsIgnoreCase("")) { // Empty selection
					// disables the
					// episode
					// group items.
					txtStopNotes.setEnabled(false);
					btnStopDate.setEnabled(false);
					cmbStopEpisode.setEnabled(false);
					cmbStopEpisode.setBackground(ResourceUtils
							.getColor(iDartColor.WIDGET_BACKGROUND));
					btnPreviousEpisodes.setEnabled(false);
					txtStopNotes.setText("");
					cmbStopEpisode.setText("");
					btnStopDate.setText("Data Fim");
					rbtnDestroyStock.setEnabled(false);
					rbtnReturnToStock.setEnabled(false);
					rbtnDestroyStock.setSelection(false);
					rbtnReturnToStock.setSelection(true);
					return;
				} else {
					txtStopNotes.setEnabled(true);
					btnStopDate.setEnabled(true);
					cmbStopEpisode.setEnabled(true);
					cmbStopEpisode.setBackground(ResourceUtils
							.getColor(iDartColor.WHITE));
					btnPreviousEpisodes.setEnabled(true);
					txtStopNotes.setText("");
					cmbStopEpisode.setText("");
					btnStopDate.setText("Data Fim");
					rbtnDestroyStock.setEnabled(true);
					rbtnReturnToStock.setEnabled(true);
					rbtnDestroyStock.setSelection(true);
					rbtnReturnToStock.setSelection(false);
					return;

				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});

		populateCombobox();

		Label lblCaptureDate = new Label(grpPatientPackage, SWT.NONE);
		lblCaptureDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblCaptureDate.setText("Data devolução :");
		lblCaptureDate.setBounds(10, 65, 100, 25);

		btnCaptureDate = new DateButton(
				grpPatientPackage,
				DateButton.ZERO_TIMESTAMP,
				new DateInputValidator(DateRuleFactory.beforeNowInclusive(true)));
		btnCaptureDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnCaptureDate.setText("BotNão Data");
		btnCaptureDate.setBounds(120, 55, 250, 25);

		Label lblDrugAction = new Label(grpPatientPackage, SWT.NONE);
		lblDrugAction.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblDrugAction.setText("O que você quer fazer com esses medicamentos? ");
		lblDrugAction.setBounds(10, 100, 250, 25);

		Composite cmpRbtnSelect = new Composite(grpPatientPackage, SWT.NULL);
		cmpRbtnSelect.setLayout(new RowLayout());
		cmpRbtnSelect.setBounds(10, 125, 390, 50);

		rbtnDestroyStock = new Button(cmpRbtnSelect, SWT.RADIO);
		rbtnDestroyStock.setBounds(5, 30, 360, 20);
		rbtnDestroyStock.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		rbtnDestroyStock
		.setText("Destrua os medicamentos (removidos permanentementedo sistema).");
		rbtnDestroyStock.setEnabled(false);

		rbtnReturnToStock = new Button(cmpRbtnSelect, SWT.RADIO);
		rbtnReturnToStock.setBounds(5, 5, 370, 20);
		rbtnReturnToStock
		.setText("Devolver os medicamentos para (podem ser redispensados a outros pacientes).");
		rbtnReturnToStock.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		rbtnReturnToStock.setEnabled(false);

		Group cmpEpisode = new Group(grpPatientPackage, SWT.NONE);
		cmpEpisode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmpEpisode.setBounds(400, 10, 340, 115);
		cmpEpisode.setText(" episódio");

		Label lblStopEpisode = new Label(cmpEpisode, SWT.NORMAL);
		lblStopEpisode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblStopEpisode.setText("Término : ");
		lblStopEpisode.setBounds(5, 20, 40, 15);

		cmbStopEpisode = new CCombo(cmpEpisode, SWT.BORDER);
		cmbStopEpisode.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		cmbStopEpisode.setText("");
		cmbStopEpisode.setBounds(80, 15, 130, 20);
		CommonObjects
		.populateDeactivationReasons(getHSession(), cmbStopEpisode);
		cmbStopEpisode.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent se) {
				String reason = cmbStopEpisode.getText();
				if (reason.trim().length() > 0) {
					Date stopDate = new Date();
					btnStopDate.setDate((Date) stopDate.clone());
					episodeStopDateChanged = true;
					episodeStopResonChanged = true;
				}
			}
		});

		Label lblOn = new Label(cmpEpisode, SWT.NONE);
		lblOn.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblOn.setText(" em");
		lblOn.setBounds(212, 20, 20, 15);

		btnStopDate = new DateButton(
				cmpEpisode,
				DateButton.ZERO_TIMESTAMP,
				new DateInputValidator(DateRuleFactory.beforeNowInclusive(true)));
		btnStopDate.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnStopDate.setText("Data Término");
		btnStopDate.setBounds(234, 12, 100, 25);
		btnStopDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				episodeStopDateChanged = true;
			}
		});

		Label lblStopNotes = new Label(cmpEpisode, SWT.NONE);
		lblStopNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		lblStopNotes.setText("Notas de Término : ");
		lblStopNotes.setBounds(5, 50, 70, 15);

		txtStopNotes = new Text(cmpEpisode, SWT.BORDER);
		txtStopNotes.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		txtStopNotes.setBounds(80, 45, 255, 20);

		btnPreviousEpisodes = new Button(cmpEpisode, SWT.NONE);
		btnPreviousEpisodes
		.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
		btnPreviousEpisodes.setBounds(5, 75, 329, 32);
		btnPreviousEpisodes.setText("Ver todos os episódios anteriores");
		btnPreviousEpisodes.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent se) {
				// first check that there is consistency between the state of
				// episodes and the patient's account status
				if (localPatient.hasPreviousEpisodes()) {
					epiView = new EpisodeViewer(getHSession(), getShell(),
							localPatient, false);
					epiView.openViewer();
				} else {
					MessageBox noEpisodesWarning = new MessageBox(getShell(),
							SWT.ICON_ERROR | SWT.OK);
					noEpisodesWarning.setText("episódios anteriores do paciente");
					noEpisodesWarning
					.setMessage("O Pacientet "
							+ (txtPatientId.getText()).toUpperCase()
							+ " não tem episódios anteriores para editar.\n"
							+ "Se você deseja terminar o episódio atual, você pode usar\n "
							+ "esta tela para atribuir a data do Término doepisódio para este paciente.");
					noEpisodesWarning.open();
				}
			}
		});
	}

	protected void clearWidgetSelected() {
		txtPatientId.setText("");
		cmbReturnReason.setText("");
		cmbStopEpisode.setText("");
		txtStopNotes.setText("");
		btnCaptureDate.setText("Data de Retorno");
		btnStopDate.setText("Data Término");
		tblPackages.clearAll();
		tblPackages.removeAll();
		tblPackages.redraw();
		tblPackages.setItemCount(0);
		rbtnDestroyStock.setSelection(false);
		rbtnReturnToStock.setSelection(false);
	}

	/**
	 * Method populatePatientHistoryTable.
	 * 
	 * @param table
	 *            Table
	 * @param pat
	 *            Patient
	 */
	private void populatePatientHistoryTable(final Table table,
			final Patient pat) {
		table.clearAll();
		table.removeAll();
		table.redraw();
		table.setItemCount(0);
		tblPackageCols[0].setWidth(125);
		tblPackageCols[1].setWidth(220);
		tblPackageCols[2].setWidth(120);
		tblPackageCols[3].setWidth(120);
		tblPackageCols[4].setWidth(120);
		final List<Packages> packages = new ArrayList<Packages>();
		packages
		.addAll(packReturnFacade.getAllPackagesForPatient(localPatient));
		returnPacks = new ArrayList<Packages>();
		for (Packages pack : packages) {
			if (!pack.isPackageReturned()) {
				String contents = packReturnFacade
				.getPackageDrugsStringContent(pack);
				Date leftDte = pack.getDateLeft();
				String dte0 = sdf.format(pack.getPackDate());
				String dte1 = (leftDte == null ? "-" : sdf.format(leftDte));
				Date receivedDte = pack.getDateReceived();
				String dte2 = (receivedDte == null ? "-" : sdf
						.format(receivedDte));
				Date pickUpDte = pack.getPickupDate();
				String dte3 = (pickUpDte == null ? "-" : sdf.format(pickUpDte));
				if (!dte0.equals("-") && dte1.equals("-") && dte2.equals("-")
						&& dte3.equals("-")) {
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, pack.getPackageId());
					ti.setText(1, contents);
					// Date Packed
					ti.setText(2, dte0);
					ti.setData(pack);
					returnPacks.add(pack);
					// ti.getBounds(0).height = 100;
				} else if (!dte0.equals("-") /*
				 * && (dte1 != "-" | dte2 != "-")
				 */
						&& dte3.equals("-")) {
					TableItem ti = new TableItem(table, SWT.NONE);
					ti.setText(0, pack.getPackageId());
					ti.setText(1, contents);
					// Date Packed
					ti.setText(2, dte0);
					// Date Left
					ti.setText(3, dte1);
					// Date Received
					ti.setText(4, dte2);
					ti.getBounds(0).height = 100;
					returnPacks.add(pack); // Adding
					ti.setData(pack);

				}
			}
		}
		if (1 == table.getItems().length) {
			table.select(0);
		}
		// table.pack();
	}

	private void populateCombobox() {
		Object[] temp = PackageManager.getReturnReasons(getHSession())
		.toArray();

		String[] reasons = new String[temp.length];

		for (int i = 0; i < temp.length; i++) {
			reasons[i] = ((String) temp[i]).trim();
		}

		cmbReturnReason.setItems(reasons);

	}

	private void cmdSearchSelectedWidget() {

		String patientId = PatientBarcodeParser.getPatientId(txtPatientId
				.getText());
		
		PatientSearch search = new PatientSearch(getShell(), getHSession());
		search.setShowPatientsWithPackagesAwaiting(true);
		PatientIdentifier identifier = search.search(patientId);

		if (identifier != null) {
			txtPatientId.setText(identifier.getPatient().getPatientId());
			localPatient = identifier.getPatient();

			clearWidgetSelected();
			enableFields(true);
			txtPatientId.setText(localPatient.getPatientId());
			populatePatientHistoryTable(tblPackages, localPatient);
			enableSpecificControls(new Control[] { txtPatientId,
					btnSearchPatient, txtStopNotes, btnStopDate,
					cmbStopEpisode, btnPreviousEpisodes }, false);
			btnCaptureDate.setText(sdf.format(new Date()));
		} else {
			clearForm();
			txtPatientId.setFocus();
			txtPatientId.setText("");
		}
	}

	@Override
	protected void createContents() {
	}

	/**
	 * Method enableFields.
	 * 
	 * @param enable
	 *            boolean
	 */
	@Override
	protected void enableFields(boolean enable) {
		txtPatientId.setEnabled(!enable);
		btnSearchPatient.setEnabled(!enable);
		cmbReturnReason.setEnabled(enable);
		txtStopNotes.setEnabled(enable);
		btnCaptureDate.setEnabled(enable);
		btnStopDate.setEnabled(enable);
		cmbStopEpisode.setEnabled(enable);

		btnPreviousEpisodes.setEnabled(enable);
		btnSave.setEnabled(enable);
		rbtnDestroyStock.setEnabled(enable);
		rbtnReturnToStock.setEnabled(enable);
		if (enable) {
			cmbReturnReason.setBackground(ResourceUtils
					.getColor(iDartColor.WHITE));
		} else {
			txtPatientId.setFocus();
			cmbStopEpisode.setBackground(ResourceUtils
					.getColor(iDartColor.WIDGET_BACKGROUND));
			cmbReturnReason.setBackground(ResourceUtils
					.getColor(iDartColor.WIDGET_BACKGROUND));
		}

	}

	/**
	 * Method fieldsOk.
	 * 
	 * @return boolean
	 */
	@Override
	protected boolean fieldsOk() {
		return false;
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
	protected void clearForm() {

	}

	@Override
	protected void cmdCancelWidgetSelected() {
		closeShell(true);
	}

	@Override
	protected void cmdClearWidgetSelected() {
		clearWidgetSelected();
		enableFields(false);
	}

	@Override
	protected void cmdSaveWidgetSelected() {
		String saveMsg = "Para salvar este formulário, completa o que vem a seguir:\n";
		int msgInx = 0;

		if (tblPackages.getSelection().length <= 0) {
			msgInx++;
			saveMsg += "\n"
				+ msgInx
				+ ") Por favor, selecione um frasco na tabela \n depois você pode devolve-lo.";
		}

		// Checking if options were properly selected.
		if (cmbReturnReason.getText().equals("")) {
			msgInx++;
			saveMsg += "\n" + msgInx
			+ ") Por favor, selecione um motivo para a devolução de um frasco.";
		} else if (localPatient.getAccountStatusWithCheck()) {
			if (cmbReturnReason.getText().equals(NOLONGER_TREATED_AT_CLINIC)) {
				if (cmbStopEpisode.getText().trim().equals("")) {
					msgInx++;
					saveMsg += "\n"
						+ msgInx
						+ ") Por favor, escolha o motivo terminar este episódio do episódio da lista do menu.";
				}

				if (!episodeStopDateChanged) {
					msgInx++;
					saveMsg += "\n"
						+ msgInx
						+ ") Por favor, seleccione a datade Término deste episódio.";
				}

				if (btnCaptureDate.getText().equals("Data de Retorno")) {
					msgInx++;
					saveMsg += "\n" + msgInx
					+ ") Por favor seleccione a data de retorno.";
				}
			}
		}

		if (!rbtnDestroyStock.getSelection()
				&& !rbtnReturnToStock.getSelection()) {
			msgInx++;
			saveMsg += "\n"
				+ msgInx
				+ ") Por favor, escolha o que você quer fazer com os medicamentos: Devolva-los ou destruí-los.";
		}

		if (!rbtnDestroyStock.getSelection()
				&& !rbtnReturnToStock.getSelection()) {
			msgInx++;
			saveMsg += "\n"
				+ msgInx
				+ ") Por favor, escolha o que você quer fazer com os medicamentos: Devolva-los ou destruí-los?";
		}

		if (tblPackages.getSelection().length > 0 && msgInx <= 0) {
			String confirmBlurb = "";
			TableItem ti = tblPackages.getItem(tblPackages.getSelectionIndex());
			String packId = ti.getText(0);
			String drugsInPack = ti.getText(1);
			Packages packToReturn = (Packages) ti.getData();

			StringTokenizer st = new StringTokenizer(drugsInPack, ",");
			// Building drug string
			String drugList = "\n";
			while (st.hasMoreElements()) {
				drugList += "\t*  " + st.nextToken().trim() + "\n";
			}
			String action = (rbtnDestroyStock.getSelection() ? "destruídos"
					: "devolvidos ao stock");
			confirmBlurb = "Tem certeza de que deseja devolver o frasco '" + packId
			+ "' na farmácia? " + " Este frasco contém:\n "
			+ drugList + "\nNOTE que os medicamentos serNão " + action
			+ ".";
			/*
			 * if (cmbReturnReason.getText().equals( hashTblReasonsForReturn
			 * .get(enumReturnReasons.APPOINTMENT_MISSED))) { confirmBlurb += "
			 * This package return should only be done if patients missed (3) or
			 * more appointments."; }
			 */

			if (cmbReturnReason.getText().equals(NOLONGER_TREATED_AT_CLINIC)) {
				if (cmbStopEpisode.getText().equals("Desconhecido")) {
					confirmBlurb += "\n"
						+ " Certifique-se se o episódio \"Motivo de Término\"\n\t realmente deve ser \"Desconhecido\". ";
				}
			}
			MessageBox mb = new MessageBox(getShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Confirmar a devolução do Frasco: " + cmbReturnReason.getText());
			mb.setMessage(confirmBlurb);
			if (mb.open() == SWT.YES) {
				cmdSavePackageReturn(action, packToReturn);
			} else {
				// Nothing, form stays the same.
			}
		}

		if (msgInx > 0) {
			MessageBox msg = new MessageBox(getShell(), SWT.DIALOG_TRIM);
			msg.setText("Estado da forma dos dados");
			msg.setMessage(saveMsg);
			msg.open();
		}

	}

	/**
	 * Method cmdSavePackageReturn.
	 * 
	 * @param action
	 *            String
	 * @param packageToReturn
	 *            Packages
	 */
	private void cmdSavePackageReturn(String action, Packages packageToReturn) {
		if (packageToReturn == null) {
			getLog().error("Não foi possível obter frasco na base de dados para devolver");
			MessageBox msg = new MessageBox(getShell(), SWT.DIALOG_TRIM);
			msg.setText("Erro na devolução de frasco");
			msg
			.setMessage("Um erro ocorreu enquanto se tentava devolver frasco");
			msg.open();
		} else {
			Transaction tx = null;
			try {
				tx = getHSession().beginTransaction();
				if (btnCaptureDate.getDate() == null) {
					btnCaptureDate.setDate(new Date());
				}
				packReturnFacade.returnPackage(packageToReturn,
						rbtnDestroyStock.getSelection(), btnCaptureDate
						.getDate(),
						cmbReturnReason.getText());

				// Checking if this episode has been closed
				// And then getting the latest episode and
				// closing it. Saving patient to database
				// with the closed episode;
				String stopReason = cmbStopEpisode.getText();
				String stopNotes = txtStopNotes.getText();
				if (episodeStopDateChanged) {
					Episode mostRecentEpisode = PatientManager
					.getMostRecentEpisode(localPatient);
					if (mostRecentEpisode.getId() > 0
							&& mostRecentEpisode.isOpen()) {
						packReturnFacade.closeEpisode(mostRecentEpisode,
								stopReason, btnStopDate.getDate(), stopNotes);
					}
				}

				tx.commit();
				getHSession().flush();

				// Message box shown after the information is saved to the
				// database
				MessageBox msg = new MessageBox(getShell(), SWT.DIALOG_TRIM);
				msg.setText("Frasco devolvido com sucesso.");
				msg
				.setMessage("O Frasco foi devolvido com sucesso na farmácia.");
				msg.open();

				closeShell(true);
			} catch (HibernateException e) {
				getLog().error(
						"Falha: Frasco não foi " + action + " para o frasco: "
						+ packageToReturn.getPackageId(), e);

				if (tx != null) {
					tx.rollback();
				}

				MessageBox msg = new MessageBox(getShell(), SWT.DIALOG_TRIM
						| SWT.ICON_ERROR);
				String errorAction = (action.equals("devolvido") ? "devolvido à farmácia."
						: "destruido.");
				msg.setText("Seu Frasco não foi " + action + ".");
				msg
				.setMessage("Houve um problema ao tentar salvar informação sobre a devolução deste frasco na base de dados. "
						+ "\n\nO frasconão foi "
						+ errorAction);
				msg.open();
			}
		}

	}

	@Override
	protected void createCompButtons() {
		buildCompButtons();
		btnSave.setText("Devolver Frasco não Dispensado");
		Rectangle bounds = btnSave.getBounds();
		bounds.x -= 11;
		bounds.width = 180;
		btnSave.setBounds(bounds);
		Rectangle bounds1 = getCompButtons().getBounds();
		bounds1.width += 100;
		getCompButtons().setBounds(bounds1);
		getCompButtons().pack();
	}
}
