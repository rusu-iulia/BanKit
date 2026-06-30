package org.example.exceptions;

public class LimitaZilnicaDepasitaException extends BanKitException {
    public LimitaZilnicaDepasitaException(double sumaIncercata, double limitaRamasa) {
        super("Limita zilnică depășită. Suma: " + sumaIncercata + ", limită rămasă azi: " + String.format("%.2f", limitaRamasa) + ".");
    }
}
