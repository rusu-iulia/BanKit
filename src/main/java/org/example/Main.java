package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        BanKitService bankit = new BanKitService();

        boolean ruleaza = true;
        while(ruleaza){
            System.out.println("1. Adaugă client");
            System.out.println("2. Afișează clienți sortați alfabetic");
            System.out.println("3. Deschide un cont bancar nou");
            System.out.println("4. Simulează o plată cu cardul");
            System.out.println("5. Transferă bani");
            System.out.println("6. Afișează soldul și detaliile unui cont");
            System.out.println("7. Emite un card pentru un cont");
            System.out.println("8. Blochează un card");
            System.out.println("9. Magazin de rewards");
            System.out.println("10. Upgrade abonament");
            System.out.println("11. Generează extras de cont");
            System.out.println("0. Ieșire");
            System.out.print("Alege o opțiune: ");

            String optiune = scanner.nextLine().trim();
            switch (optiune) {
                case "1":
                    try {
                        System.out.print("Alege tipul clientului (1 - Persoană Fizică, 2 - Persoană Juridică): ");
                        String tipClient = scanner.nextLine();

                        System.out.print("Introdu adresa: ");
                        String adresa = scanner.nextLine();

                        if (tipClient.equals("1")) {
                            System.out.print("Alege abonamentul (STANDARD, PREMIUM): ");
                            TipAbonament abonament = TipAbonament.valueOf(scanner.nextLine().toUpperCase());
                            
                            System.out.print("Nume: ");
                            String nume = scanner.nextLine();
                            System.out.print("Prenume: ");
                            String prenume = scanner.nextLine();
                            System.out.print("CNP: ");
                            String cnp = scanner.nextLine();

                            PersoanaFizica pf = new PersoanaFizica(adresa, java.time.LocalDate.now(), abonament, nume, prenume, cnp);
                            bankit.adaugaClient(pf);

                        } else if (tipClient.equals("2")) {
                            System.out.print("Alege abonamentul (STANDARD, BUSINESS_PRO): ");
                            TipAbonament abonament = TipAbonament.valueOf(scanner.nextLine().toUpperCase());
                            
                            System.out.print("Denumire Companie: ");
                            String denumire = scanner.nextLine();
                            System.out.print("CUI: ");
                            String cui = scanner.nextLine();
                            System.out.print("Reprezentant Legal: ");
                            String reprezentant = scanner.nextLine();

                            PersoanaJuridica pj = new PersoanaJuridica(adresa, java.time.LocalDate.now(), abonament, denumire, cui, reprezentant);
                            bankit.adaugaClient(pj);

                        } else {
                            System.out.println("Tip client invalid.");
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                    }
                    System.out.println();
                    break;
                    
                case "2":
                    bankit.afiseazaClientiSortati();
                    break;
                    
                case "3":
                    try {
                        System.out.print("Introdu ID-ul clientului pentru care vrei să deschizi un cont: ");
                        int idClient = scanner.nextInt();
                        scanner.nextLine();

                        Client titular = bankit.cautaClientDupaId(idClient);
                        if (titular == null) {
                            System.out.println("Clientul cu ID-ul " + idClient + " nu există în sistem");
                            break; 
                        }

                        System.out.print("Alege tipul contului (1 - Curent, 2 - Economii): ");
                        String tipCont = scanner.nextLine();

                        String iban = "RO00BTRL" + System.currentTimeMillis() + "000";

                        System.out.print("Introdu valuta (RON, EUR, USD): ");
                        ValuteAcceptate valuta = ValuteAcceptate.valueOf(scanner.nextLine().toUpperCase());

                        System.out.print("Suma depusă inițial: ");
                        double sold = scanner.nextDouble();
                        scanner.nextLine();

                        Account contNou = null;
                        
                        if (tipCont.equals("1")) {
                            System.out.print("Taxă de administrare lunară: ");
                            double taxa = scanner.nextDouble();
                            scanner.nextLine();
                            contNou = new ContCurent(iban, sold, valuta, taxa);
                            
                        } else if (tipCont.equals("2")) {
                            System.out.print("Rată dobândă: ");
                            double dobanda = scanner.nextDouble();
                            scanner.nextLine();
                            contNou = new ContDeEconomii(iban, sold, valuta, dobanda);
                            
                        } else {
                            System.out.println("Tip cont invalid. Alege 1 sau 2.");
                            break;
                        }
                        bankit.deschideCont(titular, contNou);

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;
                    
                case "4":
                    try {
                        System.out.print("Introdu numărul cardului (16 cifre): ");
                        String numarCardPlata = scanner.nextLine();

                        System.out.print("Introdu suma de plată: ");
                        double suma = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.print("Introdu numele comerciantului: ");
                        String comerciant = scanner.nextLine();

                        bankit.proceseazaPlataCuCard(numarCardPlata, suma, comerciant);

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;
                    
                case "5":
                    try {
                        System.out.print("Introdu ID-ul clientului expeditor: ");
                        int idExpeditor = scanner.nextInt();
                        scanner.nextLine();

                        Client expeditor = bankit.cautaClientDupaId(idExpeditor);
                        if (expeditor == null) {
                            System.out.println("Clientul expeditor nu a fost găsit în sistem.");
                            break;
                        }

                        System.out.print("IBAN cont sursă: ");
                        String ibanSursa = scanner.nextLine();

                        System.out.print("IBAN cont destinație: ");
                        String ibanDest = scanner.nextLine();

                        System.out.print("Suma de transferat: ");
                        double sumaTransfer = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.print("Detalii transfer: ");
                        String detaliiTransfer = scanner.nextLine();

                        bankit.transferaBani(expeditor, ibanSursa, ibanDest, sumaTransfer, detaliiTransfer);

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;

                case "6":
                    try {
                        System.out.print("Introdu IBAN-ul contului: ");
                        String ibanCautat = scanner.nextLine();
                        
                        Account contGasit = bankit.cautaContDupaIban(ibanCautat);
                        if (contGasit != null) {
                            System.out.println(contGasit.toString());
                        } else {
                            System.out.println("Contul nu a fost găsit.");
                        }
                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;

                case "7":
                    try {
                        System.out.print("Introdu IBAN-ul contului la care vrei să atașezi cardul: ");
                        String ibanCardNou = scanner.nextLine();

                        Account contSursa = bankit.cautaContDupaIban(ibanCardNou);
                        if (contSursa == null) {
                            System.out.println("Contul nu a fost găsit în sistem.");
                            break;
                        }

                        System.out.print("Setează un PIN din 4 cifre pentru noul card: ");
                        String pin = scanner.nextLine();

                        long numarRandom = (long) (Math.random() * 1000000000000L); 
                        String numarCardGenerat = "4000" + String.format("%012d", numarRandom);

                        System.out.print("Ce tip de card dorești? (1 - Debit, 2 - Credit): ");
                        String tipCard = scanner.nextLine();

                        Card cardNou = null;

                        if (tipCard.equals("1")) {
                            System.out.print("Setează limita zilnică pentru plăți contactless: ");
                            double limitaContactless = Double.parseDouble(scanner.nextLine());
                            cardNou = new CardDebit(numarCardGenerat, pin, limitaContactless);
                            
                        } else if (tipCard.equals("2")) {
                            System.out.print("Setează limita maximă de credit: ");
                            double limitaCredit = Double.parseDouble(scanner.nextLine());
                            cardNou = new CardCredit(numarCardGenerat, pin, limitaCredit);
                            
                        } else {
                            System.out.println("Opțiune invalidă. Nu s-a emis niciun card.");
                            break;
                        }
                        bankit.emiteCardPentruCont(ibanCardNou, cardNou);
                        
                        System.out.println("\nDetalii Card Generat:");
                        System.out.println(cardNou.toString());

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date:" + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;

                case "8":
                    try {
                        System.out.print("Introdu IBAN-ul contului de care aparține cardul: ");
                        String ibanCard = scanner.nextLine();
                        
                        Account contPentruCard = bankit.cautaContDupaIban(ibanCard);
                        if (contPentruCard == null) {
                            System.out.println("Contul nu a fost găsit.");
                            break;
                        }

                        System.out.print("Introdu numărul cardului pe care vrei să îl blochezi: ");
                        String numarCard = scanner.nextLine();
                        
                        boolean cardGasit = false;
                        for (Card c : contPentruCard.getCarduri()) {
                            if (c != null && c.getNumarCard().equals(numarCard) && !c.isBlocat()) {
                                c.blocheazaCard(); 
                                System.out.println("Cardul " + numarCard + " a fost blocat cu succes!");
                                cardGasit = true;
                                break;
                            } else if (c != null && c.getNumarCard().equals(numarCard) && c.isBlocat()) {
                                System.out.println("Cardul " + numarCard + " este deja blocat.");
                                cardGasit = true;
                                break;
                            }
                        }
                        if (!cardGasit) {
                            System.out.println("Nu am găsit acest card atașat contului.");
                        }
                    }
                    catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;

                case "9":
                    try {
                        System.out.print("Introdu ID-ul clientului: ");
                        int idClientRev = Integer.parseInt(scanner.nextLine());
                        Client clientRev = bankit.cautaClientDupaId(idClientRev);

                        if (clientRev == null) {
                            System.out.println("Client negăsit.");
                            break;
                        }

                        System.out.println("Puncte disponibile: " + clientRev.getPuncteRev());
                        System.out.println("Catalog Recompense:");
                        
                        Reward[] catalog = bankit.getCatalogRewards();
                        for (int i = 0; i < bankit.getNumarRewardsCatalog(); i++) {
                            System.out.println((i + 1) + ". " + catalog[i].toString());
                        }

                        System.out.print("Alege numărul recompensei dorite (sau 0 pentru anulare): ");
                        int alegere = Integer.parseInt(scanner.nextLine());

                        if (alegere > 0 && alegere <= bankit.getNumarRewardsCatalog()) {
                            Reward rewardAles = bankit.getRewardDinCatalog(alegere - 1);
                            
                            if (bankit.revendicaReward(clientRev, rewardAles)) {
                                bankit.afiseazaDetaliiSpecificeReward(rewardAles);
                            }
                        } else {
                            System.out.println("Revocare operațiune.");
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;

                case "10":
                    try {
                        System.out.print("Introdu ID-ul clientului: ");
                        int idAbonament = Integer.parseInt(scanner.nextLine());

                        Client clientAbonament = bankit.cautaClientDupaId(idAbonament);
                        if (clientAbonament == null) {
                            System.out.println("Client invalid.");
                            break;
                        }

                        System.out.println("Abonamentul curent: " + clientAbonament.getAbonament());
                        
                        if (clientAbonament instanceof PersoanaFizica) {
                            if (clientAbonament.getAbonament() == TipAbonament.PREMIUM) {
                                 System.out.println("Ai deja abonament PREMIUM.");
                            } else {
                                System.out.print("Opțiune disponibilă pentru upgrade: 1. PREMIUM: ");
                                String optNou = scanner.nextLine();
                                if (optNou.equals("1")) {
                                    clientAbonament.setAbonament(TipAbonament.PREMIUM);
                                    System.out.println("Upgrade realizat cu succes la PREMIUM!");
                                } else {
                                    System.out.println("Opțiune invalidă.");
                                }
                            }
                        } 
                        else if (clientAbonament instanceof PersoanaJuridica) {
                            if (clientAbonament.getAbonament() == TipAbonament.BUSINESS_PRO) {
                                System.out.println("Compania are deja abonamentul BUSINESS_PRO.");
                                
                            } else if (clientAbonament.getAbonament() == TipAbonament.STANDARD) {
                                System.out.print("Opțiune de upgrade pentru companii: 1. BUSINESS_PRO : ");
                                String optNou = scanner.nextLine();
                                if (optNou.equals("1")) {
                                    clientAbonament.setAbonament(TipAbonament.BUSINESS_PRO);
                                    System.out.println("Upgrade realizat cu succes la BUSINESS_PRO!");
                                } else {
                                    System.out.println("Opțiune invalidă.");
                                }
                            }
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Eroare la date: " + e.getMessage());
                    } catch (Exception e) {
                        System.out.println("A apărut o eroare neașteptată.");
                        scanner.nextLine();
                    }
                    System.out.println();
                    break;

                
                case "11":
                    System.out.print("Introdu IBAN-ul contului: ");
                    String ibanExtras = scanner.nextLine();
                    bankit.genereazaExtrasDeCont(ibanExtras);
                    break;

                case "0":
                    System.out.println("La revedere!");
                    ruleaza = false;
                    break;
                    
                default:
                    System.out.println("Opțiune invalidă.");
                    break;
            }
        }
    }
        
}