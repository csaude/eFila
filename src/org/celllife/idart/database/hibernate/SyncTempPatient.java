/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.celllife.idart.database.hibernate;

import org.celllife.idart.misc.iDARTUtil;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 *
 * @author colaco
 */
@Entity
@Table(name = "sync_temp_patients")
public class SyncTempPatient {
    @Id
    private Integer id;
    @Column(name = "accountstatus")
    private Boolean accountstatus;
    @Column(name = "cellphone", length = 255)
    private String cellphone;
    @Column(name = "dateofbirth")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateofbirth;
    @Basic(optional = false)
    @Column(name = "clinic", nullable = false)
    private int clinic;
    @Column(name = "clinicname", length = 255)
    private String clinicname;
    @Column(name = "clinicuuid", length = 255)
    private String clinicuuid;
    @Basic(optional = false)
    @Column(name = "mainclinic", nullable = false)
    private int mainclinic;
    @Column(name = "mainclinicname", length = 255)
    private String mainclinicname;
    @Column(name = "mainclinicuuid", length = 255)
    private String mainclinicuuid;
    @Column(name = "firstnames", length = 255)
    private String firstnames;
    @Column(name = "homephone", length = 255)
    private String homephone;
    @Column(name = "lastname", length = 255)
    private String lastname;
    @Column(name = "modified")
    private Character modified;
    @Basic(optional = false)
    @Column(name = "patientid", nullable = false, length = 255)
    private String patientid;
    @Column(name = "province", length = 255)
    private String province;
    @Column(name = "sex")
    private Character sex;
    @Column(name = "syncstatus")
    private Character syncstatus;
    @Column(name = "workphone", length = 255)
    private String workphone;
    @Column(name = "address1", length = 255)
    private String address1;
    @Column(name = "address2", length = 255)
    private String address2;
    @Column(name = "address3", length = 255)
    private String address3;
    @Column(name = "nextofkinname", length = 255)
    private String nextofkinname;
    @Column(name = "nextofkinphone", length = 255)
    private String nextofkinphone;
    @Column(name = "race", length = 255)
    private String race;
    @Column(name = "uuidopenmrs", length = 255)
    private String uuidopenmrs;
    @Column(name = "datainiciotarv", length = 255)
    private String datainiciotarv;
    @Column(name = "syncuuid", length = 255)
    private String syncuuid ;

