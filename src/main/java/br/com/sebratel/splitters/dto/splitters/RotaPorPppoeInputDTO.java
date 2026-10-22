package br.com.sebratel.splitters.dto.splitters;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload de entrada de {@code POST /api/v1/splitters/rotas-por-pppoe}: os logins PPPoE cuja rota
 * (AP / OLT / slot / PON / splitter) se quer descobrir. Máximo de
 * {@code RotaPorPppoeService.MAX_PPPOES} por chamada.
 */
@Data
@NoArgsConstructor
public class RotaPorPppoeInputDTO {
    /** Logins PPPoE a consultar (com ou sem domínio; a caixa é ignorada). */
    private List<String> pppoes;
}
