package org.example.exceptions;

public class LimitaCreditDepasitaException extends BanKitException {
    public LimitaCreditDepasitaException(double suma, double limita) {
        super("Limita de credit (" + limita + ") ar fi depășită de suma " + suma + ".");
    }
}
