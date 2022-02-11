package org.celllife.idart.gui.patient;

import com.sun.java.swing.plaf.windows.WindowsFileChooserUI;
import model.manager.AdministrationManager;
import model.manager.PatientManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.celllife.idart.commonobjects.CentralizationProperties;
import org.celllife.idart.commonobjects.CommonObjects;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.ClinicSector;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.gui.platform.GenericFormGui;
import org.celllife.idart.gui.utils.ResourceUtils;
import org.celllife.idart.gui.utils.iDartColor;
import org.celllife.idart.gui.utils.iDartFont;
import org.celllife.idart.gui.utils.iDartImage;
import org.celllife.idart.rest.utils.RestFarmac;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.hibernate.Session;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

public class UploadPatient extends GenericFormGui {


    private Group grpClinicSearch;

    private Group grpClinics;

    private CCombo cmbClinicSectorType;

    private CCombo cmbClinicSector;

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
    public UploadPatient(Shell parent) {
        super(parent, HibernateUtil.getNewSession());
    }

    /**
     * This method initializes newClinic
     */
    @Override
    protected void createShell() {

        String shellTxt = "Exportar Lista de Faltosos";
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
        String headerTxt = "Exportar Lista de Faltosos";
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
        grpClinicSearch.setBounds(new Rectangle(33, 70, 500, 150));

        Label lblInstructions = new Label(grpClinicSearch, SWT.CENTER);
        lblInstructions.setBounds(new Rectangle(70,
                15, 260, 20));
        lblInstructions.setText("Todos campos marcados com * são obrigatorios");
        lblInstructions.setFont(ResourceUtils
                .getFont(iDartFont.VERASANS_8_ITALIC));

        Label labelSector = new Label(grpClinicSearch, SWT.CENTER);
        labelSector.setBounds(new Rectangle(20, 50, 140, 20));
        labelSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        labelSector.setText("Tipo de Sector Clínico:");

        cmbClinicSectorType = new CCombo(grpClinicSearch, SWT.BORDER | SWT.READ_ONLY);
        cmbClinicSectorType.setBounds(new Rectangle(160, 50, 220, 20));
        cmbClinicSectorType.setEditable(false);
        cmbClinicSectorType.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbClinicSectorType.setBackground(ResourceUtils.getColor(iDartColor.WHITE));
        cmbClinicSectorType.setForeground(ResourceUtils.getColor(iDartColor.BLACK));
        CommonObjects.populateClinicSectorType(getHSession(), cmbClinicSectorType);
        cmbClinicSectorType.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String newItems[] = {};
                cmbClinicSector.setItems(newItems);
                CommonObjects.populateClinicSectorBySectorType(getHSession(),cmbClinicSectorType.getText(), cmbClinicSector);
            }
        });

        Label labelSectorClinico = new Label(grpClinicSearch, SWT.CENTER);
        labelSectorClinico.setBounds(new Rectangle(20, 80, 140, 20));
        labelSectorClinico.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        labelSectorClinico.setText("Sector Clínico de alocação:");

        cmbClinicSector = new CCombo(grpClinicSearch, SWT.BORDER);
        cmbClinicSector.setBounds(new Rectangle(160, 80, 220, 20));
        cmbClinicSector.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));
        cmbClinicSector.setEditable(false);

        // btnSearch
        btnSearch = new Button(grpClinicSearch, SWT.NONE);
        btnSearch.setBounds(new Rectangle(160, 110, 152, 30));
        btnSearch.setText("Carregar Excel");
        btnSearch.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));

        btnSearch.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(
                    SelectionEvent e) {

                JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
//                FileNameExtensionFilter onlyExcel = new FileNameExtensionFilter("Ficheios EXCEL", "xls", "xlsx");
//                jfc.setFileFilter(onlyExcel);
                final List<Patient> patientList = new ArrayList<>();
                int returnValue = 2000; //jfc.showOpenDialog(null);
                
                FileDialog dialog = new FileDialog (getParent(), SWT.OPEN | SWT.MULTI);
                String [] filterNames = new String [] {"Microsoft Excel Spreadsheet Files (*.xls)"};
                String [] filterExtensions = new String [] {"*.xls"};
                String filterPath = FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath();
                dialog.setFilterNames (filterNames);
                dialog.setFilterExtensions (filterExtensions);
                dialog.setFilterPath (filterPath);
                dialog.open();
                
                StringBuilder filePath = new StringBuilder(dialog.getFilterPath());
                filePath.append(File.separator);
                filePath.append(dialog.getFileName());
                System.out.println(filePath.toString());
                File selectedFile = new File(filePath.toString());
                
                if (selectedFile.exists() && selectedFile.isFile()) {
                    System.out.println(selectedFile.getAbsolutePath());

                    try {
                        FileInputStream file = new FileInputStream(selectedFile);
                        Workbook workbook = new HSSFWorkbook(file); // XSSFWorkbook
                        final DataFormatter dataFormatter = new DataFormatter();
                        final Iterator<Sheet> sheets = workbook.sheetIterator();
                        // inicia monitoria
                        IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress() {
                            public void run(IProgressMonitor monitor)
                                    throws InvocationTargetException, InterruptedException {
                                while(sheets.hasNext()) {
                                    Sheet sh = sheets.next();
                                    if(sh.getSheetName().contains("2")){
                                        System.out.println("Sheet name is "+sh.getSheetName());
                                        System.out.println("------------------------------------------------------------------------");
                                        int countPatients = 1;
                                        Iterator<Row> iterator = sh.iterator();
                                        monitor.beginTask("Carregamento de Pacientes Faltosos e/Abandonos", sh.getLastRowNum());
                                        while(iterator.hasNext()) {
                                            try {
                                                if (monitor.isCanceled()) {
                                                    monitor.done();
                                                    return;
                                                }

                                                Row row = iterator.next();
                                                Iterator<Cell> cellIterator = row.iterator();
                                                while (cellIterator.hasNext()) {
                                                    Cell cell = cellIterator.next();
                                                    if (cell.getColumnIndex() == 0) {
                                                        String cellValue = dataFormatter.formatCellValue(cell);

                                                        Patient p = PatientManager.getPatient(getHSession(), cellValue);

                                                        if (p != null)
                                                            patientList.add(p);
                                                        else
                                                            System.out.println("Paciente com o NID [" + cellValue + "] nao foi localizado.");
                                                    }
                                                }
                                                monitor.subTask("Carregando : " + countPatients++ + " de " + sh.getLastRowNum() + "...");
                                                monitor.worked(1);
                                                Thread.sleep(10);
                                                System.out.println();
                                            }catch (Exception ex){
                                                monitor.done();
                                                MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                                                b.setMessage("Erro ao processar o documento carregado");
                                                b.setText("Erro ao processar o documento carregado, por favor verifique se o mesmo contém o formato recomendado");
                                                b.open();
                                                ex.printStackTrace();
                                            }
                                        }
                                        // termina monitoria
                                    }
                                }
                                monitor.done();
                            }
                        };

                        ProgressMonitorDialog dialogRun = new ProgressMonitorDialog(getParent());
                        try {
                            dialogRun.run(true, true, runnableWithProgress);
                        } catch (InvocationTargetException ex) {
                            ex.printStackTrace();
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        if(!patientList.isEmpty()){
                            populatePatients(patientList);
                        }else {
                            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                            b.setMessage(" Nenhum paciente da lista carregada foi encontrado no iDART");
                            b.setText("Nenhum paciente da lista carregada foi encontrado no iDART");
                            b.open();
                        }
                        workbook.close();
                    }
                    catch(Exception ex) {
                        MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                        b.setMessage("Erro ao processar o documento carregado");
                        b.setText("Erro ao processar o documento carregado, por favor verifique se o mesmo contém o formato recomendado");
                        b.open();
                        ex.printStackTrace();
                    }
                }
            }
        });
        btnSearch.setToolTipText("Pressione para carregar a lista de pacientes faltosos.");
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
        } else if (cmbClinicSector.getText().trim().equals("") || cmbClinicSector.getText().trim().equals("Selecione ...")) {
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" O campo sector nao pode estar em branco.");
            b.setText("Campos em branco");
            b.open();
            cmbClinicSector.setFocus();
            fieldsOkay = false;
        }else if(tblColumns.getCheckedElements().length <= 0){
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
        ArrayList<Patient> patientSelectedList = new ArrayList<>();
        Clinic mainClinic = AdministrationManager.getMainClinic(getHSession());
        ClinicSector clinicSector = AdministrationManager.getSectorByName(getHSession(), cmbClinicSector.getText());
        String result = "";

        if (fieldsOk()) {

            try {
                Object[] obj = tblColumns.getCheckedElements();
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] instanceof Patient) {

                        Patient patient = (Patient) obj[i];
                        patientSelectedList.add(patient);
                    }
                }
                if(patientSelectedList.isEmpty()){
                    showMessage( MessageDialog.INFORMATION, "Lista de pacientes vazia",
                            "A lista de pacienntes esta vazia, por favor, verifique o ficheiro carregado.");
                } else {
                    for(Patient downRefferalPatient : patientSelectedList){
                        DownReferDialog downReferDialog = new DownReferDialog(getParent(),getHSession(), downRefferalPatient);
                        downReferDialog.saveReferredPatient(downRefferalPatient, mainClinic, clinicSector, mainClinic, getHSession(), "Faltoso");
                    }
                    if(result.trim().isEmpty())
                        result = "Carregamento de pacientes da lista efectuado com sucesso";

                    showMessage(MessageDialog.INFORMATION, "Carregamento de pacientes da lista",
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
        lblClinicTableHeader.setBounds(new Rectangle(200, 230, 200, 20));
        lblClinicTableHeader.setText("Lista de Pacientes Faltosos e/Abandonos");
        lblClinicTableHeader.setAlignment(SWT.CENTER);
        lblClinicTableHeader.setFont(ResourceUtils.getFont(iDartFont.VERASANS_8));


        lnkSelectAllColumns = new Link(getShell(), SWT.NONE);
        lnkSelectAllColumns.setBounds(new Rectangle(70, 260, 450, 30));
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
                new Rectangle(85, 290, 420, 150));
        tblColumns.getTable().setFont(
                ResourceUtils.getFont(iDartFont.VERASANS_8));
        tblColumns.setContentProvider(new ArrayContentProvider());
    }

    private void populatePatients(List<Patient> patientList) {
        tblColumns.setInput(null);
        tblColumns.setInput(patientList);

        if(patientList.isEmpty()){
            btnSave.setEnabled(false);
            MessageBox b = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
            b.setMessage(" Nenhum resultado foi encontrado ");
            b.setText("Nenhum resultado foi encontrado");
            b.open();
        }else
            btnSave.setEnabled(true);
    }

}
