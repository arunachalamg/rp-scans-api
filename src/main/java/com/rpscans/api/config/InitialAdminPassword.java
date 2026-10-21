package com.rpscans.api.config;
import com.rpscans.api.entity.*;
import com.rpscans.api.repository.*;
import com.rpscans.api.service.PasswordHasher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
@Component
public class InitialAdminPassword implements CommandLineRunner {
 private final StaffRepository staff;
 private final StaffPermissionRepository permissions;
 private final StaffCredentialRepository credentials;
 private final PasswordHasher hasher;
 @Value("${app.bootstrap-admin-username:}") private String username;
 @Value("${app.bootstrap-admin-password:}") private String password;
 public InitialAdminPassword(StaffRepository s,StaffPermissionRepository p,StaffCredentialRepository c,PasswordHasher h){
  staff=s;permissions=p;credentials=c;hasher=h;
 }
 @Override public void run(String... args){
  if(username.isBlank()||password.isBlank())return;
  if(password.length()<12)throw new IllegalStateException("Bootstrap admin password must be at least 12 characters");
  Staff s=staff.findByUsernameIgnoreCase(username).orElseThrow(()->new IllegalStateException("Bootstrap username does not exist in staff"));
  if(!"Admin".equalsIgnoreCase(s.role))throw new IllegalStateException("Bootstrap user must have Admin role");
  if(!credentials.existsById(s.id)){
   StaffCredential c=new StaffCredential();c.staffId=s.id;c.passwordHash=hasher.hash(password);credentials.save(c);
  }
  StaffPermission p=permissions.findById(s.id).orElseGet(()->{StaffPermission value=new StaffPermission();value.staffId=s.id;return value;});
  p.loginAccess=true;permissions.save(p);
 }
}
