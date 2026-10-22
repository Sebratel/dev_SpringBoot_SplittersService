package br.com.sebratel.splitters.repository.erp.splitters;

import br.com.sebratel.splitters.model.ErpContract;
import br.com.sebratel.splitters.repository.erp.projections.RotaPorPppoeProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Rota física (splitter, porta, AP, OLT) só dos PPPoE pedidos, direto no banco do ERP.
 *
 * <p>Substitui o download da base inteira de conexões (~87 mil linhas x 59 colunas, com dados
 * pessoais de todos os clientes) que o painel de eventos do AutoISP fazia para usar 5 campos. A
 * consulta parte do PPPoE e sobe a cadeia
 * {@code conexão -> porta do splitter secundário -> splitter primário (tipo 2, ativo) -> AP -> OLT}
 * — a mesma cadeia que a visão "base de splitters" do painel (Node) percorre de cima para baixo.
 *
 * <p>Diferenças deliberadas em relação a essa visão: (1) ignora porta {@code deleted}, para não
 * devolver uma rota antiga de quem foi remanejado; (2) não traz nenhuma coluna do cliente.
 *
 * <p>O filtro {@code LOWER(TRIM(user))} não usa índice simples em {@code user}; com a lista curta
 * de PPPoE (máx. 500) o custo é um varrimento de {@code authentication_contracts}. Se o EXPLAIN
 * em produção mostrar custo relevante, criar um índice em {@code LOWER(TRIM("user"))}.
 */
@Repository
public interface RotaPorPppoeRepository extends JpaRepository<ErpContract, Long> {

    @Query(value = """
        SELECT
            ac."user"                      AS "pppoe",
            ac.id                          AS "authenticationId",
            ss.code                        AS "splitterCode",
            ss.title                       AS "splitterTitle",
            asp.port                       AS "splitterPort",
            psp.port                       AS "primaryPort",
            CASE
                WHEN parts.parts_count >= 3
                     AND parts.parts[parts.parts_count - 2] ~ '^[0-9]+$'
                THEN parts.parts[parts.parts_count - 2]::int
            END                            AS "slot",
            CASE
                WHEN parts.parts_count >= 2
                     AND parts.parts[parts.parts_count - 1] ~ '^[0-9]+$'
                THEN parts.parts[parts.parts_count - 1]::int
            END                            AS "portaExtraida",
            ap.code                        AS "accessPointCode",
            ap.title                       AS "accessPointTitle",
            conc.code                      AS "concentratorCode",
            conc.title                     AS "concentratorTitle"
        FROM erp.authentication_contracts ac
        JOIN erp.authentication_splitter_ports asp
            ON asp.authentication_contract_id = ac.id
           AND asp.deleted IS FALSE
        JOIN erp.authentication_splitters ss
            ON ss.id = asp.authentication_splitter_id
        CROSS JOIN LATERAL (
            SELECT string_to_array(ss.title, '-')                      AS parts,
                   array_length(string_to_array(ss.title, '-'), 1)     AS parts_count
        ) parts
        JOIN erp.authentication_splitter_ports psp
            ON psp.children_authentication_splitter_id = ss.id
           AND psp.deleted = FALSE
        JOIN erp.authentication_splitters ps
            ON ps.id = psp.authentication_splitter_id
           AND ps.active = TRUE
           AND ps.deleted = FALSE
           AND ps.type = 2
        LEFT JOIN erp.authentication_access_points ap
            ON ap.id = ps.authentication_access_point_id
        LEFT JOIN erp.authentication_concentrators conc
            ON conc.id = ap.authentication_concentrator_id
        WHERE LOWER(TRIM(ac."user")) IN (:pppoeKeys)
        ORDER BY ac.id
        """, nativeQuery = true)
    List<RotaPorPppoeProjection> findRotasPorPppoe(@Param("pppoeKeys") List<String> pppoeKeys);
}
