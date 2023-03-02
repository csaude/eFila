/*
 *
 * iDART: The Intelligent Dispensing of Antiretroviral Treatment
 * Copyright (C) 2006 Cell-Life
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version
 * 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.celllife.idart.start;

import model.manager.AdministrationManager;
import model.manager.PatientManager;
import model.manager.StockManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.celllife.idart.commonobjects.*;
import org.celllife.idart.database.*;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.Clinic;
import org.celllife.idart.database.hibernate.Role;
import org.celllife.idart.database.hibernate.User;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.events.EventManager;
import org.celllife.idart.gui.login.Login;
import org.celllife.idart.gui.login.LoginErr;
import org.celllife.idart.gui.welcome.*;
import org.celllife.idart.integration.eKapa.EkapaSubmitJob;
import org.celllife.idart.integration.eKapa.JobScheduler;
import org.celllife.idart.misc.MessageUtil;
import org.celllife.idart.misc.task.TaskManager;
import org.celllife.idart.rest.ApiAuthRest;
import org.celllife.idart.rest.utils.RestClient;
import org.celllife.idart.rest.utils.RestFarmac;
import org.celllife.idart.rest.utils.RestUtils;
import org.celllife.idart.sms.SmsRetrySchedulerJob;
import org.celllife.idart.sms.SmsSchedulerJob;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import static org.celllife.idart.rest.ApiAuthRest.getServerStatus;

/**
 *
 **/
public class PharmacyApplication {

    private static Logger log = Logger.getLogger(PharmacyApplication.class);
    private static Load loginLoad;

    /**
     * Method main.
     * sacur
     *
     * @param args String[]
     */

    public static void main(String[] args) {

        DOMConfigurator.configure("log4j.xml");

        // used for gui testing
        System.setProperty("org.eclipse.swtbot.search.defaultKey", iDartProperties.SWTBOT_KEY);

//        try {
//            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(PharmacyApplication.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(PharmacyApplication.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(PharmacyApplication.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(PharmacyApplication.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
        log.info("");
        log.info("*********************");
        log.info("iDART " + iDartProperties.idartVersionNumber + " starting");
        log.info("*********************");
        log.info("");

        createDisplay();
        openSplash();
        loadConstants();
        performStartupChecks();
        doInitialisationTasks();
        launch(args);

    }

    private static void createDisplay() {
        if (Display.getCurrent() == null) {
            new Display();
        }
    }

