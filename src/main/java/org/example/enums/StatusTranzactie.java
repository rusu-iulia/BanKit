package org.example.enums;

public enum StatusTranzactie {
    PENDING("În așteptare"),
    COMPLETATA("Completată"),
    ANULATA("Anulată");

    private final String descriere;

    StatusTranzactie(String descriere) {
        this.descriere = descriere;
    }

    public String getDescriere() {
        return descriere;
    }
}
