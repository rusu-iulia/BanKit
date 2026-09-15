package org.example.dto;

import jakarta.validation.constraints.Positive;

public record CollectSubscriptionRequest(@Positive int idClient, @Positive double suma) {}
