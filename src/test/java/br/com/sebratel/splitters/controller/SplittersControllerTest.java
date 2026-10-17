package br.com.sebratel.splitters.controller;

import br.com.sebratel.splitters.BaseTest;
import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarSolicitacaoDeClienteOutputDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.splitters.service.GetConnectionsService;
import br.com.sebratel.splitters.service.ListarOltsService;
import br.com.sebratel.splitters.service.ListarSplittersService;
import br.com.sebratel.splitters.service.ProtocolosEmAndamentoService;
import br.com.sebratel.splitters.service.RecuperarSolicitacoesDeUmUsuarioService;
import br.com.sebratel.splitters.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SplittersController.class)
@AutoConfigureMockMvc(addFilters = false)
class SplittersControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;

    @MockitoBean
    private ListarSplittersService listarSplittersService;

    @MockitoBean
    private ListarOltsService listarOltsService;

    @MockitoBean
    private RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService;

    @MockitoBean
    private GetConnectionsService getConnectionsService;

    @MockitoBean
    private ProtocolosEmAndamentoService protocolosEmAndamentoService;

    @Test
    @DisplayName("Should recover Elleven token")
    void recuperarTokenDoUsuarioIntegradorEllevenSuccess() throws Exception {
        when(recuperarTokenDoUsuarioIntegradorEllevenService.executar()).thenReturn(new RecuperarTokenEllevenOutputDTO("token", 3600, "Bearer", "all"));
        mockMvc.perform(get("/api/v1/splitters/recuperarToken"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should list connections")
    void listarConnectionsSuccess() throws Exception {
        when(getConnectionsService.executar()).thenReturn(new EllevenSplitterResponseDTO<>(true, null, null, null, null));
        mockMvc.perform(get("/api/v1/splitters/listarConnections"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should list splitters")
    void listarSplittersSuccess() throws Exception {
        when(listarSplittersService.executar()).thenReturn(new EllevenSplitterResponseDTO<>(true, null, null, null, null));
        mockMvc.perform(get("/api/v1/splitters/listarSplitters"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should list splitter by ID")
    void listarSplitterPeloIdSuccess() throws Exception {
        when(listarSplittersService.executar(anyLong())).thenReturn(new EllevenSplitterResponseDTO<>(true, null, null, null, null));
        mockMvc.perform(get("/api/v1/splitters/123"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should list splitters paginated")
    void listarSplittersPaginadoSuccess() throws Exception {
        when(listarSplittersService.executar(anyInt(), anyInt())).thenReturn(new EllevenSplitterResponseDTO<>(true, null, null, null, null));
        mockMvc.perform(get("/api/v1/splitters/listarSplitters-paginado")
                        .param("inicio", "0")
                        .param("quantidade", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should list OLTs")
    void listarOltsSuccess() throws Exception {
        when(listarOltsService.executar()).thenReturn(new EllevenSplitterResponseDTO<>(true, null, null, null, null));
        mockMvc.perform(get("/api/v1/splitters/listarOlts"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should recover client requests")
    void recuperarSolicitacoesDeUmClienteSuccess() throws Exception {
        when(recuperarSolicitacoesDeUmUsuarioService.executar(anyString())).thenReturn(new RecuperarSolicitacaoDeClienteOutputDTO(true, null, null, null, null));
        mockMvc.perform(get("/api/v1/splitters/solicitacoes/cliente/123"))
                .andExpect(status().isOk());
    }
}
