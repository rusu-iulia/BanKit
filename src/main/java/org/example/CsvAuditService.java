package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvAuditService {
    private static final String FISIER_AUDIT = "audit.csv";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static CsvAuditService instance;

    private CsvAuditService() {
        File f = new File(FISIER_AUDIT);
        if (!f.exists()) {
            try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
                pw.println("nume_actiune,timestamp");
            } catch (IOException e) {
                System.err.println("Nu s-a putut crea fișierul audit CSV: " + e.getMessage());
            }
        }
    }

    public static synchronized CsvAuditService getInstance() {
        if (instance == null) instance = new CsvAuditService();
        return instance;
    }

    public synchronized void logAction(String numeActiune) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FISIER_AUDIT, true))) {
            pw.println(numeActiune + "," + LocalDateTime.now().format(FMT));
        } catch (IOException e) {
            System.err.println("Eroare la scrierea în audit CSV: " + e.getMessage());
        }
    }
}
