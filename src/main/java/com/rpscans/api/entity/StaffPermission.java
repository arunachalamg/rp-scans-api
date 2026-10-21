package com.rpscans.api.entity;
import jakarta.persistence.*;
@Entity
@Table(name="staff_permissions")
public class StaffPermission {
  @Id @Column(name="staff_id") public Long staffId;
  @Column(name="login_access",nullable=false) public boolean loginAccess=false;
  @Column(name="permissions_json",nullable=false,columnDefinition="text") public String permissionsJson="{}";
}
