package org.example.exceptions;

public class CardBlocatException extends BanKitException {
    public CardBlocatException(String numarCard) {
        super("Cardul " + numarCard + " este blocat.");
    }
}
