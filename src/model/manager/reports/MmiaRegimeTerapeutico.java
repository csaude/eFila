package model.manager.reports;

public class MmiaRegimeTerapeutico {

    private String codigo;

    private String regimeTerapeutico;

    private String totalDoentes;

    private String totalDoentesFarmaciaComunitaria;

    private String totalDoentesPREP;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getRegimeTerapeutico() {
        return regimeTerapeutico;
    }

    public void setRegimeTerapeutico(String regimeTerapeutico) {
        this.regimeTerapeutico = regimeTerapeutico;
    }

    public String getTotalDoentes() {
        return totalDoentes;
    }

    public void setTotalDoentes(String totalDoentes) {
        this.totalDoentes = totalDoentes;
    }

    public String getTotalDoentesFarmaciaComunitaria() {
        return totalDoentesFarmaciaComunitaria;
    }

    public void setTotalDoentesFarmaciaComunitaria(String totalDoentesFarmaciaComunitaria) {
        this.totalDoentesFarmaciaComunitaria = totalDoentesFarmaciaComunitaria;
    }

    public String getTotalDoentesPREP() {
        return totalDoentesPREP;
    }

    public void setTotalDoentesPREP(String totalDoentesPREP) {
        this.totalDoentesPREP = totalDoentesPREP;
    }
}
