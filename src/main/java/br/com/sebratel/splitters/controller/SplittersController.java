package br.com.sebratel.splitters.controller;

import br.com.sebratel.splitters.dto.splitters.AlertaProtocoloPendenteInputDTO;
import br.com.sebratel.splitters.dto.splitters.ConnectionDTO;
import br.com.sebratel.splitters.dto.splitters.EllevenPaginatedDTO;
import br.com.sebratel.splitters.dto.splitters.EllevenSplitterResponseDTO;
import br.com.sebratel.splitters.dto.splitters.NetworkComponentDTO;
import br.com.sebratel.splitters.dto.splitters.ProtocoloEmAndamentoDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarSolicitacaoDeClienteOutputDTO;
import br.com.sebratel.splitters.dto.splitters.RecuperarTokenEllevenOutputDTO;
import br.com.sebratel.splitters.service.GetConnectionsService;
import br.com.sebratel.splitters.service.ListarOltsService;
import br.com.sebratel.splitters.service.ListarSplittersService;
import br.com.sebratel.splitters.service.ProtocolosEmAndamentoService;
import br.com.sebratel.splitters.service.RecuperarSolicitacoesDeUmUsuarioService;
import br.com.sebratel.splitters.service.RecuperarTokenDoUsuarioIntegradorEllevenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/splitters")
public class SplittersController {

    private final RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService;
    private final ListarSplittersService listarSplittersService;
    private final ListarOltsService listarOltsService;
    private final RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService;
    private final GetConnectionsService getConnectionsService;
    private final ProtocolosEmAndamentoService protocolosEmAndamentoService;

    @Autowired
    public SplittersController(RecuperarTokenDoUsuarioIntegradorEllevenService recuperarTokenDoUsuarioIntegradorEllevenService,
                               ListarSplittersService listarSplittersService,
                               ListarOltsService listarOltsService,
                               RecuperarSolicitacoesDeUmUsuarioService recuperarSolicitacoesDeUmUsuarioService,
                               GetConnectionsService getConnectionsService,
                               ProtocolosEmAndamentoService protocolosEmAndamentoService) {
        this.recuperarTokenDoUsuarioIntegradorEllevenService = recuperarTokenDoUsuarioIntegradorEllevenService;
        this.listarSplittersService = listarSplittersService;
        this.listarOltsService = listarOltsService;
        this.recuperarSolicitacoesDeUmUsuarioService = recuperarSolicitacoesDeUmUsuarioService;
        this.getConnectionsService = getConnectionsService;
        this.protocolosEmAndamentoService = protocolosEmAndamentoService;
    }

    @GetMapping("/recuperarToken")
    public RecuperarTokenEllevenOutputDTO recuperarTokenDoUsuarioIntegradorElleven() {
        return recuperarTokenDoUsuarioIntegradorEllevenService.executar();
    }

    @GetMapping("/listarConnections")
    public EllevenSplitterResponseDTO<List<ConnectionDTO>> listarConnections() {
        return getConnectionsService.executar();
    }


    @GetMapping("/listarSplitters")
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> listarSplitters() {
        return listarSplittersService.executar();
    }

    @GetMapping("/{splitterId}")
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> listarSplitterPeloId(@PathVariable Long splitterId) {
        return listarSplittersService.executar(splitterId);
    }

    @GetMapping("/listarSplitters-paginado")
    public EllevenSplitterResponseDTO<EllevenPaginatedDTO<List<NetworkComponentDTO>>> listarSplittersPaginado(
            @RequestParam(defaultValue = "0") int inicio,
            @RequestParam(defaultValue = "20") int quantidade
    ) {
        return listarSplittersService.executar(inicio, quantidade);
    }

    @GetMapping("/listarOlts")
    public EllevenSplitterResponseDTO<List<NetworkComponentDTO>> listarOlts() {
        return listarOltsService.executar();
    }

    @GetMapping("/solicitacoes/cliente/{clientId}")
    public RecuperarSolicitacaoDeClienteOutputDTO recuperarSolicitacoesDeUmCliente(@PathVariable String clientId) {
            return recuperarSolicitacoesDeUmUsuarioService.executar(clientId);
    }

    /**
     * Cruza uma lista de PPPoEs (pendentes de informação de andar) com os
     * protocolos de instalação/manutenção em aberto no Elleven.
     *
     * POST /api/v1/splitters/alertas-protocolo-pendente
     * Body: { "pppoes": ["user1", "user2", ...] }
     *
     * Retorna apenas os PPPoEs que possuem protocolo aberto agora,
     * enriquecidos com dados do cliente, splitter e tipo do protocolo.
     */
    @PostMapping("/alertas-protocolo-pendente")
    public List<ProtocoloEmAndamentoDTO> alertasProtocoloPendente(
            @RequestBody AlertaProtocoloPendenteInputDTO input) {
        return protocolosEmAndamentoService.buscarAlertas(input.getPppoes());
    }
}
