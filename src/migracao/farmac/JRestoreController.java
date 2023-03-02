/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.farmac;

import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.lowagie.tools.Executable.*;

/**
 *
 * @author colaco
 */
public class JRestoreController {

    Session sess = HibernateUtil.getNewSession();

    public JRestoreController() {
    }

//    
//     Runtime r = Runtime.getRuntime();
//        try {
//            Process p = r.exec("C:/Arquivos de programas/PostgreSQL/9.1/bin/pg_restore -d "+ tfDataBase.getText().trim() + " -F c -v -h localhost -p 5432 -U postgres " + tfBackup.getText());
//           log.trace(p);
//            if (p != null) {
//                OutputStream outputStream = p.getOutputStream();
//                outputStream.write("123\r\n".getBytes()); //Como vcs podem notar, já estou passando a senha no código, mas isso não está adiantando!
//                outputStream.flush();
//                outputStream.close();
//                InputStreamReader streamReader = new InputStreamReader(p.getErrorStream());
//                BufferedReader reader = new BufferedReader(streamReader);
//                String linha;
//                while ((linha = reader.readLine()) != null) {
//                   log.trace(linha);
//                }
//            }
//            JOptionPane.showMessageDialog(null, "Estrutura do Banco de Dados Restaurada!");
//
//
//        } catch (Exception e) {
//            JOptionPane.showMessageDialog(null, "Erro na Restauração do Banco de Dados.");
//            e.printStackTrace();
//        }
//        
//        
        
    public void executeCommand(String databaseName, String databasePassword, String type, String tableName, String backupFilePath, File fileName) {

        List<String> commands = getPgComands(databaseName, tableName, fileName, backupFilePath, type);
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

    private List<String> getPgComands(String databaseName, String tableName, File fileName, String backupFilePath, String type) {

        String OS = System.getProperty("os.name").toLowerCase();
        System.err.println(OS);

        ArrayList<String> commands = new ArrayList<>();
        switch (type) {
            case "restore":
                if (isWindows()) {
                    commands.add("C:\\Program Files\\PostgreSQL\\9.5\\bin\\pg_restore");
                } else if(isLinux()){
                    commands.add("/opt/PostgreSQL/9.6/bin/pg_restore");
                }else if(isMac()){
                    commands.add("/Library/PostgreSQL/9.6/bin/pg_restore");
                }
                commands.add("-h");
                commands.add("localhost");
                commands.add("-p");
                commands.add("5432");
                commands.add("-U");
                commands.add("postgres");
                commands.add("-d");
                commands.add(databaseName);
                commands.add("-t");
                commands.add(tableName);
                commands.add("-v");
                commands.add(backupFilePath+ File.separator + fileName.getName().replace(".zip", "")+ File.separator + fileName.getName().replaceAll(".zip", ".sql"));
                break;
            default:
                return Collections.EMPTY_LIST;
        }
        return commands;
    }
}
