# BanKit — Java Banking Application

A full-stack banking application built for the PAOJ university course. The backend is a Spring Boot REST API backed by a MySQL database; the frontend is a React SPA. The project also retains a Swing ATM simulator accessible through the web UI.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.3.3, Java 21 |
| Database | MySQL (hosted on Railway), raw JDBC — no JPA |
| Frontend | React 18, Vite 5, React Router v6, plain CSS |
| Build | Maven |

---

## Project Structure

```
├── src/main/java/org/example/
│   ├── BanKitApplication.java        # Spring Boot entry point
│   ├── BanKitService.java            # All business logic + DB queries
│   ├── DatabaseConnection.java       # JDBC connection singleton
│   ├── DatabaseInitializer.java      # Auto-creates tables on startup
│   ├── CsvAuditService.java          # Audit log to audit.csv (singleton)
│   ├── BancomatGUI.java              # Swing ATM simulator
│   ├── Client.java / Account.java / Card.java / Reward.java / Tranzactie.java
│   ├── controller/                   # REST controllers
│   │   ├── AuthController.java
│   │   ├── ClientController.java
│   │   ├── AccountController.java
│   │   ├── CardController.java
│   │   ├── RewardController.java
│   │   └── AdminController.java
│   ├── dto/                          # Java records for request/response
│   ├── config/                       # CorsConfig, GlobalExceptionHandler
│   ├── enums/                        # TipAbonament, TipTranzactie, StatusTranzactie, ValuteAcceptate
│   └── exceptions/                   # Custom exception hierarchy
└── frontend/
    └── src/
        ├── api.js                    # All API calls (fetch + credentials: include)
        ├── context/AuthContext.jsx   # Session auth state
        └── pages/
            ├── LoginPage.jsx
            ├── DashboardPage.jsx
            ├── AccountDetailPage.jsx
            ├── TransferPage.jsx
            ├── CardsPage.jsx
            ├── RewardsPage.jsx
            ├── SettingsPage.jsx
            └── AdminPage.jsx
```

---

## Domain Model

All domain classes live in `org.example` (package-private subclasses — controllers use only the public abstract types and DTOs).

| Abstract | Concrete subclasses |
|----------|-------------------|
| `Client` | `PersoanaFizica` (CNP), `PersoanaJuridica` (CUI) |
| `Account` | `ContCurent` (monthly fee), `ContDeEconomii` (interest rate) |
| `Card` | `CardDebit` (contactless limit), `CardCredit` (credit limit + current debt) |
| `Reward` | `FlightReward` (airline, destination), `AccommodationReward` (hotel, nights) |

### Enums

| Enum | Values |
|------|--------|
| `TipAbonament` | `STANDARD`, `PREMIUM`, `BUSINESS_PRO` |
| `TipTranzactie` | `DEPUNERE`, `RETRAGERE_NUMERAR`, `TRANSFER_BANCAR`, `PLATA_CARD`, `SCHIMB_VALUTAR` |
| `StatusTranzactie` | `COMPLETATA`, `ANULATA`, `IN_PROCESARE` |
| `ValuteAcceptate` | `RON`, `EUR`, `USD` |

### Subscription tiers

| Plan | RevPoints multiplier | FX fee | Available to |
|------|---------------------|--------|--------------|
| `STANDARD` | 1x | 1% | Individuals |
| `PREMIUM` | 2x | 0% | Individuals |
| `BUSINESS_PRO` | 0x | 0% | Legal entities |

### Custom exceptions

All business exceptions extend `BanKitException`:

`AuthenticationException`, `CardBlocatException`, `ClientNotFoundException`, `ContNotFoundException`, `FonduriInsuficienteException`, `LimitaCreditDepasitaException`, `LimitaZilnicaDepasitaException`, `PuncteInsuficienteException`

---

## Features

### Client-facing
- View all accounts with live balances
- Account statement (filter by month/year)
- Deposit funds
- Bank transfer between accounts (same currency)
- Card payment — debit reduces balance; credit increases debt; individuals earn RevPoints; legal entities get 1% cashback
- Currency exchange between own accounts (fixed rates: EUR/RON 5.20, USD/RON 4.60, EUR/USD 1.08; fee depends on subscription)
- Change card PIN (requires old PIN)
- Change login password
- Set daily card spending limit
- Rewards shop — redeem RevPoints for flights (Rome, Amsterdam, Barcelona, Paris, London, Dubai, New York) and accommodations (Brașov, Predeal, Sinaia, Mamaia, Antalya, Maldives)
- Swing ATM simulator: balance inquiry, cash withdrawal, cash deposit, PIN change (card auto-blocks after 3 failed attempts)

### Admin panel
- Add clients (individual or legal entity)
- View all clients sorted alphabetically
- Open current or savings accounts (auto-generated IBAN)
- Issue debit or credit cards for a current account
- Block / unblock cards
- View a client's accounts by ID
- Generate account statements
- Apply monthly interest to all savings accounts
- Apply monthly admin fees to all current accounts
- View audit log
- Search client by CNP / CUI
- Collect subscription fee from a client's account

---

## Authentication

HTTP sessions via `HttpSession`. A `clientId` (int) is stored on login. Admins authenticate with a separate password and have `isAdmin = true` in their session.

---

## Audit Log

`CsvAuditService` (singleton) appends a timestamped entry to `audit.csv` for every significant action:

`LOGIN_REUSIT`, `LOGIN_ESUAT`, `ADAUGA_CLIENT`, `DESCHIDE_CONT`, `EMITE_CARD`, `CARD_BLOCAT`, `CARD_DEBLOCAT`, `PIN_SCHIMBAT`, `DEPUNERE_BANI`, `TRANSFER_BANCAR`, `PLATA_CARD`, `SCHIMB_VALUTAR`, `LIMITA_ZILNICA_SETATA`, `PAROLA_SCHIMBATA`, `REVENDICA_REWARD`, `UPGRADE_ABONAMENT`, `DOBANDA_APLICATA`, `TAXA_APLICATA`

---

## OOP Concepts Demonstrated

- **Inheritance** — concrete client, account, card, and reward types extend their abstract base classes
- **Polymorphism** — type-specific behavior via `instanceof` with Java 16+ pattern matching
- **Abstraction** — abstract classes and abstract methods for all core entities
- **Encapsulation** — private fields, public accessors, internal validations
- **Singleton** — `DatabaseConnection` and `CsvAuditService`
- **Exception hierarchy** — all business exceptions extend `BanKitException`
- **Comparable** — `Client` implements `Comparable` for alphabetical sorting

---

## Running Locally

### Prerequisites
- JDK 21+
- Node.js 18+
- Maven 3.8+
- A MySQL database (configure credentials in `src/main/resources/db.properties`)

### Backend

```bash
mvn spring-boot:run
# Starts on http://localhost:8080
```

`DatabaseInitializer` creates all tables automatically on first run.

### Frontend

```bash
cd frontend
npm install
npm run dev
# Starts on http://localhost:5173
# Vite proxies /api requests to localhost:8080
```

Open `http://localhost:5173` in your browser.
