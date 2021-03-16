
package org.celllife.idart.database.dao;

import model.manager.reports.*;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.hibernate.Drug;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.PrescriptionToPatient;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.gui.alert.RiscoRoptura;
import org.celllife.idart.gui.sync.dispense.SyncLinha;
import org.celllife.idart.gui.sync.patients.SyncLinhaPatients;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

/**
 * Esta classe efectua conexao com a BD postgres e tem metodo para a manipulacao
 * dos dados
 *
 * @author EdiasJambaia
 */
public class ConexaoJDBC {

    private static Logger log = Logger.getLogger(ConexaoJDBC.class);
    Connection conn_db; // Conex�o com o servidor de banco de dados
    Statement st; // Declara��o para executar os comandos

    /**
     * Conexao a base de dado
     *
     * @param usr
     * @param pwd
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void conecta(String usr, String pwd) throws SQLException,
            ClassNotFoundException {

        DOMConfigurator.configure("log4j.xml");

        // String url = "jdbc:postgresql://192.168.0.105/pharm?charSet=LATIN1";
        String url = iDartProperties.hibernateConnectionUrl;

        //log.trace(" url "+iDartProperties.hibernateConnectionUrl);
        log.info("Conectando ao banco de dados  URL = " + url);

        // Carregar o driver
        Class.forName("org.postgresql.Driver");

        // Conectar com o servidor de banco de dados
        conn_db = DriverManager.getConnection(url, usr, pwd);

        log.info("Conectado...Criando a declara��o");

        st = conn_db.createStatement();

    }

    /**
     * Mapa para pacientes e desagregacao no MMIA
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Map MMIA(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();
        String query = " SELECT  distinct p.patient, "
                + " 		p.reasonforupdate,  "
                + " 		p.dispensatrimestral, "
                + " 		p.dispensasemestral,  "
                + " 		p.prep, "
                + " 		p.ptv, "
                + " 		p.dc, "
                + " 		p.ppe, "
                + " 		p.ce, "
                + " 		l.linhanome, "
                + " 		EXTRACT(year FROM age('" + endDate + "',pack.dateofbirth)) :: int dateofbirth,  "
                + " 		ep.startreason,  "
                + " 		CASE "
                + " 			WHEN p.dispensatrimestral = 1 AND pack.pickupdate >= '" + startDate + "' THEN p.tipodt "
                + " 			ELSE 'Transporte'  "
                + " 		END  tipodt, "
                + " 		CASE "
                + " 			WHEN p.dispensasemestral = 1 AND pack.pickupdate >= '" + startDate + "' THEN p.tipods "
                + " 			ELSE 'Transporte'  "
                + " 		END  tipods, "
                + " 		COALESCE(pack.weekssupply,0) weekssupply  "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pa.weekssupply) weekssupply, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pre.tipodoenca like '%ARV' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode "
                + " WHERE P.tipodoenca like '%ARV'";

        int totalpacientestransito = 0;
        int totalpacientesinicio = 0;
        int totalpacientesmanter = 0;
        int totalpacientesalterar = 0;
        int totalpacientestransferidoDe = 0;
        int totalpacientesmanterTransporte = 0;

        int mesesdispensadosparaDM = 0;
        int mesesdispensadosparaDT = 0;
        int mesesdispensadosparaDS = 0;
        int mesedispennsados = 0;

        int totalpacientesppe = 0;
        int totalpacientesprep = 0;
        int totalpacientesCE = 0;
        int totalpacienteptv = 0;
        int totalpacientedc = 0;

        int totallinhas1 = 0;
        int totallinhas2 = 0;
        int totallinhas3 = 0;

        int pacientesEmTarv = 0;
        int adultosEmTarv = 0;
        int pediatrico04EmTARV = 0;
        int pediatrico59EmTARV = 0;
        int pediatrico1014EmTARV = 0;

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                boolean nonuspatient = rs.getString("startreason").contains("nsito") || rs.getString("startreason").contains("ternidade");

                // Paciente Transito ou Inicio na Maternidade
                if (nonuspatient) {
                    totalpacientestransito++;
                } else {
                    pacientesEmTarv++;
                    //Total de semanas de dispensa
                    mesedispennsados = mesedispennsados + rs.getInt("weekssupply");
                }

                // Tipo de Pacinte
                if (!nonuspatient && rs.getString("reasonforupdate").contains("Inicia")) {
                    totalpacientesinicio++;
                } else if (!nonuspatient && (rs.getString("reasonforupdate").contains("Manter") || rs.getString("reasonforupdate").contains("Reiniciar"))) {
                    totalpacientesmanter++;
                } else if (!nonuspatient && rs.getString("reasonforupdate").contains("Alterar")) {
                    totalpacientesalterar++;
                } else if (!nonuspatient && rs.getString("reasonforupdate").contains("ransfer")) {
                    totalpacientestransferidoDe++;
                }

                // Manuntencao Transporte DT
                if (!nonuspatient && rs.getString("tipodt") != null) {
                    if (rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Transporte")) {
                        totalpacientesmanterTransporte++;
                        mesedispennsados = mesedispennsados - rs.getInt("weekssupply");
                    }

                }

                // Manuntencao Transporte DS
                if (!nonuspatient && rs.getString("tipods") != null) {
                    if (rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Transporte")) {
                        totalpacientesmanterTransporte++;
                        mesedispennsados = mesedispennsados - rs.getInt("weekssupply");
                    }

                }

                // Dispensa Trimenstral ou Semestral
                if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 0) {
                    mesesdispensadosparaDM++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 1 && rs.getInt("dispensasemestral") == 0) {
                    mesesdispensadosparaDT++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 1) {
                    mesesdispensadosparaDS++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 1) {
                    mesesdispensadosparaDS++;
                }

                // Sector de Levantamento
                if (!nonuspatient && !rs.getString("prep").equalsIgnoreCase("F")) {
                    totalpacientesprep++;
                }
                if (!nonuspatient && !rs.getString("ptv").equalsIgnoreCase("F")) {
                    totalpacienteptv++;
                }
                if (!nonuspatient && !rs.getString("dc").equalsIgnoreCase("F")) {
                    totalpacientedc++;
                }
                if (!nonuspatient && !rs.getString("ppe").equalsIgnoreCase("F")) {
                    totalpacientesppe++;
                }
                if (!nonuspatient && !rs.getString("ce").equalsIgnoreCase("F")) {
                    totalpacientesCE++;
                }
                // linha Terapeutica
                if (!nonuspatient && rs.getString("linhanome").contains("1")) {
                    totallinhas1++;
                } else if (!nonuspatient && rs.getString("linhanome").contains("2")) {
                    totallinhas2++;
                } else if (!nonuspatient && rs.getString("linhanome").contains("3")) {
                    totallinhas3++;
                }

                // idade
                if (!nonuspatient && rs.getInt("dateofbirth") >= 15) {
                    adultosEmTarv++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 0 && rs.getInt("dateofbirth") <= 4) {
                    pediatrico04EmTARV++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 5 && rs.getInt("dateofbirth") <= 9) {
                    pediatrico59EmTARV++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 10 && rs.getInt("dateofbirth") <= 14) {
                    pediatrico1014EmTARV++;
                }
            }
            rs.close();
        }

        map.put("totalpacientestransito", totalpacientestransito);
        map.put("totalpacientesinicio", totalpacientesinicio);
        map.put("totalpacientesmanter", totalpacientesmanter);
        map.put("totalpacientesalterar", totalpacientesalterar);
        map.put("totalpacientestransferidoDe", totalpacientestransferidoDe);
        map.put("mesesdispensadosparaDM", mesesdispensadosparaDM);
        map.put("mesesdispensadosparaDT", mesesdispensadosparaDT);
        map.put("mesesdispensadosparaDS", mesesdispensadosparaDS);
        map.put("mesesdispensados", mesedispennsados / 4);
        map.put("totalpacientesmanterTransporte", totalpacientesmanterTransporte);
        map.put("totalpacientesppe", totalpacientesppe);
        map.put("totallinhas1", totallinhas1);
        map.put("totallinhas2", totallinhas2);
        map.put("totallinhas3", totallinhas3);
        map.put("totallinhas", totallinhas1 + totallinhas2 + totallinhas3);
        map.put("totalpacientesprep", totalpacientesprep);
        map.put("totalpacientesCE", totalpacientesCE);
        map.put("totalpacienteptv", totalpacienteptv);
        map.put("totalpacientedc", totalpacientedc);
        map.put("pacientesEmTarv", pacientesEmTarv);
        map.put("adultosEmTarv", adultosEmTarv);
        map.put("pediatrico04EmTARV", pediatrico04EmTARV);
        map.put("pediatrico59EmTARV", pediatrico59EmTARV);
        map.put("pediatrico1014EmTARV", pediatrico1014EmTARV);
        return map;

    }


    public Map MMIAACTUALIZADO(String startDate, String endDate, String diseaseType) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();
        String query = " SELECT  distinct p.patient, "
                + " 		p.reasonforupdate,  "
                + " 		p.dispensatrimestral, "
                + " 		p.dispensasemestral,  "
                + " 		p.prep, "
                + " 		p.ptv, "
                + " 		p.dc, "
                + " 		p.ppe, "
                + " 		p.ce, "
                + " 		l.linhanome, "
                + " 		EXTRACT(year FROM age('" + endDate + "',pack.dateofbirth)) :: int dateofbirth,  "
                + " 		ep.startreason,  "
                + " 		COALESCE(pack.weekssupply,0) weekssupply  "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pa.weekssupply) weekssupply, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pds.amount <> 0 and pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' and pre.tipodoenca like '%" + diseaseType + "%' "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode ";

        int totalpacientestransito = 0;
        int totalpacientesinicio = 0;
        int totalpacientesmanter = 0;
        int totalpacientesalterar = 0;
        int totalpacientestransferidoDe = 0;

        int pacientesdispensadosparaDM = 0;
        int pacientesdispensadosparaDT = 0;
        int pacientesdispensadosparaDS = 0;
        int mesesdispennsados = 0;

        int totalpacientesppe = 0;
        int totalpacientesprep = 0;
        int totalpacientesCE = 0;
        int totalpacienteptv = 0;
        int totalpacientedc = 0;

        int totallinhas1 = 0;
        int totallinhas2 = 0;
        int totallinhas3 = 0;

        int pacientesEmTarv = 0;
        int adultosEmTarv = 0;
        int pediatrico04EmTARV = 0;
        int pediatrico59EmTARV = 0;
        int pediatrico1014EmTARV = 0;

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                boolean nonuspatient = rs.getString("startreason").contains("nsito") || rs.getString("startreason").contains("ternidade");

                // Paciente Transito ou Inicio na Maternidade
                if (nonuspatient) {
                    totalpacientestransito++;
                } else {
                    pacientesEmTarv++;
                    //Total de semanas de dispensa
                    mesesdispennsados = mesesdispennsados + rs.getInt("weekssupply");
                }

                // Tipo de Pacinte
                if (!nonuspatient && rs.getString("reasonforupdate").contains("Inicia")) {
                    totalpacientesinicio++;
                } else if (!nonuspatient && (rs.getString("reasonforupdate").contains("Manter") || rs.getString("reasonforupdate").contains("Reiniciar"))) {
                    totalpacientesmanter++;
                } else if (!nonuspatient && rs.getString("reasonforupdate").contains("Alterar")) {
                    totalpacientesalterar++;
                } else if (!nonuspatient && rs.getString("reasonforupdate").contains("ransfer")) {
                    totalpacientestransferidoDe++;
                }


                // Dispensa Trimenstral ou Semestral
                if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 0) {
                    pacientesdispensadosparaDM++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 1 && rs.getInt("dispensasemestral") == 0) {
                    pacientesdispensadosparaDT++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 1) {
                    pacientesdispensadosparaDS++;
                }

                // Sector de Levantamento
                if (!nonuspatient && !rs.getString("prep").equalsIgnoreCase("F")) {
                    totalpacientesprep++;
                }
                if (!nonuspatient && !rs.getString("ptv").equalsIgnoreCase("F")) {
                    totalpacienteptv++;
                }
                if (!nonuspatient && !rs.getString("dc").equalsIgnoreCase("F")) {
                    totalpacientedc++;
                }
                if (!nonuspatient && !rs.getString("ppe").equalsIgnoreCase("F")) {
                    totalpacientesppe++;
                }
                if (!nonuspatient && !rs.getString("ce").equalsIgnoreCase("F")) {
                    totalpacientesCE++;
                }
                // linha Terapeutica
                if (!nonuspatient && rs.getString("linhanome").contains("1")) {
                    totallinhas1++;
                } else if (!nonuspatient && rs.getString("linhanome").contains("2")) {
                    totallinhas2++;
                } else if (!nonuspatient && rs.getString("linhanome").contains("3")) {
                    totallinhas3++;
                }

                // idade
                if (!nonuspatient && rs.getInt("dateofbirth") >= 15) {
                    adultosEmTarv++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 0 && rs.getInt("dateofbirth") <= 4) {
                    pediatrico04EmTARV++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 5 && rs.getInt("dateofbirth") <= 9) {
                    pediatrico59EmTARV++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 10 && rs.getInt("dateofbirth") <= 14) {
                    pediatrico1014EmTARV++;
                }
            }
            rs.close();
        }

        map.put("totalpacientestransito", totalpacientestransito);
        map.put("totalpacientesinicio", totalpacientesinicio);
        map.put("totalpacientesmanter", totalpacientesmanter);
        map.put("totalpacientesalterar", totalpacientesalterar);
        map.put("totalpacientestransferidoDe", totalpacientestransferidoDe);
        map.put("pacientesdispensadosparaDM", pacientesdispensadosparaDM);
        map.put("pacientesdispensadosparaDT", pacientesdispensadosparaDT);
        map.put("pacientesdispensadosparaDS", pacientesdispensadosparaDS);
        map.put("mesesdispensados", mesesdispennsados / 4);
        map.put("totalpacientesppe", totalpacientesppe);
        map.put("totallinhas1", totallinhas1);
        map.put("totallinhas2", totallinhas2);
        map.put("totallinhas3", totallinhas3);
        map.put("totallinhas", totallinhas1 + totallinhas2 + totallinhas3);
        map.put("totalpacientesprep", totalpacientesprep);
        map.put("totalpacientesCE", totalpacientesCE);
        map.put("totalpacienteptv", totalpacienteptv);
        map.put("totalpacientedc", totalpacientedc);
        map.put("pacientesEmTarv", pacientesEmTarv);
        map.put("adultosEmTarv", adultosEmTarv);
        map.put("pediatrico04EmTARV", pediatrico04EmTARV);
        map.put("pediatrico59EmTARV", pediatrico59EmTARV);
        map.put("pediatrico1014EmTARV", pediatrico1014EmTARV);
        return map;

    }


    public Map MMIA_Actualizado_Dispensas(String startDate, String endDate, String diseaseType) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();
        String query = " SELECT  distinct p.patient, "
                + " 		ep.startreason,  "
                + " 		p.reasonforupdate,  "
                + " 		p.dispensatrimestral, "
                + " 		p.dispensasemestral  "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pa.weekssupply) weekssupply, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pds.amount <> 0 and pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' and pre.tipodoenca like '%" + diseaseType + "%'  "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode ";

        int pacientesdispensadosparaDM = 0;
        int pacientesdispensadosparaDT = 0;
        int pacientesdispensadosparaDS = 0;

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                boolean nonuspatient = rs.getString("startreason").contains("nsito") || rs.getString("startreason").contains("ternidade");

                // Dispensa Trimenstral ou Semestral
                if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 0) {
                    pacientesdispensadosparaDM++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 1 && rs.getInt("dispensasemestral") == 0) {
                    pacientesdispensadosparaDT++;
                } else if (!nonuspatient && rs.getInt("dispensatrimestral") == 0 && rs.getInt("dispensasemestral") == 1) {
                    pacientesdispensadosparaDS++;
                }
            }
            rs.close();
        }

        map.put("pacientesdispensadosparaDM", pacientesdispensadosparaDM);
        map.put("pacientesdispensadosparaDT", pacientesdispensadosparaDT);
        map.put("pacientesdispensadosparaDS", pacientesdispensadosparaDS);
        return map;

    }


    public List<MmiaStock> getStockMmmia(Date dataInicial, Date dataFinal, StockCenter stockCenter) {

        List<MmiaStock> mmiaStockXLSList = new ArrayList<MmiaStock>();

        Integer drugId = null;
        String drugFNM = null;
        String drugName = null;
        Integer drugPacksize = null;
        String drugExpireDate = null;
        String drugpackFrom = null;

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = " SELECT " +
                    "   distinct " +
                    "   drug.id, " +
                    "   drug.atccode_id, " +
                    "   drug.name , " +
                    "   drug.packsize , " +
                    "   drug.packsize || ' ' || form.formlanguage1 as packform , " +
                    "   MAX(to_char(stock.expirydate,'MM/YYYY')) as mindate " +
                    " FROM " +
                    "   drug " +
                    " INNER JOIN stock ON stock.drug = drug.id" +
                    " LEFT JOIN form ON form.id = drug.form" +
                    " WHERE " +
                    "     drug.sidetreatment = 'F'" +
                    " AND " +
                    "     stock.stockcenter = " + stockCenter.getId() +
                    " AND " +
                    "   drug.id=stock.drug " +
                    " GROUP BY 1,2,3,4,5 " +
                    " ORDER BY drug.atccode_id asc ";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    drugId = rs.getInt("id");
                    drugFNM = rs.getString("atccode_id");
                    drugName = rs.getString("name");
                    drugPacksize = rs.getInt("packsize");
                    drugExpireDate = rs.getString("mindate");
                    drugpackFrom = rs.getString("packform");

                    MmiaStock mmiaStock = getStockDetails(dataInicial, dataFinal, stockCenter.getId(), drugId, drugPacksize);
                    mmiaStock.setFnm(drugFNM);
                    mmiaStock.setMedicamento(drugName);
                    mmiaStock.setQuantidadeFrasco(drugpackFrom);
                    mmiaStock.setValidade(drugExpireDate);
                    mmiaStockXLSList.add(mmiaStock);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return mmiaStockXLSList;
    }


    public MmiaStock getStockDetails(Date dataInicial, Date dataFinal, Integer stockCenterId, Integer drugId, Integer drugPacksize) {

        MmiaStock mmiaStockLS = new MmiaStock();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String startDate = dateFormat.format(dataInicial);

        String endDate = dateFormat.format(dataFinal);

        int openingpills = 0;
        int received = 0;
        int dispensedpills = 0;
        int dispensed = 0;
        int adjusted = 0;
        int adjustedpills = 0;
        int destroyed = 0;
        int destroyedpills = 0;
        int returned = 0;
        int returnedpills = 0;


        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select " +
                    " " +
                    "COALESCE(( COALESCE(a.received, 0) - COALESCE(b.issued + b.pills, 0) - COALESCE(f.adjusted + f.pills, 0) + COALESCE(h.returned + h.pills, 0)), 0) " +
                    " " +
                    "as openingpills, " +
                    " " +
                    "COALESCE(c.received,0) as received, " +
                    " " +
                    "COALESCE(e.issued,0) + COALESCE(e.pills,0) as destroyed, " +
                    " " +
                    "COALESCE(d.issued,0) + COALESCE(d.pills,0) as dispensed, " +
                    " " +
                    "COALESCE(g.adjusted,0) + COALESCE(g.pills,0) as adjusted, " +
                    " " +
                    "COALESCE(i.returned,0) + COALESCE(i.pills,0) as returned " +
                    " " +
                    "from ( " +
                    "select sum(s.unitsreceived) as received " +
                    "from drug as d, stock as s " +
                    "where s.drug = d.id and d.id = " + drugId + " and s.stockCenter = " + stockCenterId + " and s.datereceived::timestamp::date < '" + startDate + "'::timestamp::date " +
                    " " +
                    ") as a, " +
                    " " +
                    "(select round(floor(sum(pd.amount::real)/" + drugPacksize + ")::numeric,0) as issued,  CASE WHEN MOD(sum(pd.amount), " + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    " " +
                    "from drug as d, stock as s, packageddrugs as pd, package as p " +
                    "where d.id =   " + drugId + " " +
                    "and s.stockCenter =   " + stockCenterId + " " +
                    "and s.drug = d.id and pd.stock = s.id " +
                    "and pd.parentpackage = p.id " +
                    "and p.packdate::timestamp::date <   '" + startDate + "'::timestamp::date " +
                    " " +
                    ") as b, " +
                    " " +
                    "( " +
                    "select sum(s.unitsreceived) as received from drug as d, stock as s " +
                    "where d.id =   " + drugId + " and s.stockCenter =   " + stockCenterId + " and s.drug = d.id " +
                    "and s.datereceived::timestamp::date  >=   '" + startDate + "'::timestamp::date AND s.datereceived::timestamp::date  <=   '" + endDate + "'::timestamp::date " +
                    " " +
                    ") as c, " +
                    " " +
                    "(select round(floor(sum(pd.amount::real)/" + drugPacksize + ")::numeric,0) as issued, CASE WHEN MOD(sum(pd.amount),  " + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    "from drug as d, stock as s, packageddrugs as pd, package as p,prescription as pre " +
                    "where d.id =   " + drugId + " and s.stockCenter =   " + stockCenterId + " " +
                    "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id " +
                    "and p.prescription = pre.id " +
                    "and p.packdate::timestamp::date >=   '" + startDate + "'::timestamp::date and p.packdate::timestamp::date <=   '" + endDate + "'::timestamp::date " +
                    " " +
                    ") as d, " +
                    " " +
                    "(select round(floor(sum(pd.amount::real)/" + drugPacksize + ")::numeric,0) as issued, CASE WHEN MOD(sum(pd.amount),  " + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    "from drug as d, stock as s, packageddrugs as pd, package as p " +
                    "where d.id =   " + drugId + " and s.stockCenter =   " + stockCenterId + " " +
                    "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id " +
                    "and p.prescription is null " +
                    "and p.packdate::timestamp::date  >=   '" + startDate + "'::timestamp::date AND  p.packdate::timestamp::date <=   '" + endDate + "'::timestamp::date " +
                    " " +
                    ") as e, " +
                    " " +
                    "(select round(floor(sum(sa.adjustedvalue::real)/" + drugPacksize + ")::numeric,0) as adjusted, CASE WHEN MOD(sum(sa.adjustedvalue)," + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    " " +
                    "from drug as d, stock as s, stockAdjustment as sa " +
                    "where d.id = " + drugId + " " +
                    "and s.stockCenter = " + stockCenterId + " " +
                    "and s.drug = d.id " +
                    "and sa.stock = s.id " +
                    "and sa.captureDate::timestamp::date <   '" + startDate + "'::timestamp::date " +
                    " " +
                    ") as f, " +
                    " " +
                    "(select round(floor(sum(sa.adjustedvalue::real)/" + drugPacksize + ")::numeric,0) as adjusted, CASE WHEN MOD(sum(sa.adjustedvalue)," + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    " " +
                    "from drug as d, stock as s, stockAdjustment as sa " +
                    "where d.id =   " + drugId + " " +
                    "and s.stockCenter =   " + stockCenterId + " " +
                    "and s.drug = d.id " +
                    "and sa.stock = s.id " +
                    "and sa.captureDate::timestamp::date  >= '" + startDate + "'::timestamp::date AND sa.captureDate::timestamp::date <=   '" + endDate + "'::timestamp::date " +
                    " " +
                    ") as g, " +
                    " " +
                    "(select round(floor(sum(pd.amount::real)/" + drugPacksize + ")::numeric,0) as returned, CASE WHEN MOD(sum(pd.amount),  " + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    " " +
                    "from drug as d, stock as s, packageddrugs as pd, package as p " +
                    "where d.id =   " + drugId + " " +
                    "and s.stockCenter =   " + stockCenterId + " " +
                    "and s.drug = d.id and pd.stock = s.id " +
                    "and pd.parentpackage = p.id " +
                    "and p.stockReturned = true " +
                    "and p.packageReturned = true " +
                    "and p.dateReturned::timestamp::date <   '" + startDate + "'::timestamp::date " +
                    " " +
                    ") as h, " +
                    " " +
                    "(select round(floor(sum(pd.amount::real)/" + drugPacksize + ")::numeric,0) as returned, CASE WHEN MOD(sum(pd.amount),  " + drugPacksize + ") > 0 THEN 1 ELSE 0 END as pills " +
                    " " +
                    "from drug as d, stock as s, packageddrugs as pd, package as p " +
                    "where d.id =   " + drugId + " " +
                    "and s.stockCenter =   " + stockCenterId + " " +
                    "and s.drug = d.id and pd.stock = s.id " +
                    "and pd.parentpackage = p.id " +
                    "and p.stockReturned = true " +
                    "and p.packageReturned = true " +
                    "and p.dateReturned::timestamp::date  >=   '" + startDate + "'::date AND p.dateReturned::timestamp::timestamp::date <=   '" + endDate + "'::timestamp::date " +
                    " " +
                    ") as i ";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    openingpills = rs.getInt("openingpills");
                    received = rs.getInt("received");
                    dispensed = rs.getInt("dispensed");
                    adjusted = rs.getInt("adjusted");
                    destroyed = rs.getInt("destroyed");
                    returned = rs.getInt("returned");

                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        int totalpills = openingpills + returned + received - dispensed - destroyed - adjusted;

        mmiaStockLS.setSaldo(String.valueOf(openingpills));
        mmiaStockLS.setEntrdas(String.valueOf(received));
        mmiaStockLS.setSaidas(String.valueOf(dispensed));
        mmiaStockLS.setPerdasAjustes(String.valueOf(adjusted));
        mmiaStockLS.setInventario(String.valueOf(totalpills));

        return mmiaStockLS;

    }

    public List<MmiaRegimeTerapeutico> getRegimenMmmia(Date dataInicial, Date dataFinal) {

        List<MmiaRegimeTerapeutico> mmiaRegimenXLSList = new ArrayList<MmiaRegimeTerapeutico>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String startDate = dateFormat.format(dataInicial);

        String endDate = dateFormat.format(dataFinal);

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = " SELECT  distinct rt.regimeesquema,rt.codigoregime, count(distinct p.patient) as totalpacientes " +
                    "FROM (select max(pre.date) predate, max(pa.pickupdate) pickupdate, pat.id " +
                    "from package pa " +
                    "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                    "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                    "inner join prescription pre on pre.id = pa.prescription " +
                    "inner join patient pat ON pre.patient=pat.id " +
                    "where pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <=   '" + endDate + "') " +
                    " OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >   '" + endDate + "' " +
                    "  and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day',   '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' " +
                    "  and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day',   '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <=   '" + endDate + "')) " +
                    "GROUP BY 3 order by 3) pack " +
                    "inner join prescription p on p.date = pack.predate and p.patient=pack.id " +
                    "inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate " +
                    "inner join regimeterapeutico rt on rt.regimeid = p.regimeid " +
                    "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                    "    from episode WHERE stopdate is null and startdate <=   '" + endDate + "' " +
                    "    GROUP BY 2,3 " +
                    ") visit on visit.patient = pack.id " +
                    "where visit.startreason not like '%ansito%' and visit.startreason not like '%ternidade%' " +
                    "group by 1,2 order by 1";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    MmiaRegimeTerapeutico mmiaRegimeTerapeutico = new MmiaRegimeTerapeutico();
                    mmiaRegimeTerapeutico.setCodigo(rs.getString("codigoregime"));
                    mmiaRegimeTerapeutico.setRegimeTerapeutico(rs.getString("regimeesquema"));
                    mmiaRegimeTerapeutico.setTotalDoentes(String.valueOf(rs.getInt("totalpacientes")));

                    mmiaRegimenXLSList.add(mmiaRegimeTerapeutico);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return mmiaRegimenXLSList;
    }


    public List<MmiaRegimeTerapeutico> getRegimenMmmiaActualizado(Date dataInicial, Date dataFinal) {

        List<MmiaRegimeTerapeutico> mmiaRegimenXLSList = new ArrayList<MmiaRegimeTerapeutico>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String startDate = dateFormat.format(dataInicial);

        String endDate = dateFormat.format(dataFinal);

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = " SELECT  distinct rt.regimeesquema,rt.codigoregime, count(distinct p.patient) as totalpacientes " +
                    "FROM (select max(pre.date) predate, max(pa.pickupdate) pickupdate, pat.id " +
                    "from package pa " +
                    "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                    "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                    "inner join prescription pre on pre.id = pa.prescription " +
                    "inner join patient pat ON pre.patient=pat.id " +
                    "where pds.amount <> 0 and (pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <=   '" + endDate + "')  " +
                    "GROUP BY 3 order by 3) pack " +
                    "inner join prescription p on p.date = pack.predate and p.patient=pack.id " +
                    "inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate " +
                    "inner join regimeterapeutico rt on rt.regimeid = p.regimeid " +
                    "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                    "    from episode WHERE stopdate is null and startdate <=   '" + endDate + "' " +
                    "    GROUP BY 2,3 " +
                    ") visit on visit.patient = pack.id " +
                    "where visit.startreason not like '%ansito%' and visit.startreason not like '%ternidade%' " +
                    "group by 1,2 order by 1";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    MmiaRegimeTerapeutico mmiaRegimeTerapeutico = new MmiaRegimeTerapeutico();
                    mmiaRegimeTerapeutico.setCodigo(rs.getString("codigoregime"));
                    mmiaRegimeTerapeutico.setRegimeTerapeutico(rs.getString("regimeesquema"));
                    mmiaRegimeTerapeutico.setTotalDoentes(String.valueOf(rs.getInt("totalpacientes")));

                    mmiaRegimenXLSList.add(mmiaRegimeTerapeutico);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return mmiaRegimenXLSList;
    }

    /**
     * Mapa para pacientes e desagregacao no Relatorio de indicadores mensais
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Map indicadoresMensais(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();
        String query = "SELECT  distinct p.patient,p.reasonforupdate, p.dispensatrimestral, " +
                "p.dispensasemestral, p.prep,p.ptv,p.dc,p.ppe,p.ce,l.linhanome, " +
                "p.af, p.gaac,p.ca,p.tb,p.ccr,p.saaj,p.cpn,p.fr, " +
                "EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) :: int dateofbirth, ep.startreason, " +
                "CASE " +
                "	WHEN p.dispensatrimestral = 1 AND pa.pickupdate >= '" + startDate + "' THEN p.tipodt " +
                "	ELSE 'Transporte' " +
                "END  tipodt, " +
                "CASE " +
                "	WHEN p.dispensasemestral = 1 AND pa.pickupdate >= '" + startDate + "' THEN p.tipods " +
                "	ELSE 'Transporte' " +
                "END  tipods "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pa.weekssupply) weekssupply, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ) )  "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode ";

        int adultnovosdt = 0;
        int adultmanuntencaodt = 0;
        int adulttransportedt = 0;

        int pednovosdt = 0;
        int pedmanuntencaodt = 0;
        int pedtransportedt = 0;

        int adultnovosds = 0;
        int adultmanuntencaods = 0;
        int adulttransporteds = 0;

        int pednovosds = 0;
        int pedmanuntencaods = 0;
        int pedtransporteds = 0;

        int totalmmia = 0;

        int totalaf = 0;
        int totalgaac = 0;
        int totalca = 0;
        int totalptv = 0;
        int totalcpn = 0;
        int totaltb = 0;
        int totalccr = 0;
        int totalsaaj = 0;
        int totalprep = 0;
        int totaldc = 0;
        int totalppe = 0;
        int totalCE = 0;
        int totalDM = 0;


        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                boolean nonuspatient = rs.getString("startreason").contains("nsito") || rs.getString("startreason").contains("ternidade");

                // Total no MMIA
                if (!nonuspatient) {
                    totalmmia++;
                }

                // Sector de Levantamento
                if (!nonuspatient && !rs.getString("prep").equalsIgnoreCase("F")) {
                    totalprep++;
                }
                if (!nonuspatient && !rs.getString("ptv").equalsIgnoreCase("F")) {
                    totalptv++;
                }
                if (!nonuspatient && !rs.getString("dc").equalsIgnoreCase("F")) {
                    totaldc++;
                }
                if (!nonuspatient && !rs.getString("ppe").equalsIgnoreCase("F")) {
                    totalppe++;
                }
                if (!nonuspatient && !rs.getString("ce").equalsIgnoreCase("F")) {
                    totalCE++;
                }
                if (!nonuspatient && !rs.getString("ccr").equalsIgnoreCase("F")) {
                    totalccr++;
                }
                if (!nonuspatient && !rs.getString("gaac").equalsIgnoreCase("F")) {
                    totalgaac++;
                }
                if (!nonuspatient && !rs.getString("saaj").equalsIgnoreCase("F")) {
                    totalsaaj++;
                }
                if (!nonuspatient && !rs.getString("ca").equalsIgnoreCase("F")) {
                    totalca++;
                }
                if (!nonuspatient && !rs.getString("tb").equalsIgnoreCase("F")) {
                    totaltb++;
                }
                if (!nonuspatient && !rs.getString("af").equalsIgnoreCase("F")) {
                    totalaf++;
                }
                if (!nonuspatient && !rs.getString("cpn").equalsIgnoreCase("F")) {
                    totalcpn++;
                }

                // idade e DT
                if (!nonuspatient && rs.getInt("dateofbirth") >= 15 && rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Novo")) {
                    adultnovosdt++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 15 && rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Manunt")) {
                    adultmanuntencaodt++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 15 && rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Transporte")) {
                    adulttransportedt++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") < 15 && rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Novo")) {
                    pednovosdt++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") < 15 && rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Manunt")) {
                    pedmanuntencaodt++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") < 15 && rs.getInt("dispensatrimestral") == 1 && rs.getString("tipodt").contains("Transporte")) {
                    pedtransportedt++;
                }

                // idade e DS
                if (!nonuspatient && rs.getInt("dateofbirth") >= 15 && rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Novo")) {
                    adultnovosds++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 15 && rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Manunt")) {
                    adultmanuntencaods++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") >= 15 && rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Transporte")) {
                    adulttransporteds++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") < 15 && rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Novo")) {
                    pednovosds++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") < 15 && rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Manunt")) {
                    pedmanuntencaods++;
                } else if (!nonuspatient && rs.getInt("dateofbirth") < 15 && rs.getInt("dispensasemestral") == 1 && rs.getString("tipods").contains("Transporte")) {
                    pedtransporteds++;
                }

                if (!nonuspatient && rs.getInt("dispensasemestral") != 1 && rs.getInt("dispensatrimestral") != 1) {
                    totalDM++;
                }

            }
            rs.close();
        }

        map.put("adultnovosdt", adultnovosdt);
        map.put("adultmanuntencaodt", adultmanuntencaodt);
        map.put("adulttransportedt", adulttransportedt);
        map.put("adultcumulativodt", adultnovosdt + adultmanuntencaodt + adulttransportedt);

        map.put("pednovosdt", pednovosdt);
        map.put("pedmanuntencaodt", pedmanuntencaodt);
        map.put("pedtransportedt", pedtransportedt);
        map.put("pedcumulativodt", pednovosdt + pedmanuntencaodt + pedtransportedt);

        map.put("adultnovosds", adultnovosds);
        map.put("adultmanuntencaods", adultmanuntencaods);
        map.put("adulttransporteds", adulttransporteds);
        map.put("adultcumulativods", adultnovosds + adultmanuntencaods + adulttransporteds);

        map.put("pednovosds", pednovosds);
        map.put("pedmanuntencaods", pedmanuntencaods);
        map.put("pedtransporteds", pedtransporteds);
        map.put("pedcumulativods", pednovosds + pedmanuntencaods + pedtransporteds);

        map.put("totalDM", totalDM);
        map.put("totalmmia", totalmmia);
        map.put("totalaf", totalaf);
        map.put("totalgaac", totalgaac);
        map.put("totalca", totalca);
        map.put("totalptv", totalptv);
        map.put("totalcpn", totalcpn);
        map.put("totaltb", totaltb);
        map.put("totalccr", totalccr);
        map.put("totalsaaj", totalsaaj);
        map.put("totalprep", totalprep);
        map.put("totaldc", totaldc);
        map.put("totalppe", totalppe);
        map.put("totalCE", totalCE);
        return map;

    }


    /**
     * Mapa para pacientes e desagregacao no Relatorio de DT
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Map DispensaTrimestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();
        String query = "SELECT 	distinct pat.patientid, pat.firstnames, "
                + "		pat.lastname, "
                + "		pg_catalog.date(p.date) dataprescricao, "
                + "		pg_catalog.date(pack.pickupdate) dataLevantamento, "
                + "		pack.dateexpectedstring proximoLevantamento , "
                + "		reg.regimeesquema, "
                + "		CASE "
                + "          WHEN p.dispensatrimestral = 1 AND pack.pickupdate >= '" + startDate + "' AND (select count(id) = 1 from package where prescription = p.id and pickupdate <= '" + endDate + "' ) THEN p.tipodt "
                + "          WHEN p.dispensatrimestral = 1 AND pack.pickupdate >= '" + startDate + "' AND (select count(id) > 1 from package where prescription = p.id and pickupdate <= '" + endDate + "' ) THEN 'Manuntencao' "
                + "			ELSE 'Transporte' "
                + "		END  tipodt "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pdit.dateexpectedstring) dateexpectedstring, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pre.tipodoenca like '%ARV' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode "
                + "  inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " WHERE p.tipodoenca like '%ARV' and p.dispensatrimestral = 1 and (ep.startreason not like '%nsito%' and ep.startreason not like '%ternidade%') "
                + " order by 8";

        int totalpacientesmanter = 0;
        int totalpacientesnovos = 0;
        int totalpacienteManuntencaoTransporte = 0;
        int totalpacienteCumulativo = 0;

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                totalpacienteCumulativo++;

                if (rs.getString("tipodt") != null) {

                    // Tipo de Pacinte
                    if (rs.getString("tipodt").contains("Novo")) {
                        totalpacientesnovos++;
                    } else if ((rs.getString("tipodt").contains("Manunte"))) {
                        totalpacientesmanter++;
                    } else if (rs.getString("tipodt").contains("Transporte")) {
                        totalpacienteManuntencaoTransporte++;
                    }
                }
            }
            rs.close();
        }

        map.put("totalpacientesnovos", totalpacientesnovos);
        map.put("totalpacientesmanter", totalpacientesmanter);
        map.put("totalpacienteManuntencaoTransporte", totalpacienteManuntencaoTransporte);
        map.put("totalpacienteCumulativo", totalpacienteCumulativo);
        return map;

    }

    /**
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public List<DispensaTrimestralSemestral> dispensaTrimestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();
        String query = "SELECT 	distinct pat.patientid, pat.firstnames, "
                + "		pat.lastname, "
                + "		pg_catalog.date(p.date) dataprescricao, "
                + "		pg_catalog.date(pack.pickupdate) dataLevantamento, "
                + "		pack.dateexpectedstring proximoLevantamento , "
                + "		reg.regimeesquema, "
                + "		CASE "
                + "          WHEN p.dispensatrimestral = 1 AND pack.pickupdate >= '" + startDate + "' AND (select count(id) = 1 from package where prescription = p.id and pickupdate <= '" + endDate + "') THEN p.tipodt "
                + "          WHEN p.dispensatrimestral = 1 AND pack.pickupdate >= '" + startDate + "' AND (select count(id) > 1 from package where prescription = p.id and pickupdate <= '" + endDate + "') THEN 'Manuntencao' "
                + "			ELSE 'Transporte' "
                + "		END  tipodt "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pdit.dateexpectedstring) dateexpectedstring, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pre.tipodoenca like '%ARV' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode "
                + "  inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " WHERE p.tipodoenca like '%ARV' and p.dispensatrimestral = 1 and (ep.startreason not like '%nsito%' and ep.startreason not like '%ternidade%') "
                + " order by 8";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        List<DispensaTrimestralSemestral> dispensaTrimestralXLS = new ArrayList<DispensaTrimestralSemestral>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {
                DispensaTrimestralSemestral lstDispensaTrimestral = new DispensaTrimestralSemestral();
                lstDispensaTrimestral.setPatientIdentifier(rs.getString("patientid"));
                lstDispensaTrimestral.setNome(rs.getString("firstnames") + rs.getString("lastname"));
                lstDispensaTrimestral.setRegimeTerapeutico(rs.getString("regimeesquema"));
                lstDispensaTrimestral.setTipoPaciente(rs.getString("tipodt"));
                lstDispensaTrimestral.setDataPrescricao(rs.getString("dataprescricao"));
                lstDispensaTrimestral.setDataLevantamento(rs.getString("dataLevantamento"));
                lstDispensaTrimestral.setDataProximoLevantamento(rs.getString("proximoLevantamento"));

                dispensaTrimestralXLS.add(lstDispensaTrimestral);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return dispensaTrimestralXLS;
    }

    /**
     * Mapa para pacientes e desagregacao no Relatorio de Ds
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Map DispensaSemestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();

        String query = "SELECT 	distinct pat.patientid, pat.firstnames, "
                + "		pat.lastname, "
                + "		pg_catalog.date(p.date) dataprescricao, "
                + "		pg_catalog.date(pack.pickupdate) dataLevantamento, "
                + "		pack.dateexpectedstring proximoLevantamento , "
                + "		reg.regimeesquema, "
                + "		CASE "
                + "			WHEN p.dispensasemestral = 1 AND pack.pickupdate >= '" + startDate + "' THEN p.tipods "
                + "			ELSE 'Transporte' "
                + "		END  tipods "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pdit.dateexpectedstring) dateexpectedstring, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pre.tipodoenca like '%ARV' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ) )  "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode "
                + "  inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " WHERE p.tipodoenca like '%ARV' and p.dispensasemestral = 1 and (ep.startreason not like '%nsito%' and ep.startreason not like '%ternidade%') "
                + " order by 8";

        int totalpacientesmanter = 0;
        int totalpacientesnovos = 0;
        int totalpacienteManuntencaoTransporte = 0;
        int totalpacienteCumulativo = 0;

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                totalpacienteCumulativo++;
                // Tipo de Pacinte

                if (rs.getString("tipods") != null) {

                    if (rs.getString("tipods").contains("Novo")) {
                        totalpacientesnovos++;
                    } else if ((rs.getString("tipods").contains("Manunte"))) {
                        totalpacientesmanter++;
                    } else if (rs.getString("tipods").contains("Transporte")) {
                        totalpacienteManuntencaoTransporte++;
                    }
                }
            }
            rs.close();
        }

        map.put("totalpacientesnovos", totalpacientesnovos);
        map.put("totalpacientesmanter", totalpacientesmanter);
        map.put("totalpacienteManuntencaoTransporte", totalpacienteManuntencaoTransporte);
        map.put("totalpacienteCumulativo", totalpacienteCumulativo);
        return map;
    }


    /**
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public List<DispensaTrimestralSemestral> dispensaSemestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        Map<String, Object> map = new HashMap<String, Object>();

        String query = "SELECT 	distinct pat.patientid, pat.firstnames, "
                + "		pat.lastname, "
                + "		pg_catalog.date(p.date) dataprescricao, "
                + "		pg_catalog.date(pack.pickupdate) dataLevantamento, "
                + "		pack.dateexpectedstring proximoLevantamento , "
                + "		reg.regimeesquema, "
                + "		CASE "
                + "			WHEN p.dispensasemestral = 1 AND pack.pickupdate >= '" + startDate + "' THEN p.tipods "
                + "			ELSE 'Transporte' "
                + "		END  tipods "
                + " FROM  "
                + " ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pat.dateofbirth) dateofbirth, max(pdit.dateexpectedstring) dateexpectedstring, "
                + " 			pat.id, max(visit.id) episode "
                + " 	from package pa  "
                + " 	inner join packageddrugs pds on pds.parentpackage = pa.id  "
                + " 	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id  "
                + " 	inner join prescription pre on pre.id = pa.prescription  "
                + " 	inner join patient pat ON pre.patient=pat.id  "
                + " 	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + " 				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + " 				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + " 	where pre.tipodoenca like '%ARV' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + " 	GROUP BY 5 order by 5) pack  "
                + " 	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + " 	inner join patient pat on pat.id = pack.id  "
                + " 	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                + " 	inner join linhat l on l.linhaid = p.linhaid  "
                + " 	inner join episode ep on ep.id = pack.episode "
                + "  inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " WHERE p.tipodoenca like '%ARV' and p.dispensasemestral = 1 and (ep.startreason not like '%nsito%' and ep.startreason not like '%ternidade%') "
                + " order by 8";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        List<DispensaTrimestralSemestral> dispensaSemestralXLS = new ArrayList<DispensaTrimestralSemestral>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {
                DispensaTrimestralSemestral lstDispensaSemestral = new DispensaTrimestralSemestral();
                lstDispensaSemestral.setPatientIdentifier(rs.getString("patientid"));
                lstDispensaSemestral.setNome(rs.getString("firstnames") + rs.getString("lastname"));
                lstDispensaSemestral.setRegimeTerapeutico(rs.getString("regimeesquema"));
                lstDispensaSemestral.setTipoPaciente(rs.getString("tipods"));
                lstDispensaSemestral.setDataPrescricao(rs.getString("dataprescricao"));
                lstDispensaSemestral.setDataLevantamento(rs.getString("dataLevantamento"));
                lstDispensaSemestral.setDataProximoLevantamento(rs.getString("proximoLevantamento"));

                dispensaSemestralXLS.add(lstDispensaSemestral);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return dispensaSemestralXLS;
    }


    /**
     * Total de pacientes novos que iniciam dispensa trimestral
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesNovosDispensaTrimestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT patient, dispensatrimestral, date "
                + " FROM prescription "
                + " WHERE dispensatrimestral=1 AND tipodt = 'Novo' "
                + " AND pg_catalog.date(date) >= " + "\'" + startDate + "\'"
                + " AND pg_catalog.date(date) <=" + " \'" + endDate + "\'"
                + " group by patient, dispensatrimestral, date ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    /**
     * Total de pacientes novos que iniciam dispensa semestral
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesNovosDispensaSemestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT patient, dispensasemestral, date "
                + " FROM prescription "
                + " WHERE dispensasemestral=1 AND tipods = 'Novo' "
                + " AND pg_catalog.date(date) >= " + "\'" + startDate + "\'" + " AND pg_catalog.date(date) <=" + " \'" + endDate + "\'"
                + " group by patient, dispensasemestral, date ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    /**
     * Total de pacientes manutencao transporte em dispensa semestral
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesManuntencaoTransporteDispensaTrimestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT distinct pat.id "
                + "         FROM (select max(pre.date) predate,  pat.id "
                + "                 FROM package pa "
                + "                 INNER JOIN packageddrugs pds on pds.parentpackage = pa.id "
                + "                 INNER JOIN prescription pre on pre.id = pa.prescription "
                + "                 INNER JOIN packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                + "                  INNER JOIN patient pat ON pre.patient=pat.id "
                + "                 WHERE pg_catalog.date(pa.pickupdate) < " + "\'" + startDate + "\'" + " AND pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= " + "\'" + endDate + "\'"
                + "             GROUP BY 2"
                + ") pack "
                + " INNER JOIN prescription pre on pre.date = pack.predate and pre.patient=pack.id "
                + " INNER JOIN patient pat ON pre.patient=pat.id  "
                + " INNER JOIN regimeterapeutico reg ON pre.regimeid=reg.regimeid  "
                + " WHERE pre.dispensatrimestral = 1 and length(TRIM(pre.tipodt)) > 0 and pre.tipodt is not null"
                + " group by  pat.id ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    /**
     * Total de pacientes manutencao transporte em dispensa semestral
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesManuntencaoTransporteDispensaSemestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT distinct pat.id "
                + "         FROM (select max(pre.date) predate,  pat.id "
                + "                 FROM package pa "
                + "                 INNER JOIN packageddrugs pds on pds.parentpackage = pa.id "
                + "                 INNER JOIN prescription pre on pre.id = pa.prescription "
                + "                 INNER JOIN packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                + "                  INNER JOIN patient pat ON pre.patient=pat.id "
                + "                 WHERE pg_catalog.date(pa.pickupdate) < " + "\'" + startDate + "\'" + " AND pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= " + "\'" + endDate + "\'"
                + "             GROUP BY 2"
                + ") pack "
                + " INNER JOIN prescription pre on pre.date = pack.predate and pre.patient=pack.id "
                + " INNER JOIN patient pat ON pre.patient=pat.id  "
                + " INNER JOIN regimeterapeutico reg ON pre.regimeid=reg.regimeid  "
                + " WHERE pre.dispensasemestral = 1 and length(TRIM(pre.tipodt)) > 0 and pre.tipodt is not null"
                + " group by  pat.id ) v ";


        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    public int totalPacientesCumulativoDispensaTrimestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT distinct pr.patient "
                + " FROM prescription pr"
                + " WHERE pr.dispensatrimestral = 1 "
                + " group by  pr.patient ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    public int totalPacientesCumulativoDispensaSemestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT distinct pr.patient "
                + " FROM prescription pr"
                + " WHERE pr.dispensasemestral = 1 "
                + " group by  pr.patient ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    /**
     * Total de pacientes Manter em dispensa trimestral
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesManterDispensaTrimestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT distinct pr.patient, pr.dispensatrimestral, pr.date "
                + " FROM prescription pr"
                + " inner join package pack on pack.prescription = pr.id "
                + " inner join packageddrugs packdrug on packdrug.parentPackage = pack.id "
                + " WHERE (pr.dispensatrimestral=1 and pr.tipodt like '%Manunte%')"
                + " AND pg_catalog.date(pr.date) >= " + "\'" + startDate + "\'" + " AND pg_catalog.date(pr.date) <=" + " \'" + endDate + "\'"
                + " group by  pr.patient, pr.dispensatrimestral,pr.date ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    /**
     * Total de pacientes Manter em dispensa semestral
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesManterDispensaSemestral(String startDate, String endDate) throws ClassNotFoundException, SQLException {

        String query = " SELECT count(*) soma "
                + " FROM ( SELECT distinct pr.patient, pr.dispensasemestral, pr.date "
                + " FROM prescription pr"
                + " inner join package pack on pack.prescription = pr.id "
                + " inner join packageddrugs packdrug on packdrug.parentPackage = pack.id "
                + " WHERE (pr.dispensasemestral=1 and pr.tipods = 'Manuntencao')"
                + " AND pg_catalog.date(pr.date) >= " + "\'" + startDate + "\'" + " AND pg_catalog.date(pr.date) <=" + " \'" + endDate + "\'"
                + " group by  pr.patient, pr.dispensasemestral,pr.date ) v ";

        int total = 0;
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            rs.next();
            total = rs.getInt("soma");
            rs.close(); //
        }

        return total;

    }

    /**
     * Retorna a conexao com a base de dados
     *
     * @param usr
     * @param pwd
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Connection retornaConexao(String usr, String pwd)
            throws SQLException, ClassNotFoundException {

        String url = iDartProperties.hibernateConnectionUrl;

        // Carregar o driver
        Class.forName("org.postgresql.Driver");

        // Conectar com o servidor de banco de dados
        conn_db = DriverManager.getConnection(url, usr, pwd);

        // st = conn_db.createStatement();
        return conn_db;

    }

    /**
     * Devolve a lista de PrescriptionToPatient, na verdade so devolve lista de
     * tamanho 1
     *
     * @param patientid
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public List<PrescriptionToPatient> listPtP(String patientid)
            throws ClassNotFoundException, SQLException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = ""
                + "SELECT "
                + "p.id, "
                + "p.current, "
                + "p.duration, "
                + "p.reasonforupdate, "
                + "p.notes, "
                + "pt.patientid, "
                + "rt.regimeesquema, "
                + " date_part(\'YEAR\',now())-date_part(\'YEAR\',pt.dateofbirth) as idade,  "
                + " p.motivomudanca AS motivomudanca, "
                + " p.datainicionoutroservico as datainicionoutroservico, "
                + "lt.linhanome " + " FROM " + "  patient pt, "
                + "regimeterapeutico rt,  " + "linhat lt, "
                + "prescription AS p " + "WHERE ("
                + "(p.current = \'T\'::bpchar) " + "AND "
                + "(pt.id = p.patient) " + "AND " + "(pt.patientid=\'"
                + patientid + "\') " + "AND " + "(rt.regimeid=p.regimeid))";

        // ResultSet rs =
        // st.executeQuery("select id, current, duration, reasonforupdate, notes, patientid from PrescriptioToPatient where patientid=\'"+patientid+"\'");
        List<PrescriptionToPatient> ptp = new ArrayList();
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                ptp.add(new PrescriptionToPatient(rs.getInt("id"), rs.getString("current"), rs.getInt("duration"), rs.getString("reasonforupdate"), rs.getString("notes"),
                        rs.getString("patientid"), rs.getString("regimeesquema"), rs.getInt("idade"), rs.getString("motivomudanca"), rs.getDate("datainicionoutroservico")));
            }
            rs.close(); // � necess�rio fechar o resultado ao terminar
        }

        st.close();
        conn_db.close();
        return ptp;
    }

    /**
     * Converte uma data para o formato DD Mon YYYY
     *
     * @param date
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Date converteData(String date) throws ClassNotFoundException,
            SQLException {

        Date data = new Date();
        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select to_date(\'" + date + "\', \'DD Mon YYYY\')";
        ResultSet rs = st.executeQuery(query);

        rs.next();
        data = rs.getDate("to_date");

        st.close();
        conn_db.close();
        return data;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     * @throws SQLException
     */
    public int mesesDispensadosParaDM(String startDate, String endDate) throws SQLException {

        int mesesPacientes = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.dispensatrimestral = 0 AND  pre.dispensasemestral = 0 and (pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR " +
                "((pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "') " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        ResultSet rs = st.executeQuery(query);

        if (rs != null) {
            while (rs.next()) {
                // numero de semanas dispensadas por 4 para obter numero de meses dispensados
//                mesesPacientes = rs.getInt("soma") / 4;
                mesesPacientes = rs.getInt("nrPacientesTarv");
            }
            rs.close(); //
        }
        return mesesPacientes;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     * @throws SQLException
     */
    public int mesesDispensadosParaDT(String startDate, String endDate) throws SQLException {

        int mesesPacientes = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.dispensatrimestral = 1 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR " +
                "(pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "') " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        ResultSet rs = st.executeQuery(query);

        if (rs != null) {
            while (rs.next()) {
                // numero de semanas dispensadas por 4 para obter numero de meses dispensados
//                mesesPacientes = rs.getInt("soma") / 4;
                mesesPacientes = rs.getInt("nrPacientesTarv");

            }
            rs.close(); //
        }
        return mesesPacientes;
    }

    /**
     * @param startDate
     * @param endDate
     * @return
     * @throws SQLException
     */
    public int mesesDispensadosParaDS(String startDate, String endDate) throws SQLException {

        int mesesPacientes = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.dispensasemestral = 1 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR " +
                "(pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "') " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        ResultSet rs = st.executeQuery(query);

        if (rs != null) {
            while (rs.next()) {
                // numero de semanas dispensadas por 4 para obter numero de meses dispensados
//                mesesPacientes = rs.getInt("soma") / 4;
                mesesPacientes = rs.getInt("nrPacientesTarv");
            }
            rs.close(); //
        }
        return mesesPacientes;
    }

    /**
     * devolve um vector de todos medicamentos com seus AMC, SALDO E QUANTIDADE
     * DE REQUISICAO
     *
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Vector<RiscoRoptura> selectRiscoDeRopturaStock()
            throws ClassNotFoundException, SQLException {

        String query = "SELECT drugname, consumo_max_ult_3meses, saldos "
                + "FROM " + "alimenta_risco_roptura";

        Vector<RiscoRoptura> riscos = new Vector<RiscoRoptura>();
        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                RiscoRoptura rr = new RiscoRoptura(rs.getString("drugname"),
                        rs.getInt("consumo_max_ult_3meses"),
                        rs.getInt("saldos"),
                        rs.getInt("consumo_max_ult_3meses") * 3
                                - rs.getInt("saldos"));

                riscos.add(rr);
                log.trace("   ");

            }
            rs.close(); // � necess�rio fechar o resultado ao terminar
        }

        st.close();
        conn_db.close();
        return riscos;

    }

    /**
     * Total de pacientes que levantaram ARVs num periodo
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesFarmacia(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {

        /*
         * String query="" + "SELECT  " +
         * " distinct packagedruginfotmp.patientid " + " FROM  " +
         * " packagedruginfotmp " + "  WHERE " +
         * "  packagedruginfotmp.dispensedate::timestamp::date >=  " +
         * "\'"+startDate+"\'" +
         * "AND packagedruginfotmp.dispensedate::timestamp::date <= " +
         * " \'"+endDate+"\'" + " AND " + " dispensedate IS NOT NULL";
         */
        String query = "SELECT SUM(count) AS totalPharm FROM ( "
                + "select abc.regimeesquema, abc.count, abc2.count2 "
                + "from "
                + "( "
                + "select regimeesquema, count(*) "
                + "from "
                + "(select * from prescription,regimeterapeutico, package "
                + "where prescription.regimeid=regimeterapeutico.regimeid AND "
                + "prescription.ppe='F' "
                + "AND regimeterapeutico.active=true and prescription.id=package.prescription AND "
                + "package.pickupdate::timestamp::date >= " + "'"
                + startDate
                + "'::timestamp::date  AND  package.pickupdate::timestamp::date <= "
                + "'"
                + endDate
                + "'::timestamp::date  order by pediatrico asc "
                + ") as tabela "
                + "group by regimeesquema "
                + ") AS abc "
                + "full OUTER JOIN (select "
                + "regimeesquema, count(*) as count2 "
                + "from (select* from prescription,regimeterapeutico, package "
                + "where prescription.regimeid=regimeterapeutico.regimeid AND "
                + "prescription.ppe='F' "
                + "AND regimeterapeutico.active=true and prescription.id=package.prescription "
                + "AND package.weekssupply=8 AND package.pickupdate::timestamp::date >= "
                + "'"
                + startDate
                + "'::timestamp::date - INTEGER '30' AND  package.pickupdate::timestamp::date <= "
                + "'"
                + endDate
                + "'::timestamp::date - INTEGER '30'  order by pediatrico asc) as tabela "
                + "group by regimeesquema "
                + ") as abc2 on abc.regimeesquema=abc2.regimeesquema "
                + ") AS totalIdartPharm";

        int total = 0;

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);

        if (rs != null) {
            while (rs.next()) {
                total = rs.getInt("totalPharm");
            }

            rs.close();
        }

        return total;
    }

