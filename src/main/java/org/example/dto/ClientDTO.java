package org.example.dto;

import java.util.List;

public record ClientDTO(
    int id,
    String numeComplet,
    String tip,
    String adresa,
    int puncteRev,
    String abonament,
    String dataInrolare,
    List<AccountDTO> conturi
) {}
