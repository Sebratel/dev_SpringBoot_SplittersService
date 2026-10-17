package br.com.sebratel.splitters.dto.splitters;

public record ConnectionDTO( Integer id,
                            String user,
                            String integrationCode,
                            String serviceTag,
                            String equipmentSerialNumber,
                            String mac,
                            String integrationCodeMap,
                            Integer status,
                            ClientDTO client,
                            AddressDTO address,
                            SplitterDTO splitter,
                            ContractDTO contract,
                            AuthenticationAccessPointDTO authenticationAccessPoint)
    {}