    public int pacientesActivosEmTarv(String startDate, String endDate)
            throws SQLException, ClassNotFoundException {

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate,  pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where (pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR  " +
                "((pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "') " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "where visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);

        int numeroDePacientesEmTarv = 0;

        if (rs != null) {
            while (rs.next()) {
                numeroDePacientesEmTarv = rs.getInt("nrPacientesTarv");
            }

            rs.close();
        }

        return numeroDePacientesEmTarv;
    }

    public int pacientesLinhasActivosEmTarv(String startDate, String endDate, int linha)
            throws SQLException, ClassNotFoundException {

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate,  pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "inner join linhat l on l.linhaid = pre.linhaid " +
                "where (l.linhanome like '%" + linha + "%') and (pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR  " +
                "((pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "') " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "where visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);

        int numeroDePacientesEmTarv = 0;

        if (rs != null) {
            while (rs.next()) {
                numeroDePacientesEmTarv = rs.getInt("nrPacientesTarv");
            }

            rs.close();
        }

        return numeroDePacientesEmTarv;
    }

    public int pacientesActivosEmTarvMaiorIdade(String startDate, String endDate, int idade)
            throws SQLException, ClassNotFoundException {

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate,  pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where ( EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) :: int >=" + idade + ")" +
                "and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR  " +
                "((pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "')) " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "where visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);

        int numeroDePacientesEmTarv = 0;

        if (rs != null) {
            while (rs.next()) {
                numeroDePacientesEmTarv = rs.getInt("nrPacientesTarv");
            }

            rs.close();
        }

        return numeroDePacientesEmTarv;
    }

