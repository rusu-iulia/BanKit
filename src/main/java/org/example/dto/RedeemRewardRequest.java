package org.example.dto;

import jakarta.validation.constraints.NotBlank;

public record RedeemRewardRequest(@NotBlank String idOferta) {}
