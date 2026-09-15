package org.example.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.Account;
import org.example.BanKitService;
import org.example.Client;
import org.example.dto.*;
import org.example.exceptions.ContNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final BanKitService service;

    public AccountController(BanKitService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    private Integer getClientId(HttpSession session) {
        return (Integer) session.getAttribute("clientId");
    }

    @GetMapping("/{iban}")
    public ResponseEntity<?> getAccount(@PathVariable String iban, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null && !isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Account cont = service.cautaContDupaIban(iban);
        if (cont == null) throw new ContNotFoundException(iban);
        AccountDTO dto = service.toAccountDTO(cont);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{iban}/statement")
    public ResponseEntity<?> getStatement(
            @PathVariable String iban,
            @RequestParam String start,
            @RequestParam String end,
            HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null && !isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        List<TransactionDTO> transactions = service.getExtrasDeContDTO(iban, start, end);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@Valid @RequestBody DepositRequest req, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null && !isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        service.depunereBani(req.iban(), req.suma());
        return ResponseEntity.ok(Map.of("message", "Depunere efectuată cu succes."));
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@Valid @RequestBody TransferRequest req, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = service.cautaClientDupaIdCuConturi(clientId);
        service.transferaBani(client, req.ibanSursa(), req.ibanDestinatie(), req.suma(),
                req.detalii() != null ? req.detalii() : "Transfer bancar");
        return ResponseEntity.ok(Map.of("message", "Transfer efectuat cu succes."));
    }

    @PostMapping("/exchange")
    public ResponseEntity<?> exchange(@Valid @RequestBody ExchangeRequest req, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        service.schimbaValuta(req.ibanSursa(), req.ibanDestinatie(), req.suma());
        return ResponseEntity.ok(Map.of("message", "Schimb valutar efectuat cu succes."));
    }

    @PostMapping("/open")
    public ResponseEntity<?> openAccount(@Valid @RequestBody OpenAccountRequest req, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        String iban;
        if ("CONT_CURENT".equalsIgnoreCase(req.tip())) {
            iban = service.deschideContCurentAPI(req.idClient(), req.valuta(), req.sold(), req.taxaAdministrare());
        } else {
            iban = service.deschideContEconomiiAPI(req.idClient(), req.valuta(), req.sold(), req.rataDobanda());
        }
        return ResponseEntity.ok(Map.of("iban", iban, "message", "Cont deschis cu succes."));
    }

    @DeleteMapping("/{iban}")
    public ResponseEntity<?> closeAccount(@PathVariable String iban, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        service.inchideContAPI(iban);
        return ResponseEntity.ok(Map.of("message", "Cont închis cu succes."));
    }
}
