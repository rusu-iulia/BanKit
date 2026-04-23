package org.example;

import java.util.Objects;

public abstract class Reward {
    private String idOferta;
    private String numeOferta;
    private int costPuncte;
    private boolean rewardRevendicat;

    public Reward(String idOferta, String numeOferta, int costPuncte) {
        if (idOferta == null || idOferta.trim().isEmpty()) {
            throw new IllegalArgumentException("ID invalid.");
        }
        if (numeOferta == null || numeOferta.trim().isEmpty()) {
            throw new IllegalArgumentException("Numele ofertei e invalid.");
        }
        if (costPuncte <= 0) {
            throw new IllegalArgumentException("Cost invalid.");
        }
        this.idOferta = idOferta;
        this.numeOferta = numeOferta;
        this.costPuncte = costPuncte;
        this.rewardRevendicat = false;
    }

    public String getIdOferta() {
        return idOferta;
    }

    public String getNumeOferta() {
        return numeOferta;
    }

    public int getCostPuncte() {
        return costPuncte;
    }

    public boolean sePoateRevendica(int puncteDisponibile) {
        return !rewardRevendicat && puncteDisponibile >= costPuncte;
    }

    public void revendica() {
        if (rewardRevendicat) {
            throw new IllegalStateException("Recompensa a fost deja folosită.");
        }
        this.rewardRevendicat = true;
    }

    public boolean getRewardRevendicat() {
        return rewardRevendicat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reward reward)) return false;
        return Objects.equals(idOferta, reward.idOferta);
    }
    
    @Override
    public String toString() {
        return "Oferta " + numeOferta + " costă " + costPuncte + " RevPoints și " + (rewardRevendicat ? " a fost revendicată." : " nu a fost revendicată.");
    }
}

class FlightReward extends Reward {
    private String companieAeriana;
    private String destinatie;

    public FlightReward(String idOferta, String numeOferta, int costPuncte, String companieAeriana, String destinatie) {
        super(idOferta, numeOferta, costPuncte);
        if (companieAeriana == null || companieAeriana.trim().isEmpty()) {
            throw new IllegalArgumentException("Compania aeriană invalidă.");
        }
        if (destinatie == null || destinatie.trim().isEmpty()) {
            throw new IllegalArgumentException("Destinația invalidă.");
        }
        this.companieAeriana = companieAeriana;
        this.destinatie = destinatie;
    }

    @Override
    public String toString() {
        return super.toString() + " Aceasta este o ofertă de zbor cu " + companieAeriana + " către " + destinatie;
    }
}

class AccommodationReward extends Reward {
    private String hotel;
    private int numarNopti;

    public AccommodationReward(String idOferta, String numeOferta, int costPuncte, String hotel, int numarNopti) {
        super(idOferta, numeOferta, costPuncte);
        if (hotel == null || hotel.trim().isEmpty()) {
            throw new IllegalArgumentException("Hotelul invalid.");
        }
        if (numarNopti <= 0) {
            throw new IllegalArgumentException("Numărul de nopți trebuie să fie pozitiv.");
        }
        this.hotel = hotel;
        this.numarNopti = numarNopti;
    }

    @Override
    public String toString() {
        return super.toString() + " Aceasta este o ofertă de cazare la " + hotel + " pe o durată de " + numarNopti + " nopți.";
    }
}