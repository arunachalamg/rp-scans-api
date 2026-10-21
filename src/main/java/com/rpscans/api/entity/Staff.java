package com.rpscans.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "staff")
public class Staff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "staff_id", nullable = false, unique = true, length = 30)
    public String staffId;
    @Column(nullable = false) public String name;
    @Column(nullable = false) public String role;
    @Column(nullable = false) public String phone;
    public String email;
    @Column(columnDefinition = "text") public String address;
    @Column(unique = true) public String username;
    @Column(nullable = false) public boolean active = true;
}
