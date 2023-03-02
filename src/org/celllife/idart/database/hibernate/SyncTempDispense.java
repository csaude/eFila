/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.celllife.idart.database.hibernate;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author Colaco
 */
@Entity
@Table(name = "sync_temp_dispense")
@NamedQueries({
    @NamedQuery(name = "SyncTempDispense.findAll", query = "SELECT s FROM SyncTempDispense s")})
public class SyncTempDispense implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "id")
    private Integer id;
    @Column(name = "clinicalstage")
    private Integer clinicalstage;
    @Column(name = "current")
    private Character current;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "doctor")
    private Integer doctor;
    @Column(name = "duration")
    private Integer duration;
    @Column(name = "modified")
    private Character modified;
    @Basic(optional = false)
    @Column(name = "patient")
    private int patient;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Column(name = "weight")
    private Double weight;
    @Column(name = "reasonforupdate")
    private String reasonforupdate;
    @Column(name = "notes")
    private String notes;
    @Column(name = "enddate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enddate;
    @Column(name = "drugtypes")
    private String drugtypes;
    @Column(name = "regimenome")
    private String regimenome;
    @Column(name = "datainicionoutroservico")
    @Temporal(TemporalType.TIMESTAMP)
    private Date datainicionoutroservico;
    @Column(name = "motivomudanca")
    private String motivomudanca;
    @Column(name = "linhanome")
    private String linhanome;
    @Column(name = "ppe")
    private Character ppe;
    @Column(name = "ptv")
    private Character ptv;
    @Column(name = "tb")
    private Character tb;
    @Column(name = "tpi")
    private Character tpi;
    @Column(name = "tpc")
    private Character tpc;
    @Basic(optional = false)
    @Column(name = "dispensatrimestral")
    private int dispensatrimestral;
    @Column(name = "tipodt")
    private String tipodt;
    @Column(name = "gaac")
    private Character gaac;
    @Column(name = "af")
    private Character af;
    @Column(name = "ca")
    private Character ca;
    @Column(name = "ccr")
    private Character ccr;
    @Column(name = "saaj")
    private Character saaj;
    @Column(name = "fr")
    private Character fr;
    @Column(name = "amountpertime")
    private String amountpertime;
    @Column(name = "dispensedate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dispensedate;
    @Column(name = "drugname")
    private String drugname;
    @Column(name = "expirydate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expirydate;
    @Column(name = "patientid")
    private String patientid;
    @Column(name = "patientfirstname")
    private String patientfirstname;
    @Column(name = "patientlastname")
    private String patientlastname;
    @Column(name = "dateexpectedstring")
    private String dateexpectedstring;
    @Column(name = "pickupdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date pickupdate;
    @Column(name = "timesperday")
    private Integer timesperday;
    @Column(name = "weekssupply")
    private Integer weekssupply;
    @Column(name = "qtyinhand")
    private String qtyinhand;
    @Column(name = "summaryqtyinhand")
    private String summaryqtyinhand;
    @Column(name = "qtyinlastbatch")
    private String qtyinlastbatch;
    @Column(name = "syncstatus")
    private Character syncstatus;
    @Basic(optional = false)
    @Column(name = "mainclinic")
    private int mainclinic;
    @Basic(optional = false)
    @Column(name = "mainclinicname")
    private String mainclinicname;
    @Column(name = "mainclinicuuid")
    private String mainclinicuuid;
    @Column(name = "prescriptionid")
    private String prescriptionid;
    @Column(name = "tipods")
    private String tipods;
    @Basic(optional = false)
    @Column(name = "dispensasemestral")
    private int dispensasemestral;
    @Column(name = "durationsentence")
    private String durationsentence;
    @Column(name = "dc")
    private Character dc;
    @Column(name = "prep")
    private Character prep;
    @Column(name = "ce")
    private Character ce;
    @Column(name = "cpn")
    private Character cpn;
    @Column(name = "prescricaoespecial")
    private Character prescricaoespecial;
    @Column(name = "motivocriacaoespecial")
    private String motivocriacaoespecial;
    @Column(name = "syncuuid", length = 255)
    private String syncuuid ;
    @Column(name = "uuidopenmrs", length = 255)
    private String uuidopenmrs ;

    public SyncTempDispense() {
    }

    public SyncTempDispense(Integer id) {
        this.id = id;
    }

    public SyncTempDispense(Integer id, int patient, int dispensatrimestral, int mainclinic, String mainclinicname, int dispensasemestral) {
        this.id = id;
        this.patient = patient;
        this.dispensatrimestral = dispensatrimestral;
        this.mainclinic = mainclinic;
        this.mainclinicname = mainclinicname;
        this.dispensasemestral = dispensasemestral;
        this.syncuuid = UUID.randomUUID().toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClinicalstage() {
        return clinicalstage;
    }

    public void setClinicalstage(Integer clinicalstage) {
        this.clinicalstage = clinicalstage;
    }

    public Character getCurrent() {
        return current;
    }

    public void setCurrent(Character current) {
        this.current = current;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getDoctor() {
        return doctor;
    }

    public void setDoctor(Integer doctor) {
        this.doctor = doctor;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Character getModified() {
        return modified;
    }

    public void setModified(Character modified) {
        this.modified = modified;
    }

    public int getPatient() {
        return patient;
    }

    public void setPatient(int patient) {
        this.patient = patient;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getReasonforupdate() {
        return reasonforupdate;
    }

    public void setReasonforupdate(String reasonforupdate) {
        this.reasonforupdate = reasonforupdate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getDrugtypes() {
        return drugtypes;
    }

    public void setDrugtypes(String drugtypes) {
        this.drugtypes = drugtypes;
    }

    public String getRegimenome() {
        return regimenome;
    }

    public void setRegimenome(String regimenome) {
        this.regimenome = regimenome;
    }

    public Date getDatainicionoutroservico() {
        return datainicionoutroservico;
    }

    public void setDatainicionoutroservico(Date datainicionoutroservico) {
        this.datainicionoutroservico = datainicionoutroservico;
    }

    public String getMotivomudanca() {
        return motivomudanca;
    }

    public void setMotivomudanca(String motivomudanca) {
        this.motivomudanca = motivomudanca;
    }

    public String getLinhanome() {
        return linhanome;
    }

    public void setLinhanome(String linhanome) {
        this.linhanome = linhanome;
    }

    public Character getPpe() {
        return ppe;
    }

    public void setPpe(Character ppe) {
        this.ppe = ppe;
    }

    public Character getPtv() {
        return ptv;
    }

    public void setPtv(Character ptv) {
        this.ptv = ptv;
    }

    public Character getTb() {
        return tb;
    }

    public void setTb(Character tb) {
        this.tb = tb;
    }

    public Character getTpi() {
        return tpi;
    }

    public void setTpi(Character tpi) {
        this.tpi = tpi;
    }

    public Character getTpc() {
        return tpc;
    }

    public void setTpc(Character tpc) {
        this.tpc = tpc;
    }

    public int getDispensatrimestral() {
        return dispensatrimestral;
    }

    public void setDispensatrimestral(int dispensatrimestral) {
        this.dispensatrimestral = dispensatrimestral;
    }

    public String getTipodt() {
        return tipodt;
    }

    public void setTipodt(String tipodt) {
        this.tipodt = tipodt;
    }

    public Character getGaac() {
        return gaac;
    }

    public void setGaac(Character gaac) {
        this.gaac = gaac;
    }

    public Character getAf() {
        return af;
    }

    public void setAf(Character af) {
        this.af = af;
    }

    public Character getCa() {
        return ca;
    }

    public void setCa(Character ca) {
        this.ca = ca;
    }

    public Character getCcr() {
        return ccr;
    }

    public void setCcr(Character ccr) {
        this.ccr = ccr;
    }

    public Character getSaaj() {
        return saaj;
    }

    public void setSaaj(Character saaj) {
        this.saaj = saaj;
    }

    public Character getFr() {
        return fr;
    }

    public void setFr(Character fr) {
        this.fr = fr;
    }

    public String getAmountpertime() {
        return amountpertime;
    }

    public void setAmountpertime(String amountpertime) {
        this.amountpertime = amountpertime;
    }

    public Date getDispensedate() {
        return dispensedate;
    }

    public void setDispensedate(Date dispensedate) {
        this.dispensedate = dispensedate;
    }

    public String getDrugname() {
        return drugname;
    }

    public void setDrugname(String drugname) {
        this.drugname = drugname;
    }

    public Date getExpirydate() {
        return expirydate;
    }

    public void setExpirydate(Date expirydate) {
        this.expirydate = expirydate;
    }

    public String getPatientid() {
        return patientid;
    }

    public void setPatientid(String patientid) {
        this.patientid = patientid;
    }

    public String getPatientfirstname() {
        return patientfirstname;
    }

    public void setPatientfirstname(String patientfirstname) {
        this.patientfirstname = patientfirstname;
    }

    public String getPatientlastname() {
        return patientlastname;
    }

    public void setPatientlastname(String patientlastname) {
        this.patientlastname = patientlastname;
    }

    public String getDateexpectedstring() {
        return dateexpectedstring;
    }

    public void setDateexpectedstring(String dateexpectedstring) {
        this.dateexpectedstring = dateexpectedstring;
    }

    public Date getPickupdate() {
        return pickupdate;
    }

    public void setPickupdate(Date pickupdate) {
        this.pickupdate = pickupdate;
    }

    public Integer getTimesperday() {
        return timesperday;
    }

    public void setTimesperday(Integer timesperday) {
        this.timesperday = timesperday;
    }

    public Integer getWeekssupply() {
        return weekssupply;
    }

    public void setWeekssupply(Integer weekssupply) {
        this.weekssupply = weekssupply;
    }

    public String getQtyinhand() {
        return qtyinhand;
    }

    public void setQtyinhand(String qtyinhand) {
        this.qtyinhand = qtyinhand;
    }

    public String getSummaryqtyinhand() {
        return summaryqtyinhand;
    }

    public void setSummaryqtyinhand(String summaryqtyinhand) {
        this.summaryqtyinhand = summaryqtyinhand;
    }

    public String getQtyinlastbatch() {
        return qtyinlastbatch;
    }

    public void setQtyinlastbatch(String qtyinlastbatch) {
        this.qtyinlastbatch = qtyinlastbatch;
    }

    public Character getSyncstatus() {
        return syncstatus;
    }

    public void setSyncstatus(Character syncstatus) {
        this.syncstatus = syncstatus;
    }

    public int getMainclinic() {
        return mainclinic;
    }

    public void setMainclinic(int mainclinic) {
        this.mainclinic = mainclinic;
    }

    public String getMainclinicname() {
        return mainclinicname;
    }

    public void setMainclinicname(String mainclinicname) {
        this.mainclinicname = mainclinicname;
    }

    public String getPrescriptionid() {
        return prescriptionid;
    }

    public void setPrescriptionid(String prescriptionid) {
        this.prescriptionid = prescriptionid;
    }

    public String getTipods() {
        return tipods;
    }

    public void setTipods(String tipods) {
        this.tipods = tipods;
    }

    public int getDispensasemestral() {
        return dispensasemestral;
    }

    public void setDispensasemestral(int dispensasemestral) {
        this.dispensasemestral = dispensasemestral;
    }

    public String getDurationsentence() {
        return durationsentence;
    }

    public void setDurationsentence(String durationsentence) {
        this.durationsentence = durationsentence;
    }

    public Character getDc() {
        return dc;
    }

    public void setDc(Character dc) {
        this.dc = dc;
    }

    public Character getPrep() {
        return prep;
    }

    public void setPrep(Character prep) {
        this.prep = prep;
    }

    public Character getCe() {
        return ce;
    }

    public void setCe(Character ce) {
        this.ce = ce;
    }

    public Character getCpn() {
        return cpn;
    }

    public void setCpn(Character cpn) {
        this.cpn = cpn;
    }

    public Character getPrescricaoespecial() {
        return prescricaoespecial;
    }

    public void setPrescricaoespecial(Character prescricaoespecial) {
        this.prescricaoespecial = prescricaoespecial;
    }

    public String getMotivocriacaoespecial() {
        return motivocriacaoespecial;
    }

    public void setMotivocriacaoespecial(String motivocriacaoespecial) {
        this.motivocriacaoespecial = motivocriacaoespecial;
    }

    public String getSyncuuid() {
        return syncuuid;
    }

    public void setSyncuuid(String syncuuid) {
        this.syncuuid = syncuuid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SyncTempDispense)) {
            return false;
        }
        SyncTempDispense other = (SyncTempDispense) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.celllife.idart.database.hibernate.SyncTempDispense[ id=" + id + " ]";
    }

    public String getUuidopenmrs() {
        return uuidopenmrs;
    }

    public void setUuidopenmrs(String uuidopenmrs) {
        this.uuidopenmrs = uuidopenmrs;
    }

    public String getMainclinicuuid() {
        return mainclinicuuid;
    }

    public void setMainclinicuuid(String mainclinicuuid) {
        this.mainclinicuuid = mainclinicuuid;
    }
}
