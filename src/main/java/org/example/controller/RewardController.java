package org.example.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.example.BanKitService;
import org.example.Client;
import org.example.Reward;
import org.example.dto.RewardDTO;
import org.example.dto.RedeemRewardRequest;
import org.example.exceptions.BanKitException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
public class RewardController {

    private final BanKitService service;

    public RewardController(BanKitService service) {
        this.service = service;
    }

    private Integer getClientId(HttpSession session) {
        return (Integer) session.getAttribute("clientId");
    }

    @GetMapping
    public ResponseEntity<?> getRewards(HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        List<RewardDTO> rewards = service.getRewardsDTO(clientId);
        return ResponseEntity.ok(rewards);
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@Valid @RequestBody RedeemRewardRequest req, HttpSession session) {
        Integer clientId = getClientId(session);
        if (clientId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Neautentificat."));
        }
        Client client = service.cautaClientDupaIdCuConturi(clientId);
        Reward reward = service.getCatalogRewards().stream()
                .filter(r -> r.getIdOferta().equals(req.idOferta()))
                .findFirst()
                .orElseThrow(() -> new BanKitException("Oferta cu ID-ul " + req.idOferta() + " nu a fost găsită."));
        service.revendicaReward(client, reward);
        return ResponseEntity.ok(Map.of("message", "Recompensă revendicată cu succes."));
    }
}
