package org.example.exceptions;

public class PuncteInsuficienteException extends BanKitException {
    public PuncteInsuficienteException(int disponibile, int necesare) {
        super("Puncte insuficiente: ai " + disponibile + " dar sunt necesare " + necesare + ".");
    }
}
