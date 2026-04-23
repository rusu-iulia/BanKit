package org.example;

import java.time.LocalDate;
import java.util.Objects;

public abstract class Client implements Comparable<Client>{
    private static int contorIdClient = 100;
    private int idClient;
    private String adresa;
    private Account[] conturi;
    private int puncteRev;
    private LocalDate dataInrolare;
    private TipAbonament abonament;


    public Client(String adresa, LocalDate dataInrolare, TipAbonament abonament) {
        if (adresa == null || adresa.trim().isEmpty()) {
            throw new IllegalArgumentException("Adresă invalidă.");
        }
        if (dataInrolare == null) {
            throw new IllegalArgumentException("Data înrolării invalidă.");
        }
        if (abonament == null) {
            throw new IllegalArgumentException("Abonament invalid.");
        }
        this.idClient = contorIdClient++;
        this.adresa = adresa;
        this.conturi = new Account[10];
        this.puncteRev = 0;
        this.dataInrolare = dataInrolare;
        this.abonament = abonament;
    }

    public int getIdClient() {
        return idClient;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        if (adresa == null || adresa.trim().isEmpty()) {
            throw new IllegalArgumentException("Adresă invalidă.");
        }
        this.adresa = adresa;
    }

    public Account[] getConturi() {
        return conturi.clone(); 
    }

    public void adaugaCont(Account cont) {
        if (cont == null) return;
        for (int i = 0; i < conturi.length; i++) {
            if (conturi[i] == null) {
                conturi[i] = cont;
                return;
            }
        }
        throw new IllegalStateException("Array-ul de conturi e plin (maxim 10 conturi).");
    }

    public void stergeCont(Account cont) {
        for (int i = 0; i < conturi.length; i++) {
            if (conturi[i] != null && conturi[i].equals(cont)) {
                conturi[i] = null;
                return;
            }
        }
    }

    public int getNumarConturi() {
        int count = 0;
        for (Account a : conturi) {
            if (a != null) count++;
        }
        return count;
    }

    public int getPuncteRev() {
        return puncteRev;
    }

    public void setPuncteRev(int puncteRev) {
        if (puncteRev < 0) {
            throw new IllegalArgumentException("Puncte RevPoints invalide.");
        }
        this.puncteRev = puncteRev;
    }

    public void adaugaPuncte(int puncte) {
        if (puncte > 0) {
            this.puncteRev += puncte;
        }
    }

    public LocalDate getDataInrolare() {
        return dataInrolare;
    }

    public void setDataInrolare(LocalDate dataInrolare) {
        if (dataInrolare == null) {
            throw new IllegalArgumentException("Data înrolării invalidă.");
        }
        this.dataInrolare = dataInrolare;
    }


    public TipAbonament getAbonament() {
        return abonament;
    }

    public void setAbonament(TipAbonament abonament) {
        if (abonament == null) {
            throw new IllegalArgumentException("Abonament invalid.");
        }
        this.abonament = abonament;
    }

    public boolean revendicaReward(Reward reward) {
        if (reward.sePoateRevendica(this.puncteRev)) {
            reward.revendica();
            this.puncteRev -= reward.getCostPuncte();
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client client)) return false;
        return idClient == client.idClient;
    }

    @Override
    public String toString() {
        return "Clientul " + idClient +
                ", cu adresa " + adresa +
                ", are " + getNumarConturi() +
                " conturi, " + puncteRev +
                " puncte RevPoints și a semnat contractul la această bancă pe data de '" + dataInrolare + '\'';
    }

    @Override
    public int compareTo(Client o) {
        return this.toString().compareTo(o.toString());
    }
}

class PersoanaFizica extends Client {
    private String nume;
    private String prenume;
    private String CNP;

