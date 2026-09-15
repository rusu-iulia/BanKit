package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record EmitCardRequest(
    @NotBlank String iban,
    @NotBlank String pin,
    @NotBlank String tip,
    @PositiveOrZero double limitaContactless,
    @PositiveOrZero double limitaCredit
) {}
