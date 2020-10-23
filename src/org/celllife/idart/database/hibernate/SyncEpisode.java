package org.celllife.idart.database.hibernate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "sync_temp_episode")
public class SyncEpisode {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "strpickup")
    private Date startDate;
    @Column(name = "stopdate")
    private Date stopDate;
    @Column(name = "startreason")
    private String startReason;
    @Column(name = "stopreason")
    private String stopReason;
    @Column(name = "startnotes")
    private String startNotes;
    @Column(name = "stopnotes")
    private String stopNotes;
    @Column(name = "patientuuid")
    private String patientUUID;
    @Column(name = "syncstatus")
    private char syncStatus;
    @Column(name = "strpickup")
    private String usuuid;
    @Column(name = "strpickup")
    private String clinicuuid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStopDate() {
        return stopDate;
    }

    public void setStopDate(Date stopDate) {
        this.stopDate = stopDate;
    }

    public String getStartReason() {
        return startReason;
    }

    public void setStartReason(String startReason) {
        this.startReason = startReason;
    }

    public String getStopReason() {
        return stopReason;
    }

    public void setStopReason(String stopReason) {
        this.stopReason = stopReason;
    }

    public String getStartNotes() {
        return startNotes;
    }

    public void setStartNotes(String startNotes) {
        this.startNotes = startNotes;
    }

    public String getStopNotes() {
        return stopNotes;
    }

    public void setStopNotes(String stopNotes) {
        this.stopNotes = stopNotes;
    }

    public String getPatientUUID() {
        return patientUUID;
    }

    public void setPatientUUID(String patientUUID) {
        this.patientUUID = patientUUID;
    }

    public char getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(char syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getUsuuid() {
        return usuuid;
    }

    public void setUsuuid(String usuuid) {
        this.usuuid = usuuid;
    }

    public String getClinicuuid() {
        return clinicuuid;
    }

    public void setClinicuuid(String clinicuuid) {
        this.clinicuuid = clinicuuid;
    }
}
