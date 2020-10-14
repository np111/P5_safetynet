package com.safetynet.alerts.repository.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses", uniqueConstraints = {
        @UniqueConstraint(name = "address", columnNames = {"address"}),
        // @UniqueConstraint(name = "unique_location", columnNames = {"address", "city", "zip"})
})
@NoArgsConstructor
@Data
public class AddressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "zip")
    private String zip;

    @Column(name = "firestation")
    private String firestation;

    public boolean isComplete() {
        return getCity() != null && getZip() != null;
    }
}
