package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Tranzactie {
    private static int contorIdTranzactie = 100;
    private int idTranzactie;
    private LocalDateTime dataTranzactie;
    private LocalDateTime dataModificare;
    private double suma;
    private TipTranzactie tip;
    private Account contSursa; // agregare
    private Account contDestinatie;
    private String detalii;
    private StatusTranzactie status;

    // Constructor pentru tranzactii care nu implica un cont destinatie (ex: plati cu cardul, retrageri numerar)
    public Tranzactie(double suma, TipTranzactie tip, Account contSursa, String detalii) {
        if (suma <= 0) {
            throw new IllegalArgumentException("Suma invalidă.");
        }
        if (tip == null) {
            throw new IllegalArgumentException("Tipul tranzacției invalid.");
        }
        if (contSursa == null) {
            throw new IllegalArgumentException("Contul sursă invalid.");
        }
        if (detalii == null || detalii.trim().isEmpty()) {
            throw new IllegalArgumentException("Detalii invalide.");
        }
        this.idTranzactie = contorIdTranzactie++;
        this.dataTranzactie = LocalDateTime.now();
        this.dataModificare = LocalDateTime.now();
        this.suma = suma;
        this.tip = tip;
        this.contSursa = contSursa;
        this.contDestinatie = null;
        this.detalii = detalii;
        this.status = StatusTranzactie.PENDING;
    }

    // Constructor pentru tranzactii care implica un cont destinatie (ex: transferuri)
    public Tranzactie(double suma, TipTranzactie tip, Account contSursa, Account contDestinatie, String detalii) {
        this(suma, tip, contSursa, detalii);
        if (contDestinatie == null) {
            throw new IllegalArgumentException("Contul destinație invalid.");
        }
        this.contDestinatie = contDestinatie;
    }

    public int getIdTranzactie() {
        return idTranzactie;
    }

    public LocalDateTime getDataTranzactie() {
        return dataTranzactie;
    }

    public LocalDateTime getDataModificare() {
        return dataModificare;
    }

    public double getSuma() {
        return suma;
    }

    public TipTranzactie getTip() {
        return tip;
    }

    public Account getContSursa() {
        return contSursa;
    }

    public Account getContDestinatie() {
        return contDestinatie;
    }

    public String getDetalii() {
        return detalii;
    }

    public StatusTranzactie getStatus() {
        return status;
    }

    public void marcheazaCompletata() {
        if (status == StatusTranzactie.ANULATA) {
            throw new IllegalStateException("Nu se poate marca o tranzacție anulată ca completată.");
        }
        this.status = StatusTranzactie.COMPLETATA;
        this.dataModificare = LocalDateTime.now();
    }

    public void anuleazaTranzactie() {
        if (status == StatusTranzactie.COMPLETATA) {
            throw new IllegalStateException("Nu se poate anula o tranzacție deja completată.");
        }
        this.status = StatusTranzactie.ANULATA;
        this.dataModificare = LocalDateTime.now();
    }


    public boolean isCompletata() {
        return status == StatusTranzactie.COMPLETATA;
    }

    public boolean isAnulata() {
        return status == StatusTranzactie.ANULATA;
    }

    public boolean isPending() {
        return status == StatusTranzactie.PENDING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tranzactie that)) return false;
        return Objects.equals(idTranzactie, that.idTranzactie);
    }


    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String dataFormatata = dataTranzactie.format(formatter);
        String infoDestinatie = (contDestinatie != null) 
                ? " în contul " + contDestinatie.getIban() 
                : "";

        return "Tranzacția cu id-ul " + idTranzactie + " a fost efectuată la data de " + dataFormatata +
                ". A fost o tranzacție de tip " + tip + " în valoare de " + suma + " " + contSursa.getValuta() +
                " din contul " + contSursa.getIban() + infoDestinatie + ". Detalii: " + detalii +
                " (Status: " + status.getDescriere() + ")";
    }
}