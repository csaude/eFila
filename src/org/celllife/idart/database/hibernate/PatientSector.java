package org.celllife.idart.database.hibernate;

import javax.persistence.*;
import java.util.Date;

@Entity
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
    @JoinColumn(name = "clinic")
    private Clinic clinic;

    @ManyToOne
    @JoinColumn(name = "patient")
    private Patient patient;

    public PatientSector() {
        super();
    }

    public PatientSector(Clinic clinic, Patient patient, Date startDate) {
        this.clinic = clinic;
        this.patient = patient;
        this.startdate = startDate;
    }

    public PatientSector(Clinic clinic, Patient patient, Date endtDate, String endNotes) {
        this.clinic = clinic;
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

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }


}
