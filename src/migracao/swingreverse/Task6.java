/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package migracao.swingreverse;

import migracao.connection.hibernateConection;
import migracao.entidadesHibernate.ExportDispense.PackageDrugInfoExportService;
import migracao.entidadesHibernate.importPatient.PatientImportService;
import migracao.farmac.JRestoreController;
import migracao.farmac.PasswordProtectedZip;
import model.manager.*;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 * @author colaco
 */
public class Task6 extends SwingWorker<String, Void> {

    //Lista dos possiveis Localizacao do logFile
    final static List<String> logFileLocations = new ArrayList<>();
    final static String logFileName = "EnvioDispensasFarmacLogFile.txt";
    private final Random rnd = new Random();
    final static Logger log = Logger.getLogger(Task6.class);
    // Esta classe vai ler e escrever um logFile  com os detalhe das excecpiotns que podem ocorrer
    // durante o processo de uniao de nids. O ficheiro deve ser criado na pasta de instalacao do idart que pode ser
    // C:\\idart ou C:\\Program Files\\idart ou C:\\Program Files (x86)\\idart.
    ReadWriteTextFile rwTextFile = new ReadWriteTextFile();
    String logFile;
    int lengthOfTask = 0;

    Task6() {

//        Alterado para carregar o directorio do Projecto para qualquer Sistema Operacional
//        logFileLocations.add("C:\\iDART_V2017");
//        logFileLocations.add("C:\\Program Files\\idart");
//        logFileLocations.add("C:\\Program Files (x86)\\idart");
//        logFileLocations.add("C:\\idart");
//        logFileLocations.add("C:\\Idart");
        logFileLocations.add(System.getProperty("user.dir"));
       log.trace(System.getProperty("user.dir"));
    }

