/*
 * Decompiled with CFR 0_114.
 */
package migracao.swingreverse;

import migracao.connection.hibernateConection;
import migracao.connection.hibernateConectionRemote;
import migracao.entidades.*;
import migracao.entidadesHibernate.ExportDispense.PackageDrugInfoExportService;
import migracao.entidadesHibernate.ExportDispense.PatientExportService;
import migracao.entidadesHibernate.importPatient.PatientIdentifierImportService;
import migracao.entidadesHibernate.importPatient.PatientImportService;
import migracao.entidadesHibernate.servicos.*;
import model.manager.AdministrationManager;
import model.manager.PatientManager;
import org.apache.log4j.Logger;
import org.celllife.idart.database.hibernate.Episode;
import org.celllife.idart.database.hibernate.IdentifierType;
import org.celllife.idart.database.hibernate.PatientIdentifier;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.hibernate.Session;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class Task1
        extends SwingWorker<String, Void> {

    //Lista dos possiveis Localizacao do logFile
    final static List<String> logFileLocations = new ArrayList<>();
    final static String logFileName = "EnvioDispensasLogFile.txt";
    final static Logger log = Logger.getLogger(Task1.class);
    private final Random rnd = new Random();
    // Esta classe vai ler e escrever um logFile  com os detalhe das excecpiotns que podem ocorrer
    // durante o processo de uniao de nids. O ficheiro deve ser criado na pasta de instalacao do idart que pode ser
    // C:\\idart ou C:\\Program Files\\idart ou C:\\Program Files (x86)\\idart.
    ReadWriteTextFile rwTextFile = new ReadWriteTextFile();
    String logFile;

    Task1() {

        logFileLocations.add(System.getProperty("user.dir"));
//       log.trace(System.getProperty("user.dir"));
    }

    @Override
    public String doInBackground() {
        System.err.println("AGUARDE UM MOMENTO ....");
        try {
            ConceptService conceptService = new ConceptService();
            EncounterProviderService encounterProviderService = new EncounterProviderService();
            EncounterService encounterService = new EncounterService();
            EncounterTypeService encounterTypeService = new EncounterTypeService();
            FormService formService = new FormService();
            ObsService obsService = new ObsService();
            PatientIdentifierService patientIdentifierService = new PatientIdentifierService();
            PatientExportService patientExportService = new PatientExportService();
            UsersService usersService = new UsersService();
            VisitTypeService visitTypeService = new VisitTypeService();
            VisitService visitService = new VisitService();
            ProviderService providerService = new ProviderService();
            EncounterRoleService encounterRoleService = new EncounterRoleService();
            GlobalPropertyService globalPropertyService = new GlobalPropertyService();
            LocationService locationService = new LocationService();
            PackageDrugInfoExportService packageDrugInfoExportService = new PackageDrugInfoExportService();
            System.err.println("PROCESSANDO ....");
            List<PackageDrugInfo> packageDrugInfos = packageDrugInfoExportService.findAll();
            int current = 0;
            int contanonSend = 0;
            int lengthOfTask = packageDrugInfos.size();
            int pacientesEmTransito = 0;
            //Esvazia o log file
            logFile = getLogFileLocation();
            Session sess = HibernateUtil.getNewSession();

            //        while (current <= lengthOfTask && !this.isCancelled()) {
            try {
                Thread.sleep(this.rnd.nextInt(50) + 1);
                if (lengthOfTask == 0) {
                    System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    System.err.println("#### Sem Dispensas Listadas para a Migracao ####");
                    return "Done";
                }
                Users users = usersService.findById("1");
                Concept concept = conceptService.findById("311");
                VisitType visitType = visitTypeService.findById("8");
                EncounterType encounterType = encounterTypeService.findById("18");
                Form form = formService.findById("130");
                Provider provider = providerService.findById("1");
                EncounterRole encounterRole = encounterRoleService.findById("1");
                GlobalProperty globalProperty = globalPropertyService.findByDefaultName();
                Location location = locationService.findById(globalProperty.getPropertyValue());
                List<Concept> conceptsFarmacia = conceptService.findAll();
                PatientIdentifierImportService patientIdentifierCRAMService = new PatientIdentifierImportService();
                IdentifierType identifierTypeCRAM = AdministrationManager.getCRAMIdentifierType(patientIdentifierCRAMService.patientImportDao().openCurrentSessionwithTransaction());

                for (PackageDrugInfo packageDrugInfo : packageDrugInfos) {
                    ++current;
                    if (packageDrugInfo.getPatientId().length() <= 5 && !packageDrugInfo.getPatientId().contains("/")) {
                        PatientIdentifier patientIdentifier = patientIdentifierCRAMService.findByIdentifier(packageDrugInfo.getPatientId());

                        if (patientIdentifier != null && !patientIdentifier.getType().equals(identifierTypeCRAM)) {
                            patientIdentifier.setType(identifierTypeCRAM);
                            try {
                                patientIdentifierCRAMService.update(patientIdentifier);
                            } catch (Exception e) {
                               log.trace("Paciente com dois NIDS - " + packageDrugInfo.getPatientId());
                            }
                        }
                    } else {

                        // Ultimo episodio do paciente
                        Episode patientLastEpisdode = PatientManager.getLastEpisode(sess, packageDrugInfo.getPatientId());

                        try {
                            this.setProgress(100 * current / lengthOfTask);
                            PatientImportService patientImportService = new PatientImportService();
                            org.celllife.idart.database.hibernate.Patient importedPatient = patientImportService.findByPatientId(packageDrugInfo.getPatientId());
                            String name = "";
                            String surname = "";
                            Patient patient = null;

                            if (importedPatient != null) {
                                name = importedPatient.getFirstNames();
                                surname = importedPatient.getLastname();

                                PatientIdentifierService identifierDao = new PatientIdentifierService();
                                List<migracao.entidades.PatientIdentifier> patientIdentifierOpenmrs = null;

                                if (importedPatient.getUuidopenmrs() == null) {
                                    patientIdentifierOpenmrs = identifierDao.findByNidAndNameAndSurname(packageDrugInfo.getPatientId(), name, surname);
                                } else {
                                    patientIdentifierOpenmrs = identifierDao.findByPatientUuid(importedPatient.getUuidopenmrs());
                                }

                                if (!patientIdentifierOpenmrs.isEmpty()) {
                                    patient = patientExportService.findById(patientIdentifierOpenmrs.get(0).getPatientId().getPatientId() + "");

                                    if (patient != null) {
                                        if (importedPatient.getUuidopenmrs().isEmpty()) {
                                            PersonService personService = new PersonService();
                                            Person person = personService.findById(patient.getPatientId().toString());
                                            patientIdentifierOpenmrs = identifierDao.findByNidAndNameAndSurname(packageDrugInfo.getPatientId(), name, surname);

                                            // grava Uuid para os pacientes que nao tem durante a dispensa
                                            PatientIdentifierImportService patientIdentifierImportService = new PatientIdentifierImportService();

                                            PatientIdentifier importedPatientIdentifier = patientIdentifierImportService.findByIdentifier(packageDrugInfo.getPatientId());

                                            if (importedPatientIdentifier != null) {
                                                org.celllife.idart.database.hibernate.Patient p = importedPatientIdentifier.getPatient();

                                                p.setUuidopenmrs(person.getUuid());
                                                patientImportService.update(p);
                                            }
                                        }

                                        Visit visit = ExportData.InsereVisitas(patient, packageDrugInfo, visitType, location, concept, users, visitService);
                                        Encounter encounter = ExportData.InsereEncounter(visit, packageDrugInfo, encounterType, users, location, form, patient, encounterService);
                                        EncounterProvider encounterProvider = ExportData.InsereEncounterProvider(packageDrugInfo, encounter, users, encounterRole, provider, encounterProviderService);
                                       
                                        for (Concept conceptFarmaciaForm : conceptsFarmacia) {
                                            
                                            ExportData.InsereObs(patient, packageDrugInfo, location, conceptFarmaciaForm, users, encounter, obsService, packageDrugInfoExportService);
                                        }

                                        PackageDrugInfoExportService rapidSave = new PackageDrugInfoExportService();
                                        packageDrugInfo.setNotes("Exported");
                                        rapidSave.update(packageDrugInfo);
                                    } else {
                                        // verifica se nao e paciente em transito
                                        if (!(patientLastEpisdode.getStartReason().contentEquals("Paciente em Transito") | patientLastEpisdode.getStartReason().contentEquals("Inicio na maternidade"))) {
                                            contanonSend++;
                                        } else {
                                            PackageDrugInfoExportService rapidSave = new PackageDrugInfoExportService();
                                            packageDrugInfo.setNotes("Exported");
                                            rapidSave.update(packageDrugInfo);

                                            pacientesEmTransito++;
                                        }

                                    }
                                    System.err.println("**********************************************************************************************************************************************************************************");
                                    System.err.println(" Dispensa do Paciente " + packageDrugInfo.getPatientFirstName() + " " + packageDrugInfo.getPatientLastName() + " com o nid NID " + packageDrugInfo.getPatientId() + " Enviado para o OpenMRS.");
                                    System.err.println("**********************************************************************************************************************************************************************************");
                                } else {
                                    if (patientLastEpisdode.getStartReason().contentEquals("Paciente em Transito") | patientLastEpisdode.getStartReason().contentEquals("Inicio na maternidade")) {
                                        PackageDrugInfoExportService rapidSave = new PackageDrugInfoExportService();
                                        packageDrugInfo.setNotes("Exported");
                                        rapidSave.update(packageDrugInfo);
                                        pacientesEmTransito++;

                                    } else {
                                        contanonSend++;
                                        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                                        System.err.println("Paciente " + packageDrugInfo.getPatientFirstName() + " " + packageDrugInfo.getPatientLastName() + " com o nid NID " + packageDrugInfo.getPatientId() + " nao foi encontrado no OpenMRS. Verifique o NID no OpenMRS ou Contacte o Administrador");
                                        List<String> listNidsProblematicos = new ArrayList<>();
                                        listNidsProblematicos.add("---------------------------------------------------------------------- ----------------------------------------------------------------");
                                        listNidsProblematicos.add("NID: " + packageDrugInfo.getPatientId());
                                        listNidsProblematicos.add("NOME: " + packageDrugInfo.getPatientFirstName());
                                        listNidsProblematicos.add("APELIDO: " + packageDrugInfo.getPatientLastName());
                                        listNidsProblematicos.add("ERRO: " + " nao foi encontrado no OpenMRS. Verifique o NID no OpenMRS ou Contacte o Administrador");
                                        listNidsProblematicos.add("CAUSA: " + "Verificar se no openmrs o nome,nid,apelido do paciente sao iguais");
                                        rwTextFile.writeSmallTextFile(listNidsProblematicos, logFile);
                                        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

                                    }

                                }
                            } else {

                                if (patientLastEpisdode.getStartReason().contentEquals("Paciente em Transito") | patientLastEpisdode.getStartReason().contentEquals("Inicio na maternidade")) {
                                    PackageDrugInfoExportService rapidSave = new PackageDrugInfoExportService();
                                    packageDrugInfo.setNotes("Exported");
                                    rapidSave.update(packageDrugInfo);

                                    pacientesEmTransito++;

                                } else {
                                    contanonSend++;
                                    List<String> listNidsProblematicos = new ArrayList<>();
                                    listNidsProblematicos.add("---------------------------------------------------------------------- ----------------------------------------------------------------");
                                    listNidsProblematicos.add("NID: " + packageDrugInfo.getPatientId());
                                    listNidsProblematicos.add("NOME: " + packageDrugInfo.getPatientFirstName());
                                    listNidsProblematicos.add("APELIDO: " + packageDrugInfo.getPatientLastName());
                                    listNidsProblematicos.add("ERRO: " + "Paciente nao esta registado no iDART , contactar o Administrador");
                                    rwTextFile.writeSmallTextFile(listNidsProblematicos, logFile);
                                }
                            }

                        } catch (NullPointerException nl) {
                           log.trace("Null pointer exception: " + nl.getCause().toString());
                        } catch (Exception e) {
                            // Podem ocorrer diferentes tipos de exceptions, coomo nao podemos prever todas vamos escreve-las
                            //num logfile e continuar com a execucao ciclo   
                            List<String> listNidsProblematicos = new ArrayList<>();
                            listNidsProblematicos.add("---------------------------------------------------------------------- ----------------------------------------------------------------");
                            listNidsProblematicos.add("NID: " + packageDrugInfo.getPatientId());
                            listNidsProblematicos.add("NOME: " + packageDrugInfo.getPatientFirstName());
                            listNidsProblematicos.add("APELIDO: " + packageDrugInfo.getPatientLastName());
                            listNidsProblematicos.add("ERRO: " + e.getMessage());
                            rwTextFile.writeSmallTextFile(listNidsProblematicos, logFile);
                        }

                    }
                }
            } catch (InterruptedException ie) {
                return "Interrupted";
            }
            System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.err.println("" + (lengthOfTask - pacientesEmTransito) + " Total de dispensas por Enviar ao OpenMRS!!!!!!");
            if (contanonSend != 0) {
                System.err.println("" + contanonSend + " Dispensas nao actualizadas no OpenMRS!!!!!!");
            }
            if ((lengthOfTask - contanonSend) != 0) {
                System.err.println("" + (lengthOfTask - contanonSend - pacientesEmTransito) + " Dispensas Actualizadas no OpenMRS com Sucesso!!!!!!");
            }
            hibernateConection.getInstanceLocal().close();
            hibernateConectionRemote.getInstanceRemote().close();
            current = lengthOfTask * 2;
            //        }

        } catch (Exception e) {
            System.err.println("ACONTECEU UM ERRO INESPERADO, Ligue o Servidor OpenMRS e Tente Novamente ou Contacte o Administrador \n" + e.getMessage());
        }
        return "Done";
    }

    public String getLogFileLocation() {

        String fileLocation = "";

        for (int i = 0; i < logFileLocations.size(); i++) {

            File dir = new File(logFileLocations.get(i));

            //Se o Ficheiro nao e encontrado em nenhuma das locations criar o ficheiro
            // No directorio que existir
            if (dir.exists()) {
                File logFile = new File(logFileLocations.get(i) + File.separator + logFileName);
                if (logFile.exists()) {
                    try {
                        logFile.delete();
                        logFile.createNewFile();
                        fileLocation = logFile.getPath();
                        break;
                    } catch (IOException e) {
                       log.trace("cannot create log file" + e.getMessage());
                    }
                } //create new file
                else {
                    try {

                        logFile.createNewFile();
                        fileLocation = logFile.getPath();
                       log.trace(fileLocation + ":  Criado");
                        break;

                    } catch (IOException e) {
                       log.trace("cannot create log file" + e.getMessage());
                    }

                }

            }

        }

        return fileLocation;

    }

}
