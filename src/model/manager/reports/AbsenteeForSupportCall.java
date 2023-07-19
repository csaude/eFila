package model.manager.reports;

public class AbsenteeForSupportCall {

	private String patientIdentifier;
	
	private String nome;

	private String age;

	private String dataQueFaltouLevantamento;
	
	private String dataIdentificouAbandonoTarv;
	
	private String efectuouLigacao;
	
	private String dataRegressoUnidadeSanitaria;
	
	private String chamadaEfectuada;
	
	private String contacto;
	
	private String listaFaltososSemana;

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

	public String getEfectuouLigacao() {
		return efectuouLigacao;
	}

	public void setEfectuouLigacao(String efectuouLigacao) {
		this.efectuouLigacao = efectuouLigacao;
	}

	public String getDataRegressoUnidadeSanitaria() {
		return dataRegressoUnidadeSanitaria;
	}

	public void setDataRegressoUnidadeSanitaria(String dataRegressoUnidadeSanitaria) {
		this.dataRegressoUnidadeSanitaria = dataRegressoUnidadeSanitaria;
	}

	public String getChamadaEfectuada() {
		return chamadaEfectuada;
	}

	public void setChamadaEfectuada(String chamadaEfectuada) {
		this.chamadaEfectuada = chamadaEfectuada;
	}

	public String getContacto() {
		return contacto;
	}

	public void setContacto(String contacto) {
		this.contacto = contacto;
	}

	public String getListaFaltososSemana() {
		return listaFaltososSemana;
	}

	public void setListaFaltososSemana(String listaFaltososSemana) {
		this.listaFaltososSemana = listaFaltososSemana;
	}
}