    @Override
    public String doInBackground() {
        System.err.println("AGUARDE UM MOMENTO ....");
        try {

            PasswordProtectedZip pzip = new PasswordProtectedZip();
            JFileChooser jfc = new JFileChooser(System.getProperty("user.home") + File.separator + "Dropbox" + File.separator + "FARMAC" + File.separator + "DISPENSAS" + File.separator, FileSystemView.getFileSystemView());
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

            try {
                pzip.unCompressPasswordProtectedFiles(jfc.getSelectedFile());
            } catch (ZipException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            JRestoreController restore = new JRestoreController();
            restore.executeCommand(iDartProperties.hibernateDatabase, iDartProperties.hibernatePassword, "restore", iDartProperties.hibernateTableExport, jfc.getSelectedFile().getPath().replaceAll(".zip", ""), jfc.getSelectedFile());

            if (new File(jfc.getSelectedFile().getPath().replace(".zip", "")).exists()) {
                try {
                    FileUtils.deleteDirectory(new File(jfc.getSelectedFile().getPath().replace(".zip", "")));
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            Session session = HibernateUtil.getNewSession();
            PackageDrugInfoExportService packageDrugInfoExportService = new PackageDrugInfoExportService();
            PatientImportService patientImportService = new PatientImportService();
            Clinic clinic = AdministrationManager.getMainClinic(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
            IdentifierType identifierType = AdministrationManager.getNationalIdentifierType(patientImportService.patientImportDao().openCurrentSessionwithTransaction());
            AttributeType attributeType = PatientManager.getAttributeTypeObject(patientImportService.patientImportDao().openCurrentSessionwithTransaction(), "ARV Start Date");
            User user = AdministrationManager.getUserByName(patientImportService.patientImportDao().openCurrentSessionwithTransaction(), "admin");
            if (user == null) {
                user = AdministrationManager.getUserByName(patientImportService.patientImportDao().openCurrentSessionwithTransaction(), "Admin");
            }
            List listPacientes = patientImportService.findAllPatientFromClinic(clinic.getClinicName());

            if (listPacientes != null) {
                lengthOfTask = listPacientes.size();
            }

            System.err.println("PROCESSANDO ....");

            int current = 0;
            int contanonSend = 0;
            int pacientesEmTransito = 0;
            //Esvazia o log file
            logFile = getLogFileLocation();

            try {
                Thread.sleep(this.rnd.nextInt(50) + 1);
                if (lengthOfTask == 0) {
                    System.err.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    System.err.println("#### Sem Dispensas Listadas ####");
                    return "Done";
                }
                //   Users users = usersService.findById("1");

                for (Object patient : listPacientes) {
                    ++current;
                    String nidPaciente = ((Object[]) patient)[0].toString();
                    Date dataDispensa = (Date) ((Object[]) patient)[1];
                    // os erros vao para o logfile
                    try {
                        this.setProgress(100 * current / lengthOfTask);
                        Session sess = HibernateUtil.getNewSession();

                        Patient importedPatient = patientImportService.findByPatientId(nidPaciente);

                        if (importedPatient != null) {

                            List<SyncTempDispense> dispenses = patientImportService.findAllExportedFromPatient(clinic.getClinicName(), importedPatient, dataDispensa);

                            Prescription prescription = getPrescritionFarmacQty0(importedPatient, dispenses, sess);

                            saveDispenseFarmacQty0(prescription, importedPatient, dispenses, clinic, user, sess);

                            System.err.println("**********************************************************************************************************************************************************************************");
                            System.err.println(" Dispensa do Paciente " + importedPatient.getFirstNames() + " " + importedPatient.getLastname() + " com o nid NID " + importedPatient.getPatientId() + " Enviado para o IDART.");
                            System.err.println("**********************************************************************************************************************************************************************************");
                        }

                    } catch (NullPointerException nl) {
                       log.trace("Null pointer exception: " + nl.getCause().toString());
                    } catch (Exception e) {
                        // Podem ocorrer diferentes tipos de exceptions, coomo nao podemos prever todas vamos escreve-las
                        //num logfile e continuar com a execucao ciclo   
                        List<String> listNidsProblematicos = new ArrayList<>();
                        listNidsProblematicos.add("---------------------------------------------------------------------- ----------------------------------------------------------------");
//                        listNidsProblematicos.add("NID: " + patient.getPatientId());
//                        listNidsProblematicos.add("NOME: " + patient.getFirstNames());
//                        listNidsProblematicos.add("APELIDO: " + patient.getLastname());
                        listNidsProblematicos.add("ERRO: " + e.getMessage());
                        rwTextFile.writeSmallTextFile(listNidsProblematicos, logFile);
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
            current = lengthOfTask * 2;

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
                File logFile = new File(logFileLocations.get(i) + File.separator  + logFileName);
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

    // Add for dispense more than 1 month with Qty = 0
    public void saveDispenseFarmacQty0(Prescription prescription, Patient patient, List<SyncTempDispense> syncTempDispense, Clinic clinic, User user, Session sess) {

        Transaction tx = null;
        try {
            // Prescriotion Duration
            tx = sess.beginTransaction();
            java.util.List<PackageDrugInfo> allPackagedDrugsListTemp = new ArrayList<PackageDrugInfo>();

            for (int i = 0; i < syncTempDispense.size(); i++) {

                Calendar theCal = Calendar.getInstance();
                theCal.setTime(syncTempDispense.get(i).getDispensedate());
                //  theCal.add(Calendar.DATE, (4 * i * 7) + (i * 2));
                theCal.add(Calendar.DATE, i * 30);

                Drug drug = DrugManager.getDrug(sess, syncTempDispense.get(i).getDrugname());
                List<Stock> stockList = null;
                Stock stock = null;

                if (drug != null) {
                    stockList = StockManager.getAllStockForDrug(sess, drug);
                }

                if (stockList == null) {
                    stock = StockManager.getAllCurrentStock(sess).get(0);
                } else {
                    if (stockList.size() == 0) {
                        stock = StockManager.getAllCurrentStock(sess).get(0);
                    } else {
                        stock = stockList.get(0);
                    }
                }

                PackageDrugInfo pditemp = new PackageDrugInfo();
                pditemp.setAmountPerTime(0);
                pditemp.setClinic(clinic.getClinicName());
                pditemp.setDispensedQty(0);
                pditemp.setBatchNumber("");
                pditemp.setFormLanguage1("");
                pditemp.setFormLanguage2("");
                pditemp.setFormLanguage3("");
                pditemp.setDrugName(syncTempDispense.get(i).getDrugname());
                pditemp.setExpiryDate(syncTempDispense.get(i).getExpirydate());
                pditemp.setPatientId(syncTempDispense.get(i).getPatientid());
                pditemp.setPatientFirstName(syncTempDispense.get(i).getPatientfirstname());
                pditemp.setPatientLastName(syncTempDispense.get(i).getPatientlastname());
                pditemp.setSpecialInstructions1("");
                pditemp.setSpecialInstructions2("");
                pditemp.setStockId(stock.getId());
                pditemp.setTimesPerDay(syncTempDispense.get(i).getTimesperday());
                pditemp.setNumberOfLabels(0);
                pditemp.setCluser(user);
                pditemp.setDispenseDate(theCal.getTime());
                pditemp.setWeeksSupply(syncTempDispense.get(i).getWeekssupply());
                pditemp.setQtyInHand(syncTempDispense.get(i).getQtyinhand());
                pditemp.setSummaryQtyInHand(syncTempDispense.get(i).getSummaryqtyinhand());
                pditemp.setQtyInLastBatch(syncTempDispense.get(i).getQtyinlastbatch());
                pditemp.setPrescriptionDuration(syncTempDispense.get(i).getDuration());
                pditemp.setDateExpectedString(syncTempDispense.get(i).getDateexpectedstring());
                pditemp.setPickupDate(theCal.getTime());
                pditemp.setNotes("");
                allPackagedDrugsListTemp.add(pditemp);
            }

            savePackageAndPackagedDrugsWhithFarmacQty0(true, allPackagedDrugsListTemp, prescription, clinic, sess);
            TemporaryRecordsManager.savePackageDrugInfosToDB(sess, allPackagedDrugsListTemp);
            sess.flush();
            tx.commit();

        } catch (HibernateException he) {

            MessageBox errorBox = new MessageBox(null, SWT.OK | SWT.ICON_ERROR);
            errorBox.setText("Não pode salvar: Verificar Prescricao");
            errorBox.setMessage("Houve um problema ao salvar a Prescricao. Por favor, tente novamente.");
            if (tx != null) {
                tx.rollback();
            }
//            getLog().error(he);
        }
    }

    // Create or edit Prescription FARMAC
    Prescription getPrescritionFarmacQty0(Patient patient, List<SyncTempDispense> syncTempDispense, Session sess) {

        Prescription prescription = null;

        prescription = PackageManager.getPrescriptionFromPatient(sess, patient, (java.sql.Date) syncTempDispense.get(0).getDispensedate());

        if (prescription == null) {
            prescription = new Prescription();
            Session s = HibernateUtil.getNewSession();
            //   String prescriptionId = PackageManager.getNewPrescriptionId(sess, patient, syncTempDispense.getDate());
            SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
            Doctor doctorProvider = PrescriptionManager.getProvider(sess);
            LinhaT linhat = null;
            RegimeTerapeutico regimeTerapeutico = null;

            prescription.setClinicalStage(0);
            prescription.setCurrent('T');
            prescription.setDate(syncTempDispense.get(0).getDate());
            prescription.setEndDate(syncTempDispense.get(0).getEnddate());
            prescription.setDoctor(doctorProvider);
            prescription.setDuration(syncTempDispense.get(0).getDuration());
            prescription.setModified(syncTempDispense.get(0).getModified());
            prescription.setPatient(patient);
            prescription.setPrescriptionId(patient.getPatientId() + "-" + df.format(syncTempDispense.get(0).getDate()) + " - Farmac");
            prescription.setReasonForUpdate(syncTempDispense.get(0).getReasonforupdate());
            prescription.setNotes("FARMAC: " + syncTempDispense.get(0).getNotes());
            prescription.setRegimeTerapeutico(regimeTerapeutico);
            prescription.setLinha(linhat);
            prescription.setDatainicionoutroservico(syncTempDispense.get(0).getDatainicionoutroservico());
            prescription.setMotivoMudanca(syncTempDispense.get(0).getMotivomudanca());
            prescription.setPpe(syncTempDispense.get(0).getPpe());
            prescription.setPtv(syncTempDispense.get(0).getPtv());
            prescription.setTb(syncTempDispense.get(0).getTb());
            prescription.setGaac(syncTempDispense.get(0).getGaac());
            prescription.setAf(syncTempDispense.get(0).getAf());
            prescription.setFr(syncTempDispense.get(0).getFr());
            prescription.setCa(syncTempDispense.get(0).getCa());
            prescription.setSaaj(syncTempDispense.get(0).getSaaj());
            prescription.setCcr(syncTempDispense.get(0).getCcr());
            prescription.setTpc(syncTempDispense.get(0).getTpc());
            prescription.setTpi(syncTempDispense.get(0).getTpi());
            prescription.setDrugTypes(syncTempDispense.get(0).getDrugtypes());
            prescription.setTipoDoenca("TARV");

            List<PrescribedDrugs> prescribedDrugsList = new ArrayList<PrescribedDrugs>();

            // Save the Prescription Drugs
            for (int i = 0; i < syncTempDispense.size(); i++) {

                Drug drug = DrugManager.getDrug(sess, syncTempDispense.get(i).getDrugname());
                PrescribedDrugs newPD = new PrescribedDrugs();
                if (drug.getPackSize() > 30) {
                    newPD.setAmtPerTime(2);
                } else {
                    newPD.setAmtPerTime(1);
                }
                newPD.setDrug(drug);
                newPD.setModified(syncTempDispense.get(i).getModified());
                newPD.setPrescription(prescription);
                newPD.setTimesPerDay(syncTempDispense.get(i).getTimesperday());
                prescribedDrugsList.add(newPD);
            }

            PackageManager.saveNewPrescription(sess, prescription, true,iDartProperties.SERVICOTARV);
        }

        return prescription;

    }

    public void savePackageAndPackagedDrugsWhithFarmacQty0(boolean dispenseNow,
                                                           java.util.List<PackageDrugInfo> allPackageDrugsList, Prescription prescription, Clinic clinic, Session sess) {

        // if pack date is today, store the time too, else store 12am
        PackageDrugInfo packageDrugInfo = allPackageDrugsList.get(0);
        Set<Packages> packageses = new HashSet();
        packageses.clear();
        Date packDate = new Date();

        Packages newPack = new Packages();

        packDate.setTime(packageDrugInfo.getDispenseDate().getTime());
        newPack.setPickupDate(prescription.getDate());
        newPack.setPackDate(prescription.getDate());
        newPack.setPackageId(packageDrugInfo.getPackageId());
        newPack.setModified('T');
        newPack.setPrescription(prescription);
        newPack.getPrescription().setPackages(packageses);
        newPack.setPackageId(newPack.getPrescription().getPrescriptionId() + "- Farmac");
        newPack.setModified('T');
        newPack.setClinic(clinic);

        //int numPeriods = getSelectedWeekSupply();
        //getLog().info("getSelectedWeekSupply() called");
        // 1 mes tem 4 semanas
        newPack.setWeekssupply(4);
        /*
         * If the pharmacist is giving the drugs to the patient now, set the
         * dateLeft, dateReceived and pickupDate to today. Else ... set these
         * attributes to null (they will be set when the packages have left the
         * pharmacy, arrived at the remote clinic, and when the patient has
         * picked up their medications
         */
        if (dispenseNow) {
            newPack.setDateLeft(prescription.getDate());
            newPack.setDateReceived(prescription.getDate());
            newPack.setPickupDate(prescription.getDate());
        } else {
            if (iDartProperties.downReferralMode
                    .equalsIgnoreCase(iDartProperties.OFFLINE_DOWNREFERRAL_MODE)) {
                newPack.setDateLeft(prescription.getDate());
                newPack.setDateReceived(prescription.getDate());
                newPack.setPickupDate(null);
            } else {
                newPack.setDateLeft(null);
                newPack.setDateReceived(null);
                newPack.setPickupDate(null);
            }
        }

        // Make up a set of package drugs for this particular package
        java.util.List<PackagedDrugs> packagedDrugsList = new ArrayList<PackagedDrugs>();

        for (int ib = 0; ib < allPackageDrugsList.size(); ib++) {

            PackageDrugInfo pdi = allPackageDrugsList.get(ib);
            PackagedDrugs pd = new PackagedDrugs();
            pd.setAmount(pdi.getDispensedQty());
            pd.setParentPackage(newPack);
            pd.setStock(StockManager.getStock(sess, pdi.getStockId()));
            pd.setModified('T');
            packagedDrugsList.add(pd);
            pdi.setPackagedDrug(pd);
            pdi.setNotes(packageDrugInfo.getNotes());
            pdi.setPackageId(newPack.getPackageId());

        }

        newPack.setPackagedDrugs(packagedDrugsList);
        newPack.setDrugTypes("ARV");

        PackageManager.savePackageQty0(newPack);

    }

}
