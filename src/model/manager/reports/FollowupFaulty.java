package model.manager.reports;

public class FollowupFaulty {

	private String patientIdentifier;
	
	private String nome;

	private String age;
	
	private String dataQueFaltouLevantamento;
	
	private String dataIdentificouAbandonoTarv;
	
	private String dataRegressouUnidadeSanitaria;

	public String getPatientIdentifier() {
		return patientIdentifier;
	}

	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getDataQueFaltouLevantamento() {
		return dataQueFaltouLevantamento;
	}

	public void setDataQueFaltouLevantamento(String dataQueFaltouLevantamento) {
		this.dataQueFaltouLevantamento = dataQueFaltouLevantamento;
	}

	public String getDataIdentificouAbandonoTarv() {
		return dataIdentificouAbandonoTarv;
	}

	public void setDataIdentificouAbandonoTarv(String dataIdentificouAbandonoTarv) {
		this.dataIdentificouAbandonoTarv = dataIdentificouAbandonoTarv;
	}

	public String getDataRegressouUnidadeSanitaria() {
		return dataRegressouUnidadeSanitaria;
	}

	public void setDataRegressouUnidadeSanitaria(String dataRegressouUnidadeSanitaria) {
		this.dataRegressouUnidadeSanitaria = dataRegressouUnidadeSanitaria;
	}
}
