package com.rpscans.api.dto;
public record PatientResponse(Long id,String patientCode,String name,String fatherHusbandName,Integer age,String gender,String phone,String email,String address) {}
