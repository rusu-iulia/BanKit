package org.example;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import org.example.enums.TipAbonament;
import org.example.enums.ValuteAcceptate;
import org.example.exceptions.*;

public class Main {
    private static final String PAROLA_ADMIN = "admin2026";
    public static void main(String[] args) {
        DatabaseInitializer.initializeTables();
        Scanner scanner = new Scanner(System.in);
        BanKitService bankit = new BanKitService();

        boolean ruleaza = true;
        while (ruleaza) {
            System.out.println("\n========== BanKit ==========");
            System.out.println("1. Login Admin");
            System.out.println("2. Login Client");
            System.out.println("0. Ieșire");
            System.out.print("Alege: ");
            String optiune = scanner.nextLine().trim();

            switch (optiune) {
                case "1":
                    System.out.print("Parolă admin: ");
                    String parolaAdmin = scanner.nextLine();
                    if (PAROLA_ADMIN.equals(parolaAdmin)) {
                        menuAdmin(scanner, bankit);
                    } else {
                        System.out.println("Parolă incorectă!");
                    }
                    break;
                case "2":
                    loginClient(scanner, bankit);
                    break;
                case "0":
                    System.out.println("La revedere!");
                    ruleaza = false;
                    break;
                default:
                    System.out.println("Opțiune invalidă.");
            }
        }
        scanner.close();
    }


    private static void menuAdmin(Scanner scanner, BanKitService bankit) {
        boolean adminRuleaza = true;
        while (adminRuleaza) {
            System.out.println("\n======== MENIU ADMIN ========");
            System.out.println("1.  Adaugă client");
            System.out.println("2.  Afișează clienți sortați");
            System.out.println("3.  Deschide cont bancar");
            System.out.println("4.  Emite card pentru cont");
            System.out.println("5.  Blochează card");
            System.out.println("6.  Deblochează card");
            System.out.println("7.  Afișează conturile unui client");
            System.out.println("8.  Generează extras de cont");
            System.out.println("9.  Aplică dobânzi lunare");
            System.out.println("10. Aplică taxe de administrare");
            System.out.println("11. Jurnal audit");
            System.out.println("12. Caută client după CNP/CUI");
            System.out.println("13. Încasează taxă abonament");
            System.out.println("0.  Logout");
            System.out.print("Alege: ");
            String optiune = scanner.nextLine().trim();

            try {
                switch (optiune) {
                    case "1":  adaugaClientAdmin(scanner, bankit);       break;
                    case "2":  bankit.afiseazaClientiSortati();          break;
                    case "3":  deschideContAdmin(scanner, bankit);       break;
                    case "4":  emiteCardAdmin(scanner, bankit);          break;
                    case "5":  blocheazaCardAdmin(scanner, bankit);      break;
                    case "6":  deblocheazaCardAdmin(scanner, bankit);    break;
                    case "7":  afiseazaConturiClient(scanner, bankit);   break;
                    case "8":  extrasDeCont(scanner, bankit);            break;
                    case "9":  bankit.aplicaDobanziLunare();             break;
                    case "10": bankit.aplicaTaxeAdministrare();          break;
                    case "11": afiseazaFisierAudit();                    break;
                    case "12": cautaClientDupaCNPAdmin(scanner, bankit);        break;
                    case "13": incaseazaTaxaAbonamentAdmin(scanner, bankit);  break;
                    case "0":
                        System.out.println("Deconectat din contul admin.");
                        adminRuleaza = false;
                        break;
                    default:
                        System.out.println("Opțiune invalidă.");
                }
            } catch (Exception e) {
                System.out.println("Eroare neașteptată: " + e.getMessage());
            }
        }
    }

