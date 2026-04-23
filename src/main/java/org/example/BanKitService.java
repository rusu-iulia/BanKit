package org.example;

public class BanKitService {

    private Client[] clienti = new Client[50];
    private int numarClienti = 0;

    private String[] ibansConturi = new String[100];
    private Account[] conturi = new Account[100];
    private int numarConturi = 0;

    private Tranzactie[] istoricTranzactii = new Tranzactie[500];
    private int numarTranzactii = 0;

    private Reward[] catalogRewards = new Reward[20];
    private int numarRewardsCatalog = 0;

    public BanKitService() {
        initializeazaCatalogRewards();
    }

    public void adaugaClient(Client client) {
        if (client == null) {
            return;
        }
        if (existaClient(client.getIdClient())) {
            System.out.println("Clientul cu id " + client.getIdClient() + " exista deja in sistem.");
            return;
        }
        if(numarClienti >= clienti.length) {
            System.out.println("Capacitate maxima de clienti atinsa. Nu se pot adauga mai multi clienti.");
            return;
        }
        clienti[numarClienti++] = client;
        System.out.println("Client adăugat: " + client.getIdClient());
    }

    public void deschideCont(Client client, Account cont) {
        if (numarConturi >= conturi.length) {
            System.out.println("Capacitate maxima de conturi a băncii atinsa. Nu se pot deschide mai multe conturi.");
            return;
        }
        cont.setTitular(client);
        client.adaugaCont(cont);
        ibansConturi[numarConturi] = cont.getIban();
        conturi[numarConturi] = cont;
        numarConturi++;
        System.out.println("Cont " + cont.getValuta() + " deschis cu succes (" + cont.getIban() + ").");
    }

    public boolean proceseazaPlataCuCard(String numarCard, double suma, String detaliiComerciant) {
        Card card = cautaCardDupaNumar(numarCard);

        if (card == null) {
            System.out.println("Eroare: Cardul nu a fost găsit în sistem.");
            return false;
        }

        Account cont = card.getContSursa();
        Client client = cont.getTitular(); 

        if (card instanceof CardCredit) {
            CardCredit cardCredit = (CardCredit) card; 
            
            if (!cardCredit.potCheltui(suma)) {
                System.out.println("Plată respinsă: Limita de credit a fost depășită!");
                return false;
            }
            cardCredit.inregistreazaCheltuiala(suma);
            System.out.println("Plată acceptată. Datoria ta curentă este acum: " + cardCredit.getDatorieCurenta());
        } 
        else if (card instanceof CardDebit) {
            CardDebit cardDebit = (CardDebit) card; 
            
            if (cont.getSold() < suma) {
                System.out.println("Plată respinsă: Fonduri insuficiente în contul curent.");
                return false;
            }
            cont.retragere(suma);
            System.out.println("Plată acceptată. Sold rămas: " + cont.getSold());
        }

        if (client instanceof PersoanaFizica) {
            int puncteCastigate = (int) ((suma / 10) * client.getAbonament().getMultiplicatorPuncte());
            client.setPuncteRev(client.getPuncteRev() + puncteCastigate);
            System.out.println("Bonus: Ai câștigat " + puncteCastigate + " RevPoints!");

        } else if (client instanceof PersoanaJuridica) {
            double cashback = suma * 0.01;
            cont.depunere(cashback);
            System.out.println("Bonus: Cashback de " + cashback + " " + cont.getValuta() + " a fost adăugat în cont!");
        }

        Tranzactie t = new Tranzactie(suma, TipTranzactie.PLATA_CARD, cont, detaliiComerciant);
        if (numarTranzactii < istoricTranzactii.length) {
            istoricTranzactii[numarTranzactii++] = t;
        }
        
        return true;
    }

    public boolean transferaBani(Client clientSursa, String ibanSursa, String ibanDestinatie, double suma, String detalii) {
        Account contSursa = cautaContDupaIban(ibanSursa);
        Account contDestinatie = cautaContDupaIban(ibanDestinatie);

        if (contSursa == null || contDestinatie == null) {
            System.out.println("Eroare: Unul dintre conturi nu există în sistem.");
            return false;
        }

        if (!contSursa.getValuta().equals(contDestinatie.getValuta())) {
            System.out.println("Eroare: Transferurile se pot face doar între conturi cu aceeași valută.");
            return false;
        }

        if (contSursa.retragere(suma)) {
            contDestinatie.depunere(suma);

            Tranzactie t = new Tranzactie(suma, TipTranzactie.TRANSFER_BANCAR, contSursa, contDestinatie, detalii);
            t.marcheazaCompletata();
            
            if (numarTranzactii < istoricTranzactii.length) {
                istoricTranzactii[numarTranzactii++] = t;
            }
            
            System.out.println("Transfer de " + suma + " " + contSursa.getValuta() + " realizat cu succes către " + ibanDestinatie);
            return true;
        } else {
            System.out.println("Eroare: Fonduri insuficiente în contul sursă.");
            return false;
        }
    }
    
