package org.example;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {}

    public static void initializeTables() {
        Connection con = DatabaseConnection.getInstance().getConnection();
        try (Statement st = con.createStatement()) {

            st.execute("""
                CREATE TABLE IF NOT EXISTS clienti (
                    id_client          INT          PRIMARY KEY AUTO_INCREMENT,
                    adresa             VARCHAR(255) NOT NULL,
                    parola_hash        VARCHAR(64),
                    puncte_rev         INT          NOT NULL DEFAULT 0,
                    data_inrolare      DATE         NOT NULL,
                    abonament          VARCHAR(20)  NOT NULL,
                    nume               VARCHAR(100),
                    prenume            VARCHAR(100),
                    cnp                VARCHAR(13),
                    denumire_companie  VARCHAR(200),
                    cui                VARCHAR(6),
                    reprezentant_legal VARCHAR(200)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS conturi (
                    iban              VARCHAR(24) PRIMARY KEY,
                    tip               VARCHAR(20) NOT NULL,
                    sold              DOUBLE      NOT NULL DEFAULT 0,
                    valuta            VARCHAR(10) NOT NULL,
                    id_titular        INT,
                    taxa_administrare DOUBLE,
                    rata_dobanda      DOUBLE,
                    FOREIGN KEY (id_titular) REFERENCES clienti(id_client) ON DELETE CASCADE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS carduri (
                    numar_card         VARCHAR(16) PRIMARY KEY,
                    tip                VARCHAR(20) NOT NULL,
                    pin                VARCHAR(4)  NOT NULL,
                    is_blocat          BOOLEAN     NOT NULL DEFAULT FALSE,
                    iban_cont          VARCHAR(24),
                    limita_zilnica     DOUBLE,
                    limita_contactless DOUBLE,
                    limita_credit      DOUBLE,
                    datorie_curenta    DOUBLE      DEFAULT 0,
                    FOREIGN KEY (iban_cont) REFERENCES conturi(iban) ON DELETE CASCADE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS tranzactii (
                    id_tranzactie   INT         PRIMARY KEY AUTO_INCREMENT,
                    data_tranzactie DATETIME    NOT NULL,
                    data_modificare DATETIME,
                    suma            DOUBLE      NOT NULL,
                    tip             VARCHAR(30) NOT NULL,
                    iban_sursa      VARCHAR(24),
                    iban_destinatie VARCHAR(24),
                    detalii         TEXT,
                    status_t        VARCHAR(20) NOT NULL,
                    FOREIGN KEY (iban_sursa)      REFERENCES conturi(iban),
                    FOREIGN KEY (iban_destinatie) REFERENCES conturi(iban)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS revendicari (
                    id_client INT,
                    id_oferta VARCHAR(10),
                    PRIMARY KEY (id_client, id_oferta),
                    FOREIGN KEY (id_client) REFERENCES clienti(id_client) ON DELETE CASCADE
                )
                """);

            System.out.println("Tabele inițializate cu succes.");
        } catch (SQLException e) {
            throw new RuntimeException("Eroare la inițializarea tabelelor: " + e.getMessage(), e);
        }
    }

}
