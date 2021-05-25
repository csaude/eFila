package model.manager.reports;

import model.manager.excel.conversion.exceptions.ReportException;
import org.celllife.idart.commonobjects.LocalObjects;
import org.celllife.idart.database.hibernate.Patient;
import org.celllife.idart.database.hibernate.Prescription;
import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;
import java.util.Map;

public class PatientHistoryReport extends AbstractJasperReport {

	private final Patient patient;

	public static final String PATIENT_HISTORY_FILA = "patientHistoryFILA";
	public static final String PATIENT_HISTORY_FILT = "patientHistoryFILT";
	public static final String PATIENT_HISTORY_PREP = "patientHistoryPrep";

	private String reportType;

	public PatientHistoryReport(Shell parent, Patient patient, String reportType) {
		super(parent);
		this.patient = patient;
		this.reportType = reportType;
	}

	@Override
	protected void generateData() throws ReportException {
	}

	@Override
	protected Map<String, Object> getParameterMap() throws ReportException {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("patientId", patient.getPatientId());
		map.put("age", patient.getAge());
		map.put("HIBERNATE_SESSION", hSession);
		map.put("facilityName", LocalObjects.pharmacy.getPharmacyName());
		map.put("pharmacist1", LocalObjects.pharmacy.getPharmacist());
		map.put("pharmacist2", LocalObjects.pharmacy.getAssistantPharmacist());
		map.put("path", getReportPath());
		map.put("disease", reportType.equalsIgnoreCase(PATIENT_HISTORY_FILA) ? Prescription.TIPO_DOENCA_TARV : reportType.equalsIgnoreCase(PATIENT_HISTORY_FILT) ?Prescription.TIPO_DOENCA_TB:Prescription.TIPO_DOENCA_PREP);
		return map;
	}

	@Override
	protected String getReportFileName() {
		return this.reportType;
	}

}
