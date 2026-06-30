package org.example.exceptions;

public class ContNotFoundException extends BanKitException {
    public ContNotFoundException(String iban) {
        super("Contul cu IBAN-ul " + iban + " nu a fost găsit.");
    }
}