    private static void menuClient(Scanner scanner, BanKitService bankit, Client client) {
        boolean clientRuleaza = true;
        while (clientRuleaza) {
            System.out.println("\n======== MENIU CLIENT " + client.getNumeComplet() + " ========");
            System.out.println("1.  Afișează conturile mele");
            System.out.println("2.  Extras de cont");
            System.out.println("3.  Depune bani");
            System.out.println("4.  Transfer bancar");
            System.out.println("5.  Plată cu cardul");
            System.out.println("6.  Schimb valutar");
            System.out.println("7.  Schimbă PIN");
            System.out.println("8.  Schimbă parola contului");
            System.out.println("9.  Setează limită zilnică card");
            System.out.println("10. Magazin rewards");
            System.out.println("11. Simulator ATM");
            System.out.println("0.  Logout");
            System.out.print("Alege: ");
            String optiune = scanner.nextLine().trim();

            try {
                switch (optiune) {
                    case "1":  afiseazaConturileMele(client, bankit);             break;
                    case "2":  extrasDeCont(scanner, bankit);                    break;
                    case "3":  depuneBani(scanner, bankit);                      break;
                    case "4":  transferBancar(scanner, bankit, client);          break;
                    case "5":  plataCuCard(scanner, bankit, client);             break;
                    case "6":  schimbValuta(scanner, bankit);                    break;
                    case "7":  schimbaPin(scanner, bankit, client);              break;
                    case "8":  schimbaParola(scanner, bankit, client);           break;
                    case "9":  seteazaLimitaZilnica(scanner, bankit, client);    break;
                    case "10": magazinRewardsClient(scanner, bankit, client);    break;
                    case "11": simulatorATM(bankit);                             break;
                    case "0":
                        System.out.println("Deconectat din contul " + client.getIdClient() + ".");
                        clientRuleaza = false;
                        break;
                    default:
                        System.out.println("Opțiune invalidă.");
                }
            } catch (Exception e) {
                System.out.println("Eroare neașteptată: " + e.getMessage());
            }
        }
    }

