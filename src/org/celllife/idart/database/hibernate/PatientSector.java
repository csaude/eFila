package org.celllife.idart.database.hibernate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "patient_sector")
public class PatientSector {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private Date startdate;

    @Column(nullable = true)
    private Date enddate;

    @Column(nullable = true)
    private String endnotes;

    @ManyToOne
    @JoinColumn(name = "clinicsector")
    private ClinicSector clinicsector;

    @ManyToOne
    @JoinColumn(name = "patient")
    private Patient patient;

    public PatientSector() {
        super();
    }

    public PatientSector(ClinicSector clinicsector, Patient patient, Date startDate) {
        this.clinicsector = clinicsector;
        this.patient = patient;
        this.startdate = startDate;
    }

    public PatientSector(ClinicSector clinicsector, Patient patient, Date endtDate, String endNotes) {
        this.clinicsector = clinicsector;
        this.patient = patient;
        this.enddate = endtDate;
        this.endnotes = endNotes;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public String getEndnotes() {
        return endnotes;
    }

    public void setEndnotes(String endnotes) {
        this.endnotes = endnotes;
    }

    public ClinicSector getClinicsector() {
        return clinicsector;
    }

    public void setClinicsector(ClinicSector clinicsector) {
        this.clinicsector = clinicsector;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }


}
