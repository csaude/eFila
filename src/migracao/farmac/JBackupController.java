/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.farmac;

/**
 *
 * @author colaco
 */

import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.lowagie.tools.Executable.*;

/**
 *
 * @author jgilson
 */
public class JBackupController {

    Session sess = HibernateUtil.getNewSession();

    public JBackupController() {
    }

    public void executeCommand(String databaseName, String databasePassword, String type, String tableName, File backupFilePath, String unidadeSanitaria) {

        if (!backupFilePath.exists()) {
            File dir = backupFilePath;
            dir.mkdirs();
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String backupFileName = "backup_" + sdf.format(new Date()) +"_"+ unidadeSanitaria + ".sql";

        List<String> commands = getPgComands(databaseName, tableName, backupFilePath, backupFileName, type);
        if (!commands.isEmpty()) {
            try {
                ProcessBuilder pb = new ProcessBuilder(commands);
                pb.environment().put("PGPASSWORD", databasePassword);

                Process process = pb.start();

                try (BufferedReader buf = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line = buf.readLine();
                    while (line != null) {
                        System.err.println(line);
                        line = buf.readLine();
                    }
                }

                process.waitFor();
                process.destroy();

                System.err.println("==========> O Processo de  " + type + " Ocorreu com Sucesso. <==========");


            } catch (IOException | InterruptedException ex) {
                System.err.println("Exception: " + ex);
            }
        } else {
            System.err.println("Error: Parametros Invalidos.");
        }
    }

    private List<String> getPgComands(String databaseName, String tableName, File backupFilePath, String backupFileName, String type) {

        String OS = System.getProperty("os.name").toLowerCase();
        System.err.println(OS);

        ArrayList<String> commands = new ArrayList<>();
        switch (type) {
            case "backup":
                if (isWindows()) {
                    commands.add("C:\\Program Files\\PostgreSQL\\9.5\\bin\\pg_dump");
                } else if(isLinux()){
                    commands.add("/opt/PostgreSQL/9.6/bin/pg_dump");
                }else if(isMac()) {
                    commands.add("/Library/PostgreSQL/9.6/bin/pg_dump");
                }
                commands.add("-h"); //database server host
                commands.add("localhost");
                commands.add("-p"); //database server port number
                commands.add("5432");
                commands.add("-U"); //connect as specified database user
                commands.add("postgres");
                commands.add("-F"); //output file format (custom, directory, tar, plain text (default))
                commands.add("c");
                commands.add("-b"); //include large objects in dump
                commands.add("-v"); //verbose mode
                commands.add("-f"); //output file or directory name
                commands.add(backupFilePath.getAbsolutePath() + File.separator + backupFileName);
                commands.add("-a"); //So os inserts
                commands.add("-d"); //database name
                commands.add(databaseName);
                commands.add("-t"); //Table name
                commands.add(tableName);
                break;
            default:
                return Collections.EMPTY_LIST;
        }
        return commands;
    }

}
