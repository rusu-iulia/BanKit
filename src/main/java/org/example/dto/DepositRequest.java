package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record DepositRequest(@NotBlank String iban, @Positive double suma) {}