    public int pacientesActivosEmTarvFaixaEtaria(String startDate, String endDate, int minYears, int maxYears)
            throws SQLException, ClassNotFoundException {

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate,  pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where (EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) :: int >= " + minYears +
                "and EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) :: int <= " + maxYears + ")" +
                "and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR  " +
                "((pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "')) " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "where visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";


        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);

        int numeroDePacientesEmTarv = 0;

        if (rs != null) {
            while (rs.next()) {
                numeroDePacientesEmTarv = rs.getInt("nrPacientesTarv");
            }

            rs.close();
        }

        return numeroDePacientesEmTarv;
    }


    /**
     * Total de pacientes que iniciaram o tratamento de ARV num periodo
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesInicio(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.reasonforupdate='Inicia' and (pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total = rs.getInt("nrPacientesTarv");

            }
            rs.close(); //
        }
        return total;

    }

    public int totalPacientesEmTransito(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;


        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason like '%nsito%' or visit.startreason like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total = rs.getInt("nrPacientesTarv");

            }
            rs.close(); //
        }
        return total;
    }

    /**
     * PARA MMIA PERSONALIZADO
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesInicioP(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT "
                + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, "
                + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\'  "
                + " AND  "
                + " prescription.reasonforupdate=\'Inicia\' AND prescription.ptv=\'F\' AND prescription.tb=\'F\' "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid, "
                + " packagedruginfotmp.dispensedate "
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes na manutencao de ARV num periodo
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesManter(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate,  pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where (pre.reasonforupdate='Manter' OR pre.reasonforupdate = 'Reiniciar') and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') OR  " +
                "((pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) - pg_catalog.date(pa.pickupdate)) > 50 " +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "')) " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "where visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total = rs.getInt("nrPacientesTarv");

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes na manutencao Transporte de ARV num periodo
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesTransporte(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate,  pat.id, pa.prescription, pdit.dateexpectedstring " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where (pre.dispensatrimestral = 1 OR pre.dispensasemestral = 1)" +
                "and pg_catalog.date(pa.pickupdate) < '" + startDate + "' and  " +
                "pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) >= '" + endDate + "' " +
                "GROUP BY 2,3,4) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "where visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total = rs.getInt("nrPacientesTarv");

            }
            rs.close();
        }
        return total;

    }

    /**
     * Total de pacientes trabsferidos de num periodo
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesTransferidoDe(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.reasonforupdate like '%ransfer%' and (pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total = rs.getInt("nrPacientesTarv");

            }
            rs.close(); //
        }
        return total;

    }


    /**
     * PARA MMIA PERSONALIZADO
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesManterP(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT "
                + " dispensa_packege.patientid"
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, "
                + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND prescription.ptv=\'F\' AND prescription.tb=\'F\'  AND (prescription.reasonforupdate=\'Manter\' OR prescription.reasonforupdate=\'Transfer de\')"
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + " packagedruginfotmp.dispensedate"
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes na manutencao de ARV num periodo
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesAlterar(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.reasonforupdate='Alterar' and (pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "') " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total = rs.getInt("nrPacientesTarv");

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * PARA MMIA PERSONALIZADO
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesAlterarP(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT "
                + " dispensa_packege.patientid"
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, "
                + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND  prescription.ptv=\'F\' AND prescription.tb=\'F\'  "
                + " AND  "
                + " prescription.reasonforupdate=\'Alterar\'  "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + "packagedruginfotmp.dispensedate "
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes PPE
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesPPE(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.ppe='T' and pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                total = rs.getInt("nrPacientesTarv");
            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes PrEP
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesPrEP(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.prep='T' and pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                total = rs.getInt("nrPacientesTarv");
            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes CE
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesCriancasExpostas(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.ce='T' and pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                total = rs.getInt("nrPacientesTarv");
            }
            rs.close(); //
        }
        return total;

    }


    /**
     * Total de pacientes PPE
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientes_PTV(Date startDate, Date endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = "SELECT count(*) nrPacientesTarv " +
                "FROM ( " +
                "SELECT  distinct visit.patient " +
                "FROM (select max(pa.pickupdate) pickupdate, pat.id, pa.prescription " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "where pre.ptv='T' and pg_catalog.date(pa.pickupdate) >= '" + startDate + "'" +
                "and pg_catalog.date(pa.pickupdate) <= '" + endDate + "' " +
                "GROUP BY 2,3) pack " +
                "INNER JOIN (SELECT MAX (startdate),patient, episode.startreason " +
                "			 from episode WHERE stopdate is null  " +
                "			 GROUP BY 2,3" +
                ") visit on visit.patient = pack.id " +
                "WHERE visit.startreason not like '%nsito%' and visit.startreason not like '%aternidade%'" +
                ") as pacienteTarv";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                total++;
            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes PTV iNICIO
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesPTVInicio(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT " + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, " + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND prescription.ptv=\'T\'  "
                + " AND  "
                + " prescription.reasonforupdate=\'Inicia\'  "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + " packagedruginfotmp.dispensedate"
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes PTV Manter
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesPTVManter(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT " + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, " + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND prescription.ptv=\'T\'  "
                + " AND  "
                + " prescription.reasonforupdate=\'Manter\'  "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + " packagedruginfotmp.dispensedate "
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes TB Alterar
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesTbAlterar(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT " + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, " + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND prescription.tb=\'T\'  "
                + " AND  "
                + " prescription.reasonforupdate=\'Alterar\'  "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + " packagedruginfotmp.dispensedate"
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Devolve o regime anterior de uma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaRegime(int idpaciente, String tipoPaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " regimeterapeutico.regimeesquema "
                + "  FROM " + "  regimeterapeutico , " + "  prescription "
                + "  WHERE "
                + "  prescription.regimeid =regimeterapeutico.regimeid "
                + "  AND " + "  prescription.patient=" + idpaciente + "  AND prescription.tipoDoenca='"+tipoPaciente+ "' AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String regime = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                regime = rs.getString("regimeesquema");

            }
            rs.close(); //
        }

        return regime;

    }

    /**
     * Devolve ppe duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaPpe(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " ppe " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String ppe = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                ppe = rs.getString("ppe");

            }
            rs.close(); //
        }

        return ppe;

    }

    /**
     * Devolve a linha anterior duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaLinha(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " linhat.linhanome " + "  FROM "
                + "  linhat , " + "  prescription " + "  WHERE "
                + "  prescription.linhaid =linhat.linhaid " + "  AND "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String linha = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                linha = rs.getString("linhanome");

            }
            rs.close(); //
        }

        return linha;

    }

    public int carregaDispensaTrimestral(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + "  dispensatrimestral "
                + "  FROM "
                + "  prescription "
                + "  WHERE "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        // 0 = nao
        // 1 = sim
        int dispensaTrimestral = 0;
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                dispensaTrimestral = rs.getInt("dispensatrimestral");
            }
            rs.close(); //
        }

        return dispensaTrimestral;

    }

    public int carregaDispensaSemestral(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + "  dispensasemestral "
                + "  FROM "
                + "  prescription "
                + "  WHERE "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";
        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        // 0 = nao
        // 1 = sim
        int dispensaSemestral = 0;
        ResultSet rs = st.executeQuery(query);
        if (rs != null) {
            while (rs.next()) {
                dispensaSemestral = rs.getInt("dispensasemestral");
            }
            rs.close(); //
        }

        return dispensaSemestral;

    }


    /**
     * Devolve tb duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaTb(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " tb " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String tb = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                tb = rs.getString("tb");

            }
            rs.close(); //
        }

        return tb;

    }

    /**
     * Devolve tb duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaPrescricaoEspecial(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " prescricaoespecial " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String prescricaoespecial = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                prescricaoespecial = rs.getString("prescricaoespecial");

            }
            rs.close(); //
        }

        return prescricaoespecial;

    }

    public String carregaMotivoCriacaEspecial(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " motivocriacaoespecial " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String motivocriacaoespecial = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                motivocriacaoespecial = rs.getString("motivocriacaoespecial");

            }
            rs.close(); //
        }

        return motivocriacaoespecial;

    }

    public String carregaCcr(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " ccr "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String ccr = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                ccr = rs.getString("ccr");

            }
            rs.close(); //
        }

        return ccr;

    }

    public String carregaCpn(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " cpn "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String cpn = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                cpn = rs.getString("cpn");

            }
            rs.close(); //
        }

        return cpn;

    }

    public String carregaAf(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " af "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String af = "";
        ResultSet rs = st.executeQuery(query);

        if (af != null) {

            while (rs.next()) {

                af = rs.getString("af");

            }
            rs.close(); //
        }

        return af;

    }

    public String carregaFr(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " fr "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String fr = "";
        ResultSet rs = st.executeQuery(query);

        if (fr != null) {

            while (rs.next()) {

                fr = rs.getString("fr");

            }
            rs.close(); //
        }

        return fr;

    }

    public String carregaGaac(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " gaac "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String gaac = "";
        ResultSet rs = st.executeQuery(query);

        if (gaac != null) {

            while (rs.next()) {

                gaac = rs.getString("gaac");

            }
            rs.close(); //
        }

        return gaac;

    }

    public String carregaDc(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " dc "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String dc = "";
        ResultSet rs = st.executeQuery(query);

        if (dc != null) {

            while (rs.next()) {

                dc = rs.getString("dc");

            }
            rs.close(); //
        }

        return dc;

    }

    public String carregaCa(int idpaciente) throws ClassNotFoundException, SQLException {

        String query = " "
                + " SELECT "
                + " ca "
                + "  FROM "
                + "   "
                + "  prescription "
                + "  WHERE "
                + "   "
                + "  "
                + "  prescription.patient=" + idpaciente
                + "  AND "
                + "  prescription.current=\'T\'"
                + "";

        conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

        String ca = "";
        ResultSet rs = st.executeQuery(query);

        if (ca != null) {

            while (rs.next()) {

                ca = rs.getString("ca");

            }
            rs.close(); //
        }

        return ca;

    }

    /**
     * Devolve tb duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaSAAJ(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " saaj " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String saaj = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                saaj = rs.getString("saaj");

            }
            rs.close(); //
        }

        return saaj;

    }

    /**
     * Devolve se um ARV � pedi�trico ou adulto
     *
     * @param iddrug
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaPediatric(int iddrug) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " pediatric " + "  FROM " + "   "
                + "  drug " + "  WHERE " + "   " + "  " + "  drug.id=" + iddrug;

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String pediatric = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                pediatric = rs.getString("pediatric");

            }
            rs.close(); //
        }

        return pediatric;

    }

    /**
     * Devolve ptv duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaPtv(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " ptv " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String ptv = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                ptv = rs.getString("ptv");

            }
            rs.close(); //
        }

        return ptv;

    }

    /**
     * Devolve prep duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaPrEP(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " prep " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String prep = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                prep = rs.getString("prep");

            }
            rs.close(); //
        }

        return prep;

    }

    /**
     * Devolve ce duma prescricao
     *
     * @param idpaciente
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String carregaCE(int idpaciente) throws ClassNotFoundException,
            SQLException {

        String query = " " + " SELECT " + " ce " + "  FROM " + "   "
                + "  prescription " + "  WHERE " + "   " + "  "
                + "  prescription.patient=" + idpaciente + "  AND "
                + "  prescription.current=\'T\'" + "";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String ce = "";
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                ce = rs.getString("ce");

            }
            rs.close(); //
        }

        return ce;

    }

    /**
     * Total de Meses Dispensados
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws SQLException
     */
    public int mesesDispensados(String startDate, String endDate)
            throws SQLException {

        int meses = 0;
        double somaSemanas = 0;

        String query = " SELECT " + " weekssupply, packageid"
                + " FROM packagedruginfotmp " + "" + " WHERE "
                + "  packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'" + startDate + "\'::timestamp::date "
                + "AND packagedruginfotmp.dispensedate::timestamp::date <="
                + " \'" + endDate
                + "\'::timestamp::date GROUP BY packageid, weekssupply";

        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                somaSemanas += rs.getInt("weekssupply");

            }
            rs.close(); //

            meses = (int) Math.round(somaSemanas / 4);
        }

        return meses;
    }

    /**
     * Insere pacientes que nao estao ainda no SESP
     *
     * @param nid
     * @param nomes
     * @param apelido
     * @param dataderegisto
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void inserPacienteIdart(String nid, String nomes, String apelido,
                                   Date dataderegisto) throws ClassNotFoundException, SQLException {
        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        st.executeUpdate(""
                + " INSERT INTO registadosnoidart (nid, nomes, apelido, dataderegisto) "
                + "  VALUES( \'" + nid + "\',\'" + nomes + "\',\'" + apelido
                + "\',\'"
                + new SimpleDateFormat("yyyy-MM-dd").format(dataderegisto)
                + "\')");

    }

    /**
     * VE se o paciente foi dispensado ARV no periodo
     *
     * @param patientid
     * @return
     * @throws ClassNotFoundException
     */
    public boolean dispensadonoperiodo(String patientid)
            throws ClassNotFoundException {

        boolean foidispensado = false;
        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            ResultSet rs = st
                    .executeQuery(""
                            + " SELECT "
                            + "  patientid FROM  "
                            + "   packagedruginfotmp "
                            + "  WHERE "
                            + " to_timestamp(dateexpectedstring, \'DD Mon YYYY\')::DATE > now()::DATE "
                            + "  AND patientid = \'" + patientid + "" + "\'");

            if (rs != null) {
                while (rs.next()) {
                    foidispensado = true;
                }
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return foidispensado;

    }

    /**
     * Total de pacientes TB iNICIO
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesTbInicio(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT " + " dispensa_packege.patientid  "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, " + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND prescription.tb=\'T\'  "
                + " AND  "
                + " prescription.reasonforupdate=\'Inicia\'  "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + "packagedruginfotmp.dispensedate"
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes TB Manter
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesTbManter(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT " + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, " + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\' AND prescription.tb=\'T\'  "
                + " AND  "
                + "  prescription.reasonforupdate=\'Manter\'   "
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid ,"
                + "packagedruginfotmp.dispensedate"
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes PTV Alterar
     *
     * @param startDate
     * @param endDate
     * @return
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public int totalPacientesPTVAlterar(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT  DISTINCT"
                + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, "
                + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\'  AND prescription.ptv=\'T\' AND prescription.reasonforupdate=\'Alterar\'"
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + "packagedruginfotmp.dispensedate "
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;

    }

    /**
     * Total de pacientes pvt sem discriminar
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public int totalPacientesPTV(String startDate, String endDate)
            throws ClassNotFoundException, SQLException {
        int total = 0;

        String query = " SELECT DISTINCT " + " dispensa_packege.patientid "
                + "	FROM "
                + "	(SELECT  "
                + "	prescription.id, package.packageid "
                + " FROM "
                + " prescription, " + " 	package "
                + " WHERE "
                + " prescription.id = package.prescription "
                + " AND "
                + "  prescription.ppe=\'F\'  AND prescription.ptv=\'T\'"
                + " )as prescription_package, "
                + " ( " + " SELECT "
                + " packagedruginfotmp.patientid, "
                + " packagedruginfotmp.packageid,"
                + "  packagedruginfotmp.dispensedate"
                + " FROM " + " package, packagedruginfotmp "
                + " WHERE "
                + " package.packageid=packagedruginfotmp.packageid " + " AND "
                + "				 packagedruginfotmp.dispensedate::timestamp::date >= "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <= "
                + " \'"
                + endDate
                + "\'::timestamp::date"
                + " ) as dispensa_packege ,"
                + " ("
                + "     select packagedruginfotmp.patientid,  "
                + " 	  max(packagedruginfotmp.dispensedate) as lastdispense"
                + " 	 FROM "
                + " 	 package, packagedruginfotmp  "
                + "  	 WHERE  "
                + "	 package.packageid=packagedruginfotmp.packageid  "
                + "	 AND  "
                + "					 packagedruginfotmp.dispensedate::timestamp::date >=  "
                + "\'"
                + startDate
                + "\'::timestamp::date  AND  packagedruginfotmp.dispensedate::timestamp::date <=  "
                + " \'"
                + endDate
                + "\'::timestamp::date  "
                + "  group by packagedruginfotmp.patientid "
                + "     ) as ultimadatahora "
                + "	 WHERE  "
                + "	 dispensa_packege.packageid=prescription_package.packageid  "
                + "	  and  "
                + "  dispensa_packege.dispensedate=ultimadatahora.lastdispense";

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        ResultSet rs = st.executeQuery(query);
        if (rs != null) {

            while (rs.next()) {

                total++;

            }
            rs.close(); //
        }
        return total;
    }

    public String getLivroRegistoDiario(boolean i, boolean m, boolean a, boolean t, boolean r, String startDate, String endDate, String diseaseType) {

        Vector<String> v = new Vector<String>();

        if (i)
            v.add("Inicia");
        if (m)
            v.add("Manter");
        if (a)
            v.add("Alterar");
        if (t)
            v.add("Transfer de");
        if (r)
            v.add("Reiniciar");


        String condicao = "(\'";

        if (v.size() > 1) {
            for (int j = 0; j < v.size() - 1; j++) {
                condicao += v.get(j) + "\' , \'";
            }

            condicao += v.get(v.size() - 1) + "\')";
        }

        if (v.size() == 1) {

            condicao += v.get(0) + "\')";
        }

        String query = " SELECT  distinct p.patient, "
                + " pat.patientid as nid, "
                + " pat.firstnames as nome, "
                + " pat.lastname as apelido,  "
                + " p.reasonforupdate as tipotarv, "
                + " reg.regimeesquema as regime,  "
                + " CASE  "
                + " 	WHEN p.dispensatrimestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DT' "
                + "                ELSE 'DT - TRANSPORTE' "
                + "           END "
                + " 	WHEN p.dispensasemestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DS' "
                + "                ELSE 'DS - TRANSPORTE' "
                + "           END "
                + " 	ELSE 'DM' "
                + " END AS tipodispensa, "
                + " pa.pickupdate::date as datalevantamento, "
                + " to_date(pack.dateexpectedstring, 'DD-Mon-YYYY') as dataproximolevantamento,  "
                + " CASE WHEN p.prep = 'T' THEN 'Sim' ELSE 'Nao' END AS prep, "
                + " CASE WHEN p.ppe = 'T' THEN 'Sim' ELSE 'Nao' END AS ppe, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) BETWEEN 0 AND 4 THEN 'Sim' ELSE 'Nao' END AS ZeroQuatro, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) BETWEEN 5 AND 9 THEN 'Sim' ELSE 'Nao' END AS CincoNove, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) BETWEEN 10 AND 14 THEN 'Sim' ELSE 'Nao' END AS DezCatorze, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) >= 15 THEN 'Sim' ELSE 'Nao' END AS Maior15, "
                + " l.linhanome, "
                + " pack.packid as packid, "
                + " ep.startreason  "
                + " FROM  ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pdit.dateexpectedstring) dateexpectedstring, max(pa.id) packid, "
                + " 			pat.id, max(visit.id) episode"
                + "	from package pa "
                + "	inner join packageddrugs pds on pds.parentpackage = pa.id "
                + "	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                + "	inner join prescription pre on pre.id = pa.prescription  "
                + "	inner join patient pat ON pre.patient=pat.id  "
                + "	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + "				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + "				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + "	where pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	 AND pre.tipodoenca = '" + diseaseType + "'   ))   "
                + "	GROUP BY 5 order by 5) pack  "
                + "	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + "	inner join patient pat on pat.id = pack.id  "
                + "	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate  "
                + "	inner join linhat l on l.linhaid = p.linhaid "
                + "	inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " inner join episode ep on ep.id = pack.episode "
                + "	where p.reasonforupdate IN " + condicao + " AND p.tipodoenca like '%" + diseaseType + "%' ";

        return query;
    }

    public String getQueryHistoricoLevantamentos(boolean i, boolean m,
                                                 boolean a, boolean t, boolean r, String startDate, String endDate, String diseaseType) {

        Vector<String> v = new Vector<String>();

        if (i)
            v.add("Inicia");
        if (m)
            v.add("Manter");
        if (a)
            v.add("Alterar");
        if (t)
            v.add("Transfer de");
        if (r)
            v.add("Reiniciar");

        String condicao = "(\'";

        if (v.size() > 1) {
            for (int j = 0; j < v.size() - 1; j++) {
                condicao += v.get(j) + "\' , \'";
            }

            condicao += v.get(v.size() - 1) + "\')";
        }

        if (v.size() == 1) {

            condicao += v.get(0) + "\')";
        }

        String query = " SELECT  distinct p.patient, "
                + " pat.patientid as nid, "
                + " pat.firstnames as nome, "
                + " pat.lastname as apelido,  "
                + " p.reasonforupdate as tipotarv, "
                + " reg.regimeesquema as regime,  "
                + " CASE  "
                + " 	WHEN p.dispensatrimestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DT' "
                + "                ELSE 'DT - TRANSPORTE' "
                + "           END "
                + " 	WHEN p.dispensasemestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DS' "
                + "                ELSE 'DS - TRANSPORTE' "
                + "           END "
                + " 	ELSE 'DM' "
                + " END AS tipodispensa, "
                + " pa.pickupdate::date as datalevantamento, "
                + " to_date(pack.dateexpectedstring, 'DD-Mon-YYYY') as dataproximolevantamento,  "
                + " ep.startreason  "
                + " FROM  ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pdit.dateexpectedstring) dateexpectedstring, max(pa.id) packid, "
                + " 			pat.id, max(visit.id) episode "
                + "	from package pa "
                + "	inner join packageddrugs pds on pds.parentpackage = pa.id "
                + "	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                + "	inner join prescription pre on pre.id = pa.prescription  "
                + "	inner join patient pat ON pre.patient=pat.id  "
                + "	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + "				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + "				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + "	where pre.tipodoenca like '%" + diseaseType + "%'  and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + "	GROUP BY 5 order by 5) pack  "
                + "	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + "	inner join patient pat on pat.id = pack.id  "
                + "	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate  "
                + "	inner join linhat l on l.linhaid = p.linhaid "
                + "	inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " inner join episode ep on ep.id = pack.episode "
                + "	where p.tipodoenca like '%" + diseaseType + "%' and p.reasonforupdate IN " + condicao + " ";

        return query;
    }


    /**
     * @param i
     * @param m
     * @param a
     * @param startDate
     * @param endDate
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public List<HistoricoLevantamentoXLS> getQueryHistoricoLevantamentosXLS(boolean i, boolean m, boolean a, boolean t, boolean r, String startDate, String endDate, String diseaseType) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        Vector<String> v = new Vector<String>();

        if (i)
            v.add("Inicia");
        if (m)
            v.add("Manter");
        if (a)
            v.add("Alterar");
        if (t)
            v.add("Transfer de");
        if (r)
            v.add("Reiniciar");

        String condicao = "(\'";

        if (v.size() > 1) {
            for (int j = 0; j < v.size() - 1; j++) {
                condicao += v.get(j) + "\' , \'";
            }

            condicao += v.get(v.size() - 1) + "\')";
        }

        if (v.size() == 1) {

            condicao += v.get(0) + "\')";
        }

        String query = " SELECT  distinct p.patient, "
                + " pat.patientid as nid, "
                + " pat.firstnames as nome, "
                + " pat.lastname as apelido,  "
                + " p.reasonforupdate as tipotarv, "
                + " reg.regimeesquema as regime,  "
                + " CASE  "
                + " 	WHEN p.dispensatrimestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DT' "
                + "                ELSE 'DT - TRANSPORTE' "
                + "           END "
                + " 	WHEN p.dispensasemestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DS' "
                + "                ELSE 'DS - TRANSPORTE' "
                + "           END "
                + " 	ELSE 'DM' "
                + " END AS tipodispensa, "
                + " pa.pickupdate::date as datalevantamento, "
                + " to_date(pack.dateexpectedstring, 'DD-Mon-YYYY') as dataproximolevantamento,  "
                + " ep.startreason  "
                + " FROM  ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pdit.dateexpectedstring) dateexpectedstring, max(pa.id) packid, "
                + " 			pat.id , max(visit.id) episode "
                + "	from package pa "
                + "	inner join packageddrugs pds on pds.parentpackage = pa.id "
                + "	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                + "	inner join prescription pre on pre.id = pa.prescription  "
                + "	inner join patient pat ON pre.patient=pat.id  "
                + "	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + "				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + "				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + "	where pre.tipodoenca like '%" + diseaseType + "%' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + "	GROUP BY 5 order by 5) pack  "
                + "	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + "	inner join patient pat on pat.id = pack.id  "
                + "	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate  "
                + "	inner join linhat l on l.linhaid = p.linhaid "
                + "	inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " inner join episode ep on ep.id = pack.episode "
                + "	where p.tipodoenca like '%" + diseaseType + "%' and p.reasonforupdate IN " + condicao + " ";

        List<HistoricoLevantamentoXLS> levantamentoXLSs = new ArrayList<HistoricoLevantamentoXLS>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {
                HistoricoLevantamentoXLS levantamentoXLS = new HistoricoLevantamentoXLS();
                levantamentoXLS.setPatientIdentifier(rs.getString("nid"));
                levantamentoXLS.setNome(rs.getString("nome"));
                levantamentoXLS.setApelido(rs.getString("apelido"));
                levantamentoXLS.setTipoTarv(rs.getString("tipotarv"));
                levantamentoXLS.setTipoPaciente(rs.getString("startreason"));
                levantamentoXLS.setRegimeTerapeutico(rs.getString("regime"));
                levantamentoXLS.setTipoDispensa(rs.getString("tipodispensa"));
                levantamentoXLS.setDataLevantamento(rs.getString("datalevantamento"));
                levantamentoXLS.setDataProximoLevantamento(rs.getString("dataproximolevantamento"));

                levantamentoXLSs.add(levantamentoXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return levantamentoXLSs;

    }

    public List<HistoricoLevantamentoXLS> getReferralHistoricoLevantamentosXLS(String startDate, String endDate) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select distinct patientid as nid, " +
                "patientfirstname ||' '|| patientlastname as nome, " +
                "reasonforupdate as tipoPaciente, " +
                "regimenome as regimeTerapeutico, " +
                "CASE " +
                "WHEN dispensatrimestral = 1 THEN 'DT' " +
                "WHEN dispensasemestral = 1 THEN 'DS' " +
                "ELSE 'DM' " +
                "        END AS tipodispensa, " +
                "pg_catalog.date(pickupdate) as dataLevantamento, " +
                "to_date(dateexpectedstring, 'DD-Mon-YYYY') as dataProximoLev, " +
                "mainclinicname as referencia " +
                "from sync_temp_dispense " +
                "where pg_catalog.date(pickupdate) >= '" + startDate + "'::date " +
                "AND pg_catalog.date(pickupdate) < ('" + endDate + "'::date + INTERVAL '1 day') " +
                "GROUP BY 1,2,3,4,5,6,7,8 " +
                "order by 6";

        List<HistoricoLevantamentoXLS> levantamentoXLSs = new ArrayList<HistoricoLevantamentoXLS>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {
                HistoricoLevantamentoXLS levantamentoXLS = new HistoricoLevantamentoXLS();
                levantamentoXLS.setPatientIdentifier(rs.getString("nid"));
                levantamentoXLS.setNome(rs.getString("nome"));
                levantamentoXLS.setApelido(rs.getString("apelido"));
                levantamentoXLS.setTipoTarv(rs.getString("tipotarv"));
                levantamentoXLS.setRegimeTerapeutico(rs.getString("regime"));
                levantamentoXLS.setTipoDispensa(rs.getString("tipodispensa"));
                levantamentoXLS.setDataLevantamento(rs.getString("datalevantamento"));
                levantamentoXLS.setDataProximoLevantamento(rs.getString("dataproximolevantamento"));
                levantamentoXLS.setClinic(rs.getString("referencia"));

                levantamentoXLSs.add(levantamentoXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return levantamentoXLSs;

    }

    /**
     * @param i
     * @param m
     * @param a
     * @param startDate
     * @param endDate
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public List<LivroRegistoDiarioXLS> getLivroRegistoDiarioXLS(boolean i, boolean m, boolean a, boolean t, boolean r, String startDate, String endDate, String diseaseType) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        List<LivroRegistoDiarioXLS> diarioXLS;

        Vector<String> v = new Vector<String>();

        if (i)
            v.add("Inicia");
        if (m)
            v.add("Manter");
        if (a)
            v.add("Alterar");
        if (t)
            v.add("Transfer de");
        if (r)
            v.add("Reiniciar");

        String condicao = "(\'";

        if (v.size() > 1) {
            for (int j = 0; j < v.size() - 1; j++) {
                condicao += v.get(j) + "\' , \'";
            }

            condicao += v.get(v.size() - 1) + "\')";
        }

        if (v.size() == 1) {

            condicao += v.get(0) + "\')";
        }

        String query = " SELECT  distinct p.patient, "
                + " pat.patientid as nid, "
                + " pat.firstnames as nome, "
                + " pat.lastname as apelido,  "
                + " p.reasonforupdate as tipotarv, "
                + " reg.regimeesquema as regime,  "
                + " CASE  "
                + " 	WHEN p.dispensatrimestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DT' "
                + "                ELSE 'DT - TRANSPORTE' "
                + "           END "
                + " 	WHEN p.dispensasemestral = 1 THEN "
                + "           CASE WHEN  pack.pickupdate >= '" + startDate + "' THEN 'DS' "
                + "                ELSE 'DS - TRANSPORTE' "
                + "           END "
                + " 	ELSE 'DM' "
                + " END AS tipodispensa, "
                + " pa.pickupdate::date as datalevantamento, "
                + " to_date(pack.dateexpectedstring, 'DD-Mon-YYYY') as dataproximolevantamento,  "
                + " CASE WHEN p.prep = 'T' THEN 'Sim' ELSE 'Nao' END AS prep, "
                + " CASE WHEN p.ppe = 'T' THEN 'Sim' ELSE 'Nao' END AS ppe, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) BETWEEN 0 AND 4 THEN 'Sim' ELSE 'Nao' END AS ZeroQuatro, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) BETWEEN 5 AND 9 THEN 'Sim' ELSE 'Nao' END AS CincoNove, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) BETWEEN 10 AND 14 THEN 'Sim' ELSE 'Nao' END AS DezCatorze, "
                + " CASE WHEN EXTRACT(year FROM age('" + endDate + "',pat.dateofbirth)) >= 15 THEN 'Sim' ELSE 'Nao' END AS Maior15, "
                + " l.linhanome, "
                + " pack.packid as packid,"
                + " drug_set.name, "
                + " drug_set.amount, "
                + " ep.startreason  "
                + " FROM  ( "
                + " 	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pdit.dateexpectedstring) dateexpectedstring, max(pa.id) packid, "
                + " 			pat.id, max(visit.id) episode "
                + "	from package pa "
                + "	inner join packageddrugs pds on pds.parentpackage = pa.id "
                + "	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                + "	inner join prescription pre on pre.id = pa.prescription  "
                + "	inner join patient pat ON pre.patient=pat.id  "
                + "	INNER JOIN (SELECT MAX (startdate), patient, id  "
                + "				from episode WHERE stopdate is null and startdate <= '" + endDate + "' "
                + "				GROUP BY 2,3) visit on visit.patient = pat.id  "
                + "	where pre.tipodoenca like '%" + diseaseType + "%' and pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + startDate + "' and pg_catalog.date(pa.pickupdate) <= '" + endDate + "')  "
                + "	OR (pg_catalog.date(pa.pickupdate) < '" + startDate + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + endDate + "'  "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + startDate + "' "
                + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + endDate + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + endDate + "' "
                + "	   ))   "
                + "	GROUP BY 5 order by 5) pack  "
                + "	inner join prescription p on p.date = pack.predate and p.patient=pack.id  "
                + "	inner join patient pat on pat.id = pack.id  "
                + "	inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate  "
                + "	inner join linhat l on l.linhaid = p.linhaid "
                + "	inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                + " inner join episode ep on ep.id = pack.episode "
                + "  LEFT JOIN ("
                + "      select drug.name, sum(packdrug.amount) as amount, pack.id as drugid"
                + "      from packageddrugs as packdrug, stock, drug, prescribeddrugs as predrug,"
                + "      package as pack,"
                + "      prescription as pre"
                + "      where pre.tipodoenca like '%" + diseaseType + "%' and packdrug.stock = stock.id"
                + "      and stock.drug = drug.id"
                + "      and packdrug.parentPackage = pack.id"
                + "      and pack.prescription = pre.id"
                + "      and predrug.prescription = pre.id"
                + "      and predrug.drug = drug.id"
                + "      group by drug.name,pack.id) drug_set ON pack.packid = drug_set.drugid"
                + "	where p.tipodoenca like '%" + diseaseType + "%' and p.reasonforupdate IN " + condicao + " ";

        diarioXLS = new ArrayList<LivroRegistoDiarioXLS>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {
                LivroRegistoDiarioXLS registoDiarioXLS = new LivroRegistoDiarioXLS();
                registoDiarioXLS.setPatientIdentifier(rs.getString("nid"));
                registoDiarioXLS.setNome(rs.getString("nome"));
                registoDiarioXLS.setApelido(rs.getString("apelido"));
                registoDiarioXLS.setTipoPaciente(rs.getString("startreason"));
                registoDiarioXLS.setZeroQuatro(rs.getString("zeroquatro"));
                registoDiarioXLS.setCincoNove(rs.getString("cinconove"));
                registoDiarioXLS.setDezCatorze(rs.getString("dezcatorze"));
                registoDiarioXLS.setMaiorQuinze(rs.getString("maior15"));
                registoDiarioXLS.setTipoTarv(rs.getString("tipotarv"));
                registoDiarioXLS.setRegimeTerapeutico(rs.getString("regime"));
                registoDiarioXLS.setTipoDispensa(rs.getString("tipodispensa"));
                registoDiarioXLS.setLinha(rs.getString("linhanome"));
                registoDiarioXLS.setDataLevantamento(rs.getString("datalevantamento"));
                registoDiarioXLS.setDataProximoLevantamento(rs.getString("dataproximolevantamento"));
                registoDiarioXLS.setPpe(rs.getString("ppe"));
                registoDiarioXLS.setPrep(rs.getString("prep"));
                registoDiarioXLS.setProdutos(rs.getString("name"));
                registoDiarioXLS.setQuantidade(rs.getString("amount"));

                diarioXLS.add(registoDiarioXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return diarioXLS;
    }


    public String getQueryPrescricoeSemDispensas(String startDate, String endDate) {

        String query = "SELECT pa.patientid nid, pa.firstnames firstname, pa.lastname lastname,pa.uuidopenmrs uuid,pr.date dataprescricao \r  " +
                " FROM prescription pr\r  " +
                " INNER JOIN patient pa ON pa.id=pr.patient\r  " +
                " WHERE pr.id NOT IN (\r  " +
                " SELECT prescription FROM package\r  " +
                ")\r  " +
                " AND pr.date::timestamp::date >= '" + startDate + "'::timestamp::date\r  " +
                " AND pr.date::timestamp::date <= '" + endDate + "'::timestamp::date\r  " +
                " AND pr.current='T';";

        return query;
    }

    public List<PrescricaoSemFilaXLS> getQueryPrescricoeSemDispensasXLS(String startDate, String endDate) {

        List<PrescricaoSemFilaXLS> prescricaoSemFilaXLSs = new ArrayList<PrescricaoSemFilaXLS>();

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "SELECT pa.patientid nid, pa.firstnames firstname, pa.lastname lastname,pa.uuidopenmrs uuid,pr.date dataprescricao \r  " +
                    " FROM prescription pr\r  " +
                    " INNER JOIN patient pa ON pa.id=pr.patient\r  " +
                    " WHERE pr.id NOT IN (\r  " +
                    " SELECT prescription FROM package\r  " +
                    ")\r  " +
                    " AND pr.date::timestamp::date >= '" + startDate + "'::timestamp::date\r  " +
                    " AND pr.date::timestamp::date <= '" + endDate + "'::timestamp::date\r  " +
                    " AND pr.current='T';";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    PrescricaoSemFilaXLS prescricaoSemFilaXLS = new PrescricaoSemFilaXLS();
                    prescricaoSemFilaXLS.setPatientIdentifier(rs.getString("nid"));
                    prescricaoSemFilaXLS.setNome(rs.getString("firstname"));
                    prescricaoSemFilaXLS.setApelido(rs.getString("lastname"));
                    prescricaoSemFilaXLS.setUuidOpenmrs(rs.getString("uuid"));
                    prescricaoSemFilaXLS.setDataPrescricao(rs.getString("dataprescricao"));

                    prescricaoSemFilaXLSs.add(prescricaoSemFilaXLS);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


        return prescricaoSemFilaXLSs;
    }

    public void insere_sync_temp_dispense() {

        delete_sync_temp_dispense();
        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        /*		if (data != null) {
         try {

         while (data.next()) {

         st.executeUpdate(" INSERT INTO sync_temp_dispense(nid,ultimo_levantamento) values (\'"
         + data.getString("nid")
         + "\',\'"
         + data.getString("ultimo_lev") + "\')");

         }
         st.close();
         } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         }
         } else
        log.trace("NULL NULL NULL NULL");*/
    }

    public int total_rows() {

        ResultSet data = null;
        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            data = st.executeQuery("SELECT  *   FROM  sync_view_dispense ");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        int rows = 0;
        try {
            while (data.next()) {
                rows++;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            data.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return rows;
    }

    public void delete_sync_temp_dispense() {

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            st.execute("DELETE FROM sync_temp_dispense");
            st.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Vector<SyncLinha> sync_table_dispense() {

        insere_sync_temp_dispense();
        Vector<SyncLinha> linha = new Vector<SyncLinha>();
        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            String query = "SELECT "
                    + " sync_view_dispense.nid as a, "
                    + "  sync_view_dispense.ultimo_lev as b,  "
                    + "   sync_view_dispense.tipo_tarv as c, "
                    + "  sync_view_dispense.regime as d, "
                    + "   sync_view_dispense.linha as e, "
                    + "  "
                    + " sync_view_dispense.ultimo_sesp as f, to_date(tabela.proximolev, 'DD Mon YYYY')  as g  "
                    + ""
                    + " FROM  "
                    + "   sync_view_dispense,"
                    + ""
                    + "(select patientid, max (packagedruginfotmp.dateexpectedstring) proximolev from packagedruginfotmp "
                    + "" + "" + "GROUP BY patientid ) as tabela  WHERE  "
                    + " sync_view_dispense.nid= tabela.patientid";

            ResultSet linhas = st.executeQuery(query);

            //log.trace(" Query: "+query );
            while (linhas.next()) {

                SyncLinha synclinha = new SyncLinha(linhas.getString("a"),
                        linhas.getString("b"), linhas.getString("c"),
                        linhas.getString("d"), linhas.getString("e"),
                        linhas.getString("f"), linhas.getString("g"));

                log.trace(linhas.getString("a") + " "
                        + linhas.getString("b") + " " + linhas.getString("c")
                        + " " + linhas.getString("d") + " "
                        + linhas.getString("e") + " " + linhas.getString("f"));

                linha.add(synclinha);

            }

        } catch (SQLException e) {

        }

        log.trace(" Vector size " + linha.size());

        return linha;

    }

    public Vector<SyncLinhaPatients> sync_table_patients() {

        Vector<SyncLinhaPatients> linha = new Vector<SyncLinhaPatients>();
        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {

            ResultSet linhas = st.executeQuery("SELECT "
                    + " sync_view_patients.nid as a, "
                    + "  sync_view_patients.datanasc  as b, "
                    + " sync_view_patients.pnomes as c, "
                    + " sync_view_patients.unome as d, "
                    + "  sync_view_patients.sexo as e, "
                    + "  sync_view_patients.dataabertura as f " + "  FROM "
                    + " sync_view_patients ");

            while (linhas.next()) {

                SyncLinhaPatients synclinha = new SyncLinhaPatients(
                        linhas.getString("a"), linhas.getString("b"),
                        linhas.getString("c"), linhas.getString("d"),
                        linhas.getString("e"), linhas.getString("f"));

                /*
                 *log.trace
                 * (linhas.getString("a")+" "+linhas.getString("b") +" "+
                 * linhas.getString("c") +" "+linhas.getString("d")+" "+
                 * linhas.getString("e")+" "+ linhas.getString("f"));
                 */
                linha.add(synclinha);

            }

        } catch (SQLException e) {

        }

        return linha;
    }

    public void delete_sync_temp_patients() {
        // TODO Auto-generated method stub
        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            st.execute("DELETE FROM sync_temp_patients");
            st.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void insere_sync_temp_patients() {

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        //ConexaoODBC conn = new ConexaoODBC();
        //ResultSet data = conn.result_for_sync_patients();

        /*		if (data != null)
         try {

         while (data.next()) {

         String datanasc = data.getString("datanasc");
         String dataabertura = data.getString("dataabertura");
         String unomes = data.getString("apelido");

         if (unomes == null || contemInterrogacao(unomes))
         unomes = "  ";
         if (datanasc == null)
         datanasc = new SimpleDateFormat("yyyy-MM-dd")
         .format(new Date());
         if (dataabertura == null)
         dataabertura = new SimpleDateFormat("yyyy-MM-dd")
         .format(new Date());

         String query = " INSERT INTO sync_temp_patients(nid,datanasc,pnomes, unomes, sexo, dataabertura) values (\'"
         + data.getString("nid")
         + "\',"
         + "\'"
         + datanasc
         + "\',"
         + "\'"
         + data.getString("nome")
         + "\',"
         + "\'"
         + unomes
         + "\',"
         + "\'"
         + data.getString("sexo")
         + "\',"
         + "\'"
         + dataabertura + "\')" + "";
        log.trace(query);

         st.executeUpdate(query);

         }
         st.close();
         } catch (SQLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
         }*/
    }

    private boolean contemInterrogacao(String unomes) {
        boolean contem = false;

        if (unomes != null) {

            for (int i = 0; i < unomes.length(); i++) {
                if (unomes.charAt(i) == '?') {
                    contem = true;
                    break;
                }
            }
        }
        return contem;

    }

    public void syncdata_patients(SyncLinhaPatients syncLinhaPatients) {

        String sexo = syncLinhaPatients.getSexo();
        if (sexo.trim().equals("null")) {
            sexo = "U";
        }
        String apelido = syncLinhaPatients.getUnomes();

        if (apelido.trim().equals("null")) {
            apelido = " ";
        }

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            int id = nextval_hibernate_sequence();

            st.executeUpdate(""
                    + "INSERT INTO "
                    + " patient "
                    + " (id, accountstatus, dateofbirth, clinic, firstnames, lastname, modified, patientid, province , sex) "
                    + " VALUES ( " + "" + id + "," + " \'t\'," + "\'"
                    + syncLinhaPatients.getDatanasc() + "\'," + "2," + "\'"
                    + syncLinhaPatients.getPnomes() + "\'," + "\'" + apelido
                    + "\'," + "\'T\'," + "\'" + syncLinhaPatients.getNid()
                    + "\'," + "\'Select a Province\'," + "\'"
                    + sexo.trim().charAt(0) + "\')");

            st.executeUpdate("" + "INSERT INTO episode " + "(id, "
                    + "startdate, " + "startreason, " + "patient, " + "index, "
                    + "clinic" + ") " + "VALUES " + "(" + ""
                    + nextval_hibernate_sequence() + "," + "\'"
                    + syncLinhaPatients.getDataabertura() + "\',"
                    + "\'Novo Paciente\'," + "" + id + "," + "0,2)");

            st.executeUpdate(" INSERT INTO " + " patientidentifier " + "("
                    + "id, " + "value, " + "patient_id," + "type_id" + ") "
                    + "VALUES " + "" + "(" + "" + nextval_hibernate_sequence()
                    + "," + "\'" + syncLinhaPatients.getNid() + "\'," + "" + id
                    + ", 0)");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();

        }

    }

    private int nextval_hibernate_sequence() {
        int id = 0;

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        ResultSet rsId = null;
        try {
            rsId = st
                    .executeQuery("SELECT nextval(\'hibernate_sequence\') as id");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (rsId != null) {
            try {
                while (rsId.next()) {
                    id = rsId.getInt("id");
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return id;
    }

    public boolean jaTemFilaInicio(String nid, String tipoPaciente) {

        boolean jatemFilaInicio = false;

        String query = " SELECT  "
                + " prescription.id, "
                + " package.packageid ,"
                + " prescription.reasonforupdate as tipotarv, "
                + " prescription.current as currentprescription, "
                + " patient.patientid "
                + " FROM  "
                + " prescription  "
                + " inner join package on prescription.id = package.prescription "
                + " inner join patient on patient.id = prescription.patient"
                + " WHERE   "
                + " prescription.ppe=\'F\' "
                + " AND  prescription.tipodoenca = '"+tipoPaciente+"' AND "
                + " prescription.reasonforupdate like 'Inici%' "
                + " AND patient.patientid = \'" + nid + "\'";
        try {
            conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            ResultSet rs = st.executeQuery(query);

            while (rs.next()) {
                if(tipoPaciente.equalsIgnoreCase(iDartProperties.PNCT)){
                    if(rs.getString("currentprescription").equals("T"))
                        jatemFilaInicio = true;
                    break;
                }else{
                    if (rs.getString("patientid").equals(nid)) {
                        jatemFilaInicio = true;
                        break;
                    }
                }
                log.trace("/*/*//*///*/*//*/*/" + rs.getString("nid"));
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return jatemFilaInicio;

    }

    public List<SecondLinePatients> getSecondLinePatients(String dataInicio, String dataFim) {

        List<SecondLinePatients> secondLinePatientsXLS = new ArrayList<SecondLinePatients>();

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select distinct pat.id as id, "
                    + "pat.patientId as nid, "
                    + "pat.dateOfBirth AS dob, "
                    + "pat.firstnames || ' ' || pat.lastname as nome, "
                    + "c.clinicName as clinic, "
                    + "pat.cellphone as cellno, "
                    + "EXTRACT(year FROM age('" + dataFim + "',pat.dateofbirth))::Integer as idade, "
                    + "(pat.address1 ||' '||pat.address2||' '||pat.address3) as endereco, "
                    + "CASE WHEN (pat.sex = 'F' OR pat.sex = 'f')  THEN 'Feminino' "
                    + "WHEN (pat.sex = 'M' OR pat.sex = 'm') THEN 'Masculino' "
                    + "ELSE 'Outro' "
                    + "END as sex, "
                    + "reg.regimeesquema as esquematerapeutico, "
                    + "l.linhanome as linhaterapeutica, "
                    + "p.reasonforupdate as tipoPaciente "
                    + "FROM  ( "
                    + "	select max(pre.date) predate, max(pa.pickupdate) pickupdate, max(pdit.dateexpectedstring) dateexpectedstring, max(pa.id) packid, "
                    + "	pat.id,  max(visit.id) episode 	from package pa "
                    + "	inner join packageddrugs pds on pds.parentpackage = pa.id "
                    + "	inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id "
                    + "	inner join prescription pre on pre.id = pa.prescription "
                    + "	inner join patient pat ON pre.patient=pat.id "
                    + "	INNER JOIN (SELECT MAX (startdate), patient, id "
                    + "				from episode WHERE stopdate is null and startdate <= '" + dataFim + "' "
                    + "				GROUP BY 2,3) visit on visit.patient = pat.id "
                    + "	where pds.amount <> 0 and ((pg_catalog.date(pa.pickupdate) >= '" + dataInicio + "' and pg_catalog.date(pa.pickupdate) <= '" + dataFim + "') "
                    + "	OR (pg_catalog.date(pa.pickupdate) < '" + dataInicio + "' and pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY')) > '" + dataFim + "' "
                    + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + dataFim + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date >= '" + dataInicio + "' "
                    + "		and (pa.pickupdate + (INTERVAL '1 month'*(date_part('day', '" + dataFim + "'::timestamp - pa.pickupdate::timestamp)/30)::integer))::date <= '" + dataFim + "' "
                    + "	   )) "
                    + "	   GROUP BY 5 order by 5) pack "
                    + "	   inner join prescription p on p.date = pack.predate and p.patient=pack.id "
                    + "	   inner join patient pat on pat.id = pack.id "
                    + "	   inner join package pa on pa.prescription = p.id and pa.pickupdate = pack.pickupdate "
                    + "	   inner join linhat l on l.linhaid = p.linhaid "
                    + "	   inner join regimeterapeutico reg on reg.regimeid = p.regimeid "
                    + "	   inner join clinic c on c.id = pat.clinic "
                    + "	   inner join episode ep on ep.id = pack.episode "
                    + "	   where l.linhanome like '%2%' and (ep.startreason not like '%nsito%' and ep.startreason not like '%ternidade%') ";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    SecondLinePatients pacienteSegundaLinha = new SecondLinePatients();
                    pacienteSegundaLinha.setPatientIdentifier(rs.getString("nid"));
                    pacienteSegundaLinha.setNome(rs.getString("nome"));
                    pacienteSegundaLinha.setIdade(rs.getInt("idade"));
                    pacienteSegundaLinha.setTherapeuticScheme(rs.getString("esquematerapeutico"));
                    pacienteSegundaLinha.setLine(rs.getString("linhaterapeutica"));
                    pacienteSegundaLinha.setArtType(rs.getString("tipoPaciente"));

                    secondLinePatientsXLS.add(pacienteSegundaLinha);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return secondLinePatientsXLS;
    }

    public List<AbsenteeForSupportCall> getAbsenteeForSupportCallQuartelyDispensation(String minDays, String maxDays, String dataInicial, String dataFinal) {

        List<AbsenteeForSupportCall> absenteeForSupportCallsXLS = new ArrayList<AbsenteeForSupportCall>();

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select  " +
                    "pat.patientid as nid,  " +
                    "(pat.lastname||', '|| pat.firstnames) as nome,  " +
                    " pat.nextofkinname as supportername,  " +
                    "pat.nextofkinphone as supporterphone,  " +
                    "pat.cellphone as cellno,  " +
                    "date_part('year',age(pat.dateofbirth)) as idade,  " +
                    "app.appointmentDate::date as dateexpected,  " +
                    "('" + dataInicial + "'::date - app.appointmentDate::date)::integer as dayssinceexpected,  " +
                    "CASE  " +
                    "    WHEN (('" + dataInicial + "'::date - app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '60 days')  " +
                    "    ELSE  " +
                    "  CASE  " +
                    "      WHEN ((app.appointmentDate::date - app.visitdate::date) > 60) THEN (app.appointmentDate::date + INTERVAL '60 days')  " +
                    "              ELSE null  " +
                    "      END  " +
                    "END  " +
                    "  AS datelostfollowup,  " +
                    "  " +
                    "  CASE  " +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date  " +
                    "    ELSE null  " +
                    "  END  " +
                    "  AS datereturn,  " +
                    "max(app.appointmentDate) as ultimaData  " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt  " +
                    "where app.patient = pat.id  " +
                    "and idt.name = 'NID'  " +
                    "and pi.value = pat.patientid  " +
                    "and idt.id = pi.type_id  " +
                    "and app.appointmentDate is not null  " +
                    "and (app.visitDate is null)  " +
                    "and ('" + dataInicial + "'::date - app.appointmentDate::date) between " + Integer.parseInt(minDays) + " and " + Integer.parseInt(maxDays) + "  " +
                    "and exists (select prescription.id  " +
                    "from prescription  " +
                    "where prescription.patient = pat.id  " +
                    "and prescription.dispensatrimestral = 1  " +
                    "and (('" + dataInicial + "'::date between prescription.date and prescription.endDate)or(('" + dataInicial + "'::date > prescription.date)) and (prescription.endDate is null)))  " +
                    "and exists (select id from episode where episode.patient = pat.id  " +
                    "and (('" + dataInicial + "'::date between episode.startdate and episode.stopdate)or(('" + dataInicial + "'::date > episode.startdate)) and (episode.stopdate is null)))  " +
                    "group by 1,2,3,4,5,6,7,8,9,10  " +
                    "order by nid asc";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    AbsenteeForSupportCall absenteeForSupportCall = new AbsenteeForSupportCall();
                    absenteeForSupportCall.setPatientIdentifier(rs.getString("nid"));
                    absenteeForSupportCall.setNome(rs.getString("nome"));
                    absenteeForSupportCall.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    absenteeForSupportCall.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    absenteeForSupportCall.setDataRegressoUnidadeSanitaria(rs.getString("datereturn"));
                    absenteeForSupportCall.setContacto(rs.getString("cellno"));
//                    absenteeForSupportCall.setListaFaltososSemana(rs.getString("tipoPaciente"));

                    absenteeForSupportCallsXLS.add(absenteeForSupportCall);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return absenteeForSupportCallsXLS;

    }

    public List<AbsenteeForSupportCall> getAbsenteeForSupportCallHold(String minDays, String maxDays, String dataInicial, String dataFinal) {

        List<AbsenteeForSupportCall> absenteeForSupportCallsXLS = new ArrayList<AbsenteeForSupportCall>();

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select  " +
                    "pat.patientid as nid,  " +
                    "(pat.lastname||', '|| pat.firstnames) as nome,  " +
                    " pat.nextofkinname as supportername,  " +
                    "pat.nextofkinphone as supporterphone,  " +
                    "pat.cellphone as cellno,  " +
                    "date_part('year',age(pat.dateofbirth)) as idade,  " +
                    "app.appointmentDate::date as dateexpected,  " +
                    "('" + dataInicial + "'::date - app.appointmentDate::date)::integer as dayssinceexpected,  " +
                    "CASE  " +
                    "    WHEN (('" + dataInicial + "'::date - app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '60 days')  " +
                    "    ELSE  " +
                    "  CASE  " +
                    "      WHEN ((app.appointmentDate::date - app.visitdate::date) > 60) THEN (app.appointmentDate::date + INTERVAL '60 days')  " +
                    "              ELSE null  " +
                    "      END  " +
                    "END  " +
                    "  AS datelostfollowup,  " +
                    "  " +
                    "  CASE  " +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date  " +
                    "    ELSE null  " +
                    "  END  " +
                    "  AS datereturn,  " +
                    "max(app.appointmentDate) as ultimaData  " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt  " +
                    "where app.patient = pat.id  " +
                    "and idt.name = 'NID'  " +
                    "and pi.value = pat.patientid  " +
                    "and idt.id = pi.type_id  " +
                    "and app.appointmentDate is not null  " +
                    "and (app.visitDate is null)  " +
                    "and ('" + dataInicial + "'::date - app.appointmentDate::date) between " + Integer.parseInt(minDays) + " and " + Integer.parseInt(maxDays) + "  " +
                    "and exists (select prescription.id  " +
                    "from prescription  " +
                    "where prescription.patient = pat.id  " +
                    "and prescription.dispensatrimestral = 0  " +
                    "and prescription.reasonforupdate = 'Inicia'  " +
                    "and (('" + dataInicial + "'::date between prescription.date and prescription.endDate)or(('" + dataInicial + "'::date > prescription.date)) and (prescription.endDate is null)))  " +
                    "and exists (select id from episode where episode.patient = pat.id  " +
                    "and (('" + dataInicial + "'::date between episode.startdate and episode.stopdate)or(('" + dataInicial + "'::date > episode.startdate)) and (episode.stopdate is null)))  " +
                    "group by 1,2,3,4,5,6,7,8,9,10  " +
                    "order by nid asc";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    AbsenteeForSupportCall absenteeForSupportCall = new AbsenteeForSupportCall();
                    absenteeForSupportCall.setPatientIdentifier(rs.getString("nid"));
                    absenteeForSupportCall.setNome(rs.getString("nome"));
                    absenteeForSupportCall.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    absenteeForSupportCall.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    absenteeForSupportCall.setDataRegressoUnidadeSanitaria(rs.getString("datereturn"));
                    absenteeForSupportCall.setContacto(rs.getString("cellno"));
//                    absenteeForSupportCall.setListaFaltososSemana(rs.getString("tipoPaciente"));

                    absenteeForSupportCallsXLS.add(absenteeForSupportCall);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return absenteeForSupportCallsXLS;

    }


    public List<FollowupFaulty> lostToFollowupFaultyQuartelyLayOff(String minDays, String maxDays, String date, String clinicid) {

        List<FollowupFaulty> faultyQuartelyLayOffs = new ArrayList<FollowupFaulty>();

        try {

            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select pat.patientid as patID, " +
                    "(pat.firstnames||', '|| pat.lastname) as name, " +
                    "pat.nextofkinname as supportername, " +
                    "pat.nextofkinphone as supporterphone, " +
                    "pat.cellphone as cellno, " +
                    "pat.homephone as homeno, " +
                    "pat.workphone as workno, " +
                    "date_part('year',age(pat.dateofbirth))::Integer as age, " +
                    "app.appointmentDate::date as dateexpected, " +
                    "('" + date + "'::date-app.appointmentDate::date)::integer as dayssinceexpected, " +
                    "CASE " +
                    "WHEN (('" + date + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '61 days') " +
                    "ELSE " +
                    "CASE " +
                    "WHEN ((app.appointmentDate::date - app.visitdate::date) > 61) THEN (app.appointmentDate::date + INTERVAL '61 days') " +
                    "ELSE null " +
                    "END " +
                    "END " +
                    "AS datelostfollowup, " +
                    "CASE " +
                    "WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date " +
                    "ELSE null " +
                    "END " +
                    "AS datereturn, " +
                    "pat.address1 || " +
                    "case when ((pat.address2 is null)or(pat.address2 like ''))  then '' " +
                    "else ',' || pat.address2 " +
                    "end " +
                    "|| " +
                    "case when ((pat.address3 is null)or(pat.address3 like '')) then '' " +
                    "else ',' || pat.address3 " +
                    "end " +
                    "as address, " +
                    "max(app.appointmentDate) as ultimaData " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt, prescription as presc " +
                    "where app.patient = pat.id " +
                    "and presc.patient = pat.id " +
                    "and presc.current = 'T' " +
                    "and presc.dispensatrimestral = 1 " +
                    "and idt.name = 'NID' " +
                    "and pi.value = pat.patientid " +
                    "and idt.id = pi.type_id " +
                    "and '" + clinicid + "' = pat.clinic " +
                    "and app.appointmentDate is not null " +
                    "and (app.visitdate::date is null) " +
                    "and (app.appointmentDate::date < '" + date + "'::date and ('" + date + "'::date - app.appointmentDate::date) between '" + minDays + "' and '" + maxDays + "') " +
                    "and exists (select prescription.id " +
                    "from prescription " +
                    "where prescription.patient = pat.id " +
                    "and (('" + date + "' between prescription.date and prescription.endDate)or(('" + date + "' > prescription.date)) and (prescription.endDate is null))) " +
                    "and exists (select id from episode where episode.patient = pat.id " +
                    "and (('" + date + "' between episode.startdate and episode.stopdate)or(('" + date + "' > episode.startdate)) and (episode.stopdate is null))) " +
                    "group by 1,2,3,4,5,6,7,8,9,10,11,12,13 " +
                    "order by age asc";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    FollowupFaulty faultyQuartelyLayOff = new FollowupFaulty();
                    faultyQuartelyLayOff.setPatientIdentifier(rs.getString("patID"));
                    faultyQuartelyLayOff.setNome(rs.getString("name"));
                    faultyQuartelyLayOff.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    faultyQuartelyLayOff.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    faultyQuartelyLayOff.setDataRegressouUnidadeSanitaria(rs.getString("datereturn"));

                    faultyQuartelyLayOffs.add(faultyQuartelyLayOff);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return faultyQuartelyLayOffs;
    }


    public List<FollowupFaulty> lostToFollowupFaultySemiAnnual(String minDays, String maxDays, String date, String clinicid) {

        List<FollowupFaulty> lostToFollowupFaultySemiAnnuals = new ArrayList<FollowupFaulty>();

        try {

            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select pat.patientid as patID, " +
                    "(pat.firstnames||', '|| pat.lastname) as name, " +
                    "pat.nextofkinname as supportername, " +
                    "pat.nextofkinphone as supporterphone, " +
                    "pat.cellphone as cellno, " +
                    "pat.homephone as homeno, " +
                    "pat.workphone as workno, " +
                    "date_part('year',age(pat.dateofbirth))::Integer as age, " +
                    "app.appointmentDate::date as dateexpected, " +
                    "('" + date + "'::date-app.appointmentDate::date)::integer as dayssinceexpected, " +
                    "CASE " +
                    "WHEN (('" + date + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '61 days') " +
                    "ELSE " +
                    "CASE " +
                    "WHEN ((app.appointmentDate::date - app.visitdate::date) > 61) THEN (app.appointmentDate::date + INTERVAL '61 days') " +
                    "ELSE null " +
                    "END " +
                    "END " +
                    "AS datelostfollowup, " +
                    "CASE " +
                    "WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date " +
                    "ELSE null " +
                    "END " +
                    "AS datereturn, " +
                    "pat.address1 || " +
                    "case when ((pat.address2 is null)or(pat.address2 like ''))  then '' " +
                    "else ',' || pat.address2 " +
                    "end " +
                    "|| " +
                    "case when ((pat.address3 is null)or(pat.address3 like '')) then '' " +
                    "else ',' || pat.address3 " +
                    "end " +
                    "as address, " +
                    "max(app.appointmentDate) as ultimaData " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt, prescription as presc " +
                    "where app.patient = pat.id " +
                    "and presc.patient = pat.id " +
                    "and presc.current = 'T' " +
                    "and presc.dispensasemestral = 1 " +
                    "and idt.name = 'NID' " +
                    "and pi.value = pat.patientid " +
                    "and idt.id = pi.type_id " +
                    "and '" + clinicid + "' = pat.clinic " +
                    "and app.appointmentDate is not null " +
                    "and (app.visitdate::date is null) " +
                    "and (app.appointmentDate::date < '" + date + "'::date and ('" + date + "'::date - app.appointmentDate::date) between '" + minDays + "' and '" + maxDays + "') " +
                    "and exists (select prescription.id " +
                    "from prescription " +
                    "where prescription.patient = pat.id " +
                    "and (('" + date + "' between prescription.date and prescription.endDate)or(('" + date + "' > prescription.date)) and (prescription.endDate is null))) " +
                    "and exists (select id from episode where episode.patient = pat.id " +
                    "and (('" + date + "' between episode.startdate and episode.stopdate)or(('" + date + "' > episode.startdate)) and (episode.stopdate is null))) " +
                    "group by 1,2,3,4,5,6,7,8,9,10,11,12,13 " +
                    "order by age asc";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    FollowupFaulty lostToFollowupFaultySemiAnnual = new FollowupFaulty();
                    lostToFollowupFaultySemiAnnual.setPatientIdentifier(rs.getString("patID"));
                    lostToFollowupFaultySemiAnnual.setNome(rs.getString("name"));
                    lostToFollowupFaultySemiAnnual.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    lostToFollowupFaultySemiAnnual.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    lostToFollowupFaultySemiAnnual.setDataRegressouUnidadeSanitaria(rs.getString("datereturn"));

                    lostToFollowupFaultySemiAnnuals.add(lostToFollowupFaultySemiAnnual);

                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return lostToFollowupFaultySemiAnnuals;
    }

    /**
     * Actualiza a ultima prescricao para current T depois de remover a current
     *
     * @param patient
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void updateLastPrescriptionToTrue(Patient patient) throws ClassNotFoundException, SQLException {
        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        st.executeUpdate("update prescription set current = 'T' " +
                " where patient =  " + patient.getId() +
                " and date = (select max(date) " +
                "            from prescription " +
                "               where patient = " + patient.getId() + " ) ");

    }

    /**
     * @param clinicid
     * @param minimumDate
     * @param maximumDate
     * @param date
     */
    public List<RegistoChamadaTelefonicaXLS> getMissedAppointmentsReport(String minimumDate, String maximumDate, Date date, String clinicid) {

        List<RegistoChamadaTelefonicaXLS> chamadaTelefonicaXLS = new ArrayList<RegistoChamadaTelefonicaXLS>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String data = dateFormat.format(date);


        try {
            conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);


            String query = "select" +
                    " pat.patientid as patID," +
                    " (pat.firstnames||', '|| pat.lastname) as name," +
                    " pat.nextofkinname as supportername," +
                    " pat.nextofkinphone as supporterphone," +
                    " pat.cellphone as cellno," +
                    " pat.homephone as homeno," +
                    " pat.workphone as workno," +
                    " date_part('year',age(pat.dateofbirth))::Integer as age," +
                    " app.appointmentDate::date as dateexpected," +
                    " ('" + data + "'::date-app.appointmentDate::date)::integer as dayssinceexpected," +
                    " CASE" +
                    "    WHEN (('" + data + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '61 days')" +
                    "    ELSE" +
                    "  CASE" +
                    "      WHEN ((app.appointmentDate::date - app.visitdate::date) > 61) THEN (app.appointmentDate::date + INTERVAL '61 days')" +
                    "              ELSE null" +
                    "      END" +
                    " END" +
                    "  AS datelostfollowup," +
                    "  CASE" +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date" +
                    "    ELSE null" +
                    "  END" +
                    "  AS datereturn," +
                    "  pat.address1 ||" +
                    " case when ((pat.address2 is null)or(pat.address2 like ''))  then ''" +
                    " else ',' || pat.address2" +
                    " end" +
                    " ||" +
                    " case when ((pat.address3 is null)or(pat.address3 like '')) then ''" +
                    " else ',' || pat.address3" +
                    " end" +
                    " as address," +
                    " case when (presc.ptv = 'F' and presc.tb = 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as tarv," +
                    " case when (presc.ptv <> 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as ptv," +
                    " case when (presc.tb <> 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as tb," +
                    " max(app.appointmentDate) as ultimaData" +
                    " from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt, prescription as presc" +
                    " where app.patient = pat.id" +
                    " and presc.patient = pat.id" +
                    " and presc.\"current\" = 'T'" +
                    " and presc.tb = 'F'" +
                    " and presc.ptv = 'F'" +
                    " and presc.ccr = 'F'" +
                    " and idt.name = 'NID'" +
                    " and pi.value = pat.patientid" +
                    " and idt.id = pi.type_id" +
                    " and '" + clinicid + "' = pat.clinic" +
                    " and app.appointmentDate is not null" +
                    " and (app.visitdate::date is null)" +
                    " and (app.appointmentDate::date < '" + data + "'::date and ('" + data + "'::date - app.appointmentDate::date) between '" + minimumDate + "' and '" + maximumDate + "')" +
                    " and exists (select prescription.id" +
                    " from prescription" +
                    " where prescription.patient = pat.id" +
                    " and (('" + data + "' between prescription.date and prescription.endDate)or(('" + data + "'::date > prescription.date)) and (prescription.endDate is null)))" +
                    " and exists (select id from episode where episode.patient = pat.id" +
                    " and (('" + data + "'::date between episode.startdate and episode.stopdate)or(('" + data + "'::date > episode.startdate)) and (episode.stopdate is null)))" +
                    " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16" +
                    " order by age asc";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    RegistoChamadaTelefonicaXLS chamadaTelefonica = new RegistoChamadaTelefonicaXLS();
                    chamadaTelefonica.setNome(rs.getString("name"));
                    chamadaTelefonica.setNid(rs.getString("patid"));
                    chamadaTelefonica.setIdade(rs.getString("age"));
                    chamadaTelefonica.setContacto(((rs.getString("cellno") == null || "".equals(rs.getString("cellno").trim())) ? " "
                            : rs.getString("cellno") + " (c) ") + ((rs.getString("homeno") == null || "".equals(rs.getString("homeno").trim())) ? " "
                            : rs.getString("homeno") + " (h) ") + ((rs.getString("workno") == null || "".equals(rs.getString("workno").trim())) ? " "
                            : rs.getString("workno") + " (w)"));
                    chamadaTelefonica.setEndereco(rs.getString("address"));
                    chamadaTelefonica.setTarv(rs.getString("tarv"));
                    chamadaTelefonica.setTb(rs.getString("tb"));
                    chamadaTelefonica.setSmi(rs.getString("ptv"));

                    chamadaTelefonicaXLS.add(chamadaTelefonica);
                }
                rs.close();
            }

            st.close();
            conn_db.close();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chamadaTelefonicaXLS;
    }


    /**
     * @param clinicid
     * @param minimumDate
     * @param maximumDate
     * @param date
     */
    public List<RegistoChamadaTelefonicaXLS> getMissedAppointmentsPTV(String minimumDate, String maximumDate, Date date, String clinicid) {

        List<RegistoChamadaTelefonicaXLS> chamadaTelefonicaXLS = new ArrayList<RegistoChamadaTelefonicaXLS>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String data = dateFormat.format(date);

        try {
            conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

            String query = "select" +
                    " pat.patientid as patID," +
                    " (pat.firstnames||', '|| pat.lastname) as name," +
                    " pat.nextofkinname as supportername," +
                    " pat.nextofkinphone as supporterphone," +
                    " pat.cellphone as cellno," +
                    " pat.homephone as homeno," +
                    " pat.workphone as workno," +
                    " date_part('year',age(pat.dateofbirth))::Integer as age," +
                    " app.appointmentDate::date as dateexpected," +
                    " ('" + data + "'::date-app.appointmentDate::date)::integer as dayssinceexpected," +
                    " CASE" +
                    "    WHEN (('" + data + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '61 days')" +
                    "    ELSE" +
                    "	CASE" +
                    "	    WHEN ((app.appointmentDate::date - app.visitdate::date) > 61) THEN (app.appointmentDate::date + INTERVAL '61 days')" +
                    "              ELSE null" +
                    "    	END" +
                    " END" +
                    "  AS datelostfollowup," +
                    "  CASE" +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date" +
                    "    ELSE null" +
                    "  END" +
                    "  AS datereturn," +
                    "  pat.address1 ||" +
                    " case when ((pat.address2 is null)or(pat.address2 like ''))  then ''" +
                    " else ',' || pat.address2" +
                    " end" +
                    " ||" +
                    " case when ((pat.address3 is null)or(pat.address3 like '')) then ''" +
                    " else ',' || pat.address3" +
                    " end" +
                    " as address," +
                    " case when (presc.ptv = 'F' and presc.tb = 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as tarv," +
                    " case when (presc.ptv <> 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as ptv," +
                    " case when (presc.tb <> 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as tb," +
                    " max(app.appointmentDate) as ultimaData" +
                    " from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt, prescription as presc" +
                    " where app.patient = pat.id" +
                    " and presc.patient = pat.id" +
                    " and presc.\"current\" = 'T'" +
                    " and presc.tb='T'" +
                    " and idt.name = 'NID'" +
                    " and pi.value = pat.patientid" +
                    " and idt.id = pi.type_id" +
                    " and '" + clinicid + "' = pat.clinic" +
                    " and app.appointmentDate is not null" +
                    " and (app.visitdate::date is null)" +
                    " and (app.appointmentDate::date < '" + data + "'::date and ('" + data + "'::date - app.appointmentDate::date) between '" + minimumDate + "' and '" + maximumDate + "')" +
                    " and exists (select prescription.id" +
                    " from prescription" +
                    " where prescription.patient = pat.id" +
                    " and (('" + data + "' between prescription.date and prescription.endDate)or(('" + data + "' > prescription.date)) and (prescription.endDate is null)))" +
                    " and exists (select id from episode where episode.patient = pat.id" +
                    " and (('" + data + "' between episode.startdate and episode.stopdate)or(('" + data + "' > episode.startdate)) and (episode.stopdate is null)))" +
                    " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16" +
                    " order by age asc";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    RegistoChamadaTelefonicaXLS chamadaTelefonica = new RegistoChamadaTelefonicaXLS();
                    chamadaTelefonica.setNome(rs.getString("name"));
                    chamadaTelefonica.setNid(rs.getString("patid"));
                    chamadaTelefonica.setIdade(rs.getString("age"));
                    chamadaTelefonica.setContacto(((rs.getString("cellno") == null || "".equals(rs.getString("cellno").trim())) ? " "
                            : rs.getString("cellno") + " (c) ") + ((rs.getString("homeno") == null || "".equals(rs.getString("homeno").trim())) ? " "
                            : rs.getString("homeno") + " (h) ") + ((rs.getString("workno") == null || "".equals(rs.getString("workno").trim())) ? " "
                            : rs.getString("workno") + " (w)"));
                    chamadaTelefonica.setEndereco(rs.getString("address"));
                    chamadaTelefonica.setTarv(rs.getString("tarv"));
                    chamadaTelefonica.setTb(rs.getString("tb"));
                    chamadaTelefonica.setSmi(rs.getString("ptv"));

                    chamadaTelefonicaXLS.add(chamadaTelefonica);
                }
                rs.close();
            }

            st.close();
            conn_db.close();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chamadaTelefonicaXLS;
    }


    /**
     * @param clinicid
     * @param minimumDate
     * @param maximumDate
     * @param date
     */
    public List<RegistoChamadaTelefonicaXLS> getMissedAppointmentsSMI(String minimumDate, String maximumDate, Date date, String clinicid) {

        List<RegistoChamadaTelefonicaXLS> chamadaTelefonicaXLS = new ArrayList<RegistoChamadaTelefonicaXLS>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String data = dateFormat.format(date);

        try {
            conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

            String query = "select" +
                    " pat.patientid as patID," +
                    " (pat.firstnames||', '|| pat.lastname) as name," +
                    " pat.nextofkinname as supportername," +
                    " pat.nextofkinphone as supporterphone," +
                    " pat.cellphone as cellno," +
                    " pat.homephone as homeno," +
                    " pat.workphone as workno," +
                    " date_part('year',age(pat.dateofbirth))::Integer as age," +
                    " app.appointmentDate::date as dateexpected," +
                    " ('" + data + "'::date-app.appointmentDate::date)::integer as dayssinceexpected," +
                    " CASE" +
                    "    WHEN (('" + data + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '61 days')" +
                    "    ELSE" +
                    "	CASE" +
                    "	    WHEN ((app.appointmentDate::date - app.visitdate::date) > 61) THEN (app.appointmentDate::date + INTERVAL '61 days')" +
                    "              ELSE null" +
                    "    	END" +
                    " END" +
                    "  AS datelostfollowup," +
                    "  CASE" +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date" +
                    "    ELSE null" +
                    "  END" +
                    "  AS datereturn," +
                    "  pat.address1 ||" +
                    " case when ((pat.address2 is null)or(pat.address2 like ''))  then ''" +
                    " else ',' || pat.address2" +
                    " end" +
                    " ||" +
                    " case when ((pat.address3 is null)or(pat.address3 like '')) then ''" +
                    " else ',' || pat.address3" +
                    " end" +
                    " as address," +
                    " case when (presc.ptv = 'F' and presc.tb = 'F' and presc.ccr = 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as tarv," +
                    " case when (presc.ptv <> 'F' OR presc.ccr <> 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as ptv," +
                    " case when (presc.tb <> 'F')  then 'Sim'" +
                    " else 'Nao'" +
                    " end as tb," +
                    " max(app.appointmentDate) as ultimaData" +
                    " from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt, prescription as presc" +
                    " where app.patient = pat.id" +
                    " and presc.patient = pat.id" +
                    " and presc.\"current\" = 'T'" +
                    " and (presc.ptv='T' OR presc.ccr='T')" +
                    " and idt.name = 'NID'" +
                    " and pi.value = pat.patientid" +
                    " and idt.id = pi.type_id" +
                    " and '" + clinicid + "' = pat.clinic" +
                    " and app.appointmentDate is not null" +
                    " and (app.visitdate::date is null)" +
                    " and (app.appointmentDate::date < '" + data + "'::date and ('" + data + "'::date - app.appointmentDate::date) between '" + minimumDate + "' and '" + maximumDate + "')" +
                    " and exists (select prescription.id" +
                    " from prescription" +
                    " where prescription.patient = pat.id" +
                    " and (('" + data + "' between prescription.date and prescription.endDate)or(('" + data + "' > prescription.date)) and (prescription.endDate is null)))" +
                    " and exists (select id from episode where episode.patient = pat.id" +
                    " and (('" + data + "' between episode.startdate and episode.stopdate)or(('" + data + "' > episode.startdate)) and (episode.stopdate is null)))" +
                    " group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16" +
                    " order by age asc";

            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    RegistoChamadaTelefonicaXLS chamadaTelefonica = new RegistoChamadaTelefonicaXLS();
                    chamadaTelefonica.setNome(rs.getString("name"));
                    chamadaTelefonica.setNid(rs.getString("patid"));
                    chamadaTelefonica.setIdade(rs.getString("age"));
                    chamadaTelefonica.setContacto(((rs.getString("cellno") == null || "".equals(rs.getString("cellno").trim())) ? " "
                            : rs.getString("cellno") + " (c) ") + ((rs.getString("homeno") == null || "".equals(rs.getString("homeno").trim())) ? " "
                            : rs.getString("homeno") + " (h) ") + ((rs.getString("workno") == null || "".equals(rs.getString("workno").trim())) ? " "
                            : rs.getString("workno") + " (w)"));
                    chamadaTelefonica.setEndereco(rs.getString("address"));
                    chamadaTelefonica.setTarv(rs.getString("tarv"));
                    chamadaTelefonica.setTb(rs.getString("tb"));
                    chamadaTelefonica.setSmi(rs.getString("ptv"));

                    chamadaTelefonicaXLS.add(chamadaTelefonica);
                }
                rs.close();
            }

            st.close();
            conn_db.close();


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chamadaTelefonicaXLS;
    }

    public List<AbsenteeForSupportCall> getAbsentee(String minDays, String maxDays, Date date, String clinicid) {

        List<AbsenteeForSupportCall> absenteeXLS = new ArrayList<AbsenteeForSupportCall>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String data = dateFormat.format(date);

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select " +
                    "pat.patientid as patID, " +
                    "(pat.lastname||', '|| pat.firstnames) as name, " +
                    " pat.nextofkinname as supportername, " +
                    "pat.nextofkinphone as supporterphone, " +
                    "pat.cellphone as cellno, " +
                    "date_part('year',age(pat.dateofbirth)) as age, " +
                    "app.appointmentDate::date as dateexpected, " +
                    "( '" + data + "'::date-app.appointmentDate::date)::integer as dayssinceexpected, " +
                    "CASE " +
                    "    WHEN (('" + data + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '60 days') " +
                    "    ELSE " +
                    "CASE " +
                    "    WHEN ((app.appointmentDate::date - app.visitdate::date) > 60) THEN (app.appointmentDate::date + INTERVAL '60 days')" +
                    "              ELSE null " +
                    "    END " +
                    "END " +
                    "  AS datelostfollowup, " +
                    "" +
                    "  CASE " +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date " +
                    "    ELSE null " +
                    "  END " +
                    "  AS datereturn, " +
                    "max(app.appointmentDate) as ultimaData " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt " +
                    "where app.patient = pat.id " +
                    "and idt.name = 'NID' " +
                    "and pi.value = pat.patientid " +
                    "and idt.id = pi.type_id " +
                    "and " + clinicid + " = pat.clinic " +
                    "and (app.visitDate > app.appointmentDate OR app.visitDate is null ) " +
                    "and ('" + data + "'::date - app.appointmentDate::date) between " + minDays + " and " + maxDays + " " +
                    "and exists (select prescription.id " +
                    "from prescription " +
                    "where prescription.patient = pat.id " +
                    "and prescription.tipodoenca like '%ARV' " +
                    "and prescription.dispensatrimestral = 0 " +
                    "and (('" + data + "' between prescription.date and prescription.endDate)or(('" + data + "' > prescription.date)) and (prescription.endDate is null))) " +
                    "and exists (select id from episode where episode.patient = pat.id " +
                    "and (('" + data + "' between episode.startdate and episode.stopdate)or(('" + data + "' > episode.startdate)) and (episode.stopdate is null))) " +
                    "group by 1,2,3,4,5,6,7,8,9,10 " +
                    "order by patID asc";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    AbsenteeForSupportCall absentee = new AbsenteeForSupportCall();
                    absentee.setPatientIdentifier(rs.getString("patID"));
                    absentee.setNome(rs.getString("name"));
                    absentee.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    absentee.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    absentee.setDataRegressoUnidadeSanitaria(rs.getString("datereturn"));
                    absentee.setContacto(rs.getString("cellno"));

                    absenteeXLS.add(absentee);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return absenteeXLS;

    }

    public List<AbsenteeForSupportCall> getAbsenteeAndLostToFalowUp(String minDays, String maxDays, Date date, String clinicid) {

        List<AbsenteeForSupportCall> absenteeAndLostToFalowUpXLS = new ArrayList<AbsenteeForSupportCall>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String data = dateFormat.format(date);

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select  " +
                    "pat.patientid as patID,  " +
                    "(pat.lastname||', '|| pat.firstnames) as name,  " +
                    " pat.nextofkinname as supportername,  " +
                    "pat.nextofkinphone as supporterphone,  " +
                    "pat.cellphone as cellno,  " +
                    "date_part('year',age(pat.dateofbirth)) as age,  " +
                    "app.appointmentDate::date as dateexpected,  " +
                    "('" + data + "'::date-app.appointmentDate::date)::integer as dayssinceexpected,  " +
                    "CASE  " +
                    "    WHEN (('" + data + "'::date-app.appointmentDate::date) > 59 AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '60 days')  " +
                    "    ELSE  " +
                    "  CASE  " +
                    "      WHEN ((app.appointmentDate::date - app.visitdate::date) > 61) THEN (app.appointmentDate::date + INTERVAL '60 days')  " +
                    "              ELSE null  " +
                    "      END  " +
                    "END  " +
                    "  AS datelostfollowup,  " +
                    "  " +
                    "  CASE  " +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) > 0 THEN app.visitdate::date  " +
                    "    ELSE null  " +
                    "  END  " +
                    "  AS datereturn,  " +
                    "  pat.address1 ||  " +
                    "case when ((pat.address2 is null)or(pat.address2 like ''))  then ''  " +
                    "else ',' || pat.address2  " +
                    "end  " +
                    "||  " +
                    "case when ((pat.address3 is null)or(pat.address3 like '')) then ''  " +
                    "else ',' || pat.address3  " +
                    "end  " +
                    "as address,  " +
                    "max(app.appointmentDate) as ultimaData  " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt  " +
                    "where app.patient = pat.id  " +
                    "and idt.name = 'NID'  " +
                    "and pi.value = pat.patientid  " +
                    "and idt.id = pi.type_id  " +
                    "and " + clinicid + " = pat.clinic " +
                    "and app.appointmentDate is not null  " +
                    "and (app.appointmentDate::date) between ('" + data + "'::date - INTERVAL '90 days') and '" + data + "'::date  " +
                    "and exists (select prescription.id  " +
                    "from prescription  " +
                    "where prescription.patient = pat.id  " +
                    "and prescription.dispensatrimestral = 0  " +
                    "and (('" + data + "' between prescription.date and prescription.endDate)or(('" + data + "' > prescription.date)) and (prescription.endDate is null)))  " +
                    "and exists (select id from episode where episode.patient = pat.id  " +
                    "and (('" + data + "' between episode.startdate and episode.stopdate)or(('" + data + "' > episode.startdate)) and (episode.stopdate is null)))  " +
                    "group by 1,2,3,4,5,6,7,8,9,10,11  " +
                    "order by dayssinceexpected";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    AbsenteeForSupportCall absenteeAndLostToFalowUp = new AbsenteeForSupportCall();
                    absenteeAndLostToFalowUp.setPatientIdentifier(rs.getString("patID"));
                    absenteeAndLostToFalowUp.setNome(rs.getString("name"));
                    absenteeAndLostToFalowUp.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    absenteeAndLostToFalowUp.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    absenteeAndLostToFalowUp.setDataRegressoUnidadeSanitaria(rs.getString("datereturn"));
                    absenteeAndLostToFalowUp.setContacto(rs.getString("cellno"));

                    absenteeAndLostToFalowUpXLS.add(absenteeAndLostToFalowUp);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return absenteeAndLostToFalowUpXLS;

    }

    public void UpdateDatabase() throws SQLException {
        String s = new String();
        StringBuffer sb = new StringBuffer();

        try {
            FileReader fr = new FileReader(new File("AlteracoesIDARTSql.sql"));
            // be sure to not have line starting with "--" or "/*" or any other non aplhabetical character

            BufferedReader br = new BufferedReader(fr);

            while ((s = br.readLine()) != null) {
                sb.append(s);
            }
            br.close();

            // here is our splitter ! We use ";" as a delimiter for each request
            // then we are sure to have well formed statements

            String[] inst = sb.toString().split(";");

            conecta(iDartProperties.hibernateUsername, iDartProperties.hibernatePassword);

            for (int i = 0; i < inst.length; i++) {
                // we ensure that there is no spaces before or after the request string
                // in order to not execute empty statements
                try {
                    if (!inst[i].trim().equals("")) {
                        if (inst[i].startsWith("CREATE OR REPLACE FUNCTION")) {
                            inst[i] = inst[i].replaceAll("!", ";");
                        }
                        st.executeUpdate(inst[i]);
                        log.trace(">>" + inst[i]);
                    }
                    break;
                } catch (SQLException e) {
                    log.trace("### - SQL Error " + e.getMessage());
                } finally {

                    continue;
                }

            }

        } catch (Exception e) {
            log.trace("*** Error : " + e.toString());
            log.trace("*** ");
            log.trace("*** Error : ");
            e.printStackTrace();
            log.trace("################################################");
            log.trace(sb.toString());
        }

    }

    public List<BalanceteDiarioXLS> getBalanceteDiarioXLS(String startDate, String endDate, Drug drug, StockCenter clinic) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select  " +
                "COALESCE((a.allreceived * " + drug.getPackSize() + " - COALESCE((b.allissued * " + drug.getPackSize() + ") + b.allpill, 0) - COALESCE(f.alladjusted, 0) + COALESCE((h.allreturned * " + drug.getPackSize() + ") + h.allpills, 0)), 0) " +
                "as openingpills,  " +
                "COALESCE(c.received,0) as received,  " +
                "COALESCE(sum(e.issued),0) as destroyed , COALESCE(sum(e.pill),0) as destroyedpills,  " +
                "COALESCE(sum(d.issued),0) as dispensed , COALESCE(sum(d.pill),0) as dispensedpills,  " +
                "COALESCE(g.adjusted,0) as adjusted,  " +
                "COALESCE(sum(i.returned),0) as returned , COALESCE(sum(i.pills),0) as returnedpills,  " +
                "pg_catalog.date(drug.searchdate) as movdate,  " +
                "g.notes  " +
                "from  " +
                "(select generate_series('" + startDate + "'::date, '" + endDate + "'::date, '1 day'::interval) searchdate, id  " +
                "from drug where id = '" + drug.getId() + "' " +
                ") as drug  " +
                "left join  " +
                "(select sum(s.unitsreceived) as allreceived, d.id  " +
                " from drug as d, stock as s  " +
                " where d.id = d.id and s.stockCenter = '" + clinic.getId() + "' and s.drug = d.id  " +
                " and pg_catalog.date(s.datereceived) < '" + startDate + "'::date  " +
                " GROUP BY 2  " +
                ") as a on a.id = drug.id  " +
                "left join  " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as allissued, MOD(sum(pd.amount)," + drug.getPackSize() + ") as allpill,d.id  " +
                "from drug as d, stock as s, packageddrugs as pd, package as p,prescription as pre  " +
                "where d.id = d.id and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id  " +
                "and p.prescription = pre.id  " +
                "and pg_catalog.date(p.packdate) < '" + startDate + "'::date  " +
                "  GROUP BY 3  " +
                ") as b on b.id = drug.id  " +
                "left join  " +
                "(select sum(sa.stockcount) as alladjusted, d.id  " +
                "from drug as d, stock as s, stockAdjustment as sa  " +
                "where d.id = d.id  " +
                "and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id  " +
                "and sa.stock = s.id  " +
                " and pg_catalog.date(sa.captureDate) < '" + startDate + "'::date  " +
                "group by 2  " +
                ") as f on f.id = drug.id  " +
                "left join  " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as allreturned, MOD(sum(pd.amount)," + drug.getPackSize() + ") as allpills,d.id  " +
                "from drug as d, stock as s, packageddrugs as pd, package as p  " +
                "where d.id = d.id  " +
                "and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id and pd.stock = s.id  " +
                "and pd.parentpackage = p.id  " +
                "and p.stockReturned = true  " +
                "and p.packageReturned = true  " +
                "and pg_catalog.date(p.dateReturned) < '" + startDate + "'::date  " +
                " GROUP BY 3  " +
                ") as h on h.id = drug.id  " +
                "left join  " +
                "(select sum(s.unitsreceived) as received, s.datereceived, d.id  " +
                " from drug as d, stock as s  " +
                "where d.id = d.id and s.stockCenter = '" + clinic.getId() + "' and s.drug = d.id  " +
                " GROUP BY 2,3  " +
                ") as c on c.id = drug.id and pg_catalog.date(c.datereceived) = pg_catalog.date(drug.searchdate)  " +
                "left join  " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as issued, MOD(sum(pd.amount)," + drug.getPackSize() + ") as pill,p.packdate,d.id  " +
                "from drug as d, stock as s, packageddrugs as pd, package as p,prescription as pre  " +
                "where d.id = d.id and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id  " +
                "and p.prescription = pre.id  " +
                "  GROUP BY 3,4  " +
                ") as d on d.id = drug.id and pg_catalog.date(d.packdate) = pg_catalog.date(drug.searchdate)  " +
                "left join  " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as issued, MOD(sum(pd.amount)," + drug.getPackSize() + ") as pill,p.packdate,d.id  " +
                "from drug as d, stock as s, packageddrugs as pd, package as p  " +
                "where d.id = d.id and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id  " +
                "and p.prescription is null  " +
                "  GROUP BY 3,4  " +
                ") as e on e.id = drug.id and pg_catalog.date(e.packdate) = pg_catalog.date(drug.searchdate)  " +
                "left join  " +
                "(select sum(sa.stockcount) as adjusted, sa.notes,sa.captureDate, d.id  " +
                "from drug as d, stock as s, stockAdjustment as sa  " +
                "where d.id = d.id  " +
                "and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id  " +
                "and sa.stock = s.id  " +
                "group by 2,3,4  " +
                ") as g on g.id = drug.id and pg_catalog.date(g.captureDate) = pg_catalog.date(drug.searchdate)  " +
                "left join  " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as returned, MOD(sum(pd.amount)," + drug.getPackSize() + ") as pills,p.dateReturned,d.id  " +
                "from drug as d, stock as s, packageddrugs as pd, package as p  " +
                "where d.id = d.id  " +
                "and s.stockCenter = '" + clinic.getId() + "'  " +
                "and s.drug = d.id and pd.stock = s.id  " +
                "and pd.parentpackage = p.id  " +
                "and p.stockReturned = true  " +
                "and p.packageReturned = true  " +
                " GROUP BY 3,4  " +
                ") as i on i.id = drug.id and pg_catalog.date(i.dateReturned) = pg_catalog.date(drug.searchdate)  " +
                "GROUP BY 1,received, movdate,adjusted, notes  " +
                "ORDER BY movdate";

        List<BalanceteDiarioXLS> listBalanceteDiarioXLS = new ArrayList<BalanceteDiarioXLS>();
        ResultSet rs = st.executeQuery(query);

        long somaUnidadesRecebidas = 0;
        long somaUnidadesDevolvidas = 0;
        long somaUnidadesDispensadas = 0;
        long somaUnidadesDestruidas = 0;
        long somaUnidadesAjustadas = 0;


        if (rs != null) {

            while (rs.next()) {
                long lostAdjust = 0;
                long dispensed = 0;
                long stock = 0;
                long totalpills = 0;


                totalpills = rs.getInt("openingpills") + somaUnidadesRecebidas
                        + rs.getInt("received") * drug.getPackSize() + somaUnidadesDevolvidas
                        + rs.getInt("returned") * drug.getPackSize() + rs.getInt("returnedpills")
                        - rs.getInt("dispensed") * drug.getPackSize() - somaUnidadesDispensadas
                        - somaUnidadesDestruidas - somaUnidadesAjustadas - dispensed * drug.getPackSize()
                        - rs.getInt("dispensedpills") - rs.getInt("destroyed") * drug.getPackSize()
                        - rs.getInt("destroyedpills") - rs.getInt("adjusted");

                somaUnidadesRecebidas = somaUnidadesRecebidas + rs.getInt("received") * drug.getPackSize();
                somaUnidadesDevolvidas = somaUnidadesDevolvidas + rs.getInt("returned") * drug.getPackSize() + rs.getInt("returnedpills");
                somaUnidadesDispensadas = somaUnidadesDispensadas + rs.getInt("dispensed") * drug.getPackSize() + rs.getInt("dispensedpills");
                somaUnidadesDestruidas = somaUnidadesDestruidas + rs.getInt("destroyed") * drug.getPackSize() + rs.getInt("destroyedpills");
                somaUnidadesAjustadas = somaUnidadesAjustadas + rs.getInt("adjusted");


                //   if (rs.getInt("adjusted") % drug.getPackSize() == 0)
                lostAdjust = rs.getInt("adjusted") / drug.getPackSize();

                //   if (rs.getInt("dispensedpills") % drug.getPackSize() == 0)
                dispensed = rs.getInt("dispensed");
                //     else
                //        dispensed = rs.getInt("dispensed") + 1;

                if (totalpills % drug.getPackSize() == 0)
                    stock = totalpills / drug.getPackSize();
                else
                    stock = totalpills / drug.getPackSize() + 1;


                BalanceteDiarioXLS balanceteDiarioXLS = new BalanceteDiarioXLS();
                balanceteDiarioXLS.setDataMovimento(rs.getString("movdate"));
                balanceteDiarioXLS.setEntrance(rs.getString("received"));
                balanceteDiarioXLS.setLostAjust(String.valueOf(lostAdjust));
                balanceteDiarioXLS.setOutgoing(String.valueOf(dispensed));
                balanceteDiarioXLS.setStock(String.valueOf(stock));
                balanceteDiarioXLS.setNotes(rs.getString("notes"));

                listBalanceteDiarioXLS.add(balanceteDiarioXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return listBalanceteDiarioXLS;

    }

    public List<FichaStockXLS> getFichaStockXLS(String startDate, String endDate, Drug drug, StockCenter clinic) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select g.datamovimento,g.cliente,g.tipomovimento,COALESCE(g.quantidade,0) quantidade ,g.numeroguia, " +
                "COALESCE(saldo.openingpills,0) as openingpills,COALESCE(saldo.destroyed,0) destroyed ,COALESCE(saldo.destroyedpills,0) destroyedpills , " +
                "COALESCE(saldo.returned,0) returned ,COALESCE(saldo.returnedpills,0) returnedpills , " +
                "drug.id " +
                "from drug " +
                "left join " +
                "( " +
                "select s.datereceived as datamovimento, " +
                "'DPM' as cliente, " +
                "'Requisição' as tipomovimento, " +
                "COALESCE(s.unitsreceived, 0) as quantidade, " +
                "s.numeroguia as numeroguia, " +
                "d.id " +
                "from drug d " +
                "inner join stock s on s.drug = d.id " +
                "where (pg_catalog.date(s.datereceived)) = '" + startDate + "'::date " +
                "and s.drug = d.id " +
                "UNION " +
                "select pa.pickupdate as datamovimento, " +
                "pat.patientid || ' - ' || pat.firstnames || ' ' || pat.lastname as  cliente , " +
                "'Distribuição' as tipomovimento, " +
                " CASE " +
                "  WHEN MOD(pds.amount," + drug.getPackSize() + ") > 0 THEN " +
                "   COALESCE(round(floor((pds.amount::real)/" + drug.getPackSize() + ")::numeric,0) + 1, 0) " +
                "  ELSE " +
                "   COALESCE(round(floor((pds.amount::real)/" + drug.getPackSize() + ")::numeric,0), 0) " +
                " END " +
                " as quanntidade, " +
                "' - ' as numguia, " +
                "d.id " +
                "from package pa " +
                "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                "inner join prescription pre on pre.id = pa.prescription " +
                "inner join patient pat ON pre.patient=pat.id " +
                "inner join stock st on st.id = pds.stock " +
                "inner join drug d on d.id = st.drug " +
                "where st.stockCenter =  '" + clinic.getId() + "' " +
                "and (pg_catalog.date(pa.pickupdate)) = '" + startDate + "'::date " +
                "UNION " +
                "select sa.captureDate as datamovimento, " +
                "' - ' as cliente, " +
                " CASE " +
                "   WHEN sa.stocktake IS NULL THEN ' Ajuste '" +
                "   ELSE ' Inventario ' " +
                " END as tipomovimento, " +
                "COALESCE(sa.stockcount, 0) as quantidade, " +
                "' - ' as numeroguia, " +
                "d.id " +
                "from drug as d " +
                "inner join stock s on s.drug = d.id " +
                "inner join stockAdjustment sa on sa.stock = s.id " +
                "where(pg_catalog.date(sa.captureDate)) = '" + startDate + "'::date " +
                "order by 1 asc " +
                ") as g on g.id = drug.id " +
                " " +
                "left join " +
                "( select " +
                "COALESCE((a.allreceived * " + drug.getPackSize() + " - COALESCE((b.allissued * " + drug.getPackSize() + ") + b.allpill, 0) - COALESCE(f.alladjusted, 0) + COALESCE((h.allreturned * " + drug.getPackSize() + ") + h.allpills, 0)), 0) " +
                "as openingpills, " +
                "COALESCE(sum(e.issued),0) as destroyed , COALESCE(sum(e.pill),0) as destroyedpills, " +
                "COALESCE(sum(i.returned),0) as returned , COALESCE(sum(i.pills),0) as returnedpills, " +
                "drug.id " +
                "from " +
                "(select generate_series('" + startDate + "'::date, '" + startDate + "'::date, '1 day'::interval) searchdate, id " +
                "from drug where id = '" + drug.getId() + "' " +
                ") as drug " +
                "left join " +
                "(select sum(s.unitsreceived) as allreceived, d.id " +
                " from drug as d, stock as s " +
                " where d.id = d.id and s.stockCenter =  '" + clinic.getId() + "' and s.drug = d.id " +
                " and pg_catalog.date(s.datereceived) < '" + startDate + "'::date " +
                " GROUP BY 2 " +
                ") as a on a.id = drug.id " +
                "left join " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as allissued, MOD(sum(pd.amount)," + drug.getPackSize() + ") as allpill,d.id " +
                "from drug as d, stock as s, packageddrugs as pd, package as p,prescription as pre " +
                "where d.id = d.id and s.stockCenter =  '" + clinic.getId() + "' " +
                "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id " +
                "and p.prescription = pre.id " +
                "and pg_catalog.date(p.packdate) < '" + startDate + "'::date " +
                "  GROUP BY 3 " +
                ") as b on b.id = drug.id " +
                "left join " +
                "(select sum(sa.stockcount) as alladjusted, d.id " +
                "from drug as d, stock as s, stockAdjustment as sa " +
                "where d.id = d.id " +
                "and s.stockCenter =  '" + clinic.getId() + "' " +
                "and s.drug = d.id " +
                "and sa.stock = s.id " +
                " and pg_catalog.date(sa.captureDate) < '" + startDate + "'::date " +
                "group by 2 " +
                ") as f on f.id = drug.id " +
                "left join " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as allreturned, MOD(sum(pd.amount)," + drug.getPackSize() + ") as allpills,d.id " +
                "from drug as d, stock as s, packageddrugs as pd, package as p " +
                "where d.id = d.id " +
                "and s.stockCenter =  '" + clinic.getId() + "' " +
                "and s.drug = d.id and pd.stock = s.id " +
                "and pd.parentpackage = p.id " +
                "and p.stockReturned = true " +
                "and p.packageReturned = true " +
                "and pg_catalog.date(p.dateReturned) < '" + startDate + "'::date " +
                " GROUP BY 3 " +
                ") as h on h.id = drug.id " +
                "left join " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as issued, MOD(sum(pd.amount)," + drug.getPackSize() + ") as pill,p.packdate,d.id " +
                "from drug as d, stock as s, packageddrugs as pd, package as p " +
                "where d.id = d.id and s.stockCenter =  '" + clinic.getId() + "' " +
                "and s.drug = d.id and pd.stock = s.id and pd.parentpackage = p.id " +
                "and p.prescription is null " +
                "  GROUP BY 3,4 " +
                ") as e on e.id = drug.id and pg_catalog.date(e.packdate) = pg_catalog.date(drug.searchdate) " +
                "left join " +
                "(select round(floor(sum(pd.amount::real)/" + drug.getPackSize() + ")::numeric,0) as returned, MOD(sum(pd.amount)," + drug.getPackSize() + ") as pills,p.dateReturned,d.id " +
                "from drug as d, stock as s, packageddrugs as pd, package as p " +
                "where d.id = d.id " +
                "and s.stockCenter =  '" + clinic.getId() + "' " +
                "and s.drug = d.id and pd.stock = s.id " +
                "and pd.parentpackage = p.id " +
                "and p.stockReturned = true " +
                "and p.packageReturned = true " +
                " GROUP BY 3,4 " +
                ") as i on i.id = drug.id and pg_catalog.date(i.dateReturned) = pg_catalog.date(drug.searchdate) " +
                "GROUP BY 1,6 " +
                ") saldo on saldo.id = drug.id " +
                "where drug.id = '" + drug.getId() + "' " +
                "Order by 3 desc";

        List<FichaStockXLS> listFichaStockXLS = new ArrayList<FichaStockXLS>();
        ResultSet rs = st.executeQuery(query);

        long somaUnidadesRecebidas = 0;
        long somaUnidadesDevolvidas = 0;
        long somaUnidadesDispensadas = 0;
        long somaUnidadesDestruidas = 0;
        long somaUnidadesAjustadas = 0;


        if (rs != null) {

            while (rs.next()) {
                long recebidos = 0;
                long dispensados = 0;
                long ajustados = 0;
                long stock = 0;
                long totalpills = 0;

                if (rs.getString("tipomovimento").contains("Requis")) {
                    recebidos = rs.getInt("quantidade");
                }

                if (rs.getString("tipomovimento").contains("Dist")) {
                    dispensados = rs.getInt("quantidade");
                }

                if (rs.getString("tipomovimento").contains("Ajuste") || rs.getString("tipomovimento").contains("Inventario")) {
                    ajustados = rs.getInt("quantidade") / drug.getPackSize();
                }

                totalpills = rs.getInt("openingpills")
                        + rs.getInt("returned") * drug.getPackSize()
                        + rs.getInt("returnedpills")
                        - rs.getInt("destroyed") * drug.getPackSize()
                        - rs.getInt("destroyedpills")
                        + somaUnidadesRecebidas * drug.getPackSize() + recebidos * drug.getPackSize()
                        - somaUnidadesDispensadas * drug.getPackSize() - dispensados * drug.getPackSize()
                        - somaUnidadesAjustadas - ajustados * drug.getPackSize();

                if (rs.getString("tipomovimento").contains("Requis")) {
                    somaUnidadesRecebidas = somaUnidadesRecebidas + rs.getInt("quantidade");
                }

                if (rs.getString("tipomovimento").contains("Dist")) {
                    somaUnidadesDispensadas = somaUnidadesDispensadas + rs.getInt("quantidade");
                }

                if (rs.getString("tipomovimento").contains("Ajuste") || rs.getString("tipomovimento").contains("Inventario")) {
                    somaUnidadesAjustadas = somaUnidadesAjustadas + rs.getInt("quantidade");
                }
                somaUnidadesDevolvidas = somaUnidadesDevolvidas + rs.getInt("returned") * drug.getPackSize() + rs.getInt("returnedpills");
                somaUnidadesDestruidas = somaUnidadesDestruidas + rs.getInt("destroyed") * drug.getPackSize() + rs.getInt("destroyedpills");

                if (totalpills % drug.getPackSize() == 0)
                    stock = totalpills / drug.getPackSize();
                else
                    stock = totalpills / drug.getPackSize() + 1;


                FichaStockXLS fichaStockXLS = new FichaStockXLS();
                fichaStockXLS.setDataMovimento(rs.getString("datamovimento"));
                fichaStockXLS.setTipoMovimento(rs.getString("tipomovimento"));
                if (rs.getString("tipomovimento").contains("Ajuste") || rs.getString("tipomovimento").contains("Inventario"))
                    fichaStockXLS.setQuantidade(String.valueOf(ajustados));
                else
                    fichaStockXLS.setQuantidade(rs.getString("quantidade"));
                fichaStockXLS.setCliente(rs.getString("cliente"));
                fichaStockXLS.setStock(String.valueOf(stock));
                fichaStockXLS.setNotes(rs.getString("numeroguia"));

                listFichaStockXLS.add(fichaStockXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return listFichaStockXLS;

    }

    public List<PacienteReferidoXLS> getReferedPatients(String dataInicio, String dataFim) {

        List<PacienteReferidoXLS> pacienteReferidoXLSList = new ArrayList<PacienteReferidoXLS>();

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select distinct " +
                    "p.patientid as nid, " +
                    "p.firstnames ||' '||p.lastname as nome, " +
                    "extract(year FROM age(current_date, p.dateofbirth))::integer as idade, " +
                    "c.clinicname as facilityName, " +
                    "nc.facilityName as mainfacilityName, " +
                    "rt.regimeesquema, " +
                    "lt.linhanome, " +
                    "CASE " +
                    "  WHEN pr.dispensatrimestral = 1 THEN 'DT' " +
                    "  WHEN pr.dispensasemestral = 1 THEN 'DS' " +
                    "ELSE 'DM' " +
                    "END AS tipodispensa, " +
                    "to_char(pack.proxLev,'DD-MM-YYYY') as proxLev, " +
                    "to_char(Max(e.startdate),'DD-MM-YYYY') as dataReferencia, " +
                    "to_char(Max(pr.date),'DD-MM-YYYY') as ultimaPrescricao " +
                    "from Patient as p " +
                    "inner join clinic c on c.id = p.clinic " +
                    "inner join prescription pr on pr.patient = p.id " +
                    "inner join regimeterapeutico rt on rt.regimeid = pr.regimeid " +
                    "inner join linhat lt on lt.linhaid = pr.linhaid " +
                    "inner join episode e on e.patient = p.id " +
                    "inner join nationalclinics nc on nc.id = c.clinicdetails_id " +
                    "inner join ( " +
                    "select distinct pat.id patient, Max(pg_catalog.date(to_date(pdit.dateexpectedstring,'DD Mon YYYY'))) proxLev, " +
                    "Max(p.date) prescriptiondate " +
                    "from package pa " +
                    "inner join packageddrugs pds on pds.parentpackage = pa.id " +
                    "inner join packagedruginfotmp pdit on pdit.packageddrug = pds.id " +
                    "inner join prescription p on p.id = pa.prescription " +
                    "inner join patient pat on pat.id = p.patient " +
                    " where pg_catalog.date(p.date) < '" + dataFim + "'::date + INTERVAL '1 day' " +
                    "group by 1 " +
                    ") pack on pack.patient = p.id and pack.prescriptiondate = pr.date " +
                    "where  e.startdate >=  '" + dataInicio + "'::date " +
                    "AND e.startdate < ('" + dataFim + "'::date + INTERVAL '1 day') " +
                    "AND pg_catalog.date(pr.date) < ('" + dataFim + "'::date + INTERVAL '1 day') " +
                    "AND startreason like '%eferido%' " +
                    "AND c.mainclinic <> true " +
                    "group by 1,2,3,4,5,6,7,8,9 " +
                    "order by p.patientid asc";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    PacienteReferidoXLS pacienteReferidoXLS = new PacienteReferidoXLS();
                    pacienteReferidoXLS.setNid(rs.getString("nid"));
                    pacienteReferidoXLS.setNome(rs.getString("nome"));
                    pacienteReferidoXLS.setIdade(rs.getString("idade"));
                    pacienteReferidoXLS.setDataultimaPrescricao(rs.getString("ultimaprescricao"));
                    pacienteReferidoXLS.setRegimaterapeutico(rs.getString("regimeesquema"));
                    pacienteReferidoXLS.setTipoDispensa(rs.getString("tipodispensa"));
                    pacienteReferidoXLS.setDataProximoLevantamento(rs.getString("proxlev"));
                    pacienteReferidoXLS.setDatareferencia(rs.getString("datareferencia"));
                    pacienteReferidoXLS.setFarmaciaReferencia(rs.getString("facilityname"));

                    pacienteReferidoXLSList.add(pacienteReferidoXLS);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return pacienteReferidoXLSList;
    }

    public List<PacienteReferidoXLS> getPatientsReferedFrom(String dataInicio, String dataFim) {

        List<PacienteReferidoXLS> pacienteReferidoXLSList = new ArrayList<PacienteReferidoXLS>();

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select distinct  " +
                    "p.patientid as nid,  " +
                    "p.firstnames ||' '||p.lastname as nome,  " +
                    "extract(year FROM age(current_date, p.dateofbirth))::integer as idade,  " +
                    "CASE  " +
                    "  WHEN (stp.province IS null OR stp.province like '%Selec%')  " +
                    "    THEN stp.mainclinicname  " +
                    "  ELSE stp.mainclinicname ||' - '||stp.province  " +
                    "END as facilityName,  " +
                    "c.clinicname as mainfacilityName,  " +
                    "to_char(Min(e.startdate),'DD-MM-YYYY') as dataEntrada  " +
                    "from Patient as p  " +
                    "inner join clinic c on c.id = p.clinic  " +
                    "inner join sync_temp_patients stp on stp.uuidopenmrs = p.uuidopenmrs  " +
                    "inner join episode e on e.patient = p.id  " +
                    "where e.startdate >= '" + dataInicio + "'::date  " +
                    "AND e.startdate < ('" + dataFim + "'::date + INTERVAL '1 day')  " +
                    "AND e.startreason like '%Novo%'  " +
                    "AND c.mainclinic = true  " +
                    "group by 1,2,3,4,5  " +
                    "order by p.patientid asc";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    PacienteReferidoXLS pacienteReferidoXLS = new PacienteReferidoXLS();
                    pacienteReferidoXLS.setNid(rs.getString("nid"));
                    pacienteReferidoXLS.setNome(rs.getString("nome"));
                    pacienteReferidoXLS.setIdade(rs.getString("idade"));
                    pacienteReferidoXLS.setDatareferencia(rs.getString("dataEntrada"));
                    pacienteReferidoXLS.setFarmaciaReferencia(rs.getString("facilityName"));

                    pacienteReferidoXLSList.add(pacienteReferidoXLS);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return pacienteReferidoXLSList;
    }

    public List<FarmaciasRegistadasXLS> getFarmaciasRegistadasXLS() throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select c.code, c.clinicname, " +
                "c.facilitytype, " +
                "c.province, " +
                "c.district, " +
                "c.telephone, " +
                "CASE " +
                " WHEN nc.facilityname = c.clinicname OR nc.facilityname IS NULL THEN '-' " +
                " ELSE nc.facilityname " +
                "END as facilityname, " +
                "c.uuid " +
                "FROM clinic c " +
                "left join nationalclinics nc ON nc.id = c.clinicdetails_id " +
                "WHERE c.mainclinic <> true " +
                "order by nc.facilityname";

        List<FarmaciasRegistadasXLS> farmaciaregistadaXLS = new ArrayList<FarmaciasRegistadasXLS>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {
                FarmaciasRegistadasXLS farmaciasRegistadasXLS = new FarmaciasRegistadasXLS();
                farmaciasRegistadasXLS.setCode(rs.getString("code"));
                farmaciasRegistadasXLS.setClinicName(rs.getString("clinicname"));
                farmaciasRegistadasXLS.setFacilityType(rs.getString("facilitytype"));
                farmaciasRegistadasXLS.setDistrict(rs.getString("district"));
                farmaciasRegistadasXLS.setProvince(rs.getString("province"));
                farmaciasRegistadasXLS.setContact(rs.getString("telephone"));
                farmaciasRegistadasXLS.setDependence(rs.getString("facilityname"));
                farmaciasRegistadasXLS.setUuid(rs.getString("uuid"));

                farmaciaregistadaXLS.add(farmaciasRegistadasXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return farmaciaregistadaXLS;

    }

    public List<AbsenteeForSupportCall> getLostToFallowUp(Date dataInicial, Date dataFinal, String clinicid) {

        List<AbsenteeForSupportCall> lostToFollowUpXLS = new ArrayList<AbsenteeForSupportCall>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        String startDate = dateFormat.format(dataInicial);

        String endDate = dateFormat.format(dataFinal);

        try {
            conecta(iDartProperties.hibernateUsername,
                    iDartProperties.hibernatePassword);

            String query = "select " +
                    "pat.patientid as patID, " +
                    "(pat.lastname||', '|| pat.firstnames) as name, " +
                    " pat.nextofkinname as supportername, " +
                    "pat.nextofkinphone as supporterphone, " +
                    "pat.cellphone as cellno, " +
                    "date_part('year',age(pat.dateofbirth)) as age, " +
                    "app.appointmentDate::date as dateexpected, " +
                    "('" + startDate + "' ::date-app.appointmentDate::date)::integer as dayssinceexpected, " +
                    "CASE " +
                    "    WHEN ((('" + startDate + "' ::date-app.appointmentDate::date) > 59) OR (('" + endDate + "' ::date-app.appointmentDate::date) <= 90) AND app.visitdate::date IS NULL) THEN (app.appointmentDate::date + INTERVAL '60 days') " +
                    "    ELSE " +
                    " CASE " +
                    "     WHEN ((app.appointmentDate::date - app.visitdate::date) > 60) THEN (app.appointmentDate::date + INTERVAL '60 days') " +
                    "              ELSE null " +
                    "     END " +
                    "END " +
                    "  AS datelostfollowup, " +
                    " " +
                    "  CASE " +
                    "    WHEN (app.visitdate::date - app.appointmentdate::date) < 0 THEN app.visitdate::date " +
                    "    ELSE null " +
                    "  END " +
                    "  AS datereturn, " +
                    "max(app.appointmentDate) as ultimaData " +
                    "from patient as pat, appointment as app, patientidentifier as pi,identifiertype as idt " +
                    "where app.patient = pat.id " +
                    "and idt.name = 'NID' " +
                    "and pi.value = pat.patientid " +
                    "and idt.id = pi.type_id " +
                    "and " + clinicid + " = pat.clinic " +
                    "and app.appointmentDate is not null " +
                    "and (app.visitDate is null) " +
                    "and (((app.appointmentDate::date + INTERVAL '60 days') between '" + startDate + "'  and '" + endDate + "' ) OR ((app.appointmentDate::date + INTERVAL '90 days') between '" + startDate + "'  and '" + endDate + "' )) " +
                    "and exists (select prescription.id " +
                    "from prescription " +
                    "where prescription.patient = pat.id " +
                    "and prescription.endDate is null) " +
                    "and exists (select id from episode where episode.patient = pat.id " +
                    "and episode.stopdate is null) " +
                    "group by 1,2,3,4,5,6,7,8,9,10 " +
                    "order by patID asc";


            ResultSet rs = st.executeQuery(query);

            if (rs != null) {

                while (rs.next()) {
                    AbsenteeForSupportCall lostTofallowUp = new AbsenteeForSupportCall();
                    lostTofallowUp.setPatientIdentifier(rs.getString("patID"));
                    lostTofallowUp.setNome(rs.getString("name"));
                    lostTofallowUp.setDataQueFaltouLevantamento(rs.getString("dateexpected"));
                    lostTofallowUp.setDataIdentificouAbandonoTarv(rs.getString("datelostfollowup"));
                    lostTofallowUp.setDataRegressoUnidadeSanitaria(rs.getString("datereturn"));
                    lostTofallowUp.setContacto(rs.getString("cellno"));

                    lostToFollowUpXLS.add(lostTofallowUp);
                }
                rs.close();
            }

            st.close();
            conn_db.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return lostToFollowUpXLS;

    }


    public List<LinhaTerapeuticaXLS> getLinhaTerapeutocaXLS(String startDate, String endDate, StockCenter clinic) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select rt.codigoregime, " +
                " rt.regimeesquema, " +
                " rt.active, " +
                " count(*)  as total_prescricao " +
                " from package pack " +
                " inner join prescription pr on pr.id = pack.prescription " +
                " inner join regimeterapeutico rt on pr.regimeid = rt.regimeid " +
                " where pr.date between '" + startDate + "' and '" + endDate + "' " +
                " group by 1,2,3 " +
                " order by 3 asc";

        List<LinhaTerapeuticaXLS> listLinhaTerapeuticaXLS = new ArrayList<LinhaTerapeuticaXLS>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                LinhaTerapeuticaXLS linhaTerapeuticaXLS = new LinhaTerapeuticaXLS();
                linhaTerapeuticaXLS.setCodigoRegime(rs.getString("codigoregime"));
                linhaTerapeuticaXLS.setRegime(rs.getString("regimeesquema"));
                if (rs.getBoolean("active"))
                    linhaTerapeuticaXLS.setActivo("Activo");
                else
                    linhaTerapeuticaXLS.setActivo("Inactivo");
                linhaTerapeuticaXLS.setContagem(rs.getString("total_prescricao"));

                listLinhaTerapeuticaXLS.add(linhaTerapeuticaXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return listLinhaTerapeuticaXLS;

    }

    public List<PrescricoesDuplicadasXLS> getPrescricoesDuplicadas(String startDate, String endDate, StockCenter clinic) throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        String query = "select distinct pt.id, " +
                "pt.patientid as nid, " +
                "pt.firstnames || ' '|| pt.lastname as nome, " +
                "pe.date as dataPrescricao, " +
                "pe.reasonforupdate, " +
                "rt.regimeesquema, " +
                "lt.linhanome, " +
                "CASE " +
                " WHEN pe.dispensatrimestral = 1 THEN 'DT' " +
                " WHEN pe.dispensasemestral = 1 THEN 'DS' " +
                " ELSE 'DM' " +
                "END AS tipodispensa, " +
                "pe.notes " +
                "from prescription pe " +
                "inner join regimeterapeutico rt on rt.regimeid = pe.regimeid " +
                "inner join linhat lt on lt.linhaid = pe.linhaid " +
                "inner join " +
                "(select pat.id, pat.patientid from patient pat " +
                "inner join ( " +
                " select p.patient, p.date from prescription p " +
                "  inner join package pack on p.id = pack.prescription " +
                "  inner join packageddrugs pds on pds.parentpackage = pack.id " +
                " where p.date between '" + startDate + "' and '" + endDate + "' and pds.amount <> 0 " +
                " group by 1,2 " +
                ") pr on pr.patient = pat.id " +
                "group by 1 " +
                "having count(pat.id) > 1 " +
                "order by 1 " +
                " )pp on pp.id = pe.patient " +
                " inner join patient pt on pt.id = pe.patient " +
                " where pe.date between '" + startDate + "' and '" + endDate + "' ";

        List<PrescricoesDuplicadasXLS> prescricoesDuplicadasXLSList = new ArrayList<PrescricoesDuplicadasXLS>();
        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                PrescricoesDuplicadasXLS prescricoesDuplicadasXLS = new PrescricoesDuplicadasXLS();
                prescricoesDuplicadasXLS.setNid(rs.getString("nid"));
                prescricoesDuplicadasXLS.setNome(rs.getString("nome"));
                prescricoesDuplicadasXLS.setDataPrescricao(rs.getString("dataPrescricao"));
                prescricoesDuplicadasXLS.setTipoPaciente(rs.getString("reasonforupdate"));
                prescricoesDuplicadasXLS.setRegimeTerapeutico(rs.getString("regimeesquema"));
                prescricoesDuplicadasXLS.setLinha(rs.getString("linhanome"));
                prescricoesDuplicadasXLS.setTipoDispensa(rs.getString("tipodispensa"));
                prescricoesDuplicadasXLS.setNotas(rs.getString("notes"));

                prescricoesDuplicadasXLSList.add(prescricoesDuplicadasXLS);
            }
            rs.close();
        }

        st.close();
        conn_db.close();

        return prescricoesDuplicadasXLSList;

    }

    public int idMostUsedDoctor()
            throws SQLException, ClassNotFoundException {

        conecta(iDartProperties.hibernateUsername,
                iDartProperties.hibernatePassword);

        int id = 0;

        String query = " select p.doctor as id, count(*) as maxData " +
                " from Prescription p  " +
                " group by 1 " +
                " order by maxData desc limit 1";

        ResultSet rs = st.executeQuery(query);

        if (rs != null) {

            while (rs.next()) {

                id = rs.getInt("id");

            }
            rs.close();

        }

        return id;
    }

}