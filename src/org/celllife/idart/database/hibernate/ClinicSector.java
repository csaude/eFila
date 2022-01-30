package org.celllife.idart.database.hibernate;

import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
public class ClinicSector {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(unique = true, nullable = false)
    private String sectorname;

    @Column(nullable = true)
    private String telephone;

    @Column(nullable = false)
    private String uuid;

    @OneToMany
    @IndexColumn(name = "clinicSectorIndex")
    private Set<PatientSector> patientSectors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="clinic", referencedColumnName="uuid")
    private Clinic clinic;

    @ManyToOne
    @JoinColumn(name = "clinicSectorType")
    private ClinicSectorType clinicSectorType;

    @Column(nullable = false)
    private String clinicuuid;

    public ClinicSector() {
        super();
    }

    public ClinicSector(Clinic clinic,ClinicSectorType clinicSectorType, String name, String telephone, String code) {
        this.telephone = telephone;
        this.code = code;
        this.sectorname = name;
        this.clinicSectorType = clinicSectorType;
        this.clinic = clinic;
        this.clinicuuid = clinic.getUuid();
        this.uuid = UUID.randomUUID().toString();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSectorname() {
        return sectorname;
    }

    public void setSectorname(String sectorname) {
        this.sectorname = sectorname;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Set<PatientSector> getPatientSectors() {
        return patientSectors;
    }

    public void setPatientSectors(Set<PatientSector> patientSectors) {
        this.patientSectors = patientSectors;
    }

    public Clinic getClinic() {
        return clinic;
    }

    public void setClinic(Clinic clinic) {
        this.clinic = clinic;
    }

    public String getClinicuuid() {
        return clinicuuid;
    }

    public void setClinicuuid(String clinicuuid) {
        this.clinicuuid = clinicuuid;
    }

    public ClinicSectorType getClinicSectorType() {
        return clinicSectorType;
    }

    public void setClinicSectorType(ClinicSectorType clinicSectorType) {
        this.clinicSectorType = clinicSectorType;
    }

    @Override
    public String toString() {
        return sectorname;
    }
}
