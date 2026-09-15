package org.example.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.BanKitService;
import org.example.Client;
import org.example.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final BanKitService service;

    public CardController(BanKitService service) {
        this.service = service;
    }

    private boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }

    private Integer getClientId(HttpSession session) {
        return (Integer) session.getAttribute("clientId");
    }

    @PostMapping("/emit")
    public ResponseEntity<?> emitCard(@Valid @RequestBody EmitCardRequest req, HttpSession session) {
        if (!isAdmin(session)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acces interzis."));
        }
        String numarCard;
        if ("CARD_DEBIT".equalsIgnoreCase(req.tip())) {
            numarCard = service.emiteCardDebitAPI(req.iban(), req.pin(), req.limitaContactless());
        } else {
            numarCard = service.emiteCardCreditAPI(req.iban(), req.pin(), req.limitaCredit());
        }
        return ResponseEntity.ok(Map.of("numarCard", numarCard, "message", "Card emis cu succes."));
    }

    @PostMapping("/{number}/block")
    public ResponseEntity<?> blockCard(@PathVariable String number, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null && !isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = clientId != null ? service.cautaClientDupaIdCuConturi(clientId) : null;
        service.blocheazaCard(number, client);
        return ResponseEntity.ok(Map.of("message", "Cardul a fost blocat cu succes."));
    }

    @PostMapping("/{number}/unblock")
    public ResponseEntity<?> unblockCard(@PathVariable String number, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null && !isAdmin(session)) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = clientId != null ? service.cautaClientDupaIdCuConturi(clientId) : null;
        service.deblocheazaCard(number, client);
        return ResponseEntity.ok(Map.of("message", "Cardul a fost deblocat cu succes."));
    }

    @PutMapping("/{number}/pin")
    public ResponseEntity<?> changePin(@PathVariable String number,
                                        @Valid @RequestBody ChangePinRequest req,
                                        HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = service.cautaClientDupaIdCuConturi(clientId);
        service.schimbaPinCard(number, req.pinVechi(), req.pinNou(), client);
        return ResponseEntity.ok(Map.of("message", "PIN schimbat cu succes."));
    }

    @PutMapping("/{number}/limit")
    public ResponseEntity<?> setDailyLimit(@PathVariable String number,
                                             @Valid @RequestBody SetDailyLimitRequest req,
                                             HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = service.cautaClientDupaIdCuConturi(clientId);
        service.setLimitaZilnicaCard(number, req.limita(), client);
        return ResponseEntity.ok(Map.of("message", "Limita zilnică a fost setată cu succes."));
    }

    @PostMapping("/pay")
    public ResponseEntity<?> cardPayment(@Valid @RequestBody CardPaymentRequest req, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        service.proceseazaPlataCuCard(req.numarCard(), req.suma(), req.comerciant());
        return ResponseEntity.ok(Map.of("message", "Plată procesată cu succes."));
    }
}
