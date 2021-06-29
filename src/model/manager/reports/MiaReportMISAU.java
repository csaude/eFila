package model.manager.reports;


import model.manager.excel.conversion.exceptions.ReportException;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.database.hibernate.User;
import org.eclipse.swt.widgets.Shell;

import java.text.SimpleDateFormat;
import java.util.*;

public class MiaReportMISAU extends AbstractJasperReport {

    private final StockCenter stockCenter;
    private final Date theEndDate;
    private Date theStartDate;


    public MiaReportMISAU(Shell parent, StockCenter stockCenter, Date theStartDate, Date theEndDate) {
        super(parent);
        this.stockCenter = stockCenter;
        this.theStartDate = theStartDate;
        this.theEndDate = theEndDate;
    }

    @Override
    protected void generateData() throws ReportException {
    }

    @Override
    protected Map<String, Object> getParameterMap() throws ReportException {

        ConexaoJDBC conn = new ConexaoJDBC();

        Map<String, Object> map = new HashMap<String, Object>();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            //Total de pacientes que levantaram arv 20 a 20
            Map mapaDoMMIA = conn.MMIA(dateFormat.format(theStartDate),dateFormat.format(theEndDate));

            int totalpacientestransito = Integer.parseInt(mapaDoMMIA.get("totalpacientestransito").toString());
            int totalpacientesinicio = Integer.parseInt(mapaDoMMIA.get("totalpacientesinicio").toString());
            int totalpacientesmanter = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanter").toString());
            int totalpacientesalterar = Integer.parseInt(mapaDoMMIA.get("totalpacientesalterar").toString());
            int totalpacientestransferidoDe = Integer.parseInt(mapaDoMMIA.get("totalpacientestransferidoDe").toString());
            int mesesdispensadosparaDM = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDM").toString());
            int mesesdispensadosparaDT = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDT").toString());
            int mesesdispensadosparaDS = Integer.parseInt(mapaDoMMIA.get("mesesdispensadosparaDS").toString());
            int totalpacientesmanterTransporte = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanterTransporte").toString());
            int totalpacientesppe = Integer.parseInt(mapaDoMMIA.get("totalpacientesppe").toString());
            int totallinhas1 = Integer.parseInt(mapaDoMMIA.get("totallinhas1").toString());
            int totallinhas2 = Integer.parseInt(mapaDoMMIA.get("totallinhas2").toString());
            int totallinhas3 = Integer.parseInt(mapaDoMMIA.get("totallinhas3").toString());
            int totalpacientesprep = Integer.parseInt(mapaDoMMIA.get("totalpacientesprep").toString());
            int totalpacientesCE = Integer.parseInt(mapaDoMMIA.get("totalpacientesCE").toString());
            int totalpacienteptv = Integer.parseInt(mapaDoMMIA.get("totalpacienteptv").toString());
            int mesesdispensados = Integer.parseInt(mapaDoMMIA.get("mesesdispensados").toString());
            int pacientesEmTarv = Integer.parseInt(mapaDoMMIA.get("pacientesEmTarv").toString());
            int adultosEmTarv = Integer.parseInt(mapaDoMMIA.get("adultosEmTarv").toString());
            int pediatrico04EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico04EmTARV").toString());
            int pediatrico59EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico59EmTARV").toString());
            int pediatrico1014EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico1014EmTARV").toString());

            map.put("stockCenterId", new Integer(stockCenter.getId()));
            map.put("date", theStartDate);
            map.put("dateFormat", dateFormat.format(theStartDate));
            map.put("monthStart", dateFormat.format(theStartDate));

            User localUser = LocalObjects.getUser(getHSession());

            map.put("username", localUser.getUsername());
            map.put("monthEnd", dateFormat.format(theEndDate));
            map.put("dateEnd", theEndDate);
            map.put("stockCenterName", stockCenter.getStockCenterName());
            map.put("path", getReportPath());
//		    map.put("totalpacientesfarmacia", String.valueOf(totalpacientesfarmacia));
            map.put("facilityName", LocalObjects.currentClinic.getClinicName());
            map.put("pharmacist1", LocalObjects.pharmacy.getPharmacist());
            map.put("pharmacist2", LocalObjects.pharmacy.getAssistantPharmacist());
            map.put("totalpacientesinicio", String.valueOf(totalpacientesinicio));
            map.put("totalpacientesmanter", String.valueOf(totalpacientesmanter));
            map.put("totalpacientesalterar", String.valueOf(totalpacientesalterar));
            map.put("totalpacientestransito", String.valueOf(totalpacientestransito));
            map.put("totalpacientestransferidoDe", String.valueOf(totalpacientestransferidoDe));

            map.put("mesesdispensadosparaDM", String.valueOf(mesesdispensadosparaDM));
            map.put("mesesdispensadosparaDT", String.valueOf(mesesdispensadosparaDT));
            map.put("mesesdispensadosparaDS", String.valueOf(mesesdispensadosparaDS));
            map.put("mesesdispensados", String.valueOf(mesesdispensadosparaDM+mesesdispensadosparaDT+mesesdispensadosparaDS));

            map.put("totalpacientesadulto", String.valueOf(adultosEmTarv));
            map.put("totalpacientes04", String.valueOf(pediatrico04EmTARV));
            map.put("totalpacientes59", String.valueOf(pediatrico59EmTARV));
            map.put("totalpacientes1014", String.valueOf(pediatrico1014EmTARV));

            map.put("totallinhas1", String.valueOf(totallinhas1));
            map.put("totallinhas2", String.valueOf(totallinhas2));
            map.put("totallinhas3", String.valueOf(totallinhas3));
            map.put("totallinhas", String.valueOf(totallinhas3 + totallinhas2 + totallinhas1));

            map.put("totalpacientesppe", String.valueOf(totalpacientesppe));
            map.put("totalpacientesCE", String.valueOf(totalpacientesCE));
            map.put("totalpacientesprep", String.valueOf(totalpacientesprep));
            map.put("pacientesEmTarv", String.valueOf(pacientesEmTarv));

            map.put("dataelaboracao", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            map.put("mes", mesPortugues(theStartDate));
            map.put("mes2", mesPortugues(theEndDate));
            map.put("diseaseType", MiaReport.TARV_MIA_REPORT);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }


    @Override
    protected String getReportFileName() {
        return "MmiaReportNovoMISAU";
    }


    private String mesPortugues(Date data) {

        String mes = "";

        Calendar calendar = new GregorianCalendar();
        Date trialTime = data;
        calendar.setTime(trialTime);


        int mesint = calendar.get(Calendar.MONTH);

       log.trace(mesint);

        switch (mesint) {
            case 0:
                mes = "Janeiro";
                break;
            case 1:
                mes = "Fevereiro";
                break;
            case 2:
                mes = "Março";
                break;
            case 3:
                mes = "Abril";
                break;
            case 4:
                mes = "Maio";
                break;
            case 5:
                mes = "Junho";
                break;
            case 6:
                mes = "Julho";
                break;
            case 7:
                mes = "Agosto";
                break;
            case 8:
                mes = "Setembro";
                break;
            case 9:
                mes = "Outubro";
                break;
            case 10:
                mes = "Novembro";
                break;
            case 11:
                mes = "Dezembro";
                break;
            default:
                mes = "";
                break;
        }

        return mes;
    }
}