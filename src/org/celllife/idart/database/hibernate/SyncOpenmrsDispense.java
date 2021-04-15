package org.celllife.idart.database.hibernate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sync_openmrs_dispense")
public class SyncOpenmrsDispense {

    @Id
    @GeneratedValue
    private Integer id;
    @Column(name = "strpickup")
    private String strPickUp;
    private String nid;
    private String uuid;
    @Column(name = "encountertype")
    private String encounterType;
    @Column(name = "strfacility")
    private String strFacility;
    @Column(name = "filauuid")
    private String filaUuid;
    private String provider;
    @Column(name = "regimeuuid")
    private String regimeUuid;
    @Column(name = "regimenanswer")
    private String regimenAnswer;
    @Column(name = "dispensedamountuuid")
    private String dispensedAmountUuid;
    @Column(name = "dosageuuid")
    private String dosageUuid;
    @Column(name = "returnvisituuid")
    private String returnVisitUuid;
    @Column(name = "strnextpickup")
    private String strNextPickUp;
    @Column(name = "dispensemodeanswer")
    private String dispenseModeAnswer;
    @ManyToOne
    @JoinColumn(name = "prescription", nullable = false)
    private Prescription prescription;
    @Column(name = "syncstatus")
    private Character syncstatus;
    @Column(name = "notas")
    private String notas;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStrPickUp() {
        return strPickUp;
    }

    public void setStrPickUp(String strPickUp) {
        this.strPickUp = strPickUp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(String encounterType) {
        this.encounterType = encounterType;
    }

    public String getStrFacility() {
        return strFacility;
    }

    public void setStrFacility(String strFacility) {
        this.strFacility = strFacility;
    }

    public String getFilaUuid() {
        return filaUuid;
    }

    public void setFilaUuid(String filaUuid) {
        this.filaUuid = filaUuid;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getRegimeUuid() {
        return regimeUuid;
    }

    public void setRegimeUuid(String regimeUuid) {
        this.regimeUuid = regimeUuid;
    }

    public String getRegimenAnswer() {
        return regimenAnswer;
    }

    public void setRegimenAnswer(String regimenAnswer) {
        this.regimenAnswer = regimenAnswer;
    }

    public String getDispensedAmountUuid() {
        return dispensedAmountUuid;
    }

    public void setDispensedAmountUuid(String dispensedAmountUuid) {
        this.dispensedAmountUuid = dispensedAmountUuid;
    }

    public String getDosageUuid() {
        return dosageUuid;
    }

    public void setDosageUuid(String dosageUuid) {
        this.dosageUuid = dosageUuid;
    }

    public String getReturnVisitUuid() {
        return returnVisitUuid;
    }

    public void setReturnVisitUuid(String returnVisitUuid) {
        this.returnVisitUuid = returnVisitUuid;
    }

    public String getStrNextPickUp() {
        return strNextPickUp;
    }

    public void setStrNextPickUp(String strNextPickUp) {
        this.strNextPickUp = strNextPickUp;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public Character getSyncstatus() {
        return syncstatus;
    }

    public void setSyncstatus(Character syncstatus) {
        this.syncstatus = syncstatus;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getDispenseModeAnswer() {
        return dispenseModeAnswer;
    }

    public void setDispenseModeAnswer(String dispenseModeAnswer) {
        this.dispenseModeAnswer = dispenseModeAnswer;
    }
}
