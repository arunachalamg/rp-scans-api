package com.rpscans.api.dto;
import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.LocalDate; import java.util.List;
public record VisitRequest(@NotNull Long patientId,@NotNull LocalDate visitDate,String referringDoctor,String referringStaff,boolean selfReferral,boolean repeatPatient,String clinicalHistory,@NotEmpty List<@Valid InvestigationRequest> investigations,List<@Valid PaymentRequest> payments) {}
