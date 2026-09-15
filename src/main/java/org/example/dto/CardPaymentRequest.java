package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CardPaymentRequest(
    @NotBlank String numarCard,
    @Positive double suma,
    @NotBlank String comerciant
) {}
