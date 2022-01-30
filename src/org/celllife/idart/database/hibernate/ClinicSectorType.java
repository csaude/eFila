package org.celllife.idart.database.hibernate;

import javax.persistence.*;

@Entity
@Table(name = "clinic_sector_type")
public class ClinicSectorType {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(unique = true, nullable = false)
    private String description;

    public ClinicSectorType() {
        super();
    }

    public ClinicSectorType(String code, String description) {
        this.code = code;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
