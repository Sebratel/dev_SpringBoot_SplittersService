package br.com.sebratel.splitters.dto.splitters;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload de entrada para o endpoint de alertas de protocolo pendente.
 * O frontend envia a lista de PPPoEs que constam na fila de pendências
 * de informação de andar, e o BFF cruza com protocolos em aberto no Elleven.
 */
@Data
@NoArgsConstructor
public class AlertaProtocoloPendenteInputDTO {
    /** Lista de logins PPPoE presentes na fila de pendências de andar. */
    private List<String> pppoes;
}
