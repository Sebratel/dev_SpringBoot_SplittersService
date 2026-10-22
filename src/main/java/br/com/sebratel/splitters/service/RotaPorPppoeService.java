package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.RotaPorPppoeDTO;
import br.com.sebratel.splitters.exceptions.InvalidMassiveRequestException;
import br.com.sebratel.splitters.repository.erp.projections.RotaPorPppoeProjection;
import br.com.sebratel.splitters.repository.erp.splitters.RotaPorPppoeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Resolve a rota física (splitter, AP, OLT, slot, PON) dos PPPoE informados.
 *
 * <p>O login do AutoISP casa com o do ERP com ou sem domínio ({@code fulano@provedor} e
 * {@code fulano}) e sem distinguir caixa; por isso cada PPPoE gera até duas chaves normalizadas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RotaPorPppoeService {

    /** Limite por chamada: protege o banco e o tamanho da cláusula {@code IN}. */
    public static final int MAX_PPPOES = 500;

    private final RotaPorPppoeRepository repository;

    public List<RotaPorPppoeDTO> buscar(List<String> pppoes) {
        if (pppoes == null || pppoes.isEmpty()) {
            return List.of();
        }
        if (pppoes.size() > MAX_PPPOES) {
            throw new InvalidMassiveRequestException(
                    "Máximo de " + MAX_PPPOES + " PPPoE por consulta (recebido: " + pppoes.size() + ").");
        }

        List<String> keys = normalizar(pppoes);
        if (keys.isEmpty()) {
            return List.of();
        }

        // Só contagens no log: PPPoE identifica cliente.
        List<RotaPorPppoeProjection> rows = repository.findRotasPorPppoe(keys);
        log.info("Rotas por PPPoE: {} informados, {} chaves, {} rotas encontradas",
                pppoes.size(), keys.size(), rows.size());

        return rows.stream().map(RotaPorPppoeService::toDto).toList();
    }

    /** minúsculas + trim + variante sem domínio (parte antes do primeiro '@', se houver). */
    static List<String> normalizar(List<String> pppoes) {
        Set<String> keys = new LinkedHashSet<>();
        for (String raw : pppoes) {
            if (raw == null) continue;
            String key = raw.trim().toLowerCase(Locale.ROOT);
            if (key.isEmpty()) continue;
            keys.add(key);
            int at = key.indexOf('@');
            if (at > 0) keys.add(key.substring(0, at));
        }
        return new ArrayList<>(keys);
    }

    private static RotaPorPppoeDTO toDto(RotaPorPppoeProjection p) {
        return RotaPorPppoeDTO.builder()
                .pppoe(p.getPppoe())
                .authenticationId(p.getAuthenticationId())
                .splitterCode(p.getSplitterCode())
                .splitterTitle(p.getSplitterTitle())
                .splitterPort(p.getSplitterPort())
                .primaryPort(p.getPrimaryPort())
                .slot(p.getSlot())
                .portaExtraida(p.getPortaExtraida())
                .accessPointCode(p.getAccessPointCode())
                .accessPointTitle(p.getAccessPointTitle())
                .concentratorCode(p.getConcentratorCode())
                .concentratorTitle(p.getConcentratorTitle())
                .build();
    }
}
