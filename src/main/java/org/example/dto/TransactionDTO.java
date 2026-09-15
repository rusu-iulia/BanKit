package org.example.dto;

public record TransactionDTO(
    int id,
    String dataTranzactie,
    double suma,
    String tip,
    String ibanSursa,
    String ibanDestinatie,
    String detalii,
    String status,
    String valuta
) {}
