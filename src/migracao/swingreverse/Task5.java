/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import migracao.farmac.JBackupController;
import migracao.farmac.PasswordProtectedZip;
import model.manager.AdministrationManager;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author colaco
 */
public class Task5 extends SwingWorker<String, Void> {

    private final Random rnd = new Random();

    Task5() {
    }

    @Override
    public String doInBackground() {
        System.err.println("AGUARDE UM MOMENTO ....");
        try {

            Session sess = HibernateUtil.getNewSession();
            String unidadeSanitaria = AdministrationManager.getMainClinic(sess).getClinicName().replaceAll(" ", "").trim();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            JBackupController backup = new JBackupController();
            // to backup
            PasswordProtectedZip pzip = new PasswordProtectedZip();
            File backupFilePath = new File(System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FARMAC"+ File.separator + "DISPENSAS" + File.separator + "backup_" + sdf.format(new Date()) + "_" + unidadeSanitaria);

            System.err.println("PROCESSANDO ....");

            try {

                backup.executeCommand(iDartProperties.hibernateDatabase, iDartProperties.hibernatePassword, "backup", iDartProperties.hibernateTableExport, backupFilePath, unidadeSanitaria);
                pzip.compressWithPassword(backupFilePath.getPath());
            } catch (ZipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (backupFilePath.exists()) {
                try {
                    FileUtils.deleteDirectory(backupFilePath);
                } catch (IOException ex) {
                    Logger.getLogger(JBackupController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        } catch (Exception e) {
            System.err.println("ACONTECEU UM ERRO INESPERADO, Contacte o Administrador \n" + e);
            e.printStackTrace();
        }
        return "Done";
    }
}
