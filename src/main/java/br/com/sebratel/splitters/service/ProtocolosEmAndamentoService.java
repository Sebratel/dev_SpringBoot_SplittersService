package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.ProtocoloEmAndamentoDTO;
import br.com.sebratel.splitters.repository.erp.projections.ProtocoloEmAndamentoProjection;
import br.com.sebratel.splitters.repository.erp.splitters.ProtocoloEmAndamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Cruza os PPPoEs recebidos (lista de pendências de andar) com protocolos
 * de instalação/manutenção em aberto no Elleven ERP.
 *
 * O frontend envia a lista de PPPoEs que estão na fila de pendências;
 * o serviço retorna apenas os que têm protocolo aberto agora.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProtocolosEmAndamentoService {

    private static final Set<Integer> IDS_MANUTENCAO = Set.of(
            42, 310, 1061, 1188, 1266, 1267, 1269, 1270, 1271, 1272, 1299, 1300
    );

    private final ProtocoloEmAndamentoRepository repository;

    public List<ProtocoloEmAndamentoDTO> buscarAlertas(List<String> pppoeList) {
        if (pppoeList == null || pppoeList.isEmpty()) {
            log.debug("Lista de PPPoE vazia — nenhum alerta a verificar.");
            return List.of();
        }

        log.info("Verificando alertas de protocolo em andamento para {} PPPoEs", pppoeList.size());

        List<ProtocoloEmAndamentoProjection> resultados = repository.findProtocolosEmAndamento(pppoeList);

        log.info("Encontrados {} protocolos em andamento que coincidem com pendencias de andar", resultados.size());

        return resultados.stream()
                .map(p -> ProtocoloEmAndamentoDTO.builder()
                        .protocolo(p.getProtocolo())
                        .pppoe(p.getPppoe())
                        .cliente(p.getCliente())
                        .splitter(p.getSplitter())
                        .tipoProtocolo(p.getTipoProtocolo())
                        .categoria(resolverCategoria(p.getTipoProtocolo()))
                        .build())
                .toList();
    }

    /**
     * Classifica o tipo do protocolo em "INSTALACAO" ou "MANUTENCAO"
     * baseado no título do incident_type.
     */
    private String resolverCategoria(String tipoProtocolo) {
        if (tipoProtocolo == null) return "INSTALACAO";
        String upper = tipoProtocolo.toUpperCase();
        if (upper.contains("MANUT") || upper.contains("MIGRA")) return "MANUTENCAO";
        return "INSTALACAO";
    }
}
