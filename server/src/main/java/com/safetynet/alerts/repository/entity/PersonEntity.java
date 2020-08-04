package com.safetynet.alerts.repository.entity;

import com.safetynet.alerts.api.model.Person;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "persons")
@NoArgsConstructor
@Data
public class PersonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @ManyToOne(targetEntity = AddressEntity.class, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", nullable = false, foreignKey = @ForeignKey(name = "fk__persons__addresses"))
    private AddressEntity address;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email", nullable = false)
    private String email;

    @OneToOne(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MedicalRecordEntity medicalRecord;

    public Person toPerson() {
        AddressEntity addressEntity = getAddress();
        return Person.builder()
                .id(getId())
                .firstName(getFirstName())
                .lastName(getLastName())
                .address(addressEntity.getAddress())
                .city(addressEntity.getCity())
                .zip(addressEntity.getZip())
                .phone(getPhone())
                .email(getEmail())
                .build();
    }

    public Person toCompletePerson(ZonedDateTime now) {
        return toCompletePerson(now, false);
    }

    public Person toCompletePerson(ZonedDateTime now, boolean withMedicalRecords) {
        AddressEntity addressEntity = getAddress();
        MedicalRecordEntity medicalRecord = getMedicalRecord();
        Person.Builder res = Person.builder()
                .id(getId())
                .firstName(getFirstName())
                .lastName(getLastName())
                .address(addressEntity.getAddress())
                .city(addressEntity.getCity())
                .zip(addressEntity.getZip())
                .phone(getPhone())
                .email(getEmail());
        if (medicalRecord != null) {
            res.birthdate(medicalRecord.getBirthdate());
            res.age(now == null ? null : medicalRecord.calculateAge(now.toLocalDate()));
            if (withMedicalRecords) {
                res.medications(new ArrayList<>(medicalRecord.getMedications()));
                res.allergies(new ArrayList<>(medicalRecord.getAllergies()));
            }
        }
        return res.build();
    }
}
