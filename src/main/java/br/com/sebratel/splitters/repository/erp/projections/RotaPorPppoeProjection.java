package br.com.sebratel.splitters.repository.erp.projections;

/**
 * Rota de uma conexão PPPoE (conexão -> porta do splitter secundário -> splitter primário ->
 * ponto de acesso -> OLT). Os nomes dos getters correspondem aos aliases (entre aspas) da query.
 */
public interface RotaPorPppoeProjection {
    String getPppoe();
    Long getAuthenticationId();
    String getSplitterCode();
    String getSplitterTitle();
    Integer getSplitterPort();
    Integer getPrimaryPort();
    Integer getSlot();
    Integer getPortaExtraida();
    String getAccessPointCode();
    String getAccessPointTitle();
    String getConcentratorCode();
    String getConcentratorTitle();
}
