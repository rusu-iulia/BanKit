package org.example.dto;

public record RewardDTO(
    String idOferta,
    String numeOferta,
    int costPuncte,
    String tip,
    String info1,
    String info2,
    boolean claimed
) {}
