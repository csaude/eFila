package model.manager.reports;

public class FollowupFaulty {

	private String patientIdentifier;
	
	private String nome;

	private Integer age;
	
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

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
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
