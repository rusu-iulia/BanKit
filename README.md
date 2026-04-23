# 💳 Bankit – Digital Banking Simulator

**Bankit** este o aplicație de tip neo-bank dezvoltată în **Java**, inspirată de funcționalitățile Revolut. Proiectul simulează un ecosistem bancar complet, oferind suport pentru portofele multivalută, gestionarea cardurilor și un sistem de loialitate bazat pe puncte de recompensă (**RevPoints**) și **Cashback**.

---

## 🚀 Funcționalități Principale (Etapa I)

Aplicația implementează următoarele 10 acțiuni fundamentale prin clasa `BankitService`:

1.  **Înrolare Client:** Crearea profilurilor pentru Persoane Fizice și Persoane Juridice.
2.  **Deschidere Conturi:** Generarea de portofele în RON, EUR sau USD.
3.  **Schimb Valutar (FX):** Conversie între conturi folosind rate de schimb fixe.
4.  **Plăți cu Cardul:** Simularea tranzacțiilor la comercianți cu validarea soldului.
5.  **Sistem RevPoints (PF):** Acumularea de puncte pentru vacanțe la fiecare plată.
6.  **Sistem Cashback (PJ):** Restituirea unui procent din tranzacție pentru firme.
7.  **Transfer Peer-to-Peer:** Trimiterea de bani între utilizatori pe baza IBAN-ului.
8.  **Management Carduri:** Activarea sau blocarea cardurilor atașate conturilor.
9.  **Store Recompense:** Schimbul punctelor pe zboruri sau cazări.
10. **Raportare:** Vizualizarea listei de clienți sortată alfabetic și a soldurilor.

---

## 🏗️ Arhitectura Sistemului

### Modelul de Date (Obiecte)
Sistemul utilizează o ierarhie complexă de clase pentru a respecta principiile OOP:
* **Clienți:** `Client` (Abstract), `PersoanaFizica`, `PersoanaJuridica`.
* **Conturi:** `Account` (Abstract), `CheckingAccount`, `SavingsAccount`.
* **Carduri:** `Card` (Abstract), `DebitCard`, `CreditCard`.
* **Lifestyle:** `Reward` (Abstract), `FlightReward`.

### Logica de Calcul (LaTeX)
Pentru conversiile valutare și sistemul de puncte, se folosesc următoarele formule:

* **Conversie Valutară:**
    $$\text{Suma Primita} = (\text{Suma Trimisa} \times \text{Curs Schimb}) - \text{Comision}$$

* **Acumulare RevPoints (Persoane Fizice):**
    $$\text{Puncte Noi} = \frac{\text{Valoare Tranzactie}}{10} \times \text{Multiplicator Abonament}$$

---

## 🛠️ Detalii Tehnice & Gestiunea Datelor

Proiectul utilizează colecții specifice pentru eficiență:
1.  **`TreeSet<Client>`**: Garantează stocarea clienților în ordine alfabetică, facilitând generarea de rapoarte ordonate.
2.  **`HashMap<String, Account>`**: Permite regăsirea instantanee a unui cont după IBAN ($O(1)$), optimizând procesul de transfer de bani.

### Concepte OOP Aplicate:
* **Încapsulare:** Toate atributele sunt `private` sau `protected`, accesate prin Getters/Setters.
* **Moștenire:** Extinderea clasei `Client` pentru a diferenția logica de business între indivizi și companii.
* **Polimorfism:** Tratarea unitară a diferitelor tipuri de conturi în listele de tranzacții.

---

## 📂 Structura Proiectului

```text
src/
 └── ro.bankit.
      ├── model/      # Entitățile (PF, PJ, Conturi, Carduri)
      ├── service/    # Creierul aplicației (BankitService)
      ├── gui/        # Componentele interfeței grafice (Swing)
      └── main/       # Punctul de lansare (Main.java)
