package model.manager.reports;

public class DispensaTrimestralSemestral {
	
	private String patientIdentifier;
	
	private String nome;

	private String idade;

	private String regimeTerapeutico;
	
	private String tipoPaciente;
	
	private String dataPrescricao;
	
	private String dataLevantamento;
	
	private String dataProximoLevantamento;

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

	public String getIdade() {
		return idade;
	}

	public void setIdade(String idade) {
		this.idade = idade;
	}

	public String getRegimeTerapeutico() {
		return regimeTerapeutico;
	}

	public void setRegimeTerapeutico(String regimeTerapeutico) {
		this.regimeTerapeutico = regimeTerapeutico;
	}

	public String getTipoPaciente() {
		return tipoPaciente;
	}

	public void setTipoPaciente(String tipoPaciente) {
		this.tipoPaciente = tipoPaciente;
	}

	public String getDataPrescricao() {
		return dataPrescricao;
	}

	public void setDataPrescricao(String dataPrescricao) {
		this.dataPrescricao = dataPrescricao;
	}

	public String getDataLevantamento() {
		return dataLevantamento;
	}

	public void setDataLevantamento(String dataLevantamento) {
		this.dataLevantamento = dataLevantamento;
	}

	public String getDataProximoLevantamento() {
		return dataProximoLevantamento;
	}

	public void setDataProximoLevantamento(String dataProximoLevantamento) {
		this.dataProximoLevantamento = dataProximoLevantamento;
	}
}
