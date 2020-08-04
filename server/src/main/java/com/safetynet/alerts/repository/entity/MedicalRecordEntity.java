package com.safetynet.alerts.repository.entity;

import com.safetynet.alerts.api.model.MedicalRecord;
import com.safetynet.alerts.util.DateUtil;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "medicalrecords")
@NoArgsConstructor
@Data
@EqualsAndHashCode(exclude = "person")
@ToString(exclude = "person")
public class MedicalRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "person_id", updatable = false, nullable = false)
    private Long id;

    @OneToOne(targetEntity = PersonEntity.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", updatable = false, nullable = false, foreignKey = @ForeignKey(name = "fk__medicalrecords__persons"))
    private PersonEntity person;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicalrecord_medications", joinColumns = @JoinColumn(
            name = "medicalrecord_id", foreignKey = @ForeignKey(name = "fk__medicalrecord_medications__medicalrecords")))
    @Column(name = "medication")
    @Fetch(value = FetchMode.SUBSELECT)
    @OnDelete(action = OnDeleteAction.CASCADE) @JoinColumn
    private List<String> medications;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicalrecord_allergies", joinColumns = @JoinColumn(
            name = "medicalrecord_id", foreignKey = @ForeignKey(name = "fk__medicalrecord_allergies__medicalrecords")))
    @Column(name = "allergy")
    @Fetch(value = FetchMode.SUBSELECT)
    @OnDelete(action = OnDeleteAction.CASCADE) @JoinColumn
    private List<String> allergies;

    public Integer calculateAge(@NonNull LocalDate today) {
        return DateUtil.calculateAge(getBirthdate(), today);
    }

    public MedicalRecord toMedicalRecord() {
        PersonEntity personEntity = getPerson();
        return MedicalRecord.builder()
                .personId(personEntity.getId())
                .firstName(personEntity.getFirstName())
                .lastName(personEntity.getLastName())
                .birthdate(getBirthdate())
                .medications(new ArrayList<>(getMedications()))
                .allergies(new ArrayList<>(getAllergies()))
                .build();
    }
}
