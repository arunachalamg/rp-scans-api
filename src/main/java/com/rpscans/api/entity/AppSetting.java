package com.rpscans.api.entity;
import jakarta.persistence.*;
@Entity
@Table(name="app_settings")
public class AppSetting {
  @Id @Column(name="setting_key",length=120) public String key;
  @Column(name="setting_value",nullable=false,columnDefinition="text") public String value;
  public AppSetting(){}
  public AppSetting(String key,String value){this.key=key;this.value=value;}
}
