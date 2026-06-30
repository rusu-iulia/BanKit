package org.example.enums;

public enum TipAbonament {
    STANDARD(1.0, 0.01),
    PREMIUM(2.0, 0.00),
    BUSINESS_PRO(0.0, 0.00);

    private final double multiplicatorPuncte;
    private final double comisionSchimb;

    TipAbonament(double multiplicatorPuncte, double comisionSchimb) {
        this.multiplicatorPuncte = multiplicatorPuncte;
        this.comisionSchimb = comisionSchimb;
    }

    public double getMultiplicatorPuncte() {
        return multiplicatorPuncte;
    }

    public double getComisionSchimb() {
        return comisionSchimb;
    }
}