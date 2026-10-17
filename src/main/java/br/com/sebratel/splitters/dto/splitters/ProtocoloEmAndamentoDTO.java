package br.com.sebratel.splitters.dto.splitters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Protocolo de instalação ou manutenção em aberto cujo cliente/splitter
 * consta na lista de pendências de informação de andar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProtocoloEmAndamentoDTO {
    /** Número do protocolo no Elleven. */
    private String protocolo;
    /** Login PPPoE do contrato. */
    private String pppoe;
    /** Nome do cliente. */
    private String cliente;
    /** Título do splitter ao qual o contrato está vinculado. */
    private String splitter;
    /** Tipo do incidente (ex: "TEC - Instalação de Fibra", "Fibra - Manutenção"). */
    private String tipoProtocolo;
    /** Categoria simplificada: "INSTALACAO" ou "MANUTENCAO". */
    private String categoria;
}
