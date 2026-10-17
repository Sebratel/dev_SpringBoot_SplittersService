package br.com.sebratel.splitters.dto.splitters;

public record AuthenticationAccessPointDTO (

    Integer id,
    String code,
    String title,
    String integrationCodeMap,
    Integer portOlt,
    Integer slotOlt
){}
