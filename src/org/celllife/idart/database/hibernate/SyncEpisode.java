package org.celllife.idart.database.hibernate;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.celllife.idart.misc.iDARTUtil;
// import org.jetbrains.annotations.NotNull;

import javax.persistence.*;
import java.util.Calendar;
import java.util.Date;

@Entity
@Table(name = "sync_temp_episode")
public class SyncEpisode {

    @Id
    @GeneratedValue
    private Integer id;

    @SerializedName("startdate")
    @Column(name = "startdate")
    private Date startDate;

    @SerializedName("stopdate")
    @Column(name = "stopdate")
    private Date stopDate;

    @SerializedName("startreason")
    @Column(name = "startreason")
    private String startReason;

    @SerializedName("stopreason")
    @Column(name = "stopreason")
    private String stopReason;

    @SerializedName("startnotes")
    @Column(name = "startnotes")
    private String startNotes;

    @SerializedName("stopnotes")
    @Column(name = "stopnotes")
    private String stopNotes;

    @SerializedName("patientuuid")
    @Column(name = "patientuuid")
    private String patientUUID;

    @SerializedName("syncstatus")
    @Column(name = "syncstatus")
    private char syncStatus;

    @SerializedName("usuuid")
    @Column(name = "usuuid")
    private String usuuid;

    @SerializedName("clinicuuid")
    @Column(name = "clinicuuid")
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

    public static SyncEpisode generateFromEpisode(Episode e, Clinic currentClinic, String facilityUUID) {
        SyncEpisode episode = new SyncEpisode();
        episode.setClinicuuid(currentClinic.getUuid());
        episode.setStopReason(iDARTUtil.stringHasValue(e.getStopReason()) ? e.getStopReason() : null);
        Calendar cal = Calendar.getInstance();
        cal.setTime(e.getStartDate());
        cal.add(Calendar.MINUTE, 5);
        Date newDate = cal.getTime();
        episode.setStartReason(e.getStartReason());
        episode.setStopDate(e.getStopDate());
        episode.setStartDate(newDate);
        episode.setStartNotes(iDARTUtil.stringHasValue(e.getStartNotes()) ? e.getStartNotes() : null);
        episode.setStopNotes(iDARTUtil.stringHasValue(e.getStopNotes()) ? e.getStopNotes() : null);
        episode.setPatientUUID(e.getPatient().getUuidopenmrs());
        episode.setUsuuid(facilityUUID);
        episode.setSyncStatus('R');

        return episode;
    }
}
