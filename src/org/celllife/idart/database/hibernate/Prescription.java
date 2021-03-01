/*
 * iDART: The Intelligent Dispensing of Antiretroviral Treatment
 * Copyright (C) 2006 Cell-Life
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License version
 * 2 for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package org.celllife.idart.database.hibernate;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 */
@Entity
public class Prescription {

	@Id
	@GeneratedValue
	private Integer id;

	private int clinicalStage;

	private char current;

	private Date date;
	private Date datainicionoutroservico;

	@ManyToOne
	@JoinColumn(name = "doctor")
	private Doctor doctor;
	
	@ManyToOne
	@JoinColumn(name = "regimeid")
	private RegimeTerapeutico regimeTerapeutico;
	
	
	private String motivoMudanca;

	private int duration;

	private char modified;

	@Transient
	@OneToMany
	@Cascade( { org.hibernate.annotations.CascadeType.ALL,
		org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
		private Set<SyncOpenmrsDispense> syncOpenmrsDispenses;


	@OneToMany(mappedBy = "prescription")
	@Cascade( { org.hibernate.annotations.CascadeType.ALL,
			org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	private Set<Packages> packages;

	@ManyToOne
	@JoinColumn(name = "patient", nullable = false)
	private Patient patient;

	@OneToMany
	@JoinColumn(name = "prescription")
	@IndexColumn(name = "prescribeddrugsindex")
	@Cascade( { org.hibernate.annotations.CascadeType.ALL,
		org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
		private List<PrescribedDrugs> prescribedDrugs;

	private String prescriptionId;

	private String reasonForUpdate;

	private String notes;

	private Double weight;

	private Date endDate;
	
	private String drugTypes;
	
	private int dispensaTrimestral;
	
	private int dispensaSemestral;
	
	private String tipoDT;
	
	private String tipoDS;
	
	private String durationSentence;

	private String tipoDoenca;

	@Transient
	private HashSet<Drug> arvDrugSet;
	
	@ManyToOne
	@JoinColumn(name = "linhaid")
	private LinhaT linha;
	
	private char ppe;
	private char ptv;
	private char tb;
	private char tpc;
	private char tpi;
	private char saaj;
	
	private char gaac;
	private char af;
	private char ca;
	private char fr;
	private char cpn;
	private char ccr;
	private char dc;
	private char prep;
	private char ce;
	private char prescricaoespecial;
	private String motivocriacaoespecial;

	/**
	 * @param clinicalStage
	 * @param current
	 * @param date
	 * @param doctor
	 * @param duration
	 * @param id
	 * @param modified
	 * @param packages
	 * @param patient
	 * @param prescribedDrugs
	 * @param prescriptionId
	 * @param reasonForUpdate
	 * @param notes
	 * @param clinic
	 * @regimeTerapeutico
	 * @param datainicionoutroservico
	 *@param motivoMudanca
	 *@param ppe
	 *@param ptv
	 *@param tb
	 *@param tpi
	 *@param tpc
	 */
	public Prescription(int clinicalStage, char current, Date date,
			Doctor doctor, int duration, int id, char modified,
			Set<Packages> packages, Patient patient,
			List<PrescribedDrugs> prescribedDrugs, String prescriptionId,
			String reasonForUpdate, String notes, RegimeTerapeutico regimeTerapeutico,  Clinic clinic, 
			Date datainicionoutroservico, String motivoMudanca, char ppe, char ptv, char tb, char tpc, 
			char tpi, char saaj, char gaac, char af, char ca, char fr, char cpn, char ccr, int dispensaSemestral,
						String tipoDS, char dc, char prep, char ce, char prescricaoespecial, String motivocriacaoespecial) {
		super();
		this.clinicalStage = clinicalStage;
		this.current = current;
		this.date = date;
		this.doctor = doctor;
		this.duration = duration;
		this.id = id;
		this.modified = modified;
		this.packages = packages;
		this.patient = patient;
		this.prescribedDrugs = prescribedDrugs;
		this.prescriptionId = prescriptionId;
		this.reasonForUpdate = reasonForUpdate;
		this.notes = notes;
		this.regimeTerapeutico = regimeTerapeutico;
		this.datainicionoutroservico = datainicionoutroservico;
		this.motivoMudanca = motivoMudanca;
		this.ppe = ppe;
		this.ptv = ptv;
		this.tb = tb;
		this.tpc = tpc;
		this.tpi = tpi;
		this.saaj = saaj;
		this.gaac = gaac; 
		this.af = af; 
		this.ca = ca; 
		this.fr = fr; 
		this.cpn = cpn; 
		this.ccr = ccr;
		this.dispensaSemestral = dispensaSemestral; 
		this.tipoDS = tipoDS;
		this.dc = dc;
		this.prep = prep;
		this.ce = ce;
		this.prescricaoespecial = prescricaoespecial;
		this.motivocriacaoespecial = motivocriacaoespecial;
	}

	public Prescription() {
		super();

	}

	/**
	 * Method getClinicalStage.
	 * 
	 * @return int
	 */
	public int getClinicalStage() {
		return clinicalStage;
	}

	/**
	 * Method getCurrent.
	 * 
	 * @return char
	 */
	public char getCurrent() {
		return current;
	}

	/**
	 * Method getDate.
	 * 
	 * @return Date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Method getDoctor.
	 * 
	 * @return Doctor
	 */
	public Doctor getDoctor() {
		return doctor;
	}

	/**
	 * Method getDuration.
	 * 
	 * @return int
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * Method getId.
	 * 
	 * @return int
	 */
	public int getId() {
		return id;
	}

	/**
	 * Method getModified.
	 * 
	 * @return char
	 */
	public char getModified() {
		return modified;
	}

	/**
	 * Method getPackages.
	 * 
	 * @return Set<Packages>
	 */
	public Set<Packages> getPackages() {
		return packages;
	}

	/**
	 * Method getPatient.
	 * 
	 * @return Patient
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * Method getPrescribedDrugs.
	 * 
	 * @return List<PrescribedDrugs>
	 */
	public List<PrescribedDrugs> getPrescribedDrugs() {
		return prescribedDrugs;
	}

	/**
	 * Method getPrescriptionId.
	 * 
	 * @return String
	 */
	public String getPrescriptionId() {
		return prescriptionId;
	}

	/**
	 * Method getWeight.
	 * 
	 * @return Double
	 */
	public Double getWeight() {
		return weight;
	}

	/**
	 * Method getReasonForUpdate.
	 * 
	 * @return String
	 */
	public String getReasonForUpdate() {
		return reasonForUpdate;
	}

	/**
	 * Method setClinicalStage.
	 * 
	 * @param clinicalStage
	 *            int
	 */
	public void setClinicalStage(int clinicalStage) {
		this.clinicalStage = clinicalStage;
	}

	/**
	 * Method setCurrent.
	 * 
	 * @param currentt
	 *            char
	 */
	public void setCurrent(char currentt) {
		this.current = currentt;
	}

	/**
	 * Method setDate.
	 * 
	 * @param date
	 *            Date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Method setDoctor.
	 * 
	 * @param doctor
	 *            Doctor
	 */
	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
	}

	/**
	 * Method setDuration.
	 * 
	 * @param duration
	 *            int
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Method setId.
	 * 
	 * @param id
	 *            int
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Method setModified.
	 * 
	 * @param modified
	 *            char
	 */
	public void setModified(char modified) {
		this.modified = modified;
	}

	/**
	 * Method setPackages.
	 * 
	 * @param packages
	 *            Set<Packages>
	 */
	public void setPackages(Set<Packages> packages) {
		this.packages = packages;
	}

	/**
	 * Method setPatient.
	 * 
	 * @param patient
	 *            Patient
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * Method setPrescribedDrugs.
	 * 
	 * @param prescribedDrugs
	 *            List<PrescribedDrugs>
	 */
	public void setPrescribedDrugs(List<PrescribedDrugs> prescribedDrugs) {
		this.prescribedDrugs = prescribedDrugs;
	}

	/**
	 * Method setPrescriptionId.
	 * 
	 * @param prescriptionId
	 *            String
	 */
	public void setPrescriptionId(String prescriptionId) {
		this.prescriptionId = prescriptionId;
	}

	/**
	 * Method setReasonForUpdate.
	 * 
	 * @param reasonForUpdate
	 *            String
	 */
	public void setReasonForUpdate(String reasonForUpdate) {
		this.reasonForUpdate = reasonForUpdate;
	}

	/**
	 * Method getNotes.
	 * 
	 * @return String
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Method setNotes.
	 * 
	 * @param notes
	 *            String
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Method setWeight.
	 * 
	 * @param weight
	 *            Double
	 */
	public void setWeight(Double weight) {
		this.weight = weight;
	}

	/**
	 * Method getEndDate.
	 * 
	 * @return Date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Method setEndDate.
	 * 
	 * @param endDate
	 *            Date
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean containsARVDrug() {
		return !getARVDrugSet().isEmpty();
	}

	public Set<Drug> getARVDrugSet() {
		if (arvDrugSet == null) {
			arvDrugSet = new HashSet<Drug>();
		}
		for (PrescribedDrugs pd : prescribedDrugs) {
			Drug theDrug = pd.getDrug();
			if (theDrug.isARV()) {
				arvDrugSet.add(theDrug);
			}
		}
		return arvDrugSet;
	}

	public boolean isCurrent() {
		return Character.toUpperCase(current) == 'T';
	}

	/**
	 * @return the drugTypes
	 */
	public String getDrugTypes() {
		return drugTypes;
	}

	/**
	 * @param drugTypes the drugTypes to set
	 */
	public void setDrugTypes(String drugTypes) {
		this.drugTypes = drugTypes;
	}

	public RegimeTerapeutico getRegimeTerapeutico() {
		return regimeTerapeutico;
	}

	public void setRegimeTerapeutico(RegimeTerapeutico regimeTerapeutico) {
		this.regimeTerapeutico = regimeTerapeutico;
	}

	public Date getDatainicionoutroservico() {
		return datainicionoutroservico;
	}

	public void setDatainicionoutroservico(Date datainicionoutroservico) {
		this.datainicionoutroservico = datainicionoutroservico;
	}

	public String getMotivoMudanca() {
		return motivoMudanca;
	}

	public void setMotivoMudanca(String motivoMudanca) {
		this.motivoMudanca = motivoMudanca;
	}

	public char getPpe() {
		return ppe;
	}

	public void setPpe(char ppe) {
		this.ppe = ppe;
	}

	public char getPtv() {
		return ptv;
	}

	public void setPtv(char ptv) {
		this.ptv = ptv;
	}
	
	public char getSaaj() {
		return saaj;
	}
	
	public void setSaaj(char saaj) {
		this.saaj = saaj;
	}
	
	public char getTb() {
		return tb;
	}
	
	public void setTb(char tb) {
		this.tb = tb;
	}
	
	public char getGaac() {
		return gaac;
	}

	public void setGaac(char gaac) {
		this.gaac = gaac;
	}

	public char getAf() {
		return af;
	}

	public void setAf(char af) {
		this.af = af;
	}

	public char getCa() {
		return ca;
	}

	public void setCa(char ca) {
		this.ca = ca;
	}

	public char getFr() {
		return fr;
	}

	public void setFr(char fr) {
		this.fr = fr;
	}

	public char getCpn() {
		return cpn;
	}

	public void setCpn(char cpn) {
		this.cpn = cpn;
	}

	public char getCcr() {
		return ccr;
	}

	public void setCcr(char ccr) {
		this.ccr = ccr;
	}

	public char getTpc() {
		return tpc;
	}

	public void setTpc(char tpc) {
		this.tpc = tpc;
	}

	public char getTpi() {
		return tpi;
	}

	public void setTpi(char tpi) {
		this.tpi = tpi;
	}

	public String getTipoDT() {
		return tipoDT;
	}

	public void setTipoDT(String tipoDT) {
		this.tipoDT = tipoDT;
	}

	public int getDispensaTrimestral() {
		return dispensaTrimestral;
	}

	public void setDispensaTrimestral(int dispensaTrimestral) {
		this.dispensaTrimestral = dispensaTrimestral;
	}

	public int getDispensaSemestral() {
		return dispensaSemestral;
	}

	public void setDispensaSemestral(int dispensaSemestral) {
		this.dispensaSemestral = dispensaSemestral;
	}

	public String getTipoDS() {
		return tipoDS;
	}

	public void setTipoDS(String tipoDS) {
		this.tipoDS = tipoDS;
	}

	public char getDc() {
		return dc;
	}

	public void setDc(char dc) {
		this.dc = dc;
	}

	public char getPrep() {
		return prep;
	}

	public void setPrep(char prep) {
		this.prep = prep;
	}

	public char getCe() {
		return ce;
	}

	public void setCe(char ce) {
		this.ce = ce;
	}

	public char getPrescricaoespecial() {
		return prescricaoespecial;
	}

	public void setPrescricaoespecial(char prescricaoespecial) {
		this.prescricaoespecial = prescricaoespecial;
	}

	public String getMotivocriacaoespecial() {
		return motivocriacaoespecial;
	}

	public void setMotivocriacaoespecial(String motivocriacaoespecial) {
		this.motivocriacaoespecial = motivocriacaoespecial;
	}

	public void setLinha(LinhaT linha) {
		this.linha=linha;
		
	}

	public LinhaT getLinha() {
		return this.linha;
		
	}

	public String getDurationSentence() {
		return durationSentence;
	}

	public void setDurationSentence(String durationSentence) {
		this.durationSentence = durationSentence;
	}

	public Set<SyncOpenmrsDispense> getSyncOpenmrsDispenses() {
		return syncOpenmrsDispenses;
	}

	public void setSyncOpenmrsDispenses(Set<SyncOpenmrsDispense> syncOpenmrsDispenses) {
		this.syncOpenmrsDispenses = syncOpenmrsDispenses;
	}

	public String getTipoDoenca() {
		return tipoDoenca;
	}

	public void setTipoDoenca(String tipoDoenca) {
		this.tipoDoenca = tipoDoenca;
	}
}
