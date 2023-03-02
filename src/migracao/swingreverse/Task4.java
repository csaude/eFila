/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import migracao.entidadesHibernate.importPatient.PatientImportService;
import migracao.farmac.JBackupController;
import migracao.farmac.JRestoreController;
import migracao.farmac.PasswordProtectedZip;
import model.manager.AdministrationManager;
import model.manager.PatientManager;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.gui.platform.GenericGui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author colaco
 */
public class Task4 extends SwingWorker<String, Void> {

    private final Random rnd = new Random();
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Task4.class);
      GenericGui conn;
      
    Task4() {
    }

    @Override
    public String doInBackground() {
        System.err.println("AGUARDE UM MOMENTO ....");

        PatientImportService patientImportService = new PatientImportService();
        Clinic clinic = AdministrationManager.getMainClinic(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
        IdentifierType identifierType = AdministrationManager.getNationalIdentifierType(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
        AttributeType attributeType = PatientManager.getAttributeTypeObject(patientImportService.patientImportDao().openCurrentSessionwithTransaction(), "ARV Start Date");
        List<SyncTempPatient> patientList = patientImportService.findAllImport();

        int current = 0;
        int npacientesDaFarmac = 0;
        int lengthOfTask = patientList.size();

        try {

            PasswordProtectedZip pzip = new PasswordProtectedZip();
            JFileChooser jfc = new JFileChooser(System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FARMAC" + File.separator+ "REFERENCIAS" + File.separator, FileSystemView.getFileSystemView());
            jfc.setDialogTitle("Seleccione o ficheiro: ");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setAcceptAllFileFilterUsed(true);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(".zip", ".7zip");
            jfc.addChoosableFileFilter(filter);
            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                if (jfc.getSelectedFile().isDirectory()) {
                   log.trace("You selected the directory: " + jfc.getSelectedFile());
                }
            }

            System.err.println("PROCESSANDO ....");

            try {
                pzip.unCompressPasswordProtectedFiles(jfc.getSelectedFile());
            } catch (ZipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
             //   e.printStackTrace();
            }

            JRestoreController restore = new JRestoreController();
            restore.executeCommand(iDartProperties.hibernateDatabase, iDartProperties.hibernatePassword, "restore", iDartProperties.hibernateTableImport, jfc.getSelectedFile().getPath().replaceAll(".zip", ""), jfc.getSelectedFile());
            while (current <= lengthOfTask && !this.isCancelled()) {
                try {
                    Thread.sleep(this.rnd.nextInt(50) + 1);
                    if (lengthOfTask == 0) {
                        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.err.println("#### Sem Pacientes Listados para a Migracao ####");
                        return "Done";
                    }

                    if (new File(jfc.getSelectedFile().getPath().replace(".zip", "")).exists()) {
                        try {
                            FileUtils.deleteDirectory(new File(jfc.getSelectedFile().getPath().replace(".zip", "")));

                            // Inicia a Insercao de pacientes no Idart
                            for (SyncTempPatient patientSync : patientList) {
                                ++current;
                                
                                if (clinic.getClinicName().equalsIgnoreCase(patientSync.getClinicname())){
                                    ++npacientesDaFarmac;
                                }
                                
                                Patient paciente = null ; //DadosPacienteFarmac.InserePaciente(patientSync, clinic);

                            }

                        } catch (IOException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(JBackupController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.err.println("" + npacientesDaFarmac + " Pacientes Importados para o IDART com sucesso!!!!!!");
                        //hibernateConection.getInstanceLocal().close();
                        current = lengthOfTask * 2;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("1. Se o erro persistir,por favor, Contacte o Administrador \n" + e);
                  //  e.printStackTrace();
                } 
            }
            System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("2. Se o erro persistir,por favor, Contacte o Administrador \n" + e);
        //    e.printStackTrace();
        }
        return "Done";
    }
}
