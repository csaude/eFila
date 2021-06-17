package model.manager.reports;

import model.manager.excel.conversion.exceptions.ReportException;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.commonobjects.iDartProperties;
import org.celllife.idart.database.dao.ConexaoJDBC;
import org.eclipse.swt.widgets.Shell;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;


public class LivroRegistoDiario extends AbstractJasperReport {
	
	
	private final Date theEndDate;
	private Date theStartDate;
	private boolean inicio;
	private boolean manutencao;
	private boolean alteraccao;
	private boolean trasfereDe;
	private boolean reInicio;
	private String diseaseType;
	private  boolean fim;


	public LivroRegistoDiario(Shell parent, Date theStartDate, Date theEndDate, boolean inicio,boolean manutencao,boolean alteraccao, boolean trasfereDe, boolean reInicio, boolean fim, String diseaseType) {
		super(parent);
		
		this.theStartDate=theStartDate;
		this.theEndDate = theEndDate;
		this.alteraccao=alteraccao;
		this.inicio=inicio;
		this.manutencao=manutencao;
		this.trasfereDe=trasfereDe;
		this.reInicio=reInicio;
		this.diseaseType = diseaseType;
		this.fim = fim;
	}

	@Override
	protected void generateData() throws ReportException {
	}

	@Override
	protected Map<String, Object> getParameterMap() throws ReportException {
		
		// Set the parameters for the report
		Map<String, Object> map = new HashMap<String, Object>();
				
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		map.put("date", theStartDate);
		map.put("dateFormat", dateFormat.format(theStartDate));
		map.put("monthStart", dateFormat.format(theStartDate));
		//calStart.add(Calendar.MONTH, 1);

		map.put("monthEnd", dateFormat.format(theEndDate));
		map.put("dateEnd", theEndDate);
		
		map.put("mes", mesPortugues(theStartDate));
		map.put("mes2",mesPortugues(theEndDate));
		map.put("diseaseType", this.diseaseType);

		ConexaoJDBC con=new ConexaoJDBC();
		
		String query = con.getLivroRegistoDiario(this.inicio, this.manutencao, this.alteraccao,this.trasfereDe,this.reInicio, this.fim, dateFormat.format(theStartDate),dateFormat.format(theEndDate), this.diseaseType);

//		Vector<String> v = new Vector<String>();
//
//		if (this.inicio) {
//			v.add("Inicia");
//			v.add("Transfer de");
//		}
//		if (this.manutencao) {
//			v.add("Manter");
//			v.add("Reiniciar");
//		}
//		if (this.alteraccao) {
//			v.add("Alterar");
//		}
//
//		String condicao = "\'";
//
//		if (v.size() == 5) {
//			for (int j = 0; j < v.size() - 1; j++) {
//				condicao += v.get(j) + "\' , \'";
//			}
//
//			condicao += v.get(v.size() - 1) + "\'";
//		}
//
//		if (v.size() == 2) {
//			for (int j = 0; j < v.size() - 1; j++) {
//				condicao += v.get(j) + "\' , \'";
//			}
//
//			condicao += v.get(v.size() - 1) + "\'";
//		}
//
//		if (v.size() == 1) {
//
//			condicao += v.get(0) + "\'";
//		}

		map.put("query",query);
//		map.put("condicao",condicao);
		map.put("path", getReportPath());
		map.put("provincia","Zambézia");
		map.put("distrito","Nicoadala");
		map.put("facilityName", LocalObjects.currentClinic.getClinicName());

		return map;
	}


	@Override
	protected String getReportFileName() {

		String reportFile = "LivroRegistoDiarioARV";

		if(this.diseaseType.equalsIgnoreCase(iDartProperties.PNCT))
			reportFile = "LivroRegistoDiarioTB";
		else
		if(this.diseaseType.equalsIgnoreCase(iDartProperties.PREP))
			reportFile = "LivroRegistoDiarioPREP";

		return reportFile;
	}
	


private String mesPortugues(Date data )
{
	
	
	String mes="";
	
	 GregorianCalendar calendar = new GregorianCalendar();
	 
	 calendar.setTime(data);
	 
	@SuppressWarnings("static-access")
	int mesint =calendar.MONTH;
	
	switch(mesint)
	{
	case 1: mes="Janeiro";break;
	case 2: mes="Fevereiro";break;
	case 3: mes="Março";break;
	case 4: mes="Abril";break;
	case 5: mes="Maio";break;
	case 6: mes="Junho";break;
	case 7: mes="Julho";break;
	case 8: mes="Agosto";break;
	case 9: mes="Setembro";break;
	case 10: mes="Outubro";break;
	case 11: mes="Novembro";break;
	case 12: mes="Dezembro";break;
	default:mes="";break;


	
	}
	
	return mes;
}
}
