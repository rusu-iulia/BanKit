package org.example.exceptions;

public class ClientNotFoundException extends BanKitException {
    public ClientNotFoundException(int idClient) {
        super("Clientul cu ID-ul " + idClient + " nu a fost găsit.");
    }
}
