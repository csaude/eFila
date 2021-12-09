/*
 * Decompiled with CFR 0_114.
 *
 * Could not load the following classes:
 *  org.hibernate.Session
 */
package migracao.swingreverse;

import migracao.connection.hibernateConection;
import migracao.connection.hibernateConectionRemote;
import migracao.entidades.*;
import migracao.entidadesHibernate.importPatient.PatientAttributeImportService;
import migracao.entidadesHibernate.importPatient.PatientIdentifierImportService;
import migracao.entidadesHibernate.importPatient.PatientImportService;
import migracao.entidadesHibernate.importPatient.PatientProgramService;
import migracao.entidadesHibernate.servicos.*;
import model.manager.AdministrationManager;
import model.manager.PatientManager;
import org.celllife.idart.database.hibernate.AttributeType;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.IdentifierType;
import org.celllife.idart.database.hibernate.Patient;

import javax.swing.*;
import java.util.List;
import java.util.Random;

class Task extends SwingWorker<String, Void> {

    private final Random rnd = new Random();

    Task() {
    }

    public String convertIntegerToMonth(int month) {

        String srtMonth = "";
        switch (month) {
            case 1:
                srtMonth = "Jan";
                break;
            case 2:
                srtMonth = "Feb";
                break;
            case 3:
                srtMonth = "Mar";
                break;
            case 4:
                srtMonth = "Apr";
                break;
            case 5:
                srtMonth = "May";
                break;
            case 6:
                srtMonth = "Jun";
                break;
            case 7:
                srtMonth = "jul";
                break;
            case 8:
                srtMonth = "Aug";
                break;
            case 9:
                srtMonth = "Sep";
                break;
            case 10:
                srtMonth = "Oct";
                break;
            case 11:
                srtMonth = "Nov";
                break;
            case 12:
                srtMonth = "Dec";
                break;
            default:
                break;
        }
        return srtMonth;
    }

    @Override
    public String doInBackground() {
        System.err.println("AGUARDE UM MOMENTO ....");
        try {
            PatientService patientService = new PatientService();
            PersonService personService = new PersonService();
            PersonNameService personNameService = new PersonNameService();
            PersonAddressService personAddressService = new PersonAddressService();
            PatientIdentifierService patientIdentifierService = new PatientIdentifierService();
            ObsService obsService = new ObsService();
            PatientProgramService patientProgramService = new PatientProgramService();
            PatientImportService patientImportService = new PatientImportService();
            PatientIdentifierImportService patientIdentifierImportService = new PatientIdentifierImportService();
            PatientAttributeImportService patientAttributeImportService = new PatientAttributeImportService();

            // Alterado por levar tempo no carregamento das consultas        
            //Carrega todos pacientes novos no Programa TARV - Criar na base de dados OpenMRS a coluna idart natabela patient-program
            List<PatientProgram> patientProgramlist = patientProgramService.findAll();

            Clinic clinic = AdministrationManager.getMainClinic(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
            IdentifierType identifierType = AdministrationManager.getNationalIdentifierType(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
            IdentifierType pREPIdentifierType = AdministrationManager.getPrEPIdentifierType(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
            AttributeType attributeType = PatientManager.getAttributeTypeObject(patientImportService.patientImportDao().openCurrentSessionwithTransaction(), "ARV Start Date");

            int current = 0;
            int tarvCount = 0;
            int prepCount = 0;
            int lengthOfTask = patientProgramlist.size();
            String personId = null;
            System.err.println("PROCESSANDO ....");
            while (current <= lengthOfTask && !this.isCancelled()) {
                try {
                    Thread.sleep(this.rnd.nextInt(50) + 1);
                    if (lengthOfTask == 0) {
                        System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        System.err.println("#### Sem Pacientes Listados para a Migracao ####");
                        return "Done";
                    }
                    for (PatientProgram patientProgram : patientProgramlist) {

                        try {

                            ++current;
                            String dataTarv = null;
                            personId = patientProgram.getPatientId().getPatientId().toString();
                            // if (patientImportService.findByPatientId(patientProgram.getPatientId().getPatientId().toString()) != null && (patientImportService.findByPatientId(patientProgram.getPatientId().getPatientId().toString()) == null))
                            //     continue;
                            this.setProgress(100 * current / lengthOfTask);
                            PersonName personName = personNameService.findByPersonId(personId);
                            PersonAddress personAddress = personAddressService.findByPersonId(patientProgram.getPatientId().getPatientId().toString());
                            PatientIdentifier patientIdentifier = patientIdentifierService.findByPatientId(patientProgram.getPatientId().getPatientId().toString());
                            Person person = personService.findById(patientProgram.getPatientId().getPatientId().toString());
                            String numeroTelefone = personService.findByCellphone(person.getPersonId());
                            String locationUuid = patientProgram.getLocationId().getUuid();

                            Patient patient = DadosPaciente.InserePaciente(locationUuid, patientIdentifier.getIdentifier(), person, personName, personAddress, clinic, patientImportService, patientProgram.getPatientId().getPatientId().toString(), numeroTelefone);

                            if (patientProgram.getProgramId().getProgramId() != 2) {
                                prepCount++;
                                DadosPaciente.InserePatientIdentifier(patient, pREPIdentifierType, patientIdentifier.getIdentifier(), patientIdentifierImportService);
                            } else {
                                tarvCount++;
                                DadosPaciente.InserePatientIdentifier(patient, identifierType, patientIdentifier.getIdentifier(), patientIdentifierImportService);

                                String dateopenmrs = patientProgram.getDateEnrolled().toString();
                                String year = dateopenmrs.substring(0, 4);
                                String month = convertIntegerToMonth(Integer.parseInt(dateopenmrs.substring(5, 7)));
                                String day = dateopenmrs.substring(8, 10);
                                String dateEnrolled = day + " " + month + " " + year;

                                DadosPaciente.InserePatientAttribute(patient, dateEnrolled, attributeType, patientAttributeImportService);
                            }

                            PatientProgramService patientProgramServiceActualiza = new PatientProgramService();
                            patientProgram.setIdart("Imported");
                            patientProgramServiceActualiza.update(patientProgram);
                            //   ObsService obsServiceActualizacao = new ObsService();
                            //   obj.setComments("Imported");
                            //   obsServiceActualizacao.update(obj);
                            System.err.println(personName.getGivenName() + " " + personName.getFamilyName() + " Paciente com o NID " + patientIdentifier.getIdentifier() + " Inserido com Sucesso");

                        } catch (Exception e) {
                            PersonName personName = personNameService.findByPersonId(patientProgram.getPatientId().toString());
                            System.err.println("Dados do paciente em falta: (NID) " + personName.getGivenName() + " " + personName.getMiddleName() + " " + personName.getFamilyName());
                            break;

                        } finally {

                            continue;
                        }
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    return "Interrupted";
                }
                System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.err.println("" + lengthOfTask + " Pacientes Importados do OpenMRS para o IDART com sucesso!!!!!!");
                System.err.println("TARV - " + tarvCount + " Pacientes !!!!!!");
                System.err.println("PrEP - " + prepCount + " Pacientes !!!!!!");
                hibernateConection.getInstanceLocal().close();
                hibernateConectionRemote.getInstanceRemote().close();
                current = lengthOfTask * 2;
            }
        } catch (Exception e) {

            System.err.println("ACONTECEU UM ERRO INESPERADO, Ligue o Servidor OpenMRS e Tente Novamente ou Contacte o Administrador \n" + e);
            e.printStackTrace();
        }
        return "Done";
    }
}
