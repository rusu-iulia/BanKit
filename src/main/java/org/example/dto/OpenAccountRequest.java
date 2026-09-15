package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record OpenAccountRequest(
    @Positive int idClient,
    @NotBlank String tip,
    @NotBlank String valuta,
    @PositiveOrZero double sold,
    @PositiveOrZero double taxaAdministrare,
    @PositiveOrZero double rataDobanda
) {}
