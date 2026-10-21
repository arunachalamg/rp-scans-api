package com.rpscans.api.repository;
import com.rpscans.api.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AppSettingRepository extends JpaRepository<AppSetting,String>{}
