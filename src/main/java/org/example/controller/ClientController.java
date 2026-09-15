package org.example.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.BanKitService;
import org.example.dto.AccountDTO;
import org.example.dto.CardDTO;
import org.example.dto.ClientDTO;
import org.example.dto.CreateClientRequest;
import org.example.dto.UpdateClientRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final BanKitService service;

    public ClientController(BanKitService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    private Integer getClientId(HttpSession session) {
        return (Integer) session.getAttribute("clientId");
    }

    @GetMapping
    public ResponseEntity<?> getAllClients(HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        List<ClientDTO> clients = service.getToateClientiiDTO();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClient(@PathVariable int id, HttpSession session) {
        Integer clientId = getClientId(session);
        if (!isAdmin(session) && (clientId == null || clientId != id)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        ClientDTO dto = service.getClientDTO(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<?> createClient(@Valid @RequestBody CreateClientRequest req, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        int newId;
        if ("PERSOANA_FIZICA".equalsIgnoreCase(req.tip())) {
            newId = service.adaugaPersoanaFizicaAPI(
                req.adresa(), req.parola(), req.abonament(),
                req.nume(), req.prenume(), req.cnp()
            );
        } else {
            newId = service.adaugaPersoanaJuridicaAPI(
                req.adresa(), req.parola(), req.abonament(),
                req.denumire(), req.cui(), req.reprezentant()
            );
        }
        return ResponseEntity.ok(Map.of("id", newId, "message", "Client creat cu succes."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClient(@PathVariable int id,
                                           @Valid @RequestBody UpdateClientRequest req,
                                           HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        service.atualizaClientAPI(id, req.adresa(), req.abonament());
        return ResponseEntity.ok(Map.of("message", "Client actualizat cu succes."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClient(@PathVariable int id, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        service.stergeClientAPI(id);
        return ResponseEntity.ok(Map.of("message", "Client șters cu succes."));
    }

    @GetMapping("/me/accounts")
    public ResponseEntity<?> getMyAccounts(HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        ClientDTO dto = service.getClientDTO(clientId);
        return ResponseEntity.ok(dto.conturi());
    }

    @GetMapping("/me/cards")
    public ResponseEntity<?> getMyCards(HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        ClientDTO dto = service.getClientDTO(clientId);
        List<CardDTO> allCards = dto.conturi().stream()
                .flatMap(acc -> acc.carduri().stream())
                .toList();
        return ResponseEntity.ok(allCards);
    }
}
