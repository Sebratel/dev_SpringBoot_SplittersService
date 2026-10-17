package br.com.sebratel.splitters.repository.erp.splitters;

import br.com.sebratel.splitters.model.ErpContract;
import br.com.sebratel.splitters.repository.erp.projections.ProtocoloEmAndamentoProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Busca protocolos de instalação (fibra/telefonia) e manutenção em aberto
 * cujo PPPoE do contrato está na lista de pendências de informação de andar.
 *
 * IDs de instalação incluídos (sem rádio):
 *   12    TEC - Instalação de Fibra
 *   275   TEC - Instalação Telefonia
 *   1011  TEC - Instalação Telefonia com Portabilidade
 *   1014  TEC - Instalação de Fibra – CORPORATIVO
 *   1136  TEC - Instalação de Fibra – Cortesia
 *
 * IDs de manutenção incluídos:
 *   42    Fibra - Manutenção
 *   310   Manutenção / Migração CTO
 *   1061  Infraestrutura - Manutenção de Splitter / CTO
 *   1188  Infra - Manutenção em CTO Avariada
 *   1266  TEC-MANUT-INT-CLIN-REGULARIZACAO
 *   1267  TEC-MANUT-ACESSO-CLI-INCIDENTE/PROBLEMA
 *   1269  TEC-MANUT-ACESSO-CLI-PREVENCAO/PROBLEMA
 *   1270  TEC-MANUT-ACESSO-CLI-INCIDENTE/RECORRENTE
 *   1271  TEC-MANUT-ACESSO-CLI-PREVENCAO/RECORRENTE
 *   1272  TEC-MANUT-ACESSO-CLI-REQUISICAO
 *   1299  TEC-CORP-MANUT-ACESSO-CLI-REQUISICAO
 *   1300  TEC-CORP-MANUT-INCIDENTE/PROBLEMA
 */
@Repository
public interface ProtocoloEmAndamentoRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        SELECT
            ai.protocol                         AS protocolo,
            ac."user"                           AS pppoe,
            p.name                              AS cliente,
            COALESCE(s.title, 'Nao identificado') AS splitter,
            it.title                            AS tipoProtocolo,
            ist.title                           AS statusProtocolo
        FROM erp.assignment_incidents ai
        JOIN erp.incident_types   it  ON it.id  = ai.incident_type_id
        JOIN erp.incident_status  ist ON ist.id = ai.incident_status_id
        JOIN erp.contract_service_tags cst ON cst.id = ai.contract_service_tag_id
        JOIN erp.contracts             c   ON c.id   = cst.contract_id
        JOIN erp.authentication_contracts ac ON ac.contract_id = c.id
        JOIN erp.people                    p  ON p.id = c.client_id
        LEFT JOIN erp.authentication_splitter_ports asp ON asp.authentication_contract_id = ac.id
        LEFT JOIN erp.authentication_splitters       s   ON s.id = asp.authentication_splitter_id
        WHERE it.id IN (
            -- Instalação (fibra/telefonia, sem rádio)
            12, 275, 1011, 1014, 1136,
            -- Manutenção
            42, 310, 1061, 1188, 1266, 1267, 1269, 1270, 1271, 1272, 1299, 1300
        )
        AND ist.title = 'Aberto'
        AND ac."user" IN (:pppoeList)
        ORDER BY p.name
        """, nativeQuery = true)
    List<ProtocoloEmAndamentoProjection> findProtocolosEmAndamento(
            @Param("pppoeList") List<String> pppoeList
    );
}
