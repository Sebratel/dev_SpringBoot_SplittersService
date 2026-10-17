package br.com.sebratel.splitters.dto.splitters;

public record NetworkComponentDTO(
        Long id,
        String code,
        ValueTextDTO type,
        boolean active,
        String title,
        String description,
        Integer outPorts,
        String integrationCode,
        String integrationCodeMap,
        String oldIntegrationCode,
        NetworkBoxDTO networkBox,
        AddressDTO address,
        OltDTO olt
) {}