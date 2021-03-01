package model.manager.reports;


import model.manager.excel.conversion.exceptions.ReportException;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.database.hibernate.User;
import org.eclipse.swt.widgets.Shell;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MiaReport extends AbstractJasperReport {

    private final StockCenter stockCenter;
    private final String month;
    private final String year;


    public MiaReport(Shell parent, StockCenter stockCenter, String Month, String Year) {
        super(parent);
        this.stockCenter = stockCenter;
        this.month = Month;
        this.year = Year;
    }

    @Override
    protected void generateData() throws ReportException {
    }

    @Override
    protected Map<String, Object> getParameterMap() throws ReportException {

        //total de pacientes
        ConexaoJDBC conn = new ConexaoJDBC();

        // Set the parameters for the report
        Map<String, Object> map = new HashMap<String, Object>();

        String startDayStr;
        String endDayStr;

        if (getMes(month) > 9)
            endDayStr = year + "-" + getMes(month) + "-20";
        else
            endDayStr = year + "-0" + getMes(month) + "-20";

        if (getMes(month) == 1)
            startDayStr = (Integer.parseInt(year) - 1) + "-12" + "-21";
        else if (getMes(month) < 11)
            startDayStr = year + "-0" + (getMes(month) - 1) + "-21";
        else
            startDayStr = year + "-" + (getMes(month) - 1) + "-21";

        java.sql.Date theStartDate = java.sql.Date.valueOf(startDayStr);
        java.sql.Date theEndDate = java.sql.Date.valueOf(endDayStr);

        Date startMonth1 = getRetrospectiveDate(theStartDate);
        Date endMonth1 = getRetrospectiveDate(theEndDate);

        Date startMonth2 = getRetrospectiveDate(startMonth1);
        Date endMonth2 = getRetrospectiveDate(endMonth1);

        Date startMonth3 = getRetrospectiveDate(startMonth2);
        Date endMonth3 = getRetrospectiveDate(endMonth2);

        Date startMonth4 = getRetrospectiveDate(startMonth3);
        Date endMonth4 = getRetrospectiveDate(endMonth3);

        Date startMonth5 = getRetrospectiveDate(startMonth4);
        Date endMonth5 = getRetrospectiveDate(endMonth4);

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            //Total de pacientes que levantaram arv 20 a 20
            Map mapaDoMMIA = conn.MMIAACTUALIZADO(dateFormat.format(theStartDate),dateFormat.format(theEndDate));
            Map mapaDoMMIAMes5  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth5), dateFormat.format(endMonth5));
            Map mapaDoMMIAMes4  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth4), dateFormat.format(endMonth4));
            Map mapaDoMMIAMes3  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth3), dateFormat.format(endMonth3));
            Map mapaDoMMIAMes2  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth2), dateFormat.format(endMonth2));
            Map mapaDoMMIAMes1  = conn.MMIA_Actualizado_Dispensas(dateFormat.format(startMonth1), dateFormat.format(endMonth1));

            int totalpacientestransito = Integer.parseInt(mapaDoMMIA.get("totalpacientestransito").toString());
            int totalpacientesinicio = Integer.parseInt(mapaDoMMIA.get("totalpacientesinicio").toString());
            int totalpacientesmanter = Integer.parseInt(mapaDoMMIA.get("totalpacientesmanter").toString());
            int totalpacientesalterar = Integer.parseInt(mapaDoMMIA.get("totalpacientesalterar").toString());
            int totalpacientestransferidoDe = Integer.parseInt(mapaDoMMIA.get("totalpacientestransferidoDe").toString());

            int pacientesdispensadosparaDM = Integer.parseInt(mapaDoMMIA.get("pacientesdispensadosparaDM").toString());
            int pacientesdispensadosparaDT = Integer.parseInt(mapaDoMMIA.get("pacientesdispensadosparaDT").toString());
            int pacientesdispensadosparaDS = Integer.parseInt(mapaDoMMIA.get("pacientesdispensadosparaDS").toString());

            int totallinhas1 = Integer.parseInt(mapaDoMMIA.get("totallinhas1").toString());
            int totallinhas2 = Integer.parseInt(mapaDoMMIA.get("totallinhas2").toString());
            int totallinhas3 = Integer.parseInt(mapaDoMMIA.get("totallinhas3").toString());

            int totalpacientesppe = Integer.parseInt(mapaDoMMIA.get("totalpacientesppe").toString());
            int totalpacientesprep = Integer.parseInt(mapaDoMMIA.get("totalpacientesprep").toString());
            int totalpacientesCE = Integer.parseInt(mapaDoMMIA.get("totalpacientesCE").toString());

            int adultosEmTarv = Integer.parseInt(mapaDoMMIA.get("adultosEmTarv").toString());
            int pediatrico04EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico04EmTARV").toString());
            int pediatrico59EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico59EmTARV").toString());
            int pediatrico1014EmTARV = Integer.parseInt(mapaDoMMIA.get("pediatrico1014EmTARV").toString());
            int pacientesEmTarv = Integer.parseInt(mapaDoMMIA.get("pacientesEmTarv").toString());


            int totalDSM5 = Integer.parseInt(mapaDoMMIAMes5.get("pacientesdispensadosparaDS").toString());
            int totalDSM4 = Integer.parseInt(mapaDoMMIAMes4.get("pacientesdispensadosparaDS").toString());
            int totalDSM3= Integer.parseInt(mapaDoMMIAMes3.get("pacientesdispensadosparaDS").toString());
            int totalDSM2 = Integer.parseInt(mapaDoMMIAMes2.get("pacientesdispensadosparaDS").toString());
            int totalDSM1 = Integer.parseInt(mapaDoMMIAMes1.get("pacientesdispensadosparaDS").toString());
            int totalDTM2 = Integer.parseInt(mapaDoMMIAMes2.get("pacientesdispensadosparaDT").toString());
            int totalDTM1 = Integer.parseInt(mapaDoMMIAMes1.get("pacientesdispensadosparaDT").toString());

            int totalDS = totalDSM1 + totalDSM2 + totalDSM3 + totalDSM4 + totalDSM5 + pacientesdispensadosparaDS;
            int totalDT = totalDTM1 + totalDTM2 + pacientesdispensadosparaDT;

            int totalMes = pacientesdispensadosparaDM + pacientesdispensadosparaDT + pacientesdispensadosparaDS;
            int totalGeral = totalDS + totalDT + pacientesdispensadosparaDM;


            double ajuste = 0;
            double roundedAjuste = 0.0;

            if(totalMes > 0){
                ajuste = (double) totalGeral / totalMes * 100;
            }

            roundedAjuste = (double) Math.round(ajuste * 100) / 100;

            User localUser = LocalObjects.getUser(getHSession());

            map.put("username", localUser.getUsername());
            map.put("monthEnd", dateFormat.format(theEndDate));
            map.put("date", theStartDate);
            map.put("dateEnd", theEndDate);
            map.put("stockCenterName", stockCenter.getStockCenterName());
            map.put("stockCenterId", stockCenter.getId());
            map.put("path", getReportPath());
            map.put("facilityName", LocalObjects.currentClinic.getClinicName());
            map.put("pharmacist1", LocalObjects.pharmacy.getPharmacist());
            map.put("pharmacist2", LocalObjects.pharmacy.getAssistantPharmacist());

            map.put("totalpacientesinicio", String.valueOf(totalpacientesinicio));
            map.put("totalpacientesmanter", String.valueOf(totalpacientesmanter));
            map.put("totalpacientesalterar", String.valueOf(totalpacientesalterar));
            map.put("totalpacientestransito", String.valueOf(totalpacientestransito));
            map.put("totalpacientestransferidoDe", String.valueOf(totalpacientestransferidoDe));

            map.put("mesesdispensadosparaDM", String.valueOf(pacientesdispensadosparaDM));
            map.put("mesesdispensadosparaDT", String.valueOf(pacientesdispensadosparaDT));
            map.put("mesesdispensadosparaDS", String.valueOf(pacientesdispensadosparaDS));
            map.put("pacientesdispensados", String.valueOf(pacientesdispensadosparaDM+pacientesdispensadosparaDT+pacientesdispensadosparaDS));

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

            map.put("totalDSM5", String.valueOf(totalDSM5));
            map.put("totalDSM4", String.valueOf(totalDSM4));
            map.put("totalDSM3", String.valueOf(totalDSM3));
            map.put("totalDSM2", String.valueOf(totalDSM2));
            map.put("totalDSM1", String.valueOf(totalDSM1));
            map.put("totalDTM2", String.valueOf(totalDTM2));
            map.put("totalDTM1", String.valueOf(totalDTM1));
            map.put("totalDS", String.valueOf(totalDS));
            map.put("totalDT", String.valueOf(totalDT));
            map.put("totalMes", String.valueOf(totalMes));
            map.put("totalGeral", String.valueOf(totalGeral));
            map.put("ajuste", roundedAjuste+" %");

            map.put("dataelaboracao", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            map.put("mes", mesPortugues(theStartDate.getMonth()));
            map.put("mes2", mesPortugues(theEndDate.getMonth()));

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return map;
    }


    @Override
    protected String getReportFileName() {
        return "MmiaReportActualizado";
    }

    private int getMes(String mesString) {
        int mes = 0;

        switch (mesString) {
            case "Janeiro":
                mes = 1;
                break;
            case "Fevereiro":
                mes = 2;
                break;
            case "Março":
                mes = 3;
                break;
            case "Abril":
                mes = 4;
                break;
            case "Maio":
                mes = 5;
                break;
            case "Junho":
                mes = 6;
                break;
            case "Julho":
                mes = 7;
                break;
            case "Agosto":
                mes = 8;
                break;
            case "Setembro":
                mes = 9;
                break;
            case "Outubro":
                mes = 10;
                break;
            case "Novembro":
                mes = 11;
                break;
            case "Dezembro":
                mes = 12;
                break;

            default:
                mes = 0;
                break;

        }

        return mes;
    }

    private String mesPortugues(int intMonth) {

        String mes = "";

        switch (intMonth) {
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

    private Date getRetrospectiveDate(Date date){

        Calendar retroDate = Calendar.getInstance();
        retroDate.setTime(date);
        retroDate.add(Calendar.MONTH, -1);

        return retroDate.getTime();

    }

}
