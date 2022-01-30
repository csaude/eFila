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

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 */
@Entity
public class Clinic implements Serializable {

	@Id
	@GeneratedValue
	private Integer id;

	private boolean mainClinic;

	// Such as for CIPRA, Primary Investigator R.Wood
	private String notes;

	@Column(nullable = false)
	private String code;

	@OneToMany(mappedBy = "clinic")
	@Cascade( { org.hibernate.annotations.CascadeType.ALL,
		org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
		private Set<Patient> patients;

	@OneToMany
	@JoinColumn(name = "clinic")
	private Set<Episode> episodes;

	private String telephone;

	@OneToOne
	private NationalClinics clinicDetails;
	
	@Column(unique = true, nullable = false)
	private String clinicName;

	@Column(nullable = false)
	private String province;

	@Column(nullable = false)
	private String district;

	@Column(nullable = false)
	private String subDistrict;

	@Column(nullable = false)
	private String uuid;

	@Column(nullable = false)
	private String facilityType;

	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE},
			mappedBy="clinics")
			@JoinTable(name = "SiteUser", joinColumns = { @JoinColumn(name = "clinicId") }, inverseJoinColumns = { @JoinColumn(name = "userId") })
			private Set<User> users;


	public Clinic() {
		super();
	}

	/**
	 * Constructor for iDartSite.
	 * @param name String
	 * @param users Set<User>
	 */
	public Clinic(String name, Set<User> users) {

		this.clinicName = name;
		this.users=users;
	}

	/**
	 * Constructor
	 * @param name
	 *            String
	 * @param code
	 * @param telephone
	 * @param notes
	 */
	public Clinic(String name, String code, String telephone, String notes, String province, String district, String subDistrict) {
		this.code = code;
		this.telephone = telephone;
		this.notes = notes;
		this.clinicName = name;
		this.province = province;
		this.district = district;
		this.subDistrict = subDistrict;
		this.uuid = UUID.randomUUID().toString();
		this.users = new HashSet<User>();
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
	 * Method getPatients.
	 * 
	 * @return Set<Patient>
	 */
	public Set<Patient> getPatients() {
		return patients;
	}

	/**
	 * Method getTelephone.
	 * 
	 * @return String
	 */
	public String getTelephone() {
		return telephone;
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
	 * Method setPatients.
	 * 
	 * @param patients
	 *            Set<Patient>
	 */
	public void setPatients(Set<Patient> patients) {
		this.patients = patients;
	}

	/**
	 * Method setTelephone.
	 * 
	 * @param telephone
	 *            String
	 */
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	/**
	 * Method isMainClinic.
	 * 
	 * @return boolean
	 */
	public boolean isMainClinic() {
		return mainClinic;
	}

	/**
	 * Method setMainClinic.
	 * 
	 * @param mainClinic
	 *            boolean
	 */
	public void setMainClinic(boolean mainClinic) {
		this.mainClinic = mainClinic;
	}

	/**
	 * Method getUsers.
	 * @return Set<User>
	 */
	public Set<User> getUsers() {
		return users;
	}

	/**
	 * Method setUsers.
	 * @param users Set<User>
	 */
	public void setUsers(Set<User> users) {
		this.users = users;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the clinicName
	 */
	public String getClinicName() {
		return clinicName;
	}

	/**
	 * @param clinicName the clinicName to set
	 */
	public void setClinicName(String clinicName) {
		this.clinicName = clinicName;
	}

	public void setEpisodes(Set<Episode> episodes) {
		this.episodes = episodes;
	}

	public Set<Episode> getEpisodes() {
		return episodes;
	}

	/**
	 * @return the clinicDetails
	 */
	public NationalClinics getClinicDetails() {
		return clinicDetails;
	}

	/**
	 * @param clinicDetails the clinicDetails to set
	 */
	public void setClinicDetails(NationalClinics clinicDetails) {
		this.clinicDetails = clinicDetails;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getSubDistrict() {
		return subDistrict;
	}

	public void setSubDistrict(String subDistrict) {
		this.subDistrict = subDistrict;
	}

	public String getUuid(){
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "[ "+code+" ] - "+clinicName;
	}

	public String getFacilityType() {
		return facilityType;
	}

	public void setFacilityType(String facilityType) {
		this.facilityType = facilityType;
	}
}