    public PersoanaFizica(String adresa, LocalDate dataInrolare, TipAbonament abonament, String nume, String prenume, String CNP) {
        super(adresa, dataInrolare, abonament);
        if (nume == null || nume.trim().isEmpty()) {
            throw new IllegalArgumentException("Nume invalid.");
        }
        if (prenume == null || prenume.trim().isEmpty()) {
            throw new IllegalArgumentException("Prenume invalid.");
        }
        if (!isValidCNP(CNP)) {
            throw new IllegalArgumentException("CNP invalid.");
        }
        if (abonament == TipAbonament.BUSINESS_PRO) {
            throw new IllegalArgumentException("Persoanele fizice nu pot avea abonament BUSINESS_PRO.");
        }
        this.nume = nume;
        this.prenume = prenume;
        this.CNP = CNP;
    }

    private static boolean isValidCNP(String CNP) {
        return CNP != null && CNP.matches("\\d{13}");
    }

    public String getNume() {
        return nume;
    }


    public void setNume(String nume) {
        if (nume == null || nume.trim().isEmpty()) {
            throw new IllegalArgumentException("Nume invalid.");
        }
        this.nume = nume;
    }

    public String getPrenume() {
        return prenume;
    }

    public void setPrenume(String prenume) {
        if (prenume == null || prenume.trim().isEmpty()) {
            throw new IllegalArgumentException("Prenume invalid.");
        }
        this.prenume = prenume;
    }

    public String getNumeComplet(){
        return nume + " " + prenume;
    }
    
    public String getCNP() {
        return "****" + CNP.substring(9);
    }

    public void setCNP(String CNP) {
        if (!isValidCNP(CNP)) {
            throw new IllegalArgumentException("CNP invalid.");
        }
        this.CNP = CNP;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersoanaFizica that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(CNP, that.CNP);
    }

    @Override
    public String toString() {
        return nume + " " + prenume + " este un client al băncii și este persoană fizică cu CNP-ul " + getCNP() + ". " + super.toString();
    }
}

class PersoanaJuridica extends Client {
    private String denumireCompanie;
    private String CUI;
    private String reprezentantLegal;

    public PersoanaJuridica(String adresa, LocalDate dataInrolare, TipAbonament abonament, String denumireCompanie, String CUI, String reprezentantLegal) {
        super(adresa, dataInrolare, abonament);
        if (denumireCompanie == null || denumireCompanie.trim().isEmpty()) {
            throw new IllegalArgumentException("Denumirea companiei invalidă.");
        }
        if (!isValidCUI(CUI)) {
            throw new IllegalArgumentException("CUI invalid.");
        }
        if (reprezentantLegal == null || reprezentantLegal.trim().isEmpty()) {
            throw new IllegalArgumentException("Reprezentantul legal invalid.");
        }
        if (abonament == TipAbonament.PREMIUM) {
            throw new IllegalArgumentException("Persoanele juridice nu pot avea abonament PREMIUM.");
        }
        this.denumireCompanie = denumireCompanie;
        this.CUI = CUI;
        this.reprezentantLegal = reprezentantLegal;
    }

    private static boolean isValidCUI(String CUI) {
        return CUI != null && CUI.matches("\\d{6}");
    }

    public String getDenumireCompanie() {
        return denumireCompanie;
    }

    public void setDenumireCompanie(String denumireCompanie) {
        if (denumireCompanie == null || denumireCompanie.trim().isEmpty()) {
            throw new IllegalArgumentException("Denumirea companiei invalidă.");
        }
        this.denumireCompanie = denumireCompanie;
    }

    public String getCUI() {
        return "****" + CUI.substring(Math.max(0, CUI.length() - 4));
    }

    public void setCUI(String CUI) {
        if (!isValidCUI(CUI)) {
            throw new IllegalArgumentException("CUI invalid.");
        }
        this.CUI = CUI;
    }

    public String getReprezentantLegal() {
        return reprezentantLegal;
    }

    public void setReprezentantLegal(String reprezentantLegal) {
        if (reprezentantLegal == null || reprezentantLegal.trim().isEmpty()) {
            throw new IllegalArgumentException("Reprezentantul legal invalid.");
        }
        this.reprezentantLegal = reprezentantLegal;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersoanaJuridica that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(CUI, that.CUI);
    }

    @Override
    public String toString() {
        return denumireCompanie + " este un client al băncii și este persoană juridică cu CUI-ul " + getCUI() + ", reprezentată legal de " + reprezentantLegal + ". " + super.toString();
    }
}