package org.example.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.BanKitService;
import org.example.Client;
import org.example.dto.CollectSubscriptionRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final BanKitService service;

    public AdminController(BanKitService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    @PostMapping("/interest")
    public ResponseEntity<?> applyInterest(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        service.aplicaDobanziLunare();
        return ResponseEntity.ok(Map.of("message", "Dobânzi lunare aplicate cu succes."));
    }

    @PostMapping("/fees")
    public ResponseEntity<?> applyFees(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        service.aplicaTaxeAdministrare();
        return ResponseEntity.ok(Map.of("message", "Taxe de administrare aplicate cu succes."));
    }

    @PostMapping("/subscription")
    public ResponseEntity<?> collectSubscription(@Valid @RequestBody CollectSubscriptionRequest req,
                                                   HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        Client client = service.cautaClientDupaIdCuConturi(req.idClient());
        service.incaseazaTaxaAbonament(client, req.suma());
        return ResponseEntity.ok(Map.of("message", "Taxă de abonament incasată cu succes."));
    }
}
