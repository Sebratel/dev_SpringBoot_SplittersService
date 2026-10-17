package br.com.sebratel.splitters.dto.splitters;


public record NetworkBoxDTO(
        Long id,
        String code,
        String title,
        boolean active,
        ValueTextDTO status,
        ValueTextDTO type,
        String integrationCode
) {}