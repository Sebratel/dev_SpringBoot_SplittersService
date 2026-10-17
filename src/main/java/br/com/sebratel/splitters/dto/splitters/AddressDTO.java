package br.com.sebratel.splitters.dto.splitters;

public record AddressDTO(
        String street,
        String postalCode,
        String number,
        String neighborhood,
        String city,
        Long codeCityId,
        String state,
        String latitude,
        String longitude,
        String addressComplement
) {}