    @Column(name = "prescriptiondate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date prescriptiondate;
    @Column(name = "duration")
    private Integer duration;
    @Column(name = "prescriptionenddate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date prescriptionenddate;
    @Column(name = "regimenome")
    private String regimenome;
    @Column(name = "linhanome")
    private String linhanome;
    @Basic(optional = false)
    @Column(name = "dispensatrimestral")
    private int dispensatrimestral;
    @Basic(optional = false)
    @Column(name = "dispensasemestral")
    private int dispensasemestral;
    @Column(name = "prescriptionid")
    private String prescriptionid;
    @Column(name = "prescricaoespecial")
    private Character prescricaoespecial;
    @Column(name = "motivocriacaoespecial")
    private String motivocriacaoespecial;

    public SyncTempPatient() {
        super();
        this.id = -1;
        this.syncuuid = UUID.randomUUID().toString();
    }

    /**
     * Method getAccountStatus.
     *
     * @return Boolean
     * @deprecated use use getAccountStatusWithCheck
     */
    @Deprecated
    public Boolean getAccountStatus() {
        return accountstatus;
    }

    /**
     * Method to concatenate the address fields into a single address
     *
     * @return
     */
    public String getFullAddress() {
        return ((address1 == null || "".equals(address1)) ? "" : address1)
                + ((address2 == null || "".equals(address2)) ? "" : "; "
                + address2)
                + ((address3 == null || "".equals(address3)) ? "" : "; "
                + address3);
    }

    /**
     * Method getAge.
     *
     * @return int
     */
    public int getAge() {
        return getAgeAt(null);
    }

    public int getAgeAt(Date date) {
        return iDARTUtil.getAgeAt(getDateofbirth() == null ? new Date()
                : getDateofbirth(), date);
    }

    /**
     * Method getClinic.
     *
     * @return Clinic
     */
    public Integer getCurrentclinic() {
        return clinic;
    }

    public char getSyncstatus(){
            return syncstatus;
    }

    public void setSyncstatus(char syncstatus) {
        this.syncstatus = syncstatus;
    }


    public Boolean getAccountstatus() {
        return accountstatus;
    }

    public void setAccountstatus(Boolean accountstatus) {
        this.accountstatus = accountstatus;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public Date getDateofbirth() {
        return dateofbirth;
    }

    public void setDateofbirth(Date dateofbirth) {
        this.dateofbirth = dateofbirth;
    }

    public int getClinic() {
        return clinic;
    }

    public void setClinic(int clinic) {
        this.clinic = clinic;
    }

    public String getClinicname() {
        return clinicname;
    }

    public void setClinicname(String clinicname) {
        this.clinicname = clinicname;
    }

    public int getMainclinic() {
        return mainclinic;
    }

    public void setMainclinic(int mainclinic) {
        this.mainclinic = mainclinic;
    }

    public String getFirstnames() {
        return firstnames;
    }

    public void setFirstnames(String firstnames) {
        this.firstnames = firstnames;
    }

    public String getHomephone() {
        return homephone;
    }

    public void setHomephone(String homephone) {
        this.homephone = homephone;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Character getModified() {
        return modified;
    }

    public void setModified(Character modified) {
        this.modified = modified;
    }

    public String getPatientid() {
        return patientid;
    }

    public void setPatientid(String patientid) {
        this.patientid = patientid;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public Character getSex() {
        return sex;
    }

    public void setSex(Character sex) {
        this.sex = sex;
    }

    public String getWorkphone() {
        return workphone;
    }

    public void setWorkphone(String workphone) {
        this.workphone = workphone;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getAddress3() {
        return address3;
    }

    public void setAddress3(String address3) {
        this.address3 = address3;
    }

    public String getNextofkinname() {
        return nextofkinname;
    }

    public void setNextofkinname(String nextofkinname) {
        this.nextofkinname = nextofkinname;
    }

    public String getNextofkinphone() {
        return nextofkinphone;
    }

    public void setNextofkinphone(String nextofkinphone) {
        this.nextofkinphone = nextofkinphone;
    }

    public String getRace() {
        return race;
    }

    public void setRace(String race) {
        this.race = race;
    }

    public String getUuid() {
        return uuidopenmrs;
    }

    public void setUuid(String uuid) {
        this.uuidopenmrs = uuid;
    }

    public String getSyncuuid() {
        return syncuuid;
    }

    public void setSyncuuid(String syncuuid) {
        this.syncuuid = syncuuid;
    }

    public String getDatainiciotarv() {
        return datainiciotarv;
    }

    public void setDatainiciotarv(String datainiciotarv) {
        this.datainiciotarv = datainiciotarv;
    }

    @Override
    public String toString() {
        return getFirstnames() + " " + getLastname() + " - "+ getPatientid();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMainclinicname() {
        return mainclinicname;
    }

    public void setMainclinicname(String mainclinicname) {
        this.mainclinicname = mainclinicname;
    }

    public String getClinicuuid() {
        return clinicuuid;
    }

    public void setClinicuuid(String clinicuuid) {
        this.clinicuuid = clinicuuid;
    }

    public String getMainclinicuuid() {
        return mainclinicuuid;
    }

    public void setMainclinicuuid(String mainclinicuuid) {
        this.mainclinicuuid = mainclinicuuid;
    }

    public void setSyncstatus(Character syncstatus) {
        this.syncstatus = syncstatus;
    }

    public String getUuidopenmrs() {
        return uuidopenmrs;
    }

    public void setUuidopenmrs(String uuidopenmrs) {
        this.uuidopenmrs = uuidopenmrs;
    }

    public Date getPrescriptiondate() {
        return prescriptiondate;
    }

    public void setPrescriptiondate(Date prescriptiondate) {
        this.prescriptiondate = prescriptiondate;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Date getPrescriptionenddate() {
        return prescriptionenddate;
    }

    public void setPrescriptionenddate(Date prescriptionenddate) {
        this.prescriptionenddate = prescriptionenddate;
    }

    public String getRegimenome() {
        return regimenome;
    }

    public void setRegimenome(String regimenome) {
        this.regimenome = regimenome;
    }

    public String getLinhanome() {
        return linhanome;
    }

    public void setLinhanome(String linhanome) {
        this.linhanome = linhanome;
    }

    public int getDispensatrimestral() {
        return dispensatrimestral;
    }

    public void setDispensatrimestral(int dispensatrimestral) {
        this.dispensatrimestral = dispensatrimestral;
    }

    public int getDispensasemestral() {
        return dispensasemestral;
    }

    public void setDispensasemestral(int dispensasemestral) {
        this.dispensasemestral = dispensasemestral;
    }

    public String getPrescriptionid() {
        return prescriptionid;
    }

    public void setPrescriptionid(String prescriptionid) {
        this.prescriptionid = prescriptionid;
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
}
