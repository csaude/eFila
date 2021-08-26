package model.manager.reports;

public class MissedAppointmentsReportReferredXLS {

        private String patientIdentifier;

        private String nome;

        private String dataQueFaltouLevantamento;

        private String dataIdentificouAbandonoTarv;

        private String dataRegressoUnidadeSanitaria;

        private String farmaciaReferencia;

        private String contacto;

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

    public String getDataRegressoUnidadeSanitaria() {
        return dataRegressoUnidadeSanitaria;
    }

    public void setDataRegressoUnidadeSanitaria(String dataRegressoUnidadeSanitaria) {
        this.dataRegressoUnidadeSanitaria = dataRegressoUnidadeSanitaria;
    }

    public String getFarmaciaReferencia() {
        return farmaciaReferencia;
    }

    public void setFarmaciaReferencia(String farmaciaReferencia) {
        this.farmaciaReferencia = farmaciaReferencia;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }
}
