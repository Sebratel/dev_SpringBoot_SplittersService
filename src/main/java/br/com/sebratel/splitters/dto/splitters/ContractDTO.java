package br.com.sebratel.splitters.dto.splitters;

public record ContractDTO (
    Integer id,
    Integer status,
    String statusDescription,
    Integer stage,
    String stageDescription
){}
