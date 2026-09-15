package org.example.dto;

import jakarta.validation.constraints.Positive;

public record SetDailyLimitRequest(@Positive double limita) {}
