package org.example.dto;

public record CardDTO(
    String numarCard,
    String numarCardMascat,
    String tip,
    boolean isBlocat,
    double limitaZilnica,
    double limitaContactless,
    double limitaCredit,
    double datorieCurenta,
    String ibanCont
) {}
