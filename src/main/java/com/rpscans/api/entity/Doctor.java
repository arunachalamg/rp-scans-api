package com.rpscans.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "doctors")
public class Doctor {
 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 public Long id;
 @NotBlank
 @Column(nullable = false, unique = true)
 public String code;
 @NotBlank
 @Column(nullable = false)
 public String name;
 public String hospital;
 public String address;
 public String phone;
 @Column(nullable = false)
 public boolean active = true;
}
