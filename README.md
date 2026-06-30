# BanKit — Sistem Bancar Java

Aplicație bancară completă implementată în Java, cu interfață în consolă și simulator ATM grafic (Swing), persistență în baza de date PostgreSQL și jurnal de audit CSV.

---

## Tehnologii folosite

- **Java 17+** cu programare orientată pe obiecte
- **PostgreSQL** — persistența datelor prin JDBC
- **Swing (Java AWT/Swing)** — interfața grafică pentru simulatorul ATM
- **Maven** — managementul dependențelor

---

## Arhitectura proiectului

### Entități principale

| Clasă | Descriere |
|-------|-----------|
| `Client` | Clasă abstractă de bază pentru clienți |
| `PersoanaFizica` | Client persoană fizică (nume, prenume, CNP) |
| `PersoanaJuridica` | Client persoană juridică (denumire, CUI, reprezentant legal) |
| `Account` | Clasă abstractă de bază pentru conturi bancare |
| `ContCurent` | Cont curent cu taxă lunară de administrare |
| `ContDeEconomii` | Cont de economii cu rată de dobândă |
| `Card` | Clasă abstractă de bază pentru carduri |
| `CardDebit` | Card de debit cu limită contactless |
| `CardCredit` | Card de credit cu limită de credit și datorie curentă |
| `Reward` | Clasă abstractă de bază pentru recompense |
| `FlightReward` | Recompensă zbor (companie aeriană, destinație) |
| `AccommodationReward` | Recompensă cazare (hotel, număr nopți) |
| `Tranzactie` | Înregistrarea unei operațiuni financiare |

### Enumerații

| Enum | Valori |
|------|--------|
| `TipAbonament` | `STANDARD`, `PREMIUM`, `BUSINESS_PRO` |
| `TipTranzactie` | `DEPUNERE`, `RETRAGERE_NUMERAR`, `TRANSFER_BANCAR`, `PLATA_CARD`, `SCHIMB_VALUTAR` |
| `StatusTranzactie` | `COMPLETATA`, `ANULATA`, `IN_PROCESARE` |
| `ValuteAcceptate` | `RON`, `EUR`, `USD` |

### Excepții personalizate

| Excepție | Situație |
|----------|----------|
| `BanKitException` | Eroare generală de business |
| `AuthenticationException` | Autentificare eșuată |
| `CardBlocatException` | Operațiune pe card blocat |
| `ClientNotFoundException` | Client negăsit |
| `ContNotFoundException` | Cont sau card negăsit |
| `FonduriInsuficienteException` | Sold insuficient |
| `LimitaCreditDepasitaException` | Depășire limită de credit |
| `LimitaZilnicaDepasitaException` | Depășire limită zilnică card |
| `PuncteInsuficienteException` | Puncte insuficiente pentru recompensă |

### Servicii

| Clasă | Rol |
|-------|-----|
| `BanKitService` | Serviciul principal cu toată logica de business și interacțiunea cu BD |
| `DatabaseConnection` | Singleton pentru conexiunea JDBC la PostgreSQL |
| `DatabaseInitializer` | Crearea automată a tabelelor la pornirea aplicației |
| `CsvAuditService` | Singleton pentru jurnalizarea acțiunilor în `audit.csv` |
| `BancomatGUI` | Interfața grafică Swing pentru simulatorul ATM |

---

## Tipuri de abonamente

| Abonament | Multiplicator RevPoints | Comision schimb valutar | Disponibil pentru |
|-----------|------------------------|--------------------------|-------------------|
| `STANDARD` | 1x | 1% | Persoane fizice |
| `PREMIUM` | 2x | 0% | Persoane fizice |
| `BUSINESS_PRO` | 0x | 0% | Persoane juridice |

---

## Funcționalități

### Meniu Admin

1. **Adaugă client** — înregistrare persoană fizică (CNP) sau persoană juridică (CUI) cu parolă și abonament ales
2. **Afișează clienți sortați** — lista tuturor clienților din baza de date, sortată alfabetic
3. **Deschide cont bancar** — deschidere Cont Curent sau Cont de Economii pentru un client, cu IBAN generat automat și valuta aleasă (RON / EUR / USD)
4. **Emite card pentru cont** — emitere Card Debit sau Card Credit pentru un Cont Curent, cu PIN și limite personalizate
5. **Blochează card** — blocarea unui card activ
6. **Deblochează card** — deblocarea unui card blocat
7. **Afișează conturile unui client** — vizualizarea tuturor conturilor unui client după ID
8. **Generează extras de cont** — toate tranzacțiile unui IBAN într-o perioadă specificată (format `MM/yyyy`)
9. **Aplică dobânzi lunare** — aplicarea ratei de dobândă pe toate Conturile de Economii
10. **Aplică taxe de administrare** — deducerea taxei lunare din toate Conturile Curente
11. **Jurnal audit** — vizualizarea fișierului `audit.csv` cu toate acțiunile loggate și timestamp-ul lor
12. **Caută client după CNP/CUI** — căutare client și afișarea conturilor sale
13. **Încasează taxă abonament** — retragere manuală a unei sume din contul curent al unui client

