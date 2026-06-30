package org.example.exceptions;

public class FonduriInsuficienteException extends BanKitException {
    public FonduriInsuficienteException(double suma, String valuta) {
        super("Fonduri insuficiente pentru suma de " + suma + " " + valuta + ".");
    }
}
