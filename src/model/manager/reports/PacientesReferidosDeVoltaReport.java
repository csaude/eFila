package model.manager.reports;

import model.manager.excel.conversion.exceptions.ReportException;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.hibernate.StockCenter;
import org.celllife.idart.database.hibernate.User;
import org.eclipse.swt.widgets.Shell;

import java.text.SimpleDateFormat;
import java.util.*;

public class PacientesReferidosDeVoltaReport extends AbstractJasperReport {

    private final StockCenter stockCenter;
    private final Date theEndDate;
    private Date theStartDate;


    public PacientesReferidosDeVoltaReport(Shell parent, StockCenter stockCenter, Date theStartDate,
                                    Date theEndDate) {
        super(parent);
        this.stockCenter = stockCenter;
        this.theStartDate=theStartDate;
        this.theEndDate = theEndDate;
    }

    @Override
    protected void generateData() throws ReportException {
    }

    @Override
    protected Map<String, Object> getParameterMap() throws ReportException {

        // Set the parameters for the report
        Map<String, Object> map = new HashMap<String, Object>();

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd"); // TODO Auto-generated catch block
        map.put("stockCenterId", new Integer(stockCenter.getId()));
        map.put("date", theStartDate);
        User localUser = LocalObjects.getUser(getHSession());
        map.put("username",localUser.getUsername());
        map.put("dateEnd", theEndDate);
        map.put("path", getReportPath());

        return map;
    }


    @Override
    protected String getReportFileName() {
        return "PacientesReferidosDeVolta";
    }



    private String mesPortugues(Date data )
    {

        String mes="";

        Calendar calendar = new GregorianCalendar();
        Date trialTime = data;
        calendar.setTime(trialTime);


        int mesint =calendar.get(Calendar.MONTH);
        System.out.println(mesint);
        switch(mesint)
        {
            case 0: mes="Janeiro";break;
            case 1: mes="Fevereiro";break;
            case 2: mes="Março";break;
            case 3: mes="Abril";break;
            case 4: mes="Maio";break;
            case 5: mes="Junho";break;
            case 6: mes="Julho";break;
            case 7: mes="Agosto";break;
            case 8: mes="Setembro";break;
            case 9: mes="Outubro";break;
            case 10: mes="Novembro";break;
            case 11: mes="Dezembro";break;
            default:mes="";break;

        }
        return mes;
    }

}