    private static void updateDatabase() {
        try {
            ConexaoJDBC conn = new ConexaoJDBC();
            conn.UpdateDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void launch(String[] args) {

        closeSplash();

        if (args != null && args.length > 0) {
            String option = args[0];
            args = Arrays.copyOfRange(args, 1, args.length);
            TaskManager.runTask(option, args);
        }
        runIDART();
    }

    private static void performStartupChecks() {

        try {
            if (!DatabaseTools._().checkDatabase()) {
                startSetupWizard(DatabaseWizard.PAGE_CREATE_DB);
                updateDatabase();
            }
        } catch (ConnectException e) {
            startSetupWizard(DatabaseWizard.PAGE_CONNECTION_DETAILS);
            updateDatabase();
        } catch (DatabaseEmptyException e) {
            startSetupWizard(DatabaseWizard.PAGE_CREATE_DB);
            updateDatabase();
        } catch (DatabaseException e) {
            String msg = "Error while checking database consistency: ";
            log.error(msg, e);
            showStartupErrorDialog(msg + e.getMessage());
            System.exit(1);
        }

        loginLoad.updateProgress(30);

        try {
            DatabaseTools._().update();
        } catch (DatabaseException e) {
            String msg = "Error while updating the database: "
                    + e.getMessage();
            if (DatabaseTools._().isOldVersion()) {
                msg = "Database needs to be manually updated to version 3.5.0.\n"
                        + "Please run the necessary update scripts and try again.";
            }
            log.error(msg, e);
            showStartupErrorDialog(msg);
            System.exit(1);
        }

        try {
            HibernateUtil.setValidation(false);
        } catch (Exception e) {
            String msg = "Error while checking database consistency: ";
            log.error(msg, e);
            showStartupErrorDialog(msg + e.getMessage());
            System.exit(1);
        }

        loginLoad.updateProgress(50);
    }

    private static void exit(int exitStatus) {
        closeSplash();
        System.exit(exitStatus);
    }

    private static void runIDART() {
        boolean userExited;
        GenericWelcome welcome = null;
        JobScheduler scheduler = new JobScheduler();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        EventManager events = new EventManager();
        events.register();
        do {
            Login loginScreen = new Login();

            if (loginScreen.isSuccessfulLogin()) {
                startEkapaJob(scheduler);
                startSmsJobs(scheduler);

                if (CentralizationProperties.centralization.equalsIgnoreCase("on")) {

                    startRestFarmacThread(executorService);

                    startRestReferedPatientLastDispenseThread(executorService);

                }

                if ((CentralizationProperties.centralization.equalsIgnoreCase("on") && CentralizationProperties.pharmacy_type.equalsIgnoreCase("U"))
                        || CentralizationProperties.centralization.equalsIgnoreCase("off")) {

                    startRestOpenMRSThread(executorService);
                }

                try {
                    Role role = LocalObjects.getUser(HibernateUtil.getNewSession()).getRoleSet().iterator().next();
                    if (role != null && role.isStudyWorker()) {
                        welcome = new StudyWorkerWelcome();
                    } else if (role != null && role.isReportWorker()) {
                        welcome = new ReportWorkerWelcome();
                    } else {
                        if (LocalObjects.currentClinic == LocalObjects.mainClinic) {
                            welcome = new PharmacyWelcome();
                        } else {
                            welcome = new ClinicWelcome();
                        }
                    }
                } catch (Exception e) {
                    log.error("iDART CRASH: - Fatal Error caused by Exception:", e);

                    MessageUtil.showError(e, "iDART Error", MessageUtil.getCrashMessage());
                    closeAllShells();
                    exit(1);

                    log.fatal(e.getMessage(), e);
                }
                log.debug("Logged out..");

                if (welcome == null) {
                    closeAllShells();
                    userExited = true;
                } else {
                    userExited = false;
                }
            } else {
                userExited = true;
            }

        } while (!userExited && welcome != null && welcome.isTimedOut());

        scheduler.shutdown();
        executorService.shutdown();
        events.deRegister();
        log.info("");
        log.info("*********************");
        log.info("iDART " + iDartProperties.idartVersionNumber + " exited");
        log.info("*********************");
        log.info("");
    }

    private static void startSmsJobs(JobScheduler scheduler) {
        if (iDartProperties.isCidaStudy) {
            String cidaGroupName = "cida";
            if (!scheduler.hasJob(cidaGroupName, SmsSchedulerJob.JOB_NAME)) {
                scheduler.scheduleOnceOff(SmsSchedulerJob.JOB_NAME, cidaGroupName, SmsSchedulerJob.class);
            }
            if (!scheduler.hasJob(cidaGroupName, SmsRetrySchedulerJob.JOB_NAME)) {
                scheduler.schedule(SmsRetrySchedulerJob.JOB_NAME, cidaGroupName, SmsRetrySchedulerJob.class, 60);
            }
        }
    }

    private static void startEkapaJob(JobScheduler scheduler) {
        if (iDartProperties.isEkapaVersion) {
            if (!scheduler.hasJob(EkapaSubmitJob.GROUP_NAME, EkapaSubmitJob.JOB_NAME)) {
                scheduler.schedule(EkapaSubmitJob.JOB_NAME, EkapaSubmitJob.GROUP_NAME, EkapaSubmitJob.class, 2);
            }
        }
    }

    public static void startRestFarmacThread(ScheduledExecutorService executorService) {

        final String url = CentralizationProperties.centralized_server_url;

        final PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();

        int synctime = 30;

        if (RestUtils.isNumeric(CentralizationProperties.syncFarmacTimerInSeconds))
            synctime = Integer.parseInt(CentralizationProperties.syncFarmacTimerInSeconds);

        if (synctime < 30)
            synctime = 30;

        executorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {

                Session sess = HibernateUtil.getNewSession();
                Transaction tx = sess.beginTransaction();

                try {
                    if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("P")) {
                        RestFarmac.setCentralPatients(sess);
                        RestFarmac.setCentralDispenses(sess);
                    } else {
                        Clinic mainClinic = AdministrationManager.getMainClinic(sess);
                        if (getServerStatus(url).contains("Red")) {
                            log.trace("Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                            log.info("Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                        } else {

                            if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
                                RestFarmac.restPostMissedPickupPatients(sess, url, pool);
                                log.info("");
                                log.info("*********************");
                                log.info("Open DDD Thread ");
                                RestFarmac.restPostPatients(sess, url, pool);
                                RestFarmac.restPostLocalDispenses(sess, url, pool, CentralizationProperties.last_dispense_before_reference);
                                RestFarmac.restGeAllDispenses(url, mainClinic, pool);
                                RestFarmac.restPostEpisodes(sess, url, pool);
                                RestFarmac.restGeAllEpisodes(url, mainClinic, pool);
                                RestFarmac.restGetAllPatientsModified(url, mainClinic, pool);
                                log.info("*********************");
                                log.info("Close DDD Thread");
                            } else if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("F")) {
                                RestFarmac.restGeAllPatients(url, mainClinic, pool);
                                RestFarmac.restPostDispenses(sess, url, pool);
                            }
                        }

                        if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("F"))
                            RestFarmac.setPatientsFromRest(sess);
                        if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
                            RestFarmac.setPatientFromClinicSector(sess);
                            RestFarmac.setDispensesFromRest(sess);
                            RestFarmac.setEpisodesFromRest(sess, mainClinic);
                        }
                    }
                    assert tx != null;
                    tx.commit();
                    sess.flush();
                    sess.close();
                } catch (IOException e) {
                    assert tx != null;
                    tx.rollback();
                    sess.close();
                    log.trace(new Date() + " : [FARMAC REST] Erro " + e.getMessage());
                    log.info(new Date() + " : [FARMAC REST] Erro " + e.getMessage());
                }
            }

        }, 0, synctime, TimeUnit.SECONDS);

    }

    public static void startRestOpenMRSThread(ScheduledExecutorService executorService) {

        final String url = JdbcProperties.urlBase;
        int synctime = 30;

        if (RestUtils.isNumeric(JdbcProperties.syncOpenmrsTimerInSeconds))
            synctime = Integer.parseInt(JdbcProperties.syncOpenmrsTimerInSeconds);

        if (synctime < 30)
            synctime = 30;

        executorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                log.info("");
                log.info("*********************");
                log.info("Open OpenMRS Thread ");

                Session session = HibernateUtil.getNewSession();

                try {
                    if (getServerStatus(url).contains("Red")) {
                        log.trace(new Date() + " :Servidor OpenMRS offline, verifique a conexao com OpenMRS ou contacte o administrador");
                        log.error(new Date() + " :Servidor OpenMRS offline, verifique a conexao com OpenMRS ou contacte o administrador");
                        log.info(new Date() + " :Servidor OpenMRS offline, verifique a conexao com OpenMRS ou contacte o administrador");
                    }else {
                        User currentUser = LocalObjects.getUser(HibernateUtil.getNewSession());

                        assert currentUser != null;
                        if (ApiAuthRest.loginOpenMRS(currentUser)) {
                            RestClient.setOpenmrsPatients(session);
                            RestClient.setOpenmrsPatientFila(session);
                        } else {
                            log.error("O Utilizador " + currentUser.getUsername() + " não se encontra no OpenMRS ou serviço rest no OpenMRS não está  em funcionamento.");
                        }
                    }
                } catch (IOException e) {
                    log.trace(new Date() + " : Erro " + e.getMessage());
                    log.error(new Date() + " : Erro " + e.getMessage());
                    log.info(new Date() + " : Erro " + e.getMessage());
                }
                session.clear();
                session.close();
                log.info("*********************");
                log.info("Close OpenMRS Thread");
            }
        }, 0, synctime, TimeUnit.SECONDS);

    }

    public static void startRestReferedPatientLastDispenseThread(ScheduledExecutorService executorService) {

        final String url = CentralizationProperties.centralized_server_url;
        final PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
        int synctime = 30;

        executorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                log.info("*********************");
                log.info("Open DDD Last Dispense Thread ");

                Session sess = HibernateUtil.getNewSession();
                Transaction tx = sess.beginTransaction();

                try {
                    if (getServerStatus(url).contains("Red")) {
                        log.trace("Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                        log.info("Servidor Rest offline, verifique a sua internet ou contacte o administrador");
                    } else {
                        if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
                            RestFarmac.restPostLocalDispenses(sess, url, pool, CentralizationProperties.last_dispense_after_reference);
                        }
                    }
                    assert tx != null;
                    tx.commit();
                    sess.flush();
                    sess.close();
                } catch (IOException e) {
                    assert tx != null;
                    tx.rollback();
                    sess.close();
                    log.trace(new Date() + " : [FP Dispense Error on REST - POST] Erro " + e.getMessage());
                    log.info(new Date() + " : [FP Dispense Error on REST - POST] Erro " + e.getMessage());
                }
                log.info("*********************");
                log.info("Close DDD Last Dispense Thread ");
            }
        }, 0, synctime, TimeUnit.SECONDS);


    }

    private static void closeAllShells() {
        Display display = Display.getCurrent();

        if (display != null) {
            for (Shell s : display.getShells()) {
                if (s != null) {
                    s.dispose();
                }
            }
            display.dispose();
        }
    }

    private static void openSplash() {
        loginLoad = new Load();
    }

    private static void loadConstants() {
        try {
            iDartProperties.setiDartProperties();

            if (log.isTraceEnabled()) {
                try {
                    log.trace("Current iDART properties: \n"
                            + iDartProperties.printProperties());
                } catch (Exception e1) {
                    log.error("Error printing properties", e1);
                }
            }
        } catch (Exception e) {
            log.error("Unable to load idart.properties file.", e);
            showStartupErrorDialog("Unable to load properties from idart.properties file." +
                    " Please ensure it exists.");
            System.exit(1);
        }

        try {
            JdbcProperties.setJdbcProperties();

            if (log.isTraceEnabled()) {
                try {
                    log.trace("Current iDART properties: \n"
                            + JdbcProperties.jdbcProperties());
                } catch (Exception e1) {
                    log.error("Error printing properties", e1);
                }
            }
        } catch (Exception e) {
            log.error("Unable to load jdbc.properties file.", e);
            showStartupErrorDialog("Unable to load properties from jdbc.properties file." +
                    " Please ensure it exists.");
            System.exit(1);
        }

        try {
            PrinterProperties.setPrinterProperties();
            if (log.isTraceEnabled()) {
                try {
                    log.trace("Current iDART Printers properties: \n"
                            + PrinterProperties.printProperties());

                } catch (Exception e1) {
                    log.error("Error printing properties", e1);
                }
            } else {
                String formatlabel = null;

                if (PrinterProperties.labelprintPageFormat.equalsIgnoreCase("1"))
                    formatlabel = "PORTRAIT";
                else if (PrinterProperties.labelprintPageFormat.equalsIgnoreCase("0"))
                    formatlabel = "LANDSCAPE";
                else
                    formatlabel = "EMPTY";

                log.info("Current iDART Printers properties: {labelprintHeight= " + PrinterProperties.labelprintHeight +
                        ", labelprintWidth= " + PrinterProperties.labelprintWidth +
                        ", labelBarCodeScalay= " + PrinterProperties.labelBarCodeScalay +
                        ", labelBarCodeScalax= " + PrinterProperties.labelBarCodeScalax +
                        ", labelprintPageFormat= " + formatlabel +
                        "}");
            }
        } catch (Exception e) {
            log.error("Unable to load printer.properties file.", e);
            showStartupErrorDialog("Unable to load properties from printer.properties file." +
                    " Please ensure it exists.");
            System.exit(1);
        }

        try {
            CentralizationProperties.setCentralizationProperties();

            if (log.isTraceEnabled()) {
                try {
                    log.trace("Current Centralization iDART properties: \n"
                            + CentralizationProperties.centralizationProperties());
                } catch (Exception e1) {
                    log.error("Error printing Centralization properties", e1);
                }
            }
        } catch (Exception e) {
            log.error("Unable to load centralization.properties file.", e);
            showStartupErrorDialog("Unable to load properties from centralization.properties file." +
                    " Please ensure it exists.");
            System.exit(1);
        }

        try {
            PropertiesManager.sms();

            if (log.isTraceEnabled()) {
                try {
                    log.trace("Current properties: \n"
                            + PropertiesManager.printProperties());
                } catch (Exception e1) {
                    log.error("Error printing properties", e1);
                }
            }
        } catch (Exception e) {
            log.error("Unable to load sms.properties file.", e);
            showStartupErrorDialog("Unable to load properties from sms.properties file." +
                    " Please ensure it exists.");
            System.exit(1);
        }
        loginLoad.updateProgress(10);
    }

    private static void startSetupWizard(int startPage) {
        closeSplash();
        DatabaseWizard wizard = new DatabaseWizard(startPage);
        Shell shell = new Shell();
        WizardDialog dialog = new WizardDialog(shell, wizard);
        int returnCode = dialog.open();
        if (returnCode == Window.CANCEL) {
            showStartupErrorDialog("Startup failed. Unable to initialise the database.\n"
                    + "Check the error logs in the iDART folder for more information.");
            System.exit(1);
        }
    }

    private static void closeSplash() {
        if (loginLoad.isOpen()) {
            loginLoad.killMe();
        }
    }

    /**
     * Method doInitialisationTasks.
     *
     * @return boolean
     */
    private static boolean doInitialisationTasks() {
        Session hSession = HibernateUtil.getNewSession();
        Transaction tx = null;
        try {
            log.info("Starting Initialisation Tasks");
            tx = hSession.beginTransaction();

            setPatientAttributes();
            CommonObjects.loadLanguages();

            loginLoad.updateProgress(5);

            // set default clinic
            LocalObjects.mainClinic = AdministrationManager
                    .getMainClinic(hSession);

            if (CentralizationProperties.pharmacy_type.equalsIgnoreCase("U")) {
                if (LocalObjects.mainClinic.getUuid() != JdbcProperties.location) {
                    LocalObjects.mainClinic.setUuid(JdbcProperties.location);
                    AdministrationManager.saveClinic(hSession, LocalObjects.mainClinic);
                }
            } else {
                if (CentralizationProperties.location != null && !CentralizationProperties.location.isEmpty())
                    if (LocalObjects.mainClinic.getUuid() != CentralizationProperties.location) {
                        LocalObjects.mainClinic.setUuid(CentralizationProperties.location);
                        AdministrationManager.saveClinic(hSession, LocalObjects.mainClinic);
                    }
            }
            LocalObjects.nationalIdentifierType = AdministrationManager
                    .getNationalIdentifierType(hSession);

            loginLoad.updateProgress(5);

            LocalObjects.pharmacy = AdministrationManager
                    .getPharmacyDetails(hSession);
            // do some database queries while loading
            PatientManager.checkPregnancies(hSession);

            loginLoad.updateProgress(5);

            // update units remaining for stock
            StockManager.updateStockLevels(hSession);

            loginLoad.updateProgress(5);

            hSession.flush();
            tx.commit();
            log.info("Finishing Initialisation Tasks");
            return true;
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            log.error("Hibernate error during startup tasks.", e);
            return false;
        } finally {
            try {
                hSession.close();
            } catch (Exception e) {
            }
        }


    }

    private static void setPatientAttributes() {
        Session sess = HibernateUtil.getNewSession();
        Transaction tx = sess.beginTransaction();

        PatientManager.checkPatientAttributes(sess);

        tx.commit();
        sess.flush();
        sess.close();
    }

    /**
     * Method showStartupErrorDialog.
     *
     * @param message String
     */
    private static void showStartupErrorDialog(String message) {
        closeSplash();
        new LoginErr(message);
    }

}
