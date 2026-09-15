package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record TransferRequest(
    @NotBlank String ibanSursa,
    @NotBlank String ibanDestinatie,
    @Positive double suma,
    String detalii
) {}
