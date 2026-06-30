package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.example.enums.TipAbonament;
import org.example.exceptions.AuthenticationException;

public abstract class Client implements Comparable<Client> {

    abstract String getNumeComplet();
    abstract String getIdentificatorUnic();

    private static int contorIdClient = 100;
    private int idClient;
    private String adresa;
    private String parolaHash;
    private List<Account> conturi = new ArrayList<>();
    private int puncteRev;
    private LocalDate dataInrolare;
    private TipAbonament abonament;

    private static String hashParola(String parola) {
        return Integer.toHexString(parola.hashCode());
    }

    public Client(String adresa, LocalDate dataInrolare, TipAbonament abonament, String parola) {
        if (adresa == null || adresa.trim().isEmpty()) throw new IllegalArgumentException("Adresă invalidă.");
        if (dataInrolare == null) throw new IllegalArgumentException("Data înrolării invalidă.");
        if (abonament == null) throw new IllegalArgumentException("Abonament invalid.");
        this.idClient = contorIdClient++;
        this.adresa = adresa;
        this.puncteRev = 0;
        this.dataInrolare = dataInrolare;
        this.abonament = abonament;
        if (parola != null && !parola.trim().isEmpty()) {
            this.parolaHash = hashParola(parola);
        }
    }

    String getParolaHash() { return parolaHash; }
    void setParolaHash(String hash) { this.parolaHash = hash; }
    void setIdClient(int id) { this.idClient = id; }

    public boolean verificaParola(String parola) {
        if (parolaHash == null || parola == null) return false;
        return parolaHash.equals(hashParola(parola));
    }

    public void schimbaParola(String parolaVeche, String parolaNou) {
        if (!verificaParola(parolaVeche)) throw new AuthenticationException();
        if (parolaNou == null || parolaNou.trim().isEmpty()) {
            throw new IllegalArgumentException("Parola nouă nu poate fi goală.");
        }
        this.parolaHash = hashParola(parolaNou);
    }

    public int getIdClient() { return idClient; }

    public String getAdresa() { return adresa; }

    public void setAdresa(String adresa) {
        if (adresa == null || adresa.trim().isEmpty()) throw new IllegalArgumentException("Adresă invalidă.");
        this.adresa = adresa;
    }

    public List<Account> getConturi() {
        return Collections.unmodifiableList(conturi);
    }

    public void adaugaCont(Account cont) {
        if (cont == null) return;
        conturi.add(cont);
    }

    public int getNumarConturi() { return conturi.size(); }

    public int getPuncteRev() { return puncteRev; }

    public void setPuncteRev(int puncteRev) {
        if (puncteRev < 0) throw new IllegalArgumentException("Puncte RevPoints invalide.");
        this.puncteRev = puncteRev;
    }

    public void adaugaPuncte(int puncte) {
        if (puncte > 0) this.puncteRev += puncte;
    }

    public LocalDate getDataInrolare() { return dataInrolare; }

    public void setDataInrolare(LocalDate dataInrolare) {
        if (dataInrolare == null) throw new IllegalArgumentException("Data înrolării invalidă.");
        this.dataInrolare = dataInrolare;
    }

    public TipAbonament getAbonament() { return abonament; }

    public void setAbonament(TipAbonament abonament) {
        if (abonament == null) throw new IllegalArgumentException("Abonament invalid.");
        this.abonament = abonament;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client client)) return false;
        return idClient == client.idClient;
    }

    @Override
    public int hashCode() { return Integer.hashCode(idClient); }

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
        return this.getNumeComplet().compareToIgnoreCase(o.getNumeComplet());
    }
}

class PersoanaFizica extends Client {

    private String nume;
    private String prenume;
    private String CNP;

    public PersoanaFizica(String adresa, LocalDate dataInrolare, TipAbonament abonament, String parola,
                          String nume, String prenume, String CNP) {
        super(adresa, dataInrolare, abonament, parola);
        if (nume == null || nume.trim().isEmpty()) throw new IllegalArgumentException("Nume invalid.");
        if (prenume == null || prenume.trim().isEmpty()) throw new IllegalArgumentException("Prenume invalid.");
        if (!isValidCNP(CNP)) throw new IllegalArgumentException("CNP invalid.");
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

    public String getNume() { return nume; }

    public void setNume(String nume) {
        if (nume == null || nume.trim().isEmpty()) throw new IllegalArgumentException("Nume invalid.");
        this.nume = nume;
    }

    public String getPrenume() { return prenume; }

    public void setPrenume(String prenume) {
        if (prenume == null || prenume.trim().isEmpty()) throw new IllegalArgumentException("Prenume invalid.");
        this.prenume = prenume;
    }

    @Override
    String getNumeComplet() { return nume + " " + prenume; }

    @Override
    String getIdentificatorUnic() { return CNP; }

    public String getCNP() { return "****" + CNP.substring(9); }

    public void setCNP(String CNP) {
        if (!isValidCNP(CNP)) throw new IllegalArgumentException("CNP invalid.");
        this.CNP = CNP;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PersoanaFizica that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(CNP, that.CNP);
    }

    @Override
    public int hashCode() { return Objects.hash(super.hashCode(), CNP); }

    @Override
    public String toString() {
        return nume + " " + prenume + " este un client al băncii și este persoană fizică cu CNP-ul " + getCNP() + ". " + super.toString();
    }
}

class PersoanaJuridica extends Client {

    private String denumireCompanie;
    private String CUI;
    private String reprezentantLegal;

    public PersoanaJuridica(String adresa, LocalDate dataInrolare, TipAbonament abonament, String parola,
                            String denumireCompanie, String CUI, String reprezentantLegal) {
        super(adresa, dataInrolare, abonament, parola);
        if (denumireCompanie == null || denumireCompanie.trim().isEmpty()) {
            throw new IllegalArgumentException("Denumirea companiei invalidă.");
        }
        if (!isValidCUI(CUI)) throw new IllegalArgumentException("CUI invalid.");
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

    public void setDenumireCompanie(String denumireCompanie) {
        if (denumireCompanie == null || denumireCompanie.trim().isEmpty()) {
            throw new IllegalArgumentException("Denumirea companiei invalidă.");
        }
        this.denumireCompanie = denumireCompanie;
    }

    @Override
    String getNumeComplet() { return denumireCompanie; }

    @Override
    String getIdentificatorUnic() { return CUI; }

    public String getCUI() { return "****" + CUI.substring(Math.max(0, CUI.length() - 4)); }

    public void setCUI(String CUI) {
        if (!isValidCUI(CUI)) throw new IllegalArgumentException("CUI invalid.");
        this.CUI = CUI;
    }

    public String getReprezentantLegal() { return reprezentantLegal; }

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
    public int hashCode() { return Objects.hash(super.hashCode(), CUI); }

    @Override
    public String toString() {
        return denumireCompanie + " este un client al băncii și este persoană juridică cu CUI-ul " + getCUI()
                + ", reprezentată legal de " + reprezentantLegal + ". " + super.toString();
    }
}
