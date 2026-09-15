package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateClientRequest(@NotBlank String adresa, @NotBlank String abonament) {}
