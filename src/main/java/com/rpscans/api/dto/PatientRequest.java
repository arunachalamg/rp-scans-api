package com.rpscans.api.dto;

import jakarta.validation.constraints.*;

public record PatientRequest(@NotBlank String name, String fatherHusbandName, @Min(0) @Max(120) Integer age,
                             @NotBlank String gender, @NotBlank String phone, @Email String email, String address) {
}
