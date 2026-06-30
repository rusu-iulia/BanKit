package org.example;

import java.time.LocalDate;
import java.util.Objects;
import org.example.exceptions.*;

public abstract class Card {
    private String numarCard;
    private String pin;
    private boolean isBlocat;
    private ContCurent contSursa;
    private int tentativePin = 0;
    private double limitaZilnica = 50000;
    private double cheltuialaAzi = 0;
    private LocalDate dataUltimaCheltuiala;

    public Card(String numarCard, String pin) {
        if (!isValidCardNumber(numarCard)) throw new IllegalArgumentException("Număr card invalid.");
        if (!isValidPin(pin)) throw new IllegalArgumentException("PIN invalid.");
        this.numarCard = numarCard;
        this.pin = pin;
        this.isBlocat = false;
    }

    private static boolean isValidCardNumber(String numarCard) {
        return numarCard != null && numarCard.matches("\\d{16}");
    }

    private static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4}");
    }

    public String getNumarCard() { return numarCard; }

    String getPin() { return pin; }

    public ContCurent getContSursa() { return contSursa; }

    public void setContSursa(ContCurent contSursa) { this.contSursa = contSursa; }

    public void verificaPin(String pin) {
        if (isBlocat) throw new CardBlocatException(numarCard);
        if (this.pin.equals(pin)) {
            tentativePin = 0;
        } else {
            tentativePin++;
            if (tentativePin >= 3) {
                isBlocat = true;
                throw new CardBlocatException(numarCard);
            }
            throw new IllegalArgumentException("PIN incorect. Mai ai " + (3 - tentativePin) + " încercări.");
        }
    }

    public void schimbaPin(String pinVechi, String pinNou) {
        verificaPin(pinVechi);
        if (!isValidPin(pinNou)) throw new IllegalArgumentException("PIN-ul nou trebuie să aibă 4 cifre.");
        this.pin = pinNou;
    }

    public boolean isBlocat() { return isBlocat; }

    public void blocheazaCard() { this.isBlocat = true; }

    public void deblocheazaCard() {
        this.isBlocat = false;
        this.tentativePin = 0;
    }

    public void verificaSiInregistreazaCheltuiala(double suma) {
        LocalDate azi = LocalDate.now();
        if (dataUltimaCheltuiala == null || !dataUltimaCheltuiala.equals(azi)) {
            cheltuialaAzi = 0;
            dataUltimaCheltuiala = azi;
        }
        if (cheltuialaAzi + suma > limitaZilnica) {
            throw new LimitaZilnicaDepasitaException(suma, limitaZilnica - cheltuialaAzi);
        }
        cheltuialaAzi += suma;
    }

    public void setLimitaZilnica(double limitaZilnica) {
        if (limitaZilnica <= 0) throw new IllegalArgumentException("Limita zilnică trebuie să fie pozitivă.");
        this.limitaZilnica = limitaZilnica;
    }

    public double getLimitaZilnica() { return limitaZilnica; }
    public double getCheltuialaAzi() { return cheltuialaAzi; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return Objects.equals(numarCard, card.numarCard);
    }

    @Override
    public int hashCode() { return Objects.hash(numarCard); }

    @Override
    public String toString() {
        String maskedCard = "**** **** **** " + numarCard.substring(12);
        return "Cardul " + maskedCard + '\'' + (isBlocat ? " este blocat" : " nu este blocat") + '.';
    }
}

class CardDebit extends Card {
    private double limitaContactless;

    public CardDebit(String numarCard, String pin, double limitaContactless) {
        super(numarCard, pin);
        if (limitaContactless < 0) throw new IllegalArgumentException("Limita contactless nu poate fi negativă.");
        this.limitaContactless = limitaContactless;
    }

    public double getLimitaContactless() { return limitaContactless; }

    public void setLimitaContactless(double limitaContactless) {
        if (limitaContactless < 0) throw new IllegalArgumentException("Limita contactless nu poate fi negativă.");
        this.limitaContactless = limitaContactless;
    }

    @Override
    public String toString() {
        return super.toString() + "\nAcesta este un card de debit cu limita contactless de " + limitaContactless + '.';
    }
}

class CardCredit extends Card {
    private double limitaCredit;
    private double datorieCurenta;

    public CardCredit(String numarCard, String pin, double limitaCredit) {
        super(numarCard, pin);
        if (limitaCredit <= 0) throw new IllegalArgumentException("Limita credit trebuie să fie pozitivă.");
        this.limitaCredit = limitaCredit;
        this.datorieCurenta = 0;
    }

    public double getLimitaCredit() { return limitaCredit; }

    public double getDatorieCurenta() { return datorieCurenta; }

    public boolean potCheltui(double suma) { return (datorieCurenta + suma) <= limitaCredit; }

    public void inregistreazaCheltuiala(double suma) {
        if (potCheltui(suma)) this.datorieCurenta += suma;
    }

    public void platesteDatorie(double suma) {
        if (suma <= 0) throw new IllegalArgumentException("Suma de plătit trebuie să fie pozitivă.");
        if (suma > datorieCurenta) throw new IllegalArgumentException("Suma de plătit nu poate depăși datoria curentă.");
        this.datorieCurenta -= suma;
    }

    void setDatorieCurenta(double datorie) { this.datorieCurenta = datorie; }

    @Override
    public String toString() {
        return super.toString() + "\nAcesta este un card de credit cu limita credit de " + limitaCredit + '.';
    }
}
