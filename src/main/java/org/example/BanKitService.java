package org.example;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.example.enums.StatusTranzactie;
import org.example.enums.TipAbonament;
import org.example.enums.TipTranzactie;
import org.example.enums.ValuteAcceptate;
import org.example.exceptions.*;

public class BanKitService {

    private final Connection conn;
    private final List<Reward> catalogRewards = new ArrayList<>();
    private int contorIban = 123456;

    public BanKitService() {
        this.conn = DatabaseConnection.getInstance().getConnection();
        initializeazaCatalogRewards();
    }

    private Client clientDinBD(ResultSet rs) {
        try {
            String adresa = rs.getString("adresa");
            LocalDate dataInrolare = rs.getDate("data_inrolare").toLocalDate();
            TipAbonament abonament = TipAbonament.valueOf(rs.getString("abonament").toUpperCase());
            int idClient = rs.getInt("id_client");
            int puncteRev = rs.getInt("puncte_rev");
            String parolaHash = rs.getString("parola_hash");
            String cnp = rs.getString("cnp");

            Client client;
            if (cnp != null && !cnp.isBlank()) {
                client = new PersoanaFizica(adresa, dataInrolare, abonament, null,
                        rs.getString("nume"), rs.getString("prenume"), cnp);
            } else {
                client = new PersoanaJuridica(adresa, dataInrolare, abonament, null,
                        rs.getString("denumire_companie"), rs.getString("cui"), rs.getString("reprezentant_legal"));
            }
            client.setIdClient(idClient);
            client.setPuncteRev(puncteRev);
            client.setParolaHash(parolaHash);
            return client;
        }catch (SQLException e){
            throw new RuntimeException("Eroare la cautarea clientului in baza de date: " + e.getMessage(), e);
        }
    }

    private Account contDinBD(ResultSet rs) {
        try {
            String iban = rs.getString("iban");
            String tip = rs.getString("tip");
            double sold = rs.getDouble("sold");
            ValuteAcceptate valuta = ValuteAcceptate.valueOf(rs.getString("valuta"));

            if ("CONT_CURENT".equals(tip)) {
                return new ContCurent(iban, sold, valuta, rs.getDouble("taxa_administrare"));
            } else {
                return new ContDeEconomii(iban, sold, valuta, rs.getDouble("rata_dobanda"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea contului in baza de date: " + e.getMessage(), e);
        }
    }

    private void incarcaCarduriPentruCont(Account cont) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM carduri WHERE iban_cont=?");
        ps.setString(1, cont.getIban());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Card card = cardDinBD(rs);
            card.setContSursa((ContCurent) cont);
            cont.adaugaCard(card);
        }
        rs.close();
    }

    private Card cardDinBD(ResultSet rs) {
        try {
            String numarCard = rs.getString("numar_card");
            String tip = rs.getString("tip");
            String pin = rs.getString("pin");
            boolean isBlocat = rs.getBoolean("is_blocat");
            double limitaZilnica = rs.getDouble("limita_zilnica");

            Card card;
            if ("CARD_DEBIT".equals(tip)) {
                card = new CardDebit(numarCard, pin, rs.getDouble("limita_contactless"));
            } else {
                CardCredit cc = new CardCredit(numarCard, pin, rs.getDouble("limita_credit"));
                cc.setDatorieCurenta(rs.getDouble("datorie_curenta"));
                card = cc;
            }
            if (limitaZilnica > 0 && limitaZilnica < Double.MAX_VALUE) {
                card.setLimitaZilnica(limitaZilnica);
            }
            return card;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea cardului in baza de date: " + e.getMessage(), e);
        }
    }

    private void actualizeazaSoldCont(String iban, double sold){
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE conturi SET sold=? WHERE iban=?");
            ps.setDouble(1, sold);
            ps.setString(2, iban);
            ps.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea soldului in baza de date: " + e.getMessage(), e);
        }
    }

