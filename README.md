# Proiect

Acest proiect implementează un sistem bancar orientat pe obiecte, cu separare clară între entitățile de domeniu și logica de business.

## Descriere generală

Aplicația pornește din clasa `Main`, care afișează un meniu interactiv în consolă și primește opțiunile utilizatorului. Fluxul aplicației este gestionat de `BanKitService`, care coordonează operațiunile asupra clienților, conturilor, cardurilor și recompenselor.

## Componente OOP principale

- `Client` - clasa de bază pentru clienți
- `PersoanaFizica` și `PersoanaJuridica` - extensii pentru tipuri de clienți cu atribute specifice
- `Account` - abstracție pentru conturi bancare
- `ContCurent` și `ContDeEconomii` - implementări concrete pentru tipuri de cont
- `Card` - model de bază pentru carduri
- `CardDebit` și `CardCredit` - tipuri de carduri cu comportamente distincte
- `Reward` - model de recompense și mecanismul de revendicare
- `TipAbonament`, `TipTranzactie`, `StatusTranzactie`, `ValuteAcceptate` - enumerații care definesc starea și tipurile din domeniu

## Fluxul aplicației

1. `Main` construiește un obiect `BanKitService` și afișează meniul principal.
2. Utilizatorul alege o opțiune numerică și aplicația citește datele necesare din consolă.
3. `BanKitService` execută logica specifică pentru fiecare operațiune:
   - adăugarea unui client
   - deschiderea unui cont bancar
   - emiterea și blocarea cardurilor
   - procesarea plăților și transferurilor
   - afișarea detaliilor și generarea extraselor
   - gestionarea magazinului de recompense și upgrade-ului de abonament
4. Rezultatele operațiunii sunt afișate imediat în consolă.
5. Aplicatia continuă să ruleze până când utilizatorul selectează opțiunea de ieșire.

## Comportament și interacțiuni

- Se folosesc diverse clase de serviciu și entități pentru a păstra datele și regulile de business separate.
- Meniul principal controlează fluxul printr-un `switch` care apelează metode din `BanKitService`.
- Validările se fac la nivelul intrării și se gestionează prin excepții pentru a evita stări incorecte.
- Tipurile concrete de client, cont și card permit extinderea ușoară a comportamentului fără a modifica logica principală.

## Scop

Scopul proiectului este de a demonstra conceptele OOP: moștenire, polimorfism, încapsulare și separarea responsabilităților în contextul unui sistem bancar simplificat.