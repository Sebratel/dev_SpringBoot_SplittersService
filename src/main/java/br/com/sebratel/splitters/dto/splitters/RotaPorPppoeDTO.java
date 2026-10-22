package br.com.sebratel.splitters.dto.splitters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rota física de uma conexão PPPoE. Só campos de topologia: nome, telefone, e-mail, endereço e
 * coordenadas do cliente <b>não</b> saem deste endpoint (minimização de dados).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotaPorPppoeDTO {
    /** Login PPPoE como cadastrado no ERP. */
    private String pppoe;
    /** Id da conexão ({@code authentication_contracts.id}). */
    private Long authenticationId;
    /** Código do splitter secundário (caixa) onde o cliente está ligado. */
    private String splitterCode;
    /** Título do splitter secundário; a UI extrai slot/PON dele ("... 7 2/4"). */
    private String splitterTitle;
    /** Porta do splitter secundário ocupada pelo cliente. */
    private Integer splitterPort;
    /** Porta do splitter primário que alimenta o secundário. */
    private Integer primaryPort;
    /** Slot derivado do título do splitter (penúltimo número separado por "-"); pode ser nulo. */
    private Integer slot;
    /** Porta PON derivada do título do splitter (último número separado por "-"); pode ser nula. */
    private Integer portaExtraida;
    /** Código do ponto de acesso (AP). */
    private String accessPointCode;
    /** Título do ponto de acesso (AP). */
    private String accessPointTitle;
    /** Código da OLT (concentrador). */
    private String concentratorCode;
    /** Título da OLT (concentrador). */
    private String concentratorTitle;
}
