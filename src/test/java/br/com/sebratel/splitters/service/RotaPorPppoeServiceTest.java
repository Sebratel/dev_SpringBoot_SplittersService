package br.com.sebratel.splitters.service;

import br.com.sebratel.splitters.dto.splitters.RotaPorPppoeDTO;
import br.com.sebratel.splitters.exceptions.InvalidMassiveRequestException;
import br.com.sebratel.splitters.repository.erp.projections.RotaPorPppoeProjection;
import br.com.sebratel.splitters.repository.erp.splitters.RotaPorPppoeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotaPorPppoeServiceTest {

    @Mock
    private RotaPorPppoeRepository repository;

    @InjectMocks
    private RotaPorPppoeService service;

    @SuppressWarnings("unchecked")
    private List<String> keysSentToRepository() {
        ArgumentCaptor<List<String>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).findRotasPorPppoe(captor.capture());
        return captor.getValue();
    }

    @Test
    @DisplayName("Lista nula ou vazia não consulta o banco")
    void listaNulaOuVaziaNaoConsultaOBanco() {
        assertTrue(service.buscar(null).isEmpty());
        assertTrue(service.buscar(List.of()).isEmpty());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Só nulos e espaços: nada a consultar, não toca no banco")
    void somenteNulosEEspacosNaoConsultaOBanco() {
        assertTrue(service.buscar(Arrays.asList(null, "", "   ")).isEmpty());
        verify(repository, never()).findRotasPorPppoe(anyList());
    }

    @Test
    @DisplayName("Normaliza: minúsculas, trim e variante sem domínio, sem repetir")
    void normalizaChaves() {
        when(repository.findRotasPorPppoe(anyList())).thenReturn(List.of());

        service.buscar(List.of("  Fulano@Provedor.com ", "FULANO", "Beltrano"));

        assertEquals(List.of("fulano@provedor.com", "fulano", "beltrano"), keysSentToRepository());
    }

    @Test
    @DisplayName("'@' na primeira posição não gera variante vazia")
    void arrobaNaPrimeiraPosicao() {
        assertEquals(List.of("@x"), RotaPorPppoeService.normalizar(List.of("@x")));
    }

    @Test
    @DisplayName("Aceita exatamente o limite e rejeita acima dele")
    void respeitaOLimite() {
        List<String> noLimite = IntStream.range(0, RotaPorPppoeService.MAX_PPPOES)
                .mapToObj(i -> "u" + i).toList();
        when(repository.findRotasPorPppoe(anyList())).thenReturn(List.of());
        service.buscar(noLimite);

        List<String> acima = new ArrayList<>(noLimite);
        acima.add("extra");
        InvalidMassiveRequestException ex =
                assertThrows(InvalidMassiveRequestException.class, () -> service.buscar(acima));
        assertTrue(ex.getMessage().contains("500"));
    }

    @Test
    @DisplayName("Mapeia a projeção para o DTO, incluindo campos nulos")
    void mapeiaProjecao() {
        RotaPorPppoeProjection p = mock(RotaPorPppoeProjection.class);
        when(p.getPppoe()).thenReturn("Fulano@prov");
        when(p.getAuthenticationId()).thenReturn(10L);
        when(p.getSplitterCode()).thenReturn("C123");
        when(p.getSplitterTitle()).thenReturn("SPL 7 2/4");
        when(p.getSplitterPort()).thenReturn(3);
        when(p.getPrimaryPort()).thenReturn(5);
        when(p.getSlot()).thenReturn(null);
        when(p.getPortaExtraida()).thenReturn(2);
        when(p.getAccessPointCode()).thenReturn("AP1");
        when(p.getAccessPointTitle()).thenReturn("Ponto 1");
        when(p.getConcentratorCode()).thenReturn("OLT1");
        when(p.getConcentratorTitle()).thenReturn("OLT-X");
        when(repository.findRotasPorPppoe(anyList())).thenReturn(List.of(p));

        List<RotaPorPppoeDTO> out = service.buscar(List.of("fulano@prov"));

        assertEquals(1, out.size());
        RotaPorPppoeDTO dto = out.get(0);
        assertEquals("Fulano@prov", dto.getPppoe());
        assertEquals(10L, dto.getAuthenticationId());
        assertEquals("C123", dto.getSplitterCode());
        assertEquals("SPL 7 2/4", dto.getSplitterTitle());
        assertEquals(3, dto.getSplitterPort());
        assertEquals(5, dto.getPrimaryPort());
        assertNull(dto.getSlot());
        assertEquals(2, dto.getPortaExtraida());
        assertEquals("AP1", dto.getAccessPointCode());
        assertEquals("Ponto 1", dto.getAccessPointTitle());
        assertEquals("OLT1", dto.getConcentratorCode());
        assertEquals("OLT-X", dto.getConcentratorTitle());
    }
}
