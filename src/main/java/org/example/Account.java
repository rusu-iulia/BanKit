package org.example;

import java.util.Objects;

public abstract class Account {
    private String iban;
    private double sold;
    private ValuteAcceptate valuta;
    private Client titular; // agregare: daca un client isi reziliaza contul, clientul poate exista in continuare in sistem, dar fara acel cont
    private Card[] carduri = new Card[5]; // compozitie: un card nu poate exista fara un cont. cand se sterge contul, dispare si cardul
    private int numarCarduri = 0;

    public Account(String iban){
        if (!isValidIban(iban)) {
            throw new IllegalArgumentException("IBAN invalid.");
        }
        this.iban = iban;
        this.sold = 0.0;
        this.valuta = ValuteAcceptate.RON;
    }
    public Account(String iban, double sold, ValuteAcceptate valuta){
        if (!isValidIban(iban)) {
            throw new IllegalArgumentException("IBAN invalid.");
        }
        if (sold < 0) {
            throw new IllegalArgumentException("Soldul contului nu poate fi negativ.");
        }
        this.iban = iban;
        this.sold = sold;
        this.valuta = valuta != null ? valuta : ValuteAcceptate.RON;
    }

    private static boolean isValidIban(String iban) {
        return iban != null && iban.length() == 24 && iban.startsWith("RO") && iban.substring(2).matches("[A-Z0-9]+");
    }
    public String getIban() {
        return iban;
    }

    private void setIban(String iban) {
        if (!isValidIban(iban)) {
            throw new IllegalArgumentException("IBAN invalid.");
        }
        this.iban = iban;
    }

    public double getSold(){
        return sold;
    }

    public Client getTitular() {
        return titular;
    }

    public void setTitular(Client titular) {
        this.titular = titular;
    }

    public void adaugaCard(Card card) {
        if (numarCarduri < carduri.length) {
            carduri[numarCarduri] = card;
            numarCarduri++;
        } else {
            throw new IllegalStateException("S-a atins numărul maxim de carduri pentru acest cont.");
        }
    }

    public Card[] getCarduri() {
        return carduri;
    }
    public void depunere(double suma) {
        if (suma <= 0) {
            throw new IllegalArgumentException("Suma depusă trebuie să fie pozitivă.");
        }
        this.sold += suma;
    }

    public boolean retragere(double suma) {
        if (suma <= 0) {
            throw new IllegalArgumentException("Suma retrasă trebuie să fie pozitivă.");
        }
        if (this.sold < suma) {
            return false;
        }
        this.sold -= suma;
        return true;
    }

    public String getValuta() {
        return valuta.toString();
    }

    public void setValuta(String valuta) {
        this.valuta = ValuteAcceptate.valueOf(valuta);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Account account)) return false;
        return Objects.equals(iban, account.iban);
    }

    @Override
    public String toString() {
        return "Contul cu ibanul " + iban + '\'' +
                " are un sold de " + sold + " " + valuta + '\'';
    }
}

class ContCurent extends Account{
    private double taxaAdministrare;

    public ContCurent(String iban, double sold, ValuteAcceptate valuta, double taxaAdministrare) {
        super(iban, sold, valuta);
        if (taxaAdministrare < 0) {
            throw new IllegalArgumentException("Taxa de administrare nu poate fi negativă.");
        }
        this.taxaAdministrare = taxaAdministrare;
    }

    public double getTaxaAdministrare() {
        return taxaAdministrare;
    }

    public void setTaxaAdministrare(double taxaAdministrare) {
        this.taxaAdministrare = taxaAdministrare;
    }

    public void aplicaTaxaLunara() {
        if (!retragere(taxaAdministrare)) {
            throw new IllegalStateException("Fonduri insuficiente pentru aplicarea taxei lunare");
        }
    }

    @Override
    public String toString() {
        return  super.toString() + " și este un cont curent cu o taxă de administrare de "
                + taxaAdministrare + '\'';
    }
}

class ContDeEconomii extends Account {
    private double rataDobanda;

    public ContDeEconomii(String iban, double sold, ValuteAcceptate valuta, double rataDobanda) {
        super(iban, sold, valuta);
        if (rataDobanda < 0) {
            throw new IllegalArgumentException("Rata dobânzii nu poate fi negativă.");
        }
        this.rataDobanda = rataDobanda;
    }

    public double getRataDobanda() {
        return rataDobanda;
    }

    public void setRataDobanda(double rataDobanda) {
        this.rataDobanda = rataDobanda;
    }

    public void adaugaDobanda() {
        double dobanda = getSold() * rataDobanda;
        depunere(dobanda);
    }

    @Override
    public String toString() {
        return  super.toString() + " este un cont de economii cu o rată dedobândă de "
                + rataDobanda + '\'';
    }
}