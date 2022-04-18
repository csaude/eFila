/*
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
package model.manager;

import model.nonPersistent.PharmacyDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.util.HibernateUtil;
import org.celllife.idart.rest.utils.RestUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class AdministrationManager {

    private static Log log = LogFactory.getLog(AdministrationManager.class);

    // ------- METHODS FOR DOCTOR MANAGER --------------------------------

    /**
     * Method getAllDoctors.
     *
     * @param sess Session
     * @return List<Doctor>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Doctor> getAllDoctors(Session sess)
            throws HibernateException {
        List<Doctor> result = sess.createQuery(
                "select d from Doctor as d order by upper(d.lastname)").list();

        return result;
    }


    public static Doctor getMostUsedDoctor( Session sess) throws HibernateException, SQLException, ClassNotFoundException {

        ConexaoJDBC conexaoJDBC = new ConexaoJDBC();
        int doctorId = conexaoJDBC.idMostUsedDoctor();
        Doctor result = (Doctor) sess.createQuery(
                 "select d from Doctor as d where d.id = "+ doctorId).uniqueResult();
        return result;
    }
    /**
     * Devolve lista de regimes para alimentar o combobox no formulario de
     * prescricao
     */
    @SuppressWarnings("unchecked")
    public static List<RegimeTerapeutico> getAllRegimes(Session sess)
            throws HibernateException {

        List<RegimeTerapeutico> result = sess.createQuery(
                "select r from RegimeTerapeutico as r").list();

        return result;
    }

    /**
     * Devolve lista de regimes para alimentar o combobox no formulario de
     * prescricao
     */
    @SuppressWarnings("unchecked")
    public static List<RegimeTerapeutico> getAllRegimesByDiseaseType(Session sess, String diseaseType)
            throws HibernateException {

        List<RegimeTerapeutico> result = sess.createQuery(
                "select r from RegimeTerapeutico as r where r.tipoDoenca = '"+ diseaseType +"'").list();

        return result;
    }


    /**
     * Devolve lista de linhas terapeuticas para alimentar o combobox no
     * formulario de prescricao
     */
    @SuppressWarnings("unchecked")
    public static List<LinhaT> getAllLinhas(Session sess)
            throws HibernateException {

        List<LinhaT> result = sess.createQuery(
                "select l from LinhaT as l)").list();

        return result;
    }

    public static List<SimpleDomain> getAllDiseases(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description = 'Disease' order by s.value asc";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    public static List<SimpleDomain> getAllTakePeriod(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description = 'Period' order by s.id asc";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    public static List<SimpleDomain> getAllMotivoPrescricao(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description = 'prescription_reason' order by s.id asc";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    public static List<SimpleDomain> getAllModoDispensa(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description = 'dispense_mode' order by s.id asc";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    public static List<SimpleDomain> getAllModoDispensaByDescriptionLike(Session sess, String description)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description = 'dispense_mode' and s.value like '%" +description+"%' order by s.id asc";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    /**
     * Saves the current doctor
     *
     * @param s         Session
     * @param theDoctor Doctor
     * @throws HibernateException
     */
    public static void saveDoctor(Session s, Doctor theDoctor)
            throws HibernateException {
        // if this is the 1st time we're accessing the doctor List
        s.save(theDoctor);
    }

    /**
     * Method getDoctor.
     *
     * @param sess            Session
     * @param doctorsFullName String
     * @return Doctor
     */
    public static Doctor getDoctor(Session sess, String doctorsFullName) {
        Doctor theDoc = null;
        List<Doctor> docList = AdministrationManager.getAllDoctors(sess);
        if (docList != null) {
            for (int i = 0; i < docList.size(); i++) {
                theDoc = docList.get(i);
                if (theDoc.getFullname().equals(doctorsFullName)) {
                    break;
                }
            }
        }
        return theDoc;
    }

    /*
     *
     * Devolve um regime terapeutico
     */
    public static RegimeTerapeutico getRegimeTerapeutico(Session sess, String regimeesquema) {
        RegimeTerapeutico regime = null;
        List<RegimeTerapeutico> regimeList = AdministrationManager.getAllRegimes(sess);
        if (regimeList != null) {
            for (int i = 0; i < regimeList.size(); i++) {
                regime = regimeList.get(i);
                if (regime.getRegimeesquema().equalsIgnoreCase(regimeesquema)) {
                    break;
                }
            }
        }
        return regime;
    }

    public static RegimeTerapeutico getRegimeTerapeuticoRest(Session sess, String regimeesquema) {
        RegimeTerapeutico regime = null;
        List<RegimeTerapeutico> regimeList = AdministrationManager.getAllRegimes(sess);
        if (regimeList != null) {
            for (int i = 0; i < regimeList.size(); i++) {
                regime = regimeList.get(i);
                if (regime.getRegimeesquema().contains(regimeesquema)) {
                    break;
                }
            }
        }
        return regime;
    }

    /**
     * Devolve uma linha Terapeutica
     *
     * @param sess
     * @param linhat
     * @return
     */
    public static LinhaT getLinha(Session sess, String linhat) {
        LinhaT linha = null;
        List<LinhaT> lt = AdministrationManager.getAllLinhas(sess);
        if (lt != null) {
            for (int i = 0; i < lt.size(); i++) {
                linha = lt.get(i);
                if (linha.getLinhanome().equals(linhat)) {
                    break;
                }else linha = lt.get(0);
            }
        }
        return linha;
    }

    //

    public static Motivomudanca getMotivoMudanca(Session sess, String motivomudanca) {
        Motivomudanca motivo = null;
        List<Motivomudanca> motivoList = AdministrationManager.getAllMotivos(sess);
        if (motivoList != null) {
            for (int i = 0; i < motivoList.size(); i++) {
                motivo = motivoList.get(i);
                if (motivo.getMotivo().equals(motivomudanca)) {
                    break;
                }
            }
        }
        return motivo;
    }

    //Previous regime
    public static String loadRegime(int idPatient, String tipoPaciente) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaRegime(idPatient, tipoPaciente);

    }

    //Previous Dispensa Trimestral
    public static int loadDispensaTrimestral(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaDispensaTrimestral(idPatient);

    }

    //Previous Dispensa Semestral
    public static int loadDispensaSemestral(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaDispensaSemestral(idPatient);

    }

    //Previous Linha
    public static String loadLinha(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaLinha(idPatient);

    }

    //Previous PPE
    public static String loadPpe(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaPpe(idPatient);

    }

    //Previous PrEP
    public static String loadPrEP(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaPrEP(idPatient);

    }

    //Previous CE
    public static String loadCE(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaCE(idPatient);

    }

    //Previous CCR
    public static String loadCcr(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaCcr(idPatient);

    }

    //Previous CPN
    public static String loadCpn(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaCpn(idPatient);

    }

    //Previous AF
    public static String loadAf(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaAf(idPatient);

    }

    //Previous CA
    public static String loadCa(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaAf(idPatient);

    }

    //Previous FR
    public static String loadFr(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaFr(idPatient);

    }

    //Previous FR
    public static String loadGaac(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaGaac(idPatient);

    }

    //Previous DC
    public static String loadDc(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaDc(idPatient);

    }

    //Previous TB
    public static String loadTb(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaTb(idPatient);

    }

    //Previous TB
    public static String loadSAAJ(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaSAAJ(idPatient);

    }

    //Previous PrescricaoEspecial
    public static String loadPrescricaoEspecial(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaPrescricaoEspecial(idPatient);

    }

    //Previous Motivo Especial
    public static String loadMotivoEspecial(int idPatient) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaMotivoCriacaEspecial(idPatient);

    }

    //Previous Pediatric or Adult ARV
    public static String loadPediatric(int iddrug) throws ClassNotFoundException, SQLException {
        ConexaoJDBC conn = new ConexaoJDBC();

        return conn.carregaTb(iddrug);

    }

    // ------- METHODS FOR CLINIC MANAGER --------------------------------

    /**
     * Return the Default Clinic's name
     *
     * @param sess
     * @return String
     */
    public static String getDefaultClinicName(Session sess) {

        Clinic mainClinic = getMainClinic(sess);

        if (mainClinic != null) {
            return mainClinic.getClinicName();
        } else {
            log.warn("Returning first clinic found, not default clinic");
            return getClinicNames(sess).get(0);
        }

    }

    /**
     * Return the Default Clinic (usually located at the main StockCenter)
     *
     * @param sess
     * @return Clinic
     * @throws HibernateException
     */
    public static Clinic getMainClinic(Session sess) throws HibernateException {

        Clinic c = (Clinic) sess.createQuery(
                "select c from Clinic as c where c.mainClinic = true")
                .uniqueResult();
        if (c == null) {
            log.warn("Default clinic not found");
        }
        return c;

    }

    public static ClinicSector getClinicSectorFromUUID(Session sess, String uuid) throws HibernateException {

        ClinicSector clinicSector = (ClinicSector) sess.createQuery(
                "select cs from ClinicSector as cs where cs.uuid = :uuid")
                .setString("uuid", uuid).setMaxResults(1).uniqueResult();
        if (clinicSector == null) {
            log.warn("Clinic Sector [ "+ uuid +" ] not found");
        }
        return clinicSector;

    }

    public static ClinicSectorType getClinicSectorTypeByDescription(Session sess, String description) throws HibernateException {

        ClinicSectorType clinicSectorType = (ClinicSectorType) sess.createQuery(
                "select cst from ClinicSectorType as cst where cst.description = :description")
                .setString("description", description).setMaxResults(1).uniqueResult();
        if (clinicSectorType == null) {
            log.warn("Clinic Sector Type[ "+ description +" ] nao foi encontrado");
        }
        return clinicSectorType;

    }

    /**
     * Return all Clinic Names
     *
     * @param sess
     * @return List<String>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<String> getClinicNames(Session sess)
            throws HibernateException {
        List<String> clinicList = sess
                .createQuery(
                        "select c.clinicName from Clinic as c order by c.mainClinic DESC")
                .list();

        return clinicList;
    }

    /**
     * Return all Clinics
     *
     * @param sess
     * @return List<Clinic>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Clinic> getClinics(Session sess)
            throws HibernateException {
        List<Clinic> clinicList = sess.createQuery(
                "select c from Clinic as c order by c.mainClinic desc, c.clinicName asc")
                .list();

        return clinicList;
    }

    /**
     * Return all Clinics
     *
     * @param sess
     * @return List<Clinic>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SystemFunctionality> getSystemFunctionalities(Session sess)
            throws HibernateException {
        List<SystemFunctionality> clinicList = sess.createQuery(
                "select c from SystemFunctionality as c order by c.description")
                .list();

        return clinicList;
    }

    public static List<NationalClinics> getClinicsDetails(Session sess)
            throws HibernateException {
        @SuppressWarnings("unchecked")
        List<NationalClinics> clinicList = sess.createQuery(
                "from NationalClinics")
                .list();

        return clinicList;
    }

    /**
     * Get the clinic with this name
     *
     * @param sess
     * @param name
     * @return Clinic
     * @throws HibernateException
     */
    public static Clinic getClinicbyName(Session sess, String name)
            throws HibernateException {

        Clinic clinic = (Clinic) sess.createQuery(
                "select c from Clinic as c where c.clinicName like :theName")
                .setString("theName", name).setMaxResults(1).uniqueResult();

        return clinic;
    }

    /**
     * Get the clinic with this name
     *
     * @param sess
     * @param uuid
     * @return Clinic
     * @throws HibernateException
     */
    public static Clinic getClinicbyUuid(Session sess, String uuid)
            throws HibernateException {

        Clinic clinic = (Clinic) sess.createQuery(
                "select c from Clinic as c where c.uuid like :uuid")
                .setString("uuid", uuid).setMaxResults(1).uniqueResult();

        return clinic;
    }

    /**
     * Method getRemoteClinics.
     *
     * @param sess Session
     * @return List<Clinic>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Clinic> getRemoteClinics(Session sess)
            throws HibernateException {
        List<Clinic> clinicList = sess.createQuery(
                "select c.name from" + " Clinic c where c.mainClinic = false")
                .list();
        return clinicList;
    }

    /**
     * Method getClinic.
     *
     * @param session    Session
     * @param clinicName String
     * @return Clinic
     * @throws HibernateException
     */
    public static Clinic getClinic(Session session, String clinicName)
            throws HibernateException {
        Clinic myClinic = null;
        myClinic = (Clinic) session
                .createQuery(
                        "select c from Clinic as c where c.clinicName = :clinic_Name")
                .setString("clinic_Name", clinicName)
                .uniqueResult();
        return myClinic;
    }

    public static NationalClinics getSearchDetails(Session session, String facilityName, String province)
            throws HibernateException {
        NationalClinics myClinic = null;
        myClinic = (NationalClinics) session
                .createQuery(
                        "from NationalClinics where facilityName like :facilityname and province like :province")
                .setString("facilityname", facilityName).setString("province", province)
                .uniqueResult();
        return myClinic;
    }

    /**
     * This method saves the clinic objects passed to it
     *
     * @param s         Session
     * @param theClinic
     * @throws HibernateException
     */
    public static void saveClinic(Session s, Clinic theClinic)
            throws HibernateException {

        s.save(theClinic);

    }

    /**
     * This method saves the Nationalclinic objects passed to it
     *
     * @param sess         Session
     * @param theClinic
     * @throws HibernateException
     */
    public static void saveNacionalClinic(Session sess, NationalClinics theClinic)
            throws HibernateException {

        sess.save(theClinic);

    }

    /**
     * Checks if the clinic exists
     *
     * @param session    Session
     * @param clinicName the clinic name to check
     * @return true if the clinic exists else false
     * @throws HibernateException
     */
    public static boolean clinicExists(Session session, String clinicName)
            throws HibernateException {
        boolean result = false;
        Clinic clinic = getClinic(session, clinicName);
        if (clinic == null) {
            result = false;
        } else {
            result = true;
        }
        return result;
    }

    // ------- METHODS FOR USER MANAGER --------------------------------

    /**
     * Method saveUser.
     *
     * @param session  Session
     * @param userName String
     * @param password String
     * @param clinics  Set<Clinics>
     * @return boolean
     */
    public static boolean saveUser(Session session, String userName, String password, Set<Role> roles, Set<Clinic> clinics) {
        if (!userExists(session, userName)) {
            User user = new User(userName, password, 'T', roles, clinics);
            user.setState(1);
            session.save(user);

            // log the transaction
            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(session));
            logging.setItemId(String.valueOf(user.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Added New User");
            logging.setMessage("Added New User " + user.getUsername()
                    + " with clinic access " + getClinicAccessString(user));
            session.save(logging);

            return true;
        }
        return false;
    }

    public static boolean saveSector(Session session, String sectorName, String code, String telefone, Clinic clinic, ClinicSectorType clinicSectorType) {
        if (getSectorByName(session, sectorName) != null || getSectorByCode(session, code) != null) {
            return false;
        } else {
            ClinicSector clinicSector = new ClinicSector(clinic, clinicSectorType, sectorName, telefone, code);
            session.save(clinicSector);

            // log the transaction
            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(session));
            logging.setItemId(String.valueOf(clinicSector.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Added New Clinic Sector");
            logging.setMessage("Added New Clinic Sector " + clinicSector.getSectorname());
            session.save(logging);

            return true;
        }
    }

    public static boolean updateSector(Session s, ClinicSector clinicSector, String code, String sectorname, String telefone, ClinicSectorType clinicSectorType)
            throws HibernateException {
        log.info("Updating sector " + clinicSector.getSectorname());
        ClinicSector nameSector = getSectorByName(s, sectorname);
        ClinicSector codeSector = getSectorByCode(s, code);

        if (codeSector != null ) {
            if(!codeSector.getId().equals(clinicSector.getId()))
                return false;
        }

        if (nameSector != null) {
            if (!nameSector.getId().equals(clinicSector.getId()))
                return false;
        }

            clinicSector.setSectorname(sectorname);
            clinicSector.setCode(code);
            clinicSector.setTelephone(telefone);
            clinicSector.setClinicSectorType(clinicSectorType);
            s.update(clinicSector);

            // log the transaction
            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(s));
            logging.setItemId(String.valueOf(clinicSector.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Updated Clinic Sector");
            logging.setMessage("Updated Clinic Sector " + clinicSector.getSectorname()
                    + ": changed.");
            s.save(logging);

            return true;
    }

    /**
     * Method getClinicAccessString.
     *
     * @param u User
     * @return String
     */
    public static String getClinicAccessString(User u) {
        StringBuffer clinicList = new StringBuffer();
        for (Clinic s : u.getClinics()) {
            clinicList.append(s.getClinicName());
            clinicList.append(", ");
        }
        // remove last comma and spac
        if (clinicList.length() > 2) {
            clinicList = clinicList.delete(clinicList.length() - 2, clinicList.length());
        }
        return clinicList.toString();

    }

    public static String getRoleAccessString(User u) {
        StringBuffer roleList = new StringBuffer();
        for (Role r : u.getRoleSet()) {
            roleList.append(r.getDescription());
            roleList.append(", ");
        }
        // remove last comma and spac
        if (roleList.length() > 2) {
            roleList = roleList.delete(roleList.length() - 2, roleList.length());
        }
        return roleList.toString();

    }

    /**
     * Method userExists.
     *
     * @param session Session
     * @param name    String
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public static boolean userExists(Session session, String name) {
        List<User> userList = session.createQuery(
                "from User u where upper(u.username) = :name").setString(
                "name", name.toUpperCase()).list();
        if (userList.size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean functionalityExists(Session session, SystemFunctionality functionality) {
        List<SystemFunctionality> userList = session.createQuery(
                "from SystemFunctionality u where upper(u.description) = :description").setString(
                "description", functionality.getDescription().toUpperCase()).list();
        if (userList.size() > 0) {
            return true;
        }
        return false;
    }

    public static boolean roleExists(Session session, Role role) {
        List<Role> userList = session.createQuery(
                "from Role u where upper(u.description) = :description").setString(
                "description", role.getDescription().toUpperCase()).list();
        if (userList.size() > 0) {
            return true;
        }
        return false;
    }
    /**
     * Returns a string list of the Usernames for the StockCenter
     *
     * @param sess Session
     * @return List
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<String> getUserList(Session sess)
            throws HibernateException {
        String query = "select user.username from User as user order by user.username asc";
        List<String> result = sess.createQuery(query).list();
        return result;
    }

    /**
     * Returns a list of all the users
     *
     * @param sess Session
     * @return List
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<User> getUsers(Session sess) throws HibernateException {
        String query = "from User";
        List<User> result = sess.createQuery(query).list();
        return result;
    }

    public static ClinicSector getSectorByName(Session sess, String sectorname)
            throws HibernateException {

        return (ClinicSector) sess
                .createQuery(
                        "select sector from ClinicSector as sector where sector.sectorname = :sectorname")
                .setString("sectorname", sectorname).setMaxResults(1)
                .uniqueResult();
    }

    public static ClinicSector getSectorByCode(Session sess, String code)
            throws HibernateException {

        return (ClinicSector) sess
                .createQuery(
                        "select sector from ClinicSector as sector where sector.code = :code")
                .setString("code", code).setMaxResults(1)
                .uniqueResult();
    }

    /**
     * Method getUserByName.
     *
     * @param sess     Session
     * @param username String
     * @return User
     * @throws HibernateException
     */
    public static User getUserByName(Session sess, String username)
            throws HibernateException {

        User user = (User) sess
                .createQuery(
                        "select user from User as user where user.username = :theUserName")
                .setString("theUserName", username).setMaxResults(1)
                .uniqueResult();
        return user;
    }

    public static Role getRoleByCode(Session sess, String code) throws HibernateException {

        Role role = (Role) sess
                .createQuery(
                        "select role from Role as role where role.code = :code")
                .setString("code", code).setMaxResults(1)
                .uniqueResult();
        return role;
    }

    /**
     * Method getUserById.
     *
     * @param sess  Session
     * @param theId int
     * @return User
     * @throws HibernateException
     */
    public static User getUserById(Session sess, int theId)
            throws HibernateException {

        if (sess == null) {
            sess = HibernateUtil.getNewSession();
        }

        User user = (User) sess.createQuery(
                "select user from User as user where user.id = :theId")
                .setInteger("theId", theId).setMaxResults(1).uniqueResult();
        return user;
    }

    /**
     * @param s
     * @param u
     * @param password
     * @throws HibernateException
     */
    public static void updateUserPassword(Session s, User u, String password)
            throws HibernateException {
        log.info("Updating password for user " + u.getUsername());

        u.setPassword(password);

        u.setModified('T');

        s.update(u);

        // log the transaction
        Logging logging = new Logging();
        logging.setIDart_User(LocalObjects.getUser(s));
        logging.setItemId(String.valueOf(u.getId()));
        logging.setModified('Y');
        logging.setTransactionDate(new Date());
        logging.setTransactionType("Updated User");
        logging.setMessage("Updated User " + u.getUsername()
                + ": Password change.");
        s.save(logging);

    }

    /**
     * @param s
     * @param u
     * @throws HibernateException
     */
    public static void updateUserState(Session s, User u) throws HibernateException {
        log.info("Updating state for user " + u.getUsername());

        u.setModified('T');

        // log the transaction
        Logging logging = new Logging();
        logging.setIDart_User(LocalObjects.getUser(s));
        logging.setItemId(String.valueOf(u.getId()));
        logging.setModified('Y');
        logging.setTransactionDate(new Date());
        logging.setTransactionType("Updated User");
        logging.setMessage("Updated User " + u.getUsername() + ": State changed.");
        s.save(logging);
    }

    /**
     * @param s
     * @param u
     * @param clinicsSet
     * @throws HibernateException
     */
    public static void updateUserClinics(Session s, User u,
                                         Set<Clinic> clinicsSet) throws HibernateException {

        log.info("Updating clinic access for user " + u.getUsername());
        String oldClinicAccessStr = getClinicAccessString(u);

        u.setClinics(clinicsSet);

        String newClinicAccessStr = getClinicAccessString(u);

        u.setModified('T');

        // log the transaction
        Logging logging = new Logging();
        logging.setIDart_User(LocalObjects.getUser(s));
        logging.setItemId(String.valueOf(u.getId()));
        logging.setModified('Y');
        logging.setTransactionDate(new Date());
        logging.setTransactionType("Updated User");
        logging.setMessage("Updated User " + u.getUsername()
                + ": Clinic access change from " + oldClinicAccessStr + " to "
                + newClinicAccessStr);
        s.save(logging);

    }

    public static void updateUserRoles(Session s, User u,
                                         Set<Role> roleSet) throws HibernateException {

        log.info("Updating Role access for user " + u.getUsername());
        String oldClinicAccessStr = getClinicAccessString(u);

        u.setRoleSet(roleSet);

        String newClinicAccessStr = getRoleAccessString(u);

        u.setModified('T');

        // log the transaction
        Logging logging = new Logging();
        logging.setIDart_User(LocalObjects.getUser(s));
        logging.setItemId(String.valueOf(u.getId()));
        logging.setModified('Y');
        logging.setTransactionDate(new Date());
        logging.setTransactionType("Updated User");
        logging.setMessage("Updated User " + u.getUsername()
                + ": Role access change from " + oldClinicAccessStr + " to "
                + newClinicAccessStr);
        s.save(logging);

    }


    /**
     * @param u
     * @param clinics
     * @throws HibernateException
     */
    public static void updateUserClinicAccess(User u, Set<Clinic> clinics)
            throws HibernateException {

        u.setClinics(clinics);
        u.setModified('T');

    }

    // ------- METHODS FOR StockCenter MANAGER --------------------------------

    /**
     * Returns a StockCenter by name
     *
     * @param session Session
     * @return StockCenter
     */
    public static StockCenter getStockCenter(Session session, String name) {
        StockCenter result = null;
        result = (StockCenter) session
                .createQuery(
                        "select sc from StockCenter as sc where upper(stockCenterName) = :stockCenterName")
                .setString("stockCenterName", name.toUpperCase())
                .setMaxResults(1).uniqueResult();
        return result;
    }

    /**
     * Returns all Stock Centers
     *
     * @param session Session
     * @return List<StockCenter>
     */
    @SuppressWarnings("unchecked")
    public static List<StockCenter> getStockCenters(Session session) {
        List<StockCenter> result = session.createQuery("select sc from StockCenter as sc").list();
        return result;
    }

    /**
     * Returns the preferred Stock Center
     *
     * @param session Session
     * @return List<StockCenter>
     */
    public static StockCenter getPreferredStockCenter(Session session) {
        StockCenter result = (StockCenter) session.createQuery(
                "select sc from StockCenter as sc where sc.preferred = true")
                .uniqueResult();
        return result;
    }

    /**
     * Method saveStockCenter.
     *
     * @param session        Session
     * @param theStockCenter StockCenter
     */
    public static void saveStockCenter(Session session,
                                       StockCenter theStockCenter) {

        if (theStockCenter.isPreferred()) {
            session.createQuery("Update StockCenter set preferred = false")
                    .executeUpdate();
        }
        session.saveOrUpdate(theStockCenter);
    }

    // ------- METHODS FOR SIMPLE DOMAIN MANAGER
    // --------------------------------

    /**
     * Method simpleDomainExists.
     *
     * @param session     Session
     * @param name        String
     * @param description String
     * @param value       String
     * @return boolean
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static boolean simpleDomainExists(Session session, String name,
                                             String description, String value) throws HibernateException {
        List<SimpleDomain> domainList = session
                .createQuery(
                        "from SimpleDomain sd where upper(sd.name) =:name"
                                + " and upper(sd.description) =:description and upper(sd.value) =:value")
                .setString("name", name.toUpperCase()).setString("description",
                        description.toUpperCase()).setString("value",
                        value.toUpperCase()).list();
        if (domainList.size() > 0) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static String dispenseModUUID(Session session, String value) throws HibernateException {
        List<SimpleDomain> domainList = session.createQuery("from SimpleDomain sd where sd.description = 'dispense_mode' order by sd.id asc").list();


        if (domainList.size() > 0) {
            for(SimpleDomain sd : domainList){

                if(sd.getValue().equalsIgnoreCase(value)){
                    return sd.getName();
                }
            }
        }
        return " ";
    }

    /**
     * Method addSimpleDomain.
     *
     * @param session Session
     * @param sDomain SimpleDomain
     * @throws HibernateException
     */

    public static void addSimpleDomain(Session session, SimpleDomain sDomain)
            throws HibernateException {

        if (!simpleDomainExists(session, sDomain.getName(), sDomain
                .getDescription(), sDomain.getValue())) {
            session.save(sDomain);
        }
    }

    /**
     * Method getClinicalStages.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SimpleDomain> getClinicalStages(Session sess)
            throws HibernateException {
        List<SimpleDomain> result = null;
        String qString = "select s from SimpleDomain as s where s.name='clinical_stage' order by s.value asc";
        Query q = sess.createQuery(qString);
        result = q.list();
        return result;
    }

    /**
     * Method getPrescriptionDurations.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SimpleDomain> getPrescriptionDurations(Session sess)
            throws HibernateException {
        List<SimpleDomain> result = sess
                .createQuery(
                        "select s from SimpleDomain as s where s.name='prescriptionDuration' order by s.id")
                .list();

        return result;
    }

    /**
     * Method getClinicSectorType.
     *
     * @param sess Session
     * @return List<ClinicSectorType>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<ClinicSectorType> getClinicSecrtorType(Session sess)
            throws HibernateException {
        List<ClinicSectorType> result = sess
                .createQuery(
                        "select s from ClinicSectorType as s order by s.id")
                .list();

        return result;
    }

    /**
     * Method getClinicSectorBySectorType.
     *
     * @param sess Session
     * @return List<ClinicSectorType>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<ClinicSector> getClinicSectorBySectorType(Session sess, String sectorType)
            throws HibernateException {
        List<ClinicSector> result = sess
                .createQuery(
                        "select s from ClinicSector as s where s.clinicSectorType.description = :sectorType  order by s.id")
                .setString("sectorType", sectorType)
                .list();

        return result;
    }

    /**
     * Method getReasonForUpdate.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SimpleDomain> getReasonForUpdate(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.name like 'reason_for_update' order by s.value";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    public static List<SimpleDomain> getprofilaxiaINH(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.name like 'inh_prophylaxis' order by s.id";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    /**
     * Method getProvinces.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<String> getProvinces(Session sess)
            throws HibernateException {
        String qString = "select distinct(name) from Province"
                + " order by name";
        Query q = sess.createQuery(qString);
        List<String> result = q.list();
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getDistrict(Session sess, String prov)
            throws HibernateException {
        String qString = "select distinct(d.name) from District as d, Province as p " +
                " where d.province = p.id and p.name=:province order by d.name";
        Query q = sess.createQuery(qString).setString("province", prov);
        List<String> result = q.list();
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getSubDistrict(Session sess, String dist)
            throws HibernateException {
        String qString = "select distinct(subDistrict) from NationalClinics where district = :district"
                + " order by subDistrict";
        Query q = sess.createQuery(qString).setString("district", dist);
        List<String> result = q.list();
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFacilityName(Session sess, String subdis)
            throws HibernateException {
        String qString = "select distinct(facilityName) from NationalClinics where subDistrict = :subdistrict"
                + " order by facilityName";
        Query q = sess.createQuery(qString).setString("subdistrict", subdis);
        List<String> result = q.list();
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getFacilityType(Session sess, String facname)
            throws HibernateException {
        String qString = "select distinct(facilityType) from NationalClinics where facilityName = :facilityname";
        Query q = sess.createQuery(qString).setString("facilityname", facname);
        List<String> result = q.list();
        return result;
    }


    public static List<String> getAllFacilityType(Session sess)
            throws HibernateException {
        String qString = "select distinct(value) from SimpleDomain where description  = 'pharmacy_type' order by 1 desc";
        Query q = sess.createQuery(qString);
        List<String> result = q.list();
        return result;
    }
    /**
     * Method to get a NationalClinic based on given fields
     *
     * @param sess
     * @param facility
     * @param
     * @return
     * @throws HibernateException
     */
    public static NationalClinics getNationalClinic(Session sess, String facilityType, String facility)
            throws HibernateException {
        String qString = "from NationalClinics "
                + " where facilityname = :facility " +
                "   and facilityType = :facilityType ";

        Query q = sess.createQuery(qString).setString("facilityType", facilityType)
                .setString("facility", facility);
        NationalClinics result = (NationalClinics) q.uniqueResult();
        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getDeseases(Session sess)
            throws HibernateException {
        String qString = "select distinct(value) from SimpleDomain as s where s.description= :disease order by s.value";
        Query q = sess.createQuery(qString).setString("disease", "Disease");
        List<String> result = q.list();
        return result;
    }

    public static List<SimpleDomain> getRegimens(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.name= :regimen order by s.value";
        Query q = sess.createQuery(qString).setString("regimen", "regimen");
        List<SimpleDomain> result = q.list();
        return result;
    }

    /**
     * Method getReportParameters.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SimpleDomain> getReportParameters(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description='report_parameter'";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        if (result == null) {
            log.warn("No report parameter entries found in SimpleDomain");
        }
        return result;
    }

    /**
     * Method getActivationReasons.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SimpleDomain> getActivationReasons(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.name='activation_reason' order by s.value asc";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    /**
     * Method getDeactivationReasons.
     *
     * @param sess Session
     * @return List<SimpleDomain>
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<SimpleDomain> getDeactivationReasons(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.name='deactivation_reason' order by s.value";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        return result;
    }

    /**
     * @param sess Session
     * @return all the user-defined drug groups eg 1A-30, 1A-40 etc
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Object[]> getDrugGroupNamesAndRegs(Session sess)
            throws HibernateException {

        String qString = "select regimeesquema,codigoregime from RegimeTerapeutico r";
        Query q = sess.createQuery(qString);
        List<Object[]> result = q.list();

        return result;
    }

    @SuppressWarnings("unchecked")
    public static List<Drug> getDrugs(Session sess)
            throws HibernateException {
        List<Drug> drugList = sess.createQuery(
                "select d from Drug as d order by d.tipoDoenca desc")
                .list();

        return drugList;
    }

    public static List<RegimeTerapeutico> getRegimeTerapeutico(Session sess)
            throws HibernateException {

        String qString = "select r from RegimeTerapeutico r order by r.regimeesquema";
        Query q = sess.createQuery(qString);
        List<RegimeTerapeutico> result = q.list();

        return result;
    }


    public static List<RegimeTerapeutico> getRegimeTerapeuticoActivo(Session sess)
            throws HibernateException {

        String qString = "select r from RegimeTerapeutico r where r.active = true order by r.regimeesquema";
        Query q = sess.createQuery(qString);
        List<RegimeTerapeutico> result = q.list();

        return result;
    }

    /**
     * @param sess Session
     * @return all the user-defined drug groups eg 1A-30, 1A-40 etc
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Regimen> getDrugGroups(Session sess)
            throws HibernateException {

        String qString = "from Regimen r order by r.regimenName";
        Query q = sess.createQuery(qString);
        List<Regimen> result = q.list();

        return result;
    }

    /**
     * @param sess Session
     * @return todas linha terapeuticas
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<LinhaT> getLinhasT(Session sess)
            throws HibernateException {

        String qString = "from LinhaT lt order by lt.linhanome";
        Query q = sess.createQuery(qString);
        List<LinhaT> result = q.list();

        return result;
    }

    /**
     * This method saves the simpleDomain objects passed to it
     *
     * @param sess         Session
     * @param simpleDomain
     * @throws HibernateException
     */
    public static void saveSimpleDomain(Session sess, SimpleDomain simpleDomain)
            throws HibernateException {

        sess.save(simpleDomain);
    }

    // ------- METHODS FOR LOGGIN MANAGER --------------------------------
    // ------- METHODS FOR FORM MANAGER --------------------------------

    /**
     * Used to populate combo boxes with drug forms
     *
     * @param sess Session
     * @return all the form names eg tablets, solution etc
     * @throws HibernateException
     */
    @SuppressWarnings("unchecked")
    public static List<Form> getForms(Session sess) throws HibernateException {
        String qString = "from Form as f order by f.form";
        Query q = sess.createQuery(qString);
        List<Form> result = q.list();

        return result;
    }

    public static List<AtcCode> getAtccodes(Session sess) {
        String qString = "from AtcCode as f order by f.name";
        Query q = sess.createQuery(qString);
        @SuppressWarnings("unchecked")
        List<AtcCode> result = q.list();

        return result;
    }

    /**
     * This method gets a form from the database.
     *
     * @param session  the current hibernate session
     * @param formName the name of the form to get
     * @return the form
     */
    public static Form getForm(Session session, String formName) {
        return (Form) (session.createQuery(
                "from Form as f where upper(f.form) = :form").setString("form",
                formName.toUpperCase()).uniqueResult());
    }

    public static AtcCode getAtccodeFromName(Session session, String name) {
        return (AtcCode) (session.createQuery(
                "from AtcCode as a where upper(a.name) = :name").setString("name",
                name.toUpperCase()).uniqueResult());
    }

    public static AtcCode getAtccodeFromCode(Session session, String code) {
        return (AtcCode) (session.createQuery(
                "from AtcCode as a where upper(a.code) = :code").setString("code",
                code.toUpperCase()).uniqueResult());
    }

    /**
     * Method to save only unique form
     *
     * @param session the current hibernate session
     * @param form    the form to save
     * @throws HibernateException
     */
    public static void saveForm(Session session, Form form)
            throws HibernateException {
        if (!formExists(session, form.getForm())) {
            session.save(form);
        }
    }

    /**
     * Method to check if form already exists
     *
     * @param session  the current hibernate session
     * @param formName the name of the form
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public static boolean formExists(Session session, String formName) {
        List<Form> result = session.createQuery(
                "from Form as f where upper(f.form) = :form").setString("form",
                formName.toUpperCase()).list();
        if (result.size() > 0) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public static PharmacyDetails getPharmacyDetails(Session sess)
            throws HibernateException {
        String qString = "select s from SimpleDomain as s where s.description='pharmacy_detail'";
        Query q = sess.createQuery(qString);
        List<SimpleDomain> result = q.list();

        if (result == null) {
            log.warn("No report parameter entries found in SimpleDomain");
            // return result;
        }

        PharmacyDetails phd = new PharmacyDetails();
        // pharmacist
        // assistant_pharmacist
        // pharmacy_name
        // pharmacy_street
        // pharmacy_city
        // pharmacy_contact_no
        //
        if (result != null) {
            for (SimpleDomain s : result) {
                if (s.getName().equalsIgnoreCase("pharmacist")) {
                    phd.setPharmacist(s.getValue());
                } else if (s.getName().equalsIgnoreCase("assistant_pharmacist")) {
                    phd.setAssistantPharmacist(s.getValue());
                } else if (s.getName().equalsIgnoreCase("pharmacy_name")) {
                    phd.setPharmacyName(s.getValue());
                } else if (s.getName().equalsIgnoreCase("pharmacy_street")) {
                    phd.setStreet(s.getValue());
                } else if (s.getName().equalsIgnoreCase("pharmacy_city")) {
                    phd.setCity(s.getValue());
                } else if (s.getName().equalsIgnoreCase("pharmacy_contact_no")) {
                    phd.setContactNo(s.getValue());
                }
            }
        }
        log.debug(phd.toString());
        return phd;

    }

    public static void savePharmacyDetails(Session session,
                                           PharmacyDetails pharmDet) {

        String qString = "UPDATE SimpleDomain SET value = '$value' WHERE name = '$name' and "
                + "description = 'pharmacy_detail'";

        session.createQuery(
                qString.replace("$name", "pharmacist").replace("$value",
                        pharmDet.getPharmacist())).executeUpdate();
        session.createQuery(
                qString.replace("$name", "assistant_pharmacist").replace(
                        "$value", pharmDet.getAssistantPharmacist()))
                .executeUpdate();
        session.createQuery(
                qString.replace("$name", "pharmacy_name").replace("$value",
                        pharmDet.getPharmacyName())).executeUpdate();
        session.createQuery(
                qString.replace("$name", "pharmacy_street").replace("$value",
                        pharmDet.getStreet())).executeUpdate();
        session.createQuery(
                qString.replace("$name", "pharmacy_city").replace("$value",
                        pharmDet.getCity())).executeUpdate();
        session.createQuery(
                qString.replace("$name", "pharmacy_contact_no").replace(
                        "$value", pharmDet.getContactNo())).executeUpdate();

    }

    // --------------------- Methods for Study Manager ------------------
    public static Study getCidaStudy(Session session) {

        Study result = (Study) session.createQuery("select study from Study study "
                + "where id = 1").uniqueResult();

        return result;

    }

    public static List<StudyParticipant> getP() {
        return null;
    }

    public static IdentifierType getNationalIdentifierType(Session hSession) {
        // FIXME: figure out a way to identify the national id type
        return (IdentifierType) hSession.createQuery(
                "from IdentifierType where id = 0").uniqueResult();
    }

    public static IdentifierType getCRAMIdentifierType(Session hSession) {
        // FIXME: figure out a way to identify the national id type
        return (IdentifierType) hSession.createQuery(
                "from IdentifierType where name = 'CRAM'").uniqueResult();
    }

    public static IdentifierType getPrEPIdentifierType(Session hSession) {
        // FIXME: figure out a way to identify the national id type
        return (IdentifierType) hSession.createQuery(
                "from IdentifierType where name = 'PREP'").uniqueResult();
    }

    //Insere regime terapeutico
    public static void saveRegimeTerapeutico(Session s, RegimeTerapeutico regimeTerapeutico)
            throws HibernateException {
        // if this is the 1st time we're accessing the doctor List
        s.save(regimeTerapeutico);
    }

    //devolve todos motivos de mudanca de arv
    @SuppressWarnings("unchecked")
    public static List<Motivomudanca> getAllMotivosS(Session sess)
            throws HibernateException {

        List<Motivomudanca> result = sess.createQuery(
                "select r from Motivomudanca as r").list();

        return result;
    }

    //Insere motivo de mudanca
    public static void saveMotivoMudanca(Session s, Motivomudanca motivomudanca)
            throws HibernateException {
        // if this is the 1st time we're accessing the doctor List
        s.save(motivomudanca);
    }

    @SuppressWarnings("unchecked")
    public static List<Motivomudanca> getAllMotivos(Session sess) throws HibernateException {

        List<Motivomudanca> result = sess.createQuery(
                "select r from Motivomudanca as r").list();

        return result;
    }

    public static void saveSyncTempPatient(Session s, SyncTempPatient syncTempPatient)
            throws HibernateException {

        s.saveOrUpdate(syncTempPatient);
    }

    public static void saveSyncTempDispense(Session s, SyncTempDispense syncTempDispense)
            throws HibernateException {

        s.saveOrUpdate(syncTempDispense);
    }

    // Devolve a lista de todos pacientes referidos
    public static List<SyncTempPatient> getAllSyncTempPatient(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "select sync from sync_temp_patients as sync").list();

        return result;
    }

    // Devolve a lista de todos pacientes referidos prontos para ser enviado (Estado do paciente P- Pronto, E- Exportado)
    public static List<SyncTempPatient> getAllSyncTempPatientReadyToSend(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "from SyncTempPatient sync where sync.exclusaopaciente = false and (sync.syncstatus = 'P' or sync.syncstatus = 'U' or sync.syncstatus is null)").list();

        return result;
    }

    // Devolve a lista de todos pacientes referidos prontos para ser enviado (Estado do paciente P- Pronto, E- Exportado)
    public static List<SyncTempPatient> getAllSyncTempPatientReadyToSave(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "from SyncTempPatient sync where sync.syncstatus = 'I'").list();

        return result;
    }

    // Devolve a lista de todos pacientes referidos prontos para ser enviado (Estado do paciente P- Pronto, E- Exportado, I-Importado)
    public static List<SyncTempDispense> getAllSyncTempDispenseReadyToSave(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "from SyncTempDispense sync where sync.syncstatus = 'I' order by sync.date asc").list();

        return result;
    }

    // Devolve a lista de todas dispensas de pacientes referidos prontos para ser enviado (Estado do paciente P- Pronto, E- Exportado, I-Importado)
    public static List<SyncTempDispense> getAllSyncTempDispenseReadyToSend(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "from SyncTempDispense sync where sync.syncstatus = 'P' or sync.syncstatus is null order by sync.date asc").list();

        return result;
    }

    // Devolve a lista de todos dispensas locais prontos para ser enviado (Estado do paciente P- Pronto, E- Exportado, I-Importado L- Last Local Dispense)
    public static List<SyncTempDispense> getAllLocalSyncTempDispenseReadyToSend(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "from SyncTempDispense sync where sync.syncstatus = 'L' order by sync.date asc").list();

        return result;
    }

    // Devolve a lista de todos pacientes referidos prontos para ser enviado (Estado do paciente P- Pronto, E- Exportado, I-Importado)
    public static SyncTempDispense getSyncTempDispense(Session sess, SyncTempDispense syncTempDispense) throws HibernateException {
        SyncTempDispense result;
        result = (SyncTempDispense) sess.createQuery(
                "select sync from SyncTempDispense sync where sync.patientid ='"+syncTempDispense.getPatientid()+"' " +
                        " and sync.prescriptionid ='"+syncTempDispense.getPrescriptionid()+"' " +
                        " and pg_catalog.date(sync.pickupdate = '"+ RestUtils.castDateToString(syncTempDispense.getPickupdate()) +"'").setMaxResults(1).uniqueResult();

        return result;
    }

    // Devolve a lista de todos pacientes referidos pelo nid e clinicID
    public static SyncTempPatient getSyncTempPatienByNIDandClinicNameUuid(Session sess, String nid, String clinicUuid) throws HibernateException {
        SyncTempPatient result;

        List patientIdentifiers = sess.createQuery("from SyncTempPatient sync where sync.mainclinicuuid = '" + clinicUuid + "' and sync.patientid = '" + nid+"'").list();

        if (patientIdentifiers.isEmpty())
            result = null;
        else
            result = (SyncTempPatient) patientIdentifiers.get(0);

        return result;
    }

    // Devolve a lista de todos pacientes referidos pelo nid e clinicname
    public static SyncTempPatient getSyncTempPatienByNIDandClinicName(Session sess, String nid, String clinicname) throws HibernateException {
        SyncTempPatient result;

        List patientIdentifiers = sess.createQuery("from SyncTempPatient sync where sync.mainclinicname = '" + clinicname + "' and sync.patientid = '" + nid+"'").list();

        if (patientIdentifiers.isEmpty())
            result = null;
        else
            result = (SyncTempPatient) patientIdentifiers.get(0);

        return result;
    }

    // Devolve a lista de todos pacientes referidos pelo nid
    public static SyncTempPatient getSyncTempPatienByNID(Session sess, String nid) throws HibernateException {
        SyncTempPatient result;

        List patientIdentifiers = sess.createQuery("from SyncTempPatient sync where sync.patientid = '" + nid+"'").list();

        if (patientIdentifiers.isEmpty())
            result = null;
        else
            result = (SyncTempPatient) patientIdentifiers.get(0);

        return result;
    }

    // Devolve a lista de todos pacientes referidos por uuid
    public static SyncTempPatient getSyncTempPatienByUuid(Session sess, String uuid) throws HibernateException {

        SyncTempPatient result = null;
        Date maxDate = RestUtils.castStringToDate("1900-01-01");
        List patientIdentifiers = sess.createQuery("from SyncTempPatient sync where sync.uuidopenmrs = '" + uuid + "'").list();

        if (!patientIdentifiers.isEmpty())
            for(SyncTempPatient p : (List<SyncTempPatient>) patientIdentifiers){
                try{
                    if(p.getPrescriptiondate() != null)
                        if(p.getPrescriptiondate().after(maxDate)){
                            result = p;
                            maxDate = p.getPrescriptiondate();
                        }
                }catch (Exception e){
                    log.trace("Prescrição sem data de registo do Paciente: "+ p.getPatientid());
                }finally {
                    continue;
                }
            }

        if(result == null)
            result = (SyncTempPatient) patientIdentifiers.get(0);

        return result;

    }

    // Devolve a lista de todos pacientes referidos por uuid
    public static SyncTempPatient getSyncTempPatienByUuidAndClinicUuid(Session sess, String uuid, String clinicUuid) throws HibernateException {

        SyncTempPatient result;

        List patientIdentifiers = sess.createQuery("from SyncTempPatient sync where sync.mainclinicuuid = '" + clinicUuid + "' and sync.uuidopenmrs = '" + uuid + "' order by id desc").list();

        if (patientIdentifiers.isEmpty())
            result = null;
        else
            result = (SyncTempPatient) patientIdentifiers.get(0);

        return result;

    }

    // Devolve a lista de dispensa de pacientes referidos por id
    public static SyncTempDispense getSyncTempDispenseById(Session sess, int id) throws HibernateException {

        SyncTempDispense result;

        List dispenses = sess.createQuery("from SyncTempDispense sync where sync.id = " + id).list();

        if (dispenses.isEmpty())
            result = null;
        else
            result = (SyncTempDispense) dispenses.get(0);

        return result;

    }

    // Devolve a lista de dispensa de pacientes referidos por id
    public static List<SyncTempDispense> getAllSyncTempDispenseByuuid(Session sess, String uuid) throws HibernateException {

        List  result;
        result = sess.createQuery("from SyncTempDispense sync where sync.uuidopenmrs = '" + uuid + "'").list();
        return result;

    }

    // Devolve a lista de todos pacientes enviados das clinicsSectors prontos para ser gravados (Estado do paciente R- Pronto, S- Importado, U-Actualizado)
    public static List<SyncMobilePatient> getAllSyncMobilePatientReadyToSave(Session sess) throws HibernateException {
        List result;
        result = sess.createQuery(
                "from SyncMobilePatient sync where sync.syncstatus = 'R'").list();

        return result;
    }

    public static Role getRoleByDescription(Session sess, String description) throws HibernateException {
        Role role = (Role) sess
                    .createQuery("select role from Role as role where role.description = :description")
                    .setString("description", description).setMaxResults(1)
                    .uniqueResult();
        return role;
    }

    public static SystemFunctionality getFunctionalityByDescription(Session sess, String description) throws HibernateException {
        SystemFunctionality functionality = (SystemFunctionality) sess
                .createQuery("select sf from SystemFunctionality as sf where sf.description = :description")
                .setString("description", description).setMaxResults(1)
                .uniqueResult();
        return functionality;
    }

    public static List<Role> getRoles(Session sess) {
        String query = "from Role";
        List<Role> result = sess.createQuery(query).list();
        return result;
    }

    public static List<SystemFunctionality> getSysFunctionalities(Session sess) {
        String query = "from SystemFunctionality";
        List<SystemFunctionality> result = sess.createQuery(query).list();
        return result;
    }

    public static List<ClinicSector> getParagemUnica(Session sess) {
        String qString = "from ClinicSector";
        Query q = sess.createQuery(qString);
        List<ClinicSector> result = q.list();

        return result;
    }

    public static void saveSystemFuntionality(Session session, SystemFunctionality functionality) {
        if (functionality.getId()  == null ||  functionality.getId() <= 0) {

            session.save(functionality);

            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(session));
            logging.setItemId(String.valueOf(functionality.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Added New Functionality");
            logging.setMessage("Added New Functionality: " + functionality.getId()+" -> " + functionality.getDescription());
            session.save(logging);

        }else {
            session.update(functionality);
            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(session));
            logging.setItemId(String.valueOf(functionality.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Updated Functionality");
            logging.setMessage("Updated Functionality: " + functionality.getId()+" -> "+ functionality.getDescription());
            session.save(logging);

        }
    }

    public static void saveRole(Session session, Role role) {
        if (role.getId()  == null || role.getId() <= 0) {

            session.save(role);

            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(session));
            logging.setItemId(String.valueOf(role.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Added New role");
            logging.setMessage("Added New role " + role.getDescription());
            session.save(logging);

        }else {
            session.update(role);

            Logging logging = new Logging();
            logging.setIDart_User(LocalObjects.getUser(session));
            logging.setItemId(String.valueOf(role.getId()));
            logging.setModified('Y');
            logging.setTransactionDate(new Date());
            logging.setTransactionType("Updated a role");
            logging.setMessage("Updated role: " +role.getId()+" -> "+ role.getDescription());
            session.save(logging);
        }

    }

}
