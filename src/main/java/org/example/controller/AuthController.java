package org.example.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.BanKitService;
import org.example.Client;
import org.example.dto.ChangePasswordRequest;
import org.example.dto.ClientDTO;
import org.example.dto.LoginRequest;
import org.example.exceptions.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final BanKitService service;

    @Value("${bankit.admin.password}")
    private String adminPassword;

    public AuthController(BanKitService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpSession session) {
        var client = service.autentificaDupaCod(req.cod(), req.parola());
        session.setAttribute("clientId", client.getIdClient());
        session.removeAttribute("isAdmin");
        ClientDTO dto = service.toClientDTO(client);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/admin-login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> body, HttpSession session) {
        String parola = body.get("parola");
        if (!adminPassword.equals(parola)) {
            throw new AuthenticationException();
        }
        session.setAttribute("isAdmin", true);
        session.removeAttribute("clientId");
        return ResponseEntity.ok(Map.of("role", "admin"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Deconectat cu succes."));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        if (Boolean.TRUE.equals(isAdmin)) {
            return ResponseEntity.ok(Map.of("role", "admin"));
        }
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        ClientDTO dto = service.getClientDTO(clientId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpSession session) {
        Integer clientId = (Integer) session.getAttribute("clientId");
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = service.cautaClientDupaIdCuConturi(clientId);
        service.schimbaParolaClient(client, req.parolaVeche(), req.parolaNou());
        return ResponseEntity.ok(Map.of("message", "Parola a fost schimbată cu succes."));
    }
}
