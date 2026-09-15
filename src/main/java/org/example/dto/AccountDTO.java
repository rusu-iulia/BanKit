package org.example.dto;

import java.util.List;

public record AccountDTO(
    String iban,
    String tip,
    double sold,
    String valuta,
    double taxaAdministrare,
    double rataDobanda,
    List<CardDTO> carduri
) {}
