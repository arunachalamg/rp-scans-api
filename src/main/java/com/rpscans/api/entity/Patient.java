package com.rpscans.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;

import java.time.*;


@Data
@Entity
@Table(name = "patients")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "patient_code", unique = true)
    public String patientCode;
    @NotBlank
    @Column(nullable = false)
    public String name;
    public String fatherHusbandName;
    @PositiveOrZero
    public Integer age;
    public String gender;
    public String phone;
    public String email;
    public String address;
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void code() {
        if (patientCode == null && id != null) patientCode = "RP" + String.format("%06d", id);
    }
}
