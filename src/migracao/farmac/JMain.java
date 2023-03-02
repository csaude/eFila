/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.farmac;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.commons.io.FileUtils;
import org.celllife.idart.commonobjects.iDartProperties;

import net.lingala.zip4j.exception.ZipException;

/**
 *
 * @author colaco
 */
public class JMain {

    public static void main(String[] args) {
//
//        Session sess = HibernateUtil.getNewSession();
//        JFileChooser busca_arquivo = new JFileChooser(System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FARMAC" + File.separator);
//        File backupFilePath = new File(System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FARMAC" + File.separator);
//        // File backupFilePath = new File(System.getProperty("user.home") + File.separator + "backup_" + sdf.format(new Date()) +"_"+ unidadeSanitaria);
//
//        JBackupController backup = new JBackupController();
//        // to backup
//         PasswordProtectedZip pzip = new PasswordProtectedZip();
//        try {
//            backup.executeCommand(iDartProperties.hibernateDatabase, iDartProperties.hibernatePassword, "backup",iDartProperties.hibernateTableImport, backupFilePath, unidadeSanitaria);
//            pzip.compressWithPassword(backupFilePath.getPath());
//        } catch (ZipException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }


            JFileChooser jfc = new JFileChooser(System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FARMAC" + File.separator, FileSystemView.getFileSystemView());
		jfc.setDialogTitle("Seleccione o ficheiro: ");
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setAcceptAllFileFilterUsed(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(".zip",".7zip");
		jfc.addChoosableFileFilter(filter);
		int returnValue = jfc.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			if (jfc.getSelectedFile().isDirectory()) {
				System.out.println("You selected the directory: " + jfc.getSelectedFile());
			}
		}

        // to restore
        PasswordProtectedZip pzip = new PasswordProtectedZip();
        try {
            pzip.unCompressPasswordProtectedFiles(jfc.getSelectedFile());
        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
          JRestoreController restore = new JRestoreController();
          restore.executeCommand(iDartProperties.hibernateDatabase, iDartProperties.hibernatePassword, "restore", iDartProperties.hibernateTableImport, jfc.getSelectedFile().getPath().replaceAll(".zip", ""),jfc.getSelectedFile());
        if (new File(jfc.getSelectedFile().getPath().replace(".zip", "")).exists()) {
            try {
                FileUtils.deleteDirectory(new File(jfc.getSelectedFile().getPath().replace(".zip", "")));
            } catch (IOException ex) {
                Logger.getLogger(JBackupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
