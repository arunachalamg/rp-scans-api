package com.rpscans.api.entity;
import jakarta.persistence.*;
@Entity @Table(name="staff_credentials")
public class StaffCredential {
 @Id @Column(name="staff_id") public Long staffId;
 @Column(name="password_hash",nullable=false,length=512) public String passwordHash;
}