    public void afiseazaClientiSortati() {
        if (numarClienti == 0) {
            System.out.println("Nu exista clienti adaugati.");
            return;
        }

        Client[] clientiSortati = new Client[numarClienti];
        for (int i = 0; i < numarClienti; i++) {
            clientiSortati[i] = clienti[i];
        }

        for (int i = 0; i < numarClienti - 1; i++) {
            for (int j = 0; j < numarClienti - i - 1; j++) {
                if (clientiSortati[j].compareTo(clientiSortati[j + 1]) > 0) {
                    Client temp = clientiSortati[j];
                    clientiSortati[j] = clientiSortati[j + 1];
                    clientiSortati[j + 1] = temp;
                }
            }
        }

        for (int i = 0; i < numarClienti; i++) {
            System.out.println(clientiSortati[i].toString());
        }
    }

    public boolean revendicaReward(Client client, Reward reward) {
        if (!existaClient(client.getIdClient())) {
            System.out.println("Client invalid.");
            return false;
        }
        boolean success = client.revendicaReward(reward);
        if (success) {
            System.out.println("Recompensă revendicată cu succes pentru client " + client.getIdClient() + ".");
        } else {
            System.out.println("Revendicare eșuată: puncte insuficiente sau recompensă deja folosită.");
        }
        return success;
    }

    private boolean existaClient(int idClient) {
        for (int i = 0; i < numarClienti; i++) {
            if (clienti[i] != null && clienti[i].getIdClient() == idClient) {
                return true;
            }
        }
        return false;
    }

    public Client cautaClientDupaId(int idClient) {
        for (int i = 0; i < numarClienti; i++) {
            if (clienti[i] != null && clienti[i].getIdClient() == idClient) {
                return clienti[i];
            }
        }
        return null;
    }

    public Account cautaContDupaIban(String iban) {
        for (int i = 0; i < numarConturi; i++) {
            if (ibansConturi[i] != null && ibansConturi[i].equals(iban)) {
                return conturi[i];
            }
        }
        return null;
    }
    
    public Card cautaCardDupaNumar(String numarCard) {
        for (int i = 0; i < numarConturi; i++) {
            if (conturi[i] != null) {
                for (Card c : conturi[i].getCarduri()) {
                    if (c != null && c.getNumarCard().equals(numarCard)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }
    
    public void emiteCardPentruCont(String iban, Card cardNou) {
        Account cont = cautaContDupaIban(iban);
        
        if (cont == null) {
            System.out.println("Contul cu IBAN-ul " + iban + " nu a fost găsit.");
            return;
        }
        try {
            cardNou.setContSursa(cont);
            cont.adaugaCard(cardNou);
            System.out.println("Cardul " + cardNou.getNumarCard() + " a fost emis cu succes pentru contul " + iban);
        } catch (IllegalStateException e) {
            System.out.println("Eroare la emiterea cardului: " + e.getMessage());
        }
    }
    
    public void afiseazaDetaliiSpecificeReward(Reward reward) {
        System.out.println("Detalii Recompensă " + reward.getIdOferta());
        System.out.println("Nume: " + reward.getNumeOferta());
        
        if (reward instanceof FlightReward) {
            FlightReward zbor = (FlightReward) reward; 
            System.out.println("Clientul a revendicat o recompensă de tip zbor." );
        } 
        else if (reward instanceof AccommodationReward) {
            AccommodationReward cazare = (AccommodationReward) reward;
            System.out.println("Clientul a revendicat o recompensă de tip cazare.");
        }
        System.out.println("Detalii complete: " + reward.toString());
    }
    
    public void genereazaExtrasDeCont(String iban) {
        Account cont = cautaContDupaIban(iban);
        if (cont == null) {
            System.out.println("Contul nu a fost găsit.");
            return;
        }

        boolean areTranzactii = false;
        for (int i = 0; i < numarTranzactii; i++) {
            Tranzactie t = istoricTranzactii[i];
            if (t.getContSursa().equals(cont) || (t.getContDestinatie() != null && t.getContDestinatie().equals(cont))) {
                System.out.println(t.toString());
                areTranzactii = true;
            }
        }

        if (!areTranzactii) {
            System.out.println("Nu există tranzacții înregistrate pentru acest cont.");
        }
        System.out.println("Sold curent: " + cont.getSold() + " " + cont.getValuta());
    }

    private void initializeazaCatalogRewards() {
        catalogRewards[numarRewardsCatalog++] = new FlightReward("F1", "Zbor Roma", 150, "WizzAir", "Roma");
        catalogRewards[numarRewardsCatalog++] = new FlightReward("F2", "Zbor Paris", 300, "Air France", "Paris");
        catalogRewards[numarRewardsCatalog++] = new AccommodationReward("A1", "Hotel Sinaia", 100, "Hotel Internațional", 2);
        catalogRewards[numarRewardsCatalog++] = new AccommodationReward("A2", "Resort Antalya", 500, "Delphin Be Grand", 7);
    }

    public Reward[] getCatalogRewards() {
        return catalogRewards;
    }

    public int getNumarRewardsCatalog() {
        return numarRewardsCatalog;
    }

    public Reward getRewardDinCatalog(int index) {
        if (index >= 0 && index < numarRewardsCatalog) {
            return catalogRewards[index];
        }
        return null;
    }
}