    private static void loginClient(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("CNP/CUI Client: ");
            String codClient = scanner.nextLine();
            Client gasit = bankit.cautaClientDupaCNP(codClient);
            if (gasit == null) {
                System.out.println("Nu a fost găsit niciun client cu codul introdus.");
                return;
            }
            System.out.print("Parolă: ");
            String parola = scanner.nextLine();
            Client client = bankit.autentifica(gasit.getIdClient(), parola);
            System.out.println("Bine ai venit, " + client.getNumeComplet() + "!");
            menuClient(scanner, bankit, client);
        } catch (AuthenticationException e) {
            System.out.println("Autentificare eșuată: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Eroare neașteptată: " + e.getMessage());
        }
    }

    private static void adaugaClientAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("Tip client (1 - Persoană Fizică, 2 - Persoană Juridică): ");
            String tipClient = scanner.nextLine().trim();
            System.out.print("Adresa: ");
            String adresa = scanner.nextLine();
            System.out.print("Parolă cont BanKit: ");
            String parola = scanner.nextLine();

            if (tipClient.equals("1")) {
                System.out.print("Abonament (STANDARD, PREMIUM): ");
                TipAbonament abonament = TipAbonament.valueOf(scanner.nextLine().toUpperCase().trim());
                System.out.print("Nume: ");
                String nume = scanner.nextLine();
                System.out.print("Prenume: ");
                String prenume = scanner.nextLine();
                System.out.print("CNP (13 cifre): ");
                String cnp = scanner.nextLine().trim();
                bankit.adaugaClient(new PersoanaFizica(adresa, LocalDate.now(), abonament, parola, nume, prenume, cnp));
            } else if (tipClient.equals("2")) {
                System.out.print("Abonament (STANDARD, BUSINESS_PRO): ");
                TipAbonament abonament = TipAbonament.valueOf(scanner.nextLine().toUpperCase().trim());
                System.out.print("Denumire Companie: ");
                String denumire = scanner.nextLine();
                System.out.print("CUI (6 cifre): ");
                String cui = scanner.nextLine().trim();
                System.out.print("Reprezentant Legal: ");
                String reprezentant = scanner.nextLine();
                bankit.adaugaClient(new PersoanaJuridica(adresa, LocalDate.now(), abonament, parola, denumire, cui, reprezentant));
            } else {
                System.out.println("Tip client invalid.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare la date: " + e.getMessage());
        }
    }

    private static void deschideContAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("ID client: ");
            int idClient = Integer.parseInt(scanner.nextLine().trim());
            Client titular = bankit.cautaClientDupaIdCuConturi(idClient);
            if (titular == null) {
                System.out.println("Clientul cu ID-ul " + idClient + " nu există.");
                return;
            }
            System.out.print("Tip cont (1 - Curent, 2 - Economii): ");
            String tipCont = scanner.nextLine().trim();
            String iban = bankit.genereazaIban();
            System.out.print("Valuta (RON, EUR, USD): ");
            ValuteAcceptate valuta = ValuteAcceptate.valueOf(scanner.nextLine().toUpperCase().trim());
            System.out.print("Suma depusă inițial: ");
            double sold = Double.parseDouble(scanner.nextLine().trim());

            Account contNou;
            if (tipCont.equals("1")) {
                System.out.print("Taxă de administrare lunară: ");
                contNou = new ContCurent(iban, sold, valuta, Double.parseDouble(scanner.nextLine().trim()));
            } else if (tipCont.equals("2")) {
                System.out.print("Rată dobândă (ex: 0.05 pentru 5%): ");
                contNou = new ContDeEconomii(iban, sold, valuta, Double.parseDouble(scanner.nextLine().trim()));
            } else {
                System.out.println("Tip cont invalid.");
                return;
            }
            bankit.deschideCont(titular, contNou);
        } catch (IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void emiteCardAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("IBAN cont: ");
            String ibanCard = scanner.nextLine().trim();
            System.out.print("PIN (4 cifre): ");
            String pin = scanner.nextLine().trim();
            String numarCard = "4000" + String.format("%012d", (long) (Math.random() * 1000000000000L));
            System.out.print("Tip card (1 - Debit, 2 - Credit): ");
            String tipCard = scanner.nextLine().trim();

            Card cardNou;
            if (tipCard.equals("1")) {
                System.out.print("Limita contactless: ");
                cardNou = new CardDebit(numarCard, pin, Double.parseDouble(scanner.nextLine().trim()));
            } else if (tipCard.equals("2")) {
                System.out.print("Limita de credit: ");
                cardNou = new CardCredit(numarCard, pin, Double.parseDouble(scanner.nextLine().trim()));
            } else {
                System.out.println("Tip card invalid.");
                return;
            }
            bankit.emiteCardPentruCont(ibanCard, cardNou);
        } catch (BanKitException | IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void blocheazaCardAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("ID Client: ");
            Client client = bankit.cautaClientDupaIdCuConturi(Integer.parseInt(scanner.nextLine().trim()));
            bankit.cautaExistentaCard(client);
            System.out.print("Număr card: ");
            bankit.blocheazaCard(scanner.nextLine().trim(), client);
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void deblocheazaCardAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("ID Client: ");
            Client client = bankit.cautaClientDupaIdCuConturi(Integer.parseInt(scanner.nextLine().trim()));
            bankit.cautaExistentaCard(client);
            System.out.print("Număr card: ");
            bankit.deblocheazaCard(scanner.nextLine().trim(), client);
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void afiseazaConturiClient(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("ID client: ");
            int idClient = Integer.parseInt(scanner.nextLine().trim());
            Client c = bankit.cautaClientDupaIdCuConturi(idClient);
            if (c == null) {
                System.out.println("Clientul nu a fost găsit.");
                return;
            }
            List<Account> conturi = c.getConturi();
            if (conturi.isEmpty()) {
                System.out.println("Clientul nu are conturi deschise.");
            } else {
                System.out.println("Conturile clientului " + idClient + ":");
                for (Account a : conturi) System.out.println("  " + a);
            }
        } catch (NumberFormatException e) {
            System.out.println("ID invalid.");
        }
    }

    private static void extrasDeCont(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("IBAN cont: ");
            String iban = scanner.nextLine().trim();
            System.out.print("Luna început (MM/yyyy): ");
            String perioadaStart = scanner.nextLine().trim();
            System.out.print("Luna sfârșit (MM/yyyy): ");
            String perioadaFinal = scanner.nextLine().trim();
            bankit.genereazaExtrasDeCont(iban, perioadaStart, perioadaFinal);
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void cautaClientDupaCNPAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("CNP/CUI: ");
            String cod = scanner.nextLine().trim();
            Client client = bankit.cautaClientDupaCNP(cod);
            if (client == null) {
                System.out.println("Nu a fost găsit niciun client cu codul introdus.");
                return;
            }
            System.out.println("Client găsit: " + client);
            List<Account> conturi = client.getConturi();
            if (conturi.isEmpty()) {
                System.out.println("Clientul nu are conturi deschise.");
            } else {
                System.out.println("Conturile clientului:");
                for (Account a : conturi) System.out.println("  " + a);
            }
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void incaseazaTaxaAbonamentAdmin(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("ID Client: ");
            int idClient = Integer.parseInt(scanner.nextLine().trim());
            Client client = bankit.cautaClientDupaIdCuConturi(idClient);
            if (client == null) {
                System.out.println("Clientul nu a fost găsit.");
                return;
            }
            System.out.print("Sumă taxă abonament: ");
            double suma = Double.parseDouble(scanner.nextLine().trim());
            bankit.incaseazaTaxaAbonament(client, suma);
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Date invalide.");
        }
    }

    private static void afiseazaFisierAudit() {
        try (java.util.Scanner s = new java.util.Scanner(new java.io.File("audit.csv"))) {
            if (!s.hasNextLine()) { System.out.println("Fișierul audit este gol."); return; }
            System.out.println("\n=== JURNAL AUDIT ===");
            while (s.hasNextLine()) System.out.println(s.nextLine());
            System.out.println("=== SFARSIT JURNAL ===\n");
        } catch (java.io.FileNotFoundException e) {
            System.out.println("Fișierul audit.csv nu a fost găsit.");
        }
    }

    private static void afiseazaConturileMele(Client client, BanKitService bankit) {
        bankit.sincronizeazaConturi(client);
        List<Account> conturi = client.getConturi();
        if (conturi.isEmpty()) {
            System.out.println("Nu ai conturi deschise.");
        } else {
            System.out.println("Conturile tale:");
            for (Account a : conturi) System.out.println("  " + a);
        }
    }

    private static void depuneBani(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("IBAN cont: ");
            String iban = scanner.nextLine().trim();
            System.out.print("Suma de depus: ");
            bankit.depunereBani(iban, Double.parseDouble(scanner.nextLine().trim()));
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Sumă invalidă.");
        }
    }

    private static void transferBancar(Scanner scanner, BanKitService bankit, Client client) {
        try {
            System.out.print("IBAN cont sursă: ");
            String ibanSursa = scanner.nextLine().trim();
            System.out.print("IBAN cont destinație: ");
            String ibanDestinatie = scanner.nextLine().trim();
            System.out.print("Suma de transferat: ");
            double suma = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Detalii transfer: ");
            bankit.transferaBani(client, ibanSursa, ibanDestinatie, suma, scanner.nextLine());
        } catch (BanKitException e) {
            System.out.println("Transfer respins: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Sumă invalidă.");
        }
    }

    private static void plataCuCard(Scanner scanner, BanKitService bankit, Client client) {
        try {
            bankit.cautaExistentaCard(client);
            System.out.print("Număr card (16 cifre): ");
            String nrCard = scanner.nextLine().trim();
            System.out.print("Suma de plată: ");
            double suma = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Comerciant: ");
            bankit.proceseazaPlataCuCard(nrCard, suma, scanner.nextLine());
        } catch (CardBlocatException | FonduriInsuficienteException |
                 LimitaCreditDepasitaException | LimitaZilnicaDepasitaException e) {
            System.out.println("Plată respinsă: " + e.getMessage());
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Sumă invalidă.");
        }
    }

    private static void schimbValuta(Scanner scanner, BanKitService bankit) {
        try {
            System.out.print("IBAN cont sursă: ");
            String ibanSursa = scanner.nextLine().trim();
            System.out.print("IBAN cont destinație: ");
            String ibanDestinatie = scanner.nextLine().trim();
            System.out.print("Suma de schimbat: ");
            bankit.schimbaValuta(ibanSursa, ibanDestinatie, Double.parseDouble(scanner.nextLine().trim()));
        } catch (BanKitException e) {
            System.out.println("Schimb respins: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Sumă invalidă.");
        }
    }

    private static void schimbaPin(Scanner scanner, BanKitService bankit, Client client) {
        try {
            System.out.print("Număr card: ");
            String nrCard = scanner.nextLine().trim();
            System.out.print("PIN curent: ");
            String pinVechi = scanner.nextLine();
            System.out.print("PIN nou (4 cifre): ");
            bankit.schimbaPinCard(nrCard, pinVechi, scanner.nextLine(), client);
        } catch (CardBlocatException e) {
            System.out.println("Card blocat: " + e.getMessage());
        } catch (BanKitException | IllegalArgumentException e) {
            System.out.println("Eroare: " + e.getMessage());
        }
    }

    private static void schimbaParola(Scanner scanner, BanKitService bankit, Client client) {
        try {
            System.out.print("Parola curentă: ");
            String parolaVeche = scanner.nextLine();
            System.out.print("Parola nouă: ");
            String parolaNoua = scanner.nextLine();
            System.out.print("Confirmare parolă nouă: ");
            if (!parolaNoua.equals(scanner.nextLine())) {
                System.out.println("Parolele noi nu coincid.");
            } else {
                bankit.schimbaParolaClient(client, parolaVeche, parolaNoua);
            }
        } catch (AuthenticationException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Date invalide: " + e.getMessage());
        }
    }

    private static void seteazaLimitaZilnica(Scanner scanner, BanKitService bankit, Client client) {
        try {
            System.out.print("Număr card: ");
            String nc = scanner.nextLine().trim();
            System.out.print("Limită zilnică: ");
            bankit.setLimitaZilnicaCard(nc, Double.parseDouble(scanner.nextLine().trim()), client);
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Sumă invalidă.");
        }
    }

    private static void simulatorATM(BanKitService bankit) {
        try {
            new BancomatGUI(bankit);
        } catch (Exception e) {
            System.out.println("Eroare la deschiderea interfeței bancomat.");
        }
    }

    private static void magazinRewardsClient(Scanner scanner, BanKitService bankit, Client client) {
        try {
            bankit.sincronizeazaPuncte(client);
            System.out.println("Puncte disponibile: " + client.getPuncteRev());
            System.out.println("Catalog Recompense:");
            List<Reward> catalog = bankit.getCatalogRewards();
            List<String> revendicate = bankit.getRewardsRevendicate(client.getIdClient());

            for (int i = 0; i < catalog.size(); i++) {
                boolean revendicat = revendicate.contains(catalog.get(i).getIdOferta());
                String status = revendicat ? " și a fost revendicată." : " și nu a fost revendicată.";
                System.out.println((i + 1) + ". " + catalog.get(i) + status);
            }

            System.out.print("Alege recompensa (0 pentru anulare): ");
            int alegere = Integer.parseInt(scanner.nextLine().trim());

            if (alegere > 0 && alegere <= catalog.size()) {
                Reward rewardAles = catalog.get(alegere - 1);
                bankit.revendicaReward(client, rewardAles);
                bankit.afiseazaDetaliiSpecificeReward(rewardAles);
            }
        } catch (PuncteInsuficienteException e) {
            System.out.println("Revendicare eșuată: " + e.getMessage());
        } catch (BanKitException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Opțiune invalidă.");
        }
    }
}