### Meniu Client

Autentificare prin CNP/CUI + parolă.

1. **Afișează conturile mele** — sold actualizat pentru toate conturile proprii
2. **Extras de cont** — tranzacțiile unui IBAN propriu pe o perioadă aleasă
3. **Depune bani** — depunere numerar într-un cont prin IBAN
4. **Transfer bancar** — transfer între două conturi cu aceeași valută, cu detalii personalizate
5. **Plată cu cardul** — procesare plată la comerciant cu debit sau credit:
   - **Card Debit**: scade din soldul contului
   - **Card Credit**: crește datoria curentă (în limita creditului)
   - **Bonus Persoană Fizică**: câștig de RevPoints proporțional cu suma și multiplicatorul abonamentului
   - **Bonus Persoană Juridică**: cashback de 1% returnat automat în cont
6. **Schimb valutar** — conversie între conturi proprii cu valute diferite, cu comision în funcție de abonament și rate fixe (EUR/RON: 5.20, USD/RON: 4.60, EUR/USD: 1.08)
7. **Schimbă PIN** — modificarea PIN-ului unui card propriu (necesită PIN vechi)
8. **Schimbă parola contului** — modificarea parolei de autentificare (necesită parolă veche + confirmare)
9. **Setează limită zilnică card** — configurarea limitei maxime de cheltuieli pe zi pentru un card
10. **Magazin rewards** — catalog de recompense revendicabile cu RevPoints:
    - Zboruri: Roma (1500 pct), Amsterdam (2000 pct), Barcelona (2500 pct), Paris (3000 pct), Londra (3500 pct), Dubai (6000 pct), New York (9000 pct)
    - Cazări: Pensiune Brașov (700 pct), Vilă Predeal (800 pct), Hotel Sinaia (1000 pct), Hotel Mamaia (5000 pct), Resort Antalya (50.000 pct), Resort Maldive (600.000 pct)
11. **Simulator ATM** — deschide interfața grafică Swing pentru bancomat

### Simulator ATM (Swing GUI)

Autentificare cu numărul cardului (16 cifre) și PIN. După autentificare, meniu grafic cu:

- **Interogare sold** — afișează soldul și valuta contului asociat cardului
- **Retragere numerar** — retrage suma introdusă; dacă fondurile sunt insuficiente, tranzacția este salvată cu status `ANULATA`
- **Depunere numerar** — adaugă suma în contul asociat cardului
- **Schimbare PIN** — modificarea PIN-ului direct din ATM (cardul se blochează automat după 3 tentative greșite)
- **Scoate cardul** — ieșire din sesiunea ATM

---

## Jurnal de audit

Fiecare operațiune importantă este înregistrată automat în `audit.csv` (prin `CsvAuditService`, implementat ca Singleton):

`LOGIN_REUSIT`, `LOGIN_ESUAT`, `ADAUGA_CLIENT`, `DESCHIDE_CONT`, `EMITE_CARD`, `CARD_BLOCAT`, `CARD_DEBLOCAT`, `PIN_SCHIMBAT`, `DEPUNERE_BANI`, `TRANSFER_BANCAR`, `PLATA_CARD`, `SCHIMB_VALUTAR`, `LIMITA_ZILNICA_SETATA`, `PAROLA_SCHIMBATA`, `REVENDICA_REWARD`, `UPGRADE_ABONAMENT`, `DOBANDA_APLICATA`, `TAXA_APLICATA`

---

## Concepte OOP demonstrate

- **Moștenire** — `PersoanaFizica`/`PersoanaJuridica` extind `Client`; `ContCurent`/`ContDeEconomii` extind `Account`; `CardDebit`/`CardCredit` extind `Card`; `FlightReward`/`AccommodationReward` extind `Reward`
- **Polimorfism** — `instanceof` cu pattern matching Java 16+ pentru comportament specific tipului
- **Abstractizare** — clase abstracte și metode abstracte pentru entitățile de bază
- **Încapsulare** — câmpuri private, acces prin metode publice, validări interne
- **Singleton** — `DatabaseConnection` și `CsvAuditService`
- **Excepții ierarhice** — toate excepțiile de business extind `BanKitException`
- **Comparare** — `Client` implementează `Comparable` pentru sortare
