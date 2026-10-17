package br.com.sebratel.splitters.repository.erp.projections;

/**
 * Projeção de protocolo de instalação ou manutenção aberto,
 * cruzado com a lista de PPPoE pendentes de informação de andar.
 */
public interface ProtocoloEmAndamentoProjection {
    String getProtocolo();
    String getPppoe();
    String getCliente();
    String getSplitter();
    String getTipoProtocolo();
    String getStatusProtocolo();
}
