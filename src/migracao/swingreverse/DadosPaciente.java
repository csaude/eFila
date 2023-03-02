/*
 * Decompiled with CFR 0_114.
 */
package migracao.swingreverse;

import migracao.entidades.Person;
import migracao.entidades.PersonAddress;
import migracao.entidades.PersonName;
import migracao.entidadesHibernate.ExportDispense.PackageDrugInfoExportService;
import migracao.entidadesHibernate.importPatient.PatientAttributeImportService;
import migracao.entidadesHibernate.importPatient.PatientIdentifierImportService;
import migracao.entidadesHibernate.importPatient.PatientImportService;
import org.celllife.idart.database.hibernate.*;
import org.celllife.idart.database.hibernate.tmp.PackageDrugInfo;

import java.util.List;
import java.util.UUID;

public class DadosPaciente {
    public static Patient InserePaciente(String locationuuid, String patientIdentifier, Person person, PersonName personName, PersonAddress personAddress, Clinic clinic, PatientImportService patientImportService, String idPatient, String numeroTelefone) {

        Patient patient = null;
        PatientIdentifierImportService patientIdentifierImportService = new PatientIdentifierImportService();

        PatientIdentifier importedPatientIdentifier = patientIdentifierImportService.findByIdentifier(patientIdentifier);
        
        Patient importedPatient = patientImportService.findByPatientId(patientIdentifier);
      
        if (importedPatientIdentifier == null) {
            patient =  new Patient();
        }else
             patient = importedPatientIdentifier.getPatient();
           
    //    Patient patient = importedPatient == null ? new Patient() : importedPatient;
        
        if (personName.getMiddleName() != null) {
            patient.setFirstNames(personName.getGivenName() + " " + personName.getMiddleName());
        } else {
            patient.setFirstNames(personName.getGivenName());
        }
        patient.setAccountStatus(Boolean.FALSE);
        if (personAddress != null) {
            patient.setAddress1(personAddress.getAddress2()+" L.:"+personAddress.getAddress6());
            patient.setAddress2(personAddress.getAddress1()+" C.:"+personAddress.getAddress3());
            patient.setAddress3(personAddress.getAddress5());
        }
        patient.setCellphone(numeroTelefone);
        patient.setDateOfBirth(person.getBirthdate());
        patient.setClinic(clinic);
        patient.setNextOfKinName(null);
        patient.setNextOfKinPhone(null);
        patient.setHomePhone(null);
        patient.setLastname(personName.getFamilyName());
        patient.setModified('T');
        patient.setPatientId(patientIdentifier);
        if (personAddress != null) {
            patient.setProvince(personAddress.getStateProvince());
        }
        patient.setSex(person.getGender().toUpperCase().charAt(0));
        patient.setWorkPhone(null);
        patient.setRace(null);
        patient.setUuidopenmrs(person.getUuid());
        patient.setUuidlocationopenmrs(locationuuid);
        if (importedPatientIdentifier == null) {
            patientImportService.persist(patient);
        } else {
            patientImportService.update(patient);
        }
        return patient;
    }

    public static void InserePatientIdentifier(Patient patient, IdentifierType identifierType, String patientIdentifier, PatientIdentifierImportService patientIdentifierImportService) {
        PatientIdentifier importedPatientIdentifier = patientIdentifierImportService.findByIdentifier(patientIdentifier);
        if (importedPatientIdentifier == null) {
            PatientIdentifier patientIdentifierLocal = new PatientIdentifier();
            patientIdentifierLocal.setPatient(patient);
            patientIdentifierLocal.setType(identifierType);
            patientIdentifierLocal.setValue(patientIdentifier);
            patientIdentifierImportService.persist(patientIdentifierLocal);
        } else {
            importedPatientIdentifier.setValue(patientIdentifier);
            importedPatientIdentifier.setValueEdit(importedPatientIdentifier.getValue());
            patientIdentifierImportService.update(importedPatientIdentifier);
        }
    }

    public static void InserePatientAttribute(Patient patient, String dataInicioTarv, AttributeType attributeType, PatientAttributeImportService patientAttributeImportService) {
        String pacienteId = "" + patient.getId();
        PatientAttribute importpatientAttribute = patientAttributeImportService.findByPatientId(pacienteId);
        PatientAttribute patientAttribute = importpatientAttribute == null ? new PatientAttribute() : importpatientAttribute;
        patientAttribute.setPatient(patient);
        patientAttribute.setValue(dataInicioTarv);
        patientAttribute.setType(attributeType);
        if (importpatientAttribute == null) {
            patientAttributeImportService.persist(patientAttribute);
        } else {
            patientAttributeImportService.update(patientAttribute);
        }
    }
    
    
     public void actualizaNidIdart(String nidIdart, String nidOpenmrs, String uuidOpenMRS, PatientIdentifier identifierIdart){
         
        PatientIdentifierImportService patientIdentifierImportServiceIdart = new PatientIdentifierImportService();
        PatientImportService importService = new PatientImportService();
        PackageDrugInfoExportService drugInfoExportService = new PackageDrugInfoExportService();
        
        Patient p = importService.findById(identifierIdart.getPatient().getId());
        
        List<PackageDrugInfo> drugInfos = drugInfoExportService.findAllbyPatientID(nidIdart);
        // Verifica a duplicacao de NIDS....
        PatientIdentifier nidDuplicado = patientIdentifierImportServiceIdart.findByIdentifier(nidOpenmrs);
        if(nidDuplicado != null){
            if(nidDuplicado.getPatient().getId() != p.getId()){
            System.err.println(" O Nid "+nidOpenmrs+" por atribuir ao paciente com NID "+nidIdart+" ja existe para um outro paciente");
        }else{
            if(identifierIdart != null){
                identifierIdart.setValue(nidOpenmrs);
                identifierIdart.setValueEdit(nidIdart);
                patientIdentifierImportServiceIdart.update(identifierIdart);    
            }
            if(p != null){
                p.setPatientId(nidOpenmrs);
                p.setRace("Matched");
                p.setUuidopenmrs(uuidOpenMRS);
                importService.update(p);
            }
            for(PackageDrugInfo drugInfo : drugInfos){
                drugInfo.setPatientId(nidOpenmrs);
                drugInfoExportService.update(drugInfo);
            }
        }
        }else{
            if(identifierIdart != null){
                identifierIdart.setValue(nidOpenmrs);
                identifierIdart.setValueEdit(nidIdart);
                patientIdentifierImportServiceIdart.update(identifierIdart);    
            }
            if(p != null){
                p.setPatientId(nidOpenmrs);
                p.setRace("Matched");
                p.setUuidopenmrs(uuidOpenMRS);
                importService.update(p);
            }
            for(PackageDrugInfo drugInfo : drugInfos){
                drugInfo.setPatientId(nidOpenmrs);
               drugInfoExportService.update(drugInfo);
            }
        }
        
 
     }
     
     String devolveUuid(){
         UUID uuid = UUID.randomUUID();
         return uuid.toString();
     }
    
}

