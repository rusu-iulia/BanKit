package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateClientRequest(
    @NotBlank String tip,
    @NotBlank String adresa,
    @NotBlank String parola,
    @NotBlank String abonament,
    String nume,
    String prenume,
    String cnp,
    String denumire,
    String cui,
    String reprezentant
) {}
