/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.farmac;

import java.io.File;

import org.celllife.idart.commonobjects.iDartProperties;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class PasswordProtectedZip {

    public PasswordProtectedZip() {

    }

    public void compressWithPassword(String sourcePath) throws ZipException {

        String destPath = sourcePath + ".zip";
        System.err.println("Localizacao do Ficheiro " + destPath);
        ZipFile zipFile = new ZipFile(destPath);
        // Setting parameters
        ZipParameters zipParameters = new ZipParameters();
        zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        zipParameters.setEncryptFiles(true);
        zipParameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
        zipParameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
        // Setting password
        zipParameters.setPassword(iDartProperties.ZIPFILEPASSWORD);

        zipFile.addFolder(sourcePath, zipParameters);
    }

    /**
     * Method for unzipping password protected file
     *
     * @param ficheiro
     * @throws ZipException
     */
    public void unCompressPasswordProtectedFiles(File ficheiro) throws ZipException {
        try {
            String destPath = getFileName(ficheiro.getAbsolutePath());
            System.err.println("Localizacao do Ficheiro  " + destPath);
            ZipFile zipFile = new ZipFile(ficheiro);
            // If it is encrypted then provide password
            if (zipFile.isEncrypted()) {
                zipFile.setPassword(iDartProperties.ZIPFILEPASSWORD);
            }
            zipFile.extractAll(destPath, null);
        } catch (Exception e) {
            System.err.println("FICHEIRO NAO SELECCIONADO/INVALIDO");
            //    e.getStackTrace();
        }
    }

    private String getFileName(String filePath) {
        // Get the folder name from the zipped file by removing .zip extension
        return filePath.substring(0, filePath.lastIndexOf("."));
    }
}
