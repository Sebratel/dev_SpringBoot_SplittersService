package br.com.sebratel.splitters.repository.erp.splitters;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trava o formato da query sem precisar de banco: ela só pode expor colunas de topologia (nada do
 * cliente) e filtrar por parâmetro — nunca por valor concatenado.
 */
class RotaPorPppoeRepositoryQueryTest {

    private static String sql() throws NoSuchMethodException {
        return RotaPorPppoeRepository.class
                .getMethod("findRotasPorPppoe", List.class)
                .getAnnotation(Query.class)
                .value();
    }

    @Test
    @DisplayName("É consulta nativa parametrizada pela lista de PPPoE")
    void filtraPorParametro() throws Exception {
        assertTrue(RotaPorPppoeRepository.class.getMethod("findRotasPorPppoe", List.class)
                .getAnnotation(Query.class).nativeQuery());
        assertTrue(sql().contains("LOWER(TRIM(ac.\"user\")) IN (:pppoeKeys)"));
    }

    @Test
    @DisplayName("Não seleciona dados pessoais do cliente")
    void naoSelecionaDadosPessoais() throws Exception {
        String sql = sql();
        for (String proibido : List.of("erp.people", "cell_phone", "email", "address_complement",
                "client.name", "lat", "lng", "street", "neighborhood")) {
            assertFalse(sql.contains(proibido), "a query não deveria tocar em: " + proibido);
        }
        assertFalse(sql.contains("SELECT *"));
    }

    @Test
    @DisplayName("Sobe a cadeia conexão -> splitter secundário -> primário ativo -> AP -> OLT")
    void percorreACadeiaDeTopologia() throws Exception {
        String sql = sql();
        for (String parte : List.of(
                "erp.authentication_contracts", "erp.authentication_splitter_ports",
                "erp.authentication_splitters", "erp.authentication_access_points",
                "erp.authentication_concentrators", "ps.type = 2", "ps.active = TRUE",
                "ps.deleted = FALSE", "asp.deleted IS FALSE")) {
            assertTrue(sql.contains(parte), "faltou: " + parte);
        }
    }

    @Test
    @DisplayName("Aliases entre aspas batem com os getters da projeção")
    void aliasesBatemComAProjecao() throws Exception {
        String sql = sql();
        for (String alias : List.of("pppoe", "authenticationId", "splitterCode", "splitterTitle",
                "splitterPort", "primaryPort", "slot", "portaExtraida", "accessPointCode",
                "accessPointTitle", "concentratorCode", "concentratorTitle")) {
            assertTrue(sql.contains("AS \"" + alias + "\""), "alias ausente: " + alias);
        }
    }
}
