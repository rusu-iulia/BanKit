package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String cod, @NotBlank String parola) {}
