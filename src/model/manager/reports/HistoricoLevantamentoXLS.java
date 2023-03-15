package model.manager.reports;

public class HistoricoLevantamentoXLS {
	
	private String patientIdentifier;
	
	private String nome;
	
	private String apelido;

	private String telefone;

	private String idade;
	
	private String tipoTarv;

	private String tipoPaciente;

	private String regimeTerapeutico;
	
	private String tipoDispensa;

	private String modoDispensa;

	private String proveniencia;

	private String dataLevantamento;
	
	private String dataProximoLevantamento;

	private String clinic;

	private String dispenseSyncStatus;

	private String userName;

	public String getPatientIdentifier() {
		return patientIdentifier;
	}
	
	public void setPatientIdentifier(String patientIdentifier) {
		this.patientIdentifier = patientIdentifier;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}

	public String getTipoTarv() {
		return tipoTarv;
	}

	public void setTipoTarv(String tipoTarv) {
		this.tipoTarv = tipoTarv;
	}

	public String getRegimeTerapeutico() {
		return regimeTerapeutico;
	}

	public void setRegimeTerapeutico(String regimeTerapeutico) {
		this.regimeTerapeutico = regimeTerapeutico;
	}

	public String getTipoDispensa() {
		return tipoDispensa;
	}

	public void setTipoDispensa(String tipoDispensa) {
		this.tipoDispensa = tipoDispensa;
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
	public String getClinic() {
		return clinic;
	}

	public void setClinic(String clinic) {
		this.clinic = clinic;
	}

	public String getTipoPaciente() {
		return tipoPaciente;
	}

	public void setTipoPaciente(String tipoPaciente) {
		this.tipoPaciente = tipoPaciente;
	}

	public String getModoDispensa() {
		return modoDispensa;
	}

	public void setModoDispensa(String modoDispensa) {
		this.modoDispensa = modoDispensa;
	}

	public String getProveniencia() {
		return proveniencia;
	}

	public void setProveniencia(String proveniencia) {
		this.proveniencia = proveniencia;
	}

	public String getIdade() {
		return idade;
	}

	public void setIdade(String idade) {
		this.idade = idade;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getDispenseSyncStatus() {
		return dispenseSyncStatus;
	}

	public void setDispenseSyncStatus(String dispenseSyncStatus) {
		this.dispenseSyncStatus = dispenseSyncStatus;
	}
}
