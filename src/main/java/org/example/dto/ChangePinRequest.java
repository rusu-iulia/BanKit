package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePinRequest(@NotBlank String pinVechi, @NotBlank String pinNou) {}