    public void sincronizeazaConturi(Client client) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT iban, sold FROM conturi WHERE id_titular=?");
            ps.setInt(1, client.getIdClient());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String iban = rs.getString("iban");
                double sold = rs.getDouble("sold");
                client.getConturi().stream()
                        .filter(c -> c.getIban().equals(iban))
                        .findFirst()
                        .ifPresent(c -> c.setSold(sold));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la sincronizarea conturilor: " + e.getMessage(), e);
        }
    }

    public void sincronizeazaPuncte(Client client) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT puncte_rev FROM clienti WHERE id_client=?");
            ps.setInt(1, client.getIdClient());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) client.setPuncteRev(rs.getInt("puncte_rev"));
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la sincronizarea punctelor: " + e.getMessage(), e);
        }
    }

    private void actualizeazaPuncteClient(int idClient, int puncteRev){
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE clienti SET puncte_rev=? WHERE id_client=?");
            ps.setInt(1, puncteRev);
            ps.setInt(2, idClient);
            ps.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException("Eroare la actualizarea punctelor clientului in baza de date: " + e.getMessage(), e);
        }
    }

    private void actualizeazaParolaClient(int idClient, String parolaHash){
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE clienti SET parola_hash=? WHERE id_client=?");
            ps.setString(1, parolaHash);
            ps.setInt(2, idClient);
            ps.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("Eroare la actualizarea parolei clientului in baza de date: " + e.getMessage(), e);
            }
    }

    private void actualizeazaCard(Card card){
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE carduri SET is_blocat=?, pin=?, datorie_curenta=? WHERE numar_card=?");
            ps.setBoolean(1, card.isBlocat());
            ps.setString(2, card.getPin());
            ps.setDouble(3, card instanceof CardCredit cc ? cc.getDatorieCurenta() : 0);
            ps.setString(4, card.getNumarCard());
            ps.executeUpdate();
        }catch (SQLException e) {
            throw new RuntimeException("Eroare la actualizarea cardului in baza de date: " + e.getMessage(), e);
        }
    }

    public Client cautaClientDupaIdCuConturi(int idClient) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM clienti WHERE id_client=?");
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Client client = clientDinBD(rs);
            rs.close();

            PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM conturi WHERE id_titular=?");
            ps2.setInt(1, idClient);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                Account cont = contDinBD(rs2);
                cont.setTitular(client);
                incarcaCarduriPentruCont(cont);
                client.adaugaCont(cont);
            }
            rs2.close();
            return client;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea clientului: " + e.getMessage(), e);
        }
    }

    public Client cautaClientDupaCNP(String cod) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM clienti WHERE cnp=? OR cui=?");
            ps.setString(1, cod);
            ps.setString(2, cod);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Client client = clientDinBD(rs);
            int idClient = client.getIdClient();
            rs.close();

            PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM conturi WHERE id_titular=?");
            ps2.setInt(1, idClient);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                Account cont = contDinBD(rs2);
                cont.setTitular(client);
                incarcaCarduriPentruCont(cont);
                client.adaugaCont(cont);
            }
            rs2.close();
            return client;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea clientului dupa cod: " + e.getMessage(), e);
        }
    }

    private Client cautaClientDupaIdFaraConturi(int idClient){
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM clienti WHERE id_client=?");
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Client client = clientDinBD(rs);
            rs.close();
            return client;
        }catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea clientului in baza de date: " + e.getMessage(), e);
        }
    }

    public Account cautaContDupaIban(String iban) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM conturi WHERE iban=?");
            ps.setString(1, iban);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Account cont = contDinBD(rs);
            int idTitular = rs.getInt("id_titular");
            rs.close();

            Client titular = cautaClientDupaIdFaraConturi(idTitular);
            cont.setTitular(titular);
            return cont;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea contului: " + e.getMessage(), e);
        }
    }

    public Card cautaCardDupaNumar(String numarCard) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM carduri WHERE numar_card=?");
            ps.setString(1, numarCard);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;
            Card card = cardDinBD(rs);
            String ibanCont = rs.getString("iban_cont");
            rs.close();

            Account cont = cautaContDupaIban(ibanCont);
            card.setContSursa((ContCurent) cont);
            return card;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la cautarea cardului: " + e.getMessage(), e);
        }
    }

    public String genereazaIban() {
        String iban;
        do {
            iban = "RO00BTRL" + String.format("%016d", contorIban++);
        } while (cautaContDupaIban(iban) != null);
        return iban;
    }

    public Client autentifica(int idClient, String parola) {
        Client client = cautaClientDupaIdCuConturi(idClient);
        if (client == null || !client.verificaParola(parola)) {
            CsvAuditService.getInstance().logAction("LOGIN_ESUAT");
            throw new AuthenticationException();
        }
        CsvAuditService.getInstance().logAction("LOGIN_REUSIT");
        return client;
    }

    public void adaugaClient(Client client) {
        if (client == null) return;
        try {
            String checkSql = (client instanceof PersoanaFizica)
                    ? "SELECT COUNT(*) FROM clienti WHERE cnp=?"
                    : "SELECT COUNT(*) FROM clienti WHERE cui=?";
            PreparedStatement check = conn.prepareStatement(checkSql);
            check.setString(1, client.getIdentificatorUnic());
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Clientul există deja în sistem.");
                return;
            }
            rs.close();

            String sql = """
                    INSERT INTO clienti (adresa, parola_hash, puncte_rev, data_inrolare,
                        abonament, nume, prenume, cnp, denumire_companie, cui, reprezentant_legal)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, client.getAdresa());
            ps.setString(2, client.getParolaHash());
            ps.setInt(3, client.getPuncteRev());
            ps.setDate(4, Date.valueOf(client.getDataInrolare()));
            ps.setString(5, client.getAbonament().name());

            if (client instanceof PersoanaFizica pf) {
                ps.setString(6, pf.getNume());
                ps.setString(7, pf.getPrenume());
                ps.setString(8, pf.getIdentificatorUnic());
                ps.setNull(9, Types.VARCHAR);
                ps.setNull(10, Types.VARCHAR);
                ps.setNull(11, Types.VARCHAR);
            } else {
                PersoanaJuridica pj = (PersoanaJuridica) client;
                ps.setNull(6, Types.VARCHAR);
                ps.setNull(7, Types.VARCHAR);
                ps.setNull(8, Types.VARCHAR);
                ps.setString(9, pj.getNumeComplet());
                ps.setString(10, pj.getIdentificatorUnic());
                ps.setString(11, pj.getReprezentantLegal());
            }
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    client.setIdClient(generatedKeys.getInt(1));
                }
            }
            CsvAuditService.getInstance().logAction("ADAUGA_CLIENT");
            System.out.println("Client adăugat cu ID: " + client.getIdClient());
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la adăugarea clientului: " + e.getMessage(), e);
        }
    }

    public void deschideCont(Client client, Account cont) {
        try {
            PreparedStatement check = conn.prepareStatement("SELECT COUNT(*) FROM conturi WHERE iban=?");
            check.setString(1, cont.getIban());
            ResultSet rs = check.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Un cont cu IBAN-ul " + cont.getIban() + " există deja în sistem.");
                return;
            }
            rs.close();

            String sql = """
                    INSERT INTO conturi (iban, tip, sold, valuta, id_titular, taxa_administrare, rata_dobanda)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cont.getIban());
            ps.setDouble(3, cont.getSold());
            ps.setString(4, cont.getValuta());
            ps.setInt(5, client.getIdClient());

            if (cont instanceof ContCurent cc) {
                ps.setString(2, "CONT_CURENT");
                ps.setDouble(6, cc.getTaxaAdministrare());
                ps.setNull(7, Types.DOUBLE);
            } else {
                ContDeEconomii ce = (ContDeEconomii) cont;
                ps.setString(2, "CONT_ECONOMII");
                ps.setNull(6, Types.DOUBLE);
                ps.setDouble(7, ce.getRataDobanda());
            }
            ps.executeUpdate();
            cont.setTitular(client);
            CsvAuditService.getInstance().logAction("DESCHIDE_CONT");
            System.out.println("Cont " + cont.getValuta() + " deschis cu succes (" + cont.getIban() + ").");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la deschiderea contului: " + e.getMessage(), e);
        }
    }

    public void depunereBani(String iban, double suma) {
        try {
            Account cont = cautaContDupaIban(iban);
            if (cont == null) throw new ContNotFoundException(iban);
            cont.depunere(suma);
            actualizeazaSoldCont(iban, cont.getSold());
            Tranzactie t = new Tranzactie(suma, TipTranzactie.DEPUNERE, cont, "Depunere numerar");
            t.marcheazaCompletata();
            adaugaTranzactieInIstoric(t);
            CsvAuditService.getInstance().logAction("DEPUNERE_BANI");
            System.out.println("Depunere de " + suma + " " + cont.getValuta() + " realizată cu succes în contul " + iban);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la depunere: " + e.getMessage(), e);
        }
    }

    public void transferaBani(Client clientSursa, String ibanSursa, String ibanDestinatie, double suma, String detalii) {
        try {
            Account contSursa = cautaContDupaIban(ibanSursa);
            Account contDestinatie = cautaContDupaIban(ibanDestinatie);

            if (contSursa == null) throw new ContNotFoundException(ibanSursa);
            if (contDestinatie == null) throw new ContNotFoundException(ibanDestinatie);
            if (!contSursa.getValuta().equals(contDestinatie.getValuta()))
                throw new BanKitException("Transferurile se pot face doar între conturi cu aceeași valută.");


            if (!contSursa.retragere(suma)) throw new FonduriInsuficienteException(suma, contSursa.getValuta());
            contDestinatie.depunere(suma);
            actualizeazaSoldCont(ibanSursa, contSursa.getSold());
            actualizeazaSoldCont(ibanDestinatie, contDestinatie.getSold());

            Tranzactie t = new Tranzactie(suma, TipTranzactie.TRANSFER_BANCAR, contSursa, contDestinatie, detalii);
            t.marcheazaCompletata();
            adaugaTranzactieInIstoric(t);
            CsvAuditService.getInstance().logAction("TRANSFER_BANCAR");
            System.out.println("Transfer de " + suma + " " + contSursa.getValuta() + " realizat cu succes către " + ibanDestinatie);
        } catch (Exception e) {
            throw new RuntimeException("Eroare la transfer: " + e.getMessage(), e);
        }
    }

    public void proceseazaPlataCuCard(String numarCard, double suma, String detaliiComerciant) {
        try {
            Card card = cautaCardDupaNumar(numarCard);
            if (card == null) throw new ContNotFoundException(numarCard);

            Account cont = card.getContSursa();
            Client client = cont.getTitular();

            if (card.isBlocat()) throw new CardBlocatException(numarCard);
            card.verificaSiInregistreazaCheltuiala(suma);

            if (card instanceof CardCredit cardCredit) {
                if (!cardCredit.potCheltui(suma))
                    throw new LimitaCreditDepasitaException(suma, cardCredit.getLimitaCredit());
                cardCredit.inregistreazaCheltuiala(suma);
                actualizeazaCard(card);
                System.out.println("Plată acceptată. Datoria ta curentă este acum: " + cardCredit.getDatorieCurenta());
            } else if (card instanceof CardDebit) {
                if (!cont.retragere(suma)) throw new FonduriInsuficienteException(suma, cont.getValuta());
                actualizeazaSoldCont(cont.getIban(), cont.getSold());
                System.out.println("Plată acceptată. Sold rămas: " + cont.getSold());
            }

            if (client instanceof PersoanaFizica) {
                int puncteCastigate = (int) ((suma / 10) * client.getAbonament().getMultiplicatorPuncte());
                client.setPuncteRev(client.getPuncteRev() + puncteCastigate);
                actualizeazaPuncteClient(client.getIdClient(), client.getPuncteRev());
                System.out.println("Bonus: Ai câștigat " + puncteCastigate + " RevPoints!");
            } else if (client instanceof PersoanaJuridica) {
                double cashback = suma * 0.01;
                cont.depunere(cashback);
                actualizeazaSoldCont(cont.getIban(), cont.getSold());
                System.out.println("Bonus: Cashback de " + cashback + " " + cont.getValuta() + " a fost adăugat în cont!");
            }

            Tranzactie t = new Tranzactie(suma, TipTranzactie.PLATA_CARD, cont, detaliiComerciant);
            t.marcheazaCompletata();
            adaugaTranzactieInIstoric(t);
            CsvAuditService.getInstance().logAction("PLATA_CARD");
        } catch (CardBlocatException e) {
            System.out.println(e);
        }
    }

    public void schimbaValuta(String ibanSursa, String ibanDestinatie, double sumaSursa) {
        try {
            Account contSursa = cautaContDupaIban(ibanSursa);
            Account contDestinatie = cautaContDupaIban(ibanDestinatie);

            if (contSursa == null) throw new ContNotFoundException(ibanSursa);
            if (contDestinatie == null) throw new ContNotFoundException(ibanDestinatie);
            if (!contSursa.getTitular().equals(contDestinatie.getTitular()))
                throw new BanKitException("Schimbul valutar se poate face doar între propriile tale conturi!");
            if (contSursa.getValuta().equals(contDestinatie.getValuta()))
                throw new BanKitException("Conturile au aceeași valută. Pentru a muta banii, folosește opțiunea de transfer.");

            Client client = contSursa.getTitular();
            double procentComision = client.getAbonament().getComisionSchimb();
            double valoareComision = sumaSursa * procentComision;
            double sumaTotalaDeRetras = sumaSursa + valoareComision;

            if (!contSursa.retragere(sumaTotalaDeRetras))
                throw new FonduriInsuficienteException(sumaTotalaDeRetras, contSursa.getValuta());

            double rataSchimb = obtineRataSchimb(contSursa.getValuta(), contDestinatie.getValuta());
            double sumaConvertita = sumaSursa * rataSchimb;
            contDestinatie.depunere(sumaConvertita);

            actualizeazaSoldCont(ibanSursa, contSursa.getSold());
            actualizeazaSoldCont(ibanDestinatie, contDestinatie.getSold());

            System.out.println("Schimb valutar realizat cu succes.");
            System.out.printf("Retras: %.2f %s (comision: %.2f). Depus: %.2f %s (1 %s = %.4f %s)%n",
                    sumaTotalaDeRetras, contSursa.getValuta(), valoareComision,
                    sumaConvertita, contDestinatie.getValuta(),
                    contSursa.getValuta(), rataSchimb, contDestinatie.getValuta());

            Tranzactie t = new Tranzactie(sumaSursa, TipTranzactie.SCHIMB_VALUTAR, contSursa, contDestinatie,
                    "Schimb Valutar " + contSursa.getValuta() + " -> " + contDestinatie.getValuta());
            t.marcheazaCompletata();
            adaugaTranzactieInIstoric(t);
            CsvAuditService.getInstance().logAction("SCHIMB_VALUTAR");
        } catch (Exception e) {
            throw new RuntimeException("Eroare la schimbul valutar: " + e.getMessage(), e);
        }
    }

    public void emiteCardPentruCont(String iban, Card cardNou) {
        try {
            Account cont = cautaContDupaIban(iban);
            if (cont == null) throw new ContNotFoundException(iban);
            if (cont instanceof ContDeEconomii)
                throw new BanKitException("Nu se pot emite carduri pentru conturile de economii.");

            cardNou.setContSursa((ContCurent) cont);

            String sql = """
                    INSERT INTO carduri (numar_card, tip, pin, is_blocat, iban_cont,
                        limita_zilnica, limita_contactless, limita_credit, datorie_curenta)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cardNou.getNumarCard());
            ps.setBoolean(4, cardNou.isBlocat());
            ps.setString(5, iban);
            ps.setDouble(6, cardNou.getLimitaZilnica());
            ps.setString(3, cardNou.getPin());

            if (cardNou instanceof CardDebit cd) {
                ps.setString(2, "CARD_DEBIT");
                ps.setDouble(7, cd.getLimitaContactless());
                ps.setNull(8, Types.DOUBLE);
                ps.setDouble(9, 0);
            } else {
                CardCredit cc = (CardCredit) cardNou;
                ps.setString(2, "CARD_CREDIT");
                ps.setNull(7, Types.DOUBLE);
                ps.setDouble(8, cc.getLimitaCredit());
                ps.setDouble(9, cc.getDatorieCurenta());
            }
            ps.executeUpdate();
            CsvAuditService.getInstance().logAction("EMITE_CARD");
            System.out.println("Cardul " + cardNou.getNumarCard() + " a fost emis cu succes pentru contul " + iban);
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la emiterea cardului: " + e.getMessage(), e);
        }
    }

    public void blocheazaCard(String numarCard, Client client) {
        try {
            Card card = cautaCardDupaNumar(numarCard);
            if (card == null) throw new ContNotFoundException(numarCard);
            if (client != null && !card.getContSursa().getTitular().equals(client))
                throw new BanKitException("Cardul nu aparține clientului specificat.");
            if (card.isBlocat()) throw new BanKitException("Cardul este deja blocat.");
            card.blocheazaCard();
            actualizeazaCard(card);
            CsvAuditService.getInstance().logAction("CARD_BLOCAT");
            System.out.println("Cardul a fost blocat cu succes!");
        } catch (Exception e) {
            throw new RuntimeException("Eroare la blocarea cardului: " + e.getMessage(), e);
        }
    }

    public void cautaExistentaCard(Client client) {
        List<Account> listaConturi = client.getConturi();
        for (var cont : listaConturi) {
            if (!cont.getCarduri().isEmpty()) return;
        }
        throw new BanKitException("Nu au fost eliberate carduri pentru acest client.");
    }

    public void deblocheazaCard(String numarCard, Client client) {
        try {
            Card card = cautaCardDupaNumar(numarCard);
            if (card == null) throw new ContNotFoundException(numarCard);
            if (client != null && !card.getContSursa().getTitular().equals(client))
                throw new BanKitException("Cardul nu aparține clientului specificat.");
            if (!card.isBlocat()) throw new BanKitException("Cardul nu este blocat.");
            card.deblocheazaCard();
            actualizeazaCard(card);
            CsvAuditService.getInstance().logAction("CARD_DEBLOCAT");
            System.out.println("Cardul a fost deblocat cu succes!");
        } catch (Exception e) {
            throw new RuntimeException("Eroare la deblocarea cardului: " + e.getMessage(), e);
        }
    }

    public void schimbaPinCard(String numarCard, String pinVechi, String pinNou, Client client) {
        try {
            Card card = cautaCardDupaNumar(numarCard);
            if (card == null) throw new ContNotFoundException(numarCard);
            if (!card.getContSursa().getTitular().equals(client))
                throw new BanKitException("Cardul nu aparține clientului specificat.");
            card.schimbaPin(pinVechi, pinNou);
            actualizeazaCard(card);
            CsvAuditService.getInstance().logAction("PIN_SCHIMBAT");
            System.out.println("PIN schimbat cu succes!");
        } catch (Exception e) {
            throw new RuntimeException("Eroare la schimbarea PIN-ului: " + e.getMessage(), e);
        }
    }

    public void setLimitaZilnicaCard(String numarCard, double limita, Client client) {
        try {
            Card card = cautaCardDupaNumar(numarCard);
            if (card == null) throw new ContNotFoundException(numarCard);
            if (!card.getContSursa().getTitular().equals(client))
                throw new BanKitException("Cardul nu aparține clientului specificat.");
            card.setLimitaZilnica(limita);
            PreparedStatement ps = conn.prepareStatement("UPDATE carduri SET limita_zilnica=? WHERE numar_card=?");
            ps.setDouble(1, limita);
            ps.setString(2, numarCard);
            ps.executeUpdate();
            CsvAuditService.getInstance().logAction("LIMITA_ZILNICA_SETATA");
            System.out.printf("Limita zilnică a fost setată la %.2f %s%n", limita, card.getContSursa().getValuta());
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la setarea limitei zilnice: " + e.getMessage(), e);
        }
    }

    public void adaugaTranzactieInIstoric(Tranzactie t) {
        try {
            String sql = """
                    INSERT INTO tranzactii (data_tranzactie, data_modificare, suma, tip,
                        iban_sursa, iban_destinatie, detalii, status_t)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setTimestamp(1, Timestamp.valueOf(t.getDataTranzactie()));
            ps.setTimestamp(2, t.getDataModificare() != null ? Timestamp.valueOf(t.getDataModificare()) : null);
            ps.setDouble(3, t.getSuma());
            ps.setString(4, t.getTip().name());
            ps.setString(5, t.getContSursa().getIban());
            ps.setString(6, t.getContDestinatie() != null ? t.getContDestinatie().getIban() : null);
            ps.setString(7, t.getDetalii());
            ps.setString(8, t.getStatus().name());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    t.setIdTranzactie(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la salvarea tranzacției: " + e.getMessage(), e);
        }
    }

    public void aplicaDobanziLunare() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM conturi WHERE tip='CONT_ECONOMII'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ContDeEconomii econ = (ContDeEconomii) contDinBD(rs);
                double dobanda = econ.adaugaDobanda();
                if (dobanda > 0) {
                    actualizeazaSoldCont(econ.getIban(), econ.getSold());
                    CsvAuditService.getInstance().logAction("DOBANDA_APLICATA");
                    System.out.printf("Dobândă de %.2f %s aplicată pe contul %s%n",
                            dobanda, econ.getValuta(), econ.getIban());
                }
            }
            System.out.println("Dobânzi lunare aplicate cu succes.");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la aplicarea dobânzilor: " + e.getMessage(), e);
        }
    }

    public void aplicaTaxeAdministrare() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM conturi WHERE tip='CONT_CURENT'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ContCurent curent = (ContCurent) contDinBD(rs);
                try {
                    curent.aplicaTaxaLunara();
                    actualizeazaSoldCont(curent.getIban(), curent.getSold());
                    CsvAuditService.getInstance().logAction("TAXA_APLICATA");
                    System.out.printf("Taxă de %.2f %s aplicată pe contul %s%n",
                            curent.getTaxaAdministrare(), curent.getValuta(), curent.getIban());
                } catch (IllegalStateException e) {
                    System.out.println("AVERTISMENT: " + curent.getIban() + " - " + e.getMessage());
                }
            }
            System.out.println("Taxe de administrare aplicate cu succes.");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la aplicarea taxelor: " + e.getMessage(), e);
        }
    }

    public void schimbaParolaClient(Client client, String parolaVeche, String parolaNou) {
        try {
            client.schimbaParola(parolaVeche, parolaNou);
            actualizeazaParolaClient(client.getIdClient(), client.getParolaHash());
            CsvAuditService.getInstance().logAction("PAROLA_SCHIMBATA");
            System.out.println("Parola a fost schimbată cu succes!");
        } catch (Exception e) {
            throw new RuntimeException("Eroare la schimbarea parolei: " + e.getMessage(), e);
        }
    }

    public void afiseazaClientiSortati() {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM clienti");
            ResultSet rs = ps.executeQuery();
            List<Client> toti = new ArrayList<>();
            while (rs.next()) {
                Client client = clientDinBD(rs);
                PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM conturi WHERE id_titular=?");
                ps2.setInt(1, client.getIdClient());
                ResultSet rs2 = ps2.executeQuery();
                while (rs2.next()) {
                    Account cont = contDinBD(rs2);
                    cont.setTitular(client);
                    client.adaugaCont(cont);
                }
                rs2.close();
                toti.add(client);
            }
            rs.close();
            if (toti.isEmpty()) {
                System.out.println("Nu există clienți adăugați.");
                return;
            }
            Collections.sort(toti);
            for (Client c : toti) System.out.println(c);
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la afișarea clienților: " + e.getMessage(), e);
        }
    }

    public void genereazaExtrasDeCont(String iban, String perioadaInceput, String perioadaSfarsit) {
        Account cont = cautaContDupaIban(iban);
        if (cont == null) throw new ContNotFoundException(iban);

        System.out.println("EXTRAS DE CONT: " + iban);
        System.out.println("Perioada: " + perioadaInceput + " - " + perioadaSfarsit);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");
        try {
            YearMonth start = YearMonth.parse(perioadaInceput, formatter);
            YearMonth end = YearMonth.parse(perioadaSfarsit, formatter);
            Timestamp tsStart = Timestamp.valueOf(start.atDay(1).atStartOfDay());
            Timestamp tsEnd = Timestamp.valueOf(end.plusMonths(1).atDay(1).atStartOfDay());

            String sql = """
                    SELECT t.id_tranzactie, t.data_tranzactie, t.suma, t.tip,
                           t.iban_sursa, t.iban_destinatie, t.detalii, t.status_t, c.valuta
                    FROM tranzactii t
                    JOIN conturi c ON t.iban_sursa = c.iban
                    WHERE (t.iban_sursa=? OR t.iban_destinatie=?)
                      AND t.data_tranzactie >= ? AND t.data_tranzactie < ?
                    ORDER BY t.data_tranzactie
                    """;
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, iban);
            ps.setString(2, iban);
            ps.setTimestamp(3, tsStart);
            ps.setTimestamp(4, tsEnd);
            ResultSet rs = ps.executeQuery();

            boolean areTranzactii = false;
            DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            while (rs.next()) {
                areTranzactii = true;
                int id = rs.getInt("id_tranzactie");
                String data = rs.getTimestamp("data_tranzactie").toLocalDateTime().format(dtFormatter);
                double suma = rs.getDouble("suma");
                String tip = rs.getString("tip");
                String ibanSursa = rs.getString("iban_sursa");
                String ibanDest = rs.getString("iban_destinatie");
                String detalii = rs.getString("detalii");
                String status = StatusTranzactie.valueOf(rs.getString("status_t")).getDescriere();
                String valuta = rs.getString("valuta");

                String infoDestinatie = (ibanDest != null) ? " în contul " + ibanDest : "";
                System.out.printf("Tranzacția %d la %s: %s de %.2f %s din contul %s%s. Detalii: %s (Status: %s)%n",
                        id, data, tip, suma, valuta, ibanSursa, infoDestinatie, detalii, status);
            }
            if (!areTranzactii) System.out.println("Nu există nicio tranzacție înregistrată în această perioadă.");
            System.out.println("Sold curent: " + cont.getSold() + " " + cont.getValuta());
        } catch (Exception e) {
            System.out.println("Eroare: Introdu datele exact în formatul MM/yyyy (ex: 04/2026).");
        }
    }

    public List<String> getRewardsRevendicate(int idClient) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT id_oferta FROM revendicari WHERE id_client=?");
            ps.setInt(1, idClient);
            ResultSet rs = ps.executeQuery();
            List<String> ids = new ArrayList<>();
            while (rs.next()) ids.add(rs.getString("id_oferta"));
            rs.close();
            return ids;
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la citirea revendicărilor: " + e.getMessage(), e);
        }
    }

    public void revendicaReward(Client client, Reward reward) {
        if (cautaClientDupaIdCuConturi(client.getIdClient()) == null) throw new ClientNotFoundException(client.getIdClient());
        try {
            PreparedStatement psVerifica = conn.prepareStatement(
                    "SELECT COUNT(*) FROM revendicari WHERE id_client=? AND id_oferta=?");
            psVerifica.setInt(1, client.getIdClient());
            psVerifica.setString(2, reward.getIdOferta());
            ResultSet rs = psVerifica.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                throw new BanKitException("Ai revendicat deja această recompensă.");
            rs.close();

            if (client.getPuncteRev() < reward.getCostPuncte())
                throw new PuncteInsuficienteException(client.getPuncteRev(), reward.getCostPuncte());

            client.setPuncteRev(client.getPuncteRev() - reward.getCostPuncte());
            actualizeazaPuncteClient(client.getIdClient(), client.getPuncteRev());

            PreparedStatement psRevendica = conn.prepareStatement(
                    "INSERT INTO revendicari (id_client, id_oferta) VALUES (?, ?)");
            psRevendica.setInt(1, client.getIdClient());
            psRevendica.setString(2, reward.getIdOferta());
            psRevendica.executeUpdate();

            CsvAuditService.getInstance().logAction("REVENDICA_REWARD");
            System.out.println("Recompensă revendicată cu succes pentru client " + client.getIdClient() + ".");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la revendicarea recompensei: " + e.getMessage(), e);
        }
    }

    public void incaseazaTaxaAbonament(Client client, double suma) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM conturi WHERE id_titular=? AND tip='CONT_CURENT' LIMIT 1");
            ps.setInt(1, client.getIdClient());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) throw new BanKitException("Clientul nu are niciun Cont Curent activ.");

            ContCurent cont = (ContCurent) contDinBD(rs);
            rs.close();

            if (!cont.retragere(suma)) throw new FonduriInsuficienteException(suma, cont.getValuta());
            actualizeazaSoldCont(cont.getIban(), cont.getSold());

            System.out.println("S-au retras automat " + suma + " " + cont.getValuta()
                    + " din contul " + cont.getIban() + " pentru plata abonamentului.");

            Tranzactie t = new Tranzactie(suma, TipTranzactie.PLATA_CARD, cont, "Plată Upgrade Abonament");
            t.marcheazaCompletata();
            adaugaTranzactieInIstoric(t);
            CsvAuditService.getInstance().logAction("UPGRADE_ABONAMENT");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la incasarea taxei: " + e.getMessage(), e);
        }
    }

    public void afiseazaDetaliiSpecificeReward(Reward reward) {
        System.out.println("Detalii Recompensă " + reward.getIdOferta());
        System.out.println("Nume: " + reward.getNumeOferta());
        if (reward instanceof FlightReward zbor) {
            System.out.println("Tip: Zbor");
            System.out.println("Companie aeriană: " + zbor.getCompanieAeriana());
            System.out.println("Destinație: " + zbor.getDestinatie());
        } else if (reward instanceof AccommodationReward cazare) {
            System.out.println("Tip: Cazare");
            System.out.println("Hotel: " + cazare.getHotel());
            System.out.println("Număr nopți: " + cazare.getNumarNopti());
        }
        System.out.println("Detalii complete: " + reward);
    }

    public List<Reward> getCatalogRewards() {
        return java.util.Collections.unmodifiableList(catalogRewards);
    }

    private double obtineRataSchimb(String valutaSursa, String valutaDestinatie) {
        if (valutaSursa.equals("EUR") && valutaDestinatie.equals("RON")) return 5.20;
        if (valutaSursa.equals("RON") && valutaDestinatie.equals("EUR")) return 0.20;
        if (valutaSursa.equals("USD") && valutaDestinatie.equals("RON")) return 4.60;
        if (valutaSursa.equals("RON") && valutaDestinatie.equals("USD")) return 0.21;
        if (valutaSursa.equals("EUR") && valutaDestinatie.equals("USD")) return 1.08;
        if (valutaSursa.equals("USD") && valutaDestinatie.equals("EUR")) return 0.92;
        return 1.0;
    }

    private void initializeazaCatalogRewards() {
        catalogRewards.add(new FlightReward("F1", "Zbor Roma", 1500, "WizzAir", "Roma"));
        catalogRewards.add(new FlightReward("F2", "Zbor Paris", 3000, "Air France", "Paris"));
        catalogRewards.add(new FlightReward("F3", "Zbor Barcelona", 2500, "Ryanair", "Barcelona"));
        catalogRewards.add(new FlightReward("F4", "Zbor Londra", 3500, "British Airways","Londra"));
        catalogRewards.add(new FlightReward("F5", "Zbor Dubai", 6000, "Emirates","Dubai"));
        catalogRewards.add(new FlightReward("F6", "Zbor New York", 9000, "Delta Airlines", "New York"));
        catalogRewards.add(new FlightReward("F7", "Zbor Amsterdam", 2000, "KLM", "Amsterdam"));
        catalogRewards.add(new AccommodationReward("A1", "Hotel Sinaia", 1000, "Hotel Internațional", 2));
        catalogRewards.add(new AccommodationReward("A2", "Resort Antalya", 50000, "Delphin Be Grand", 7));
        catalogRewards.add(new AccommodationReward("A3", "Vila Predeal", 800, "Vila Montana", 3));
        catalogRewards.add(new AccommodationReward("A4", "Hotel Mamaia", 5000, "Vega Hotel", 5));
        catalogRewards.add(new AccommodationReward("A5", "Resort Maldive", 600000, "Sun Island Resort", 10));
        catalogRewards.add(new AccommodationReward("A6", "Pensiune Brasov", 700, "Pensiunea Codrilor", 2));
    }
}
